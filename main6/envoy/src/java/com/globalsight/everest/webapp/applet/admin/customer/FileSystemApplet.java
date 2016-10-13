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
package com.globalsight.everest.webapp.applet.admin.customer;

import java.awt.Cursor;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Vector;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.globalsight.everest.servlet.ExceptionMessage;
import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.everest.webapp.applet.common.EnvoyJApplet;
import com.globalsight.everest.webapp.applet.util.AuthenticationPrompter;
import com.globalsight.everest.webapp.applet.util.GlobalSightProxyInfo;
import com.globalsight.everest.webapp.applet.util.SwingWorker;
import com.globalsight.util.zip.ZipIt;
import com.sun.java.browser.net.ProxyInfo;




/**
 * An applet used for displaying the file system (exploring like UI)
 */
public class FileSystemApplet extends EnvoyJApplet 
{
    JProgressBar m_progressBar = null;

    //keep this AuthenticationPrompter so the username/password is just obtained once
    private AuthenticationPrompter s_authPrompter = new AuthenticationPrompter();

    //////////////////////////////////////////////////////////////////////
    //  Begin: Override Methods
    //////////////////////////////////////////////////////////////////////
    public void init() 
    {
        super.init();
    }


    public void destroy() 
    {
        super.destroy();
    }

    //////////////////////////////////////////////////////////////////////
    //  End: Override Methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Post data and return any incoming info from the server.
     * @param p_outgoingData - a vector serializable object to be
     * passed to the servlet.
     * @param p_targetParam - the parameter of the target url.
     * @return A list of serializable objects (as a result of an action).
     */
    public Vector appendDataToPostConnection(JProgressBar p_progressbar,
                                             final Vector p_outgoingData,
                                             String p_targetParam)
    {
        String urlPrefix = AppletHelper.getUrlPrefix(this);
        final String randID = getParameter(AppletHelper.RANDOM);
        String servletUrl = getParameter(AppletHelper.SERVLET_URL);
        String targetUrl = null;
        if (p_targetParam != null)
        {
            targetUrl = getParameter(p_targetParam);
        }

        final String servletLocation = urlPrefix+servletUrl+randID;
        final String targetLocation = targetUrl == null ? null :
                                      urlPrefix + targetUrl + randID;

        if ("previousURL".equals(p_targetParam) ||
            "cancelURL".equals(p_targetParam))
        {
            goToTargetPage(targetLocation);
        }
        else
        {
            m_progressBar = p_progressbar;
            final SwingWorker worker = new SwingWorker() {
                public Object construct() {                
                    return new startUpload(servletLocation, targetLocation, 
                                           randID, p_outgoingData);
                }
            };
            worker.start();
        }

        return null;        
    }


    /**
     * Prepare the info for the upload process and zip all the files.
     */
    private void performUploadProcess(String servletLocation, 
                                      String targetLocation, 
                                      String randID,
                                      Vector p_outgoingData)
    {
        try
        {
            m_progressBar.setValue(10);
            getParentFrame().setCursor(Cursor.WAIT_CURSOR);
            String lineRead = null;
            String result = null;

            Vector outgoingData = new Vector();
            outgoingData.addElement(p_outgoingData);
            outgoingData.addElement(randID);

            Object[] objs = (Object[])p_outgoingData.get(0);
            int size = objs == null ? 0 : objs.length;
            File[] files = new File[size];
            System.arraycopy(objs, 0, files, 0, size);
            m_progressBar.setValue(20);
            sendZipFile(files, servletLocation, targetLocation);
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
    }
    
    /**
     * Gets a permissible File, due Windows permission issue.
     * 
     * @param child     The child pathname string
     */
    public File getFile(String child)
    {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.startsWith("Windows"))
        {
            File parent = new File(System.getProperty("user.home"));
            if(!parent.exists()) parent.mkdirs();
            return new File(parent, child);
        }
        else
        {
            return new File(child);
        }
    }
    
    /**
     * Zip the selected files and send the zip to the server.
     * @param p_filesToBeZipped - A list of selected files to be uploaded.
     * @param p_targetURL - The target URL representing server URL.
     * @param p_targetLocation - A string representing the link to the next page.
     */
    public void sendZipFile(File[] p_filesToBeZipped, 
                            String p_targetURL,
                            final String p_targetLocation) 
    throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append("GS_");
        sb.append(System.currentTimeMillis());
        sb.append(".zip");

        File targetFile = getFile(sb.toString());
        ZipIt.addEntriesToZipFile (targetFile, p_filesToBeZipped);
        m_progressBar.setValue(30);

        PostMethod filePost = new PostMethod(p_targetURL + "&doPost=true");
        filePost.getParams().setBooleanParameter(
                                                HttpMethodParams.USE_EXPECT_CONTINUE, true);
        filePost.setDoAuthentication(true);
        try
        {
            Part[] parts = {new FilePart(targetFile.getName(), targetFile)};

            m_progressBar.setValue(40);
            filePost.setRequestEntity(
                                     new MultipartRequestEntity(parts, filePost.getParams()));

            HttpClient client = new HttpClient();
            setUpClientForProxy(client);
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

            m_progressBar.setValue(50);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK)
            {
              //no need to ask for auth again since the first upload was fine
                s_authPrompter.setAskForAuthentication(false);

                m_progressBar.setValue(60);
                InputStream is = filePost.getResponseBodyAsStream();
                m_progressBar.setValue(70);
                ObjectInputStream inputStreamFromServlet =
                new ObjectInputStream(is);
                Vector incomingData = (Vector)inputStreamFromServlet.readObject();

                inputStreamFromServlet.close();
                if (incomingData != null)
                {
                    if (incomingData.elementAt(0) instanceof ExceptionMessage)
                    {
                        resetProgressBar();
                        AppletHelper.displayErrorPage((ExceptionMessage)
                                                      incomingData.elementAt(0),
                                                      this);
                    }
                }
                else
                {
                    boolean deleted = targetFile.delete(); 
                    m_progressBar.setValue(100);
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (Exception e)
                    {
                    }
                    // now move to some other page...
                    goToTargetPage(p_targetLocation);
                }
            }
            else
            {
                //authentication may have failed, reset the need to ask
                s_authPrompter.setAskForAuthentication(true);
                resetProgressBar();
                String errorMessage = "Upload failed because: (" +
                    status + ") " + HttpStatus.getStatusText(status);
                if (status == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED)
                {
                    errorMessage = "Incorrect NTDomain\\username or password entered. Hit 'upload' again to re-try.";
                }
                AppletHelper.getErrorDlg(errorMessage,null);
            }
        }
        catch (Exception ex)
        {
            //authentication may have failed, reset the need to ask
            s_authPrompter.setAskForAuthentication(true);

            resetProgressBar();
            System.err.println(ex);
            AppletHelper.getErrorDlg(ex.getMessage(), null);
        }
        finally
        {
            filePost.releaseConnection();            
        }

    }

    /**
     * Sets up the HttpClient to use a proxy if one needs to be used.
     * Also sets up for proxy authentication (NTLM and Basic)
     * 
     * @param p_httpClient
     */
    private void setUpClientForProxy(HttpClient p_client) throws Exception
    {
        //first see if a proxy needs to be used at all
        //detectProxyInfoFromSystem() is known to work at Dell
//        ProxyInfo proxyInfo = detectProxyInfoFromSystem();

        //detectProxyInfoFromBrowser() is probably more correct,
        String urlPrefix = AppletHelper.getUrlPrefix(this);
        String servletUrl = getParameter(AppletHelper.SERVLET_URL);
        String randID = getParameter(AppletHelper.RANDOM);
        String servletLocation = urlPrefix+servletUrl+randID;
        ProxyInfo proxyInfo = GlobalSightProxyInfo.detectProxyInfoFromBrowser(
                                                                             new URL(servletLocation));

        if (proxyInfo != null)
        {
            System.out.println("---- Setting up client for proxy.");
            //set to use proxy
            p_client.getHostConfiguration().setProxy(
                                                    proxyInfo.getHost(),proxyInfo.getPort());

            //set to authenticate to proxy if need be
            p_client.getParams().setParameter(CredentialsProvider.PROVIDER,
                                              s_authPrompter);
        }
        else
        {
            System.out.println("---- No need to set client for proxy.");
        }
    }

    /**
     * Leave the applet UI and go to the page specified by 
     * the target location parameter.
     * @param p_targetLocation The target page to go to.
     */
    public void goToTargetPage(String p_targetLocation)        
    {
        try
        {
            if (p_targetLocation != null)
            {
                final URL url = new URL(p_targetLocation);
                SwingUtilities.invokeLater(new Runnable(){
                                               public void run()
                                               {
                                                   if (url != null)
                                                       getAppletContext().showDocument(url,"_self");
                                               }});
            }
        }
        catch (Exception e)
        {
            AppletHelper.getErrorDlg(e.getMessage(), null);
        }
    }


    /**
     * Reset the progress bar by setting it to zero and hiding it.
     */
    private void resetProgressBar()
    {
        m_progressBar.setValue(0);
        m_progressBar.setVisible(false);
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Inner Class
    //////////////////////////////////////////////////////////////////////
    /**
     * A class that starts the upload process in a separate thread thru the 
     * SwingWorker utility class.
     */
    class startUpload 
    {
        public startUpload(String servletLocation, 
                           String targetLocation, 
                           String randID,
                           Vector p_outgoingData) 
        {
            super();


            m_progressBar.setVisible(true);
            performUploadProcess(servletLocation, targetLocation, 
                                 randID, p_outgoingData);
        }

    } 
    //////////////////////////////////////////////////////////////////////
    //  End: Inner Class
    //////////////////////////////////////////////////////////////////////
}

