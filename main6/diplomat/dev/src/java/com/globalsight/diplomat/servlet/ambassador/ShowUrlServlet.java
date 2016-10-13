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
* ShowUrlServlet
* <p>
* This servlet goes in the top frame of the nested frameset and is responsible
* for setting the content of the bottom frame.
*/
public class ShowUrlServlet extends HttpServlet
{
   public ShowUrlServlet()
      throws ServletException
   {
      try {
         theLogger.setLogname("CxeServlets");
      }catch (IOException e) {
         throw new ServletException(e);
      }
   }

   public void doPost (HttpServletRequest p_request, HttpServletResponse p_response)
      throws IOException, ServletException
   {
      doGet(p_request,p_response);
   }

   /**
    * Writes out the frameset that goes inside the target frames.
    * @param the request
    * @param the response
    * @throws IOException
    * @throws ServletException
    */
   public void doGet (HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException
   {
      response.setContentType("text/html;");
      HttpSession session = request.getSession();
//response.setHeader("Pragma", "No-cache");
//response.setHeader("Cache-Control", "no-cache");
//response.setDateHeader("Expires", theDate.toString());
      String urlListId = request.getParameter("urlListId");
      String type = request.getParameter("type");
      String targetFrame = type + "Bottom";
      String urlIndexString = request.getParameter("urlIndex");
      int urlIndex = Integer.parseInt(urlIndexString);
      theLogger.println(Logger.DEBUG_D,
                        "ShowUrlServlet: sessionId=" +
                        request.getSession().getId() +
                        " targetFrame = " + targetFrame);
      UrlElement[] theUrls = (UrlElement[]) ServletStash.getServletStash().get(urlListId);
      UrlElement e = null;
      ArgElement arg = null;
      if (theUrls != null && theUrls.length > urlIndex)
      {
         e = theUrls[urlIndex];
      }
      else
      {
         theLogger.println(Logger.ERROR, "ShowUrlServlet: no urls.");
      }

      Date theDate = new Date();
      long time = theDate.getTime();
      String cxeTimeStamp = Long.toString(time);

      StringBuffer fileBottom = new StringBuffer("<!-- Form Section -->\n");

      StringBuffer url = new StringBuffer("");
      String parameter;
      String value;

      if (e != null && e.getType().equals("get"))
      {
         //handle a get url
         url = new StringBuffer(e.getHref()); //build up the URL
   
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
                  url.append(parameter + "=" + value);
               else
                  url.append(parameter); //some parameters have no values
         
               if (j + 1 < e.getNumArgs())
                  url.append("&"); //one more argument to add
            }
         }
         catch (ArrayIndexOutOfBoundsException aioobe)
         {}
   
         //this should force the URL to be different every time to
         //avoid being cached
         if (e.getNumArgs() > 1)
            url.append("&cxeTimeStamp=" + cxeTimeStamp);
         else
            url.append("cxeTimeStamp=" + cxeTimeStamp);
      }
      else if (e != null)
      {
         //now figure out what form to put in the StringBuffer
         fileBottom.append("<form name=\"form0\" action=\""
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
            fileBottom.append("<input type=\"hidden\" name=\"cxeTimeStamp\" value=\"" + cxeTimeStamp + "\">\n");
         }
         catch (ArrayIndexOutOfBoundsException aioobe)
         {
         }
   
         //end the form and put the button outside the form so that the spacing looks better than
         //using a submit button inside the form. Use javascript to get the button to submit the form
         fileBottom.append("</form>\n");
      }

      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<script language=\"JavaScript1.1\">");
      String onload="";
      if (e!= null && e.getType().equals("get"))
      {
         out.println("function displayUrl() {");
         out.println("\t//alert (\"currently on \" + parent." + targetFrame +
                     ".location.href);");
         out.println("\t//alert (\"going to: " + url.toString() + "\")");
         out.println("\tparent." + targetFrame + ".location=\"" +
                     url.toString() + "\";");
         out.println("}");
         onload = "onLoad=\"javascript:displayUrl()\"";
      }
      else if (e!=null)
      { /*handle the posting of the form*/
         out.println("function displayUrl() {");
         out.println("//alert(\"posting form0\");");
         out.println("document.forms[0].submit();");
         out.println("}");
         onload = "onLoad=\"javascript:displayUrl()\"";
      }
      out.println("</script>");
      out.println("<body bgcolor=\"white\" " + onload + ">");
      out.println("<!-- Main Body -->");
      out.println(fileBottom.toString());
      out.println("</body>");
      out.println("</html>");
   }

   private Logger theLogger = Logger.getLogger();
}
