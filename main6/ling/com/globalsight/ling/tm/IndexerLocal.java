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
package com.globalsight.ling.tm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.io.UnsupportedEncodingException;
import javax.naming.NamingException;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.ling.docproc.GlobalsightBreakIterator;
import com.globalsight.ling.docproc.GlobalsightRuleBasedBreakIterator;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexManager;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexManagerException;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexManagerException;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexer;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * Implementation of Indexer interface.
 *
 * @see Indexer
 */
public class IndexerLocal
    implements Indexer
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            IndexerLocal.class.getName());

    private FuzzyIndexManager m_fuzzyIndexManager = null;

    public IndexerLocal()
    {
    }

//      private void init()
//          throws FuzzyIndexManagerException, LingManagerException
//      {
//          if (m_fuzzyIndexManager == null)
//          {
//              try
//              {
//                  m_fuzzyIndexManager =
//                      (FuzzyIndexManager)LingServerProxy.getFuzzyIndexManager();
//              }
//              catch(GeneralException e)
//              {
//                  CATEGORY.error(e, e);
//                  throw new FuzzyIndexManagerException(e);
//              }
//          }
//      }

    /**
     * Index a single segment.
     * @see Indexer#index
     */
    public void index(TuvLing p_tuv)
        throws RemoteException,
               FuzzyIndexManagerException,
               LingManagerException
    {
//          Collection tuvLings;
//          Vector newTokens;

//          init();

//          if (!p_tuv.isLocalizable()) // we don't fuzzy index localizables
//          {
//              GlobalsightBreakIterator ruleBasedBreakIterator =
//                  GlobalsightRuleBasedBreakIterator.getWordInstance(
//                      p_tuv.getGlobalSightLocale().getLocale());

//              FuzzyIndexer fuzzyIndexer = new FuzzyIndexer();

//              // generate fuzzy index
//              newTokens = fuzzyIndexer.index(p_tuv.getFuzzyMatchFormat(),
//                  p_tuv.getId(), p_tuv.getGlobalSightLocale(),
//                  p_tuv.getTuLing().getTmId(), ruleBasedBreakIterator);

//              // save all the tokens from all indexed leverage groups
//              m_fuzzyIndexManager.updateFuzzyIndex(newTokens);
//          }
    }


    /**
     * Index a list of segments using stored procedure
     * @see Indexer#index(java.lang.List)
     */
    public void index(List p_tuvs)
        throws RemoteException,
               FuzzyIndexManagerException,
               LingManagerException
    {
//          if (p_tuvs.size() <= 0)
//          {
//              return;
//          }

//          init();

//          // we assume that all the tuvs are same locale, since it's
//          // indexing by page
//          GlobalsightBreakIterator ruleBasedBreakIterator =
//              GlobalsightRuleBasedBreakIterator.getWordInstance(
//                  ((TuvLing)p_tuvs.get(0)).getGlobalSightLocale().getLocale());

//          Vector paramList = new Vector(p_tuvs.size() * 20);

//          for (Iterator it = p_tuvs.iterator(); it.hasNext(); )
//          {
//              TuvLing tuv = (TuvLing)it.next();

//              if (!tuv.isLocalizable()) // we don't fuzzy index localizables
//              {
//                  FuzzyIndexer fuzzyIndexer = new FuzzyIndexer();

//                  // generate a list of parameters for each tuv
//                  List list = fuzzyIndexer.makeParameterList(
//                      tuv.getFuzzyMatchFormat(),
//                      tuv.getId(), tuv.getGlobalSightLocale(),
//                      tuv.getTuLing().getTmId(), ruleBasedBreakIterator);

//                  paramList.addAll(list);
//              }
//          }

//          // save all the tokens
//          m_fuzzyIndexManager.callIndexingProcedure(paramList);
    }
}
