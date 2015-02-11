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


package com.globalsight.ling.docproc.worldserver;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import com.globalsight.everest.page.PageTemplate;
import com.globalsight.ling.docproc.SkeletonDispose;
import com.globalsight.util.XmlParser;
import com.globalsight.ling.docproc.extractor.xliff.WSConstants;

public class WsSkeletonDispose implements SkeletonDispose, WSConstants
{
    /*
     * Deal with the skeleton of the worldserver xliff file job.
     * 
     * 1. If the tuv modified by MT, need add or modify the "translation_type"
     * value to be "machine_translation_mt", if is modified by user, the
     * attribute value should be "manual_translation".
     * 
     * 2. If the tuv modified by user, should set "translation_status" attribute
     * value to be "finished".
     * 
     * 3. remove attribute "match-quality".
     * 
     * 4. remove attribute "target_content" and "source_content".
     */
    @Override
    public String dealSkeleton(String skeleton, String localizedBy)
    {
        boolean localizedByUser = false;
        boolean localizedByMT = false;
        boolean localizedByLocalTM = false;

        if (localizedBy != null && localizedBy.equals(PageTemplate.byUser))
        {
            localizedByUser = true;
        }
        else if (localizedBy != null && localizedBy.equals(PageTemplate.byMT))
        {
            localizedByMT = true;
        }
        else if (localizedBy != null
                && localizedBy.equals(PageTemplate.byLocalTM))
        {
            localizedByLocalTM = true;
        }

        int begin = skeleton.indexOf("<" + IWS_SEGMENT_DATA);
        int end = skeleton.indexOf("</" + IWS_SEGMENT_DATA)
                + ("</" + IWS_SEGMENT_DATA +">").length();

        String iwsStr = skeleton.substring(begin, end);
        iwsStr = "<segmentdata "
                + "xmlns:iws=\"http://www.idiominc.com/ws/asset\">" + iwsStr
                + "</segmentdata>";
        Document dom = getDom(iwsStr);
        Element root = dom.getRootElement();
        List iwsStatusList = root.selectNodes("//iws:status");

        for (int x = 0; x < iwsStatusList.size(); x++)
        {
            Element status = (Element) iwsStatusList.get(x);

            if (localizedByUser || localizedByLocalTM || localizedByMT)
            {
                /*
                 * if (status.attribute("match-quality") != null)
                 * status.remove(status.attribute("match-quality"));
                 * 
                 * if (status.attribute("source_content") != null)
                 * status.remove(status.attribute("source_content"));
                 * 
                 * if (status.attribute("target_content") != null)
                 * status.remove(status.attribute("target_content"));
                 */
                List<Attribute> attrList = new ArrayList();
                attrList.addAll(status.attributes());

                for (int i = 0; i < attrList.size(); i++)
                {
                    String name = attrList.get(i).getName();
                    if (!name.equals(IWS_TRANSLATION_STATUS)
                            && !name.equals(IWS_TRANSLATION_TYPE)
                            && !name.equals(IWS_SOURCE_CONTENT))
                    {
                        status.remove(attrList.get(i));
                    }
                }
            }

            if (status.attribute(IWS_TRANSLATION_STATUS) != null)
            {
                if (localizedByUser)
                {
                    status.attribute(IWS_TRANSLATION_STATUS).setValue("finished");
                }
                else if (localizedByLocalTM)
                {
                    status.attribute(IWS_TRANSLATION_STATUS).setValue("pending");
                }
            }
            else
            {
                if (localizedByUser)
                {
                    status.addAttribute(IWS_TRANSLATION_STATUS, "finished");
                }
                else if (localizedByLocalTM)
                {
                    status.addAttribute(IWS_TRANSLATION_STATUS, "pending");
                }
            }

            if (status.attribute(IWS_TRANSLATION_TYPE) != null)
            {
                if (localizedByMT)
                {
                    status.attribute(IWS_TRANSLATION_TYPE).setValue(
                            "machine_translation_mt");
                }
                else if (localizedByUser || localizedByLocalTM)
                {
                    status.attribute(IWS_TRANSLATION_TYPE).setValue(
                            "manual_translation");
                }
            }
            else
            {
                if (localizedBy != null)
                {
                    if (localizedByMT)
                    {
                        status.addAttribute(IWS_TRANSLATION_TYPE,
                                "machine_translation_mt");
                    }
                    else if (localizedByUser || localizedByLocalTM)
                    {
                        status.addAttribute(IWS_TRANSLATION_TYPE,
                                "manual_translation");
                    }
                }
            }
        }

        iwsStr = dom.selectSingleNode("//iws:segment-metadata").asXML();
        String str = "xmlns:iws=\"http://www.idiominc.com/ws/asset\"";
        iwsStr = iwsStr.replace(str, "");
        skeleton = skeleton.substring(0, begin) + iwsStr + skeleton.substring(end);

        return skeleton;
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

}
