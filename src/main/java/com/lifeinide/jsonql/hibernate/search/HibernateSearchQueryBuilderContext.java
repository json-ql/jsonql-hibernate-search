package com.lifeinide.jsonql.hibernate.search;

import org.hibernate.search.query.dsl.BooleanJunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Lukasz Frankowski
 */
public class HibernateSearchQueryBuilderContext<E> extends BaseHibernateSearchQueryBuilderContext<E> {

	protected BooleanJunction booleanJunction;

	public HibernateSearchQueryBuilderContext(@Nullable String query, @Nonnull Class<E> entityClass,
											  @Nonnull HibernateSearch hibernateSearch) {
		super(query, entityClass, hibernateSearch);
		this.booleanJunction = getQueryBuilder().bool();
	}

	@Nonnull public BooleanJunction getBooleanJunction() {
		return booleanJunction;
	}
	
}
