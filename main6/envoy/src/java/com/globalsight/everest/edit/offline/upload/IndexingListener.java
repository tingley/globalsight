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
package com.globalsight.everest.edit.offline.upload;

import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.ling.tm.Indexer;

import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GeneralException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class is responsible for the indexing process.
 */
public class IndexingListener
{
    static private final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            IndexingListener.class);

    static private Indexer c_indexer = null;
    static private TuvManager c_tuvManager = null;

//      static
//      {
//          try
//          {
//              c_indexer = LingServerProxy.getIndexer();
//              c_tuvManager = ServerProxy.getTuvManager();
//          }
//          catch(Exception e)
//          {
//              CATEGORY.error("Failed to get Indexer or TuvManager", e);
//              // throws uncheck exception
//              throw new RuntimeException(e.toString());
//          }
//      }


    /**
     * Performs the indexing process for a given collection of Tuvs.
     * @param p_tuvs - The Tuvs used for indexing.
     */
    static public void performIndexing(List p_tuvs)
    {
        // We don't index job data (legacy Tuv) any more - 5/11/03 SY
//          try
//          {
//              // index all tuvs
//              c_indexer.index(p_tuvs);
//          }
//          catch(Exception e)
//          {
//              CATEGORY.error("Failed to index tuvs ", e);
//              return;
//          }

        // No need to mark tuvs as indexed  here
        //  Stored procedure handles the update to the 'IS_INDEXED' column
        /*
        Iterator it = p_tuvs.iterator();
        while(it.hasNext())
        {
            TuvImpl tuv = (TuvImpl)it.next();

            // all tuvs are already tested that they are not indexed yet
            try
            {
                // localizables don't get indexed
                if(!tuv.isLocalizable())
                {
                    tuv.makeIndexed();
                    // need to update tuv to mark it indexed
                    c_tuvManager.updateTuv(tuv);
                }
            }
            catch(Exception e)
            {
                CATEGORY.error("error occurd while updating tuv:"
                               + tuv.toString(), e);
                return;
            }

        } */
    }
}
