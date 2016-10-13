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
//===============================================================
// package : com.tetrasix.majix.rtf
// class : com.tetrasix.majix.RtfAnalyser
//===============================================================
// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at
// http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
// License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is TetraSys code.
//
// The Initial Developer of the Original Code is TetraSys..
// Portions created by TetraSys are
// Copyright (C) 1998-2000 TetraSys. All Rights Reserved.
//
// Contributor(s):
//===============================================================
//System.err.println("tok=" + tok + " - depth=" + depth);
package com.globalsight.ling.rtf;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import com.globalsight.ling.common.CodesetMapper;

public class RtfAnalyser
{
    //
    // Member Variables
    //

    private RtfReader _reader;
    private RtfDocument _theDocument;
    private RtfInfo _theInfo = null;
    private RtfParagraph _theParagraph;
    private RtfStyleSheet _theStyles;
    private RtfFontTable _theFonts;
    private RtfColorTable _theColors;
    private RtfParagraphProperties _theParagraphProperties;

    private RtfTextProperties _theTextProperties;
    private RtfTextPropertiesStack _theTextPropertiesStack;

    private RtfExternalEntities _theExternalEntities;
    private RtfVariables _theVariables;

    // Flag when in RTF header mode as opposed to parsing document
    // text and controls.
    private boolean _inHeader;

    // debug flag
    private boolean _debug = false;
    private boolean _debugStack = false;
    private boolean _debugConversion = false;

    private int _pictureCounter = 1;
    // If an annotation is for a bookmark, the bookmark is surrounded by
    // {\*\atrfstartN} .. {\*\atrfendN} (a bookmark around the bookmark).
    // This member holds the bookmark ID.
    private String _bookmarkAnnotation = null;
    // Place to remember an annotation ID which comes before the
    // actual annotation group.
    private String _annotationId = null;
    // flag saying a field gets always recomputed and we don't need to
    // preserve the \\fldrslt destination, e.g. TOC and SYMBOL.
    private boolean _no_field_result;

    private RtfCompoundObjectStack _theContainerStack;
    private RtfCompoundObject _theContainer;

    // Table data
    private RtfRow _theRow;
    private RtfCell _theCell;

    private boolean _isSegmentId = false;

    /**
     * Counts how many ANSI chars to skip after each Unicode char.
     * Initialized with current \\uc value for each Unicode char
     * (Unicode control) we see, then decremented for each Ansi.
     */
    private int _skipCount = 0;

    //
    // Constructor
    //

    public RtfAnalyser(RtfReader reader)
    {
        _reader = reader;
        _theDocument = new RtfDocument(reader.getFileName());
        _theParagraph = null;
        _theFonts = new RtfFontTable();
        _theColors = new RtfColorTable();
        _theStyles = new RtfStyleSheet();
        _theExternalEntities = new RtfExternalEntities();
        _theVariables = new RtfVariables();
        _theDocument.setFontTable(_theFonts);
        _theDocument.setColorTable(_theColors);
        _theDocument.setStyleSheet(_theStyles);
        _theDocument.setExternalEntities(_theExternalEntities);
        _theDocument.setVariables(_theVariables);
        _theParagraphProperties = new RtfParagraphProperties();
        _theTextProperties = new RtfTextProperties();
        _theTextPropertiesStack = new RtfTextPropertiesStack();

        _theContainerStack = new RtfCompoundObjectStack();
        _theContainer = _theDocument;

        _theRow  = null;
        _theCell = null;
        _theInfo = new RtfInfo();

        _inHeader = true;
    }

    //
    // Public Methods
    //
    public RtfDocument parse()
        throws IOException
    {
        RtfToken tok = _reader.getNextToken();

        if (tok.getType() == RtfToken.OPENGROUP)
        {
            startGroup();

            if (_debugStack)
            {
                debug("--> parse document");
            }
            parse(1);

            if (_debugStack)
            {
                debug("_theTextPropertiesStack (should be empty) =" +
                    _theTextPropertiesStack.toString());
            }
        }
        else
        {
            warn("First RTF token is not '{', is " + tok);
        }

        return _theDocument;
    }

    private void parse(int depth)
        throws IOException
    {
        RtfToken tok;

        if (_debugStack)
        {
            debug(">RTF Parser start " + (_theTextPropertiesStack.size() - 1));
        }

        while (depth > 0)
        {
            tok = _reader.getNextToken();
            if (tok == null)
            {
                if (_theParagraph != null)
                {
                    _theParagraph.setProperties(_theParagraphProperties);//?
                    _theContainer.add(_theParagraph);
                }

                if (_debugStack)
                {
                    debug("RTF Parser finished unsuccessfully on EOF!");
                }
                return;
            }

            switch (tok.getType())
            {
                case RtfToken.ASTERISK:
                {
                    tok = _reader.getNextToken();

                    if (tok.getType() == RtfToken.CONTROLWORD)
                    {
                        if (! parseStarControlWord(tok))
                        {
                            skipUntilEndOfGroup();
                        }
                        depth--;
                    }
                    else
                    {
                        // Syntax error, really.
                        skipUntilEndOfGroup();
                        depth--;
                    }
                }
                break;

                case RtfToken.DATA:
                    if (_skipCount == 0)
                    {
                        insertData(tok.getData(), false);
                    }
                    else
                    {
                        // Assumes we always read AT LEAST _skipCount
                        // ANSI chars after a Unicode char, or else
                        // RTF file is incorrect.

                        String s = tok.getData();

                        if (s.length() >= _skipCount)
                        {
                            s = tok.getData().substring(_skipCount);
                            _skipCount = 0;
                        }
                        else
                        {
                            // a \'XY encoded char
                            _skipCount--;
                        }


                        if (s.length() > 0)
                        {
                            insertData(s, false);
                        }
                    }
                    break;

                case RtfToken.CONTROLWORD:
                    if (parseControlWord(tok))
                    {
                        depth--;
                    }
                    break;

                case RtfToken.OPENGROUP:
                    startGroup();
                    //_skipCount = _theTextProperties.getSkipCount();
                    depth++;
                    break;

                case RtfToken.CLOSEGROUP:
                    endGroup();
                    //_skipCount = _theTextProperties.getSkipCount();
                    depth--;
                    break;
            }
        }

        // Add last paragraph if not terminated with "\par"
        if (_theParagraph != null)
        {
            _theParagraph.setProperties(_theParagraphProperties);//?
            _theContainer.add(_theParagraph);
        }

        if (_debugStack)
        {
            debug("<RTF Parser end " + _theTextPropertiesStack.size());
        }
    }

    //
    // Private Methods
    //

    public void debug(String s)
    {
        System.err.println(s);
    }

    public void warn(String s)
    {
        System.err.println(s);
    }

    /**
     * Parses a normal control word like \par. Returns true if a group
     * was processed and the closing parentheses was read.
     */
    private boolean parseControlWord(RtfToken tok)
        throws IOException
    {
        String name = tok.getName();

        if (name.equals("par"))
        {
            if (_theParagraph == null)
            {
                _theParagraph = new RtfParagraph();
            }
            _theParagraphProperties.setPar(true);
            _theParagraph.setProperties(_theParagraphProperties);
            _theParagraphProperties.setPar(false);
            _theContainer.add(_theParagraph);
            _theParagraph = null;
        }
        else if (name.equals("sect"))
        {
            // page break and section break are considered paragraph breaks
            if (_theParagraph != null)
            {
                _theParagraph.setProperties(_theParagraphProperties);
                _theContainer.add(_theParagraph);
                _theParagraph = null;
            }

            _theContainer.add(new RtfPageBreak(RtfPageBreak.SECTION));
        }
        else if (name.equals("page"))
        {
            // page break and section break are considered paragraph breaks
            if (_theParagraph != null)
            {
                _theParagraph.setProperties(_theParagraphProperties);
                _theContainer.add(_theParagraph);
                _theParagraph = null;
            }

            _theContainer.add(new RtfPageBreak(RtfPageBreak.PAGE));
        }
        else if (name.equals("line"))
        {
            insertLineBreak();
        }
        // Todo: page break *before* the paragraph
        else if (name.equals("pagebb"))
        {
            _theContainer.add(new RtfControl(name, tok.getData()));
        }
        else if (name.equals("pard"))
        {
            _theContainer.add(new RtfControl(name, tok.getData()));

            _theParagraphProperties.reset();
        }
        else if (name.equals("stylesheet"))
        {
            parseStyleSheet();
            return true;
        }
        else if (name.equals("fonttbl"))
        {
            parseFontTable();
            return true;
        }
        else if (name.equals("colortbl"))
        {
            parseColorTable();
            return true;
        }
        else if (name.equals("header") || name.equals("headerl") ||
            name.equals("headerr") || name.equals("headerf") ||
            name.equals("footer") || name.equals("footerl") ||
            name.equals("footerr") || name.equals("footerf"))
        {
            parseHeaderFooter(name);
            return true;
        }
        else if (name.equals("footnote"))
        {
            parseFootnote();
            return true;
        }
        else if (name.equals("object"))
        {
            parseOleObject(tok);
            return true;
        }
        else if (name.equals("shp") || name.equals("shpgrp"))
        {
            parseShape(tok);
            return true;
        }
        else if (name.equals("nonshppict"))
        {
            // Ignore data. Word writes the real picture before
            // and we read it into an RtfShapePicture object.
            // {\*\shhpict...}{\nonshppict...}
            skipUntilEndOfGroup();
            return true;
        }
        else if (name.equals("pict"))
        {
            processPict();
            return true;
        }
        else if (name.equals("ftnalt"))
        {
            // can only appear after the {\footnote destination
            ((RtfFootnote)_theContainer).setEndnote(true);
        }
        else if (name.equals("chftn"))
        {
            // automatic footnote marker
            insertMarker(name);
        }
        else if (name.equals("s"))
        {
            int code = Integer.parseInt(tok.getData());
            _theParagraphProperties.setStyle(code);
        }
        else if (name.equals("pntext"))
        {
            warn("encountered pntext");

            String data = getDataOfGroup(null);
            _theParagraphProperties.setBullet(data);
            return true;
        }
        else if (name.equals("ltrpar"))
        {
            _theParagraphProperties.setLtrPar();
        }
        else if (name.equals("rtlpar"))
        {
            _theParagraphProperties.setRtlPar();
        }
        else if (name.equals("ltrrow"))
        {
        	if (_theRow == null)
        	{
        		_theRow = new RtfRow();
        	}
            _theRow.setLtrRow();
        }
        else if (name.equals("rtlrow"))
        {
        	if (_theRow == null)
        	{
        		_theRow = new RtfRow();
        	}
            _theRow.setRtlRow();
        }
        else if (name.equals("b"))
        {
            // Handle \b1 and \b0 for upload.
            if ("0".equals(tok.getData()))
            {
                _theContainer.add(new RtfControl(name, tok.getData()));
            }
            else if ("1".equals(tok.getData()))
            {
                _theContainer.add(new RtfControl(name, tok.getData()));
            }
            else
            {
                boolean value = getSymbolValue(tok.getData());

                _theTextProperties.setBold(value);
            }
        }
        else if (name.equals("i"))
        {
            boolean value = getSymbolValue(tok.getData());

            _theTextProperties.setItalic(value);
        }
        else if (name.equals("v"))
        {
            boolean value = getSymbolValue(tok.getData());

            _theTextProperties.setHidden(value);
        }
        else if (name.equals("ul"))
        {
            boolean value = getSymbolValue(tok.getData());

            _theTextProperties.setUnderlined(value);
        }
        else if (name.equals("cf"))
        {
            _theTextProperties.setColor(Integer.parseInt(tok.getData()));
        }
        else if (name.equals("cb"))
        {
            _theTextProperties.setBgColor(Integer.parseInt(tok.getData()));
        }
        else if (name.equals("cs"))
        {
            _theTextProperties.setCharStyle(Integer.parseInt(tok.getData()));
        }
        else if (name.equals("u"))
        {
            char ch;
            int i = Integer.parseInt(tok.getData());
            if (i < 0)
            {
                // Fix for def 12870
                ch = (char)(65536 + i);
            }
            else
            {
                ch = (char)(i);
            }

            insertData(String.valueOf(ch), true);
            _skipCount = _theTextProperties.getSkipCount();
        }
        else if (name.equals("upr"))
        {
            // TODO TODO TODO: read unicode destinations
            warn("\\upr not handled yet");
        }
        else if (name.equals("uc"))
        {
            int skipCount = Integer.parseInt(tok.getData());
            _theDocument.setSkipCount(skipCount);
            _theTextProperties.setSkipCount(skipCount);
        }
        else if (name.equals("f"))
        {
            _theTextProperties.setFont(Integer.parseInt(tok.getData()));
        }
        else if (name.equals("fs"))
        {
            _theTextProperties.setFontSize(Integer.parseInt(tok.getData()));
        }
        else if (name.equals("sub"))
        {
            _theTextProperties.setSubscript(true);
        }
        else if (name.equals("super"))
        {
            _theTextProperties.setSuperscript(true);
        }
        else if (name.equals("lang"))
        {
            _theTextProperties.setLang(Integer.parseInt(tok.getData()));
        }
        else if (name.equals("deflang"))
        {
            _theDocument.setLang(Integer.parseInt(tok.getData()));
        }
        else if (name.equals("deflangfe"))
        {
            _theDocument.setFeLang(Integer.parseInt(tok.getData()));
        }
        else if (name.equals("deff"))
        {
            _theDocument.setFont(Integer.parseInt(tok.getData()));
        }
        else if (name.equals("ansicpg"))
        {
            _theDocument.setCodepage(Integer.parseInt(tok.getData()));
        }
        else if (name.equals("plain"))
        {
            _theContainer.add(new RtfControl(name, tok.getData()));

            _theTextProperties.reset();
        }
        else if (name.equals("field"))
        {
            processField();
            return true;
        }
        else if (name.equals("ldblquote"))
        {
            insertData("\u201C", true);           // LEFT DOUBLE QUOTATION MARK
        }
        else if (name.equals("rdblquote"))
        {
            insertData("\u201D", true);           // RIGHT DOUBLE QUOTATION MARK
        }
        else if (name.equals("lquote"))
        {
            insertData("\u2018", true);           // LEFT SINGLE QUOTATION MARK
        }
        else if (name.equals("rquote"))
        {
            insertData("\u2019", true);           // RIGHT SINGLE QUOTATION MARK
        }
        else if (name.equals("emdash"))
        {
            insertData("\u2014", true);           // EM DASH
        }
        else if (name.equals("endash"))
        {
            insertData("\u2013", true);          // EN DASH
        }
        else if (name.equals("bullet"))
        {
            insertData("\u2022", true);          // BULLET
        }
        else if (name.equals("-"))
        {
            // may cause problems:
            // http://mail.nl.linux.org/linux-utf8/2003-04/msg00370.html
            insertData("\u00AD", true);           // SOFT HYPHEN
        }
        else if (name.equals("_"))
        {
            insertData("\u2011", true);           // NON-BREAKING HYPHEN
        }
        else if (name.equals("~"))
        {
            insertData("\u00A0", true);           // NO-BREAK SPACE
        }
        else if (name.equals("emspace"))
        {
            insertData("\u2003", true);           // EM SPACE
        }
        else if (name.equals("enspace"))
        {
            insertData("\u2002", true);           // EN SPACE
        }
        // See http://www.alistapart.com/stories/emen/ for em&en-dash
        // and also the ellipsis char (which prints out as \'85 in
        // RTF, relative to the win-1252/ansi codepage. When read back
        // in it turned out to be an (unchangeable) Japanese ellipsis.

        // TODOXXX: add \zwj\zwnj and \: "Specifies a subentry in an
        // index entry")
        else if (name.equals("tab"))
        {
            insertTab();
        }
        // Starts a table row (there is no command to end a table)
        else if (name.equals("trowd"))
        {
            processStartRow();
            return false;
        }
        else if (name.equals("cellx"))
        {
            _theContainer.add(new RtfControl(name, tok.getData()));
        }
        else if (name.equals("cell"))
        {
            processCell();
            return false;
        }
        else if (name.equals("row"))
        {
            processRow();
            return false;
        }
        // marks an in-cell paragraph
        else if (name.equals("intbl"))
        {
            _theContainer.add(new RtfControl(name, tok.getData()));
        }
        else if (name.equals("info"))
        {
            parseInfo();
            return true;
        }
        else if (name.equals("listtext"))
        {
            // The bullet or number of a list paragraph in a group -
            // can be skipped by readers.
            skipUntilEndOfGroup();
            return true;
        }
        else if (name.equals("af") ||
            name.equals("afs") ||
            name.equals("acf") ||
            name.equals("acb") ||
            name.equals("ab") ||
            name.equals("ai") ||
            name.equals("aul") ||
            name.equals("loch") ||
            name.equals("hich") ||
            name.equals("dbch") ||
            name.equals("ltrch") ||
            name.equals("rtlch") ||
            name.equals("cgrid") ||
            // \fcs is not documented but I vaguely remember some perl
            // code saying it's the same as \fc - it's in the way anyway
            name.equals("fcs") ||
            // should probably handle langfe
            name.equals("langfe") ||
            name.equals("langnp") ||
            name.equals("langfenp") ||
            name.equals("noproof") ||
            // revision codes - ugh!
            name.equals("insrsid") ||
            name.equals("delrsid") ||
            name.equals("charrsid") ||
            name.equals("sectrsid") ||
            name.equals("pararsid") ||
            name.equals("tblrsid"))
        {
            // Ignore these controls.
        }
        /*
        else if (_theRow != null && RtfControls.isTableControl())
        {
            // If in tables, collect everything else
            _theRow.add(new RtfControl(name, tok.getData()));
        }
        */
        else
        {
            // Unknown control: add to current container if we're
            // past the header.
            if (!_inHeader)
            {
                _theContainer.add(new RtfControl(name, tok.getData()));
            }
        }

        return false;
    }

    private void skipUntilEndOfGroup()
        throws IOException
    {
        int depth = 1;
        while (depth > 0)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                break;
            }

            int type = tok.getType();
            switch (type)
            {
            case RtfToken.OPENGROUP:
                depth++;
                break;
            case RtfToken.CLOSEGROUP:
                depth--;
                break;
            default:
                break;
            }
        }

        endGroup();
    }

    private String getDataOfGroup(RtfTextProperties p_properties)
        throws IOException
    {
        StringBuffer result = new StringBuffer();

        int depth = 1;
        while (depth > 0)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                break;
            }

            int type = tok.getType();
            switch (type)
            {
                case RtfToken.DATA:
                    // CvdL: should not contain Unicode data and \\ucN
                    result.append(tok.getData());
                    break;
                case RtfToken.OPENGROUP:
                    startGroup();
                    depth++;
                    break;
                case RtfToken.CLOSEGROUP:
                    endGroup();
                    depth--;
                    break;
                case RtfToken.ASTERISK:
                    skipUntilEndOfGroup();
                    depth--;
                    break;
                case RtfToken.CONTROLWORD:
                    if(p_properties != null) // p_properties can be null
                    {
                        setTextProperties(tok, p_properties);
                    }
                    break;
            }
        }

        return result.toString();
    }

    //
    // Style Sheet
    //

    private void parseStyleSheet()
        throws IOException
    {
        for (;;)
        {
            RtfToken tok = _reader.getNextToken();

            if (tok == null)
            {
                return;
            }

            if (tok.getType() == RtfToken.OPENGROUP)
            {
                startGroup();
                parseStyleDefinition();
            }
            else if (tok.getType() == RtfToken.CLOSEGROUP)
            {
                endGroup();
                return;
            }
        }
    }

    private void parseStyleDefinition()
        throws IOException, NumberFormatException
    {
        boolean isCharStyle = false;
        boolean isAdditive = false;
        int code = 0;
        int next = 0;
        int basedon = -1;                         // no style = 222
        String name = null;
        RtfTextProperties textProperties = new RtfTextProperties();

        textProperties.setSkipCount(_theDocument.getSkipCount());
        //textProperties.setLang(_theDocument.getLang());
        //textProperties.setFont(_theDocument.getFont());

        for (;;)
        {
            RtfToken tok = _reader.getNextToken();

            if (tok == null)
            {
                break;
            }

            // Default paragraph style has no \s or \cs.
            if (tok.getType() == RtfToken.CONTROLWORD)
            {
                if (tok.getName().equals("s"))
                {
                    code = Integer.parseInt(tok.getData());
                    isCharStyle = false;

                    // Next style = cur style by default.
                    next = code;
                }
                else if (tok.getName().equals("cs"))
                {
                    code = Integer.parseInt(tok.getData());
                    isCharStyle = true;

                    // Next style = cur style by default.
                    next = code;
                }
                else if (tok.getName().equals("additive"))
                {
                    isAdditive = true;
                }
                else if (tok.getName().equals("snext"))
                {
                    next = Integer.parseInt(tok.getData());
                }
                else if (tok.getName().equals("sbasedon"))
                {
                    basedon = Integer.parseInt(tok.getData());
                }
                else if (tok.getName().equals("b"))
                {
                    textProperties.setBold(getSymbolValue(tok.getData()));
                }
                else if (tok.getName().equals("i"))
                {
                    textProperties.setItalic(getSymbolValue(tok.getData()));
                }
                else if (tok.getName().equals("v"))
                {
                    textProperties.setHidden(getSymbolValue(tok.getData()));
                }
                else if (tok.getName().equals("ul"))
                {
                    textProperties.setUnderlined(getSymbolValue(tok.getData()));
                }
                else if (tok.getName().equals("cf"))
                {
                    int color = Integer.parseInt(tok.getData());
                    textProperties.setColor(color);
                }
                else if (tok.getName().equals("uc"))
                {
                    int skipCount = Integer.parseInt(tok.getData());
                    textProperties.setSkipCount(skipCount);
                }
                else if (tok.getName().equals("f"))
                {
                    int font = Integer.parseInt(tok.getData());
                    textProperties.setFont(font);
                }
                else if (tok.getName().equals("fs"))
                {
                    int fontSize = Integer.parseInt(tok.getData());
                    textProperties.setFontSize(fontSize);
                }
                else if (tok.getName().equals("lang"))
                {
                    int lang = Integer.parseInt(tok.getData());
                    textProperties.setLang(lang);
                }
            }
            else if (tok.getType() == RtfToken.DATA)
            {
                if (name == null)
                {
                    name = tok.getData();
                }
                else
                {
                    name += tok.getData();
                }

                if (name.endsWith(";"))
                {
                    name = name.substring(0, name.length() - 1);
                }
            }
            else if (tok.getType() == RtfToken.OPENGROUP)
            {
                startGroup();
                skipUntilEndOfGroup();
            }
            else if (tok.getType() == RtfToken.CLOSEGROUP)
            {
                endGroup();
                break;
            }
        }

        if (isCharStyle)
        {
            _theStyles.defineCharacterStyle(code, name, next, basedon,
                isAdditive, textProperties);
        }
        else
        {
            _theStyles.defineParagraphStyle(code, name, next, basedon,
                textProperties);
        }
    }

    //
    // Font Table
    //

    private void parseFontTable()
        throws IOException
    {
        _inHeader = false;

        for (;;)
        {
            RtfToken tok = _reader.getNextToken();

            if (tok == null)
            {
                return;
            }

            if (tok.getType() == RtfToken.OPENGROUP)
            {
                startGroup();
                parseFontDefinition();
            }
            else if (tok.getType() == RtfToken.CLOSEGROUP)
            {
                endGroup();
                return;
            }
        }
    }

    private void parseFontDefinition()
        throws IOException, NumberFormatException
    {
        RtfFontProperties fontProperties = new RtfFontProperties();
        int code = 0;
        String name = null;

        for (;;)
        {
            RtfToken tok = _reader.getNextToken();

            if (tok == null)
            {
                break;
            }

            if (tok.getType() == RtfToken.CONTROLWORD)
            {
                if (tok.getName().equals("f"))
                {
                    code = Integer.parseInt(tok.getData());
                }
                else if (tok.getName().equals("fcharset"))
                {
                    int charset = Integer.parseInt(tok.getData());
                    fontProperties.setCharset(charset);
                }
                else if (tok.getName().equals("fprq"))
                {
                    int pitch = Integer.parseInt(tok.getData());
                    fontProperties.setPitch(pitch);
                }
                else if (tok.getName().equals("fnil") ||
                    tok.getName().equals("froman") ||
                    tok.getName().equals("fswiss") ||
                    tok.getName().equals("fmodern") ||
                    tok.getName().equals("fscript") ||
                    tok.getName().equals("fdecor") ||
                    tok.getName().equals("ftech") ||
                    tok.getName().equals("fbidi"))
                {
                    fontProperties.setFamily(tok.getName());
                }
            }
            else if (tok.getType() == RtfToken.DATA)
            {
                if (name == null)
                {
                    name = tok.getData();
                }
                else
                {
                    name += tok.getData();
                }

                if (name.endsWith(";"))
                {
                    name = name.substring(0, name.length() - 1);
                }

                fontProperties.setName(name);
            }
            else if (tok.getType() == RtfToken.OPENGROUP)
            {
                startGroup();

                tok = _reader.getNextToken();

                if (tok.getType() == RtfToken.ASTERISK)
                {
                    parseFontDefinitionAsterisks(fontProperties);
                }
                else
                {
                    skipUntilEndOfGroup();
                }
            }
            else if (tok.getType() == RtfToken.CLOSEGROUP)
            {
                endGroup();
                break;
            }
        }

        _theFonts.defineFont(code, name, fontProperties);
    }

    private void parseFontDefinitionAsterisks(RtfFontProperties p_properties)
        throws IOException, NumberFormatException
    {
        RtfToken tok = _reader.getNextToken();

        if (tok.getType() == RtfToken.CONTROLWORD)
        {
            if (tok.getName().equals("panose"))
            {
                tok = _reader.getNextToken();

                p_properties.setPanose(tok.getData());
            }
            else if (tok.getName().equals("falt"))
            {
                tok = _reader.getNextToken();

                p_properties.setAltName(tok.getData());
            }
            else if (tok.getName().equals("fprq"))
            {
                int pitch = Integer.parseInt(tok.getData());
                p_properties.setPitch(pitch);
            }
        }

        skipUntilEndOfGroup();
    }

    //
    // Color Table
    //

    private void parseColorTable()
        throws IOException
    {
        RtfToken tok;
        String red = null, green = null, blue = null;

        for (;;)
        {
            tok = _reader.getNextToken();

            if (tok == null)
            {
                return;
            }

            if (tok.getType() == RtfToken.CONTROLWORD)
            {
                String name = tok.getName();

                if (name.equals("red"))
                {
                    red = tok.getData();
                }
                else if (name.equals("green"))
                {
                    green = tok.getData();
                }
                else if (name.equals("blue"))
                {
                    blue = tok.getData();
                }
            }
            else if (tok.getType() == RtfToken.DATA)
            {
                // the semicolon separator
                _theColors.defineColor(red, green, blue);
                red = green = blue = null;
            }
            else if (tok.getType() == RtfToken.CLOSEGROUP)
            {
                endGroup();
                return;
            }
        }
    }

    private void insertData(String data, boolean isUnicode)
    {
        if (_theParagraph == null)
        {
            _theParagraph = new RtfParagraph();
            _theParagraph.setProperties(_theParagraphProperties);
        }

        if (!isUnicode)
        {
            data = convertAnsiToUnicode(data, _theTextProperties);
        }
        
              
        if (_isSegmentId && checkWhiteSpace(data))
        {
        	data = data.trim();
        } 
        
        if (_isSegmentId)
        {
            _isSegmentId = false;       
        }
        
               
        if (checkSegmentIdWriter(data))
        {
            data = data.trim();
            _isSegmentId = true;
        }
               
        _theParagraph.add(new RtfText(data, _theTextProperties), isUnicode);
    }

    private void insertMarker(String marker)
    {
        if (_theParagraph == null)
        {
            _theParagraph = new RtfParagraph();
            _theParagraph.setProperties(_theParagraphProperties);
        }

        _theParagraph.add(new RtfMarker(marker, _theTextProperties));
    }

    private void insertTab()
    {
        if (_theParagraph == null)
        {
            _theParagraph = new RtfParagraph();
            _theParagraph.setProperties(_theParagraphProperties);
        }

        _theParagraph.add(new RtfTab());
    }

    private void insertLineBreak()
    {
        if (_theParagraph == null)
        {
            _theParagraph = new RtfParagraph();
            _theParagraph.setProperties(_theParagraphProperties);
        }

        _theParagraph.add(new RtfLineBreak());
    }

    private void processPict()
        throws IOException
    {
        StringBuffer buf = new StringBuffer();
        int depth = 1;

        RtfPicture picture = new RtfPicture(_pictureCounter);

        while (depth > 0)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                break;
            }

            int type = tok.getType();
            switch (type)
            {
            case RtfToken.DATA:                   // CvdL: pick it all up
                if (depth == 1)
                {
                    // Hexadecimal picture data should be broken into
                    // lines here.
                    buf.append(tok.getData());
                    buf.append("\n");
                }
                break;
            case RtfToken.OPENGROUP:
                startGroup();
                depth++;
                break;
            case RtfToken.CLOSEGROUP:
                endGroup();
                depth--;
                break;
            case RtfToken.CONTROLWORD:
            {
                String name = tok.getName();

                if (name.equals("picw"))
                {
                    picture.setPicw(Integer.parseInt(tok.getData()));
                }
                else if (name.equals("pich"))
                {
                    picture.setPich(Integer.parseInt(tok.getData()));
                }
                else if (name.equals("picwgoal"))
                {
                    picture.setPicwgoal(Integer.parseInt(tok.getData()));
                }
                else if (name.equals("pichgoal"))
                {
                    picture.setPichgoal(Integer.parseInt(tok.getData()));
                }
                else if (name.equals("picscalex"))
                {
                    picture.setPicscalex(Integer.parseInt(tok.getData()));
                }
                else if (name.equals("picscaley"))
                {
                    picture.setPicscaley(Integer.parseInt(tok.getData()));
                }
                else if (name.equals("piccropt"))
                {
                    picture.setPiccropt(Integer.parseInt(tok.getData()));
                }
                else if (name.equals("piccropb"))
                {
                    picture.setPiccropb(Integer.parseInt(tok.getData()));
                }
                else if (name.equals("piccropr"))
                {
                    picture.setPiccropr(Integer.parseInt(tok.getData()));
                }
                else if (name.equals("piccropl"))
                {
                    picture.setPiccropl(Integer.parseInt(tok.getData()));
                }
                else if (name.equals("emfblip") || name.equals("pngblip") ||
                    name.equals("jpegblip") || name.equals("macpict") ||
                    name.equals("pmmetafile") || name.equals("wmetafile")
                    /*dibitmap + data, wbitmap + data*/)
                {
                    picture.setPictType(name + tok.getData());
                }

                break;
            }
            }
        }

        picture.setPictData(buf.toString());

        if (_theParagraph == null)
        {
            _theParagraph = new RtfParagraph();
            _theParagraph.setProperties(_theParagraphProperties);
        }

        _theParagraph.add(picture);

        _theExternalEntities.addEntity(
            new RtfPictureExternalEntity(_pictureCounter, ""));

        _pictureCounter++;
    }

    private void processField()
        throws IOException
    {
        if (_debugStack)
        {
            debug(">FIELD stack depth = " +
                (_theTextPropertiesStack.size() - 1));
        }

        RtfObject instance = null;
        int depth = 1;

        while (depth > 0)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                break;
            }

            int type = tok.getType();
            switch (type)
            {
                case RtfToken.DATA:
                    break;
                case RtfToken.OPENGROUP:
                    startGroup();
                    depth++;
                    break;
                case RtfToken.CLOSEGROUP:
                    endGroup();
                    depth--;
                    break;
                case RtfToken.CONTROLWORD:
                    if (tok.getName().equals("fldinst"))
                    {
                        instance = processFieldInstance(depth-1);
                        depth = 1;
                    }
                    else if (tok.getName().equals("fldrslt"))
                    {
                        if (_no_field_result)
                        {
                            skipUntilEndOfGroup();
                            depth--;
                        }
                        else
                        {
                            // CvdL: TODO: last value of field
                            skipUntilEndOfGroup();
                            depth--;
                        }
                    }
                    break;
            }
        }

        if (instance != null)
        {
            if (_theParagraph == null)
            {
                _theParagraph = new RtfParagraph();
                _theParagraph.setProperties(_theParagraphProperties);
            }

            _theParagraph.add(instance);
        }

        if (_debugStack)
        {
            debug("<FIELD stack depth = " + _theTextPropertiesStack.size());
        }
    }

    static private String getCommutators(String data,
      CommutatorList commutators)
    {
        char indata[] = data.trim().toCharArray();
        StringBuffer buf = new StringBuffer();
        StringBuffer bufcommutator = null;
        boolean inquotedstring = false;
        boolean incommutator = false;

        for (int ii = 0; ii < indata.length; ii++)
        {
            char ch = indata[ii];
            if (inquotedstring)
            {
                if (ch == '"')
                {
                    inquotedstring = false;
                }
                buf.append(ch);
            }
            else if (incommutator)
            {
                if (Character.isSpaceChar(ch))
                {
                    commutators.addElement(bufcommutator.toString());
                    bufcommutator = null;
                    incommutator = false;
                }
                else if (ch == '"')
                {
                    commutators.addElement(bufcommutator.toString());
                    bufcommutator = null;
                    inquotedstring = true;
                    buf.append(ch);
                    incommutator = false;
                }
                else
                {
                    bufcommutator.append(ch);
                }
            }
            else
            {
                if (Character.isSpaceChar(ch))
                {
                    // nothing to do
                }
                else if (ch == '"')
                {
                    inquotedstring = true;
                    buf.append(ch);
                }
                else if (ch == '\\')
                {
                    bufcommutator = new StringBuffer();
                    bufcommutator.append(ch);
                    incommutator = true;
                }
                else
                {
                    // should not happen unless the user typed the
                    // hyperlink target manually in the field
                    buf.append(ch);
                }
            }
        }

        return buf.toString();
    }

    // responsible for closing depth times
    private RtfObject processFieldInstance(int depth)
        throws IOException
    {
        // SY 5/13/2005
        // Record text properties (b, i, u, etc) for RtfFieldInstance.
        // RtfFieldInstance is used for ptags in offline para-view.
        // With text properties in RtfFieldInstance, addable tags
        // won't get moved around.
        RtfTextProperties fieldTextProperties
            = (RtfTextProperties)_theTextProperties.clone();
        String data = getDataOfGroup(fieldTextProperties).trim();
        for (int i = 0; i < depth - 1; i++)
        {
            skipUntilEndOfGroup();
        }

        _no_field_result = false;

        if (data != null)
        {
            if (data.startsWith("SYMBOL"))
            {
                _no_field_result = true;
                return new RtfSymbol(data, _theTextProperties);
            }
            else if (data.startsWith("TOC"))
            {
                _no_field_result = true;
                return new RtfFieldInstance(data, fieldTextProperties);
            }
            else if (data.startsWith("INCLUDEPICTURE"))
            {
                data = data.substring(14).trim();
                CommutatorList commutators = new CommutatorList();
                data = getCommutators(data, commutators);
                if (data.startsWith("\""))
                {
                    data = data.substring(1);
                    int pos2 = data.indexOf('"');
                    if (pos2 != -1)
                    {
                        data = data.substring(0, pos2);
                    }
                }
                return new RtfPicture(data);
            }
            else if (data.startsWith("HYPERLINK"))
            {
                _no_field_result = true;

                String result = getResultOfField();

                return new RtfHyperLink(data, result);
            }
            else
            {
                return new RtfFieldInstance(data, fieldTextProperties);
            }
        }

        return null;
    }

    private String getResultOfField()
        throws IOException
    {
        int depth = 0;
        String result = null;
        while (! (result != null && depth == 0))
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                return null;
            }

            int type = tok.getType();
            switch (type)
            {
                case RtfToken.CONTROLWORD:
                {
                    String name = tok.getName();
                    if (name.equals("fldrslt"))
                    {
                         result = getDataOfGroup(null).trim();
                         depth--;
                    }
                }
                break;

                case RtfToken.OPENGROUP:
                {
                    startGroup();
                    depth++;
                }
                break;

                case RtfToken.CLOSEGROUP:
                {
                    endGroup();
                    depth--;
                }
                break;

                case RtfToken.ASTERISK:
                {
                    skipUntilEndOfGroup();
                    depth--;
                }
                break;
            }
        }
        return result;
    }

    /**
     * Handles a \trowd control which starts a table row. Note that
     * Word 2002 writes out the trowd information twice, before and
     * after the cell data, so we only create a new row if we're not
     * in a row already, and otherwise just set the row properties
     * twice. The row will be terminated by the \row control.
     */
    private void processStartRow()
    {
        if (_theContainer instanceof RtfRow)
        {
            _theRow.add(new RtfControl("trowd"));
        }
        else
        {
            if (_theParagraph != null)
            {
                _theParagraph.setProperties(_theParagraphProperties);
                _theContainer.add(_theParagraph);
                _theParagraph = null;
            }

            _theRow = new RtfRow();

            _theContainerStack.push(_theContainer);
            _theContainer = _theRow;

            // System.err.println("processStartRow() - added new row");
        }
    }

    /** Handles a \row control which terminates a row. */
    private void processRow()
    {
        _theContainer = _theContainerStack.pop();
        _theContainer.add(_theRow);
        _theRow = null;

        // System.err.println("processRow() - finished row - " + _theContainer);
    }

    /** Handles a \cell control which terminates a cell. */
    private void processCell()
    {
        _theCell = new RtfCell();

        if (_theParagraph != null)
        {
            _theParagraph.setProperties(_theParagraphProperties);
            _theCell.add(_theParagraph);
            _theParagraph = null;
        }

        _theRow.add(_theCell);
    }

    /**
     * Handles the info controls as defined in the RTF 1.8 spec.
     * Custom infos {\*\myinfo ...} are discarded.
     *
     * {\info } is a destination.
     */
    private void parseInfo()
        throws IOException
    {
        _inHeader = false;

        _theInfo = new RtfInfo();
        _theDocument.setInfo(_theInfo);

        int code = -1;
        StringBuffer value = new StringBuffer();

        for (;;)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                return;
            }

            if (tok.getType() == RtfToken.OPENGROUP)
            {
                startGroup();
                parseOneInfo();
            }
            else if (tok.getType() == RtfToken.CLOSEGROUP)
            {
                endGroup();
                return;
            }
            // Info values that are not embedded inside groups.
            else if (tok.getType() == RtfToken.CONTROLWORD)
            {
                if (tok.getName().equals("version"))
                {
                    code = RtfInfo.VERSION;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("vern"))
                {
                    code = RtfInfo.VERN;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("edmins"))
                {
                    code = RtfInfo.EDMINS;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("nofpages"))
                {
                    code = RtfInfo.NOFPAGES;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("nofwords"))
                {
                    code = RtfInfo.NOFWORDS;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("nofchars"))
                {
                    code = RtfInfo.NOFCHARS;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("nofcharsws"))
                {
                    code = RtfInfo.NOFCHARSWS;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("id"))
                {
                    code = RtfInfo.ID;

                    value.append(tok.getData());
                }

                if (code >= 0)
                {
                    _theInfo.defineProperty(code, value.toString());
                }
            }
        }
    }

    private void parseOneInfo()
        throws IOException
    {
        int code = -1;
        StringBuffer value = new StringBuffer();

        for (;;)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                break;
            }

            if (tok.getType() == RtfToken.CONTROLWORD)
            {
                if (tok.getName().equals("title"))
                {
                    code = RtfInfo.TITLE;
                }
                else if (tok.getName().equals("subject"))
                {
                    code = RtfInfo.SUBJECT;
                }
                else if (tok.getName().equals("author"))
                {
                    code = RtfInfo.AUTHOR;
                }
                else if (tok.getName().equals("manager"))
                {
                    code = RtfInfo.MANAGER;
                }
                else if (tok.getName().equals("company"))
                {
                    code = RtfInfo.COMPANY;
                }
                else if (tok.getName().equals("operator"))
                {
                    code = RtfInfo.OPERATOR;
                }
                else if (tok.getName().equals("category"))
                {
                    code = RtfInfo.CATEGORY;
                }
                else if (tok.getName().equals("keywords"))
                {
                    code = RtfInfo.KEYWORDS;
                }
                else if (tok.getName().equals("comment"))
                {
                    code = RtfInfo.COMMENT;
                }
                else if (tok.getName().equals("doccomm"))
                {
                    code = RtfInfo.DOCCOMM;
                }
                else if (tok.getName().equals("hlinkbase"))
                {
                    code = RtfInfo.HLINKBASE;
                }
                else if (tok.getName().equals("creatim"))
                {
                    code = RtfInfo.CREATIM;
                }
                else if (tok.getName().equals("revtim"))
                {
                    code = RtfInfo.REVTIM;
                }
                else if (tok.getName().equals("printim"))
                {
                    code = RtfInfo.PRINTIM;
                }
                else if (tok.getName().equals("buptim"))
                {
                    code = RtfInfo.BUPTIM;
                }
                else if (tok.getName().equals("version"))
                {
                    code = RtfInfo.VERSION;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("vern"))
                {
                    code = RtfInfo.VERN;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("edmins"))
                {
                    code = RtfInfo.EDMINS;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("nofpages"))
                {
                    code = RtfInfo.NOFPAGES;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("nofwords"))
                {
                    code = RtfInfo.NOFWORDS;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("nofchars"))
                {
                    code = RtfInfo.NOFCHARS;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("nofcharsws"))
                {
                    code = RtfInfo.NOFCHARSWS;

                    value.append(tok.getData());
                }
                else if (tok.getName().equals("id"))
                {
                    code = RtfInfo.ID;

                    value.append(tok.getData());
                }
                // date and time controls
                else if (tok.getName().equals("yr") ||
                    tok.getName().equals("mo") ||
                    tok.getName().equals("dy") ||
                    tok.getName().equals("hr") ||
                    tok.getName().equals("min"))
                {
                    value.append("\\");
                    value.append(tok.getName());
                    value.append(tok.getData());
                }
            }
            else if (tok.getType() == RtfToken.DATA)
            {
                value.append(tok.getData());
            }
            else if (tok.getType() == RtfToken.CLOSEGROUP)
            {
                endGroup();
                break;
            }
        }

        if (code >= 0)
        {
            _theInfo.defineProperty(code, value.toString());
        }
    }

    /**
     * CvdL: parses a top-level \*\control like {\*\pnseclvl ...}.
     */
    private boolean parseStarControlWord(RtfToken tok)
        throws IOException
    {
        String name = tok.getName();

        if (name.equals("pn") ||                  // Word 6
            name.equals("pnseclvl") ||            // ""
            name.equals("listtable") ||           // Word97
            name.equals("listoverridetable") ||   // ""
            name.equals("listpicture") ||         // Office 2002/2003?
            name.equals("pgptbl") ||              // Office 2002 (should read)
            name.equals("revtbl") ||              // Revisions
            name.equals("rsidtbl") ||             // Revisions
            name.equals("latentstyles") ||        // ??
            name.equals("protusertbl") ||         // User Protection
            name.equals("userprops"))             // User-defined Properties
        {
            RtfCompoundObject content = new RtfCompoundObject();
            content.add(new RtfControl("*"));
            content.add(new RtfControl(tok.getName(), tok.getData()));

            readUntilEndOfGroup(content);

            _theContainer.add(content);
            return true;
        }
        else if (
            name.equals("protstart") ||           // Document protection override.
            name.equals("protend"))               // Discard.
        {
            skipUntilEndOfGroup();
            return true;
        }
        else if (
            name.equals("oldcprops") ||           // Old properties for revisions
            name.equals("oldpprops") ||           // (Office 2003?) appearing within
            name.equals("oldtprops") ||           // normal text. Error out.
            name.equals("oldsprops"))
        {
            //readUntilEndOfGroup(content);
            //return true;

            // It's easier not to handle revision data because it is
            // embedded inside segments and would cause a plethora of
            // nonsense placeholders.  With change tracking on and
            // multiple authors it's very hard to automatically
            // accept/reject the changes leading to the final version.
            // So: all input files must be cleaned beforehand.
            throw new RuntimeException("RTF file contains revisions. " +
                "Please accept/reject the revisions and save the file with " +
                "'change tracking' set to off.");
        }
        else if (name.equals("shppict"))
        {
            RtfShapePicture content = new RtfShapePicture();
            content.add(new RtfControl("*"));
            content.add(new RtfControl(tok.getName(), tok.getData()));

            readUntilEndOfGroup(content);

            if (_theParagraph != null)
            {
                _theParagraph.add(content);
            }
            else
            {
                _theContainer.add(content);
            }
            return true;
        }
        else if (name.equals("bkmkstart") || name.equals("bkmkend"))
        {
            processBookmark(name.equals("bkmkstart"));
            return true;
        }
        else if (name.equals("atrfstart") || name.equals("atrfend"))
        {
            processAnnotationBookmark(name.equals("atrfstart"));
            return true;
        }
        else if (name.equals("annotation"))
        {
            processAnnotation(tok);
            return true;
        }
        else if (name.equals("atnref"))
        {
            processAnnotationRef();
            return true;
        }
        else if (name.equals("atnid"))
        {
            processAnnotationId();
            return true;
        }
        else if (name.equals("shpinst"))
        {
            processShpinst();
            return true;
        }
        else if (name.equals("docvar"))
        {
            processDocumentVariable();
            return true;
        }

        return false;
    }

    private void parseOleObject(RtfToken tok)
        throws IOException
    {
        RtfCompoundObject content = new RtfCompoundObject();
        content.add(new RtfControl(tok.getName(), tok.getData()));

        readUntilEndOfGroup(content);

        if (_theParagraph != null)
        {
            _theParagraph.add(content);
        }
        else
        {
            _theContainer.add(content);
        }
    }

    /**
     * Parses a \shp and \shpgrp and extracts paragraphs embedded in
     * \shptxt.
     */
    private void parseShape(RtfToken tok)
        throws IOException
    {
        RtfShape content = new RtfShape(tok.getName());

        readUntilEndOfShapeGroup(content);

        // Are shapes inline objects? Maybe not, but they appear
        // inside paragraph containers.
        if (_theParagraph == null)
        {
            _theParagraph = new RtfParagraph();
            _theParagraph.setProperties(_theParagraphProperties);
        }

        _theParagraph.add(content);
    }

    /**
     * Reads a group started by a {\shp ...}. All internal controls
     * are read, internal groups are preserved, and embedded
     * paragraphs in \shptxt are extracted.
     */
    private void readUntilEndOfShapeGroup(RtfCompoundObject p_content)
        throws IOException
    {
        int depth = 1;
        while (depth > 0)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                break;
            }

            int type = tok.getType();
            switch (type)
            {
                case RtfToken.DATA:
                    // CvdL: should not contain Unicode data and \\ucN.
                    // Note that we receive picture data as one long
                    // string which we have to break into smaller
                    // pieces on output ourselves, if we so wish.
                    p_content.add(new RtfData(tok.getData()));
                    break;
                case RtfToken.OPENGROUP:
                    startGroup();

                    RtfCompoundObject content = new RtfCompoundObject();
                    readUntilEndOfShapeGroup(content);
                    p_content.add(content);
                    break;
                case RtfToken.CLOSEGROUP:
                    endGroup();
                    depth--;
                    break;
                case RtfToken.ASTERISK:
                    p_content.add(new RtfControl("*"));
                    break;
                case RtfToken.CONTROLWORD:
                    String name = tok.getName();

                    if (name.equals("shprslt"))
                    {
                        skipUntilEndOfGroup();
                        depth--;
                    }
                    else if (name.equals("shptxt"))
                    {
                        RtfCompoundObject o = new RtfShapeText();
                        parseEmbeddedParagraphs(o);
                        p_content.add(o);
                        depth--;
                    }
                    else
                    {
                        p_content.add(new RtfControl(name, tok.getData()));
                    }
                    break;
            }
        }
    }

    /**
     * Reads a group started by a {\*\control ...}. All internal
     * controls are read and internal groups are preserved.
     */
    private void readUntilEndOfGroup(RtfCompoundObject p_content)
        throws IOException
    {
        int depth = 1;
        while (depth > 0)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                break;
            }

            int type = tok.getType();
            switch (type)
            {
                case RtfToken.DATA:
                    // CvdL: should not contain Unicode data and \\ucN.
                    // Note that we receive picture data as one long
                    // string which we have to break into smaller
                    // pieces on output ourselves, if we so wish.
                    p_content.add(new RtfData(tok.getData()));
                    break;
                case RtfToken.OPENGROUP:
                    startGroup();

                    RtfCompoundObject content = new RtfCompoundObject();
                    readUntilEndOfGroup(content);
                    p_content.add(content);
                    break;
                case RtfToken.CLOSEGROUP:
                    endGroup();
                    depth--;
                    break;
                case RtfToken.ASTERISK:
                    p_content.add(new RtfControl("*"));
                    break;
                case RtfToken.CONTROLWORD:
                    p_content.add(new RtfControl(tok.getName(), tok.getData()));
                    break;
            }
        }
    }

    private void processPn()
        throws IOException
    {
        for (;;)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                return;
            }

            switch (tok.getType())
            {
            case RtfToken.CONTROLWORD:
            {
                String name = tok.getName();

                if (name.equals("pnlvlblt"))
                {
                    _theParagraphProperties.setNumStyle(
                        RtfParagraphProperties.STYLE_BULLET);
                }
                else if (name.equals("pnlvlbody"))
                {
                    _theParagraphProperties.setNumStyle(
                        RtfParagraphProperties.STYLE_NUMBERED);
                }
            }
            break;

            case RtfToken.OPENGROUP:
                startGroup();
                processPn();
                break;

            case RtfToken.CLOSEGROUP:
                endGroup();
                return;

            default:
                break;
            }
        }
    }

    private void processBookmark(boolean p_start)
        throws IOException
    {
        // if (data.length() > 0 &&
        //    !data.startsWith("_Toc") && !data.startsWith("_H"))
        RtfBookmark bookmark = new RtfBookmark(p_start);
        StringBuffer bkmk = new StringBuffer();

        int depth = 1;
        while (depth > 0)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                break;
            }

            int type = tok.getType();
            switch (type)
            {
            case RtfToken.DATA:
                // CvdL: should not contain Unicode data and \\ucN.
                bkmk.append(convertAnsiToUnicode(
                    tok.getData(), _theTextProperties));

                break;
            case RtfToken.CONTROLWORD:
                String name = tok.getName();

                if (name.equals("bkmkcolf"))
                {
                    bookmark.setFirstCol(Integer.parseInt(tok.getData()));
                }
                else if (name.equals("bkmkcoll"))
                {
                    bookmark.setLastCol(Integer.parseInt(tok.getData()));
                }

                break;
            case RtfToken.OPENGROUP:
                startGroup();
                depth++;
                break;
            case RtfToken.CLOSEGROUP:
                endGroup();
                depth--;
                break;
            }
        }

        bookmark.setName(bkmk.toString());

        if (_theParagraph == null)
        {
            _theParagraph = new RtfParagraph();
            _theParagraph.setProperties(_theParagraphProperties); // ??
        }

        _theParagraph.add(bookmark);
    }

    private void processAnnotationBookmark(boolean p_start)
        throws IOException
    {
        RtfAnnotationBookmark bookmark = new RtfAnnotationBookmark(p_start);
        StringBuffer bkmk = new StringBuffer();

        int depth = 1;
        while (depth > 0)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                break;
            }

            int type = tok.getType();
            switch (type)
            {
            case RtfToken.DATA:
                bkmk.append(tok.getData());
                break;
            case RtfToken.CONTROLWORD:
                break;
            case RtfToken.OPENGROUP:
                startGroup();
                depth++;
                break;
            case RtfToken.CLOSEGROUP:
                endGroup();
                depth--;
                break;
            }
        }

        bookmark.setName(bkmk.toString());

        if (_theParagraph == null)
        {
            _theParagraph = new RtfParagraph();
            _theParagraph.setProperties(_theParagraphProperties); // ??
        }

        _theParagraph.add(bookmark);
    }

    private void processAnnotationRef()
        throws IOException
    {
        StringBuffer ref = new StringBuffer();

        int depth = 1;
        while (depth > 0)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                break;
            }

            int type = tok.getType();
            switch (type)
            {
            case RtfToken.DATA:
                ref.append(tok.getData());
                break;
            case RtfToken.CONTROLWORD:
                break;
            case RtfToken.OPENGROUP:
                startGroup();
                depth++;
                break;
            case RtfToken.CLOSEGROUP:
                endGroup();
                depth--;
                break;
            }
        }

        ((RtfAnnotation)_theContainer).setRef(ref.toString());
    }

    private void processAnnotationId()
        throws IOException
    {
        StringBuffer id = new StringBuffer();

        int depth = 1;
        while (depth > 0)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                break;
            }

            int type = tok.getType();
            switch (type)
            {
            case RtfToken.DATA:
                id.append(tok.getData());
                break;
            case RtfToken.CONTROLWORD:
                break;
            case RtfToken.OPENGROUP:
                startGroup();
                depth++;
                break;
            case RtfToken.CLOSEGROUP:
                endGroup();
                depth--;
                break;
            }
        }

        _annotationId = id.toString();
    }

    /**
     * Processes an annotation or comment destination. Annotations are
     * embedded in the paragraph like footnotes.
     */
    private void processAnnotation(RtfToken p_tok)
        throws IOException
    {
        RtfCompoundObject content = new RtfAnnotation();
        // Set any annotation ID that was read before the annotation
        // itself and then reset it for the next annotation to come.
        ((RtfAnnotation)content).setId(_annotationId);
        _annotationId = null;

        RtfParagraph tempPara;
        RtfParagraphProperties tempProps;

        if (_theParagraph == null)
        {
            _theParagraph = new RtfParagraph();
            _theParagraph.setProperties(_theParagraphProperties);
        }

        tempPara = _theParagraph;
        tempProps = _theParagraphProperties;

        _theParagraph = new RtfParagraph();
        _theParagraphProperties = new RtfParagraphProperties();
        _theParagraph.setProperties(_theParagraphProperties);

        _theContainerStack.push(_theContainer);
        _theContainer = content;

        // Paragraph destinations start with an empty text context
        _theTextPropertiesStack.push(_theTextProperties);
        _theTextProperties = new RtfTextProperties();

        if (_debugStack)
        {
            debug("--> parse embedded annotation");
        }

        parse(1);

        //content.add(_theParagraph);
        tempPara.add(content);

        _theParagraph = tempPara;
        _theParagraphProperties = tempProps;

        _theTextProperties = _theTextPropertiesStack.pop();
        _theContainer = _theContainerStack.pop();
    }

    private void processShpinst()
        throws IOException
    {
        int depth = 1;
        boolean done = false;

        while (depth > 0)
        {
            RtfToken tok = _reader.getNextToken();

            if (tok == null)
            {
                return;
            }

            switch (tok.getType())
            {
                case RtfToken.CONTROLWORD:
                {
                    String name = tok.getName();
                    if (!done && name.equals("pict"))
                    {
                        processPict();
                        depth--;
                        done = true;
                    }
                }
                break;

                case RtfToken.OPENGROUP:
                    startGroup();
                    depth++;
                    break;

                case RtfToken.CLOSEGROUP:
                    endGroup();
                    depth--;
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Parses a {\*\docvar {name}{value} } group.
     */
    private void processDocumentVariable()
        throws IOException
    {
        boolean getName = true;
        StringBuffer name = new StringBuffer();
        StringBuffer value = new StringBuffer();

        int depth = 1;
        while (depth > 0)
        {
            RtfToken tok = _reader.getNextToken();
            if (tok == null)
            {
                break;
            }

            int type = tok.getType();
            switch (type)
            {
                case RtfToken.DATA:
                    // CvdL: should not contain Unicode data and \\ucN.
                    if (getName)
                    {
                        name.append(convertAnsiToUnicode(
                            tok.getData(), _theTextProperties));
                    }
                    else
                    {
                        value.append(convertAnsiToUnicode(
                            tok.getData(), _theTextProperties));
                    }
                    break;
                case RtfToken.OPENGROUP:
                    startGroup();
                    depth++;
                    break;
                case RtfToken.CLOSEGROUP:
                    endGroup();
                    depth--;
                    // got name, now get the value
                    getName = false;
                    break;
            }
        }

        _theVariables.addVariable(
            new RtfVariable(name.toString(), value.toString()));
    }

    /**
     * Parses a header destination - which contains a paragraph.
     */
    private void parseHeaderFooter(String p_type)
        throws IOException
    {
        RtfCompoundObject content = new RtfCompoundObject();

        if (_theParagraph != null)
        {
            _theParagraph.setProperties(_theParagraphProperties);
            _theContainer.add(_theParagraph);
            _theParagraph = null;
        }

        _theContainerStack.push(_theContainer);
        _theContainer = content;

        _theContainer.add(new RtfControl(p_type));

        if (_debugStack)
        {
            debug("--> parse header footer");
        }

        parse(1);

        _theContainer = _theContainerStack.pop();
        _theContainer.add(content);
    }

    /**
     * Parses a footnote destination (an inline destination) - which
     * contains a list of paragraphs. Adds the footnote to the
     * surrounding paragraph.
     */
    private void parseFootnote()
        throws IOException
    {
        RtfCompoundObject content = new RtfFootnote();

        RtfParagraph tempPara;
        RtfParagraphProperties tempProps;

        if (_theParagraph == null)
        {
            _theParagraph = new RtfParagraph();
            _theParagraph.setProperties(_theParagraphProperties);
        }

        tempPara = _theParagraph;
        tempProps = _theParagraphProperties;

        _theParagraph = new RtfParagraph();
        _theParagraphProperties = new RtfParagraphProperties();
        _theParagraph.setProperties(_theParagraphProperties);

        _theContainerStack.push(_theContainer);
        _theContainer = content;

        // Paragraph destinations start with an empty text context
        _theTextPropertiesStack.push(_theTextProperties);
        _theTextProperties = new RtfTextProperties();

        if (_debugStack)
        {
            debug("--> parse embedded footnote");
        }

        parse(1);

        if (_theParagraph != null)
        {
            _theContainer.add(_theParagraph);
            _theParagraph = null;
        }

        content.add(_theParagraph);

        _theParagraph = tempPara;
        _theParagraphProperties = tempProps;

        _theTextProperties = _theTextPropertiesStack.pop();
        _theContainer = _theContainerStack.pop();
    }

    /**
     * Parses a list of paragraphs embedded in a destination like
     * \shptxt. Does not add the object to any paragraph but leaves
     * this to the caller.
     */
    private void parseEmbeddedParagraphs(RtfCompoundObject p_container)
        throws IOException
    {
        RtfParagraph tempPara;
        RtfParagraphProperties tempProps;

        tempPara = _theParagraph;
        tempProps = _theParagraphProperties;

        _theParagraph = new RtfParagraph();
        _theParagraphProperties = new RtfParagraphProperties();
        _theParagraph.setProperties(_theParagraphProperties);

        _theContainerStack.push(_theContainer);
        _theContainer = p_container;

        // Paragraph destinations start with an empty text context
        _theTextPropertiesStack.push(_theTextProperties);
        _theTextProperties = new RtfTextProperties();

        if (_debugStack)
        {
            debug("--> parse embedded paragraphs");
        }

        parse(1);

        if (_theParagraph != null)
        {
            _theContainer.add(_theParagraph);
            _theParagraph = null;
        }

        _theParagraph = tempPara;
        _theParagraphProperties = tempProps;

        _theTextProperties = _theTextPropertiesStack.pop();
        _theContainer = _theContainerStack.pop();
    }

    /**
     * Helper method to turn a boolean control's value into a boolean.
     * \b = true, \b1 = true, and \b0 = false.
     */
    private boolean getSymbolValue(String p_data)
    {
        if (p_data == null || p_data.length() == 0)
        {
            return true;
        }

        int value = Integer.parseInt(p_data);

        if (value == 0)
        {
            return false;
        }

        return true;
    }

    /**
     * Method to map ANSI data (\'xy) to Unicode according to the font
     * and its codepage currently in effect. This is necessary for
     * Arabic, Cyrillic, etc.
     *
     * This method expects incoming strings to contain 1 char only.
     */
    private String convertAnsiToUnicode(String p_text,
        RtfTextProperties p_properties)
    {
        String result = p_text;

        try
        {
            int fontnum = p_properties.isFontSet() ? p_properties.getFont() :
                _theDocument.getFont();

            RtfFontProperties fontprops =
                _theDocument.getFontTable().getFontProperties(fontnum);

            // Charset code: 0=ANSI 1=Default 2=Symbol 3=Invalid 77=Mac
            // 128=Shift Jis 129=Hangul 130=Johab 134=GB2312 136=Big5
            // 161=Greek 162=Turkish 163=Vietnamese 177=Hebrew 178=Arabic
            // 179=Arabic Traditional 180=Arabic user 181=Hebrew user
            // 186=Baltic 204=Russian 222=Thai 238=Eastern European
            // 254=PC 437 255=OEM.

            int charset = fontprops.getCharset();

            String codeset = CodesetMapper.getJavaEncodingFromCharset(charset);

            // Unknown or symbol codeset...
            if (codeset == null)
            {
                if (_debugConversion)
                {
                    debug("Cannot convert text in charset " + charset + ": " +
                        hexEncode(p_text));
                }

                return p_text;
            }

            if (_debugConversion)
            {
                debug("ANSI Conversion for `" + hexEncode(p_text) +
                    "' in charset " + charset + ", codeset " + codeset);
            }

            byte[] bs = p_text.getBytes("iso-8859-1");
            result = new String(bs, codeset);           

            if (_debugConversion)
            {
                debug("Unicode result = `" + hexEncode(result) + "'");
            }
        }
        catch (Throwable t)
        {
            warn("Error in ANSI conversion: " + t);
        }

        return result;
    }

    /**
     * Debug method to print strings in hex.
     */
    public static String hexEncode(String s)
    {
        StringBuffer ret = new StringBuffer();
        char[] ZERO_ARRAY = {'0', '0', '0', '0'};

        for (int i = 0; i < s.length(); ++i)
        {
            Character c = new Character(s.charAt(i));

            if (c.charValue() > 127)
            {
                String hex = Integer.toHexString(c.charValue());
                ret.append("\\u");
                int len = hex.length();
                if (len < 4)
                {
                    ret.append(ZERO_ARRAY, 0, 4 - len);
                }
                ret.append(hex);
            }
            else
            {
                ret.append(c);
            }
        }

        return ret.toString();
    }

    /**
     * Call this when a group is opened "{".
     */
    private void startGroup()
    {
        _theTextPropertiesStack.push(_theTextProperties);
        _theTextProperties = (RtfTextProperties)
            _theTextProperties.clone();
    }

    /**
     * Call this when a group is closed "}".
     */
    private void endGroup()
    {
        _theTextProperties = _theTextPropertiesStack.pop();
    }


    /**
     * set text properties. Used to record text properties of fields.
     */
    private void setTextProperties(
        RtfToken p_tok, RtfTextProperties p_properties)
    {
        if (p_tok.getName().equals("b"))
        {
            p_properties.setBold(getSymbolValue(p_tok.getData()));
        }
        else if (p_tok.getName().equals("i"))
        {
            p_properties.setItalic(getSymbolValue(p_tok.getData()));
        }
        else if (p_tok.getName().equals("v"))
        {
            p_properties.setHidden(getSymbolValue(p_tok.getData()));
        }
        else if (p_tok.getName().equals("ul"))
        {
            p_properties.setUnderlined(getSymbolValue(p_tok.getData()));
        }
        else if (p_tok.getName().equals("cf"))
        {
            int color = Integer.parseInt(p_tok.getData());
            p_properties.setColor(color);
        }
        else if (p_tok.getName().equals("uc"))
        {
            int skipCount = Integer.parseInt(p_tok.getData());
            p_properties.setSkipCount(skipCount);
        }
        else if (p_tok.getName().equals("f"))
        {
            int font = Integer.parseInt(p_tok.getData());
            p_properties.setFont(font);
        }
        else if (p_tok.getName().equals("fs"))
        {
            int fontSize = Integer.parseInt(p_tok.getData());
            p_properties.setFontSize(fontSize);
        }
        else if (p_tok.getName().equals("lang"))
        {
            int lang = Integer.parseInt(p_tok.getData());
            p_properties.setLang(lang);
        }
    }


    //
    // Local Classes
    //

    private class CommutatorList
        extends Vector
    {
        private static final long serialVersionUID = -5882381262325384714L;

        boolean isCommutatorDefined(String commutator)
        {
            for (Enumeration enumeration = elements(); enumeration.hasMoreElements(); )
            {
                if (enumeration.nextElement().equals(commutator))
                {
                    return true;
                }
            }

            return false;
        }
    }

    private boolean checkSegmentIdWriter(String segment)
    {
        // Use to match like"# 2299 "
        // String regex = "[\\w\\s.,:?()\\\\@]{0,}#\\s\\d{4,}\\s{0,}";
        String regex = "[\\S\\s]{0,}#\\s{0,}\\d{4,}\\s{0,}";
        boolean match = segment.matches(regex);
        return match;
    }
    
    private boolean checkWhiteSpace(String data)
    {
        String regex = "(^\\s*)|(\\s*$)";
        return data.matches(regex);
    }
}
