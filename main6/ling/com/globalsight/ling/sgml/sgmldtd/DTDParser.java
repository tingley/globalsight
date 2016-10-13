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
package com.globalsight.ling.sgml.sgmldtd;

import java.text.MessageFormat;
import java.util.*;
import java.io.*;
import java.net.*;

import com.globalsight.ling.sgml.catalog.Catalog;

/**
 * Parses a DTD file and returns a DTD object
 * 
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2009/04/14 15:09:39 $ by $Author: yorkjin $
 */
public class DTDParser implements EntityExpansion
{
    protected Scanner scanner;
    protected DTD dtd;
    protected Object defaultLocation;
    protected Catalog catalog;
    protected boolean caseless;

    /** Creates a parser that will read from the specified Reader object */
    public DTDParser(Reader in)
    {
        scanner = new Scanner(in, false, this);
        dtd = new DTD();
        caseless = true;
    }

    /**
     * Creates a parser that will read from the specified Reader object
     * 
     * @param in
     *            The input stream to read
     * @param trace
     *            True if the parser should print out tokens as it reads them
     *            (used for debugging the parser)
     */
    public DTDParser(Reader in, boolean trace)
    {
        scanner = new Scanner(in, trace, this);
        dtd = new DTD();
    }

    /** Creates a parser that will read from the specified File object */
    public DTDParser(File in) throws IOException
    {
        defaultLocation = in.getParentFile();

        scanner = new Scanner(new BufferedReader(new FileReader(in)), false,
                this);
        dtd = new DTD();
        caseless = true;
    }

    /**
     * Creates a parser that will read from the specified File object
     * 
     * @param in
     *            The file to read
     * @param trace
     *            True if the parser should print out tokens as it reads them
     *            (used for debugging the parser)
     */
    public DTDParser(File in, boolean trace) throws IOException
    {
        defaultLocation = in.getParentFile();

        scanner = new Scanner(new BufferedReader(new FileReader(in)), trace,
                this);
        dtd = new DTD();
        caseless = true;
    }

    /** Creates a parser that will read from the specified URL object */
    public DTDParser(URL in) throws IOException
    {
        // LAM: we need to set the defaultLocation to the directory where
        // the sgmldtd is found so that we don't run into problems parsing any
        // relative external files referenced by the sgmldtd.
        try
        {
            String file = in.toURI().getPath();
            defaultLocation = new URL(in.getProtocol(), in.getHost(), in.getPort(),
                    file.substring(0, file.lastIndexOf('/') + 1));
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        scanner = new Scanner(new BufferedReader(new InputStreamReader(in
                .openStream())), false, this);
        dtd = new DTD();
        caseless = true;
    }

    /**
     * Creates a parser that will read from the specified URL object
     * 
     * @param in
     *            The URL to read
     * @param trace
     *            True if the parser should print out tokens as it reads them
     *            (used for debugging the parser)
     */
    public DTDParser(URL in, boolean trace) throws IOException
    {
        // LAM: we need to set the defaultLocation to the directory where
        // the sgmldtd is found so that we don't run into problems parsing any
        // relative external files referenced by the sgmldtd.
        try
        {
            String file = in.toURI().getPath();
            defaultLocation = new URL(in.getProtocol(), in.getHost(), in
                    .getPort(), file.substring(0, file.lastIndexOf('/') + 1));

            scanner = new Scanner(new BufferedReader(new InputStreamReader(in
                    .openStream())), trace, this);
            dtd = new DTD();
            caseless = true;
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    /** Creates a parser that will read from the specified Reader object */
    public DTDParser(Reader in, Catalog catalog)
    {
        this.catalog = catalog;
        scanner = new Scanner(in, false, this);
        dtd = new DTD();
        caseless = true;
    }

    /**
     * Creates a parser that will read from the specified Reader object
     * 
     * @param in
     *            The input stream to read
     * @param trace
     *            True if the parser should print out tokens as it reads them
     *            (used for debugging the parser)
     */
    public DTDParser(Reader in, Catalog catalog, boolean trace)
    {
        this.catalog = catalog;
        scanner = new Scanner(in, trace, this);
        dtd = new DTD();
        caseless = true;
    }

    /** Creates a parser that will read from the specified File object */
    public DTDParser(File in, Catalog catalog) throws IOException
    {
        this.catalog = catalog;
        defaultLocation = in.getParentFile();

        scanner = new Scanner(new BufferedReader(new FileReader(in)), false,
                this);
        dtd = new DTD();
        caseless = true;
    }

    /**
     * Creates a parser that will read from the specified File object
     * 
     * @param in
     *            The file to read
     * @param trace
     *            True if the parser should print out tokens as it reads them
     *            (used for debugging the parser)
     */
    public DTDParser(File in, Catalog catalog, boolean trace)
            throws IOException
    {
        this.catalog = catalog;
        defaultLocation = in.getParentFile();

        scanner = new Scanner(new BufferedReader(new FileReader(in)), trace,
                this);
        dtd = new DTD();
        caseless = true;
    }

    /** Creates a parser that will read from the specified URL object */
    public DTDParser(URL in, Catalog catalog) throws IOException
    {
        this.catalog = catalog;

        // LAM: we need to set the defaultLocation to the directory where
        // the sgmldtd is found so that we don't run into problems parsing any
        // relative external files referenced by the sgmldtd.
        try
        {
            String file = in.toURI().getPath();
            defaultLocation = new URL(in.getProtocol(), in.getHost(), in
                    .getPort(), file.substring(0, file.lastIndexOf('/') + 1));
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        scanner = new Scanner(new BufferedReader(new InputStreamReader(in
                .openStream())), false, this);
        dtd = new DTD();
        caseless = true;
    }

    /**
     * Creates a parser that will read from the specified URL object
     * 
     * @param in
     *            The URL to read
     * @param trace
     *            True if the parser should print out tokens as it reads them
     *            (used for debugging the parser)
     */
    public DTDParser(URL in, Catalog catalog, boolean trace) throws IOException
    {
        this.catalog = catalog;
        // LAM: we need to set the defaultLocation to the directory where
        // the sgmldtd is found so that we don't run into problems parsing any
        // relative external files referenced by the sgmldtd.
        try
        {
            String file = in.toURI().getPath();
            defaultLocation = new URL(in.getProtocol(), in.getHost(), in
                    .getPort(), file.substring(0, file.lastIndexOf('/') + 1));
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        scanner = new Scanner(new BufferedReader(new InputStreamReader(in
                .openStream())), trace, this);
        dtd = new DTD();
        caseless = true;
    }

    /**
     * Returns true if keywords should be evaluated without respect to case.
     */
    public boolean isCaseless()
    {
        return caseless;
    }

    /**
     * Sets the caseless flag (true means that casing in keywords should be
     * ignored
     */
    public void setCaseless(boolean flag)
    {
        caseless = flag;
    }

    /**
     * Parses the DTD file and returns a DTD object describing the DTD. This
     * invocation of parse does not try to guess the root element (for
     * efficiency reasons)
     */
    public DTD parse() throws IOException
    {
        return parse(false);
    }

    /**
     * Parses the DTD file and returns a DTD object describing the DTD.
     * 
     * @param guessRootElement
     *            If true, tells the parser to try to guess the root element of
     *            the document by process of elimination
     */
    public DTD parse(boolean guessRootElement) throws IOException
    {
        Token token;

        for (;;)
        {
            token = scanner.peek();

            if (token.type == Scanner.EOF)
                break;

            parseTopLevelElement();
        }

        if (guessRootElement)
        {
            Hashtable roots = new Hashtable();

            Enumeration e = dtd.elements.elements();

            while (e.hasMoreElements())
            {
                DTDElement element = (DTDElement) e.nextElement();
                roots.put(element.name, element);
            }

            e = dtd.elements.elements();
            while (e.hasMoreElements())
            {
                DTDElement element = (DTDElement) e.nextElement();
                if (!(element.content instanceof DTDContainer))
                    continue;

                Enumeration items = ((DTDContainer) element.content)
                        .getItemsVec().elements();

                while (items.hasMoreElements())
                {
                    removeElements(roots, dtd, (DTDItem) items.nextElement());
                }
            }

            if (roots.size() == 1)
            {
                e = roots.elements();
                dtd.rootElement = (DTDElement) e.nextElement();
            }
            else
            {
                dtd.rootElement = null;
            }
        }
        else
        {
            dtd.rootElement = null;
        }

        return dtd;
    }

    protected void removeElements(Hashtable h, DTD dtd, DTDItem item)
    {
        if (item instanceof DTDName)
        {
            h.remove(((DTDName) item).value);
        }
        else if (item instanceof DTDContainer)
        {
            Enumeration e = ((DTDContainer) item).getItemsVec().elements();

            while (e.hasMoreElements())
            {
                removeElements(h, dtd, (DTDItem) e.nextElement());
            }
        }
    }

    protected void parseTopLevelElement() throws IOException
    {
        Token token = scanner.get();

        // Is <? xxx ?> even valid in a DTD? I'll ignore it just in case it's
        // there
        if (token.type == Scanner.LTQUES)
        {
            StringBuffer textBuffer = new StringBuffer();

            for (;;)
            {
                String text = scanner.getUntil('?');
                textBuffer.append(text);

                token = scanner.peek();
                if (token.type == Scanner.GT)
                {
                    scanner.get();
                    break;
                }
                textBuffer.append('?');
            }
            DTDProcessingInstruction instruct = new DTDProcessingInstruction(
                    textBuffer.toString());

            dtd.items.addElement(instruct);

            return;
        }
        else if (token.type == Scanner.CONDITIONAL)
        {
            token = expect(Scanner.IDENTIFIER);

            if (isKeywordEqual(token.value, "IGNORE"))
            {
                scanner.skipConditional();
            }
            else
            {
                if (isKeywordEqual(token.value, "INCLUDE"))
                {
                    scanner.skipUntil('[');
                }
                else
                {
                    throw new DTDParseException(scanner.getUriId(),
                            MessageFormat.format(Messages
                                    .getString("DTDParser.1"),
                                    new Object[] { token.value }), scanner
                                    .getLineNumber(), scanner.getColumn());
                }
            }
        }
        else if (token.type == Scanner.ENDCONDITIONAL)
        {
            // Don't need to do anything for this token
        }
        else if (token.type == Scanner.COMMENT)
        {
            dtd.items.addElement(new DTDComment(token.value));
        }
        else if (token.type == Scanner.LTBANG)
        {

            token = expect(Scanner.IDENTIFIER);

            if (isKeywordEqual(token.value, "ELEMENT"))
            {
                parseElement();
            }
            else if (isKeywordEqual(token.value, "ATTLIST"))
            {
                parseAttlist();
            }
            else if (isKeywordEqual(token.value, "ENTITY"))
            {
                parseEntity();
            }
            else if (isKeywordEqual(token.value, "NOTATION"))
            {
                parseNotation();
            }
            else
            {
                skipUntil(Scanner.GT);
            }
        }
        else
        {
            // MAW Version 1.17
            // Previously, the parser would skip over unexpected tokens at the
            // upper level. Some invalid DTDs would still show up as valid.
            throw new DTDParseException(scanner.getUriId(), MessageFormat
                    .format(Messages.getString("DTDParser.2"),
                            new Object[] { token.type.name + "(" + token.value
                                    + ")" }), scanner.getLineNumber(), scanner
                    .getColumn());
        }
    }

    protected void skipUntil(TokenType stopToken) throws IOException
    {
        Token token = scanner.get();

        while (token.type != stopToken)
        {
            token = scanner.get();
        }
    }

    protected Token expect(TokenType expected) throws IOException
    {
        Token token = scanner.get();

        if (token.type != expected)
        {
            if (token.value == null)
            {
                throw new DTDParseException(scanner.getUriId(),
                        MessageFormat
                                .format(Messages.getString("DTDParser.3"),
                                        new Object[] { expected.name,
                                                token.type.name }), scanner
                                .getLineNumber(), scanner.getColumn());
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.3"),
                                new Object[] {
                                        expected.name,
                                        token.type.name + "(" + token.value
                                                + ")" }), scanner
                        .getLineNumber(), scanner.getColumn());
            }
        }

        return token;
    }

    protected Vector parseElementGroup() throws IOException
    {
        Token token = scanner.peek();
        if (token.type == Scanner.IDENTIFIER)
        {
            scanner.get(); // consume peeked token
            Vector elements = new Vector();
            elements.addElement(token);
            return elements;
        }
        else if (token.type == Scanner.LPAREN)
        {
            return parseElementList();
        }
        else
        {
            throw new DTDParseException(scanner.getUriId(), MessageFormat
                    .format(Messages.getString("DTDParser.4"),
                            new Object[] { token.value }), scanner
                    .getLineNumber(), scanner.getColumn());
        }
    }

    protected Vector parseElementList() throws IOException
    {
        Vector elements = new Vector();
        Token token = null;

        expect(Scanner.LPAREN);

        for (;;)
        {
            token = scanner.get();
            if (token.type == Scanner.IDENTIFIER)
            {
                elements.addElement(token);
                token = scanner.get();
                if (token.type == Scanner.PIPE)
                {
                    continue;
                }
                else if (token.type == Scanner.RPAREN)
                {
                    return elements;
                }
                else
                {
                    throw new DTDParseException(scanner.getUriId(),
                            MessageFormat.format(Messages
                                    .getString("DTDParser.5"),
                                    new Object[] { token.value }), scanner
                                    .getLineNumber(), scanner.getColumn());
                }
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.6"),
                                new Object[] { token.value }), scanner
                        .getLineNumber(), scanner.getColumn());
            }
        }
    }

    protected Vector parseExceptionList() throws IOException
    {
        Vector elements = new Vector();
        Token token = null;

        for (;;)
        {
            token = scanner.get();
            if (token.type == Scanner.IDENTIFIER)
            {
                elements.addElement(token);
                token = scanner.get();
                if (token.type == Scanner.PIPE || token.type == Scanner.COMMA)
                {
                    continue;
                }
                else if (token.type == Scanner.RPAREN)
                {
                    return elements;
                }
                else
                {
                    throw new DTDParseException(scanner.getUriId(),
                            MessageFormat.format(Messages
                                    .getString("DTDParser.7"),
                                    new Object[] { token.value }), scanner
                                    .getLineNumber(), scanner.getColumn());
                }
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.6"),
                                new Object[] { token.value }), scanner
                        .getLineNumber(), scanner.getColumn());
            }
        }
    }

    protected void parseElement() throws IOException
    {
        Vector elements = parseElementGroup();
        DTDItem content = null;
        boolean startTagOptional = true;
        boolean endTagOptional = true;
        Vector inclusions = null;
        Vector exclusions = null;

        for (Enumeration e = elements.elements(); e.hasMoreElements();)
        {
            Token name = (Token) e.nextElement();

            DTDElement element = (DTDElement) dtd.elements.get(name.value);

            if (element == null)
            {
                element = new DTDElement(name.value);
                dtd.elements.put(element.name, element);
            }
            else if (element.content != null)
            {
                // 070501 MAW: Since the ATTLIST tag can also cause an element
                // to be created,
                // only throw this exception if the element has content defined,
                // which
                // won't happen when you just create an ATTLIST. Thanks to
                // Jags Krishnamurthy of Object Edge for pointing out this
                // problem -
                // originally the parser would let you define an element more
                // than once.
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.8"),
                                new Object[] { name.value }), scanner
                        .getLineNumber(), scanner.getColumn());
            }

            dtd.items.addElement(element);
            if (content == null)
            {
                parseElementOptionality(element);
                startTagOptional = element.startTagOptional;
                endTagOptional = element.endTagOptional;

                parseContentSpec(element);
                content = element.content;

                parseExceptions(element);
                inclusions = element.inclusions;
                exclusions = element.exclusions;
            }
            else
            {
                element.content = content;
                element.startTagOptional = startTagOptional;
                element.endTagOptional = endTagOptional;
                element.inclusions = inclusions;
                element.exclusions = exclusions;
            }
        }

        expect(Scanner.GT);
    }

    protected void parseElementOptionality(DTDElement element)
            throws IOException
    {
        Token token = scanner.peek();

        if (token.type == Scanner.NMTOKEN || token.type == Scanner.IDENTIFIER)
        {
            if (token.value.equals("-"))
            {
                element.startTagOptional = false;
            }
            else if (isKeywordEqual(token.value, "O"))
            {
                element.startTagOptional = true;
            }
            else
            {
                element.startTagOptional = false;
                element.endTagOptional = false;
                return;
            }
        }
        else
        {
            element.startTagOptional = false;
            element.endTagOptional = false;
            return;
        }

        // Skip the previous token that was examined via peek
        token = scanner.get();

        token = scanner.get();

        if (token.type != Scanner.NMTOKEN && token.type != Scanner.IDENTIFIER)
        {
            if (token.value != null)
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.9"),
                                new Object[] { token.value }), scanner
                        .getLineNumber(), scanner.getColumn());
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.9"),
                                new Object[] { token.type.name }), scanner
                        .getLineNumber(), scanner.getColumn());
            }
        }
        if (token.value.equals("-"))
        {
            element.endTagOptional = false;
        }
        else if (isKeywordEqual(token.value, "O"))
        {
            element.endTagOptional = true;
        }
        else
        {
            if (token.value != null)
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.9"),
                                new Object[] { token.value }), scanner
                        .getLineNumber(), scanner.getColumn());
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.9"),
                                new Object[] { token.type.name }), scanner
                        .getLineNumber(), scanner.getColumn());
            }
        }
    }

    protected void parseContentSpec(DTDElement element) throws IOException
    {
        Token token = scanner.get();

        if (token.type == Scanner.IDENTIFIER)
        {
            if (isKeywordEqual(token.value, "EMPTY"))
            {
                element.content = new DTDEmpty();
            }
            else if (isKeywordEqual(token.value, "ANY"))
            {
                element.content = new DTDAny();
            }
            else if (isKeywordEqual(token.value, "CDATA"))
            {
                element.content = new DTDAny();
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.10"),
                                new Object[] { token.value }), scanner
                        .getLineNumber(), scanner.getColumn());
            }
        }
        else if (token.type == Scanner.LPAREN)
        {
            token = scanner.peek();

            if (token.type == Scanner.IDENTIFIER)
            {
                parseChildren(element);
            }
            else if (token.type == Scanner.LPAREN)
            {
                parseChildren(element);
            }
        }
    }

    protected void parseExceptions(DTDElement element) throws IOException
    {
        boolean parsedExclusions = false;
        boolean parsedInclusions = false;

        Token token = scanner.peek();

        if (token.type == Scanner.GT)
        {
            return;
        }
        else if (token.type == Scanner.MINUS_LPAREN)
        {
            scanner.get(); // consume peeked token
            element.exclusions = parseExceptionList();
            parsedExclusions = true;
        }
        else if (token.type == Scanner.PLUS_LPAREN)
        {
            scanner.get(); // Skip peeked token
            element.inclusions = parseExceptionList();
            parsedInclusions = true;
        }
        else
        {
            return;
        }

        token = scanner.peek();

        if (!parsedInclusions && (token.type == Scanner.PLUS_LPAREN))
        {
            scanner.get(); // Skip peeked token
            element.inclusions = parseExceptionList();
        }
        else if (!parsedExclusions && (token.type == Scanner.MINUS_LPAREN))
        {
            scanner.get(); // consume peeked token
            element.exclusions = parseExceptionList();
        }
    }

    protected void parseMixed(DTDElement element) throws IOException
    {
        DTDMixed mixed = new DTDMixed();

        mixed.add(new DTDPCData());

        scanner.get();

        element.content = mixed;

        for (;;)
        {
            Token token = scanner.get();

            if (token.type == Scanner.RPAREN)
            {
                token = scanner.peek();

                if (token.type == Scanner.ASTERISK)
                {
                    scanner.get();
                    mixed.cardinal = DTDCardinal.ZEROMANY;
                }
                else
                {
                    mixed.cardinal = DTDCardinal.NONE;
                }

                return;
            }
            else if (token.type == Scanner.PIPE)
            {
                token = scanner.get();

                mixed.add(new DTDName(token.value));
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.11"),
                                new Object[] { token.type.name }), scanner
                        .getLineNumber(), scanner.getColumn());
            }
        }
    }

    protected void parseChildren(DTDElement element) throws IOException
    {
        DTDContainer choiceSeq = parseChoiceSequence();

        Token token = scanner.peek();

        choiceSeq.cardinal = parseCardinality();

        if (token.type == Scanner.QUES)
        {
            choiceSeq.cardinal = DTDCardinal.OPTIONAL;
        }
        else if (token.type == Scanner.ASTERISK)
        {
            choiceSeq.cardinal = DTDCardinal.ZEROMANY;
        }
        else if (token.type == Scanner.PLUS)
        {
            choiceSeq.cardinal = DTDCardinal.ONEMANY;
        }
        else
        {
            choiceSeq.cardinal = DTDCardinal.NONE;
        }

        element.content = choiceSeq;
    }

    protected DTDContainer parseChoiceSequence() throws IOException
    {
        TokenType separator = null;

        DTDContainer cs = null;

        for (;;)
        {
            DTDItem item = parseCP();

            Token token = scanner.get();

            if ((token.type == Scanner.PIPE) || (token.type == Scanner.COMMA)
                    || (token.type == Scanner.AMPERSAND))
            {
                if ((separator != null) && (separator != token.type))
                {
                    throw new DTDParseException(scanner.getUriId(), Messages
                            .getString("DTDParser.12"),
                            scanner.getLineNumber(), scanner.getColumn());
                }
                separator = token.type;

                if (cs == null)
                {
                    if (token.type == Scanner.PIPE)
                    {
                        cs = new DTDChoice();
                    }
                    else
                    {
                        cs = new DTDSequence();
                    }
                }
                cs.add(item);
            }
            else if (token.type == Scanner.RPAREN)
            {
                if (cs == null)
                {
                    cs = new DTDSequence();
                }
                cs.add(item);

                return cs;
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.13"),
                                new Object[] { token.type.name }), scanner
                        .getLineNumber(), scanner.getColumn());
            }
        }
    }

    protected DTDItem parseCP() throws IOException
    {
        Token token = scanner.get();

        DTDItem item = null;

        if (token.type == Scanner.IDENTIFIER)
        {
            item = new DTDName(token.value);
        }
        else if (token.type == Scanner.LPAREN)
        {
            item = parseChoiceSequence();
        }
        else
        {
            throw new DTDParseException(scanner.getUriId(), MessageFormat
                    .format(Messages.getString("DTDParser.13"),
                            new Object[] { token.type.name }), scanner
                    .getLineNumber(), scanner.getColumn());
        }

        item.cardinal = parseCardinality();

        return item;
    }

    protected DTDCardinal parseCardinality() throws IOException
    {
        Token token = scanner.peek();

        if (token.type == Scanner.QUES)
        {
            scanner.get();
            return DTDCardinal.OPTIONAL;
        }
        else if (token.type == Scanner.ASTERISK)
        {
            scanner.get();
            return DTDCardinal.ZEROMANY;
        }
        else if (token.type == Scanner.PLUS)
        {
            scanner.get();
            return DTDCardinal.ONEMANY;
        }
        else
        {
            return DTDCardinal.NONE;
        }
    }

    protected void parseAttlist() throws IOException
    {
        Vector elements = parseElementGroup();
        Hashtable attributes = null;
        Vector attributeList = null;

        for (Enumeration e = elements.elements(); e.hasMoreElements();)
        {
            Token token = (Token) e.nextElement();

            DTDElement element = (DTDElement) dtd.elements.get(token.value);

            DTDAttlist attlist = new DTDAttlist(token.value);

            dtd.items.addElement(attlist);

            if (element == null)
            {
                element = new DTDElement(token.value);
                dtd.elements.put(token.value, element);
            }

            element.attributeList = attlist;

            if (attributes == null)
            {
                token = scanner.peek();

                while (token.type != Scanner.GT)
                {
                    parseAttdef(scanner, element, attlist);
                    token = scanner.peek();
                }
                attributes = element.attributes;
                attributeList = attlist.attributes;
            }
            else
            {
                element.attributes = attributes;
                attlist.attributes = attributeList;
            }
        }

        // MAW Version 1.17
        // Prior to this version, the parser didn't actually consume the > at
        // the
        // end of the ATTLIST definition. Because the parser ignored unexpected
        // tokens
        // at the top level, it was ignoring the >. In parsing DOCBOOK, however,
        // there
        // were two unexpected tokens, bringing this error to light.
        expect(Scanner.GT);
    }

    protected void parseAttdef(Scanner scanner, DTDElement element,
            DTDAttlist attlist) throws IOException
    {
        Token token = expect(Scanner.IDENTIFIER);

        DTDAttribute attr = new DTDAttribute(token.value);

        attlist.attributes.addElement(attr);

        element.attributes.put(token.value, attr);

        token = scanner.get();

        if (token.type == Scanner.IDENTIFIER)
        {
            if (isKeywordEqual(token.value, "NOTATION"))
            {
                attr.type = parseNotationList();
            }
            else
            {
                attr.type = token.value;
            }
        }
        else if (token.type == Scanner.LPAREN)
        {
            attr.type = parseEnumeration();
        }

        token = scanner.peek();

        if (token.type == Scanner.IDENTIFIER)
        {
            scanner.get();
            if (isKeywordEqual(token.value, "#FIXED"))
            {
                attr.decl = DTDDecl.FIXED;

                token = scanner.get();
                attr.defaultValue = token.value;
            }
            else if (isKeywordEqual(token.value, "#REQUIRED"))
            {
                attr.decl = DTDDecl.REQUIRED;
            }
            else if (isKeywordEqual(token.value, "#IMPLIED"))
            {
                attr.decl = DTDDecl.IMPLIED;
            }
            else
            {
                attr.decl = DTDDecl.VALUE;
                attr.defaultValue = token.value;
            }
        }
        else if (token.type == Scanner.STRING)
        {
            scanner.get();
            attr.decl = DTDDecl.VALUE;
            attr.defaultValue = token.value;
        }
        else if (token.type == Scanner.NMTOKEN)
        {
            scanner.get();
            attr.decl = DTDDecl.VALUE;
            attr.defaultValue = token.value;
        }
    }

    protected DTDNotationList parseNotationList() throws IOException
    {
        DTDNotationList notation = new DTDNotationList();

        Token token = scanner.get();
        if (token.type != Scanner.LPAREN)
        {
            throw new DTDParseException(scanner.getUriId(), MessageFormat
                    .format(Messages.getString("DTDParser.14"),
                            new Object[] { token.type.name }), scanner
                    .getLineNumber(), scanner.getColumn());
        }

        for (;;)
        {
            token = scanner.get();

            if (token.type != Scanner.IDENTIFIER)
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.14"),
                                new Object[] { token.type.name }), scanner
                        .getLineNumber(), scanner.getColumn());
            }

            notation.add(token.value);

            token = scanner.peek();

            if (token.type == Scanner.RPAREN)
            {
                scanner.get();
                return notation;
            }
            else if (token.type != Scanner.PIPE)
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.14"),
                                new Object[] { token.type.name }), scanner
                        .getLineNumber(), scanner.getColumn());
            }
            scanner.get(); // eat the pipe
        }
    }

    protected DTDEnumeration parseEnumeration() throws IOException
    {
        DTDEnumeration enumeration = new DTDEnumeration();

        for (;;)
        {
            Token token = scanner.get();

            if ((token.type != Scanner.IDENTIFIER)
                    && (token.type != Scanner.NMTOKEN))
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.15"),
                                new Object[] { token.type.name }), scanner
                        .getLineNumber(), scanner.getColumn());
            }

            enumeration.add(token.value);

            token = scanner.peek();

            if (token.type == Scanner.RPAREN)
            {
                scanner.get();
                return enumeration;
            }
            else if (token.type != Scanner.COMMA && token.type != Scanner.PIPE)
            {
                throw new DTDParseException(scanner.getUriId(), MessageFormat
                        .format(Messages.getString("DTDParser.15"),
                                new Object[] { token.type.name }), scanner
                        .getLineNumber(), scanner.getColumn());
            }
            scanner.get(); // eat the comma
        }
    }

    protected void parseEntity() throws IOException
    {
        boolean isParsed = false;

        Token name = scanner.get();

        if (name.type == Scanner.PERCENT)
        {
            isParsed = true;
            name = expect(Scanner.IDENTIFIER);
        }
        else if (name.type != Scanner.IDENTIFIER)
        {
            throw new DTDParseException(scanner.getUriId(), Messages
                    .getString("DTDParser.16"), scanner.getLineNumber(),
                    scanner.getColumn());
        }

        DTDEntity entity = (DTDEntity) dtd.entities.get(name.value);

        boolean skip = false;

        if (entity == null)
        {
            entity = new DTDEntity(name.value, defaultLocation);
            dtd.entities.put(entity.name, entity);
        }
        else
        {
            // 070501 MAW: If the entity already exists, create a dummy entity -
            // this way
            // you keep the original definition. Thanks to Jags Krishnamurthy of
            // Object
            // Edge for pointing out this problem and for pointing out the
            // solution
            entity = new DTDEntity(name.value, defaultLocation);
            skip = true;
        }

        dtd.items.addElement(entity);

        entity.isParsed = isParsed;

        parseEntityDef(entity);

        if (entity.isParsed && (entity.value != null) && !skip)
        {
            scanner.addEntity(entity.name, entity.value);
        }
    }

    protected void parseEntityDef(DTDEntity entity) throws IOException
    {
        Token token = scanner.get();

        if (token.type == Scanner.STRING)
        {
            // Only set the entity value if it hasn't been set yet
            // XML 1.0 spec says that you use the first value of
            // an entity, not the most recent.
            if (entity.value == null)
            {
                entity.value = token.value;
            }
        }
        else if (token.type == Scanner.IDENTIFIER)
        {
            if (isKeywordEqual(token.value, "SYSTEM"))
            {
                DTDSystem sys = new DTDSystem();
                token = expect(Scanner.STRING);

                sys.system = token.value;
                entity.externalID = sys;
                entity.type = DTDEntity.SYSTEM;
            }
            else if (isKeywordEqual(token.value, "PUBLIC"))
            {
                DTDPublic pub = new DTDPublic();

                token = expect(Scanner.STRING);
                pub.pub = token.value;
                token = scanner.peek();
                if (token.type == Scanner.STRING)
                {
                    token = scanner.get();
                    pub.system = token.value;
                }
                else if (catalog != null)
                {
                    // If there is a catalog present, try to use the
                    // catalog to resolve a public entity
                    pub.system = catalog.resolvePublic(pub.pub, null);
                }
                entity.externalID = pub;
                entity.type = DTDEntity.PUBLIC;
            }
            else if (isKeywordEqual(token.value, "CDATA"))
            {
                token = expect(Scanner.STRING);

                entity.value = token.value;
                entity.cdata = token.value;
                entity.type = DTDEntity.CDATA;
            }
            else if (isKeywordEqual(token.value, "SDATA"))
            {
                token = expect(Scanner.STRING);

                entity.value = token.value;
                entity.sdata = token.value;
                entity.type = DTDEntity.SDATA;
            }
            else if (isKeywordEqual(token.value, "NDATA"))
            {
                token = expect(Scanner.STRING);

                entity.value = token.value;
                entity.ndata = token.value;
                entity.type = DTDEntity.NDATA;
            }
            else
            {
                throw new DTDParseException(scanner.getUriId(), Messages
                        .getString("DTDParser.17"), scanner.getLineNumber(),
                        scanner.getColumn());
            }

            if (!entity.isParsed)
            {
                token = scanner.peek();
                if (token.type == Scanner.IDENTIFIER)
                {
                    if (!isKeywordEqual(token.value, "NDATA")
                            && !isKeywordEqual(token.value, "SDATA")
                            && !isKeywordEqual(token.value, "CDATA"))
                    {
                        throw new DTDParseException(scanner.getUriId(),
                                Messages.getString("DTDParser.18"), scanner
                                        .getLineNumber(), scanner.getColumn());
                    }

                    String dataType = token.value;

                    // This gets "(C/S/N)DATA" IDENTIFIER.
                    token = scanner.get();
                    // Get the DATA "Name" IDENTIFIER.
                    token = expect(Scanner.IDENTIFIER);

                    entity.value = token.value;

                    if (isKeywordEqual(dataType, "NDATA"))
                    {
                        // Save the ndata value
                        entity.ndata = token.value;
                        entity.type = DTDEntity.NDATA;
                    }
                    else if (isKeywordEqual(dataType, "SDATA"))
                    {
                        // Save the sdata value
                        entity.sdata = token.value;
                        entity.type = DTDEntity.SDATA;
                    }
                    else if (isKeywordEqual(dataType, "CDATA"))
                    {
                        // Save the cdata value
                        entity.cdata = token.value;
                        entity.type = DTDEntity.CDATA;
                    }
                }
            }
        }
        else
        {
            throw new DTDParseException(scanner.getUriId(), Messages
                    .getString("DTDParser.19"), scanner.getLineNumber(),
                    scanner.getColumn());
        }

        expect(Scanner.GT);
    }

    protected void parseNotation() throws java.io.IOException
    {
        DTDNotation notation = new DTDNotation();

        Token token = expect(Scanner.IDENTIFIER);

        notation.name = token.value;

        dtd.notations.put(notation.name, notation);
        dtd.items.addElement(notation);

        token = expect(Scanner.IDENTIFIER);

        if (isKeywordEqual(token.value, "SYSTEM"))
        {
            DTDSystem sys = new DTDSystem();

            // For <!NOTATION>, you can have SYSTEM without a SystemLiteral
            token = scanner.peek();
            if (token.type == Scanner.STRING)
            {
                token = scanner.get();
                sys.system = token.value;
            }

            notation.externalID = sys;
        }
        else if (token.value.equals("PUBLIC"))
        {
            DTDPublic pub = new DTDPublic();
            token = expect(Scanner.STRING);

            pub.pub = token.value;
            pub.system = null;

            // For <!NOTATION>, you can have PUBLIC PubidLiteral without
            // a SystemLiteral
            token = scanner.peek();
            if (token.type == Scanner.STRING)
            {
                token = scanner.get();
                pub.system = token.value;
            }

            notation.externalID = pub;
        }
        expect(Scanner.GT);
    }

    public DTDEntity expandEntity(String name)
    {
        DTDEntity entity = (DTDEntity) dtd.entities.get(name);
        if (entity != null)
            return entity;

        if (catalog != null)
        {
            try
            {
                String entityValue = catalog.resolveEntity(name, null, null);
                if (entityValue != null)
                {
                    entity = new DTDEntity(name);
                    entity.value = entityValue;
                    return entity;
                }
            }
            catch (IOException exc)
            {
                return null;
            }
        }
        return null;
    }

    public Catalog getCatalog()
    {
        return catalog;
    }

    public void setCatalog(Catalog catalog)
    {
        this.catalog = catalog;
    }

    protected boolean isKeywordEqual(String k1, String k2)
    {
        if (k1 == null)
            return (k2 == null);
        if (k2 == null)
            return false;
        if (caseless)
            return k1.equalsIgnoreCase(k2);
        return k1.equals(k2);
    }
}
