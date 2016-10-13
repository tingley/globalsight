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
package com.globalsight.diplomat.servlet.ambassador;

import com.globalsight.diplomat.util.previewUrlXml.UrlElement;
import com.globalsight.diplomat.util.previewUrlXml.ArgElement;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.ConfigParameters;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfiguration;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;

/** 
* UrlPresentationServlet
* <p>
*  The UrlPresentationServlet accesses the Session object to
 *  find out what the preview URLs are for the chosen locale.
 *  It then displays the HTML for the URLs so the user can select them
 *  and see the output in the appropriate frame.
 * This servlet expects to be called by PreviewUrlFrame.jsp
*/
public class UrlPresentationServlet extends HttpServlet
{
    public UrlPresentationServlet()
    throws ServletException
    {
        try
        {
            theLogger.setLogname("CxeServlets");
        }
        catch (IOException e)
        {
            throw new ServletException(e);
        }
    }

    public void doPost (HttpServletRequest p_request, HttpServletResponse p_response)
    throws IOException, ServletException
    {
        doGet(p_request,p_response);
    }

    /**
     * Writes out the HTML for the URL lists corresponding to the following parameters:
     * <ol> <li> urlListId -- unique id of the url list in the session</li>
     * <li> targetFrame -- where to display the url list</li>
     * <li> locale -- the locale for the url list </li> </ol>
     * @param the request
     * @param the response
     * @throws IOException
     * @throws ServletException
     */
    public void doGet (HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        try
        {
            HttpSession session = request.getSession();
	    theLogger.println(Logger.DEBUG_D,"UrlPresentationServlet: sessionId=" + session.getId());

            //just get both url lists to be safe and stick them in the session
            String srcUrlListId = request.getParameter("srcUrlListId");
            String tgtUrlListId = request.getParameter("tgtUrlListId");      
            theLogger.println(Logger.DEBUG_D,"UrlPresentationServlet: srcUrlListId= " + srcUrlListId);
            theLogger.println(Logger.DEBUG_D,"UrlPresentationServlet: tgtUrlListId= " + tgtUrlListId);

            String type = request.getParameter("type"); //Source or Target  
            String targetFrame = request.getParameter("targetFrame");
            String locale = request.getParameter("locale");
            String encoding = request.getParameter("encoding");
            theLogger.println(Logger.DEBUG_D,
                              "UrlPresentationServlet: encoding=" + encoding);
            if (encoding == null || encoding.equals(""))
                encoding="UTF-8";

            response.setContentType("text/html; charset=" + encoding);
            theLogger.println(Logger.DEBUG_D,
                              "UrlPresentationServlet: session id="
                              + request.getSession().getId() +
                              " type="  + type);
            
	    UrlElement[] theUrls = null;
            String urlListId="";

            theLogger.println(Logger.DEBUG_D, "UrlPresentationServlet " + type);
            if (type.equals("Target"))
            {
                urlListId = tgtUrlListId;
                theUrls = (UrlElement[]) ServletStash.getServletStash().get(tgtUrlListId);
            }
            else
            {
                urlListId = srcUrlListId;
                theUrls = (UrlElement[]) ServletStash.getServletStash().get(srcUrlListId);
            }

            if (theUrls == null)
            {
                theLogger.println(Logger.ERROR,
                                  "UrlPresentation: No URLs for " + urlListId);
            }
	    else
	    {
                theLogger.println(Logger.DEBUG_A, "UrlPresentation: Url list size is: " + theUrls.length);

	    }

//             if (srcUrls != null)
//                 session.setAttribute(srcUrlListId, srcUrls);
//
//             if (tgtUrls != null)
//                 session.setAttribute(tgtUrlListId, tgtUrls);

            Date theDate = new Date();
            long time = theDate.getTime();
            String cxeTimeStamp = Long.toString(time);

            SystemConfiguration config = SystemConfiguration.getInstance();
            String useSSL = config.getStringParameter("useSSL");
            String nonSSLPort = config.getStringParameter("nonSSLPort");
            String SSLPort = config.getStringParameter("SSLPort");

            String prefix;
            if (useSSL.equals("false"))
                prefix = "http://" + request.getServerName() + ":" + nonSSLPort;
            else
                prefix = "https://" + request.getServerName() + ":" + SSLPort;

            String showUrl = prefix + "/globalsight/ShowUrlFramesetServlet?urlListId=" +
                             urlListId + "&targetFrame=" + targetFrame + "&type=" + type;

            UrlElement e = null;
            StringBuffer fileMiddle = new StringBuffer("");
            StringBuffer fileBottom = new StringBuffer("<!-- Form Section -->\n");
            String parameter;
            String value;
            String formName;
            ArgElement arg;
            String selected;

            //go through all the URLs and fill out the fileMiddle and fileBottom
            //string buffers
            for (int i=0; theUrls != null && i < theUrls.length; i++)
            {
                e = theUrls[i];
                formName = "urlForm" + i;

                if (e.getType().equals("get"))
                {
                    //handle a get url
                    StringBuffer url = new StringBuffer(e.getHref()); //build up the URL

                    //always append at least one argument
                    url.append("?");
                    try
                    {
                        for (int j=0; j <e.getNumArgs(); j++)
                        {
                            arg = e.getArg(j);
                            parameter = arg.getParameter().trim();
                            value = arg.getValue().trim();

                            if (value != null)
                            {
                                url.append(URLEncoder.encode(parameter));
                                url.append("=");
                                url.append(URLEncoder.encode(value));
                            }
                            else
                            {
                                //some parameters have no values
                                url.append(URLEncoder.encode(parameter));
                            }
                            if (j + 1 < e.getNumArgs())
                                url.append("&"); //one more argument to add
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException aioobe)
                    {
                    }

                    //this should force the URL to be different every time to
                    //avoid being cached
                    if (e.getNumArgs() > 1)
                        url.append("&cxeTimeStamp=" + time);
                    else
                        url.append("cxeTimeStamp=" + time);

                    //now write out the HTML form for this URL
                    if (i == 0)
                        fileMiddle.append("\t<option value=\"" + url + "\" selected>" + e.getLabel()
                                          + "</option>\n");
                    else
                        fileMiddle.append("\t<option value=\"" + url + "\">" + e.getLabel()
                                          + "</option>\n");

                    //now figure out what form to put in the StringBuffer
                    //theform is empty since the javascript handleUrl will do the display
                    fileBottom.append("\n<!-- form number " + i + " -->\n");
                    fileBottom.append("<form name=\"");
                    fileBottom.append(formName);
                    fileBottom.append("\" method=\"get\">");
                    fileBottom.append("</form>\n");
                }
                else
                {
                    //handle a post url
                    if (i == 0)
                    {
                        fileMiddle.append("\t<option selected>" + e.getLabel() + "</option>\n");
                    }
                    else
                    {
                        fileMiddle.append("\t<option>" + e.getLabel() + "</option>\n");
                    }

                    //now figure out what form to put in the StringBuffer
                    fileBottom.append("<!-- form number " + i + " -->\n");
                    fileBottom.append("<form name=\"" + formName + "\" action=\""
                                      + e.getHref() + "\" method=\"post\" target="
                                      + targetFrame + ">\n");
                    try
                    {
                        for (int j=0; j <e.getNumArgs(); j++)
                        {
                            arg = e.getArg(j);
                            parameter = arg.getParameter().trim();
                            value = arg.getValue().trim();
                            if (value != null)
                                fileBottom.append("<input type=\"hidden\" name=\"" + parameter + "\" value=\"" + value + "\">\n");
                            else
                                fileBottom.append("<input type=\"hidden\" name=\"" + parameter + "\">\n");
                        }

                        //always append this hidden field to force netscape to load
                        fileBottom.append("<input type=\"hidden\" name=\"cxeTimeStamp\" value=\"" + time + "\">\n");
                    }
                    catch (ArrayIndexOutOfBoundsException aioobe)
                    {
                    }

                    //end the form and put the button outside the form so that the spacing looks better than
                    //using a submit button inside the form. Use javascript to get the button to submit the form
                    fileBottom.append("</form>\n");
                }
            }

            //now write out the HTML for this servlet's response
            PrintWriter out = response.getWriter();
            out.println("<html>\n");
            out.println("<META HTTP-EQUIV=\"Cache-Control\" CONTENT=\"no-cache\">");
            out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"0\">");
            out.println("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=" + encoding + "\">");
            out.println("<!-- Load Number " + time + " -->");
            out.println("<head>");
            out.println("</head>");
            out.println("<script language=\"JavaScript1.1\">");
            out.println("\n");
            out.println("function handleUrl(i) {");
            out.println("\tvar theUrl = \"" + showUrl +
                        "\" + \"&urlIndex=\" + i + \"&cxeTimeStamp=\" + \"" +
                        cxeTimeStamp + "\";");
            out.println("\t//   alert (\"showing url \" + theUrl);");
            out.println("\tparent." + targetFrame + ".location=theUrl;");
            out.println("}");
            out.println("\n");
            out.println("function loadFirstForm() {");
            out.println("\t//skip the main form");
            out.println("\tif (document.forms.length > 1) {");
            out.println("\t\thandleUrl(0);");
            out.println("\t}");
            out.println("return true;");
            out.println("}");
            out.println("</script>");
            out.println("<body bgcolor=\"#CCCCCC\" onLoad=\"javascript:loadFirstForm()\">");
            out.println("\n");
            out.println("<!-- Main Form -->");
            out.println("<center>");
            out.println("<form name=\"mainform\">");
            out.println("<table>");
            out.println("<tr>");
            out.println("<td><label><b><font face=\"arial\">" + locale + "</font></b></label></td>");
            out.println("<td><select name=\"urllist\" size=\"1\" onchange=\"javascript:handleUrl(document.mainform.urllist.selectedIndex)\">");
            out.println(fileMiddle.toString());
            out.println("</select></td>");
            out.println("</tr>");
            out.println("</table>");
            out.println("</form>");
            out.println("</center>");
            out.println(fileBottom.toString());
            out.println("</body>");
            out.println("</html>");
            out.close();
            theLogger.println(Logger.DEBUG_D, "UrlPresentationServlet: exiting.");
        }
        catch (Exception e)
        {
            theLogger.printStackTrace(Logger.ERROR, "UrlPresentationServlet problem:", e);
            throw new ServletException(e);
        }
    }

    private Logger theLogger = Logger.getLogger();
}
