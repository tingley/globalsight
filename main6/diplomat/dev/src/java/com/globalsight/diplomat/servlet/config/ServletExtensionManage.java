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

package com.globalsight.diplomat.servlet.config;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;

import java.util.Vector;
import java.util.Enumeration;

import com.globalsight.diplomat.servlet.config.DJ_Extension;
import com.globalsight.diplomat.servlet.config.DJ_FileProfileExtension;
import com.globalsight.diplomat.servlet.config.DJ_FileProfile;
import com.globalsight.diplomat.servlet.config.WrapFileProfileExtensionTable;
import com.globalsight.diplomat.util.Logger;

public class ServletExtensionManage extends HttpServlet
{
    private static final String DISPLAY_PAGE = "/jsp/ExtensionManage.jsp";
    private static final String DELETE = "delete";
    private static final String EXTENSIONS = "extensions";
    private static final String EXTENSION = "extension";
    private static final String NAME = "name";
    private static final String MODE = "mode";
    private static final String CONSTRAINTS = "constraints";
   private static final String CONSTRAINTKEYS = "constraintkeys";

 public ServletExtensionManage() throws ServletException
    {
	try {
	    theLogger.setLogname("DiplomatServlets");
	}catch (IOException e) {
	    throw new ServletException(e);
	}
    }
	/////////////////////////////////////////////////
	public void doGet(HttpServletRequest p_request,
      HttpServletResponse p_response)
      throws ServletException, IOException
	{

	    // intialize
	    initializeObjects(p_request);

	    String mode = p_request.getParameter(MODE);
	    if (mode == null)
	        mode = "";
	    // Did the user clicked on the delete button?
	    if (mode.compareTo(DELETE) == 0)
	    {
            delete (p_request);
	    }

	    // retrieve the latest extensions
	    retrieveExtensions(p_request);

		RequestDispatcher requestDispatcher =
			getServletContext().getRequestDispatcher(DISPLAY_PAGE);
		if (requestDispatcher != null)
			requestDispatcher.forward(p_request, p_response);
  	}

  	/////////////////////////////////////////////////
	public void doPost(HttpServletRequest p_request,
    	HttpServletResponse p_response)
    	throws ServletException, IOException
	{
		doGet(p_request, p_response);
	}

	/////////////////////////////////////////////////
	private void delete (HttpServletRequest p_request)
	{
	    Enumeration extensions = p_request.getParameterNames();
            Vector constraintKeys = new Vector();
            Vector constraintNames = new Vector();
	    while (extensions.hasMoreElements())
	    {
	        String key = (String) extensions.nextElement();
	        if (key.startsWith(EXTENSION))
	        {
	            long extensionID = Long.parseLong(p_request.getParameter(key));
	            DJ_FileProfileExtension extensionObj = new DJ_FileProfileExtension(extensionID, true);
	            Vector conflictList = extensionObj.getList();
	            if (conflictList.size() > 0)
	            {
	                // add the list of dependencies to the session and exit
	                for (int i=0; i<conflictList.size(); ++i)
	                {
	                    WrapFileProfileExtensionTable association =
	                        (WrapFileProfileExtensionTable) conflictList.elementAt(i);
	                    DJ_FileProfile profile = new DJ_FileProfile(association.getFileProfileID());
                            constraintKeys.add(new Long(extensionID));
	                    constraintNames.add(profile.getName());
	                }
	            }
	            else
	            {
	                // delete the extension
	                DJ_Extension x = new DJ_Extension(extensionID);
	                x.deleteEntry();
	            }
	        }
	    }

            HttpSession session = p_request.getSession(false);
            session.setAttribute(CONSTRAINTS, constraintNames);
            session.setAttribute(CONSTRAINTKEYS, constraintKeys);
	}

	/////////////////////////////////////////////////
	private void initializeObjects(HttpServletRequest p_request)
	{
	    HttpSession session = p_request.getSession(true);
	    // initialize the constraints
	    session.setAttribute(CONSTRAINTS, new Vector());
	}

	/////////////////////////////////////////////////
	private void retrieveExtensions(HttpServletRequest p_request)
	{
	    HttpSession session = p_request.getSession(false);
	    // retrieve the extensions
	    Vector extensions = DJ_Extension.retrieveExtensionProfilesByName();
	    // write to the session
	    session.setAttribute(EXTENSIONS, extensions);
	}
    private Logger theLogger = Logger.getLogger();
}
