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

package com.globalsight.diplomat.util;

//Diplomat
import com.globalsight.diplomat.javabeans.MessageBean;
import com.globalsight.diplomat.javabeans.SortedNameValuePairsBean;

// SUN java imports
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Vector;

/**
 * @deprecated since 8.6.7
 * @author YorkJin
 *
 */
public class JSPCallingUtility
{
  // JSP used for error messages
  static public final String MESSAGE_JSP_URL = "/cxe/jsp/IWMessage.jsp";

  // JSP used for importing files from IW
  static public final String IMPORT_JOB_JSP_URL = "/cxe/jsp/IWImportJob.jsp";

/**
 * Before calling invokeJSP the HttpServletRequest parameter needs to be
 * set with the relevant JavaBean attributes that are being passed to the JSP
 * specified in jspURL
 */
  static public void invokeJSP(ServletContext context, HttpServletRequest theRequest, HttpServletResponse theResponse, String jspURL)
    throws IOException, ServletException
  {
    RequestDispatcher theDispatcher = context.getRequestDispatcher(jspURL);
    theDispatcher.forward( theRequest, theResponse);
  }

/**
 * Invokes the JSP that presents messages to the user. It is a stand in for the IWMessage.tmpl HTML
 * template from the old GlobalSight code.
 */
  static public void invokeMessageJSP(ServletContext context, HttpServletRequest theRequest, HttpServletResponse theResponse, String theMessage)
    throws IOException, ServletException
  {
    MessageBean aMessageBean = new MessageBean(theMessage);
    theRequest.setAttribute("message", aMessageBean);
    invokeJSP(context, theRequest, theResponse, MESSAGE_JSP_URL);
  }

/**
 * Invokes the JSP that displays the import file confirmation dialog to TeamSite. It is a stand in for the IWImportjob.tmpl HTML
 * template from the old GlobalSight code.
 */
    static public void invokeImportJobJSP(ServletContext context,
                                          HttpServletRequest theRequest,
                                          HttpServletResponse theResponse,
                                          Vector fileProfiles,
                                          SortedNameValuePairsBean aHiddenFieldsBean,
                                          Vector jobNamePrompt)
    throws IOException, ServletException
  {
    theRequest.setAttribute("fileProfiles", fileProfiles);
    theRequest.setAttribute("hiddenFields", aHiddenFieldsBean);
    theRequest.setAttribute("jobNamePrompt", jobNamePrompt);
    invokeJSP(context, theRequest, theResponse, IMPORT_JOB_JSP_URL);
  }
}

