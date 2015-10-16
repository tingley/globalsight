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
package com.globalsight.util.gxml;

//import globalsignt classes
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.tm2.TmxTypeMapper;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.EmojiUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.exception.BooleanConvertingException;

/**
 * <p>
 * In GXML or Paginated ResultSet, each XML tag pairs and the content in between
 * are taken as a single Gxml element. GxmlElement class is generalized the
 * features of all element types. Text content of the element is stored in a
 * TextNode.
 * </p>
 */
public class GxmlElement implements Serializable
{
    private static final long serialVersionUID = -7065428615407672282L;

    private static final Logger CATEGORY = Logger.getLogger(GxmlElement.class
            .getName());

    // Element types
    public static final int NONE = -1;
    public static final int UNSPECIFIED = 0;
    public static final int DIPLOMAT = 1;
    public static final int TRANSLATABLE = 2;
    public static final int LOCALIZABLE = 3;
    public static final int SKELETON = 4;
    public static final int SEGMENT = 5;
    public static final int GXML_ROOT = 6;
    public static final int PRS_ROOT = 7;
    public static final int RECORD = 8;
    public static final int COLUMN = 9;
    public static final int LABEL = 10;
    public static final int ACQSQLPARM = 11;
    public static final int CONTENT = 12;
    public static final int BPT = 13;
    public static final int EPT = 14;
    public static final int PH = 15;
    public static final int IT = 16;
    public static final int UT = 17;
    public static final int SUB = 18;
    public static final int COLUMN_HEADER = 19;
    public static final int CONTEXT = 20;
    public static final int ROW = 21;
    // a text node, not actually an element
    public static final int TEXT_NODE = 22;
    public static final int GS = 23;

    public static final int[] TRANSLATABLE_AND_LOCALIZABLE_TYPES = new int[]
    { GxmlElement.TRANSLATABLE, GxmlElement.LOCALIZABLE };
    public static final int[] SUB_TYPE = new int[]
    { GxmlElement.SUB };
    public static final int[] TEXT_NODE_TYPE = new int[]
    { GxmlElement.TEXT_NODE };
    public static final int[] USPECIFIED_TYPE = new int[]
    { GxmlElement.UNSPECIFIED };

    /** element type */
    protected int m_type = NONE;

    /** element name, the tag name appearing in the XML document */
    protected String m_name = null;

    /** list of child elements of type GxmlElement */
    protected List m_childElements = null;

    /** store the attributes in a HashMap */
    private LinkedHashMap m_attributes = null;
    private String m_attributesAsString = null;

    /** parent element */
    private GxmlElement m_parent = null;

    private static final int STRING_BUFFER_LENGTH = 200;

    /**
     * Construct a GxmlElement with the given type and name.
     */
    GxmlElement(int p_type, String p_name)
    {
        m_type = p_type;
        m_name = p_name;
    }

    /**
     * Shallow copy constructor.
     * 
     * @param p_gxmlElement
     *            construct a GxmlElement from p_gxmlElement
     */
    public GxmlElement(GxmlElement p_gxmlElement)
    {
        m_type = p_gxmlElement.m_type;
        m_name = p_gxmlElement.m_name;
        m_parent = p_gxmlElement.m_parent;

        if (p_gxmlElement.m_childElements != null)
        {
            m_childElements = new ArrayList(p_gxmlElement.m_childElements);
        }

        if (p_gxmlElement.m_attributes != null)
        {
            m_attributes = new LinkedHashMap(p_gxmlElement.m_attributes);
        }
    }

    /**
     * Construct an empty GxmlElement
     */
    private GxmlElement()
    {
    }

    /**
     * Get the Translatable and Localizable elements from this document
     * structure. The results are a List and it keeps the same order as the
     * elements appear in the document.
     *
     * @return a List of elements of translatable or localizable
     */
    public List getTranslatableAndLocalizable()
    {
        return getChildElements(TRANSLATABLE_AND_LOCALIZABLE_TYPES);
    }

    /**
     * To get the element type
     * 
     * @return the type of the element.
     */
    public int getType()
    {
        return m_type;
    }

    /**
     * To get the element name, the name is the tag name in Gxml document.
     * 
     * @return name of the elment as a String.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Get the text value of the element. The text value is defined as the the
     * content of the TextNode, if it is a TextNode, and if it is not a
     * TextNode, it returns the summation of the content of the immediate
     * children TextNodes
     *
     * @return the text content of the element as a String. Returns an empty
     *         String if the GxmlElement is not a TextNode or it has no
     *         immediate child TextNodes.
     */
    public String getTextValue()
    {
        List children = getChildElements(TEXT_NODE);

        if (children == null)
        {
            return "";
        }

        StringBuffer result = new StringBuffer(10 * children.size());

        for (int i = 0; i < children.size(); i++)
        {
            result.append(((TextNode) children.get(i)).getTextNodeValue());
        }

        return result.toString();
    }

    /**
     * Get the text value of a TextNode element. The TextNode value is defined
     * as the content of a TextNode. If the GxmlElement is not a TextNode, an
     * empty String is returned.
     *
     * @return the content of a TextNode GxmlElement as a String. Returns an
     *         empty String if it is not a TextNode.
     */
    public String getTextNodeValue()
    {
        // Overriden in TextNode, so this is not a TextNode.
        return "";
    }

    /**
     * Get the total text value of the element. The total text value is defined
     * as the the content of the TextNode, if it is a TextNode, and if it is not
     * a TextNode, it returns the summation of the content of all the descendant
     * TextNodes. Returns an empty String if there are no TextNodes.
     *
     * @return the content of the element as a String.
     */
    public String getTotalTextValue()
    {
        List descendants = getDescendantElements(TEXT_NODE_TYPE);

        if (descendants == null)
        {
            return "";
        }

        StringBuffer result = new StringBuffer(10 * descendants.size());

        for (int i = 0; i < descendants.size(); i++)
        {
            // GBS-3997&GBS-4066
            TextNode tn = (TextNode) descendants.get(i);
            GxmlElement parent = tn.getParent();
            if (parent != null)
            {
                int nodeType = parent.getType();
                if (PH == nodeType)
                {
                    String type = parent.getAttribute(GxmlNames.PH_TYPE);
                    if (type != null && type.startsWith(EmojiUtil.TYPE_EMOJI))
                    {
                        continue;
                    }
                }
            }
            result.append(((TextNode) descendants.get(i)).getTextNodeValue());
        }

        return result.toString();
    }

    /**
     * To set an attribute value by name.
     */
    public void setAttribute(String p_attributeName, String p_value)
    {
        // Initialize the hashmap for the first time
        if (m_attributes == null)
        {
            m_attributes = new LinkedHashMap(7, 1);
        }

        m_attributes.put(p_attributeName, p_value);
        m_attributesAsString = null;
    }

    /**
     * To get an attribute value by its name.
     *
     * @return an attribute value as a String, return null if the attribute is
     *         not set.
     */
    public String getAttribute(String p_attributeName)
    {
        if (m_attributes == null)
        {
            return null;
        }

        return (String) m_attributes.get(p_attributeName);
    }

    /**
     * To get an attribute value as Boolean type by its name.
     *
     * @return an attribute value as a boolean, return null if the attribute is
     *         not set.
     *
     * @exception BooleanConvertingException
     *                , a globalsight Exception, thrown when the attribute is
     *                not a boolean value e.g. true or false
     */
    public Boolean getAttributeAsBoolean(String p_attributeName)
            throws BooleanConvertingException
    {
        if (m_attributes == null)
        {
            return null;
        }

        Boolean output = null;
        String attr = (String) m_attributes.get(p_attributeName);

        if (attr != null)
        {
            if ("true".equals(attr.toLowerCase())
                    || "false".equals(attr.toLowerCase()))
            {
                output = new Boolean(attr);
            }
            else
            {
                throw new BooleanConvertingException();
            }
        }

        return output;
    }

    /**
     * To get an attribute value as Integer type by its name.
     *
     * @return an attribute value as an Integer, return null if the attribute is
     *         not set.
     *
     * @exception NumberFormatException
     *                , thrown when the attribute value is not a number e.g.
     *                true or false
     */
    public Integer getAttributeAsInteger(String p_attributeName)
            throws NumberFormatException
    {
        if (m_attributes == null)
        {
            return null;
        }

        Integer output = null;
        String attr = (String) m_attributes.get(p_attributeName);

        if (attr != null)
        {
            output = new Integer(attr);
        }

        return output;
    }

    /**
     * To add a child Element to the existing child element list
     */
    void addChildElement(GxmlElement p_childElement)
    {
        if (m_childElements == null)
        {
            m_childElements = new ArrayList(1);
        }

        m_childElements.add(p_childElement);
    }

    /**
     * To set the child element list
     */
    void setChildElements(List p_childElements)
    {
        m_childElements = p_childElements;
    }

    /**
     * To get the whole child element list.
     * 
     * @return List of all child elements.
     */
    public List getChildElements()
    {
        if (m_childElements == null)
        {
            return new ArrayList(0);
        }

        return m_childElements;
    }

    /**
     * Return all TextNode that is not in internal text (internal="yes"), this
     * does not include sub elements.
     * 
     * @return List<GxmlElement>.
     */
    public List<GxmlElement> getTextNodeWithoutInternal()
    {
        if (m_childElements == null)
        {
            return new ArrayList<GxmlElement>(0);
        }

        List<GxmlElement> someChildElmts = new ArrayList<GxmlElement>(
                m_childElements.size());

        boolean inInternal = false;
        String bptI = "-1";
        for (int i = 0; i < m_childElements.size(); i++)
        {
            GxmlElement child = (GxmlElement) m_childElements.get(i);
            if (!inInternal && BPT == child.getType())
            {
                inInternal = "yes".equals(child.getAttribute("internal"));

                if (inInternal)
                {
                    bptI = child.getAttribute("i");
                }
            }

            if (!inInternal && TEXT_NODE == child.getType())
            {
                someChildElmts.add(child);
            }

            if (inInternal && EPT == child.getType())
            {
                String eptI = child.getAttribute("i");
                if (bptI != null && bptI.equals(eptI))
                {
                    inInternal = false;
                }
            }
        }

        return someChildElmts;
    }

    /**
     * To get a list of immediate child elements of specified types.
     * 
     * @param p_types
     *            - an array of element types
     * @return a List of child elements having the specified element types
     */
    public List getChildElements(int[] p_types)
    {
        if (m_childElements == null)
        {
            return new ArrayList(0);
        }

        List someChildElmts = new ArrayList(m_childElements.size());

        for (int i = 0; i < m_childElements.size(); i++)
        {
            GxmlElement child = (GxmlElement) m_childElements.get(i);

            if (isNumberInArray(child.getType(), p_types))
            {
                someChildElmts.add(child);
            }
        }

        return someChildElmts;
    }

    /**
     * To get a list of immediate child elements of a specified type.
     * 
     * @param p_type
     *            - an element type
     * @return a List of child elements having the specified element type
     */
    public List getChildElements(int p_type)
    {
        int[] types = new int[1];
        types[0] = p_type;
        return getChildElements(types);
    }

    /**
     * To get an element by index.
     *
     * @return a child GxmlElement, return null if the index is out off range,
     *         or if there is no child elements existing.
     */
    public GxmlElement getChildElement(int p_index)
    {
        if (p_index == -1 || m_childElements == null
                || p_index >= m_childElements.size())
        {
            return null;
        }

        return (GxmlElement) m_childElements.get(p_index);
    }

    /**
     * To get a list of descendant elements of specified types.
     * 
     * @param p_types
     *            - an array of element types
     * @return a List of descendant elements having the specified element types
     */
    public List<GxmlElement> getDescendantElements(int[] p_types,
            boolean... fromPage)
    {
        if (m_childElements == null)
        {
            return new ArrayList<GxmlElement>(0);
        }

        // First get the immediate children of the specified types
        List<GxmlElement> descendants = new ArrayList<GxmlElement>(
                m_childElements.size());

        for (int i = 0; i < m_childElements.size(); i++)
        {
            GxmlElement child = (GxmlElement) m_childElements.get(i);
            if (fromPage != null && fromPage.length != 0 && fromPage[0])
            {
                if (!child.isSkip())
                {
                    if (isNumberInArray(child.getType(), p_types))
                    {
                        descendants.add(child);
                    }
                }
                // In a tree traversal, for each child, getDescendantElements
                descendants.addAll(child.getDescendantElements(p_types, true));
            }
            else
            {
                if (isNumberInArray(child.getType(), p_types))
                {
                    descendants.add(child);
                }
                // In a tree traversal, for each child, getDescendantElements
                descendants.addAll(child.getDescendantElements(p_types));
            }
        }

        return descendants;
    }

    /**
     * For each descendant element of one of the specified types, add it's
     * parent to the returned List.
     * 
     * @param p_types
     *            - an array of element types
     * @return a List of parents of descendant elements.
     */
    public List getDescendantElementsParent(int[] p_types)
    {
        List descendantElements = getDescendantElements(p_types);
        Collection descendantParents = new HashSet(descendantElements.size());

        Iterator it = descendantElements.iterator();
        while (it.hasNext())
        {
            GxmlElement descendant = (GxmlElement) it.next();

            descendantParents.add(descendant.getParent());
        }

        return new ArrayList(descendantParents);
    }

    /**
     * To get the parent GxmlElement. May be null if no parent.
     * 
     * @return the parent GxmlElement.
     */
    public GxmlElement getParent()
    {
        return m_parent;
    }

    /**
     * Set the parent element.
     * 
     * @param p_parent
     *            the parent GxmlElement.
     */
    void setParent(GxmlElement p_parent)
    {
        m_parent = p_parent;
    }

    /**
     * Gets the end tag of this element. If the tag is empty (i.e. has no child
     * nodes) the start tag is printed as empty &lt;.../&gt; and this method
     * returns the empty string.
     *
     * @return element end tag as a String.
     */
    public String getEndTag()
    {
        if (m_childElements != null && m_childElements.size() > 0)
        {
            return "</" + getName() + ">";
        }

        return "";
    }

    /**
     * Gets the start tag of this element. The start tag includes all the
     * attributes defined. If the tag is empty (i.e. has no child nodes) the
     * start tag is printed as empty &lt;.../&gt; and getEndTag() returns the
     * empty string.
     *
     * @return element start tag as String.
     */
    public String getStartTag(boolean isXliff)
    {
        StringBuffer output = new StringBuffer(
                GxmlSaxHelper.START_TAG_STRING_BUFFER_LENGTH);

        output.append("<");
        output.append(getName());
        output.append(attributesToGxml(isXliff));

        if (m_childElements == null || m_childElements.size() == 0)
        {
            output.append('/');
        }

        output.append(">");

        return output.toString();
    }

    public String getStartTag()
    {
        return getStartTag(false);
    }

    /**
     * Prints this element out as XML including all its child elements, placing
     * new lines before start-tags.
     * 
     * @return the element as a String
     */
    public String toLines()
    {
        return toGxml(GlobalSightCategory.getLineContinuation(), false, false);
    }

    /**
     * A String representation of the object.
     * 
     * @return a String representation of the object.
     */
    public String toString()
    {
        String children = "null";

        if (m_childElements != null)
        {
            children = GlobalSightCategory.getLineContinuation();

            Iterator it = m_childElements.iterator();
            while (it.hasNext())
            {
                GxmlElement gxmlElement = (GxmlElement) it.next();

                children += gxmlElement.toString()
                        + GlobalSightCategory.getLineContinuation();
            }
        }

        return "m_name=" + (m_name != null ? m_name : "null") + " m_type="
                + Integer.toString(m_type) + " m_parent="
                + (m_parent != null ? m_parent.getName() : "null")
                + " m_attributes="
                + (m_attributes != null ? m_attributes.toString() : "null")
                + " children={" + children + "} " + " m_name="
                + (m_name != null ? m_name : "null");
    }

    /**
     * Reverse this element to XML content, including converting all its child
     * elements.
     * 
     * @return the element as a Gxml String
     */
    public String toGxml()
    {
        return toGxml("", false, false);
    }

    public String toGxml(String dataType)
    {
        if (dataType != null && "javascript".equalsIgnoreCase(dataType))
        {
            return toGxml("", false, true);
        }
        else if (IFormatNames.FORMAT_XLIFF.equalsIgnoreCase(dataType)
                || IFormatNames.FORMAT_XLIFF20.equalsIgnoreCase(dataType))
        {
            return toGxml("", false, false, true);
        }
        else
        {
            return toGxml("", false, false);
        }
    }

    protected String toGxml(String p_startTagDelimiter,
            boolean p_excludeTopTags, boolean p_handleNRT)
    {
        return toGxml(p_startTagDelimiter, p_excludeTopTags, p_handleNRT, false);
    }

    /**
     * Reverse this element to XML content, including converting all its child
     * elements.
     * 
     * @param p_startTagDelimiter
     *            placed before start-tag
     * @return the element as a Gxml String
     */
    protected String toGxml(String p_startTagDelimiter,
            boolean p_excludeTopTags, boolean p_handleNRT, boolean isXlf)
    {
        StringBuffer result = null;

        if (p_excludeTopTags)
        {
            result = new StringBuffer(
                    (m_childElements != null ? m_childElements.size()
                            * STRING_BUFFER_LENGTH : 0));
        }
        else
        {
            result = new StringBuffer(
                    GxmlSaxHelper.START_TAG_STRING_BUFFER_LENGTH
                            + (m_childElements != null ? m_childElements.size()
                                    * STRING_BUFFER_LENGTH : 0)
                            + GxmlSaxHelper.END_TAG_STRING_BUFFER_LENGTH);
        }

        result.append(p_startTagDelimiter);

        if (!p_excludeTopTags)
        {
            result.append(getStartTag(isXlf));
        }

        // print childElements if there are any
        if (m_childElements != null)
        {
            for (int i = 0; i < m_childElements.size(); i++)
            {
                GxmlElement child = (GxmlElement) m_childElements.get(i);
                /*
                 * if (isXlf && child instanceof TextNode) { String str =
                 * child.getTextNodeValue(); str = str.replaceAll("&amp;",
                 * "&amp;amp;"); str = str.replaceAll("&quot;", "&amp;quot;");
                 * str = str.replaceAll("&apos;", "&amp;apos;"); str =
                 * str.replaceAll("&#xd;", "&amp;#xd;"); str =
                 * str.replaceAll("&#x9;", "&amp;#x9;"); str =
                 * str.replaceAll("&#xa;", "&amp;#xa;"); //str =
                 * str.replaceAll("<", "&lt;"); //str = str.replaceAll(">",
                 * "&gt;"); result.append(str); } else {
                 */result.append(child.toGxml(p_startTagDelimiter, false,
                        p_handleNRT, isXlf));
                // }
            }
        }

        if (!p_excludeTopTags)
        {
            result.append(getEndTag());
        }

        return result.toString();
    }

    /**
     * Reverse this element to XML content, including converting all its child
     * elements, excluding the top element start-tag and end-tag.
     * 
     * @return the element as a Gxml String excluding the top element start-tag
     *         and end-tag.
     */
    public String toGxmlExcludeTopTags()
    {
        return toGxml("", true, false);
    }

    /**
     * @returns the attributes in Gxml string format
     */
    public String attributesToGxml(boolean isXliff)
    {
        if (m_attributesAsString == null)
        {
            if (m_attributes == null)
            {
                return "";
            }

            StringBuffer output = new StringBuffer(
                    GxmlSaxHelper.START_TAG_STRING_BUFFER_LENGTH);

            Set keys = m_attributes.keySet();

            for (Iterator it = keys.iterator(); it.hasNext();)
            {
                String attrName = (String) it.next();
                String attrValue = (String) m_attributes.get(attrName);

                if (isXliff)
                {
                    attrValue = EditUtil.encodeXmlEntities(attrValue);
                }

                if (attrValue != null)
                {
                    output.append(" ");
                    output.append(attrName);
                    output.append("=\"");
                    output.append(attrValue);
                    output.append("\"");
                }
            }

            m_attributesAsString = output.toString();
        }

        return m_attributesAsString;
    }

    public String attributesToGxml()
    {
        return attributesToGxml(false);
    }

    /**
     * Returns the nearest descendant that has the specified value of the
     * attribute. It could be this element. All elements must belong to the same
     * tree structure of elements.
     * 
     * @param p_attributeName
     *            attribute name
     * @param p_attributeValue
     *            attribute value
     * @param GxmlElement
     *            type
     * @return nearest descendant that has the specified value of the attribute,
     *         or null if there is none.
     */
    public GxmlElement getDescendantByAttributeValue(String p_attributeName,
            String p_attributeValue, int p_type)
    {
        return getNthDescendantByAttributeValue(p_attributeName,
                p_attributeValue, p_type, 1);
    }

    /**
     * Returns the N-th descendant that has the specified value of the
     * attribute. It could be this element. All elements must belong to the same
     * tree structure of elements.
     * 
     * @param p_attributeName
     *            attribute name
     * @param p_attributeValue
     *            attribute value
     * @param p_type
     *            GxmlElement type
     * @param p_position
     *            position of the element. The position starts from 1. If you
     *            want to get the nearest descendant, specify 1 here. If 0 is
     *            specified, null is returned.
     * @return N-th descendant that has the specified value of the attribute, or
     *         null if there is none.
     */
    public GxmlElement getNthDescendantByAttributeValue(String p_attributeName,
            String p_attributeValue, int p_type, int p_position)
    {
        List possibleReturns = getAllDescendantByAttributeValue(
                p_attributeName, p_attributeValue, p_type);

        p_position = p_position - 1;
        if (p_position < 0 || p_position >= possibleReturns.size())
        {
            return null;
        }

        // order the descendants by nearness to this element. return
        // first one
        return (GxmlElement) orderByNearestDescendant(possibleReturns).get(
                p_position);
    }

    /**
     * Returns the N-th descendant that has non specified attribute. It could be
     * this element. All elements must belong to the same tree structure of
     * elements.
     */
    public GxmlElement getNthDescendantByAttributeNone(String p_attributeName,
            int p_type, int p_position)
    {
        List possibleReturns = getAllDescendantByAttributeNone(p_attributeName,
                p_type);

        p_position = p_position - 1;
        if (p_position < 0 || p_position >= possibleReturns.size())
        {
            return null;
        }

        // order the descendants by nearness to this element. return
        // first one
        return (GxmlElement) orderByNearestDescendant(possibleReturns).get(
                p_position);
    }

    /**
     * Returns all descendants that have the specified value of the attribute.
     * It could contain this element. All elements must belong to the same tree
     * structure of elements.
     * 
     * @param p_attributeName
     *            attribute name
     * @param p_attributeValue
     *            attribute value
     * @param GxmlElement
     *            type
     * @return A List of all descendants (GxmlElement) that have the specified
     *         value of the attribute. If none is found, an empty List is
     *         returned.
     */
    public List getAllDescendantByAttributeValue(String p_attributeName,
            String p_attributeValue, int p_type)
    {
        List allElements = new ArrayList();

        // test self
        GxmlElement gxmlElement = this;
        if (gxmlElement.getType() == p_type)
        {
            String attributeValue = gxmlElement.getAttribute(p_attributeName);

            if (attributeValue != null
                    && attributeValue.equals(p_attributeValue))
            {
                allElements.add(gxmlElement);
            }
            else if ("none".equals(p_attributeValue)
                    && "type".equals(p_attributeName))
            {
                allElements.add(gxmlElement);
            }
        }

        // test all children
        List children = gxmlElement.getChildElements();
        if (children != null && !children.isEmpty())
        {
            Iterator it = children.iterator();

            // get all descendants that have the attribute value
            while (it.hasNext())
            {
                Object child = it.next();
                if (child == null)
                {
                    continue;
                }

                List elements = ((GxmlElement) child)
                        .getAllDescendantByAttributeValue(p_attributeName,
                                p_attributeValue, p_type);

                allElements.addAll(elements);
            }
        }

        return allElements;
    }

    /**
     * Returns all elements that has non specified attribute.
     */
    public List getAllDescendantByAttributeNone(String p_attributeName,
            int p_type)
    {
        List allElements = new ArrayList();

        // test self
        GxmlElement gxmlElement = this;
        if (gxmlElement.getType() == p_type)
        {
            String attributeValue = gxmlElement.getAttribute(p_attributeName);

            if (attributeValue == null)
            {
                allElements.add(gxmlElement);
            }
        }

        // test all children
        List children = gxmlElement.getChildElements();
        if (children != null && !children.isEmpty())
        {
            Iterator it = children.iterator();

            // get all descendants that have the attribute value
            while (it.hasNext())
            {
                Object child = it.next();
                if (child == null)
                {
                    continue;
                }

                List elements = ((GxmlElement) child)
                        .getAllDescendantByAttributeNone(p_attributeName,
                                p_type);

                allElements.addAll(elements);
            }
        }

        return allElements;
    }

    /**
     * Order the list of GxmlElements so the nearest descendants to this element
     * come first. All elements must belong to the same tree structure of
     * elements.
     * 
     * @param p_gxmlElements
     *            list of GxmlElements that are descendants of this one.
     * @return list of GxmlElements ordered by nearness to this one.
     */
    public List orderByNearestDescendant(List p_gxmlElements)
    {
        ArrayList returnList = new ArrayList(p_gxmlElements.size());

        if (p_gxmlElements.isEmpty())
        {
            return returnList;
        }

        ArrayList tempList = new ArrayList(p_gxmlElements);
        List children = getChildElements();

        if (children == null || children.isEmpty())
        {
            return returnList;
        }

        Iterator childrenIt = children.iterator();
        while (childrenIt.hasNext())
        {
            Object o = childrenIt.next();
            if (o == null)
            {
                continue;
            }

            GxmlElement child = (GxmlElement) o;
            Iterator it = p_gxmlElements.iterator();

            while (it.hasNext())
            {
                Object o2 = it.next();
                if (o2 == null)
                {
                    continue;
                }

                GxmlElement gxmlElement = (GxmlElement) o2;

                if (child.equals(gxmlElement))
                {
                    // add it to the end
                    returnList.add(gxmlElement);
                    tempList.remove(gxmlElement);
                }
            }
        }

        childrenIt = children.iterator();
        while (childrenIt.hasNext())
        {
            Object o = childrenIt.next();
            if (o == null)
            {
                continue;
            }

            GxmlElement child = (GxmlElement) o;

            // order the remaining descendants by nearness to each child
            returnList.addAll(child.orderByNearestDescendant(tempList));
        }

        return returnList;
    }

    /**
     * Order the List of GxmlElements so the parents come before their children.
     * All elements must belong to the same tree structure of elements.
     * 
     * @param p_gxmlElements
     *            GxmlElements to order
     * @return ordered List of GxmlElements with parents before their children.
     */
    public static List orderByParent(Collection p_gxmlElements)
    {
        ArrayList result = new ArrayList(p_gxmlElements.size());
        ArrayList temp = new ArrayList(p_gxmlElements);

        while (!temp.isEmpty())
        {
            Iterator it = temp.iterator();
            Object o = it.next();
            if (o == null)
            {
                temp.remove(o);
                continue;
            }

            GxmlElement gxmlElement = (GxmlElement) o;
            GxmlElement highestAncestor = getHighestAncestor(gxmlElement, temp);

            // if it is not descended from another element in the
            // array it can go first
            if (highestAncestor == null)
            {
                result.add(gxmlElement);
                temp.remove(gxmlElement);
                continue;
            }

            // if it is descended from another element in the array,
            // place his highest ancestor first
            result.add(0, highestAncestor);
            temp.remove(highestAncestor);
        }

        return result;
    }

    /**
     * Returns the highest root element of this document, which can be a
     * GxmlRootElement or a PrsRootElement.
     */
    public static GxmlElement getRootElement(GxmlElement p_element)
    {
        while (p_element.getParent() != null)
        {
            p_element = p_element.getParent();
        }

        return p_element;
    }

    /**
     * Returns the highest GxmlRootElement in the document, regardless of the
     * document being a Gxml document or a Prs with an embedded Gxml document.
     */
    public static GxmlElement getGxmlRootElement(GxmlElement p_element)
    {
        while (p_element.getType() != GXML_ROOT
                && p_element.getParent() != null)
        {
            p_element = p_element.getParent();
        }

        if (p_element.getType() == GXML_ROOT)
        {
            return p_element;
        }

        return null;
    }

    /**
     * Return the GxmlElement highest ancestor of p_gxmlElement that is also in
     * p_gxmlElements. Returns null if no ancestor in p_gxmlElements. All
     * elements must belong to the same tree structure of elements.
     * 
     * @param p_gxmlElement
     *            element to find highest ancestor for
     * @param p_gxmlElements
     *            Collection of GxmlElements of possible ancestors to find the
     *            highest in.
     * @return highest ancestor of p_gxmlElement that is also in p_gxmlElements.
     *         Returns null if no ancestor in p_gxmlElements.
     */
    public static GxmlElement getHighestAncestor(GxmlElement p_gxmlElement,
            Collection p_gxmlElements)
    {
        GxmlElement highestAncestor = null;
        GxmlElement parent = p_gxmlElement.getParent();

        while (parent != null)
        {
            Iterator it = p_gxmlElements.iterator();

            while (it.hasNext())
            {
                Object o = it.next();
                if (o == null)
                {
                    continue;
                }

                GxmlElement gxmlElement = (GxmlElement) o;
                if (parent == gxmlElement)
                {
                    highestAncestor = parent;
                    break;
                }
            }

            parent = parent.getParent();
        }

        return highestAncestor;
    }

    /**
     * Copy the un-set attributes in the target element from the source element.
     * 
     * @param p_targetElement
     *            the element to have un-sets be copied to.
     * @param p_sourceElement
     *            the element to have attributes copied from.
     * @param p_elementType
     *            the first element of this type in the tree to have the
     *            operation applied to. If Gxml.UNSPECIFIED, then apply the
     *            operation to the p_targetElement itself only.
     */
    public static void copyShallowUnsetAttributes(GxmlElement p_targetElement,
            GxmlElement p_sourceElement, int p_elementType)
    {
        GxmlElement target = p_targetElement;

        if (!(p_elementType == GxmlElement.UNSPECIFIED)
                && target.getType() != p_elementType)
        {
            List descendants = p_targetElement.getDescendantElements(new int[]
            { p_elementType });

            if (descendants == null || descendants.isEmpty())
            {
                return;
            }

            target = (GxmlElement) descendants.get(0);
        }

        if (p_sourceElement.m_attributes == null)
        {
            return;
        }

        Set keys = p_sourceElement.m_attributes.keySet();
        Iterator keysIt = keys.iterator();

        while (keysIt.hasNext())
        {
            String key = (String) keysIt.next();
            String attributeValue = target.getAttribute(key);

            if (attributeValue == null)
            {
                target.setAttribute(key, p_sourceElement.getAttribute(key));
            }
        }
    }

    /**
     * Replace the p_oldElement in the tree with p_replacementElement.
     * 
     * @param p_oldElement
     *            the element to be replaced
     * @param p_replacementElement
     *            the element to replace the old one
     */
    public static void replace(GxmlElement p_oldElement,
            GxmlElement p_replacementElement)
    {
        GxmlElement parent = p_oldElement.getParent();

        p_replacementElement.setParent(parent);
        p_oldElement.setParent(null);

        if (parent == null)
        {
            return; // nothing more to do
        }

        List children = parent.getChildElements();
        int pos = children.indexOf(p_oldElement);

        children.set(pos, p_replacementElement);
        parent.setChildElements(children);
    }

    /**
     * Normalize "type" attribute according to TmxTypeMapper class.
     */
    public void normalizeAllDescendentsType()
    {
        int[] elementTypes =
        { GxmlElement.BPT, GxmlElement.PH, GxmlElement.IT };

        List elements = getDescendantElements(elementTypes);
        Iterator it = elements.iterator();
        while (it.hasNext())
        {
            GxmlElement elem = (GxmlElement) it.next();
            String type = elem.getAttribute(GxmlNames.BPT_TYPE);
            if (type != null)
            {
                elem.setAttribute(GxmlNames.BPT_TYPE,
                        TmxTypeMapper.normalizeType(type));
            }
        }
    }

    public boolean equals(Object p_object)
    {
        if (this == p_object)
        {
            return true;
        }

        if (p_object instanceof GxmlElement)
        {
            return this.equals((GxmlElement) p_object);
        }

        return false;
    }

    public boolean equals(GxmlElement p_other)
    {
        if (this == p_other)
        {
            return true;
        }

        if (m_type != p_other.m_type)
        {
            return false;
        }

        if ((m_childElements == null && p_other.m_childElements != null)
                || (m_childElements != null && p_other.m_childElements == null))
        {
            return false;
        }

        if (m_childElements != null)
        {
            Iterator it = m_childElements.iterator();
            Iterator ito = p_other.m_childElements.iterator();
            while (it.hasNext() && ito.hasNext())
            {
                GxmlElement gxmlElement = (GxmlElement) it.next();
                GxmlElement gxmlElementO = (GxmlElement) ito.next();
                if (!gxmlElement.equals(gxmlElementO))
                {
                    return false;
                }
            }
        }
        if ((m_attributes == null && p_other.m_attributes != null)
                || (m_attributes != null && p_other.m_attributes == null))
        {
            return false;
        }
        if (this instanceof TextNode)
        {
            if (!(this.getTotalTextValue().equals(p_other.getTotalTextValue())))
            {
                return false;
            }
        }
        if (m_attributes != null)
        {
            return unorderedEquals(m_attributes, p_other.m_attributes);
        }
        return true;
    }

    /**
     * Determine equality of two Maps independant of the order of the elements.
     * 
     * @param p_map1
     *            first Map
     * @param p_map2
     *            second Map
     * @return true if the elements are equal regardless of their order.
     */
    public static final boolean unorderedEquals(Map p_map1, Map p_map2)
    {
        return (contains(p_map1, p_map2) && contains(p_map2, p_map1));
    }

    /**
     * Determines if p_map2 contains all the keys and values in p_map1. Returns
     * true if so.
     * 
     * @param p_map1
     *            first Map
     * @param p_map2
     *            second Map
     * @return true if p_map2 contains all the keys and values in p_map1
     */
    public static final boolean contains(Map p_map1, Map p_map2)
    {
        Set set = p_map1.keySet();
        Iterator it = set.iterator();

        while (it.hasNext())
        {
            Object key = it.next();
            Object value1 = p_map1.get(key);
            Object value2 = p_map2.get(key);

            if (key.toString().equals("wordcount"))
            {
                continue;
            }

            if (value1 == null && value2 != null)
            {
                return false;
            }

            if (value1 != null && value2 == null)
            {
                return false;
            }

            if (value1 != null && value2 != null && !value1.equals(value2))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests if a number is in a number array.
     */
    private boolean isNumberInArray(int p_num, int[] p_array)
    {
        for (int i = 0; i < p_array.length; i++)
        {
            if (p_num == p_array[i])
            {
                return true;
            }
        }

        return false;
    }

    private boolean isSkip()
    {
        return "true".equals(this.getAttribute("isSkip"));
    }
}
