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
package test.globalsight.ling.tm;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.rmi.RemoteException;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;
import com.globalsight.ling.tm.IndexerLocal;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexManagerException;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.everest.localemgr.LocaleManagerException;

public class IndexerTest extends TestCase 
{
    private static final Logger CATEGORY =
        Logger.getLogger(
        IndexerTest.class.getName());
    
    private List m_leverageGroups1 = null;
    private List m_leverageGroups2 = null;
    private GlobalSightLocale m_globalSightLocale1 = null;
    private long m_tmId1;
    
    /**
     */
    public IndexerTest(String p_name) 
    {
        super(p_name);
    }
    
    /**
     */
    public static void main(String[] args) 
    {
        TestRunner.run(suite());
    }
    
    /**
     */
    public void setUp() 
    {
        // test1
        // currently we assume that a LeverageGroup 1 exists (added by hand)
        m_leverageGroups1 = new ArrayList();
        m_leverageGroups1.add(new Long(1));
        m_leverageGroups2 = new ArrayList();
        m_leverageGroups2.add(new Long(2));
        LocaleManagerLocal localeManager = new LocaleManagerLocal();        
        try
        {
            m_globalSightLocale1 = localeManager.getLocaleByString("en_US");
        }
        catch(LocaleManagerException e)
        { 
            fail("LocaleManager");
        }
        catch(RemoteException e)
        {  
            fail("LocaleManager");
        }       
        m_tmId1 = 1;
        // test1
    }
    
    /**
     */
    public static Test suite() 
    {
        return new TestSuite(IndexerTest.class);
    }
    
    /* Commented out by Andrew because the IndexerLocal.index method has
     * changed.*/
/*
    public void test1() 
    {
        Exception ex = null;
        IndexerLocal indexer = new IndexerLocal();
        
        try 
        {          
            indexer.index(m_leverageGroups1, m_globalSightLocale1, m_tmId1);
            indexer.index(m_leverageGroups2, m_globalSightLocale1, m_tmId1);
        }       
        catch (RemoteException e) 
        {
            ex = e;
            CATEGORY.error("RemoteException", e);
        }
        catch (FuzzyIndexManagerException e) 
        {
            ex = e;
            CATEGORY.error("FuzzyIndexManagerException", e);
        }
        catch (LingManagerException e) 
        {
            ex = e;
            CATEGORY.error("LingManagerException", e);
        }
        catch (Exception e) 
        {
            ex = e;
            CATEGORY.error("Exception", e);
        }
        assertNull(ex);
    }
*/
}
