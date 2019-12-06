package com.lifeinide.rest.filter.test.hibernate.search;

import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.test.QueryBuilderTestFeature;
import com.lifeinide.jsonql.hibernate.search.DefaultHibernateSearchFilterQueryBuilder;
import com.lifeinide.jsonql.hibernate.search.HibernateSearch;
import com.lifeinide.jsonql.hibernate.search.HibernateSearchFilterQueryBuilder;
import com.lifeinide.rest.filter.test.hibernate.BaseHibernateJpaTest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.function.BiConsumer;

/**
 * @author Lukasz Frankowski
 */
public class HibernateSearchQueryBuilderTest extends BaseHibernateJpaTest<
	Long,
	HibernateSearchAssociatedEntity,
	HibernateSearchEntity,
	HibernateSearchFilterQueryBuilder<HibernateSearchEntity, Page<HibernateSearchEntity>>
> {

	public static final String SEARCHABLE_STRING = "in the middle of";
	public static final String SEARCHABLE_STRING_PART = "middle";

	@BeforeAll
	public void populateData() {
		doWithEntityManager(em -> populateData(em::persist));
	}

	@AfterAll
	@Override
	public void done() {
		super.done();
		try {
			FileUtils.deleteDirectory(FileSystems.getDefault().getPath("tmp").toFile());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean supports(QueryBuilderTestFeature feature) {
		switch (feature) {
			case STRICT_INEQUALITIES:
			case STRICT_DECIMALS:
			case NULLS:
			case SORTING:
				return false;
		}

		return super.supports(feature);
	}

	@Override
	protected HibernateSearchEntity buildEntity(Long previousId) {
		return new HibernateSearchEntity(previousId==null ? 1L : previousId+1);
	}

	@Override
	protected HibernateSearchAssociatedEntity buildAssociatedEntity() {
		return new HibernateSearchAssociatedEntity(1L);
	}

	@Override
	protected void doTest(BiConsumer<EntityManager, HibernateSearchFilterQueryBuilder<HibernateSearchEntity, Page<HibernateSearchEntity>>> c) {
		doWithEntityManager(em -> c.accept(em,
			new HibernateSearchFilterQueryBuilder<>(new HibernateSearch(em), HibernateSearchEntity.class, SEARCHABLE_STRING)));
	}

	@Test
	public void testLocalAndGlobalSearch() {
		doWithEntityManager(em -> {
			DefaultHibernateSearchFilterQueryBuilder<?> qb =
				new DefaultHibernateSearchFilterQueryBuilder<>(new HibernateSearch(em), HibernateSearchEntity.class, SEARCHABLE_STRING_PART);
			Page<?> page = qb.list();
			Assertions.assertEquals(100, page.getCount());

			qb = new DefaultHibernateSearchFilterQueryBuilder<>(new HibernateSearch(em), Object.class, SEARCHABLE_STRING_PART);
			page = qb.list();
			Assertions.assertEquals(101, page.getCount());
		});
	}

}
