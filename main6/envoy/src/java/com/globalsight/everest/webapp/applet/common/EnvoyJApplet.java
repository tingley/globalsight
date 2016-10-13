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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Vector;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;
// GlobalSight
import com.globalsight.everest.servlet.ExceptionMessage;

/**
 * EnvoyJApplet is the main swing applet that contains all swing
 * related components.  The applet expects two parameter to be passed
 * in (thru html tag).  The first one is the "grid" which is a fully
 * qualified path of the panel that should be contained in the applet.
 * The second parameter is "rand" which is the randomly generated id
 * used for accessing the existing http session.
 */
public class EnvoyJApplet
    extends JApplet
    implements EnvoyAppletConstants
{
    public static final String GRID = "grid";
    private EnvoyJPanel m_panel = null;
    private URL m_targetUrl = null;

    public void init()
    {
        // This line prevents the "Swing: checked access to system
        // event queue" message seen in some browsers.
        getRootPane().putClientProperty("defeatSystemEventQueueCheck",
            Boolean.TRUE);

        // set this applet as the parent component (used by grid and dialogs)
        GlobalEnvoy.setParentComponent((Component)this);

        //getContentPane().setLayout(new BorderLayout());
        try
        {
            createGrid();
        }
        //ClassNotFoundException, InstantiationException, IllegalAccessException
        // MalformedURLException
        catch (Exception cnfe)
        {
            getErrorDlg(cnfe.getMessage());
            cnfe.printStackTrace();
        }
    }

    //
    // Helper Methods
    //

    /**
     * Post data and return any incoming info from the server.
     * @param p_outgoingData - a vector serializable object to be
     * passed to the servlet.
     * @param p_targetParam - the parameter of the target url.
     * @return A list of serializable objects (as a result of an action).
     */
    public Vector appendDataToPostConnection(Vector p_outgoingData,
        String p_targetParam)
    {
        Vector incomingData = new Vector();
        String randID = this.getParameter(AppletHelper.RANDOM);
        String servletUrl = this.getParameter(AppletHelper.SERVLET_URL);
        String targetUrl = null;
        if (p_targetParam != null)
        {
            targetUrl = this.getParameter(p_targetParam);
        }
        String urlPrefix = AppletHelper.getUrlPrefix(this);
        String servletLocation = urlPrefix+servletUrl+randID;
        String targetLocation = targetUrl == null ? null :
            urlPrefix + targetUrl + randID;

        try
        {
            getParentFrame().setCursor(Cursor.WAIT_CURSOR);
            String lineRead = null;
            String result = null;

            Vector outgoingData = new Vector();
            outgoingData.addElement(p_outgoingData);
            outgoingData.addElement(randID);
            InputStream inputStream =
                makePostConnectionSend(servletLocation, outgoingData);
            ObjectInputStream inputStreamFromServlet =
                new ObjectInputStream(inputStream);
            incomingData = (Vector)inputStreamFromServlet.readObject();

            inputStreamFromServlet.close();
            if (incomingData != null)
            {
                if (incomingData.elementAt(0) instanceof ExceptionMessage)
                {
                    AppletHelper.displayErrorPage((ExceptionMessage)
                        incomingData.elementAt(0),
                        this);
                }
            }
            else
            {
                if (targetLocation != null)
                {
                    m_targetUrl = new URL(targetLocation);
                }
            }
        }
        catch (Exception ioe) //IOException, ClassNotFoundException
        {
            System.err.println(ioe);
            AppletHelper.getErrorDlg(ioe.getMessage(), null);
        }
        finally
        {
            getParentFrame().setCursor(Cursor.DEFAULT_CURSOR);
        }

        return incomingData;
    }

    /**
     * Requests that the browser shows the page indicated by
     * the target URL.
     */
    public void callNewPage()
    {
        SwingUtilities.invokeLater(new Runnable(){
                public void run()
                {
                    URL url = getTargetUrl();
                    if (url != null)
                        getAppletContext().showDocument(url,"_self");
                }});
    }

    /**
     * Get the target url for this applet
     */
    protected URL getTargetUrl()
    {
        return m_targetUrl;
    }

    /**
     * Display the error page.  The message will be the content of the
     * exception message.
     * @param p_exception - The exception that contains the error message.
     */
    public void displayErrorPage(ExceptionMessage p_exception)
    {
        AppletHelper.displayErrorPage(p_exception, this);

    }


    /**
     * Get the dialog used for displaying a UI related error message.
     * @param p_message - The message to be displayed.
     */
    public boolean getErrorDlg(String p_message)
    {
        return AppletHelper.getErrorDlg(p_message, this);
    }


    /**
     * Get the parent frame of this applet.
     * @return The parent frame as a Frame component.
     */
    public Frame getParentFrame()
    {
        return AppletHelper.getParentFrame(this);
    }

    /**
     * Get a dialog with warning message where the user can 
     * make an option.  Clicking on OK means accepting the 
     * message and Cancel will allow user ot make changes.
     * @param p_message The message to be displayed.
     */
    public boolean getWarningDlg(String p_message)
    {
        return AppletHelper.getWarningDlg(p_message, this);
    }


    /**
     * Make a post connection to the servlet.
     * @param p_servletUrl - The GlobalSight servlet URL.
     * @param p_outgoingData - The data to be posted to the server side.
     */
    protected InputStream makePostConnectionSend(String p_servletUrl,
        Vector p_outgoingData)
        throws Exception //IOException
    {
        return AppletHelper.makePostConnectionSend(p_servletUrl, p_outgoingData);
    }

    //
    // Local Methods
    //

    // create the grid, and populate it.
    private void createGrid()
    {
        String grid = this.getParameter(GRID);
        
        try
        {
            m_panel = (EnvoyJPanel) Class.forName(grid).newInstance();
        }
        //ClassNotFoundException
        catch (Exception ex)
        {
            getErrorDlg(ex.getMessage());
        }

        Vector result = makeGetConnection();

        if (result != null && result.elementAt(0) instanceof ExceptionMessage)
        {
            displayErrorPage((ExceptionMessage)result.elementAt(0));
        }
        else
        {
            m_panel.populate(result);
            if (getParameter("addToApplet") != null)
            {
                getContentPane().add(BorderLayout.CENTER, (Component)m_panel);
            }
        }
    }


    // goes to doGet of a servlet
    private Vector makeGetConnection()
    {
        return AppletHelper.makeGetConnection(this);
    }

    protected EnvoyJPanel getMainPanel()
    {
        return m_panel;
    }
}
