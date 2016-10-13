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
package com.globalsight.everest.webapp.applet.common;
// java
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Panel;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Vector;

import com.globalsight.everest.servlet.ExceptionMessage;

/**
 * EnvoyApplet is the main applet that contains the grid and related
 * components.  The applet expects two parameter to be passed in (thru
 * html tag).  The first one is the "grid" which is a fully qualified
 * path of the grid that should be contained in the applet.  The
 * second parameter is "rand" which is the randomly generated id used
 * for accessing the existing http session.
 *
 */
public class EnvoyApplet
    extends Applet
    implements EnvoyAppletConstants
{
    public static final String GRID = "grid";
    public static final String RANDOM = "rand";
    public static final String SERVLET_URL = "servletUrl";

    public void init()
    {
        // set this applet as the parent component (used by grid and dialogs)
        GlobalEnvoy.setParentComponent((Component)this);

        setLayout(new BorderLayout());
        try
        {
            createGrid();
        }
        // ClassNotFoundException, InstantiationException,
        // IllegalAccessException, MalformedURLException
        catch (Exception cnfe)
        {
            cnfe.printStackTrace();
            getErrorDlg(cnfe.getMessage(), this);
        }
    }

    //
    // Helper Methods
    //

    /**
     * Get the parent frame of this applet.
     * @return The parent frame as a Frame component.
     */
    public Frame getParentFrame()
    {
        return AppletHelper.getParentFrame(this);
    }

    /**
     * Post data and redirect page if necessary.
     * @param p_outgoingData - a vector serializable object to be
     * passed to the servlet.
     * @param p_targetParam - the parameter of the target url.
     * @return An array of serializable objects (as a result of an action).
     */
    public Vector appendDataToPostConnection(Vector p_outgoingData,
        String p_targetParam)
    {
        return AppletHelper.appendDataToPostConnection(
            p_outgoingData, p_targetParam, this);
    }

    //
    // Local Methods
    //

    // create the grid, and populate it.
    private void createGrid()
        throws ClassNotFoundException, MalformedURLException,
               InstantiationException, IllegalAccessException
    {
        String grid = this.getParameter(GRID);
        EnvoyGrid m_grid = null;

        try
        {
            m_grid = (EnvoyGrid) Class.forName(grid).newInstance();
        }
        catch (ClassNotFoundException ex)
        {
            getErrorDlg(ex.getMessage(), this);
        }

        Vector result = makeGetConnection();

        if (result != null && result.elementAt(0) instanceof ExceptionMessage)
        {
            displayErrorPage((ExceptionMessage)result.elementAt(0));
        }
        else
        {
            m_grid.populate(result);
            this.add(BorderLayout.CENTER, (Panel)m_grid);
        }
    }


    // goes to doGet of a servlet
    private Vector makeGetConnection()
    {
        return AppletHelper.makeGetConnection(this);
    }


    // make the post connection
    protected InputStream makePostConnectionSend(String p_servletUrl,
        Vector p_outgoingData)
        throws IOException
    {
        return AppletHelper.makePostConnectionSend(p_servletUrl, p_outgoingData);
    }


    // popup the error dialog
    public boolean getErrorDlg(String p_message, Component p_component)
    {
        return AppletHelper.getErrorDlg(p_message, (Component)this);
    }

    // go to the error page...
    public void displayErrorPage(ExceptionMessage p_exception)
    {
        AppletHelper.displayErrorPage(p_exception, (Component)this);

    }

    protected String getUrlPrefix()
    {
        return AppletHelper.getUrlPrefix(this);
    }
}
