package com.lifeinide.jsonql.hibernate.search.bridge;

import com.lifeinide.jsonql.hibernate.search.HibernateSearchFilterQueryBuilder;
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.StringBridge;
import org.hibernate.search.bridge.spi.IgnoreAnalyzerBridge;

/**
 * A {@link FieldBridge} to reflect entity objects in lucene index. To be implemented by the application.
 * 
 * @param <E> Entity type.
 * @see HibernateSearchFilterQueryBuilder How to use this bridge in searchable entities
 * @author Lukasz Frankowski
 */
@SuppressWarnings("unchecked")
public abstract class BaseDomainFieldBridge<E> implements FieldBridge, StringBridge, IgnoreAnalyzerBridge {

	public static final String NULL_ID = "[NULL_ID]";

	/**
	 * Returns the entity ID as {@link String} so that the full text search index can store it and make searchable.
	 * @param entity (not null) entity
	 * @return String representation of entity ID.
	 */
	public abstract String getEntityIdAsString(E entity);

	/**
	 * Checks whether the object is of {@link E} entity type.
	 * @param entity (nullable) entity
	 */
	public abstract boolean isEntity(Object entity);

	/**
	 * True if the underlying storage supports nulls, false if not. In case of plain Lucene index usage it should be set to {@code false}.
	 * In case of other storage usage supporting nulls (for example Hibernate Search with ElasticSearch backend) should be set to
	 * {@code true}.
	 */
	protected boolean supportsNulls() {
		return false;
	}

	protected String nullVal() {
		return supportsNulls() ? null : NULL_ID;
	}

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		E model = (E) value;
		if (model == null)
			luceneOptions.addFieldToDocument(name, nullVal(), document);
		else
			luceneOptions.addFieldToDocument(name, getEntityIdAsString(model), document);
	}

	@Override
	public String objectToString(Object object) {
		if (isEntity(object))
			return getEntityIdAsString((E) object);
		if (object == null)
			return nullVal();
		return object.toString();
	}

}
