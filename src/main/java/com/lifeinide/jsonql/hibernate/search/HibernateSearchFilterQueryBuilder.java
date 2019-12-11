package com.lifeinide.jsonql.hibernate.search;

import com.lifeinide.jsonql.core.BaseFilterQueryBuilder;
import com.lifeinide.jsonql.core.dto.BasePageableRequest;
import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.enums.QueryCondition;
import com.lifeinide.jsonql.core.enums.QueryConjunction;
import com.lifeinide.jsonql.core.filters.*;
import com.lifeinide.jsonql.core.intr.FilterQueryBuilder;
import com.lifeinide.jsonql.core.intr.Pageable;
import com.lifeinide.jsonql.core.intr.QueryFilter;
import com.lifeinide.jsonql.core.intr.Sortable;
import org.hibernate.search.exception.SearchException;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Implementation of {@link FilterQueryBuilder} for Hibernate Search using local filesystem Lucene index.
 *
 * <h2>Searchable fields implementation</h2>
 *
 * {@link HibernateSearchFilterQueryBuilder} can search entities of given type for a text contained in searchable fields with
 * additional filtering support by custom fields. By default we support following types of searchable fields in entities
 * (this behavior is configurable using one of constructors, though):
 *
 * <ol>
 *     <li>{@link HibernateSearch#FIELD_TEXT} containing all arbitrary texts, which should searchable, but previously be tokenized, analyzed, etc</li>
 *     <li>{@link HibernateSearch#FIELD_ID} string-like ids, which we don't want to analyze and tokenize, but made searchable as-is. For example
 *     	   invoice number "INV201012333" shouldn't be analyzer nor tokenized, but we want to be able to search such number as-is,
 *     	   possibly using wildcard search.</li>
 * </ol>
 *
 * Check the corresponsing constants description to see how to apply them on entity fields.
 *
 * <h2>Filtering fields implementation</h2>
 *
 * Besides above two kinds of searchable fields we also may want to filter the results by some other fields, like in {@code where} clause
 * in SQL. For example we may want to search all articles containing some phrase, but <strong>only</strong> written by some author. In
 * such a scenario we need both to apply search and filtering using Lucene query.
 *
 * Such filter fields should have preserved custom names (excluding {@link HibernateSearch#FIELD_TEXT} and {@link HibernateSearch#FIELD_ID}
 * names) and should usually be defined without analyzers, tokenizers, etc:
 *
 * <pre>{@code
 * @Field(analyze = Analyze.NO, norms = Norms.NO)
 * protected String myField;
 * }</pre>
 *
 * Using such definition you may now filter the results using this field with {@link HibernateSearchFilterQueryBuilder}:
 *
 * <pre>{@code
 * new HibernateSearchFilterQueryBuilder(...).add("myField", SingleValueQueryFilter.of(...)).list(...);
 * }</pre>
 *
 * To check examples of types of fields we support with filtering, please refer {@code HibernateSearchEntity} from test package.
 *
 * <h3>Field bridge for numbers</h3>
 *
 * <p>
 * By default Lucene performs text searchs even for ranges, so fox example "2" > "10" for Lucene. To assert proper numbers filtering
 * for Lucene we prepend all number with zeros, so that "*0002" > "*0010" and we can apply filtering properly. This is why the filtering
 * fields of {@link Number} type should be defined with {@link RangeNumberBridge}:
 * </p>
 *
 * Usage:
 * <pre>{@code
 * @Field(analyze = Analyze.NO, norms = Norms.NO)
 * @FieldBridge(impl = RangeNumberBridge.class)
 * protected Long longVal;
 *
 * @Field(analyze = Analyze.NO, norms = Norms.NO)
 * @FieldBridge(impl = RangeNumberBridge.class)
 * protected BigDecimal decimalVal;
 * }</pre>
 *
 * <h3>Field bridge for entities</h3>
 *
 * In case we want to store in the index the to-one relation, we first need to provide a bridge extending {@link BaseDomainFieldBridge}
 * so that Hibernate Search can convert this entity to and from String representation. For example:
 *
 * <pre>{@code
 * public class DomainFieldBridge extends BaseDomainFieldBridge<IBaseEntity<Long>> {
 *
 *    @Override
 *    public String getEntityIdAsString(IBaseEntity<Long> entity) {
 * 		return String.valueOf(entity.getId());
 *    }
 *
 *    @Override
 *    public boolean isEntity(Object entity) {
 * 		return entity instanceof IBaseEntity;
 *    }
 *
 * }
 * }</pre>
 *
 * Having this field bridge we can use it to store to-one relations in the full text index:
 *
 * <pre>{@code
 * @ManyToOne
 * @Field(analyze = Analyze.NO, norms = Norms.NO)
 * @FieldBridge(impl = DomainFieldBridge.class)
 * protected MyEntity entity;
 * }</pre>
 *
 * @see HibernateSearch How to define searchable fields on entities
 * @author Lukasz Frankowski
 */
public class HibernateSearchFilterQueryBuilder<E, P extends Page<E>>
extends BaseFilterQueryBuilder<E, P, FullTextQuery, HibernateSearchQueryBuilderContext<E>, HibernateSearchFilterQueryBuilder<E, P>> {

	public static final Logger logger = LoggerFactory.getLogger(HibernateSearchFilterQueryBuilder.class);

	protected HibernateSearchQueryBuilderContext<E> context;
	protected Map<String, FieldSearchType> fields;

	/**
	 * @param entityClass Use concrete entity class to search for the specific entities, or {@code Object.class} to do a global search.
	 */
	public HibernateSearchFilterQueryBuilder(EntityManager entityManager, Class<E> entityClass, String q,
											 Map<String, FieldSearchType> fields) {
		this(new HibernateSearch(entityManager), entityClass, q, fields);
	}

	public HibernateSearchFilterQueryBuilder(EntityManager entityManager, Class<E> entityClass, String q) {
		this(entityManager, entityClass, q, defaultSearchFields());
	}

	protected HibernateSearchFilterQueryBuilder(HibernateSearch hibernateSearch, Class<E> entityClass, String q,
												Map<String, FieldSearchType> fields) {
		QueryBuilder queryBuilder = hibernateSearch.queryBuilder(entityClass);
		BooleanJunction<?> booleanJunction = queryBuilder.bool();
		this.context = new HibernateSearchQueryBuilderContext<>(q, entityClass, hibernateSearch, queryBuilder, booleanJunction);

		BooleanJunction<?> fullTextQuery = queryBuilder.bool();

		boolean fieldFound = false;

		for (Map.Entry<String, FieldSearchType> entry: fields.entrySet()) {
			try {
				fullTextQuery.should(entry.getValue().createQuery(queryBuilder, entry.getKey(), q));
				fieldFound = true;
			} catch (Exception e) {
				// silently, this means that some of our full text fields don't exists in the entity
			}

		}

		if (!fieldFound)
			throw new SearchException(String.format("No fulltext fields found for: %s", entityClass.getSimpleName()));

		booleanJunction.must(fullTextQuery.createQuery());

//			try {
//				for (FieldAnalyzer field: HibernateSearch.ALL_FIELDS) {
//
//					if (field.analyzer == null) {
//
//						// field without analyze
//						fullTextQuery.should(queryBuilder.keyword().wildcard().onField(HibernateSearch.FIELD_ID)
//							.matching(makeWild(q)).createQuery());
//
//					} else {
//
//						// fields with analyzer
//						TokenStream tokenStream = hibernateSearch.fullTextEntityManager().getSearchFactory().getAnalyzer(field.analyzer)
//							.tokenStream("", new StringReader(query));
//						tokenStream.reset();
//
//						OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
//						TermAttribute termAttribute = tokenStream.getAttribute(TermAttribute.class);
//
//						while (tokenStream.incrementToken()) {
//							offsetAttribute.startOffset();
//							offsetAttribute.endOffset();
//							String term = termAttribute.term();
//
//							queryBool.should(this.queryBuilder.keyword().wildcard().onField(field)
//								.matching(makeWild(term)).createQuery());
//						}
//
//					}
//
//				}
//
//			} catch (IOException e) {
//				throw new RuntimeException(String.format("Can't analyze query: %s", query), e);
//			}

	}

	@Override
	public HibernateSearchFilterQueryBuilder<E, P> add(String field, DateRangeQueryFilter filter) {
		if (filter!=null) {
			LocalDate from = filter.calculateFrom();
			LocalDate to = filter.calculateTo();

			try {
				Field reflectField = context.getEntityClass().getDeclaredField(field);
				Comparable<?> fromObject = (Comparable<?>) filter.convert(from, reflectField);
				Comparable<?> toObject = (Comparable<?>) filter.convert(to, reflectField);

				if (from!=null)
					context.getBooleanJunction().must(context.getQueryBuilder().range().onField(field).above(fromObject).createQuery());
				if (to!=null)
					context.getBooleanJunction().must(context.getQueryBuilder().range().onField(field).below(toObject).createQuery());
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}

		return this;
	}

	@Override
	public HibernateSearchFilterQueryBuilder<E, P> add(String field, EntityQueryFilter<?> filter) {
		return add(field, (SingleValueQueryFilter<?>) filter);
	}

	@Override
	public HibernateSearchFilterQueryBuilder<E, P> add(String field, ListQueryFilter<? extends QueryFilter> filter) {
		if (filter!=null) {

			List<? extends QueryFilter> filters = filter.getFilters();
			BooleanJunction<?> localJunction = context.getQueryBuilder().bool();

			if (filters!=null && !filters.isEmpty()) {
				for (QueryFilter qf1: filters) {
					if (!(qf1 instanceof SingleValueQueryFilter))
						throw new UnsupportedOperationException("Only QueryFilter is supported with ListQueryFilter for full text search");
					SingleValueQueryFilter<?> qf = (SingleValueQueryFilter<?>) qf1;
					if (QueryConjunction.or.equals(filter.getConjunction()) && filters.size()>1) {
						if (QueryCondition.eq.equals(qf.getCondition()))
							should(localJunction, field, qf.getValue(), true);
						else if (QueryCondition.ge.equals(qf.getCondition()))
							localJunction.should(context.getQueryBuilder().range().onField(field).above(qf.getValue()).createQuery());
						else if (QueryCondition.le.equals(qf.getCondition()))
							localJunction.should(context.getQueryBuilder().range().onField(field).below(qf.getValue()).createQuery());
						else
							throw new UnsupportedOperationException(String.format(
								"Condition: %s is not supported with ListQueryFilter using or conjunction", qf.getCondition()));
					} else {
						if (QueryCondition.eq.equals(qf.getCondition()))
							must(localJunction, field, qf.getValue(), true);
						else if (QueryCondition.ne.equals(qf.getCondition()))
							mustNot(localJunction, field, qf.getValue(), true);
						else if (QueryCondition.ge.equals(qf.getCondition()))
							localJunction.must(context.getQueryBuilder().range().onField(field).above(qf.getValue()).createQuery());
						else if (QueryCondition.le.equals(qf.getCondition()))
							localJunction.must(context.getQueryBuilder().range().onField(field).below(qf.getValue()).createQuery());
						else
							throw new UnsupportedOperationException(String.format(
								"Condition: %s is not supported with ListQueryFilter", qf.getCondition()));
					}
				}
			}

			context.getBooleanJunction().must(localJunction.createQuery());

		}

		return this;

	}

	@Override
	public HibernateSearchFilterQueryBuilder<E, P> add(String field, SingleValueQueryFilter<?> filter) {
		if (filter!=null) {
			if (QueryCondition.eq.equals(filter.getCondition()))
				must(context.getBooleanJunction(), field, filter.getValue(), true);
			else if (QueryCondition.ne.equals(filter.getCondition()))
				mustNot(context.getBooleanJunction(), field, filter.getValue(), true);
			else if (QueryCondition.ge.equals(filter.getCondition()))
				context.getBooleanJunction().must(context.getQueryBuilder().range().onField(field).above(filter.getValue()).createQuery());
			else if (QueryCondition.le.equals(filter.getCondition()))
				context.getBooleanJunction().must(context.getQueryBuilder().range().onField(field).below(filter.getValue()).createQuery());
			else
				throw new IllegalArgumentException(
					String.format("Condition: %s not supported for HibernateSearchFilterQueryBuilder", filter.getCondition()));
		}

		return this;
	}

	@Override
	public HibernateSearchFilterQueryBuilder<E, P> add(String field, ValueRangeQueryFilter<? extends Number> filter) {
		if (filter!=null) {
			if (filter.getFrom()!=null)
				context.getBooleanJunction().must(context.getQueryBuilder().range().onField(field).above(filter.getFrom()).createQuery());
			if (filter.getTo()!=null)
				context.getBooleanJunction().must(context.getQueryBuilder().range().onField(field).below(filter.getTo()).createQuery());
		}

		return this;

	}

	public HibernateSearchFilterQueryBuilder<E, P> must(BooleanJunction<?> booleanJunction, String fieldName, Object expression) {
		must(booleanJunction, fieldName, expression, false);
		return this;
	}

	public HibernateSearchFilterQueryBuilder<E, P> must(BooleanJunction<?> booleanJunction, String fieldName, Object expression,
														boolean ignoreAnalyzer) {
		if (ignoreAnalyzer) {
			booleanJunction.must(context.getQueryBuilder().keyword().onField(fieldName).ignoreAnalyzer().
				matching(expression).createQuery());
		} else {
			booleanJunction.must(context.getQueryBuilder().keyword().onField(fieldName).matching(expression).createQuery());
		}
		return this;
	}

	public HibernateSearchFilterQueryBuilder<E, P> should(BooleanJunction<?> booleanJunction, String fieldName, Object expression) {
		return should(booleanJunction, fieldName, expression, false);
	}

	public HibernateSearchFilterQueryBuilder<E, P> should(BooleanJunction<?> booleanJunction, String fieldName, Object expression,
														boolean ignoreAnalyzer) {
		if (ignoreAnalyzer) {
			booleanJunction.should(context.getQueryBuilder().keyword().onField(fieldName).ignoreAnalyzer().
				matching(expression).createQuery());
		} else {
			booleanJunction.should(context.getQueryBuilder().keyword().onField(fieldName).matching(expression).createQuery());
		}
		return this;
	}

	public HibernateSearchFilterQueryBuilder<E, P> mustNot(BooleanJunction<?> booleanJunction, String fieldName, Object expression) {
		return mustNot(booleanJunction, fieldName, expression, false);
	}

	/**
	 * Helper method to add "must not" field constraint and showing how to work with {@code booleanJunction} and
	 * {@code queryBuilder}.
	 *
	 * @param fieldName      Field name
	 * @param expression     The must not expression
	 * @param ignoreAnalyzer if to ignore analyzer. If analyzer is ignored fields will not be sliced into tokens.
	 */
	public HibernateSearchFilterQueryBuilder<E, P> mustNot(BooleanJunction<?> booleanJunction, String fieldName, Object expression,
														   boolean ignoreAnalyzer) {
		if (ignoreAnalyzer) {
			booleanJunction.must(context.getQueryBuilder().keyword().onField(fieldName).ignoreAnalyzer().
				matching(expression).createQuery()).not().createQuery();
		} else {
			booleanJunction.must(context.getQueryBuilder().keyword().onField(fieldName).matching(expression).createQuery()).not().createQuery();
		}
		return this;
	}

	@Override
	public HibernateSearchQueryBuilderContext<E> context() {
		return context;
	}

	@Override
	public FullTextQuery build() {
		return context.getHibernateSearch().buildQuery(context.getBooleanJunction().createQuery(), context.getEntityClass());
	}

	@SuppressWarnings("unchecked")
	protected <T> Page<T> execute(Pageable pageable, Sortable<?> sortable, Consumer<FullTextQuery> queryCustomizer,
								  Function<List<?>, List<T>> resultsTransformer) {
		if (pageable==null)
			pageable = BasePageableRequest.ofUnpaged();
		if (sortable==null)
			sortable = BasePageableRequest.ofUnpaged();

		FullTextQuery fullTextQuery = build();
		if (queryCustomizer!=null)
			queryCustomizer.accept(fullTextQuery);

		if (logger.isTraceEnabled())
			logger.trace("Executing lucene query: {}", fullTextQuery.toString());

		long count = fullTextQuery.getResultSize();

		if (pageable.isPaged()) {
			fullTextQuery.setFirstResult(pageable.getOffset());
			fullTextQuery.setMaxResults(pageable.getPageSize());
		}

		List<T> resultsList;
		if (resultsTransformer!=null)
			resultsList = resultsTransformer.apply(fullTextQuery.getResultList());
		else
			resultsList = (List<T>) fullTextQuery.getResultList();

		return buildPageableResult(pageable.getPageSize(), pageable.getPage(), count, resultsList);

	}

	@SuppressWarnings("unchecked")
	@Override
	public P list(Pageable pageable, Sortable<?> sortable) {
		return (P) execute(pageable, sortable, null, null);
	}

	public static Map<String, FieldSearchType> defaultSearchFields() {
		Map<String, FieldSearchType> map = new LinkedHashMap<>();
		map.put(HibernateSearch.FIELD_TEXT, FieldSearchType.PHRASE);
		map.put(HibernateSearch.FIELD_ID, FieldSearchType.WILDCARD_TERM);
		return map;
	}

}
