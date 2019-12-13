package com.lifeinide.jsonql.hibernate.search;

import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * @author Lukasz Frankowski
 */
public class HibernateSearchQueryBuilderContext<E> extends BaseHibernateSearchQueryBuilderContext<E> {

	protected QueryBuilder queryBuilder;
	protected BooleanJunction booleanJunction;

	public HibernateSearchQueryBuilderContext(String query, Class<E> entityClass, HibernateSearch hibernateSearch,
											  QueryBuilder queryBuilder, BooleanJunction booleanJunction) {
		super(query, entityClass, hibernateSearch);
		this.queryBuilder = queryBuilder;
		this.booleanJunction = booleanJunction;
	}

	public QueryBuilder getQueryBuilder() {
		return queryBuilder;
	}

	public BooleanJunction getBooleanJunction() {
		return booleanJunction;
	}
	
}
