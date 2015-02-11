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
package com.globalsight.everest.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * The ClasspathServlet is used to load applet classes from the application
 * classpath. It uses its own ClassLoader to load the class and then sends it
 * down.
 */
public class ClasspathServlet extends HttpServlet
{
    /* NOTE THAT THIS IS HORRIBLE THAT WE HAVE TO USE THIS!!! */
    /* OUR APPLETS SHOULD BE SELF CONTAINED AND NEED NO OTHER GLOBALSIGHT */
    /* CLASSES ALL THEIR CLASSES SHOULD BE PUT INTO ONE JAR FILE. */
    /* BUT CURRENTLY WE HAVE ALL KINDS OF STUFF GETTING USED IN THE */
    /* APPLETS. JOYFUL IS THE DAY WE CAN GET RID OF THIS CLASSPATH */
    /* SERVLET AND SIMPLY HAVE APPLET CLASSES TAKEN FROM A JAR */
    /* USING A CLASSPATH SERVLET IS ALSO WAY SLOWER THAN A ONETIME */
    /* LOAD OF A JAR FILE.... */

    //////////////////////////////////////
    // Private Members                  //
    //////////////////////////////////////
    private static Logger s_logger =
    Logger.getLogger(ClasspathServlet.class);

    /**
     * The codebase string (or part of it) which will be in
     * the URL for requested .class files. This should contain
     * both leading and trailing slashes.
     */
    private static final String CODEBASE = "/classes/";
    //length up to the final slash ("/classes")
    private static final int CODEBASE_LEN = CODEBASE.length() - 1;
    private static final int BUFSIZ = 4096;

    //IE asks for these applet classes even though they do not exist
    //and are not needed. 
    private static final String IE_CLASS1 = "AppletBeanInfo.class";
    private static final String IE_CLASS2 = "$COMClassObject.class";

    /**
     * Creates a ClasspathServlet
     */
    public ClasspathServlet()
    {
        super();
    }

    /**
     * Retrieves the requested class file from the application
     * classpath.
     * 
     * @param p_request
     * @param p_response
     * @exception ServletException
     * @exception IOException
     */
    public void doGet(HttpServletRequest p_request, HttpServletResponse p_response)
    throws ServletException, IOException
    {
        String url = p_request.getRequestURL().toString();
        try
        {
            int idx = url.indexOf(CODEBASE);
            String className = url.substring(idx + CODEBASE_LEN);
            if (s_logger.isDebugEnabled())
            {
                if (url.endsWith(".class"))
                {
                    s_logger.debug("Class name is : " + className);
                }
                else
                {
                    s_logger.debug("Resource name is : " + className);
                }

            }
            InputStream is = ClasspathServlet.class.getResourceAsStream(className);
            if (is == null)
            {
                //getResourceAsStream returns null if resource does not exist
                if (url.endsWith(IE_CLASS1) || url.endsWith(IE_CLASS2))
                {
                    //IE 5.5 asks for these classes even though it doesn't need to
                    //consult http://www.imint.com/support/sp108.htm for more info
                    //better to not log this out, but we have to throw an exception
                    //so IE knows its not there
                    s_logger.debug("Ignoring IE request for un-needed class: " + url);
                }
                else
                {
                    String msg = "Requested applet resource '" + url + "' does not exist.";
                    //the applets seem to ask for all sorts of things they don't need...don't
                    //log these out as errors
                    s_logger.debug(msg);
                }
                //now send a 404 and return
                p_response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            OutputStream os = p_response.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            byte[] buf = new byte[BUFSIZ];
            int count = 0;
            while ((count = bis.read(buf)) != -1)
                bos.write(buf,0,count);
            bis.close();
            bos.close();
            is.close();
            os.close();        
        }
        catch (Exception e)
        {
            handleException(url,e);
        }
    }

    /**
     * Makes a nice message and then throws ServletException
     * 
     * @param p_url  requested url
     * @param p_ex   exception to handle
     * @exception ServletException
     */
    private void handleException(String p_url, Exception p_ex) throws ServletException
    {
        StringBuffer msg = new StringBuffer(
            "Problem trying to get class from URL '");
        msg.append(p_url);
        msg.append("' reason: ");
        msg.append(p_ex.getMessage());
        s_logger.error(msg,p_ex);
        throw new ServletException(p_ex);
    }
}

