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
import java.io.FileInputStream;
import java.net.URL;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.rmi.RemoteException;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;
import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.tm.IndexerLocal;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexManagerException;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.request.RequestHandler;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.RequestHandlerException;
import com.globalsight.everest.servlet.util.ServerProxy;

public class IndexerViaImportTest extends TestCase 
{
    private static final Logger CATEGORY =
        Logger.getLogger(
        IndexerViaImportTest.class.getName());
    
    private static RequestHandler c_rh = null;
    private static final String EVENT_FLOW_XML = "ImportData/EventFlow.xml";
    private static final String L10N_REQUEST_XML = "ImportData/L10nRequest.xml";
    private static final String L10_PROFILE_FILENAME = "XXXReplaceMeXXX";
    private String m_eventFlowXml = null;
    private String m_l10nRequestXml = null;
    private static boolean c_l10nProfileCreated = true;    
    
    /**
    */
    public IndexerViaImportTest(String p_name) 
    {
        super(p_name);
        m_eventFlowXml = new String();
        m_l10nRequestXml = new String();
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
        if (!c_l10nProfileCreated)
        {
            /* Commented by Andrew because System4SystemTest does not exist.
             * Probably, this will make all the tests fail. */
            //System4SystemTest st = new System4SystemTest("IndexerViaImportTest");
            //st.createProject();
            //st.createL10nProfile();
            c_l10nProfileCreated = true;
        }
        
        if(c_rh == null)
		{
			try
			{
				c_rh = ServerProxy.getRequestHandler();
			} 
            catch(GeneralException ge)
			{
				CATEGORY.error("Couldn't locate the request handler.");
			}                        
		}
        
        // load event flow xml
        try
		{                       
            URL url = this.getClass().getResource(EVENT_FLOW_XML);            
			FileInputStream fileIn = new FileInputStream(url.getPath());
			int i;
			while((i = fileIn.read()) != -1)
            {
                m_eventFlowXml += (char)i;
            }
		} 
        catch(FileNotFoundException e)
		{		
           fail(e.toString());
		} 
        catch(IOException e)
		{		
           fail(e.toString());
		}
        
        // load l10n profile xml
        try
		{                       
            URL url = this.getClass().getResource(L10N_REQUEST_XML);
            String path = url.getPath();
			FileInputStream fileIn = new FileInputStream(path);
			int i;
			while((i = fileIn.read()) != -1)
            {			
                m_l10nRequestXml += (char)i;
            }
		} 
        catch(FileNotFoundException e)
		{		
            fail(e.toString());
		} 
        catch(IOException e)
		{		
            fail(e.toString());
		}
    }
    
    /**
    */
    public static Test suite() 
    {
        return new TestSuite(IndexerViaImportTest.class);
    }
    
    /**
    */
    public void test1() 
    {/*
        Exception ex = null;                        		       
        String exceptionXml = new String();
	       
        // do the import
		try
		{        	                 
            String gxml1 = getGxml("ImportData/test1.html", "ISO-8859-1", "en_US");
        	c_rh.submitRequest(Request.LOCALIZATION_REQUEST,
					   	  gxml1,
                          replaceExternalPageId(m_l10nRequestXml, "ImportData/test1.html"),					   	  
                          m_eventFlowXml,
                          exceptionXml);
		}         		                       
        catch (Exception e) 
        {
            ex = e;
            CATEGORY.error("Exception", e);
        }
        assertNull(ex);*/
    }
    
    /**
    */
    public void test2() 
    {
        Exception ex = null;                        		       
        GeneralException exceptionXml = new GeneralException();
	       
        // do the import
		try
		{        	         
            String gxml1 = getGxml("ImportData/test2.html", "ISO-8859-1", "en_US");
        	c_rh.submitRequest(Request.EXTRACTED_LOCALIZATION_REQUEST,
					   	  gxml1,
                          replaceExternalPageId(m_l10nRequestXml, "ImportData/test2.html"),					   	  
                          m_eventFlowXml,
                          exceptionXml);                   
		}         		                       
        catch (Exception e) 
        {
            ex = e;
            CATEGORY.error("Exception", e);
        }
        assertNull(ex);
    }
    
    /*
    Generate the GXML.
    */
    private String getGxml(
        String p_inputFileName,
        String p_encoding,
        String p_locale
    )
    throws Exception
    {
        URL url = this.getClass().getResource(p_inputFileName);
        
        // autodetect the formatting via the file extension
        DiplomatAPI extractor = new DiplomatAPI();
        extractor.setSourceFile(url.getPath());
        extractor.setEncoding(p_encoding);
        extractor.setLocale(p_locale);
        return extractor.extract();
    }
    
    /*
    */
    private String replaceExternalPageId(String p_l10nProfileXml, String p_newName)
    throws RegExException
    {
        return RegEx.substituteAll(
            p_l10nProfileXml, 
            L10_PROFILE_FILENAME, 
            p_newName);
    }
}
