package com.lifeinide.jsonql.hibernate.search;

import com.lifeinide.jsonql.core.BaseFilterQueryBuilder;
import com.lifeinide.jsonql.core.dto.BasePageableRequest;
import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.intr.Pageable;
import com.lifeinide.jsonql.core.intr.Sortable;
import org.hibernate.search.jpa.FullTextQuery;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Lukasz Frankowski
 */
public abstract class BaseHibernateSearchFilterQueryBuilder<
	E,
	P extends Page<E>,
	C extends BaseHibernateSearchQueryBuilderContext<E>,
	SELF extends BaseHibernateSearchFilterQueryBuilder<E, P, C, SELF>
> extends BaseFilterQueryBuilder<E, P, FullTextQuery, C, SELF> {

	@SuppressWarnings("unchecked")
	protected <T> Page<T> execute(Pageable pageable, Sortable<?> sortable, Consumer<FullTextQuery> queryCustomizer,
								  Function<List<?>, List<T>> resultsTransformer) {
		if (pageable==null)
			pageable = BasePageableRequest.ofUnpaged();
		if (sortable==null)
			sortable = BasePageableRequest.ofUnpaged();

		FullTextQuery fullTextQuery = build(pageable, sortable);
		if (queryCustomizer!=null)
			queryCustomizer.accept(fullTextQuery);

		if (logger().isTraceEnabled())
			logger().trace("Executing full text query: {}", fullTextQuery.toString());

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

	protected abstract Logger logger();

}
