/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package com.globalsight.everest.page.pageexport;

import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.globalsight.everest.page.GenericPage;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class ExportingPageTest extends TestCase
{
	private String DEREK = "derek";

	private static long savedId = 0;

	public void testSave() throws Exception
	{
		ExportingPage exportingPage = new ExportingPage();

		exportingPage.setEndTime(1);
		exportingPage.setErrorMessage(DEREK);
		exportingPage.setExportPath(DEREK);
		exportingPage.setComponentPage('Y');
		exportingPage.setState("EXPORTED");

		ExportBatchEvent event = new ExportBatchEvent();
		event.setId(777);

		exportingPage.setExportBatchEvent(event);

		SourcePage page = new SourcePage();
		page.setId(666);
		exportingPage.setPageType(ExportBatchEvent.EXPORT_SOURCE);
		exportingPage.setPage((GenericPage) page);

		HibernateUtil.save(exportingPage);

		assertTrue(exportingPage.getId() != PersistentObject.INITIAL_ID);
		
		savedId = exportingPage.getId();
	}

	public void testFind() throws Exception
	{
		List result = HibernateUtil.getSession().createCriteria(ExportingPage.class)
				.add(Restrictions.like("errorMessage", DEREK)).list();

		assertTrue(result.size() != 0);
	}

	public void testDelete() throws Exception
	{
		List result = HibernateUtil.getSession().createCriteria(ExportingPage.class)
				.add(Restrictions.like("errorMessage", DEREK)).list();

		String hql = "delete from ExportingPage where id=:ID";

		Session session = HibernateUtil.getSession();
		Transaction tx = session.beginTransaction();

		session.createQuery(hql).setLong("ID", savedId).executeUpdate();

		tx.commit();
		session.close();

		List result2 = HibernateUtil.getSession().createCriteria(ExportingPage.class)
				.add(Restrictions.like("errorMessage", DEREK)).list();

		assertTrue(result.size() != result2.size());
	}
}
