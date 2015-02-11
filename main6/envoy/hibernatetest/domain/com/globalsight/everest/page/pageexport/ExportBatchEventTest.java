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

import org.hibernate.criterion.Restrictions;

import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class ExportBatchEventTest extends TestCase
{
	private String DEREK = "derek";

	public void testSave() throws Exception
	{
		ExportBatchEvent exportBatchEvent = new ExportBatchEvent();

		exportBatchEvent.setStartTime(1);
		exportBatchEvent.setEndTime(1);
		exportBatchEvent.setExportType(ExportBatchEvent.INTERIM_PRIMARY);
		exportBatchEvent.setResponsibleUserId(DEREK);
		exportBatchEvent.setTaskId(new Long(1));

		JobImpl job = new JobImpl();
		job.setId(8888);
		exportBatchEvent.setJob(job);

		HibernateUtil.save(exportBatchEvent);

		assertTrue(exportBatchEvent.getId() != PersistentObject.INITIAL_ID);
	}

	public void testFind() throws Exception
	{
		List result = HibernateUtil.getSession().createCriteria(ExportBatchEvent.class)
				.add(Restrictions.like("responsibleUserId", DEREK)).list();

		assertTrue(result.size() != 0);
	}

	public void testDelete() throws Exception
	{
		List result = HibernateUtil.getSession().createCriteria(ExportBatchEvent.class)
				.add(Restrictions.like("responsibleUserId", DEREK)).list();

		HibernateUtil.delete(result);

		List result2 = HibernateUtil.getSession().createCriteria(ExportBatchEvent.class)
				.add(Restrictions.like("responsibleUserId", DEREK)).list();

		assertTrue(result.size() != result2.size());
	}
}
