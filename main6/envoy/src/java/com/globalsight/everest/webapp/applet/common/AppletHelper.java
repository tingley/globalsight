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
import java.awt.Component;
import java.awt.Frame;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Locale;
// envoy
import com.globalsight.everest.servlet.ExceptionMessage;
import com.globalsight.everest.webapp.applet.common.GlobalEnvoy;


/**
 * A helper class used by the main applets (both AWT and Swing)
 */
public class AppletHelper
{
    public static final String RANDOM = "rand";
    public static final String SERVLET_URL = "servletUrl";

    // Default HTTP Ports
    private static int NON_SSL_PORT = 80;
    private static int SSL_PORT = 443;

    private static Hashtable m_i18nContents = null; 

    //
    // Helper Methods
    //

    /**
     * Get the parent frame of this applet.
     * @return The parent frame as a Frame component.
     */
    public static Frame getParentFrame(Component p_comp)
    {
        if (p_comp == null)
        {
            p_comp = GlobalEnvoy.getParentComponent();
        }

        while (((p_comp.getParent()) != null) && !(p_comp instanceof Frame))
        {
            p_comp = p_comp.getParent();
        }

        return(Frame)p_comp;
    }

    /**
     * Post data and redirect page if necessary.
     * @param p_outgoingData - a vector serializable object to be
     * passed to the servlet.
     * @param p_targetParam - the parameter of the target url.
     * @return An array of serializable objects (as a result of an action).
     */
    public static Vector appendDataToPostConnection(Vector p_outgoingData,
        String p_targetParam, Applet p_applet)
    {
        Vector incomingData = new Vector();
        String randID = p_applet.getParameter(RANDOM);
        String servletUrl = p_applet.getParameter(SERVLET_URL);
        String targetUrl = p_applet.getParameter(p_targetParam);
        String urlPrefix = getUrlPrefix(p_applet);
        String servletLocation = urlPrefix+servletUrl+randID;
        String targetLocation = targetUrl == null ? null :
            urlPrefix + targetUrl + randID;

        try
        {
            String lineRead = null;
            String result = null;
            Locale uiLocale = null;

            Vector outgoingData = new Vector();
            outgoingData.addElement(p_outgoingData);
            outgoingData.addElement(randID);
            InputStream inputStream =
                makePostConnectionSend(servletLocation, outgoingData);
            ObjectInputStream inputStreamFromServlet =
                new ObjectInputStream(inputStream);

            incomingData = (Vector)inputStreamFromServlet.readObject();

            try
            {
                uiLocale = (Locale)inputStreamFromServlet.readObject();
                GlobalEnvoy.setLocale(uiLocale);
            }
            catch (Throwable ex)
            {
                // locale may not have been sent
            }

            inputStreamFromServlet.close();

            if (incomingData != null)
            {
                if (incomingData.elementAt(0) instanceof ExceptionMessage)
                {
                    displayErrorPage((ExceptionMessage)
                        incomingData.elementAt(0), p_applet);
                }
            }
            else
            {
                // if there's a target url, go there...
                if (targetLocation != null)
                {
                    URL url = new URL(targetLocation);
                    p_applet.getAppletContext().showDocument(url, "_self");
                }
            }
        }
        catch (Exception ioe) //IOException, ClassNotFoundException
        {
            System.err.println(ioe);
            getErrorDlg(ioe.getMessage(), null);
        }

        return incomingData;
    }

    //
    // Local Methods
    //

    // goes to doGet of a servlet
    static Vector makeGetConnection(Applet p_applet)
    {
        String randID = p_applet.getParameter(RANDOM);
        String servletUrl = p_applet.getParameter(SERVLET_URL);
        String servletLocation = getUrlPrefix(p_applet)+servletUrl+randID;
        Vector incomingData = new Vector();
        Locale uiLocale = null;
        try
        {
            URL communicationServlet = new URL(servletLocation);
            URLConnection servletConnection =
                communicationServlet.openConnection();
            servletConnection.setUseCaches(false);
            ObjectInputStream inputStreamFromServlet =
                new ObjectInputStream(servletConnection.getInputStream());

            incomingData = (Vector)inputStreamFromServlet.readObject();

            try
            {
                uiLocale = (Locale)inputStreamFromServlet.readObject();
                GlobalEnvoy.setLocale(uiLocale);
            }
            catch (Throwable ex)
            {
                // locale may not have been sent
            }

            inputStreamFromServlet.close();
        }
        // MalformedURLException, StreamCorruptedException,
        // OptionalDataException, IOException, ClassNotFoundException
        catch (Exception exception)
        {
            System.err.println(exception);
            getErrorDlg(exception.getMessage(), null);
        }

        return incomingData;
    }


    // make the post connection
    static InputStream makePostConnectionSend(String p_servletUrl,
        Vector p_outgoingData)
        throws IOException
    {
        InputStream is = null;

            // Fix for Defect 4279 & 3738
            // Passing a parameter to define the request is POST and not GET.
            // We should probably be using HttpURLConnection that has method
            // to set the request method.
            //
            URL communicationServlet = new URL(p_servletUrl + "&doPost=true");
            URLConnection servletConnection = null;
            servletConnection = communicationServlet.openConnection();
            servletConnection.setDoInput(true);
            servletConnection.setDoOutput(true);
            servletConnection.setUseCaches(false);
            servletConnection.setDefaultUseCaches(false);

            servletConnection.setRequestProperty("Content-Type", "application/octet-stream");

            ObjectOutputStream outputStreamToServlet =
                new ObjectOutputStream(servletConnection.getOutputStream());

            int numObjects = p_outgoingData.size();
            for (int x = 0; x < numObjects; x++)
            {
                outputStreamToServlet.writeObject(
                    p_outgoingData.elementAt(x));
            }

            outputStreamToServlet.flush();
            outputStreamToServlet.close();
            is = servletConnection.getInputStream();
        return is;
    }


    // popup the error dialog
    public static boolean getErrorDlg(String p_message, Component p_component)
    {
        return getDialog(p_message, p_component, 
                         AbstractEnvoyDialog.ERROR_TYPE);
    }

    // popup the error dialog
    public static boolean getWarningDlg(String p_message, Component p_component)
    {
        return getDialog(p_message, p_component, 1);
    }


    // parse the message (word wrapping).
    private static String parseText(String p_text, Component p_component)
    {
        EnvoyWordWrapper wrap = new EnvoyWordWrapper();
        return p_component == null ?
            wrap.parseText(p_text, GlobalEnvoy.getParentComponent().
                getFontMetrics(EnvoyAppletConstants.CELL_FONT), 550) :
            wrap.parseText(p_text, p_component, EnvoyAppletConstants.CELL_FONT);
    }

    // go to the error page...
    public static void displayErrorPage(ExceptionMessage p_exception,
        Component p_component)
    {
        StringBuffer sb = new StringBuffer(p_exception.getMessage());
        String message = p_exception.isDebugMode() ?
            sb.append(",  ").append(p_exception.getStackTrace()).toString() :
            sb.toString();

        getErrorDlg(message, p_component);
    }

    // return the url prefix
    public static String getUrlPrefix(Applet p_applet)
    {
        URL url = p_applet.getCodeBase();
        String protocol = url.getProtocol();
        int port = determineValidPort(url.getPort(), protocol);

        StringBuffer sb = new StringBuffer();
        sb.append(protocol);
        sb.append("://");
        sb.append(url.getHost());
        sb.append(":");
        sb.append(port);

        return sb.toString();
    }
    
    /**
     * Set the i18n contents for local use
     * @param p_contents
     * @see #getI18nContent(String)
     */
    public static void setI18nContents(Hashtable p_contents)
    {
        m_i18nContents = p_contents;
    }
    
    /**
     * Get the i18n contents
     * @return
     */
    public static Hashtable getI18nContents()
    {
        return m_i18nContents;
    }
    
    /**
     * Get the content (String) from Hashtable
     * @param p_key
     * @return
     * @see #setI18NContents(Hashtable)
     */
    public static String getI18nContent(String p_key)
    {
        if (m_i18nContents == null || p_key == null)
        {
            return null;
        }
        
        if (m_i18nContents.containsKey(p_key))
        {
            return (String)m_i18nContents.get(p_key);
        }
        else
        {
            System.out.println(p_key + " not found. ");
            return p_key;
        }
    }

    // Determine a valid port for the applet's URL.  getPort method of URL,
    // will return -1 for an invalid port.  Since browsers do not display
    // default port (80 for non-ssl and 443 for ssl), we need to provide
    // a valid port for the applet's url string.
    private static int determineValidPort(int p_port, String p_protocol)
    {
        // Note that ssl protocols end with 's' (i.e. https, or t3s)
        return p_port == -1 ?
            (p_protocol.toLowerCase().endsWith("s") ?
                SSL_PORT : NON_SSL_PORT) : p_port;
    }

    // popup the error dialog
    private static boolean getDialog(String p_message, 
                                     Component p_component, 
                                     int p_type)
    {
        String btnLables[] = new String[3];
        btnLables[0] = "images/en_US/okButton.gif";
        btnLables[1] = "images/en_US/okButtonb.gif";
        btnLables[2] = "images/en_US/okButtonx.gif";

        Hashtable hashtable = new Hashtable();
        hashtable.put(EnvoyAppletConstants.BTN_LABELS, btnLables);
        hashtable.put(EnvoyAppletConstants.MESSAGE,
            parseText(p_message, p_component));

        return MessageDialog.getMessageDialog(getParentFrame(p_component),
            "Error dialog", hashtable, p_type);
    }
}
