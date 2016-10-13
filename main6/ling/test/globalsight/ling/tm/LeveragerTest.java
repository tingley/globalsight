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
import java.util.Collection;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.rmi.RemoteException;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm.LeveragerLocal;
import com.globalsight.ling.tm.LeverageProperties;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexManagerException;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.everest.localemgr.LocaleManagerException;

public class LeveragerTest extends TestCase 
{
    private static final Logger CATEGORY =
        Logger.getLogger(
        LeveragerTest.class.getName());
    
    private List m_leverageGroupIds = null;
    private List m_tmIdsToSearch = null;
    private GlobalSightLocale m_sourceLocale = null;
    private GlobalSightLocale m_targetLocale = null;
    private Collection m_targetLocales = null;
    private LeverageProperties m_leverageProperties = null;
    private Collection m_leverageExcludeTypes = null;
    
    private List m_originalLeveragedGroupIds = null;
    private List m_newLeveragedGroupIds = null;       
    
    /**
    */
    public LeveragerTest(String p_name) 
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
        LocaleManagerLocal localeManager = new LocaleManagerLocal();        
        try
        {
            m_sourceLocale = localeManager.getLocaleByString("en_US");
            m_targetLocale = localeManager.getLocaleByString("fr_FR");
        }
        catch(LocaleManagerException e)
        {            
             fail("LocaleManager");
        }
        catch(RemoteException e)
        {            
             fail("LocaleManager");
        }       
      
        m_leverageGroupIds = new ArrayList();
        m_tmIdsToSearch = new ArrayList();
        m_targetLocales = new ArrayList();
        m_leverageProperties = new LeverageProperties();
        m_leverageExcludeTypes = new ArrayList();
 
        m_leverageGroupIds.add(new Long(2));
        m_tmIdsToSearch.add(new Long(1));
        m_targetLocales.add(m_targetLocale);
        m_leverageProperties.setFuzzyThreshold((float)0.0);
        m_leverageProperties.setMaximumReturnedHits(10);        
        // test1
        
        // test2
        m_originalLeveragedGroupIds = new ArrayList();
        m_originalLeveragedGroupIds.add(new Long(1));
        m_newLeveragedGroupIds = new ArrayList();
        m_newLeveragedGroupIds.add(new Long(2));
        // test2
    }
    
    /**
    */
    public static Test suite() 
    {
        return new TestSuite(LeveragerTest.class);
    }
    
    /**
    Test leverager.leverage(...)
    */
    /* Commented out by Andrew because LeveragerLocal.leverage method has
     * changed. */
/*
    public void test1() 
    {
        Exception ex = null;
        LeveragerLocal leverager = null;
        
        try 
        {
          leverager = new LeveragerLocal();
          leverager.leverage(
            m_leverageGroupIds, 
            m_tmIdsToSearch,
            m_sourceLocale,
            m_targetLocales, 
            m_leverageProperties,
            m_leverageExcludeTypes);
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
    
    /**
    Test leverager.leverageForReimport(...)
    */
    public void test2() 
    {/*
        Exception ex = null;
        LeveragerLocal leverager = null;
        
        try 
        {
          leverager = new LeveragerLocal();
          leverager.leverageForReimport(
            m_originalLeveragedGroupIds,
            m_newLeveragedGroupIds,
            m_tmIdsToSearch,
            m_sourceLocale,
            m_targetLocales, 
            m_leverageProperties,
            m_leverageExcludeTypes);
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
    */}
}
