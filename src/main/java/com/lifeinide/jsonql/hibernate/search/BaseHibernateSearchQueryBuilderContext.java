package com.lifeinide.jsonql.hibernate.search;

import com.lifeinide.jsonql.core.BaseQueryBuilderContext;
import org.hibernate.search.metadata.IndexedTypeDescriptor;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * @author Lukasz Frankowski
 */
public abstract class BaseHibernateSearchQueryBuilderContext<E> extends BaseQueryBuilderContext {

	protected String query;
	protected Class<E> entityClass;
	protected HibernateSearch hibernateSearch;
	protected QueryBuilder queryBuilder;
	protected IndexedTypeDescriptor indexedTypeDescriptor = null;

	public BaseHibernateSearchQueryBuilderContext(String query, Class<E> entityClass, HibernateSearch hibernateSearch) {
		this.query = query;
		this.entityClass = entityClass;
		this.hibernateSearch = hibernateSearch;
		this.queryBuilder = hibernateSearch.queryBuilder(entityClass);
	}

	public String getQuery() {
		return query;
	}

	public Class<E> getEntityClass() {
		return entityClass;
	}

	public HibernateSearch getHibernateSearch() {
		return hibernateSearch;
	}

	public QueryBuilder getQueryBuilder() {
		return queryBuilder;
	}

	public IndexedTypeDescriptor getIndexedTypeDescriptor() {
		if (indexedTypeDescriptor==null)
			indexedTypeDescriptor = hibernateSearch.fullTextEntityManager().getSearchFactory().getIndexedTypeDescriptor(entityClass);

		return indexedTypeDescriptor;
	}
	
}
