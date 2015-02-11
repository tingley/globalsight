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

package com.globalsight.everest.page;

import junit.framework.TestCase;

import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class TemplatePartTest extends TestCase
{
	private String derek = "derek";

	private String hql = "from TemplatePart p where p.skeletonString = '" + derek + "'";

	public void testSave() throws Exception
	{
		int resultSize = listSize();

		TemplatePart templatePart = new TemplatePart();

		templatePart.setOrder(1);
		templatePart.setSkeletonClob(derek);
		templatePart.setSkeletonString(derek);

		PageTemplate template = new PageTemplate();
		template.setId(666);
		templatePart.setPageTemplate(template);

		TuImpl tu = new TuImpl();
		tu.setId(777);
		templatePart.setTu(tu);

		HibernateUtil.save(templatePart);

		assertTrue(resultSize != listSize());
	}

	public void testFind() throws Exception
	{
		assertTrue(listSize() != 0);
	}

	public void testDelete() throws Exception
	{
		int size = listSize();

		String hql = "delete " + this.hql;
		HibernateUtil.excute(hql);

		assertTrue(size != listSize());
	}

	private int listSize()
	{
		String hql = "select count(*) " + this.hql;
		return HibernateUtil.count(hql);
	}
}
