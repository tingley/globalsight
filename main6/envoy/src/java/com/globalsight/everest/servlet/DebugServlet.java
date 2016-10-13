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

import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.util.edit.EditUtil;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DebugServlet
    extends HttpServlet
{
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException,
               IOException
    {
        res.setContentType("text/html");

        PrintWriter out = res.getWriter();
        HttpSession session = req.getSession(true);

        out.println("<HTML><HEAD><TITLE>Session Debugger</TITLE></HEAD>");
        out.println("<BODY><H2>Session Debugger</H2>");

        String uri = req.getRequestURI();
        String beanName = req.getParameter("name");

        if (beanName == null)
        {
            // Show list of session objects
            Enumeration names = session.getAttributeNames();
            while (names.hasMoreElements())
            {
                String name = (String)names.nextElement();
                Object obj = session.getAttribute(name);
                Class clasz = obj.getClass();

                out.print("<LI>");
                out.print("<A href=" + uri + "?name=" + name + ">");
                out.print(name);
                out.print("</A>");
                out.print(" <I>(" + clasz.getName() + ")</I>");
                out.print("</LI>");
            }
        }
        else
        {
            // Describe a specific object by printing out its fields
            Object obj = session.getAttribute(beanName);
            out.println("<H3>Object <U>" + beanName + "</U></H3>");

            if (obj == null)
            {
                out.println("Object is <B>null</B>.");
            }
            else if (obj instanceof SessionManager)
            {
                SessionManager manager = (SessionManager)obj;

                Class clasz = manager.getClass();

                out.println("<P>");
                out.println("<I>" + clasz.getName() + "</I> ");
                out.println("<B>" + beanName + "</B>:");
                out.println("</P>");

                HashMap map = manager.getMap();

                if (map == null)
                {
                    out.println("<B>SessionManager is null!!</B>");
                }
                else
                {
                    out.println("<UL>");

                    for (Iterator it = map.keySet().iterator(); it.hasNext(); )
                    {
                        String key = (String)it.next();
                        Object val = map.get(key);

                        out.println("<LI>");
                        out.println("<B>" + key + "</B>");
                        out.println(": ");

                        try
                        {
                            out.println(EditUtil.encodeHtmlEntities(
                                val == null ? "null" : val.toString()));
                        }
                        catch (Exception e)
                        {
                            out.println("<B>Cannot be displayed!!</B>");
                        }

                        out.println("</LI>");
                    }

                    out.println("</UL>");
                }
            }
            else
            {
                Class clasz = obj.getClass();

                out.println("<P>");
                out.println("<I>" + clasz.getName() + "</I> ");
                out.println("<B>" + beanName + "</B>:");
                out.println("</P>");

                out.println("<P>");
                out.println(EditUtil.encodeHtmlEntities(
                    "Value: " + obj.toString()));
                out.println("</P>");

                out.println("<UL>");

                Field[] fields = clasz.getFields();

                for (int i = 0; i < fields.length; ++i)
                {
                    out.println("<LI>");
                    out.println(fields[i].getName());
                    out.println(" (<I>");
                    out.println(fields[i].getType().toString());
                    out.println("</I>): ");

                    try
                    {
                        out.println(EditUtil.encodeHtmlEntities(
                            fields[i].get(obj).toString()));
                    }
                    catch (Exception e)
                    {
                        out.println("<B>Cannot be displayed!!</B>");
                    }

                    out.println("</LI>");
                }

                out.println("</UL>");
            }
        }

        out.println("</BODY></HTML>");
    }

}
