package com.lifeinide.rest.filter.test.hibernate;

import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.intr.FilterQueryBuilder;
import com.lifeinide.jsonql.core.test.BaseQueryBuilderTest;
import com.lifeinide.jsonql.core.test.IBaseEntity;
import com.lifeinide.jsonql.core.test.IEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.Serializable;
import java.util.function.Consumer;

/**
 * @see BaseQueryBuilderTest
 * @author Lukasz Frankowski
 */
public abstract class BaseHibernateJpaTest<
	ID extends Serializable,
	A extends IBaseEntity<ID>,
	E extends IEntity<ID, A>,
	F extends FilterQueryBuilder<E, Page<E>, ?, F>
> extends BaseQueryBuilderTest<EntityManager, ID, A, E, F> {

	public static final String PERSISTENCE_UNIT_NAME = "test-jpa";

	protected EntityManagerFactory entityManagerFactory;

	@BeforeAll
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
	}

	@AfterAll
	public void done() {
		if (entityManagerFactory!=null)
			entityManagerFactory.close();
	}

	protected void doWithEntityManager(Consumer<EntityManager> c) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		try {
			c.accept(entityManager);
		} finally {
			entityManager.getTransaction().commit();
			entityManager.close();
		}
	}

}
