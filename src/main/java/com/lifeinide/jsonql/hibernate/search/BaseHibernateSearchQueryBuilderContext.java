package com.lifeinide.jsonql.hibernate.search;

import com.lifeinide.jsonql.core.BaseQueryBuilderContext;
import org.hibernate.search.metadata.IndexedTypeDescriptor;
import org.hibernate.search.query.dsl.QueryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Lukasz Frankowski
 */
public abstract class BaseHibernateSearchQueryBuilderContext<E> extends BaseQueryBuilderContext {

	protected String query;
	protected Class<E> entityClass;
	protected HibernateSearch hibernateSearch;
	protected QueryBuilder queryBuilder;
	protected IndexedTypeDescriptor indexedTypeDescriptor = null;

	public BaseHibernateSearchQueryBuilderContext(@Nullable String query, @Nonnull Class<E> entityClass,
												  @Nonnull HibernateSearch hibernateSearch) {
		this.query = query;
		this.entityClass = entityClass;
		this.hibernateSearch = hibernateSearch;
		this.queryBuilder = hibernateSearch.queryBuilder(entityClass);
	}

	@Nullable public String getQuery() {
		return query;
	}

	@Nonnull public Class<E> getEntityClass() {
		return entityClass;
	}

	@Nonnull public HibernateSearch getHibernateSearch() {
		return hibernateSearch;
	}

	@Nonnull public QueryBuilder getQueryBuilder() {
		return queryBuilder;
	}

	@Nonnull public IndexedTypeDescriptor getIndexedTypeDescriptor() {
		if (indexedTypeDescriptor==null)
			indexedTypeDescriptor = hibernateSearch.fullTextEntityManager().getSearchFactory().getIndexedTypeDescriptor(entityClass);

		return indexedTypeDescriptor;
	}
	
}
