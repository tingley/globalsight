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
package com.globalsight.everest.persistence.snippet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.globalsight.everest.snippet.SnippetImpl;

/**
 * This class converts a list of snippet names (the result from a
 * ReportQuery) to an ArrayList.
 */
public class SnippetQueryResultHandler
{
    static public Collection handleResult(Collection p_collection)
    {
        ArrayList result = new ArrayList();

		for (Iterator it = p_collection.iterator(); it.hasNext(); )
        {
		    SnippetImpl row = (SnippetImpl)it.next();
            result.add(row.getName());
        }

        return result;
    }
}
