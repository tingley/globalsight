/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.Serializable;
import java.util.Locale;

import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.util.EnvoyDataComparator;

/**
 * This class can be used to compare Page objects by their external page id
 * (page name) or word count. It is used for sorting this information in a
 * table.
 */
public class PageComparator extends EnvoyDataComparator implements Serializable
{
    private static final long serialVersionUID = -8981564239498281200L;

    // the name of the page
	public static final int EXTERNAL_PAGE_ID = 0;

	// the name of the file profile the page is associated with
	public static final int FILE_PROFILE_NAME = 1;

	// the total word count the page is associated with
	public static final int WORD_COUNT = 2;

	/**
	 * Creates a PageComparator with the given type and locale. If the type is
	 * not a valid type, then the default comparison is done by external page
	 * id.
	 */
	public PageComparator(int p_sortCol, boolean p_sortAsc, Locale p_locale)
	{
		super(p_sortCol, p_locale, p_sortAsc);
	}
	
	public PageComparator(int p_sortCol)
	{
	    super(p_sortCol);
	}

	public Object[] getComparableObjects(Object o1, Object o2, int sortColumn)
	{
		Object objects[] = new Object[2];
		// objects are Page
		if (o1 instanceof Page && o2 instanceof Page)
		{
			objects = getValues(objects, (Page) o1, (Page) o2, sortColumn);
		}
		else
		{
			objects[0] = o1;
			objects[1] = o2;
		}

		return objects;
	}

	private Object[] getValues(Object[] p_objects, Page p1, Page p2,
			int p_sortColumn)
	{
		switch (p_sortColumn)
		{
			default:// should always be first column in page list (external page
					// id - file name)
			case EXTERNAL_PAGE_ID:
				compareByPageName(p_objects, p1, p2);
				break;
			case WORD_COUNT:
				compareByWordCount(p_objects, p1, p2);
				break;
			case FILE_PROFILE_NAME:
				// compareByPlannedDate(p_objects, job1, job2);
				break;
		}

		return p_objects;
	}

	private void compareByPageName(Object[] p_objects, Page p_p1, Page p_p2)
	{
		p_objects[0] = new String(p_p1.getDisplayPageName());
		p_objects[1] = new String(p_p2.getDisplayPageName());
	}

	private void compareByWordCount(Object[] p_objects, Page p_p1, Page p_p2)
	{

		// a target page's word count is retrieved differently than a source
		// page
		if (p_p1 instanceof TargetPage)
		{
			TargetPage tpa = (TargetPage) p_p1;
			TargetPage tpb = (TargetPage) p_p2;

			p_objects[0] = new Integer(tpa.getWordCount().getTotalWordCount());
			p_objects[1] = new Integer(tpb.getWordCount().getTotalWordCount());
		}
		else
		// instance of sourcepage
		{
			SourcePage spa = (SourcePage) p_p1;
			SourcePage spb = (SourcePage) p_p2;
			p_objects[0] = new Integer(spa.getWordCount());
			p_objects[1] = new Integer(spb.getWordCount());
		}
	}

	/***************************************************************************
	 * private String getDataSourceName(SourcePage p_sp) { String dataSourceType =
	 * p_sp.getDataSourceType(); long dataSourceId =
	 * p_sp.getRequest().getDataSourceId();
	 * 
	 * String currentRetString; try { if (dataSourceType.equals("db")) {
	 * currentRetString = getDBProfilePersistenceManager().
	 * getDatabaseProfile(dataSourceId).getName(); } else { currentRetString =
	 * getFileProfilePersistenceManager().
	 * readFileProfile(dataSourceId).getName(); } } catch (Exception e) {
	 * currentRetString = "Unknown"; } return currentRetString; }
	 **************************************************************************/
}
