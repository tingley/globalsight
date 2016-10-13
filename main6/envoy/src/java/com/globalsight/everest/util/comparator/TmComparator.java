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
package com.globalsight.everest.util.comparator;

import java.util.Locale;
import java.util.Vector;

import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.tm.Tm;

/**
 * This class can be used to compare Tm objects.
 */
public class TmComparator
    extends StringComparator
{
    // types of comparison
    static public final int NAME = 0;
    private Vector leverageTMs;
    /**
     * Creates a TmComparator with the given type and locale.
     * If the type is not a valid type, then the default comparison
     * is done by displayName.
     */
    public TmComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public TmComparator(int p_type, Locale p_locale, Vector leverageTMs)
    {
    	super(p_type, p_locale);
    	this.leverageTMs = leverageTMs; 
    }
    /**
     * Performs a comparison of two Tm objects.
     */
    public int compare(Object p_A, Object p_B)
    {
        Tm a = (Tm) p_A;
        Tm b = (Tm) p_B;
        
        String aValue;
        String bValue;
        int rv = 0;
        if(leverageTMs != null)
        {
        	int projectTmIndex_a = getProjectTmIndex(leverageTMs, a.getId());
        	int projectTmIndex_b = getProjectTmIndex(leverageTMs, b.getId());
        	rv = projectTmIndex_a - projectTmIndex_b;
        }
        if(rv == 0)
        {
            switch (m_type)
            {
            case NAME:
                aValue = a.getName();
                bValue = b.getName();
                rv = this.compareStrings(aValue, bValue); 
                break;
            default:
                aValue = a.getName();
                bValue = b.getName();
                rv = this.compareStrings(aValue, bValue);
                break;
            }
        }

        return rv;
    }

	private int getProjectTmIndex(Vector<LeverageProjectTM> leverageTMs, long id) {
		for(int i = 0; i < leverageTMs.size(); i++)
		{
			if(id == leverageTMs.get(i).getProjectTmId()){
				return leverageTMs.get(i).getProjectTmIndex();
			}
		}
		return leverageTMs.size();
	}
}
