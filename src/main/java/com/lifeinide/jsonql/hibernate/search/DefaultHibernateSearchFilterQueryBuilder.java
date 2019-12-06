package com.lifeinide.jsonql.hibernate.search;

import com.lifeinide.jsonql.core.dto.Page;

/**
 * @author lukasz.frankowski@gmail.com
 */
public class DefaultHibernateSearchFilterQueryBuilder<E>
extends HibernateSearchFilterQueryBuilder<E, Page<E>> {

	public DefaultHibernateSearchFilterQueryBuilder(HibernateSearch hibernateSearch, Class<E> entityClass, String q) {
		super(hibernateSearch, entityClass, q);
	}
	
}
