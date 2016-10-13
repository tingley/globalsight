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

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.DriverManager;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;

import java.util.Vector;

import com.globalsight.diplomat.servlet.config.DJ_ConnectionProfile;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.ling.common.Transcoder;
import com.globalsight.ling.common.TranscoderException;

public class ServletConnectionProfile extends HttpServlet
{
    protected final String DEFAULT_PAGE = "/jsp/ConnectionProfile.jsp";
    protected final String MANAGE_PAGE = "/jsp/ConnectionProfileManage.jsp";
    protected final String CLOSE_PAGE = "/jsp/saveWindow.jsp";

    protected final String TEST_MESSAGE="testMessage";
    protected final String TEST = "Test";
    protected final String STATUS = "status";
    protected final String MODIFY = "Modify";
    protected final String NEW = "New";
    protected final String SAVE = "Save";
    protected final String PROFILE_NAME = "profileName";
    protected final String DRIVER = "driver";
    protected final String CONNECTION = "connection";
    protected final String USER_NAME = "userName";
    protected final String PASSWORD = "password";
    protected final String CONNECTION_NAME = "connectionName";
    protected final String CURRENT_NAME = "currentName";
    protected final String PROFILE_OBJECTS = "profileObjects";
    protected final String UPDATE_ME = "updateMe";
    protected final String SUCCESSFUL = "successful";
    protected final String NO_DRIVER = "noDriver";
    protected final String BAD_CONNECTION = "badConnection";

 public ServletConnectionProfile() throws ServletException
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
	    String nextPage = DEFAULT_PAGE;
	    boolean manageProfile = true;

	    Enumeration keys = p_request.getParameterNames();

	    // check for a session
        HttpSession session = p_request.getSession(true);
        if (!session.isNew())
        {
	        // determine the user's request
	        while (keys.hasMoreElements() && manageProfile)
	        {
	            String element = (String)keys.nextElement();
              if (element.equals(TEST)) {
	                testProfile(p_request);
	                manageProfile = false;
	            }
	            else if (element.equals(NEW)) {
	                newProfile(p_request);
	                manageProfile = false;
	            }
	            else if (element.equals(MODIFY)) {
                    modifyProfile(p_request);
                    manageProfile = false;
                }
                else if (element.equals(SAVE)) {
	                saveProfile(p_request);
	                nextPage = CLOSE_PAGE;
	                manageProfile = false;
                }
            }
        }
        if (manageProfile)
            nextPage = MANAGE_PAGE;

		RequestDispatcher requestDispatcher =
			getServletContext().getRequestDispatcher(nextPage);
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
	public void newProfile (HttpServletRequest p_request)
	{
	    HttpSession session = p_request.getSession(true);

	    String emptyString = "";
	    session.removeAttribute(CONNECTION_NAME);
	    session.setAttribute(PROFILE_NAME, emptyString);
	    session.setAttribute(DRIVER, emptyString);
	    session.setAttribute(CONNECTION, emptyString);
	    session.setAttribute(USER_NAME, emptyString);
	    session.setAttribute(PASSWORD, emptyString);
	    session.setAttribute(TEST_MESSAGE, emptyString);
	    session.setAttribute(UPDATE_ME, new Boolean(false));
	    session.setAttribute(STATUS, NEW);

		// save the current name
		session.setAttribute(CURRENT_NAME, emptyString);
    }

	/////////////////////////////////////////////////
	public void modifyProfile (HttpServletRequest p_request)
	{
	    HttpSession session = p_request.getSession(false);

	    Long id = new Long(p_request.getParameter(CONNECTION_NAME));
	    DJ_ConnectionProfile profile = new DJ_ConnectionProfile(id.longValue());
	    session.setAttribute(CONNECTION_NAME, id);
	    session.setAttribute(PROFILE_NAME, profile.getProfileName());
	    session.setAttribute(DRIVER, profile.getDriver());
	    session.setAttribute(CONNECTION, profile.getConnectionString());
	    session.setAttribute(USER_NAME, profile.getUserName());
	    session.setAttribute(PASSWORD, profile.getPassword());
	    session.setAttribute(TEST_MESSAGE, new String(""));
	    session.setAttribute(UPDATE_ME, new Boolean(true));
	    session.setAttribute(STATUS, MODIFY);

		// save the current name
		session.setAttribute(CURRENT_NAME, profile.getProfileName());
	}

	/////////////////////////////////////////////////
	public void saveProfile (HttpServletRequest p_request)
	{
	    HttpSession session = p_request.getSession(false);
	    if (session != null)
	    {
	        Long id = new Long(0);
	        if (session.getAttribute(CONNECTION_NAME) != null)
	            id = (Long)session.getAttribute(CONNECTION_NAME);

                String profileName = p_request.getParameter(PROFILE_NAME);
                String driverName = p_request.getParameter(DRIVER);
                String connection = p_request.getParameter(CONNECTION);
                String username = p_request.getParameter(USER_NAME);
                String password = p_request.getParameter(PASSWORD);

	        DJ_ConnectionProfile profile = new DJ_ConnectionProfile(
	            id.longValue(),
                    profileName,
                    driverName,
                    connection,
                    username,
                    password);
                
	        if ( ((Boolean)session.getAttribute(UPDATE_ME)).booleanValue() )
	            profile.update();
	        else
	            profile.insert();
	    }
	}

	/////////////////////////////////////////////////
	public void testProfile (HttpServletRequest p_request)
	{
	    HttpSession session = p_request.getSession(false);
	    if (session != null)
	    {
	        String message = SUCCESSFUL;
	        // String sql = "SELECT * FROM DUAL";
	        String profileName = p_request.getParameter(PROFILE_NAME);
	        String driver = p_request.getParameter(DRIVER);
	        String connectUrl = p_request.getParameter(CONNECTION);
	        String userName = p_request.getParameter(USER_NAME);
	        String password = p_request.getParameter(PASSWORD);

	        session.setAttribute(PROFILE_NAME, profileName);
	        session.setAttribute(DRIVER, driver);
	        session.setAttribute(CONNECTION, connectUrl);
	        session.setAttribute(USER_NAME, userName);
	        session.setAttribute(PASSWORD, password);

	        Connection connection = null;
	        Statement statement = null;
	        try
	        {
	            // We need to load the thin client jdbc driver
			    Class.forName(driver).newInstance();

			    // Check for connection method
			    if (userName.compareTo("") == 0)
			        connection = DriverManager.getConnection (connectUrl);
			    else
			        connection = DriverManager.getConnection (connectUrl, userName, password);
			    statement = connection.createStatement();
	        }
	        catch (ClassNotFoundException e) {
	            message = NO_DRIVER;
	        }
	        catch (SQLException ex) {
	            ex.printStackTrace();
	            message = BAD_CONNECTION;
	        }
	        catch (IllegalAccessException ia) {
	            ia.printStackTrace();
	            message = NO_DRIVER;
	        }
	        catch (InstantiationException ie) {
	            ie.printStackTrace();
	            message = NO_DRIVER;
	        }
	        catch (Error er) {
	            er.printStackTrace();
	            message = BAD_CONNECTION;
	        }
	        catch (Exception ie) {
	            ie.printStackTrace();
	            message = NO_DRIVER;
	        }
	        finally {
	            // save the status
	            session.setAttribute(TEST_MESSAGE, message);
	            // close the connection
	            try {
	                if (statement != null)
	                    statement.close();
	                if (connection != null)
	                    connection.close();
	            }
	            catch (SQLException closeError) {
	                closeError.printStackTrace();
	            }
	        }
	    }
	}
    private Logger theLogger = Logger.getLogger();
}
