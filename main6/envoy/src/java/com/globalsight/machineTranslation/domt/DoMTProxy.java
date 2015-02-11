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
package com.globalsight.machineTranslation.domt;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;

import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.TextNode;

public class DoMTProxy extends AbstractTranslator implements MachineTranslator
{
    private static final Logger logger = Logger.getLogger(DoMTProxy.class);

    public static final String DEFAULT_ENGINE_NAME = "translate-xliff";
    
    private static Object LOCK = new Object();

    private static boolean useTagTranslation = true;

    public DoMTProxy() throws MachineTranslationException
    {
    }

    public String getEngineName()
    {
        return ENGINE_DOMT;
    }

    /**
     * DoMT supports all locales in theory. One engine (name) is for one locale
     * pair. If the engine is not for current locale pair, "status" API will
     * return "failed" status at once, so the check will be done when real hit
     * MT.
     */
    @Override
    public boolean supportsLocalePair(Locale p_sourceLocale,
            Locale p_targetLocale) throws MachineTranslationException
    {
        return true;
    }

    @Override
    protected String doTranslation(Locale p_sourceLocale,
            Locale p_targetLocale, String p_string)
            throws MachineTranslationException
    {
        if (StringUtil.isEmpty(p_string))
            return null;

        try
        {
            String wrappedSegment = p_string;
            if (!p_string.toLowerCase().startsWith("<segment"))
            {
                wrappedSegment = "<segment>" + p_string + "</segment>";
            }

            String[] segments = new String[] { wrappedSegment };

            String[] translations = doBatchTranslation(p_sourceLocale,
                    p_targetLocale, segments);

            if (translations != null && translations.length > 0)
            {
                return GxmlUtil.stripRootTag(translations[0]);
            }
        }
        catch (Exception e)
        {
            
        }

        return null;
    }

    @Override
    protected String[] doBatchTranslation(Locale sourceLocale,
            Locale targetLocale, String[] segments)
            throws MachineTranslationException
    {
        if (sourceLocale == null || targetLocale == null
                || segments == null || segments.length < 1)
        {
            return null;
        }

        String[] results = new String[segments.length];
        if (useTagTranslation)
        {
            results = translateWithTags(sourceLocale, targetLocale, segments);
        }
        else
        {
            results = translatePureText(sourceLocale, targetLocale, segments);
        }

        return results;
    }

    /**
     * Send segments with tags to DOMT engine.
     */
    private String[] translateWithTags(Locale sourceLocale,
            Locale targetLocale, String[] segments)
    {
        String[] results = new String[segments.length];

        boolean isXlf = MTHelper.isXlf(this.getMtParameterMap());
        try
        {
            // Ensure the sequence will be unchanged after translation.
            HashMap<Integer, String> id2Segs = new HashMap<Integer, String>();
            String[] heads = new String[segments.length];
            for (int i = 0; i < segments.length; i++)
            {
                int index = segments[i].indexOf(">");
                heads[i] = segments[i].substring(0, index + 1);

                boolean hasInternalText = (segments[i].indexOf(" internal=\"yes\"") > -1);
                GxmlElement gxmlRoot = MTHelper.getGxmlElement(segments[i]);
                List subFlowList = gxmlRoot.getDescendantElements(GxmlElement.SUB_TYPE);
                if ((subFlowList == null || subFlowList.size() == 0) && !hasInternalText)
                {
                    String segmentWithId = segments[i];
                    if (!isXlf)
                    {
                        segmentWithId = segmentWithId.replace(" i=", " id=");
                    }
                    id2Segs.put(composeKey(i, 0), GxmlUtil.stripRootTag(segmentWithId));
                }
                else
                {
                    // If segment gxml HAS subs, send texts to MT then compose
                    // back again.
                    List items = MTHelper.getImmediateAndSubImmediateTextNodes(gxmlRoot);
                    for (int subIndex = 0; subIndex < items.size(); subIndex++)
                    {
                        TextNode textNode = (TextNode) items.get(subIndex);
                        id2Segs.put(composeKey(i, subIndex + 1), textNode.toGxml());
                    }
                }
            }

            if (id2Segs.size() > 0)
            {
                String srcXlf = getDoMtXliff(id2Segs, sourceLocale, targetLocale);
                if (MTHelper.isLogDetailedInfo(ENGINE_DOMT))
                {
                    logger.info("Segments in XLF sending to DoMT:" + srcXlf);
                }

                String translatedXlf = hitDoMt(sourceLocale, targetLocale, srcXlf);
                if (MTHelper.isLogDetailedInfo(ENGINE_DOMT))
                {
                    logger.info("Segments in XLF returned from DoMT:" + translatedXlf);
                }

                if (!isXlf && StringUtil.isNotEmpty(translatedXlf))
                {
                    translatedXlf = translatedXlf.replace(" id=", " i=")
                            .replace("trans-unit i=", "trans-unit id=");
                }

                // id :: translated targets
                HashMap<Integer, String> targets = extractDoMtReturning(translatedXlf);

                HashMap<Integer, HashMap<Integer, String>> targetGroups = getTargetGroups(targets);

                String translatedSegment = "";
                for (int mainIndex = 0; mainIndex < segments.length; mainIndex++)
                {
                    translatedSegment = "";
                    HashMap<Integer, String> subSet = targetGroups.get(mainIndex);
                    if (subSet == null)
                    {
                        results[mainIndex] = heads[mainIndex] + "" + "</segment>";
                        continue;
                    }

                    boolean hasInternalText = (segments[mainIndex].indexOf(" internal=\"yes\"") > -1);
                    GxmlElement gxmlRoot = MTHelper.getGxmlElement(segments[mainIndex]);
                    List subFlowList = gxmlRoot.getDescendantElements(GxmlElement.SUB_TYPE);
                    if ((subFlowList == null || subFlowList.size() == 0) && !hasInternalText)
                    {
                        translatedSegment = subSet.get(0);
                        // if DoMT fails to translate this, it returns -1.
                        if (translatedSegment == null
                                || "-1".equals(translatedSegment))
                        {
                            translatedSegment = "";
                        }
                    }
                    else
                    {
                        List items = MTHelper.getImmediateAndSubImmediateTextNodes(gxmlRoot);
                        if (items != null && items.size() > 0 && items.size() == subSet.size())
                        {
                            for (int subIndex = 0; subIndex < items.size(); subIndex++)
                            {
                                TextNode textNode = (TextNode) items.get(subIndex);
                                String trans = subSet.get(subIndex + 1);
                                if (trans == null || "-1".equals(trans))
                                {
                                    trans = "";
                                }
                                textNode.setTextBuffer(new StringBuffer(trans));
                            }
                            translatedSegment = GxmlUtil.stripRootTag(gxmlRoot.toGxml());
                        }
                    }
                    results[mainIndex] = heads[mainIndex] + translatedSegment + "</segment>";
                }
            }
        }
        catch (MachineTranslationException e)
        {
            logger.error(e.getMessage());
        }

        return results;
    }

    /**
     * Send pure texts to DoMT engine for translation.
     */
    private String[] translatePureText(Locale sourceLocale,
            Locale targetLocale, String[] segments)
    {
        String[] results = new String[segments.length];
        try
        {
            // Ensure the sequence will be unchanged after translation.
            HashMap<Integer, String> id2Segs = new HashMap<Integer, String>();
            for (int i = 0; i < segments.length; i++)
            {
                String[] segmentsFromGxml = MTHelper
                        .getSegmentsInGxml(segments[i]);
                if (segmentsFromGxml == null || segmentsFromGxml.length < 1)
                {
                    results[i] = segments[i];
                }
                else
                {
                    for (int count = 0; count < segmentsFromGxml.length; count++)
                    {
                        id2Segs.put(composeKey(i, count), EditUtil
                                .encodeXmlEntities(segmentsFromGxml[count]));
                    }
                }
            }

            if (id2Segs.size() > 0)
            {
                String srcXlf = getDoMtXliff(id2Segs, sourceLocale, targetLocale);
                if (MTHelper.isLogDetailedInfo(ENGINE_DOMT))
                {
                    logger.info("Segments in XLF sending to DoMT:" + srcXlf);
                }

                String translatedXlf = hitDoMt(sourceLocale, targetLocale, srcXlf);
                if (MTHelper.isLogDetailedInfo(ENGINE_DOMT))
                {
                    logger.info("Segments in XLF returned from DoMT:" + translatedXlf);
                }

                // id :: translated targets
                HashMap<Integer, String> targets = extractDoMtReturning(translatedXlf);

                HashMap<Integer, HashMap<Integer, String>> targetGroups = getTargetGroups(targets);

                for (int mainIndex = 0; mainIndex < segments.length; mainIndex++)
                {
                    HashMap<Integer, String> subSet = targetGroups.get(mainIndex);
                    GxmlElement gxmlRoot =
                            MTHelper.getGxmlElement(segments[mainIndex]);
                    List items = MTHelper
                            .getImmediateAndSubImmediateTextNodes(gxmlRoot);
                    for (int subIndex = 0; subIndex < items.size(); subIndex++)
                    {
                        TextNode textNode = (TextNode) items.get(subIndex);
                        if (subSet == null)
                        {
                            textNode.setTextBuffer(new StringBuffer(""));
                        }
                        else
                        {
                            String seg = subSet.get(subIndex);
                            // if DoMT fails to translate this, it returns -1.
                            if (seg == null || "-1".equals(seg))
                            {
                                seg = "";
                            }
                            textNode.setTextBuffer(new StringBuffer(seg));
                        }
                    }
                    results[mainIndex] = gxmlRoot.toGxml();
                }
            }
        }
        catch (MachineTranslationException e)
        {
            logger.error(e.getMessage());
        }

        return results;
    }
    
    @SuppressWarnings("rawtypes")
    private String hitDoMt(Locale p_sourceLocale, Locale p_targetLocale,
            String p_xlf) throws MachineTranslationException
    {
        String translatedXlf = null;
        try
        {
            String url = (String) getMtParameterMap().get(
                    MTProfileConstants.MT_DOMT_URL);
            XmlRpcClient client = DoMTUtil.getXmlRpcClient(url);

            String[] xliff = new String[] { p_xlf };
            Object[] params = new Object[]{ xliff };
            Object[] returning = null;
            int runTimes = 0;
            // DoMT can't handle concurrent requests very well, if fail, recall
            // it at most 5 times.
            while (runTimes < 5)
            {
                runTimes++;
                try
                {
                    synchronized (LOCK)
                    {
                        Thread.sleep(3000);
                        logger.info("invoke 'run' API times : " + runTimes);
                        returning = (Object[]) client.execute("run", params);
                    }

                    if (MTHelper.isLogDetailedInfo(ENGINE_DOMT))
                    {
                        DoMTUtil.logRunInfo(returning);
                    }

                    if (DoMTUtil.isRunSucceed(returning))
                    {
                        break;
                    }
                }
                catch (Exception e)
                {
                    logger.warn(e.getMessage());
                }
            }

            if (DoMTUtil.isRunSucceed(returning))
            {
                int statusTimes = 0;
                String jobId = (String) returning[0];
                String[] jobIds = new String[] {jobId};
                Object[] statusParams = new Object[4];
                statusParams[0] = jobIds;// job_ids
                statusParams[1] = new Boolean(false);// diagnostics
                statusParams[2] = new Boolean(true);// include_contents
                statusParams[3] = new Boolean(false);// delete

                HashMap status = null;
                int maxTimes = Math.round(getMaxWaitTime() / 15);
                // Wait for "getMaxWaitTime()" seconds at most.
                while (statusTimes < maxTimes)
                {
                    statusTimes++;
                    try
                    {
                        synchronized (LOCK)
                        {
                            Thread.sleep(3000);
                            logger.info("invoke 'status' API times : " + statusTimes);
                            status = (HashMap) client.execute("status", statusParams);
                        }

                        if (MTHelper.isLogDetailedInfo(ENGINE_DOMT))
                        {
                            DoMTUtil.logStatusInfo(status, jobId);
                        }

                        String jobStatus = null;
                        HashMap jobValues = (HashMap) status.get(jobId);
                        if (jobValues != null)
                        {
                            jobStatus = (String) jobValues.get(DoMTUtil.KEY_STATUS);
                        }
                        String subStatus = null;
                        HashMap serverValues = (HashMap) status.get(DoMTUtil.KEY_SERVER);
                        if (serverValues != null)
                        {
                            subStatus = (String) serverValues.get(DoMTUtil.KEY_SUBSTATUS);
                        }

                        if (DoMTUtil.JOB_STATUS_COMPLETED
                                .equalsIgnoreCase(jobStatus))
                        {
                            Object[] content = (Object[]) jobValues.get(DoMTUtil.KEY_CONTENT);
                            if (content != null && content.length > 0)
                            {
                                translatedXlf = (String) content[0];
                                break;
                            }
                        }
                        else if (DoMTUtil.JOB_STATUS_STOPPED.equalsIgnoreCase(jobStatus)
                                || DoMTUtil.JOB_STATUS_FAILED.equalsIgnoreCase(jobStatus))
                        {
                            logger.warn("Job is in '" + jobStatus
                                    + "' status, stop waiting and return.");
                            break;
                        }
                        else if ("errors".equalsIgnoreCase(subStatus))
                        {
//                            break;
                        }
                        else
                        {
                            logger.info("Wait for 15 seconds before next invoking 'status'...");
                            Thread.sleep(15000);// 15 seconds
                        }
                    }
                    catch (Exception e)
                    {
                        logger.warn(e.getMessage());
                    }
                }

                if (org.apache.commons.lang3.StringUtils.isBlank(translatedXlf))
                {
                    return null;
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new MachineTranslationException(e);
        }

        return translatedXlf;
    }

    private String getDoMtXliff(HashMap<Integer, String> segmentsNoTag,
            Locale sourceLocale, Locale targetLocale)
    {
        String sourceLang = DoMTUtil.checkLang(sourceLocale.getLanguage(),
                sourceLocale.getCountry());
        String targetLang = DoMTUtil.checkLang(targetLocale.getLanguage(),
                targetLocale.getCountry());
        String engineName = (String) getMtParameterMap().get(
                MTProfileConstants.MT_DOMT_ENGINE_NAME);

        // "translate-xliff" is a default engine on DoMT server, it is using
        // "nl" and "en" as source/target language name.
        if (DEFAULT_ENGINE_NAME.equals(engineName))
        {
            sourceLang = "nl";
            targetLang = "en";
        }

        StringBuffer xlf = new StringBuffer();
        xlf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        xlf.append("<xliff version=\"1.2\">\r\n");
        xlf.append("<file original=\"None\" source-language=\"")
                .append(sourceLang).append("\" target-language=\"")
                .append(targetLang).append("\" datatype=\"multi-format\">\r\n");
        xlf.append("<header>\r\n");
        xlf.append("  <note from=\"PTTOOLS\">\r\n");
        xlf.append("    <graphname>").append(engineName).append("</graphname>\r\n");
        xlf.append("  </note>\r\n");
        xlf.append("</header>\r\n");
        xlf.append("<body>\r\n");
        Iterator<Entry<Integer, String>> it = segmentsNoTag.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<Integer, String> entry = (Entry<Integer, String>) it.next();
            int id = entry.getKey();
            String source = entry.getValue();
            xlf.append("<trans-unit id=\"" + id + "\" translate=\"yes\">\r\n");
            xlf.append("<source>").append(source).append("</source>\r\n");
            xlf.append("</trans-unit>\r\n");            
        }
        xlf.append("</body>\r\n");
        xlf.append("</file>\r\n");
        xlf.append("</xliff>\r\n");

        return xlf.toString();
    }

    /**
     * Extract to get the target translations in DoMT returned xliff string.
     * 
     * @param mtReturning
     *            -- xliff string DoMT returned.
     * @return -- tuid : target map
     */
    private HashMap<Integer, String> extractDoMtReturning(String mtReturning)
    {
        HashMap<Integer, String> targets = new HashMap<Integer, String>();

        if (StringUtil.isEmpty(mtReturning))
            return targets;

        try
        {
            StringReader sr = new StringReader(mtReturning);
            InputSource is = new InputSource(sr);
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.parse(is);

            Element doc = parser.getDocument().getDocumentElement();
            NodeList tuNodeList = doc.getElementsByTagName("trans-unit");
            if (tuNodeList != null && tuNodeList.getLength() > 0)
            {
                for (int i = 0; i < tuNodeList.getLength(); i++)
                {
                    Node tuNode = tuNodeList.item(i);
                    String id = tuNode.getAttributes().getNamedItem("id")
                            .getNodeValue();
                    NodeList tuSubNodeList = tuNode.getChildNodes();
                    if (tuSubNodeList != null && tuSubNodeList.getLength() > 0)
                    {
                        for (int j = 0; j < tuSubNodeList.getLength(); j++)
                        {
                            Node tuSubNode = tuSubNodeList.item(j);
                            if ("target".equalsIgnoreCase(tuSubNode
                                    .getNodeName()))
                            {
                                String target = MTHelper.outputNode2Xml(tuSubNode);
                                if (StringUtil.isNotEmpty(target))
                                {
                                    int index = target.indexOf("<target ");
                                    if (index > -1)
                                        target = target.substring(index);
                                }
                                target = GxmlUtil.stripRootTag(target);
                                targets.put(Integer.parseInt(id), target);
                                break;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Fail to extract the DoMT translated xliff", e);
        }

        return targets;
    }

    private HashMap<Integer, HashMap<Integer, String>> getTargetGroups(
            HashMap<Integer, String> targets)
    {
        HashMap<Integer, HashMap<Integer, String>> result = new HashMap<Integer, HashMap<Integer, String>>();

        Iterator<Entry<Integer, String>> trgIt = targets.entrySet().iterator();
        while (trgIt.hasNext())
        {
            Entry<Integer, String> entry = trgIt.next();
            int[] indexes = getIndexes(entry.getKey());

            HashMap<Integer, String> subSet = result.get(indexes[0]);
            if (subSet == null)
            {
                subSet = new HashMap<Integer, String>();
                result.put(indexes[0], subSet);
            }
            subSet.put(indexes[1], entry.getValue());
        }

        return result;
    }

    /**
     * The key is composed of 3 parts: "1" as a start; "indexInArray" as middle;
     * if a segment with tag has sub text, the sub text index as the third part.
     * 
     * @param indexInArray
     * @param subTextIndex
     * @return
     */
    private int composeKey(int indexInArray, int subTextIndex)
    {
        String subIndex = String.valueOf(subTextIndex);
        if (subTextIndex < 10)
        {
            subIndex = "00" + subTextIndex;
        }
        else if (subTextIndex < 100 && subTextIndex >= 10)
        {
            subIndex = "0" + subTextIndex;
        }

        return Integer.parseInt("1" + indexInArray + subIndex);
    }

    /**
     * Get the main index and sub index in array.
     * @param composedKey
     * @return
     */
    private int[] getIndexes(int composedKey)
    {
        try
        {
            String key = String.valueOf(composedKey);
            String mainIndex = key.substring(1, key.length() - 3);
            String subIndex = key.substring(key.length() - 3);
            if (subIndex.startsWith("00"))
            {
                subIndex = subIndex.substring(2);
            }
            else if (subIndex.startsWith("0"))
            {
                subIndex = subIndex.substring(1);
            }

            return new int[]
            { Integer.parseInt(mainIndex), Integer.parseInt(subIndex) };
        }
        catch (Exception e)
        {

        }

        return null;
    }

    private static int getMaxWaitTime()
    {
        // Default 15 minutes.
        int maxWaitTime = 900;
        try
        {
            String param = MTHelper.getMTConfig("domt.max.wait.timeout");
            if (param != null)
            {
                maxWaitTime = Integer.parseInt(param);
            }
        }
        catch (Exception ignore)
        {

        }
        return maxWaitTime;
    }
}