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

import java.util.Comparator;
import com.globalsight.cxe.entity.dbconnection.DBDispatchImpl;
import java.util.Hashtable;
import java.util.Locale;

/**
* This class can be used to compare DBConnectionImpl objects
*/
public class DBDispatchesComparator extends StringComparator
{
	//types of comparison
	public static final int NAME = 0;
	public static final int PERPAGE = 1;
	public static final int PERBATCH = 2;
	public static final int CONNECTIONS = 3;

    private Hashtable dbConnectionPairs = new Hashtable();

	public DBDispatchesComparator(int p_type,Locale p_locale)
	{
	    super(p_type, p_locale);
	}

	public DBDispatchesComparator(Locale p_locale, Hashtable p_dbConnectionPairs)
	{
	    super(p_locale);
        dbConnectionPairs = p_dbConnectionPairs;
	}

	/**
	* Performs a comparison of two Tm objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		DBDispatchImpl a = (DBDispatchImpl) p_A;
		DBDispatchImpl b = (DBDispatchImpl) p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
		default:
		case NAME:
			aValue = a.toString();
			bValue = b.toString();
			rv = this.compareStrings(aValue,bValue);
			break;
		case PERPAGE:
			long along = a.getRecordsPerPage();
			long blong = b.getRecordsPerPage();
            if (along > blong)
               rv = 1;
            else if (along == blong)
               rv = 0;
            else
               rv = -1;
			break;
		case PERBATCH:
			along = a.getPagesPerBatch();
			blong = b.getPagesPerBatch();
            if (along > blong)
               rv = 1;
            else if (along == blong)
               rv = 0;
            else
               rv = -1;
			break;
		case CONNECTIONS:
			Long aLong = new Long(a.getConnectionId());
			Long bLong = new Long(b.getConnectionId());
            aValue = (String) dbConnectionPairs.get(aLong);
            bValue = (String) dbConnectionPairs.get(bLong);
			rv = this.compareStrings(aValue,bValue);
			break;
		}
		return rv;
	}
}
