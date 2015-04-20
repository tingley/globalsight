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
package com.globalsight.everest.tm.exporter;

import java.util.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;
import org.dom4j.tree.DefaultAttribute;

import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.util.StringUtil;
import com.globalsight.util.XmlParser;

public class TmxChecker
{
    private HashMap<String, ArrayList<String>> dtdMap = new HashMap<String, ArrayList<String>>();
    private ArrayList<String> needCheckXAttribute = new ArrayList<String>();

    public TmxChecker()
    {
        constuctDtdMap();
        constructXArray();
    }

    /*
     * Remove all attributes not belong their element in the TMX 1.4 DTD
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public String fixSegment(String segment)
    {
        Document dom = getDom(segment);
        Element root = dom.getRootElement();

        Iterator ite = dtdMap.entrySet().iterator();
        while (ite.hasNext())
        {
            Map.Entry entry = (Map.Entry) ite.next();
            String key = (String) entry.getKey();
            ArrayList array = (ArrayList) entry.getValue();

            String nodeName = "//" + key;
            List nodes = root.selectNodes(nodeName);

            for (int x = 0; x < nodes.size(); x++)
            {
                Element node = (Element) nodes.get(x);
                Attribute internalAttr = node.attribute("internal");
                Attribute typeAttr = node.attribute("type");
                ArrayList list = new ArrayList();
                list.addAll(node.attributes());
                resetXAttribute(key, list);

                for (int y = 0; y < list.size(); y++)
                {
                    Attribute temp = (Attribute) list.get(y);
                    String name = temp.getName();

                    if (!array.contains(name))
                    {
                        node.remove(temp);
                    }
                }
                // GBS-3537 & GBS-3691
				if (internalAttr != null
						&& "yes".equalsIgnoreCase(internalAttr.getValue()))
				{
					String exportedType = "x-internal";
					if (typeAttr != null)
					{
						String type = typeAttr.getValue();
						if (StringUtil.isNotEmpty(type))
						{
							exportedType += "-" + type.trim().toLowerCase();
						}
						typeAttr.setValue(exportedType);
					}
					else
					{
						node.add(new DefaultAttribute("type", exportedType));
					}
				}
            }
        }

        return root.asXML();
    }

    /**
	 * When export TM, "internal='yes' type='style'" will be merged to
	 * "type='x-internal-style'"; When import back, need revert back.
	 * 
	 * @param segment
	 * @return
	 */
    public String revertInternalTag(String segment)
    {
        Document dom = getDom(segment);
        Element root = dom.getRootElement();
        for (String tag : dtdMap.keySet())
        {
            String nodeName = "//" + tag;
            List nodes = root.selectNodes(nodeName);
            for (int x = 0; x < nodes.size(); x++)
            {
                Element node = (Element) nodes.get(x);
				if (node.attribute("type") != null
						&& node.attribute("type").getValue() != null)
                {
					Attribute typeAttr = node.attribute("type");
					String type = typeAttr.getValue();
					if ("x-internal".equalsIgnoreCase(type))
					{
						node.remove(typeAttr);
						node.add(new DefaultAttribute("internal", "yes"));
					}
					else if (type.startsWith("x-internal"))
					{
						String realTypeValue = type.substring("x-internal".length() + 1);
						typeAttr.setValue(realTypeValue);
						node.add(new DefaultAttribute("internal", "yes"));
					}
                }
            }
        }

        return root.asXML();
    }

    public void fixTuvByDtd(List tuvs)
    {
        for (int i = 0; i < tuvs.size(); i++)
        {
            SegmentTmTuv tuv = (SegmentTmTuv) tuvs.get(i);
            tuv.setSegment(fixSegment(tuv.getSegment()));
        }
    }

    /*
     * Some tag such as "ph>" has X attribute and its value maybe is String, if
     * it is string, should not be written into TMX, and set value of id
     * attribute into it's value.
     */
    private void resetXAttribute(String name, List list)
    {

        if (name == null || !needCheckXAttribute.contains(name))
        {
            return;
        }

        Attribute idAttr = null;
        Attribute xAttr = null;

        for (int x = 0; x < list.size(); x++)
        {
            Attribute temp = (Attribute) list.get(x);
            if (temp.getName().equals("id"))
            {
                idAttr = temp;
            }
            else if (temp.getName().equals("x"))
            {
                xAttr = temp;
            }
        }

        if (idAttr != null && xAttr != null)
        {
            try
            {
                Integer.parseInt(xAttr.getValue());
            }
            catch (NumberFormatException nfe)
            {
                xAttr.setValue(idAttr.getValue());
            }
        }
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

    /*
     * set the dtd standard element into the map to check the tuv contents
     */
    private void constuctDtdMap()
    {
        ArrayList<String> array = new ArrayList<String>();

        array.add("x");
        array.add("assoc");
        array.add("type");

        dtdMap.put("ph", array);

        array = new ArrayList<String>();
        array.add("x");
        array.add("i");
        array.add("type");
        dtdMap.put("bpt", array);

        array = new ArrayList<String>();
        array.add("i");
        dtdMap.put("ept", array);

        array = new ArrayList<String>();
        array.add("datatype");
        array.add("type");
        dtdMap.put("sub", array);

        array = new ArrayList<String>();
        array.add("x");
        array.add("pos");
        array.add("type");
        dtdMap.put("it", array);

        array = new ArrayList<String>();
        array.add("x");
        array.add("type");
        dtdMap.put("hi", array);

        array = new ArrayList<String>();
        array.add("x");
        dtdMap.put("ut", array);
    }

    private void constructXArray()
    {
        needCheckXAttribute.add("ph");
    }
}
