package com.lifeinide.jsonql.hibernate.search;

import com.lifeinide.jsonql.core.dto.Page;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import java.util.Map;

/**
 * @author lukasz.frankowski@gmail.com
 */
public class DefaultHibernateSearchFilterQueryBuilder<E>
extends HibernateSearchFilterQueryBuilder<E, Page<E>> {

	public DefaultHibernateSearchFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nonnull Class<E> entityClass,
													@Nullable String q, @Nullable Map<String, FieldSearchStrategy> fields) {
		super(entityManager, entityClass, q, fields);
	}

	public DefaultHibernateSearchFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nonnull Class<E> entityClass,
													@Nullable String q) {
		super(entityManager, entityClass, q);
	}

	public DefaultHibernateSearchFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nullable String q,
													@Nonnull Map<String, FieldSearchStrategy> fields) {
		super(entityManager, q, fields);
	}

	public DefaultHibernateSearchFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nullable String q) {
		super(entityManager, q);
	}

}
