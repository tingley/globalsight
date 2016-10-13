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
package com.globalsight.ling.docproc.extractor.css;

/**
 * <P>CSS stylesheets consist of the following major sections:
 *
 * <OL>
 * <LI>@charset   - Specifies the charset of the current input file
 * <LI>@import    - Specifies other CSS files to be included
 * <LI>@media     - A media section specifies styles that only apply
 *                  to the given set of media, e.g., screen, print,
 *                  tv, radio, speech, aural, tty.
 * <LI>@font-face - Allows the author to describe the fonts used on
 *                  a page in a general way, including their download
 *                  locations.
 * <LI>@page      - Specifies defaults for rendering HTML on paged
 *                  media such as printers.
 * <LI>ruleset    - see below
 * </OL>
 *
 * <P>Each major section contains a <strong>rule set</strong>, which
 * consist of a <strong>selector</strong>, to which the rule applies,
 * and a set of <strong>declarations</strong>, which are
 * attribute-value pairs, that describe the styles and their values
 * that actually apply to the elements selected by the the selector.
 *
 * <P>The parser fires the following events: (use the source, Luke)
 */
public interface IParseEvents
{
    public void handleStart();
    public void handleFinish();

    public void handleWhite(String s);
    public void handleEndOfLine(String s);
    public void handleComment(String s);
    public void handleCDO(String s);
    public void handleCDC(String s);

    public void handleStartCharset(String s);
    public void handleCharset(String s);
    public void handleEndCharset(String s);

    public void handleStartFontFace(String s);
    public void handleEndFontFace(String s);

    public void handleStartImport(String s);
    public void handleImport(String s);
    public void handleImportURI(String s);
    public void handleEndImport(String s);

    public void handleStartMedia(String s);
    public void handleMedia(String s);
    public void handleEndMedia(String s);

    public void handleStartAtRule(String s);
    // no EndAtRule so far

    public void handleStartBlock(String s);
    public void handleEndBlock(String s);

    public void handleStartDeclarations(String s);
    public void handleEndDeclarations(String s);

    public void handleToken(String s);
    public void handleDelimiter(String s);
    public void handleFunction(String s);

    public void handleStyle(String s);
    public void handleStartValues(String s);
    public void handleEndValues();
}
