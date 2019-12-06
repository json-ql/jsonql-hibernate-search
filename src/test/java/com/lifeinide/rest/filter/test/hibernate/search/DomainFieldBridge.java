package com.lifeinide.rest.filter.test.hibernate.search;

import com.lifeinide.jsonql.core.test.IBaseEntity;
import com.lifeinide.jsonql.hibernate.search.BaseDomainFieldBridge;

/**
 * @author Lukasz Frankowski
 */
public class DomainFieldBridge extends BaseDomainFieldBridge<IBaseEntity<Long>> {

	@Override
	public String getEntityIdAsString(IBaseEntity<Long> entity) {
		return String.valueOf(entity.getId());
	}

	@Override
	public boolean isEntity(Object entity) {
		return entity instanceof IBaseEntity;
	}
	
}
