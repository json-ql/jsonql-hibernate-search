package com.lifeinide.jsonql.hibernate.search;

/**
 * Represents the way the field is searched with {@link HibernateSearchFilterQueryBuilder}.
 *
 * @author Lukasz Frankowski
 */
public enum FieldSearchStrategy {

	/**
	 * Default field search strategy is just full text search for all words in the passed phrase and is appropriate to search in the
	 * {@link HibernateSearch#FIELD_TEXT} field.
	 */
	DEFAULT,

	/**
	 * This search strategy uses full phrase match with wildcard as last character to find documents with keywords starting from search
	 * string. This strategy is appropriate to lookup in this {@link HibernateSearch#FIELD_ID} field.
	 */
	WILDCARD_PHRASE;

}
