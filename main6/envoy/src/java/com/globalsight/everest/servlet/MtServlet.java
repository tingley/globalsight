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

import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.machineTranslation.babelfish.BabelfishProxy;
import com.globalsight.machineTranslation.freetranslation.FreeTranslationProxy;
import com.globalsight.machineTranslation.google.GoogleProxy;
import com.globalsight.machineTranslation.systran.SystranProxy;

import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.edit.EditUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * A servlet that asks an MT system for the translation of a segment.
 *
 * See /includes/machinetranslation.js for a UI file defining the
 * available language pairs.
 *
 * Note that the SYSTRAN service is linked to a real Systran server at
 * the customer's site for which the customer must have acquired a
 * separate licence. All other services used are free:
 *
 * Babelfish: free for almost any use.
 * FreeTranslation: free for private, non-commercial use.
 * Google: no info available. Must be as free as it gets.
 */
public class MtServlet
    extends HttpServlet
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            MtServlet.class);

    private Locale makeLocale(String p_locale)
    {
        if (p_locale.length() == 5)
        {
            return new Locale(p_locale.substring(0, 2),
                p_locale.substring(3, 5));
        }
        else
        {
            return new Locale(p_locale.substring(0, 2));
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        doGet(req, res);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException,
               IOException
    {
        res.setContentType("text/html; charset=UTF-8");
        res.setHeader("Pragma", "no-cache"); //HTTP 1.0
        res.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
        res.addHeader("Cache-Control", "no-store"); // tell proxy not to cache
        res.addHeader("Cache-Control", "max-age=0"); // stale right away

        //HttpSession session = req.getSession(true);
        PrintWriter out = res.getWriter();

        String engine  = req.getParameter("engine");
        String srcLang = req.getParameter("source");
        String trgLang = req.getParameter("target");
        String segment = req.getParameter("segment");

        String result = translate(engine, srcLang, trgLang, segment);

        out.println("<HTML>");
        out.println("<BODY>");
        out.print("<P id='result'>");
        out.print(EditUtil.encodeHtmlEntities(result));
        out.println("</P>");
        out.println("</BODY>");
        out.println("</HTML>");
    }

    private String translate(String p_engine, String p_srcLang,
        String p_trgLang, String p_string)
    {
        MachineTranslator mt = null;

        try
        {
            Locale srcLocale = makeLocale(p_srcLang);
            Locale trgLocale = makeLocale(p_trgLang);

            if (p_engine.equals(MachineTranslator.ENGINE_BABELFISH))
            {
                mt = new BabelfishProxy();
            }
            else if (p_engine.equals(MachineTranslator.ENGINE_FREETRANSLATION))
            {
                mt = new FreeTranslationProxy();
            }
            else if (p_engine.equals(MachineTranslator.ENGINE_GOOGLE))
            {
                mt = new GoogleProxy();
            }
            else if (p_engine.equals(MachineTranslator.ENGINE_SYSTRAN))
            {
                mt = new SystranProxy();
            }

            String result = mt.translate(srcLocale, trgLocale, p_string);

            return result;
        }
        catch (Throwable ignore)
        {
            // do nothing
        }

        return p_string;
    }
}
