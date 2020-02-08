package com.lifeinide.jsonql.hibernate.search.test;

import com.lifeinide.jsonql.core.test.IJsonQLTestEntity;
import com.lifeinide.jsonql.core.test.IJsonQLTestParentEntity;
import com.lifeinide.jsonql.core.test.JsonQLTestEntityEnum;
import com.lifeinide.jsonql.hibernate.search.HibernateSearch;
import com.lifeinide.jsonql.hibernate.search.bridge.BigDecimalRangeBridge;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Lukasz Frankowski
 */
@Entity
@Indexed
public class HibernateSearchEntity implements IJsonQLTestEntity<Long>, IJsonQLTestParentEntity<Long, HibernateSearchAssociatedEntity> {

	@Id private Long id;

	@Field(name = HibernateSearch.FIELD_TEXT, store = Store.YES)
	@Analyzer(impl = EnglishAnalyzer.class)
	protected String q = HibernateSearchQueryBuilderTest.SEARCHABLE_STRING;

	@Field(analyze = Analyze.NO, norms = Norms.NO, store = Store.YES)
	protected String stringVal;

	@Field(analyze = Analyze.NO, norms = Norms.NO, store = Store.YES)
	protected boolean booleanVal;

	@Field(analyze = Analyze.NO, norms = Norms.NO, store = Store.YES)
	protected Long longVal;

	@Field(analyze = Analyze.NO, norms = Norms.NO, store = Store.YES)
	@FieldBridge(impl = BigDecimalRangeBridge.class)
	protected BigDecimal decimalVal;

	@Field(analyze = Analyze.NO, norms = Norms.NO, store = Store.YES)
	protected LocalDate dateVal;

	@Enumerated(EnumType.STRING)
	@Field(analyze = Analyze.NO, norms = Norms.NO, store = Store.YES)
	protected JsonQLTestEntityEnum enumVal;

	@ManyToOne
	@Field(analyze = Analyze.NO, norms = Norms.NO, store = Store.YES)
	@FieldBridge(impl = DomainFieldBridge.class)
	protected HibernateSearchAssociatedEntity entityVal;

	public HibernateSearchEntity() {
	}

	public HibernateSearchEntity(Long id) {
		this.id = id;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	@Override
	public String getStringVal() {
		return stringVal;
	}

	@Override
	public void setStringVal(String stringVal) {
		this.stringVal = stringVal;
	}

	@Override
	public boolean isBooleanVal() {
		return booleanVal;
	}

	@Override
	public void setBooleanVal(boolean booleanVal) {
		this.booleanVal = booleanVal;
	}

	@Override
	public Long getLongVal() {
		return longVal;
	}

	@Override
	public void setLongVal(Long longVal) {
		this.longVal = longVal;
	}

	@Override
	public BigDecimal getDecimalVal() {
		return decimalVal;
	}

	@Override
	public void setDecimalVal(BigDecimal decimalVal) {
		this.decimalVal = decimalVal;
	}

	@Override
	public LocalDate getDateVal() {
		return dateVal;
	}

	@Override
	public void setDateVal(LocalDate dateVal) {
		this.dateVal = dateVal;
	}

	@Override
	public JsonQLTestEntityEnum getEnumVal() {
		return enumVal;
	}

	@Override
	public void setEnumVal(JsonQLTestEntityEnum enumVal) {
		this.enumVal = enumVal;
	}

	@Override
	public HibernateSearchAssociatedEntity getEntityVal() {
		return entityVal;
	}

	@Override
	public void setEntityVal(HibernateSearchAssociatedEntity entityVal) {
		this.entityVal = entityVal;
	}
	
}
