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

package com.globalsight.everest.tm.util;

import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.UTC;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents the TTX header of a Trados TTX file.
 */
public class Ttx
{
    //
    // TTX version constants
    //

    static public final String TTX_10 = "1.0";
    static public final String TTX_20 = "2.0";
    static public final String TTX_DTD_10 = "TODO";
    static public final String TTX_DTD_20 = "TODO";
    static public final int TTX_VERSION_10 = 100;
    static public final int TTX_VERSION_20 = 200;

    //
    // Constants for TTX element and attribute names.
    //
    static final public String ROOTELEMENT = "TRADOStag";
    static final public String VERSION = "Version";

    static final public String FRONTMATTER = "FrontMatter";

    static final public String TOOLSETTINGS = "ToolSettings";
    static final public String CREATIONDATE = "CreationDate";
    static final public String CREATIONTOOL = "CreationTool";
    static final public String CREATIONTOOLVERSION = "CreationToolVersion";

    static final public String USERSETTINGS = "UserSettings";
    static final public String DATATYPE = "DataType";
    static final public String O_ENCODING = "O-Encoding";
    static final public String SETTINGSNAME = "SettingsName";
    static final public String SETTINGSPATH = "SettingsPath";
    static final public String SOURCELANGUAGE = "SourceLanguage";
    static final public String TARGETLANGUAGE = "TargetLanguage";
    static final public String TARGETDEFAULTFONT = "TargetDefaultFont";
    static final public String SOURCEDOCUMENTPATH = "SourceDocumentPath";

    static final public String RAW = "Raw";
    static final public String DF = "df";
    static final public String UT = "ut";
    static final public String TU = "Tu";
    static final public String TUV = "Tuv";
    static final public String LANG = "Lang";

    //
    // Private members
    //

    // <TRADOStag Version>
    private String m_ttxVersion;

    // <FrontMatter> mandatory
    // <ToolSettings> mandatory
    private Date m_creationdate;
    private String m_creationtool;
    private String m_creationtoolversion;

    // <UserSettings> mandatory
    private String m_datatype;
    private String m_o_encoding;
    private String m_settingsname;
    private String m_settingspath;
    private String m_sourcelanguage;
    private String m_targetlanguage;
    private String m_targetdefaultfont;
    private String m_sourcedocumentpath;

    //
    // Constructor
    //
    public Ttx()
    {
    }

    public Ttx(Element p_header)
    {
        init(p_header);
    }

    //
    // Public Methods
    //
    public String getTtxVersion()
    {
        return m_ttxVersion;
    }

    public void setTtxVersion(String p_arg)
    {
        m_ttxVersion = p_arg;
    }

    // Arg must be in ISO 8601 format
    public void setCreationDate(String p_arg)
    {
        Date date = UTC.parseNoSeparators(p_arg);
        if (date == null)
        {
            date = UTC.parse(p_arg);
        }

        m_creationdate = date;
    }

    public void setCreationDate(Date p_arg)
    {
        m_creationdate = p_arg;
    }

    public Date getCreationDate()
    {
        return m_creationdate;
    }

    public void setCreationTool(String p_arg)
    {
        m_creationtool = p_arg;
    }

    public String getCreationTool()
    {
        return m_creationtool;
    }

    public void setCreationToolVersion(String p_arg)
    {
        m_creationtoolversion = p_arg;
    }

    public String getCreationToolVersion()
    {
        return m_creationtoolversion;
    }

    public void setDatatype(String p_arg)
    {
        m_datatype = p_arg;
    }

    public String getDatatype()
    {
        return m_datatype;
    }

    public void setOriginalEncoding(String p_arg)
    {
        m_o_encoding = p_arg;
    }

    public String getOriginalEncoding()
    {
        return m_o_encoding;
    }

    public void setSettingsName(String p_arg)
    {
        m_settingsname = p_arg;
    }

    public String getSettingsName()
    {
        return m_settingsname;
    }

    public void setSettingsPath(String p_arg)
    {
        m_settingspath = p_arg;
    }

    public String getSettingsPath()
    {
        return m_settingspath;
    }

    public void setSourceLanguage(String p_arg)
    {
        m_sourcelanguage = p_arg;
    }

    public String getSourceLanguage()
    {
        return m_sourcelanguage;
    }

    public void setTargetLanguage(String p_arg)
    {
        m_targetlanguage = p_arg;
    }

    public String getTargetLanguage()
    {
        return m_targetlanguage;
    }

    public String getTargetDefaultFont()
    {
        return m_targetdefaultfont;
    }

    public void setTargetDefaultFont(String p_arg)
    {
        m_targetdefaultfont = p_arg;
    }

    public String getSourceDocumentPath()
    {
        return m_sourcedocumentpath;
    }

    public void setSourceDocumentPath(String p_arg)
    {
        m_sourcedocumentpath = p_arg;
    }

    /** Returns the DTD devlaration &lt;!DOCTYPE ttx SYSTEM "ttx.dtd"&gt; */
    public String getTtxDeclaration()
    {
        /*
          return "<!DOCTYPE ttx SYSTEM \"" +
            getTtxDtdFromVersion(m_ttxVersion) + "\" >";
        */
        return "";
    }

    /** Returns the TTX start element &lt;TRADOStag version="2.0"&gt;. */
    public String getTtxXml()
    {
        return "<TRADOStag Version=\"" + m_ttxVersion + "\">";
    }

    /**
     * Returns the TTX header element as XML:
     * <FrontMatter [all attributes]></FrontMatter>
     */
    public String getHeaderXml()
    {
        StringBuffer result = new StringBuffer();

        result.append("<FrontMatter>\r\n");

        result.append("<ToolSettings ");
        if (m_creationdate != null)
        {
            result.append(CREATIONDATE);
            result.append("=\"");
            result.append(UTC.valueOfNoSeparators(m_creationdate));
            result.append("\" ");
        }
        if (m_creationtool != null)
        {
            result.append(CREATIONTOOL);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_creationtool));
            result.append("\" ");
        }
        if (m_creationtoolversion != null)
        {
            result.append(CREATIONTOOLVERSION);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_creationtoolversion));
            result.append("\" ");
        }

        result.append("/>\r\n");

        result.append("<UserSettings ");

        if (m_datatype != null)
        {
            result.append(DATATYPE);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_datatype));
            result.append("\" ");
        }
        if (m_o_encoding != null)
        {
            result.append(O_ENCODING);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_o_encoding));
            result.append("\" ");
        }
        if (m_settingsname != null)
        {
            result.append(SETTINGSNAME);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_settingsname));
            result.append("\" ");
        }
        if (m_settingspath != null)
        {
            result.append(SETTINGSPATH);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_settingspath));
            result.append("\" ");
        }
        if (m_sourcelanguage != null)
        {
            result.append(SOURCELANGUAGE);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_sourcelanguage));
            result.append("\" ");
        }
        if (m_targetlanguage != null)
        {
            result.append(TARGETLANGUAGE);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_targetlanguage));
            result.append("\" ");
        }
        if (m_targetdefaultfont != null)
        {
            result.append(TARGETDEFAULTFONT);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_targetdefaultfont));
            result.append("\" ");
        }
        if (m_sourcedocumentpath != null)
        {
            result.append(SOURCEDOCUMENTPATH);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_sourcedocumentpath));
            result.append("\" ");
        }

        result.append("/>\r\n");

        result.append("</FrontMatter>\r\n");

        return result.toString();
    }

    /**
     * Returns an internal version number (integer) based on the TMX DTD.
     */
    static public int getTtxDtdVersion(String p_dtd)
    {
        if (p_dtd.equals(TTX_DTD_10))
        {
            return TTX_VERSION_10;
        }
        else if (p_dtd.equals(TTX_DTD_20))
        {
            return TTX_VERSION_20;
        }

        return TTX_VERSION_20;
    }

    /**
     * Returns the DTD for the given TTX version string.
     */
    static public String getTtxDtdFromVersion(String p_version)
    {
        if (p_version.equals(TTX_10))
        {
            return TTX_DTD_10;
        }
        else if (p_version.equals(TTX_20))
        {
            return TTX_DTD_20;
        }

        return TTX_DTD_20;
    }

    //
    // Private Methods
    //
    static private boolean isSet(String s)
    {
        if (s != null && s.length() > 0)
        {
            return true;
        }

        return false;
    }

    private void init(Element p_element)
    {
        Element elem;
        Attribute attr;
        List nodes;
        Date date;

        elem = (Element)p_element.selectSingleNode("//ToolSettings");

        attr = elem.attribute(CREATIONDATE);
        if (attr == null)
        {
            date = null;
        }
        else
        {
            date = UTC.parseNoSeparators(attr.getValue());
            if (date == null)
            {
                date = UTC.parse(attr.getValue());
            }
        }
        m_creationdate = date;

        m_creationtool = elem.attributeValue(CREATIONTOOL);
        m_creationtoolversion = elem.attributeValue(CREATIONTOOLVERSION);

        elem = (Element)p_element.selectSingleNode("//UserSettings");

        m_datatype = elem.attributeValue(DATATYPE);
        m_o_encoding = elem.attributeValue(O_ENCODING);
        m_settingsname = elem.attributeValue(SETTINGSNAME);
        m_settingspath = elem.attributeValue(SETTINGSPATH);
        m_sourcelanguage = elem.attributeValue(SOURCELANGUAGE);
        m_targetlanguage = elem.attributeValue(TARGETLANGUAGE);
        m_targetdefaultfont = elem.attributeValue(TARGETDEFAULTFONT);
        m_sourcedocumentpath = elem.attributeValue(SOURCEDOCUMENTPATH);
    }
}

