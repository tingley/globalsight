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
// This software is in the public static domain.
//
// The software is provided "as is", without warranty of any kind,
// express or implied, including but not limited to the warranties
// of merchantability, fitness for a particular purpose, and
// noninfringement. In no event shall the author(s) be liable for any
// claim, damages, or other liability, whether in an action of
// contract, tort, or otherwise, arising from, out of, or in connection
// with the software or the use or other dealings in the software.
//
// Parts of this software were originally developed in the Database
// and Distributed Systems Group at the Technical University of
// Darmstadt, Germany:
//
//    http://www.informatik.tu-darmstadt.de/DVS1/
// Version 2.0
// Changes from version 1.0:
// Changes from version 1.01:
package com.globalsight.ling.sgml.dtd2;

import com.globalsight.ling.sgml.GlobalSightDtdParser;
import com.globalsight.ling.sgml.GlobalSightDtd;

import com.globalsight.ling.sgml.catalog.Catalog;

import org.xml.sax.InputSource;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Parses an external DTD or the DTD in an XML document and creates a
 * DTD object.
 *
 * <p>While DTDParser checks for most syntactic errors in the DTD, it
 * does not check for all of them. (For example, it does not check if
 * entities are well-formed.)  Thus, results are undetermined if the
 * DTD is not syntactically correct.</p>
 *
 * @author Ronald Bourret
 * @version 2.0
 */

public class DTDParser
    implements GlobalSightDtdParser
{
    // This class converts an external DTD or the DTD in an XML document
    // into a DTD object. It's not the most brilliant parser in the world,
    // nor is it the fastest, nor does it cover all the cases, but it should
    // be useable for many DTDs.
    //
    // This code generally assumes the DTD to be syntactically correct;
    // results are undetermined if it is not.

    // ********************************************************************
    // Constants
    // ********************************************************************

    // READER_READER simply means we don't care what type of Reader
    // it is -- it is all set up and ready to go.

    static final int READER_READER = 0,
        READER_STRING = 1,
        READER_URL = 2;
    static final int STATE_OUTSIDEDTD = 0,
        STATE_DTD = 1,
        STATE_ATTVALUE = 2,
        STATE_ENTITYVALUE = 3,
        STATE_COMMENT = 4,
        STATE_IGNORE = 5;
    static final int BUFSIZE = 8096,
        LITBUFSIZE = 1024,
        NAMEBUFSIZE = 1024;

    // ********************************************************************
    // Variables
    // ********************************************************************

    DTD          dtd;
    Hashtable    namespaceURIs,
        predefinedEntities = new Hashtable(),
        declaredElementTypes = new Hashtable();
    TokenList    dtdTokens;
    Reader       reader;
    int          readerType, bufferPos, bufferLen, literalPos, namePos,
        entityState, line, column;
    Stack        readerStack;
    StringBuffer literalStr, nameStr;
    char[]       buffer,
        literalBuffer = new char[LITBUFSIZE],
        nameBuffer = new char[NAMEBUFSIZE];
    boolean      ignoreQuote, ignoreMarkup;
    URL          readerURL;

    // CvdL: hook up the SGML catalog.
    Catalog m_catalog = null;

    // ********************************************************************
    // Constructors
    // ********************************************************************

    /** Create a new DTDParser. */
    public DTDParser()
    {
        dtdTokens = new TokenList(DTDConst.KEYWDS, DTDConst.KEYWD_TOKENS,
            DTDConst.KEYWD_TOKEN_UNKNOWN);
        initPredefinedEntities();
    }

    public GlobalSightDtd parseDtd(URL p_url)
        throws Exception
    {
        InputSource src = new InputSource(p_url.openStream());
        src.setSystemId(p_url.toString());
        return parseExternalSubset(src, new Hashtable());
    }

    public GlobalSightDtd parseDtd(URL p_url, boolean p_trace)
        throws Exception
    {
        InputSource src = new InputSource(p_url.openStream());
        src.setSystemId(p_url.toString());
        return parseExternalSubset(src, new Hashtable());
    }

    public void setCatalog(Catalog p_catalog)
    {
        m_catalog = p_catalog;
    }

    // ********************************************************************
    // Methods
    // ********************************************************************

    /**
     * Parse the DTD in an XML document containing an internal subset,
     * reference to an external subset, or both.
     *
     * @param src A SAX InputSource for the XML document.
     * @param namespaceURIs A Hashtable of keyed by prefixes used in
     * the DTD, mapping these to namespace URIs. May be null.
     * @return The DTD object.
     * @exception DTDException Thrown if a DTD error is found.
     * @exception EOFException Thrown if EOF is reached prematurely.
     * @exception MalformedURLException Thrown if a system ID is malformed.
     * @exception IOException Thrown if an I/O error occurs.
     */
    public DTD parseXMLDocument(InputSource src, Hashtable namespaceURIs)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        initGlobals();
        this.namespaceURIs = namespaceURIs;
        openInputSource(src);
        parseDocument();
        postProcessDTD();
        return dtd;
    }

    /**
     * Parse the DTD in an external subset.
     *
     * @param src A SAX InputSource for DTD (external subset).
     * @param namespaceURIs A Hashtable of keyed by prefixes used in
     * the DTD, mapping these to namespace URIs. May be null.
     * @return The DTD object.
     * @exception DTDException Thrown if a DTD error is found.
     * @exception EOFException Thrown if EOF is reached prematurely.
     * @exception MalformedURLException Thrown if a system ID is malformed.
     * @exception IOException Thrown if an I/O error occurs.
     */
    public DTD parseExternalSubset(InputSource src, Hashtable namespaceURIs)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        initGlobals();
        this.namespaceURIs = namespaceURIs;
        openInputSource(src);
        parseExternalSubset(true);
        postProcessDTD();

        return dtd;
    }

    // ********************************************************************
    // Methods -- general parsing (!!! IN ALPHABETICAL ORDER !!!)
    // ********************************************************************

    void parseAttlistDecl()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // <!ATTLIST already parsed

        ElementType elementType;

        requireWhitespace();
        elementType = createElementType();

        requireWhitespace();

        while (!isChar('>'))
        {
            getAttDef(elementType);

            // CvdL: allow internal comments in entity decls
            discardWhitespace();
            while (isString("--"))
            {
                discardUntil("--");
                discardWhitespace();
            }

            if (isChar('>')) break;
        }
    }

    void parseComment()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // '<!--' already parsed.
        int saveEntityState;

        saveEntityState = entityState;
        entityState = STATE_COMMENT;
        discardUntil("--");
        requireChar('>');
        entityState = saveEntityState;
    }

    void parseConditional()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // CvdL: '<![' already parsed (by parseMarkupDecl())
        int saveEntityState;

        discardWhitespace();
        if (isString("INCLUDE"))
        {
            parseInclude();
        }
        else if (isString("IGNORE"))
        {
            entityState = STATE_IGNORE;
            parseIgnoreSect();
            entityState = STATE_DTD;
        }
        else
        {
            throwDTDException("Invalid conditional section.");
        }
    }

    void parseDocTypeDecl()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        String root, publicID = null, systemID = null;

        if (!isString("<!DOCTYPE")) return;

        // Get the root element type.

        requireWhitespace();
        root = getName();
        if (root == null)
        {
            throwDTDException("Invalid root element type name in DOCTYPE.");
        }

        // Get the system ID of the external subset, if any.

        if (isWhitespace())
        {
            discardWhitespace();
            if (isString("SYSTEM"))
            {
                systemID = parseSystemLiteral();
                discardWhitespace();
            }
            else if (isString("PUBLIC"))
            {
                publicID = parsePublicID();
                systemID = parseSystemLiteral();
                discardWhitespace();
            }
        }

        // Get the internal subset, if any.

        if (isChar('['))
        {
            parseInternalSubset();
            requireChar(']');
        }

        // Get the external subset, if any.

        // CvdL: use the catalog to resolve names, preferring public IDs.
        String url = resolveExternalEntity(publicID, systemID);

        pushCurrentReader();
        createURLReader(new URL(readerURL, url));
        parseExternalSubset(false);

        // Finish the document type declaration.

        discardWhitespace();
        requireChar('>');
    }

    void parseDocument()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        if (isString("<?xml"))
        {
            parseXMLDecl();
        }
        parseMisc();
        parseDocTypeDecl();

        // Here is where the rest of the document would be parsed. However,
        // we only care about the DTD, so we simply stop.
    }

    void parseElementDecl()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        ElementType elementType;

        // <!ELEMENT already parsed

        requireWhitespace();
        elementType = addElementType();
        requireWhitespace();
        getContentModel(elementType);
        discardWhitespace();

        // CvdL: allow internal comments in element decls
        while (isString("--"))
        {
            discardUntil("--");
            discardWhitespace();
        }

        requireChar('>');
    }

    void parseEncodingDecl()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // S 'encoding' already parsed.

        // BUG! We really need to do something with this -- drive transformations
        // or reject the document as unreadable...

        parseEquals();
        getEncName();
    }

    void parseEntityDecl()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // <!ENTITY already parsed.

        Entity  entity;
        boolean isPE = false;
        String  name, notation, value = null, systemID = null, publicID = null;

        requireWhitespace();
        if (isChar('%'))
        {
            isPE = true;
            requireWhitespace();
        }

        name = getName();
        requireWhitespace();

        if (isString("PUBLIC"))
        {
            publicID = parsePublicID();
            systemID = parseSystemLiteral();
        }
        else if (isString("SYSTEM"))
        {
            systemID = parseSystemLiteral();
        }
        // CvdL: recognize CDATA
        else if (isString("CDATA"))
        {
            requireWhitespace();
            value = getEntityValue();
        }
        // CvdL: recognize SDATA
        else if (isString("SDATA"))
        {
            requireWhitespace();
            value = getEntityValue();
        }
        else
        {
            value = getEntityValue();
        }

        if (isPE)
        {
            // Parameter entity

            entity = new ParameterEntity(name);
            entity.systemID = systemID;
            entity.publicID = publicID;
            ((ParameterEntity)entity).value = value;

            if (!dtd.parameterEntities.containsKey(name))
            {
                // If a parameter entity isn't already defined, use the
                // current definition.

                dtd.parameterEntities.put(name, entity);
            }
        }
        else if (isString("NDATA"))
        {
            // Unparsed entity

            requireWhitespace();
            notation = getName();

            entity = new UnparsedEntity(name);
            entity.systemID = systemID;
            entity.publicID = publicID;
            ((UnparsedEntity)entity).notation = notation;

            if (!dtd.unparsedEntities.containsKey(name) &&
                !dtd.parsedGeneralEntities.containsKey(name))
            {
                // If an unparsed entity isn't already defined, use the
                // current definition. Remember that unparsed entities
                // and parsed general entities share the same namespace.

                dtd.unparsedEntities.put(name, entity);
            }
        }
        else
        {
            // Parsed general entity

            entity = new ParsedGeneralEntity(name);
            entity.systemID = systemID;
            entity.publicID = publicID;
            ((ParsedGeneralEntity)entity).value = value;

            if (!dtd.unparsedEntities.containsKey(name) &&
                !dtd.parsedGeneralEntities.containsKey(name))
            {
                // If parsed general entity isn't already defined, use the
                // current definition. Remember that unparsed entities
                // and parsed general entities share the same namespace.

                dtd.parsedGeneralEntities.put(name, entity);
            }
        }

        discardWhitespace();

        // CvdL: allow internal comments in entity decls
        while (isString("--"))
        {
            discardUntil("--");
            discardWhitespace();
        }

        requireChar('>');
    }

    void parseEquals()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        discardWhitespace();
        requireChar('=');
        discardWhitespace();
    }

    void parseExternalSubset(boolean eofOK)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        entityState = STATE_DTD;

        if (isString("<?xml"))
        {
            parseTextDecl();
        }

        parseExternalSubsetDecl(eofOK);

        entityState = STATE_OUTSIDEDTD;
    }

    void parseExternalSubsetDecl(boolean eofOK)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        boolean declFound = true;

        // Get the markup declarations.
        //
        // Note that we check here for an EOF. That is because this is the only
        // place it can legally occur, and then only when processing an external
        // subset only, as opposed to processing an external subset referenced
        // in a DOCTYPE statement.

        while (declFound)
        {
            try
            {
                discardWhitespace();
            }
            catch (EOFException eof)
            {
                if (eofOK) return;
                throw eof;
            }

            declFound = parseMarkupDecl();
        }
    }

    boolean parseIgnore()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // There are three possible outcomes to this function:
        // * We find a new subsection ("<![")
        // * We find an end ("]]>")
        // * We run out of characters
        //
        // The states are as follows:
        // 0 - Just toodlin' along...
        // 1 - < found
        // 2 - <! found
        // 3 - ] found
        // 4 - ]] found

        char c;
        int  state = 0;

        while (true)
        {
            c = nextChar();

            switch (state)
            {
            case 0: // Toodlin' along
                if (c == '<')
                    state = 1;
                else if (c == ']')
                    state = 3;
                break;

            case 1: // < found
                state = (c == '!') ? 2 : 0;
                break;

            case 2: // <! found
                if (c == '[') return false;
                state = 0;
                break;

            case 3: // ] found
                state = (c == ']') ? 4 : 0;
                break;

            case 4: // ]] found
                if (c == '>') return true;
                state = 0;
                break;
            }
        }
    }

    void parseIgnoreSect()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        discardWhitespace();
        requireChar('[');
        parseIgnoreSectContents();
    }

    void parseIgnoreSectContents()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // ignoreSectContents is an annoying little production. The
        // problem is that it can occur sequentially as well as
        // nested within itself. We solve the problem by keeping
        // track of the number of open <![...]]> sections. When we
        // close the last section, we're done. Note that we enter
        // this function already inside a section ([...]]>).

        int open = 1;

        while (open > 0)
        {
            open = (parseIgnore()) ? open - 1 : open + 1;
        }
    }

    void parseInclude()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        discardWhitespace();
        requireChar('[');
        parseExternalSubsetDecl(false);
        requireString("]]>");
    }

    void parseInternalSubset()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        boolean declFound = true;

        entityState = STATE_DTD;

        // Get the markup declarations

        while (declFound)
        {
            discardWhitespace();
            declFound = parseMarkupDecl();
        }

        entityState = STATE_OUTSIDEDTD;
    }

    boolean parseMarkupDecl()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        String name;

        // This function returns true if it finds a markup declaration; false
        // if it doesn't.

        if (!isChar('<')) return false;
        if (isString("!--"))
        {
            parseComment();
        }
        // CvdL: handle conditional declarations <![ IGNORE [ ... ]]>
        else if (isString("!["))
        {
            parseConditional();
        }
        else if (isChar('!'))
        {
            name = getName();

            switch (dtdTokens.getToken(name))
            {
            case DTDConst.KEYWD_TOKEN_ELEMENT:
                parseElementDecl();
                break;

            case DTDConst.KEYWD_TOKEN_ATTLIST:
                parseAttlistDecl();
                break;

            case DTDConst.KEYWD_TOKEN_ENTITY:
                parseEntityDecl();
                break;

            case DTDConst.KEYWD_TOKEN_NOTATION:
                parseNotationDecl();
                break;

            default:
                throwDTDException("Invalid markup declaration: <!" + name);
            }
        }
        else if (isChar('?'))
        {
            parsePI();
        }
        else
        {
            return false;
        }

        return true;
    }

    void parseMisc()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        boolean miscFound = true;

        while (miscFound)
        {
            discardWhitespace();
            if (isString("<!--"))
            {
                parseComment();
            }
            else if (isString("<?"))
            {
                parsePI();
            }
            else
            {
                miscFound = false;
            }
        }
    }

    void parseNotationDecl()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // <!NOTATION already parsed.

        Notation notation;
        String      keywd;

        // Create a new Notation.

        notation = new Notation();

        requireWhitespace();
        notation.name = getName();
        requireWhitespace();

        keywd = getName();

        switch (dtdTokens.getToken(keywd))
        {
        case DTDConst.KEYWD_TOKEN_SYSTEM:
            notation.systemID = parseSystemLiteral();
            discardWhitespace();
            requireChar('>');
            break;

        case DTDConst.KEYWD_TOKEN_PUBLIC:
            notation.publicID = parsePublicID();
            if (!isChar('>'))
            {
                requireWhitespace();
                if (!isChar('>'))
                {
                    notation.systemID = getSystemLiteral();
                    discardWhitespace();
                    requireChar('>');
                }
            }
            break;

        default:
            throwDTDException("Invalid keyword in notation declaration: " + keywd);
        }

        // Add the Notation to the DTD.

        if (dtd.notations.containsKey(notation.name))
            throwDTDException("Duplicate notation declaration: " + notation.name);
        dtd.notations.put(notation.name, notation);
    }

    void parsePI()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // '<?' already parsed.
        discardUntil("?>");
    }

    String parsePublicID()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // PUBLIC already parsed.

        requireWhitespace();
        return getPubidLiteral();
    }

    void parseStandalone()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // S 'standalone' already parsed.

        String yesno;

        parseEquals();
        getYesNo();
    }

    String parseSystemLiteral()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // SYSTEM already parsed.

        requireWhitespace();
        return getSystemLiteral();
    }

    void parseTextDecl()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // '<?xml' already parsed.

        requireWhitespace();

        // Parse the version, if any.

        if (isString("version"))
        {
            parseVersion();
            requireWhitespace();
        }

        // Parse the encoding declaration.

        requireString("encoding");
        parseEncodingDecl();

        // Finish up.

        discardWhitespace();
        requireString("?>");
    }

    void parseVersion()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // S 'version' already parsed.

        char quote;

        parseEquals();
        quote = getQuote();
        requireString("1.0");
        requireChar(quote);
    }

    void parseXMLDecl()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Check if we've got an XML declaration or a PI that starts
        // with XML. '<?xml' already parsed.

        if (!isWhitespace())
        {
            parsePI();
            return;
        }

        // Parse the version number.

        discardWhitespace();
        requireString("version");
        parseVersion();

        // Check for encoding and standalone declarations.

        if (isWhitespace())
        {
            discardWhitespace();

            // Parse the encoding declaration (if any), then return to the
            // same post-required-whitespace position.

            if (isString("encoding"))
            {
                parseEncodingDecl();
                if (!isWhitespace()) return;
                discardWhitespace();
            }

            // Parse the standalone declaration (if any), then return to the
            // same post-whitespace position.

            if (isString("standalone"))
            {
                parseStandalone();
                discardWhitespace();
            }
        }

        // Close the XML declaration.

        requireString("?>");
    }

    // ********************************************************************
    // Methods -- specialized parsing and DTD object building
    // ********************************************************************

    ElementType addElementType()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        XMLName name;

        // Get the element type name and add it to the DTD.  We store
        // the name in a Hashtable so we can later check if the name
        // has already been declared. Note that we only care about the
        // hashtable key, not the hashtable element.

        name = getXMLName();

        if (declaredElementTypes.containsKey(name))
        {
            throwDTDException("Duplicate element type declaration: " +
                name.getUniversalName());
        }

        declaredElementTypes.put(name, name);

        return dtd.createElementType(name);
    }

    void getAttDef(ElementType elementType)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // S already parsed.

        Attribute attribute;

        attribute = getAttribute(elementType);
        requireWhitespace();
        getAttributeType(attribute);
        requireWhitespace();
        getAttributeRequired(attribute);
    }

    Attribute getAttribute(ElementType elementType)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        XMLName   name;
        Attribute attribute;

        // Get the attribute name and create a new Attribute.

        name = getXMLName();
        attribute = new Attribute(name);

        // If the element does not have an attribute with this name, add
        // it to the ElementType. Otherwise, ignore it.

        if (!elementType.attributes.containsKey(name))
        {
            elementType.attributes.put(name, attribute);
        }

        // Return the new Attribute. Note that we do this even if it is
        // not added to the ElementType so that other code is simpler.

        return attribute;
    }

    void getAttributeDefault(Attribute attribute)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        attribute.defaultValue = getAttValue();
    }

    void getAttributeRequired(Attribute attribute)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        String name;

        if (isChar('#'))
        {
            name = getName();

            switch (dtdTokens.getToken(name))
            {
            case DTDConst.KEYWD_TOKEN_REQUIRED:
                attribute.required = Attribute.REQUIRED_REQUIRED;
                break;

            case DTDConst.KEYWD_TOKEN_IMPLIED:
                attribute.required = Attribute.REQUIRED_OPTIONAL;
                break;

            case DTDConst.KEYWD_TOKEN_FIXED:
                attribute.required = Attribute.REQUIRED_FIXED;
                requireWhitespace();
                getAttributeDefault(attribute);
                break;

            default:
                throwDTDException("Invalid attribute default: " + name);
            }
        }
        else
        {
            attribute.required = Attribute.REQUIRED_DEFAULT;
            getAttributeDefault(attribute);
        }
    }

    void getAttributeType(Attribute attribute)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        String name;

        if (isChar('('))
        {
            attribute.type = Attribute.TYPE_ENUMERATED;
            getEnumeration(attribute, false);
            return;
        }

        name = getName();

        switch (dtdTokens.getToken(name))
        {
        case DTDConst.KEYWD_TOKEN_CDATA:
            attribute.type = Attribute.TYPE_CDATA;
            break;

        case DTDConst.KEYWD_TOKEN_ID:
            attribute.type = Attribute.TYPE_ID;
            break;

        case DTDConst.KEYWD_TOKEN_IDREF:
            attribute.type = Attribute.TYPE_IDREF;
            break;

        case DTDConst.KEYWD_TOKEN_IDREFS:
            attribute.type = Attribute.TYPE_IDREFS;
            break;

        case DTDConst.KEYWD_TOKEN_ENTITY:
            attribute.type = Attribute.TYPE_ENTITY;
            break;

        case DTDConst.KEYWD_TOKEN_ENTITIES:
            attribute.type = Attribute.TYPE_ENTITIES;
            break;

        case DTDConst.KEYWD_TOKEN_NMTOKEN:
            attribute.type = Attribute.TYPE_NMTOKEN;
            break;

        case DTDConst.KEYWD_TOKEN_NMTOKENS:
            attribute.type = Attribute.TYPE_NMTOKENS;
            break;

        case DTDConst.KEYWD_TOKEN_NOTATION:
            attribute.type = Attribute.TYPE_NOTATION;
            requireWhitespace();
            requireChar('(');
            getEnumeration(attribute, true);
            break;

        default:
            throwDTDException("Invalid attribute type: " + name);
        }
    }

    void getContentModel(ElementType elementType)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Get the content model.

        if (isChar('('))
        {
            // 5/18/00, Ronald Bourret
            // Added following call to discardWhitespace(). This is needed
            // for the case where space precedes the '#':
            //    <!ELEMENT A ( #PCDATA | B )*>

            discardWhitespace();
            if (isChar('#'))
            {
                getMixedContent(elementType);
            }
            else
            {
                getElementContent(elementType);
            }
        }
        else if (isString("EMPTY"))
        {
            elementType.contentType = ElementType.CONTENT_EMPTY;
        }
        else if (isString("ANY"))
        {
            elementType.contentType = ElementType.CONTENT_ANY;
        }
        else
            throwDTDException("Invalid element type declaration.");
    }

    void getContentParticle(Group group, ElementType parent)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        Group     childGroup;
        Reference ref;

        if (isChar('('))
        {
            childGroup = new Group();
            group.members.addElement(childGroup);
            getGroup(childGroup, parent);
        }
        else
        {
            ref = getReference(group, parent, false);
            getFrequency(ref);
        }
    }

    void getElementContent(ElementType elementType)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        elementType.content = new Group();
        elementType.contentType = ElementType.CONTENT_ELEMENT;
        getGroup(elementType.content, elementType);
    }

    ElementType createElementType()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        XMLName name;

        // Get the element type name and get the ElementType from the DTD.

        name = getXMLName();
        return dtd.createElementType(name);
    }

    void getEnumeratedValue(Attribute attribute, boolean useNames, Hashtable enums)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        String name;

        discardWhitespace();
        name = useNames ? getName() : getNmtoken();
        if (enums.containsKey(name))
            throwDTDException("Enumerated values must be unique: " + name);
        attribute.enums.addElement(name);
        discardWhitespace();
    }

    void getEnumeration(Attribute attribute, boolean useNames)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        String    name;
        Hashtable enums = new Hashtable();

        attribute.enums = new Vector();

        // Get the first enumerated value.
        getEnumeratedValue(attribute, useNames, enums);

        // Get the remaining values, if any.
        while (!isChar(')'))
        {
            requireChar('|');
            getEnumeratedValue(attribute, useNames, enums);
        }
    }

    void getFrequency(Particle particle)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        if (isChar('?'))
        {
            particle.isRequired = false;
            particle.isRepeatable = false;
        }
        else if (isChar('+'))
        {
            particle.isRequired = true;
            particle.isRepeatable = true;
        }
        else if (isChar('*'))
        {
            particle.isRequired = false;
            particle.isRepeatable = true;
        }
        else
        {
            particle.isRequired = true;
            particle.isRepeatable = false;
        }
    }

    void getGroup(Group group, ElementType parent)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // This gets a choice or sequence.

        boolean moreCPs = true;

        while (moreCPs)
        {
            discardWhitespace();
            getContentParticle(group, parent);
            discardWhitespace();
            if (isChar('|'))
            {
                if (group.type == Particle.TYPE_UNKNOWN)
                {
                    group.type = Particle.TYPE_CHOICE;
                }
                else if (group.type == Particle.TYPE_SEQUENCE)
                {
                    throwDTDException("Invalid mixture of ',' and '|' in content model.");
                }
            }
            else if (isChar(','))
            {
                if (group.type == Particle.TYPE_UNKNOWN)
                {
                    group.type = Particle.TYPE_SEQUENCE;
                }
                else if (group.type == Particle.TYPE_CHOICE)
                {
                    throwDTDException("Invalid mixture of ',' and '|' in content model.");
                }
            }
            else if (isChar(')'))
            {
                moreCPs = false;
                getFrequency(group);

                // If there is a single content particle in the group,
                // we simply call it a sequence.

                if (group.type == Particle.TYPE_UNKNOWN)
                {
                    group.type = Particle.TYPE_SEQUENCE;
                }
            }
        }
    }

    void getMixedContent(ElementType parent)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        boolean moreNames = true;

        discardWhitespace();
        requireString("PCDATA");
        discardWhitespace();
        if (isChar('|'))
        {
            // Content model is mixed: (#PCDATA | A | B)*

            parent.contentType = ElementType.CONTENT_MIXED;

            // Add a choice Group for the content model.

            parent.content = new Group();
            parent.content.type = Particle.TYPE_CHOICE;
            parent.content.isRequired = false;
            parent.content.isRepeatable = true;

            // Process the element type names. There must be at least one,
            // or we would have fallen into the else clause below.

            while (moreNames)
            {
                discardWhitespace();
                getReference(parent.content, parent, true);
                discardWhitespace();
                moreNames = isChar('|');
            }

            // Close the content model.

            requireString(")*");
        }
        else
        {
            // Content model is PCDATA-only: (#PCDATA)

            parent.contentType = ElementType.CONTENT_PCDATA;
            requireChar(')');

            // 5/17/00, Ronald Bourret
            // Check if there is an asterisk after the closing parenthesis.
            // This covers the following case:
            //    <!ELEMENT A (#PCDATA)*>

            isChar('*');
        }
    }

    XMLName getXMLName()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Get the element type name and construct an XMLName from it.

        String qualifiedName;

        qualifiedName = getName();
        return XMLName.create(qualifiedName, namespaceURIs);
    }

    Reference getReference(Group group, ElementType parent, boolean mixed)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        XMLName     name;
        ElementType child;
        Reference   ref;

        // Create an ElementType for the referenced child.

        child = createElementType();

        // Add the child to the parent and vice versa. If we are processing
        // mixed content, then each child must be unique in the parent.

        if (mixed)
        {
            if (parent.children.containsKey(child.name))
            {
                throwDTDException("The element type " +
                    child.name.getUniversalName() +
                    " appeared more than once in the declaration of mixed content for the element type " +
                    child.name.getUniversalName() + ".");
            }
        }

        parent.children.put(child.name, child);
        child.parents.put(parent.name, parent);

        // Create a Reference for the child, add it to the group, and return it.

        ref = new Reference(child);
        group.members.addElement(ref);
        return ref;
    }

    // ********************************************************************
    // Methods -- utility
    // ********************************************************************

    void initGlobals()
        throws MalformedURLException
    {
        dtd = new DTD();
        entityState = STATE_OUTSIDEDTD;
        readerStack = new Stack();
        initReaderGlobals();
        declaredElementTypes.clear();
    }

    void initPredefinedEntities()
    {
        ParsedGeneralEntity entity;

        entity = new ParsedGeneralEntity("lt");
        entity.value = "<";
        predefinedEntities.put(entity.name, entity);

        entity = new ParsedGeneralEntity("gt");
        entity.value = ">";
        predefinedEntities.put(entity.name, entity);

        entity = new ParsedGeneralEntity("amp");
        entity.value = "&";
        predefinedEntities.put(entity.name, entity);

        entity = new ParsedGeneralEntity("apos");
        entity.value = "'";
        predefinedEntities.put(entity.name, entity);

        entity = new ParsedGeneralEntity("quot");
        entity.value = "\"";
        predefinedEntities.put(entity.name, entity);
    }

    void postProcessDTD() throws DTDException
    {
        if (dtd != null)
        {
            updateANYParents();
            checkElementTypeReferences();
            checkNotationReferences();
        }
    }

    private void updateANYParents()
    {
        // A common problem when building a DTD object is that element types
        // with a content model of ANY do not correctly list parents and children.
        // This method traverses the list of ElementTypes and, for each element
        // type with a content model of ANY, adds all other types as children and
        // this type as a parent.

        Enumeration parents, children;
        ElementType parent, child;

        parents = dtd.elementTypes.elements();
        while (parents.hasMoreElements())
        {
            parent = (ElementType)parents.nextElement();
            if (parent.contentType == ElementType.CONTENT_ANY)
            {
                children = dtd.elementTypes.elements();
                while (children.hasMoreElements())
                {
                    // I think this is the code equivalent of "Who's on first?"

                    child = (ElementType)children.nextElement();
                    parent.children.put(child.name, child);
                    child.parents.put(parent.name, parent);
                }
            }
        }
    }

    private void checkElementTypeReferences()
        throws DTDException
    {
        // Make sure that all referenced element types are defined.

        Enumeration parents, children;
        ElementType parent, child;

        parents = dtd.elementTypes.elements();
        while (parents.hasMoreElements())
        {
            parent = (ElementType)parents.nextElement();
            if (!parent.children.isEmpty())
            {
                children = parent.children.elements();
                while (children.hasMoreElements())
                {
                    child = (ElementType)children.nextElement();
                    if (!declaredElementTypes.containsKey(child.name))
                    {
                        throw new DTDException("Element type " +
                            child.name.getUniversalName() +
                            " is referenced in element type " +
                            parent.name.getUniversalName() +
                            " but is never defined.");
                    }
                }
            }
        }
    }

    private void checkNotationReferences()
        throws DTDException
    {
        // Checks that all notations referred to Attributes have been defined.

        Enumeration    e1, e2;
        ElementType    elementType;
        Attribute      attribute;
        String         notation;
        UnparsedEntity entity;

        e1 = dtd.elementTypes.elements();
        while (e1.hasMoreElements())
        {
            elementType = (ElementType)e1.nextElement();
            e2 = elementType.attributes.elements();
            while (e2.hasMoreElements())
            {
                attribute = (Attribute)e2.nextElement();
                if (attribute.type == Attribute.TYPE_NOTATION)
                {
                    for (int i = 0; i < attribute.enums.size(); i++)
                    {
                        notation = (String)attribute.enums.elementAt(i);

                        if (!dtd.notations.containsKey(notation))
                        {
                            throw new DTDException("Notation " + notation +
                                " not defined. Used by the " +
                                attribute.name.getUniversalName() +
                                " attribute of the " +
                                elementType.name.getUniversalName() +
                                " element type.");
                        }
                    }
                }
            }
        }

        e1 = dtd.unparsedEntities.elements();
        while (e1.hasMoreElements())
        {
            entity = (UnparsedEntity)e1.nextElement();

            if (!dtd.notations.containsKey(entity.notation))
            {
                throw new DTDException("Notation " + entity.notation +
                    " not defined. Used by the " +
                    entity.name + " unparsed entity.");
            }
        }
    }

    void throwDTDException(String s)
        throws DTDException
    {
        throw new DTDException(s + "\nLine: " + line + " Column: " + column +
            " in " + readerURL);
    }

    // ********************************************************************
    // Methods -- checking
    //
    // NOTE: These methods are all designed on the notion that they start
    // checking whatever it is they are checking at the *next* character.
    // Therefore, methods that stop only by hitting something else (such
    // as isWhitespace()) must restore the last character read. This
    // probably isn't the Parsing 101 way to do things, but it provides
    // a consistent model that is easy to understand.
    // ********************************************************************

    boolean isWhitespace()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Checks if the next character is whitespace. If not, the
        // position is restored.

        if (isWhitespace(nextChar())) return true;
        restore();
        return false;
    }

    void requireWhitespace()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Checks that the next character is whitespace and discards
        // that characters and any following whitespace characters.

        if (!isWhitespace())
            throwDTDException("Whitespace required.");
        discardWhitespace();
    }

    void discardWhitespace()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Discards a sequence of whitespace.
        while (isWhitespace());
    }

    void discardUntil(String s)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Discards a sequence of characters, stopping only after the
        // first occurrence of s is found.

        char[] chars = s.toCharArray();
        char   c;
        int    pos = 0;

        while (pos < chars.length)
        {
            c = nextChar();
            pos = (c == chars[pos]) ? pos + 1 : 0;
        }
    }

    boolean isString(String s)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Checks if the next sequence of characters matches s. If not,
        // the position is restored.

        char[] chars = s.toCharArray();
        char   c;
        int    pos = 0;

        for (int i = 0; i < chars.length; i++)
        {
            if ((c = nextChar()) != chars[i])
            {
                // Change the last character in the array to the current
                // character. Everything else up to this point has matched,
                // so there is no reason to change any earlier characters.

                chars[i] = c;
                restore(new String(chars, 0, i + 1));
                return false;
            }
        }

        return true;
    }

    boolean isChar(char c)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Checks if the next character matches c. If not, the position
        // is restored.

        if (nextChar() == c) return true;
        restore();
        return false;
    }

    void requireString(String s)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Checks that the next sequence of characters matches s.

        if (!isString(s))
        {
            throwDTDException("String required: " + s);
        }
    }

    void requireChar(char c)
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Checks that the next character matches c.

        if (!isChar(c))
            throwDTDException("Character required: " + c);
    }

    // ********************************************************************
    // Methods -- production matching
    //
    // These methods return something that matches a low-level production
    // such as AttValue or Nmtoken. Like the checking methods, all assume
    // that you start checking with the *next* character and all leave
    // the pointer at the end of whatever it is you are checking, such as
    // the last character in a Nmtoken.
    //
    // NOTE: Most of these productions check for enclosing quotes, even when
    // these quotes are part of a higher-level production.
    // ********************************************************************

    String getAttValue()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Gets something that matches the AttValue production. May be empty.

        char quote, c;

        // Set things up.

        entityState = STATE_ATTVALUE;
        quote = getQuote();
        resetLiteralBuffer();

        // Process the characters. Remember that quotes can be ignored if they
        // are included as part of a parameter entity (Included as Literal) and
        // that markup (&, <) can be ignored if included as a character reference
        // (Included). See section 4.4.

        c = nextChar();
        while ((c != quote) || ignoreQuote)
        {
            if ((c == '<') || (c == '&'))
            {
                if (!ignoreMarkup)
                    throwDTDException("Markup character '" + c + "' not allowed in default attribute value.");
            }
            appendLiteralBuffer(c);
            c = nextChar();
        }

        // Reset the state and return the value.

        entityState = STATE_DTD;
        return getLiteralBuffer();
    }

    String getEncName()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        char quote, c;

        // Set things up.
        quote = getQuote();
        resetLiteralBuffer();

        // Process the first character.
        c = nextChar();
        if (!isLatinLetter(c))
            throwDTDException("Invalid starting character in encoding name: " + c);

        // Process the remaining characters
        while ((c = nextChar()) != quote)
        {
            if (!isLatinLetter(c) && !isLatinDigit(c) &&
                (c != '.') && (c != '_') && (c != '-'))
                throwDTDException("Invalid character in encoding name: " + c);
            appendLiteralBuffer(c);
        }

        // Return the literal.
        return getLiteralBuffer();
    }

    String getEntityValue()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Gets something that matches the EntityValue production. May be empty.
        // Gets something that matches the AttValue production. May be empty.

        char quote, c;

        // Set things up.

        entityState = STATE_ENTITYVALUE;
        quote = getQuote();
        resetLiteralBuffer();

        // Process the characters. Remember that quotes can be ignored if they
        // are included as part of a parameter entity (Included as Literal) and
        // that markup (%, <) can be ignored if included as a character reference
        // (Included). See section 4.4.

        c = nextChar();
        while ((c != quote) || ignoreQuote)
        {
            if ((c == '<') || (c == '%'))
            {
                if (!ignoreMarkup)
                {
                    throwDTDException("Markup character '" + c +
                        "' not allowed in entity value.");
                }
            }
            appendLiteralBuffer(c);
            c = nextChar();
        }

        // Reset the state and return the value.

        entityState = STATE_DTD;
        return getLiteralBuffer();
    }

    String getName()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Gets something that matches the Name production. Must be non-empty.

        char c;

        // Set things up.

        resetLiteralBuffer();

        // Get the first character.

        c = nextChar();
        if (!isLetter(c) && (c != '_') && (c != ':'))
        {
            throwDTDException("Invalid name start character: " + c);
        }

        // Get characters until you hit a non-NameChar, then restore the last
        // character read.

        while (isNameChar(c))
        {
            appendLiteralBuffer(c);
            c = nextChar();
        }
        restore();

        // Return the buffered characters.

        return getLiteralBuffer();
    }

    String getNmtoken()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Gets something that matches the Nmtoken production. Must be non-empty.

        char c;

        // Set things up.

        resetLiteralBuffer();

        // Get the first character.

        c = nextChar();
        if (!isNameChar(c))
            throwDTDException("Invalid Nmtoken start character: " + c);

        // Get characters until you hit a non-NameChar, then restore the last
        // character read.

        while (isNameChar(c))
        {
            appendLiteralBuffer(c);
            c = nextChar();
        }
        restore();

        // Return the buffered characters.

        return getLiteralBuffer();
    }

    String getPubidLiteral()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Gets something that matches the PubidLiteral production. May be empty.

        char quote, c;

        quote = getQuote();
        resetLiteralBuffer();

        while ((c = nextChar()) != quote)
        {
            if (!isPubidChar(c))
            {
                throwDTDException(
                    "Invalid character in public identifier: " + c);
            }
            appendLiteralBuffer(c);
        }

        return getLiteralBuffer();
    }

    char getQuote()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        char quote;

        quote = nextChar();
        if ((quote != '\'') && (quote != '"'))
        {
            throwDTDException("Quote character required.");
        }

        return quote;
    }

    String getSystemLiteral()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // Gets something that matches the SystemLiteral production. May be empty.

        char quote, c;

        quote = getQuote();
        resetLiteralBuffer();

        while ((c = nextChar()) != quote)
        {
            appendLiteralBuffer(c);
        }
        return getLiteralBuffer();
    }

    String getYesNo()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        char    quote;
        boolean no = true;

        quote = getQuote();
        if (!isString("no"))
        {
            requireString("yes");
            no = false;
        }
        requireChar(quote);
        return ((no) ? "no" : "yes");
    }

    void resetLiteralBuffer()
    {
        literalPos = -1;
        literalStr = null;
    }

    void appendLiteralBuffer(char c)
    {
        literalPos++;
        if (literalPos >= LITBUFSIZE)
        {
            if (literalStr == null)
            {
                literalStr = new StringBuffer();
            }
            literalStr.append(literalBuffer);
            literalPos = 0;
        }
        literalBuffer[literalPos] = c;
    }

    String getLiteralBuffer()
    {
        if (literalStr == null)
        {
            return new String(literalBuffer, 0, literalPos + 1);
        }
        else
        {
            literalStr.append(literalBuffer, 0, literalPos + 1);
            return literalStr.toString();
        }
    }

    // ********************************************************************
    // Methods -- various character tests.
    //
    // Note that these tests do not read characters. They also aren't going
    // to win any speed contests, but they do work...
    // ********************************************************************

    boolean isWhitespace(char c)
    {
        // Checks if the specified character is whitespace.

        switch (c)
        {
        case 0x20:   // Space
        case 0x09:   // Carriage return
        case 0x0a:   // Line feed
        case 0x0d:   // Tab
            return true;

        default:
            return false;
        }
    }

    boolean isLatinLetter(char c)
    {
        return (((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')));
    }

    boolean isLatinDigit(char c)
    {
        return ((c >= '0') && (c <= '9'));
    }

    boolean isPubidChar(char c)
    {
        switch (c)
        {
        case '-':
        case '\'':
        case '(':
        case ')':
        case '+':
        case ',':
        case '.':
        case '/':
        case ':':
        case '=':
        case '?':
        case ';':
        case '!':
        case '*':
        case '#':
        case '@':
        case '$':
        case '_':
        case '%':
        case 0x20:
        case 0xD:
        case 0xA:
            return true;

        default:
            return (isLatinLetter(c) || isLatinDigit(c));
        }
    }

    boolean isNameChar(char c)
    {
        if (isLatinLetter(c)) return true;
        if (isLatinDigit(c)) return true;
        if ((c == '.') || (c == '-') || (c == '_') || (c == ':')) return true;
        if (isLetter(c)) return true;
        if (isDigit(c)) return true;
        if (isCombiningChar(c)) return true;
        if (isExtender(c)) return true;
        return false;
    }

    boolean isLetter(char c)
    {
        // Checks for letters (BaseChar | Ideographic)

        switch(c >> 8)
        {
        case 0x00:
            if ((c >= 0x0041) && (c <= 0x005A)) return true;
            if ((c >= 0x0061) && (c <= 0x007A)) return true;
            if ((c >= 0x00C0) && (c <= 0x00D6)) return true;
            if ((c >= 0x00D8) && (c <= 0x00F6)) return true;
            if ((c >= 0x00F8) && (c <= 0x00FF)) return true;

            return false;

        case 0x01:
            if ((c >= 0x0100) && (c <= 0x0131)) return true;
            if ((c >= 0x0134) && (c <= 0x013E)) return true;
            if ((c >= 0x0141) && (c <= 0x0148)) return true;
            if ((c >= 0x014A) && (c <= 0x017E)) return true;
            if ((c >= 0x0180) && (c <= 0x01C3)) return true;
            if ((c >= 0x01CD) && (c <= 0x01F0)) return true;
            if ((c >= 0x01F4) && (c <= 0x01F5)) return true;
            if ((c >= 0x01FA) && (c <= 0x01FF)) return true;

            return false;

        case 0x02:
            if ((c >= 0x0200) && (c <= 0x0217)) return true;
            if ((c >= 0x0250) && (c <= 0x02A8)) return true;
            if ((c >= 0x02BB) && (c <= 0x02C1)) return true;

            return false;

        case 0x03:
            if ((c >= 0x0388) && (c <= 0x038A)) return true;
            if ((c >= 0x038E) && (c <= 0x03A1)) return true;
            if ((c >= 0x03A3) && (c <= 0x03CE)) return true;
            if ((c >= 0x03D0) && (c <= 0x03D6)) return true;
            if ((c >= 0x03E2) && (c <= 0x03F3)) return true;

            if ((c == 0x0386)  || (c == 0x038C)  || (c == 0x03DA)  ||
                (c == 0x03DC)  || (c == 0x03DE)  || (c == 0x03E0)) return true;

            return false;

        case 0x04:
            if ((c >= 0x0401) && (c <= 0x040C)) return true;
            if ((c >= 0x040E) && (c <= 0x044F)) return true;
            if ((c >= 0x0451) && (c <= 0x045C)) return true;
            if ((c >= 0x045E) && (c <= 0x0481)) return true;
            if ((c >= 0x0490) && (c <= 0x04C4)) return true;
            if ((c >= 0x04C7) && (c <= 0x04C8)) return true;
            if ((c >= 0x04CB) && (c <= 0x04CC)) return true;
            if ((c >= 0x04D0) && (c <= 0x04EB)) return true;
            if ((c >= 0x04EE) && (c <= 0x04F5)) return true;
            if ((c >= 0x04F8) && (c <= 0x04F9)) return true;

            return false;

        case 0x05:
            if ((c >= 0x0531) && (c <= 0x0556)) return true;
            if ((c >= 0x0561) && (c <= 0x0586)) return true;
            if ((c >= 0x05D0) && (c <= 0x05EA)) return true;
            if ((c >= 0x05F0) && (c <= 0x05F2)) return true;

            if (c == 0x0559) return true;

            return false;

        case 0x06:
            if ((c >= 0x0621) && (c <= 0x063A)) return true;
            if ((c >= 0x0641) && (c <= 0x064A)) return true;
            if ((c >= 0x0671) && (c <= 0x06B7)) return true;
            if ((c >= 0x06BA) && (c <= 0x06BE)) return true;
            if ((c >= 0x06C0) && (c <= 0x06CE)) return true;
            if ((c >= 0x06D0) && (c <= 0x06D3)) return true;
            if ((c >= 0x06E5) && (c <= 0x06E6)) return true;

            if (c == 0x06D5) return true;

            return false;

        case 0x09:
            if ((c >= 0x0905) && (c <= 0x0939)) return true;
            if ((c >= 0x0958) && (c <= 0x0961)) return true;
            if ((c >= 0x0985) && (c <= 0x098C)) return true;
            if ((c >= 0x098F) && (c <= 0x0990)) return true;
            if ((c >= 0x0993) && (c <= 0x09A8)) return true;
            if ((c >= 0x09AA) && (c <= 0x09B0)) return true;
            if ((c >= 0x09B6) && (c <= 0x09B9)) return true;
            if ((c >= 0x09DC) && (c <= 0x09DD)) return true;
            if ((c >= 0x09DF) && (c <= 0x09E1)) return true;
            if ((c >= 0x09F0) && (c <= 0x09F1)) return true;

            if ((c == 0x093D)  || (c == 0x09B2)) return true;

            return false;

        case 0x0A:
            if ((c >= 0x0A05) && (c <= 0x0A0A)) return true;
            if ((c >= 0x0A0F) && (c <= 0x0A10)) return true;
            if ((c >= 0x0A13) && (c <= 0x0A28)) return true;
            if ((c >= 0x0A2A) && (c <= 0x0A30)) return true;
            if ((c >= 0x0A32) && (c <= 0x0A33)) return true;
            if ((c >= 0x0A35) && (c <= 0x0A36)) return true;
            if ((c >= 0x0A38) && (c <= 0x0A39)) return true;
            if ((c >= 0x0A59) && (c <= 0x0A5C)) return true;
            if ((c >= 0x0A72) && (c <= 0x0A74)) return true;
            if ((c >= 0x0A85) && (c <= 0x0A8B)) return true;
            if ((c >= 0x0A8F) && (c <= 0x0A91)) return true;
            if ((c >= 0x0A93) && (c <= 0x0AA8)) return true;
            if ((c >= 0x0AAA) && (c <= 0x0AB0)) return true;
            if ((c >= 0x0AB2) && (c <= 0x0AB3)) return true;
            if ((c >= 0x0AB5) && (c <= 0x0AB9)) return true;

            if ((c == 0x0A5E)  || (c == 0x0A8D)  || (c == 0x0ABD)  ||
                (c == 0x0AE0)) return true;

            return false;

        case 0x0B:
            if ((c >= 0x0B05) && (c <= 0x0B0C)) return true;
            if ((c >= 0x0B0F) && (c <= 0x0B10)) return true;
            if ((c >= 0x0B13) && (c <= 0x0B28)) return true;
            if ((c >= 0x0B2A) && (c <= 0x0B30)) return true;
            if ((c >= 0x0B32) && (c <= 0x0B33)) return true;
            if ((c >= 0x0B36) && (c <= 0x0B39)) return true;
            if ((c >= 0x0B5C) && (c <= 0x0B5D)) return true;
            if ((c >= 0x0B5F) && (c <= 0x0B61)) return true;
            if ((c >= 0x0B85) && (c <= 0x0B8A)) return true;
            if ((c >= 0x0B8E) && (c <= 0x0B90)) return true;
            if ((c >= 0x0B92) && (c <= 0x0B95)) return true;
            if ((c >= 0x0B99) && (c <= 0x0B9A)) return true;
            if ((c >= 0x0B9E) && (c <= 0x0B9F)) return true;
            if ((c >= 0x0BA3) && (c <= 0x0BA4)) return true;
            if ((c >= 0x0BA8) && (c <= 0x0BAA)) return true;
            if ((c >= 0x0BAE) && (c <= 0x0BB5)) return true;
            if ((c >= 0x0BB7) && (c <= 0x0BB9)) return true;

            if ((c == 0x0B3D)  || (c == 0x0B9C)) return true;

            return false;

        case 0x0C:
            if ((c >= 0x0C05) && (c <= 0x0C0C)) return true;
            if ((c >= 0x0C0E) && (c <= 0x0C10)) return true;
            if ((c >= 0x0C12) && (c <= 0x0C28)) return true;
            if ((c >= 0x0C2A) && (c <= 0x0C33)) return true;
            if ((c >= 0x0C35) && (c <= 0x0C39)) return true;
            if ((c >= 0x0C60) && (c <= 0x0C61)) return true;
            if ((c >= 0x0C85) && (c <= 0x0C8C)) return true;
            if ((c >= 0x0C8E) && (c <= 0x0C90)) return true;
            if ((c >= 0x0C92) && (c <= 0x0CA8)) return true;
            if ((c >= 0x0CAA) && (c <= 0x0CB3)) return true;
            if ((c >= 0x0CB5) && (c <= 0x0CB9)) return true;
            if ((c >= 0x0CE0) && (c <= 0x0CE1)) return true;

            if (c == 0x0CDE) return true;

            return false;

        case 0x0D:
            if ((c >= 0x0D05) && (c <= 0x0D0C)) return true;
            if ((c >= 0x0D0E) && (c <= 0x0D10)) return true;
            if ((c >= 0x0D12) && (c <= 0x0D28)) return true;
            if ((c >= 0x0D2A) && (c <= 0x0D39)) return true;
            if ((c >= 0x0D60) && (c <= 0x0D61)) return true;

            return false;

        case 0x0E:
            if ((c >= 0x0E01) && (c <= 0x0E2E)) return true;
            if ((c >= 0x0E32) && (c <= 0x0E33)) return true;
            if ((c >= 0x0E40) && (c <= 0x0E45)) return true;
            if ((c >= 0x0E81) && (c <= 0x0E82)) return true;
            if ((c >= 0x0E87) && (c <= 0x0E88)) return true;
            if ((c >= 0x0E94) && (c <= 0x0E97)) return true;
            if ((c >= 0x0E99) && (c <= 0x0E9F)) return true;
            if ((c >= 0x0EA1) && (c <= 0x0EA3)) return true;
            if ((c >= 0x0EAA) && (c <= 0x0EAB)) return true;
            if ((c >= 0x0EAD) && (c <= 0x0EAE)) return true;
            if ((c >= 0x0EB2) && (c <= 0x0EB3)) return true;
            if ((c >= 0x0EC0) && (c <= 0x0EC4)) return true;

            if ((c == 0x0E30)  || (c == 0x0E84)  || (c == 0x0E8A)  ||
                (c == 0x0E8D)  || (c == 0x0EA5)  || (c == 0x0EA7)  ||
                (c == 0x0EB0)  || (c == 0x0EBD)) return true;

            return false;

        case 0x0F:
            if ((c >= 0x0F40) && (c <= 0x0F47)) return true;
            if ((c >= 0x0F49) && (c <= 0x0F69)) return true;

            return false;

        case 0x10:
            if ((c >= 0x10A0) && (c <= 0x10C5)) return true;
            if ((c >= 0x10D0) && (c <= 0x10F6)) return true;

            return false;

        case 0x11:
            if ((c >= 0x1102) && (c <= 0x1103)) return true;
            if ((c >= 0x1105) && (c <= 0x1107)) return true;
            if ((c >= 0x110B) && (c <= 0x110C)) return true;
            if ((c >= 0x110E) && (c <= 0x1112)) return true;
            if ((c >= 0x1154) && (c <= 0x1155)) return true;
            if ((c >= 0x115F) && (c <= 0x1161)) return true;
            if ((c >= 0x116D) && (c <= 0x116E)) return true;
            if ((c >= 0x1172) && (c <= 0x1173)) return true;
            if ((c >= 0x11AE) && (c <= 0x11AF)) return true;
            if ((c >= 0x11B7) && (c <= 0x11B8)) return true;
            if ((c >= 0x11BC) && (c <= 0x11C2)) return true;

            if ((c == 0x1100)  || (c == 0x1109)  || (c == 0x113C)  ||
                (c == 0x113E)  || (c == 0x1140)  || (c == 0x114C)  ||
                (c == 0x114E)  || (c == 0x1150)  || (c == 0x1159)  ||
                (c == 0x1163)  || (c == 0x1165)  || (c == 0x1167)  ||
                (c == 0x1169)  || (c == 0x1175)  || (c == 0x119E)  ||
                (c == 0x11A8)  || (c == 0x11AB)  || (c == 0x11BA)  ||
                (c == 0x11EB)  || (c == 0x11F0)  || (c == 0x11F9)) return true;

            return false;

        case 0x1E:
            if ((c >= 0x1E00) && (c <= 0x1E9B)) return true;
            if ((c >= 0x1EA0) && (c <= 0x1EF9)) return true;

            return false;

        case 0x1F:
            if ((c >= 0x1F00) && (c <= 0x1F15)) return true;
            if ((c >= 0x1F18) && (c <= 0x1F1D)) return true;
            if ((c >= 0x1F20) && (c <= 0x1F45)) return true;
            if ((c >= 0x1F48) && (c <= 0x1F4D)) return true;
            if ((c >= 0x1F50) && (c <= 0x1F57)) return true;
            if ((c >= 0x1F5F) && (c <= 0x1F7D)) return true;
            if ((c >= 0x1F80) && (c <= 0x1FB4)) return true;
            if ((c >= 0x1FB6) && (c <= 0x1FBC)) return true;
            if ((c >= 0x1FC2) && (c <= 0x1FC4)) return true;
            if ((c >= 0x1FC6) && (c <= 0x1FCC)) return true;
            if ((c >= 0x1FD0) && (c <= 0x1FD3)) return true;
            if ((c >= 0x1FD6) && (c <= 0x1FDB)) return true;
            if ((c >= 0x1FE0) && (c <= 0x1FEC)) return true;
            if ((c >= 0x1FF2) && (c <= 0x1FF4)) return true;
            if ((c >= 0x1FF6) && (c <= 0x1FFC)) return true;

            if ((c == 0x1F59)  || (c == 0x1F5B)  || (c == 0x1F5D)  ||
                (c == 0x1FBE)) return true;

            return false;

        case 0x21:
            if ((c >= 0x212A) && (c <= 0x212B)) return true;
            if ((c >= 0x2180) && (c <= 0x2182)) return true;

            if ((c == 0x2126)  || (c == 0x212E)) return true;

            return false;

        case 0x20:
            if ((c >= 0x3041) && (c <= 0x3094)) return true;
            if ((c >= 0x30A1) && (c <= 0x30FA)) return true;
            if ((c >= 0x3021) && (c <= 0x3029)) return true;

            if (c == 0x3007) return true;

            return false;

        case 0x31:
            if ((c >= 0x3105) && (c <= 0x312C)) return true;

            return false;

        default:
            if ((c >= 0xAC00) && (c <= 0xD7A3)) return true;
            if ((c >= 0x4E00) && (c <= 0x9FA5)) return true;

            return false;
        }
    }

    boolean isDigit(char c)
    {
        // Checks for digits. Note that the Java Character.isDigit() function
        // includes the values 0xFF10 - 0xFF19, which are not considered digits
        // according to the XML spec. Therefore, we need to check if these are
        // the reason Character.isDigit() returned true.

        if (!Character.isDigit(c)) return false;
        return (c > 0xF29);
    }

    boolean isCombiningChar(char c)
    {
        // Checks for combining characters.

        switch (c >> 8)
        {
        case 0x03:
            if ((c >= 0x0300) && (c <= 0x0345)) return true;
            if ((c >= 0x0360) && (c <= 0x0361)) return true;

            return false;

        case 0x04:
            if ((c >= 0x0483) && (c <= 0x0486)) return true;

            return false;

        case 0x05:
            if ((c >= 0x0591) && (c <= 0x05A1)) return true;
            if ((c >= 0x05A3) && (c <= 0x05B9)) return true;
            if ((c >= 0x05BB) && (c <= 0x05BD)) return true;
            if ((c >= 0x05C1) && (c <= 0x05C2)) return true;

            if ((c == 0x05BF) || (c == 0x05C4)) return true;

            return false;

        case 0x06:
            if ((c >= 0x064B) && (c <= 0x0652)) return true;
            if ((c >= 0x06D6) && (c <= 0x06DC)) return true;
            if ((c >= 0x06DD) && (c <= 0x06DF)) return true;
            if ((c >= 0x06E0) && (c <= 0x06E4)) return true;
            if ((c >= 0x06E7) && (c <= 0x06E8)) return true;
            if ((c >= 0x06EA) && (c <= 0x06ED)) return true;

            if (c == 0x0670) return true;

            return false;

        case 0x09:
            if ((c >= 0x0901) && (c <= 0x0903)) return true;
            if ((c >= 0x093E) && (c <= 0x094C)) return true;
            if ((c >= 0x0951) && (c <= 0x0954)) return true;
            if ((c >= 0x0962) && (c <= 0x0963)) return true;
            if ((c >= 0x0981) && (c <= 0x0983)) return true;
            if ((c >= 0x09C0) && (c <= 0x09C4)) return true;
            if ((c >= 0x09C7) && (c <= 0x09C8)) return true;
            if ((c >= 0x09CB) && (c <= 0x09CD)) return true;
            if ((c >= 0x09E2) && (c <= 0x09E3)) return true;

            if ((c == 0x093C) || (c == 0x094D) || (c == 0x09BC) ||
                (c == 0x09BE) || (c == 0x09BF) || (c == 0x09D7)) return true;

            return false;

        case 0x0A:
            if ((c >= 0x0A40) && (c <= 0x0A42)) return true;
            if ((c >= 0x0A47) && (c <= 0x0A48)) return true;
            if ((c >= 0x0A4B) && (c <= 0x0A4D)) return true;
            if ((c >= 0x0A70) && (c <= 0x0A71)) return true;
            if ((c >= 0x0A81) && (c <= 0x0A83)) return true;
            if ((c >= 0x0ABE) && (c <= 0x0AC5)) return true;
            if ((c >= 0x0AC7) && (c <= 0x0AC9)) return true;
            if ((c >= 0x0ACB) && (c <= 0x0ACD)) return true;

            if ((c == 0x0A02) || (c == 0x0A3C) || (c == 0x0A3E) ||
                (c == 0x0A3F) || (c == 0x0ABC)) return true;

            return false;

        case 0x0B:
            if ((c >= 0x0B01) && (c <= 0x0B03)) return true;
            if ((c >= 0x0B3E) && (c <= 0x0B43)) return true;
            if ((c >= 0x0B47) && (c <= 0x0B48)) return true;
            if ((c >= 0x0B4B) && (c <= 0x0B4D)) return true;
            if ((c >= 0x0B56) && (c <= 0x0B57)) return true;
            if ((c >= 0x0B82) && (c <= 0x0B83)) return true;
            if ((c >= 0x0BBE) && (c <= 0x0BC2)) return true;
            if ((c >= 0x0BC6) && (c <= 0x0BC8)) return true;
            if ((c >= 0x0BCA) && (c <= 0x0BCD)) return true;

            if ((c == 0x0B3C) || (c == 0x0BD7)) return true;

            return false;

        case 0x0C:
            if ((c >= 0x0C01) && (c <= 0x0C03)) return true;
            if ((c >= 0x0C3E) && (c <= 0x0C44)) return true;
            if ((c >= 0x0C46) && (c <= 0x0C48)) return true;
            if ((c >= 0x0C4A) && (c <= 0x0C4D)) return true;
            if ((c >= 0x0C55) && (c <= 0x0C56)) return true;
            if ((c >= 0x0C82) && (c <= 0x0C83)) return true;
            if ((c >= 0x0CBE) && (c <= 0x0CC4)) return true;
            if ((c >= 0x0CC6) && (c <= 0x0CC8)) return true;
            if ((c >= 0x0CCA) && (c <= 0x0CCD)) return true;
            if ((c >= 0x0CD5) && (c <= 0x0CD6)) return true;
            return false;

        case 0x0D:
            if ((c >= 0x0D02) && (c <= 0x0D03)) return true;
            if ((c >= 0x0D3E) && (c <= 0x0D43)) return true;
            if ((c >= 0x0D46) && (c <= 0x0D48)) return true;
            if ((c >= 0x0D4A) && (c <= 0x0D4D)) return true;

            if (c == 0x0D57) return true;

            return false;

        case 0x0E:
            if ((c >= 0x0E34) && (c <= 0x0E3A)) return true;
            if ((c >= 0x0E47) && (c <= 0x0E4E)) return true;
            if ((c >= 0x0EB4) && (c <= 0x0EB9)) return true;
            if ((c == 0x0EBB) && (c <= 0x0EBC)) return true;
            if ((c >= 0x0EC8) && (c <= 0x0ECD)) return true;

            if ((c == 0x0E31) || (c == 0x0EB1)) return true;

            return false;

        case 0x0F:
            if ((c >= 0x0F18) && (c <= 0x0F19)) return true;
            if ((c >= 0x0F71) && (c <= 0x0F84)) return true;
            if ((c >= 0x0F86) && (c <= 0x0F8B)) return true;
            if ((c >= 0x0F90) && (c <= 0x0F95)) return true;
            if ((c >= 0x0F99) && (c <= 0x0FAD)) return true;
            if ((c >= 0x0FB1) && (c <= 0x0FB7)) return true;

            if ((c == 0x0F35) || (c == 0x0F37) || (c == 0x0F39) ||
                (c == 0x0F3E) || (c == 0x0F3F) || (c == 0x0F97) ||
                (c == 0x0FB9)) return true;

            return false;

        case 0x20:
            if ((c >= 0x20D0) && (c <= 0x20DC)) return true;

            if (c == 0x20E1) return true;

            return false;

        case 0x30:
            if ((c >= 0x302A) && (c <= 0x302F)) return true;

            if ((c == 0x3099) || (c == 0x309A)) return true;

            return false;

        default:
            return false;
        }
    }

    boolean isExtender(char c)
    {
        // Checks for extenders.

        switch (c)
        {
        case 0x00B7:
        case 0x02D0:
        case 0x02D1:
        case 0x0387:
        case 0x0640:
        case 0x0E46:
        case 0x0EC6:
        case 0x3005:
            return true;

        default:
            if ((c >= 0x3031) && (c <= 0x3035)) return true;
            if ((c >= 0x309D) && (c <= 0x309E)) return true;
            if ((c >= 0x30FC) && (c <= 0x30FE)) return true;
            return false;
        }
    }

    // ********************************************************************
    // Methods -- Entity handling
    // ********************************************************************

    char nextChar()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // This method gets a character and then deals with the Joy of Entities.

        char c;

        c = getChar();

        switch (c)
        {
        case '&':
            c = processAmpersand();
            break;

        case '%':
            c = processPercent();
            break;

        default:
            break;
        }

        if (c == '\n')
        {
            line++;
            column = 1;
        }
        else
        {
            column++;
        }

        return c;
    }

    char processAmpersand()
        throws DTDException, IOException, EOFException
    {
        char c;

        switch (entityState)
        {
        case STATE_DTD:
            throwDTDException("Invalid general entity reference or character reference.");

        case STATE_ATTVALUE:
            if (getChar() == '#')
            {
                getCharRef();
            }
            else
            {
                restore();
                getGeneralEntityRef();
            }
            return nextChar();

        case STATE_ENTITYVALUE:
            if (getChar() == '#')
            {
                getCharRef();
                return nextChar();
            }
            else
            {
                restore();
                return '&';
            }

        case STATE_OUTSIDEDTD:
        case STATE_COMMENT:
        case STATE_IGNORE:
            return '&';

        default:
            throw new IllegalStateException("Internal error: invalid entity state: " +
                entityState);
        }
    }

    char processPercent()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        char c;

        switch (entityState)
        {
        case STATE_DTD:
            // Check if we are processing a parameter entity declaration
            // rather than a parameter entity reference.
            c = getChar();
            restore();
            if (isWhitespace(c)) return '%';
            getParameterEntityRef();
            return nextChar();

        case STATE_ATTVALUE:
            return '%';

        case STATE_ENTITYVALUE:
            getParameterEntityRef();
            return nextChar();

        case STATE_OUTSIDEDTD:
        case STATE_COMMENT:
        case STATE_IGNORE:
            return '%';

        default:
            throw new IllegalStateException("Internal error: invalid entity state: " +
                entityState);
        }
    }

    void getCharRef()
        throws DTDException, IOException, EOFException
    {
        // &# already parsed.

        boolean hex = false;
        char    c;
        char[]  chars = new char[1];
        int     value = 0;

        // Check if we have a hexadecimal character reference.

        c = getChar();
        if (c == 'x')
        {
            hex = true;
            c = getChar();
        }

        // Parse the character reference and get the value.

        while (c != ';')
        {
            if (hex)
            {
                c = Character.toUpperCase(c);
                if ((c < '0') || (c > 'F') || ((c > '9') && (c < 'A')))
                {
                    throwDTDException("Invalid character in character reference: " + c);
                }
                value *= 16;
                value += (c < 'A') ? c - '0' : c - 'A' + 10;
            }
            else
            {
                if ((c < '0') || (c > '9'))
                {
                    throwDTDException("Invalid character in character reference: " + c);
                }
                value *= 10;
                value += c - '0';
            }
            c = getChar();
        }
        if (value > Character.MAX_VALUE)
        {
            throwDTDException("Invalid character reference: " + value);
        }

        // Push the current Reader and its associated information on the stack,
        // then create a new Reader for the corresponding character.

        pushCurrentReader();

        chars[0] = (char)value;
        createStringReader(new String(chars));
        ignoreQuote = true;
        ignoreMarkup = true;
    }

    void getGeneralEntityRef()
        throws DTDException, IOException, EOFException
    {
        // & already parsed.
        //
        // WARNING! This is not a generic function. It assumes it is called only
        // from inside an attribute value.

        char                c;
        int                 size;
        String              entityName;
        ParsedGeneralEntity entity;

        // Get the general entity name.

        resetNameBuffer();
        while ((c = getChar()) != ';')
        {
            appendNameBuffer(c);
        }
        entityName = getNameBuffer();

        // Push the current Reader and its associated information on the stack.

        pushCurrentReader();

        // Get the general entity and set up the Reader information.

        entity = (ParsedGeneralEntity)dtd.parsedGeneralEntities.get(entityName);
        if (entity == null)
        {
            entity = (ParsedGeneralEntity)predefinedEntities.get(entityName);
            if (entity == null)
            {
                throwDTDException("Reference to undefined parsed general entity: " +
                    entityName);
            }
        }

        if (entity.value == null)
        {
            throwDTDException("Reference to external parsed general entity in attribute value: " +
                entityName);
        }

        createStringReader(entity.value);
        ignoreQuote = true;
        ignoreMarkup = false;
    }

    void getParameterEntityRef()
        throws DTDException, MalformedURLException, IOException, EOFException
    {
        // % already parsed.
        //
        // WARNING! This is not a generic function. It assumes it is called only
        // from inside the DTD or an entity value.

        char            c;
        String          entityName;
        ParameterEntity entity;

        // Get the parameter entity name.

        resetNameBuffer();
        while ((c = getChar()) != ';')
        {
            appendNameBuffer(c);
        }
        entityName = getNameBuffer();

        // Push the current Reader and its associated information on the stack.

        pushCurrentReader();

        // Get the parameter entity.

        entity = (ParameterEntity)dtd.parameterEntities.get(entityName);
        if (entity == null)
        {
            throwDTDException(
                "Reference to undefined parameter entity: " + entityName);
        }

        // Set up the Reader information. Notice that we need to include spaces
        // before and after the entity value. Also notice that we ignore quotes
        // if the parameter entity occurs inside an entity value.

        pushStringReader(" ", false, false);
        if (entity.value != null)
        {
            pushStringReader(entity.value, (entityState == STATE_ENTITYVALUE), false);
        }
        else
        {
            pushURLReader(entity.publicID, entity.systemID,
                (entityState == STATE_ENTITYVALUE), false);
        }

        createStringReader(" ");
        ignoreQuote = false;
        ignoreMarkup = false;
    }

    void createStringReader(String s)
    {
        int size;

        // Note that readerURL is unchanged because the string is in the
        // context of its parent Reader.

        reader = new StringReader(s);
        readerType = READER_READER;
        size = (s.length() > BUFSIZE) ? BUFSIZE : s.length();
        buffer = new char[size];
        bufferPos = BUFSIZE + 1;
        bufferLen = -1;
        line = 1;
        column = 1;
    }

    void createURLReader(URL url)
        throws IOException
    {
        reader = new InputStreamReader(url.openStream());
        readerType = READER_READER;
        readerURL = url;
        buffer = new char[BUFSIZE];
        bufferPos = BUFSIZE + 1;
        bufferLen = 0;
        line = 1;
        column = 1;
    }

    void pushCurrentReader()
    {
        readerStack.push(new ReaderInfo(reader, buffer, readerURL, null, readerType,
            bufferPos, bufferLen, line, column, ignoreQuote, ignoreMarkup));
    }

    void pushStringReader(String s, boolean ignoreQuote, boolean ignoreMarkup)
    {
        readerStack.push(new ReaderInfo(null, null, null, s, READER_STRING, 0, 0, 1, 1,
            ignoreQuote, ignoreMarkup));
    }

    void pushURLReader(String p_publicID, String p_systemID,
        boolean ignoreQuote, boolean ignoreMarkup)
        throws MalformedURLException, IOException
    {
        // CvdL: use the catalog to resolve names, preferring public IDs.
        String urlString = resolveExternalEntity(p_publicID, p_systemID);

        // Create the URL in the context of the current Reader.
        URL url = new URL(readerURL, urlString);

        readerStack.push(new ReaderInfo(null, null, url, null, READER_URL, 0, 0, 1, 1,
            ignoreQuote, ignoreMarkup));
    }

    void popReader()
        throws DTDException, IOException, EOFException
    {
        ReaderInfo readerInfo;

        if (readerStack.empty())
        {
            throw new EOFException("End of file reached while parsing.");
        }

        readerInfo = (ReaderInfo)readerStack.pop();
        switch (readerInfo.type)
        {
        case READER_READER:
            // All this means is that we've already created the Reader
            // and the associated buffers.
            reader = readerInfo.reader;
            readerType = readerInfo.type;
            readerURL = readerInfo.url;
            buffer = readerInfo.buffer;
            bufferPos = readerInfo.bufferPos;
            bufferLen = readerInfo.bufferLen;
            line = readerInfo.line;
            column = readerInfo.column;
            break;

        case READER_STRING:
            // We need to create a Reader over a String.
            createStringReader(readerInfo.str);
            break;

        case READER_URL:
            // We need to create a Reader over a URL.
            createURLReader(readerInfo.url);
            break;
        }

        ignoreQuote = readerInfo.ignoreQuote;
        ignoreMarkup = readerInfo.ignoreMarkup;
    }

    void resetNameBuffer()
    {
        namePos = -1;
        nameStr = null;
    }

    void appendNameBuffer(char c)
    {
        namePos++;
        if (namePos >= LITBUFSIZE)
        {
            if (nameStr == null)
            {
                nameStr = new StringBuffer();
            }
            nameStr.append(nameBuffer);
            namePos = 0;
        }
        nameBuffer[namePos] = c;
    }

    String getNameBuffer()
    {
        if (nameStr == null)
        {
            return new String(nameBuffer, 0, namePos + 1);
        }
        else
        {
            nameStr.append(nameBuffer, 0, namePos + 1);
            return nameStr.toString();
        }
    }

    // ********************************************************************
    // Methods -- I/O
    // ********************************************************************

    void initReaderGlobals()
    {
        reader = null;
        readerURL = null;
        line = 1;
        column = 1;
        ignoreQuote = false;
        ignoreMarkup = false;
    }

    char getChar()
        throws DTDException, IOException, EOFException
    {
        // This function just gets the next character in the buffer. Entity
        // processing is done at a higher level (nextChar). Note that nextChar()
        // may change the value of reader.

        char c;

        if (bufferPos >= bufferLen)
        {
            bufferLen = reader.read(buffer, 0, buffer.length);
            if (bufferLen == -1)
            {
                // If we've hit the end of the Reader, pop the Reader off the
                // stack and get the first character in the next Reader.

                popReader();
                return getChar();
            }
            else
            {
                bufferPos = 0;
            }
        }

        // Uncomment the following line for debugging. Note that what is
        // printed is everything that goes past this function. Some characters
        // go past multiple times because of being tested and restored, as
        // with isWhitespace() or isString(String).

        // System.out.print(buffer[bufferPos]);

        // Return the character.

        return buffer[bufferPos++];
    }

    void restore()
    {
        // To restore a single character, all we need to do is decrement the
        // buffer position. It isn't really important what the character is. This
        // is obviously true in the middle of the buffer. At the bottom end, it
        // means that a new buffer full of data has just been read and that we
        // have just returned the first character; thus, the bufferPos can never
        // be 0 in this case. At the top end, it means we have just returned the
        // last character; thus, we can safely restore it.
        //
        // WARNING! This function assumes that it is never called more than once
        // in succession. If it is, it can run off the lower end of the buffer.

        bufferPos--;
    }

    void restore(String s)
    {
        pushCurrentReader();
        createStringReader(s);
    }

    void openInputSource(InputSource src)
        throws DTDException, IOException
    {
        String      srcURL;
        Reader      srcReader;
        InputStream srcStream;

        srcURL = src.getSystemId();

        try
        {
            readerURL = new URL(srcURL);
        }
        catch (MalformedURLException e)
        {
            readerURL = null;
        }

        if ((srcReader = src.getCharacterStream()) != null)
        {
            reader = srcReader;
            readerType = READER_READER;
            buffer = new char[BUFSIZE];
            bufferPos = BUFSIZE + 1;
            bufferLen = 0;
        }
        else if ((srcStream = src.getByteStream()) != null)
        {
            reader = new InputStreamReader(srcStream);
            readerType = READER_READER;
            buffer = new char[BUFSIZE];
            bufferPos = BUFSIZE + 1;
            bufferLen = 0;
        }
        else if (readerURL != null)
        {
            createURLReader(readerURL);
        }
        else
        {
            throwDTDException("InputSource does not have a character stream, byte stream, or system ID.");
        }
    }

    // CvdL - query catalog to resolve external dtds
    public String resolveExternalEntity(String p_publicID, String p_systemID)
        throws MalformedURLException, IOException
    {
        String result = null;

        if (p_publicID != null)
        {
            result = m_catalog.resolvePublic(p_publicID, p_systemID);
        }
        else if (p_systemID != null)
        {
            result = m_catalog.resolveSystem(p_systemID);
        }

        // System.err.println("Resolved entity PUBLIC " +
        // p_publicID + " SYSTEM " + p_systemID + " to " + result);

        if (result == null)
        {
            throw new IOException("Could not resolve entity PUBLIC " + p_publicID +
                " SYSTEM " + p_systemID);
        }

        return result;
    }

    // ********************************************************************
    // Inner class -- Reader information
    // ********************************************************************

    class ReaderInfo
    {
        Reader  reader;
        char[]  buffer;
        URL     url;
        String  str;
        int     type,
            bufferPos,
            bufferLen,
            line,
            column;
        boolean ignoreQuote,
            ignoreMarkup;

        ReaderInfo(Reader reader,
            char[] buffer,
            URL url,
            String str,
            int type,
            int bufferPos,
            int bufferLen,
            int line,
            int column,
            boolean ignoreQuote,
            boolean ignoreMarkup)
        {
            this.reader = reader;
            this.buffer = buffer;
            this.url = url;
            this.str = str;
            this.type = type;
            this.bufferPos = bufferPos;
            this.bufferLen = bufferLen;
            this.line = line;
            this.column = column;
            this.ignoreQuote = ignoreQuote;
            this.ignoreMarkup = ignoreMarkup;
        }
    }
}
