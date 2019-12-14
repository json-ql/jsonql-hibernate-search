package com.lifeinide.jsonql.hibernate.search;

import org.hibernate.search.query.dsl.BooleanJunction;

/**
 * @author Lukasz Frankowski
 */
public class HibernateSearchQueryBuilderContext<E> extends BaseHibernateSearchQueryBuilderContext<E> {

	protected BooleanJunction booleanJunction;

	public HibernateSearchQueryBuilderContext(String query, Class<E> entityClass, HibernateSearch hibernateSearch) {
		super(query, entityClass, hibernateSearch);
		this.booleanJunction = getQueryBuilder().bool();
	}

	public BooleanJunction getBooleanJunction() {
		return booleanJunction;
	}
	
}
