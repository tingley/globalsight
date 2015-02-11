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
package com.globalsight.ling.jtidy;

/**
 * Tag dictionary node. If the document uses just HTML 2.0 tags and
 * attributes described it as HTML 2.0. Similarly for HTML 3.2 and the
 * 3 flavors of HTML 4.0. If there are proprietary tags and attributes
 * then describe it as HTML Proprietary. If it includes the xml-lang
 * or xmlns attributes but is otherwise HTML 2.0, 3.2 or 4.0 then
 * describe it as one of the flavors of Voyager (strict, loose or
 * frameset).
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class Dict
{
    /**
     * Content model: unknown.
     */
    public static final int CM_UNKNOWN = 0;

    /**
     * Content model: empty.
     */
    public static final int CM_EMPTY = (1 << 0);

    /**
     * Content model: html.
     */
    public static final int CM_HTML = (1 << 1);

    /**
     * Content model: head.
     */
    public static final int CM_HEAD = (1 << 2);

    /**
     * Content model: block.
     */
    public static final int CM_BLOCK = (1 << 3);

    /**
     * Content model: inline.
     */
    public static final int CM_INLINE = (1 << 4);

    /**
     * Content model: list.
     */
    public static final int CM_LIST = (1 << 5);

    /**
     * Content model: definition list.
     */
    public static final int CM_DEFLIST = (1 << 6);

    /**
     * Content model: table.
     */
    public static final int CM_TABLE = (1 << 7);

    /**
     * Content model: rowgroup.
     */
    public static final int CM_ROWGRP = (1 << 8);

    /**
     * Content model: row.
     */
    public static final int CM_ROW = (1 << 9);

    /**
     * Content model: field.
     */
    public static final int CM_FIELD = (1 << 10);

    /**
     * Content model: object.
     */
    public static final int CM_OBJECT = (1 << 11);

    /**
     * Content model: param.
     */
    public static final int CM_PARAM = (1 << 12);

    /**
     * Content model: frames.
     */
    public static final int CM_FRAMES = (1 << 13);

    /**
     * Content model: heading.
     */
    public static final int CM_HEADING = (1 << 14);

    /**
     * Content model: opt.
     */
    public static final int CM_OPT = (1 << 15);

    /**
     * Content model: img.
     */
    public static final int CM_IMG = (1 << 16);

    /**
     * Content model: mixed.
     */
    public static final int CM_MIXED = (1 << 17);

    /**
     * Content model: no indent.
     */
    public static final int CM_NO_INDENT = (1 << 18);

    /**
     * Content model: obsolete.
     */
    public static final int CM_OBSOLETE = (1 << 19);

    /**
     * Content model: new.
     */
    public static final int CM_NEW = (1 << 20);

    /**
     * Content model: omitst.
     */
    public static final int CM_OMITST = (1 << 21);

    /**
     * Version: unknown.
     */
    public static final short VERS_UNKNOWN = 0;

    /**
     * Version: html 2.0.
     */
    public static final short VERS_HTML20 = 1;

    /**
     * Version: html 3.2.
     */
    public static final short VERS_HTML32 = 2;

    /**
     * Version: html 4.0 strict.
     */
    public static final short VERS_HTML40_STRICT = 4;

    /**
     * Version: html 4.0 transitional.
     */
    public static final short VERS_HTML40_LOOSE = 8;

    /**
     * Version: html 4.0 frameset.
     */
    public static final short VERS_FRAMESET = 16;

    /**
     * Version: xml.
     */
    public static final short VERS_XML = 32;

    /**
     * Version: netscape.
     */
    public static final short VERS_NETSCAPE = 64;

    /**
     * Version: microsoft.
     */
    public static final short VERS_MICROSOFT = 128;

    /**
     * Version: sun.
     */
    public static final short VERS_SUN = 256;

    /**
     * Version: malformed.
     */
    public static final short VERS_MALFORMED = 512;

    /**
     * Version: xhtml 1.1.
     */
    public static final short VERS_XHTML11 = 1024;

    /**
     * Version: xhtml basic.
     */
    public static final short VERS_BASIC = 2048;

    /**
     * All tags and attributes are ok in proprietary version of HTML.
     */
    public static final short VERS_PROPRIETARY =
        (VERS_NETSCAPE | VERS_MICROSOFT | VERS_SUN);

    /**
     * tags/attrs in HTML4 but not in earlier version.
     */
    public static final short VERS_HTML40 =
        (VERS_HTML40_STRICT | VERS_HTML40_LOOSE | VERS_FRAMESET);

    /**
     * tags/attrs which are in all versions of HTML except strict.
     */
    public static final short VERS_LOOSE =
        (VERS_HTML32 | VERS_HTML40_LOOSE | VERS_FRAMESET);

    /**
     * tags/attrs in HTML 4 loose and frameset.
     */
    public static final short VERS_IFRAME =
        (VERS_HTML40_LOOSE | VERS_FRAMESET);

    /**
     * tags/attrs in all versions from HTML 3.2 onwards.
     */
    public static final short VERS_FROM32 =
        (VERS_HTML40_STRICT | VERS_LOOSE);

    /**
     * versions with on... attributes.
     */
    public static final short VERS_EVENTS = (VERS_HTML40 | VERS_XHTML11);

    /**
     * tags/attrs in any version.
     */
    public static final short VERS_ALL =
        (VERS_HTML20 | VERS_HTML32 | VERS_HTML40 | VERS_XHTML11 | VERS_BASIC);

    /**
     * types of tags that the user can define: empty tag.
     */
    public static final short TAGTYPE_EMPTY = 1;

    /**
     * types of tags that the user can define: inline tag.
     */
    public static final short TAGTYPE_INLINE = 2;

    /**
     * types of tags that the user can define: block tag.
     */
    public static final short TAGTYPE_BLOCK = 4;

    /**
     * types of tags that the user can define: pre tag.
     */
    public static final short TAGTYPE_PRE = 8;

    /**
     * Tag name.
     */
    protected String name;

    /**
     * Version in which this tag is defined.
     */
    protected short versions;

    /**
     * model (CM_* constants).
     */
    protected int model;

    /**
     * Parser for this tag.
     */
    private Parser parser;

    /**
     * Validator for this tag.
     */
    private TagCheck chkattrs;

    /**
     * Instantiates a new Tag definition.
     * @param name tag name
     * @param versions version in which this tag is defined
     * @param model model (CM_* constants)
     * @param parser parser for this tag
     * @param chkattrs validator for this tag (can be null)
     */
    public Dict(String name, short versions, int model, Parser parser,
        TagCheck chkattrs)
    {
        this.name = name;
        this.versions = versions;
        this.model = model;
        this.parser = parser;
        this.chkattrs = chkattrs;
    }

    /**
     * Getter for <code>chkattrs</code>.
     * @return Returns the chkattrs.
     */
    public TagCheck getChkattrs()
    {
        return this.chkattrs;
    }

    /**
     * Getter for <code>model</code>.
     * @return Returns the model.
     */
    public int getModel()
    {
        return this.model;
    }

    /**
     * Getter for <code>name</code>.
     * @return Returns the name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Getter for <code>parser</code>.
     * @return Returns the parser.
     */
    public Parser getParser()
    {
        return this.parser;
    }

    /**
     * Setter for <code>chkattrs</code>.
     * @param chkattrs The chkattrs to set.
     */
    public void setChkattrs(TagCheck chkattrs)
    {
        this.chkattrs = chkattrs;
    }
    /**
     * Getter for <code>versions</code>.
     * @return Returns the versions.
     */
    public short getVersions()
    {
        return this.versions;
    }
    /**
     * Setter for <code>parser</code>.
     * @param parser The parser to set.
     */
    public void setParser(Parser parser)
    {
        this.parser = parser;
    }
}
