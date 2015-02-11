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
package com.globalsight.everest.webapp.pagehandler.edit.online;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import org.apache.xalan.xpath.xdom.XercesLiaison;
import org.apache.xalan.xslt.XSLTInputSource;
import org.apache.xalan.xslt.XSLTProcessor;
import org.apache.xalan.xslt.XSLTProcessorFactory;
import org.apache.xalan.xslt.XSLTResultTarget;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;

public class PreviewXMLPageHandler extends PageHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(PreviewXMLPageHandler.class);   

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(true);
        SessionManager sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);

    	EditorState state = (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
    	long srcPageId = state.getSourcePageId().longValue();
    	
    	long targetPageId = state.getTargetPageId().longValue();

        String action = p_request.getParameter("action") == null ? ""
                : (String) p_request.getParameter("action");
        
        String jobId = (String) sessionMgr.getAttribute(WebAppConstants.JOB_ID);
        Job m_job = getJobById(Long.valueOf(jobId).longValue());        

        FileProfile fp = m_job.getFileProfile();
                
        try {
        	File xslFile = getXsl(fp);
        	if (xslFile == null)
        	{
        		CATEGORY.info("No XSL file found.");
        		throw new EnvoyServletException(
       				EnvoyServletException.MSG_FAILED_TO_PREVIEW_XML,
        				"XSL does not exist, please upload an XSL file on file profile page.");
        	}
        
        	if (action != null)
        	{
        		File xmlFile = null;
        		if (action.equals("previewSrc"))
        		{
        			//currentLocale = m_job.getSourceLocale().toString();
        			xmlFile = getSourceXml(srcPageId);
                
        			if (!xmlFile.exists())
        			{
        				CATEGORY.info("The source XML file is missing.");
        				throw new EnvoyServletException(
        					EnvoyServletException.MSG_FAILED_TO_PREVIEW_XML,
        						"The source XML file is missing.");
        			}
        			
        			String codeset = fp.getCodeSet();
        			if (codeset == null || codeset.trim().equals(""))
        			{
        			    codeset = "UTF-8";
        			}
        			
        			byte[] data = FileUtil.readFile(xmlFile, (int) xmlFile.length());
        			String guessEncoding= FileUtil.guessEncoding(xmlFile);
        			String xmldata = null;
        			
        			if (guessEncoding != null)
                    {
                        if (FileUtil.UTF8.equals(guessEncoding))
                        {
                            byte[] newdata = new byte[data.length - 3];
                            newdata = Arrays.copyOfRange(data, 3, data.length);
                            data = newdata;
                        }
                        else if (FileUtil.UTF16BE.equals(guessEncoding)
                                || FileUtil.UTF16LE.equals(guessEncoding))
                        {
                            byte[] newdata = new byte[data.length - 2];
                            newdata = Arrays.copyOfRange(data, 2, data.length);
                            data = newdata;
                        }
                        
                        xmldata = new String(data, guessEncoding);
                    }
        			else
        			{
        			    xmldata = new String(data, codeset);
        			}
        			
        			//String xmldata = FileUtil.readFile(xmlFile, codeset);
        			File newxmlFile = File.createTempFile("GSPreview", ".xml");
        	        FileUtil.writeFile(newxmlFile, xmldata, "UTF-8");
        	        
        	        xmlFile = newxmlFile;
        		}
        		else if (action.equals("previewTar"))
        		{
        			String targetPageName = p_request.getParameter("file");
        			int index = Math.max(targetPageName.indexOf("/"),
        					targetPageName.indexOf("\\"));
                
        			xmlFile = getTargetXml(targetPageId);
        		}

        		FileInputStream xmlInputStream = null;
        		FileInputStream xslInputStream = null;
        		StringWriter sw = null;
        		
        		try
        		{
        			XSLTProcessor processor = XSLTProcessorFactory.getProcessor(new XercesLiaison());
        			
        			sw = new StringWriter();
        			xmlInputStream = new FileInputStream(xmlFile.getAbsolutePath());
        			xslInputStream = new FileInputStream(xslFile.getAbsolutePath());
        			InputStreamReader xmlReader = new InputStreamReader(xmlInputStream, "UTF-8");
        			XSLTResultTarget xslResult = new XSLTResultTarget(sw);
                
        			processor.process(
						new XSLTInputSource(xmlReader),
						new XSLTInputSource(xslInputStream),
						xslResult);
        			
        			String html = sw.getBuffer().toString();
        			p_response.setCharacterEncoding("UTF-8");
        			p_response.getWriter().write(html);
        		}
        		catch (Exception e)
        		{
        			if (CATEGORY.isDebugEnabled()) {
            			CATEGORY.error(e.getMessage());        				
        			}
        			String errMsg = e.getMessage();
        			errMsg = errMsg.replace("\"", "'");
        			throw new EnvoyServletException(
                        EnvoyServletException.MSG_FAILED_TO_PREVIEW_XML,
        					"Failed to transform XML with XSL : " + errMsg);
        		}
        		finally
        		{
        			try
        			{
        				if (xmlInputStream != null) xmlInputStream.close();
        				if (xslInputStream != null) xslInputStream.close();
        				if (sw != null) sw.close();
        			}
        			catch(Exception exp) {}
        		}
        	}
        	else
        	{
        		CATEGORY.error("action is null.");
        		super.invokePageHandler(p_pageDescriptor, p_request, p_response,p_context);
        	}
        }
        catch (Exception ex) {
        	int modeId = EditorConstants.VIEWMODE_DETAIL; //list
        	String contentLocation = null;
        	String menuLocation = null;
        	String menuStr = null;
        	if (action != null && action.equals("previewSrc")) {
        		contentLocation = "/globalsight/ControlServlet?linkName=content&pageName=ED4&srcViewMode=" + modeId;
        		menuLocation = "/globalsight/ControlServlet?linkName=sourceMenu&pageName=ED4&srcViewMode=" + modeId;
        		menuStr = "parent.sourceMenu.document.location=\"" + menuLocation + "\";";
        	} else if (action != null && action.equals("previewTar")) {
        		contentLocation = "/globalsight/ControlServlet?linkName=content&pageName=ED7&trgViewMode=" + modeId;
        		menuLocation = "/globalsight/ControlServlet?linkName=targetMenu&pageName=ED7&trgViewMode=" + modeId;
        		menuStr = "parent.targetMenu.document.location=\"" + menuLocation + "\";";
        	}

        	String errorMsg = ex.getMessage();
        	String msg2 = "Will go to 'list' view.";
        	StringBuffer sb = new StringBuffer("<SCRIPT>");
        	sb.append(menuStr);
        	sb.append("alert(\"" + errorMsg + "\\n" + msg2 + "\");");
        	sb.append("document.location=\"" + contentLocation + "\";");
        	sb.append("</SCRIPT>");
        	p_response.getWriter().write(sb.toString());

        }
    }


    private File getSourceXml(long srcPageId)
    { 	    	
    	File srcFile = null;
    	try
    	{
    		SourcePage srcPage = ServerProxy.getPageManager().getSourcePage(srcPageId);
    		srcFile = srcPage.getFile();
    	}
    	catch(Exception e)
    	{
    		CATEGORY.error("get source xml error: " + e.getMessage());
    	}
    	
    	return srcFile;

    }
    
    private File getTargetXml(long targetPageId)
    { 	    	
    	File targetFile = null;
    	
    	try 
    	{
	    	ExportHelper helper = new ExportHelper();
	    	targetFile = helper.getTargetXmlPage(targetPageId, CxeMessageType.XML_IMPORTED_EVENT);
    	}
    	catch(Exception e)
    	{
    		CATEGORY.error("get target xml error: " + e.getMessage());
    	}
    	
    	return targetFile;
    }
    
    private File getXsl(FileProfile fp)
    {
    	File xslFile = null;
    	
        StringBuffer xslPath = new StringBuffer(AmbFileStoragePathUtils.getXslDir(fp.getId()).getPath())
                .append("/")
                .append(fp.getId())
        		.append("/");
       File xslParent = new File(xslPath.toString());
       if (xslParent.exists())
       {
	   		File[] files = xslParent.listFiles();
			if (files.length > 0)
			{
				String fileName = files[0].getName();
				if (fileName.toLowerCase().endsWith("xsl")
					    || fileName.toLowerCase().endsWith("xml")
						|| fileName.toLowerCase().endsWith("xslt"))
				{
					xslFile = files[0];
				}
			}
				
       }
       
       return xslFile;
    }

    /**
     * Returns Job instance
     * 
     * @param p_jobId
     * @return
     */
    private Job getJobById(long p_jobId)
    {
        Job job = null;
        try
        {
            job = ServerProxy.getJobHandler().getJobById(p_jobId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return job;
    }
    
}
