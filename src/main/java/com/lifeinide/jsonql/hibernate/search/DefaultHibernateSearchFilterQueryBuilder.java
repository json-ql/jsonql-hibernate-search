package com.lifeinide.jsonql.hibernate.search;

import com.lifeinide.jsonql.core.dto.Page;

import javax.persistence.EntityManager;
import java.util.Map;

/**
 * @author lukasz.frankowski@gmail.com
 */
public class DefaultHibernateSearchFilterQueryBuilder<E>
extends HibernateSearchFilterQueryBuilder<E, Page<E>> {

	public DefaultHibernateSearchFilterQueryBuilder(EntityManager entityManager, Class<E> entityClass, String q, Map<String, FieldSearchStrategy> fields) {
		super(entityManager, entityClass, q, fields);
	}

	public DefaultHibernateSearchFilterQueryBuilder(EntityManager entityManager, Class<E> entityClass, String q) {
		super(entityManager, entityClass, q);
	}

	public DefaultHibernateSearchFilterQueryBuilder(EntityManager entityManager, String q, Map<String, FieldSearchStrategy> fields) {
		super(entityManager, q, fields);
	}

	public DefaultHibernateSearchFilterQueryBuilder(EntityManager entityManager, String q) {
		super(entityManager, q);
	}

}
