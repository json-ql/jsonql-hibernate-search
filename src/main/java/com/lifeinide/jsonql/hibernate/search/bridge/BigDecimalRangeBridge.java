package com.lifeinide.jsonql.hibernate.search.bridge;

import com.lifeinide.jsonql.core.filters.SingleValueQueryFilter;
import com.lifeinide.jsonql.core.filters.ValueRangeQueryFilter;
import com.lifeinide.jsonql.hibernate.search.HibernateSearchFilterQueryBuilder;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.NumberBridge;

import java.math.BigDecimal;

/**
 * A {@link FieldBridge} used to store {@link BigDecimal} values so that they are searchable using {@link SingleValueQueryFilter} and
 * {@link ValueRangeQueryFilter}.
 *
 * @see HibernateSearchFilterQueryBuilder How to use this bridge in searchable entities
 * @author Lukasz Frankowski
 */
public class BigDecimalRangeBridge extends NumberBridge {

	public static final int NUMBER_SIZE = 20;
	public static final String ZEROS_PAD_TEMPLATE = "00000000000000000000";

	protected String padZeros(String s) {
		s = ZEROS_PAD_TEMPLATE + s;
		if (s.length() - NUMBER_SIZE > 0)
			return s.substring(s.length() - NUMBER_SIZE);
		return s;
	}

	@Override
	public String objectToString(Object object) {
		if (object==null)
			return null;

		return padZeros(super.objectToString(object));
	}

	@Override
	public Object stringToObject(String stringValue) {
		if (stringValue==null)
			return null;

		return Long.valueOf(stringValue);
	}
	
}
