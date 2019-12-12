package com.lifeinide.jsonql.hibernate.search;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.engine.spi.QueryDescriptor;

import javax.persistence.EntityManager;

import static org.hibernate.search.util.StringHelper.*;

/**
 * Hibernate search helper.
 *
 * @see HibernateSearchFilterQueryBuilder
 * @author Lukasz Frankowski
 */
public class HibernateSearch {

	public static final String FIELD_TEXT = "text";
	public static final String FIELD_ID = "textid";

	protected EntityManager entityManager;

	public HibernateSearch(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public FullTextEntityManager fullTextEntityManager() {
		return Search.getFullTextEntityManager(entityManager);
	}

	public QueryBuilder queryBuilder(Class entityClass) {
		return fullTextEntityManager().getSearchFactory().buildQueryBuilder().forEntity(entityClass).get();
	}

	public FullTextQuery buildQuery(Query query, Class entityClass) {
		return fullTextEntityManager().createFullTextQuery(query, entityClass);
	}

	public FullTextQuery buildQuery(QueryDescriptor query, Class entityClass) {
		return fullTextEntityManager().createFullTextQuery(query, entityClass);
	}

	public static String makeWild(String s) {
		if (isEmpty(s))
			return s;
		if (s.endsWith("*"))
			return s;
		return s+"*";
	}

}
