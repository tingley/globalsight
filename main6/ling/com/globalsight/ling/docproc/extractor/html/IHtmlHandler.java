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
package com.globalsight.ling.docproc.extractor.html;

import com.globalsight.ling.docproc.extractor.html.HtmlObjects;

/**
 * <P>Handler functions for events that the Html Parser generates.</P>
 */
public interface IHtmlHandler
{
    /**
     * Called once at the beginning of a new document.
     */
    void handleStart();

    /**
     * Called once at the end of a document.
     */
    void handleFinish();

    /**
     * Handle an HTML comment <code>&lt;!-- --&gt;</code>.
     */
    void handleComment(HtmlObjects.Comment c);

    /**
     * Handle a ColdFusion comment <code>&lt;!--- ---&gt;</code>.
     */
    void handleCfComment(HtmlObjects.CfComment c);

    /**
     * Handle an HTML declaration <code>&lt;!DOCTYPE  &gt;</code>.
     */
    void handleDeclaration(HtmlObjects.Declaration t);

    /**
     * Handle an HTML processing instruction <code>&lt;?  ?&gt;</code>.
     */
    void handlePI(HtmlObjects.PI t);

    /**
     * Handle an HTML start tag including its attributes.
     */
    void handleStartTag(HtmlObjects.Tag t);

    /**
     * Handle a ColdFusion start tag including its attributes.
     */
    void handleCFStartTag(HtmlObjects.CFTag t);

    /**
     * Handle an HTML end tag.
     */
    void handleEndTag(HtmlObjects.EndTag t);

    /**
     * Handle a ColdFusion end tag.
     */
    void handleCFEndTag(HtmlObjects.EndTag t);

    /**
     * Handle end of line characters.
     */
    void handleNewline(HtmlObjects.Newline n);

    /**
     * Handle a ASP/JSP tag; the script text is included in the
     * argument <code>t</code>.
     */
    void handleXsp(HtmlObjects.Xsp t);

    /**
     * Handle the <code>&lt;script&gt;</code> tag; the script text is
     * included in the argument <code>t</code>.
     */
    void handleScript(HtmlObjects.Script t);

    /**
     * Handle the <code>&lt;java&gt;</code> tag; the java text is
     * included in the argument <code>t</code>.
     */
    void handleJava(HtmlObjects.Java t);

    /**
     * Handle the <code>&lt;style&gt;</code> tag; the style text is
     * included in the argument <code>t</code>.
     */
    void handleStyle(HtmlObjects.Style t);

    /**
     * Handle the <code>&lt;CFSCRIPT&gt;</code> tag; the ColdFusion
     * script text is included in the argument <code>t</code>.
     */
    void handleCFScript(HtmlObjects.CFScript t);

    /**
     * Handle the <code>&lt;CFSCRIPT&gt;</code> tag with SQL
     * statements inside; the SQL text is included in the argument
     * <code>t</code>.
     */
    void handleCFQuery(HtmlObjects.CFQuery t);

    /**
     * Handle text (#PCDATA).
     */
    void handleText(HtmlObjects.Text t);
    
    void handleSpecialChar(HtmlObjects.Text t);
}
