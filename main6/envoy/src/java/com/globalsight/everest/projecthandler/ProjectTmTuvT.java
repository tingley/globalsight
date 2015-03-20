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

package com.globalsight.everest.projecthandler;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.tm.exporter.ExportUtil;
import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.ExactMatchFormatHandler;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.UTC;
import com.globalsight.util.XmlParser;
import com.globalsight.util.edit.EditUtil;

public class ProjectTmTuvT extends PersistentObject
{
    private static final long serialVersionUID = -4634594633148769874L;
    static private final Logger s_logger = Logger
    .getLogger(ProjectTmTuvT.class);

    private ProjectTmTuT tu;

    private String segmentString;
    private String segmentClob;
    private String exactMatchKey;
    private GlobalSightLocale locale;
    private Date creationDate;
    private String creationUser;
    private Date modifyDate;
    private String modifyUser;
    private String updatedByProject;
    private String sid;

    public ProjectTmTuT getTu()
    {
        return tu;
    }

    public void setTu(ProjectTmTuT tu)
    {
        this.tu = tu;
    }

    public String getSegmentString()
    {
        return segmentString == null? segmentClob : segmentString;
    }

    public void setSegmentString(String segmentString)
    {
        if (segmentString != null)
        {
            if (EditUtil.getUTF8Len(segmentString) > CLOB_THRESHOLD)
            {
                this.segmentClob = segmentString;
            }
            else
            {
                this.segmentString = segmentString;
            }

	        preSetExactMatchKey(segmentString);
        }
    }

    public String getSegmentClob()
    {
        return segmentClob;
    }

    public void setSegmentClob(String segmentClob)
    {
        if (segmentClob != null)
        {
            if (EditUtil.getUTF8Len(segmentClob) > CLOB_THRESHOLD)
            {
                this.segmentClob = segmentClob;
            }
            else
            {
                this.segmentString = segmentClob;
            }

			preSetExactMatchKey(segmentClob);
        }
    }
    
    private void preSetExactMatchKey(String segmentStringOrClob)
    {
        ExactMatchFormatHandler handler = new ExactMatchFormatHandler();
        DiplomatBasicParser diplomatParser = new DiplomatBasicParser(handler);

        try
        {
            diplomatParser.parse(segmentStringOrClob);
            setExactMatchKey(Long.toString(GlobalSightCrc.calculate(
                    handler.toString())));
        }
        catch (DiplomatBasicParserException e)
        {
            s_logger.error(e.getMessage(), e);
        }
    }

    public String getExactMatchKey()
    {
        return exactMatchKey;
    }

    public void setExactMatchKey(String exactMatchKey)
    {
        this.exactMatchKey = exactMatchKey;
    }

    public GlobalSightLocale getLocale()
    {
        return locale;
    }

    public void setLocale(GlobalSightLocale locale)
    {
        this.locale = locale;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    public String getCreationUser()
    {
        return creationUser;
    }

    public void setCreationUser(String creationUser)
    {
        this.creationUser = creationUser;
    }

    public Date getModifyDate()
    {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate)
    {
        this.modifyDate = modifyDate;
    }

    public String getUpdatedByProject()
    {
        return updatedByProject;
    }

    public void setUpdatedByProject(String updatedByProject)
    {
        this.updatedByProject = updatedByProject;
    }

    public String getModifyUser()
    {
        return modifyUser;
    }

    public void setModifyUser(String modifyUser)
    {
        this.modifyUser = modifyUser;
    }

    @Override
    public int hashCode()
    {
        long id = getId();
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        long id = getId();

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ProjectTmTuvT other = (ProjectTmTuvT) obj;
        if (id != other.getId())
            return false;
        return true;
    }

    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
		if (sid != null && sid.length() > 254)
			sid = sid.substring(0, 254);
        this.sid = sid;
    }

    public String convertToTmx()
    {
        StringBuffer result = new StringBuffer();
        String temp;
        Tmx.Prop prop;

        result.append("<tuv xml:lang=\"");
        result.append(ExportUtil.getLocaleString(getLocale()));
        result.append("\" ");

        if (getCreationDate() != null)
        {
            result.append(Tmx.CREATIONDATE);
            result.append("=\"");
            result.append(UTC.valueOfNoSeparators(getCreationDate()));
            result.append("\" ");
        }

        temp = getCreationUser();
        if (temp != null && temp.length() > 0)
        {
            result.append(Tmx.CREATIONID);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(temp));
            result.append("\" ");
        }

        if (getModifyDate() != null)
        {
            if (getCreationDate() != null
                    && !getCreationDate().equals(getModifyDate()))
            {
                result.append(Tmx.CHANGEDATE);
                result.append("=\"");
                result.append(UTC.valueOfNoSeparators(getModifyDate()));
                result.append("\" ");
            }

        }

        temp = getModifyUser();
        if (temp != null && temp.length() > 0)
        {
            result.append(Tmx.CHANGEID);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(temp));
            result.append("\" ");
        }

        result.append(">\r\n");

        // Property for TUV's update project.
        temp = getUpdatedByProject();
        if (temp != null && temp.length() > 0)
        {
            prop = new Tmx.Prop(Tmx.PROP_CREATION_PROJECT, temp);
            result.append(prop.asXML());
        }

        result.append(convertToTmx(getSegmentString()));
        result.append("</tuv>\r\n");

        return result.toString();
    }

    private void findHiElements(ArrayList p_result, Element p_element)
    {
        // Depth-first traversal: add embedded <hi> to the list first.
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node) p_element.node(i);

            if (child instanceof Element)
            {
                findHiElements(p_result, (Element) child);
            }
        }

        if (p_element.getName().equals("hi"))
        {
            p_result.add(p_element);
        }
    }

    /**
     * Removes the given TMX 1.4 <hi> element from the segment. <hi> is special
     * since it does not surround embedded tags but text, which must be pulled
     * out of the <hi> and added to the parent segment.
     */
    private void removeHiElement(Element p_element)
    {
        Element parent = p_element.getParent();
        int index = parent.indexOf(p_element);

        // We copy the current content, clear out the parent, and then
        // re-add the old content, inserting the <hi>'s content
        // instead of the <hi>.

        ArrayList newContent = new ArrayList();
        List content = parent.content();

        for (int i = content.size() - 1; i >= 0; --i)
        {
            Node node = (Node) content.get(i);

            newContent.add(node.detach());
        }

        Collections.reverse(newContent);
        parent.clearContent();

        for (int i = 0, max = newContent.size(); i < max; ++i)
        {
            Node node = (Node) newContent.get(i);

            if (i == index)
            {
                parent.appendContent(p_element);
            }
            else
            {
                parent.add(node);
            }
        }
    }

    /**
     * Removes all TMX 1.4 <hi> elements from the segment. <hi> is special since
     * it does not surround embedded tags but text, which must be pulled out of
     * the <hi> and added to the parent segment.
     */
    private Element removeHiElements(Element p_seg)
    {
        ArrayList elems = new ArrayList();

        findHiElements(elems, p_seg);

        for (int i = 0; i < elems.size(); i++)
        {
            Element hi = (Element) elems.get(i);

            removeHiElement(hi);
        }

        return p_seg;
    }

    /**
     * Reads the segment content from the <seg> element and fixes any missing
     * sub locType attributes and sub id values.
     * 
     * @param p_root
     *            the TUV node in the DOM structure.
     * @return the segment text or XML value, encoded as XML.
     */
    private String getSegmentValue(Element p_root)
    {
        StringBuffer result = new StringBuffer();
        Element seg = p_root.element("seg");
        seg = removeHiElements(seg);
        result.append(EditUtil.encodeXmlEntities(seg.getText()));
        return result.toString();
    }

    public void reconvertFromTmx(Element p_root, ProjectTmTuT tu)
            throws Exception
    {
        // language of the TUV "EN-US", case insensitive
        String lang = p_root.attributeValue(Tmx.LANG);

        String locale = ImportUtil.normalizeLocale(lang);
        LocaleManagerLocal manager = new LocaleManagerLocal();
        setLocale(manager.getLocaleByString(locale));

        // Creation user - always set to a known value
        String user = p_root.attributeValue(Tmx.CREATIONID);
        if (user == null)
        {
            user = p_root.getParent().attributeValue(Tmx.CREATIONID);
        }
        setCreationUser(user != null ? user : Tmx.DEFAULT_USER);

        // Modification user - only set if known
        user = p_root.attributeValue(Tmx.CHANGEID);
        if (user == null)
        {
            user = p_root.getParent().attributeValue(Tmx.CHANGEID);
        }
        if (user != null)
        {
            setModifyUser(user);
        }

        Date now = new Date();
        Date date;

        String ts = p_root.attributeValue(Tmx.CREATIONDATE);
        if (ts == null)
        {
            ts = p_root.getParent().attributeValue(Tmx.CREATIONDATE);
        }

        if (ts != null)
        {
            date = UTC.parseNoSeparators(ts);
            if (date == null)
            {
                date = UTC.parse(ts);
            }

            setCreationDate(date);
        }
        else
        {
            setCreationDate(now);
        }

        ts = p_root.attributeValue(Tmx.CHANGEDATE);
        if (ts == null)
        {
            ts = p_root.getParent().attributeValue(Tmx.CHANGEDATE);
        }

        if (ts != null)
        {
            date = UTC.parseNoSeparators(ts);
            if (date == null)
            {
                date = UTC.parse(ts);
            }

            setModifyDate(date);
        }

        StringBuffer segment = new StringBuffer();
        segment.append("<segment>");
        segment.append(getSegmentValue(p_root));
        segment.append("</segment>");
        setSegmentString(segment.toString());
        setSid(tu.getSid());
    }

    /**
     * Convert a segment string to TMX by removing <sub> elements.
     * 
     * TODO: output sub information as <prop>.
     */
    private String convertToTmx(String p_segment)
    {
        StringBuffer result = new StringBuffer();
        Document dom = getDom(p_segment);
        result.append("<seg>");
        result.append(getInnerXml(dom.getRootElement()));
        result.append("</seg>\r\n");

        return result.toString();
    }

    /**
     * Converts an XML string to a DOM document.
     */
    private Document getDom(String p_xml)
    {
        XmlParser parser = null;

        try
        {
            parser = XmlParser.hire();
            return parser.parseXml(p_xml);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("invalid GXML `" + p_xml + "': "
                    + ex.getMessage());
        }
        finally
        {
            XmlParser.fire(parser);
        }
    }

    /**
     * Returns the XML representation like Element.asXML() but without the
     * top-level tag.
     */
    @SuppressWarnings("unchecked")
    private String getInnerXml(Element p_node)
    {
        StringBuffer result = new StringBuffer();
        List<Node> content = p_node.content();

        for (Node node : content)
        {
            if (node.getNodeType() == Node.TEXT_NODE)
            {
                result.append(EditUtil.encodeXmlEntities(node.getText()));
            }
            else
            {
                StringWriter out = new StringWriter();
                result.append(out.toString());
            }
        }

        return result.toString();
    }
    
    public void merge(ProjectTmTuvT tuv)
    {
        this.setCreationUser(tuv.getCreationUser());
        this.setModifyDate(new Date());
        this.setModifyUser(tuv.getModifyUser());
        this.setSegmentString(tuv.getSegmentString());
        this.setUpdatedByProject(tuv.getUpdatedByProject());
        this.setSid(tuv.getSid());
    }
}
