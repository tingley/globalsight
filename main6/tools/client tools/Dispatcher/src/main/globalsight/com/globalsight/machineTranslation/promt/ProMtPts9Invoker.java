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
package com.globalsight.machineTranslation.promt;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.machineTranslation.promt.pts9.*;

import com.globalsight.ling.common.XmlEntities;

/**
 * This is for PTS9.
 * 
 * @author York
 * 
 */
public final class ProMtPts9Invoker
{
    private PTSXLIFFTranslatorSoapProxy proxy = null;

    private static final Logger s_logger = Logger
            .getLogger(ProMtPts9Invoker.class);

    public ProMtPts9Invoker(String ptsUrl)
    {
        proxy = new PTSXLIFFTranslatorSoapProxy(ptsUrl);
    }

    public ProMtPts9Invoker(String ptsUrl, String username, String password)
    {
        proxy = new PTSXLIFFTranslatorSoapProxy(ptsUrl, username, password);
    }

    /**
     * Get all language pairs that current server supports.
     * 
     * @return
     * 
     * @throws Exception
     */
    public HashMap getDirectionsHashMap() throws Exception
    {
        HashMap directionsMap = new HashMap();
        String dirId = null;
        String dirName = null;
        try
        {
            Object[] directions = proxy.directions();
            if (directions != null && directions.length > 0)
            {
                for (int i = 0; i < directions.length; i++)
                {
                    DictionaryEntry de = (DictionaryEntry) directions[i];
                    dirId = String.valueOf((Integer) de.getKey());
                    dirName = (String) de.getValue();
                    directionsMap.put(dirName, dirId);
                }
            }
        }
        catch (RemoteException remoteEx)
        {
            s_logger
                    .info("Fail to get 'Directions of Translation' info, please check if the proMt server is configured correctly.");
            s_logger.error(remoteEx.getMessage(), remoteEx);
            throw new Exception(remoteEx.getMessage());
        }

        if (s_logger.isDebugEnabled())
        {
            s_logger.info(directionsMap);
        }

        return directionsMap;
    }

    /**
     * Get topic templates by specified direction ID.
     * 
     * @param dirId
     *            - ID for certain language pair.
     * @return
     * 
     * @throws Exception
     */
    public List getTopicTemplateByDirId(String dirId) throws Exception
    {
        List tplList = new ArrayList();
        try
        {
            int intDirId = Integer.parseInt(dirId);
            Object[] templates = proxy.templates(intDirId);
            if (templates != null && templates.length > 0)
            {
                for (int i = 0; i < templates.length; i++)
                {
                    Object template = templates[i];
                    tplList.add(template.toString());
                }
            }
        }
        catch (Exception ex)
        {
            s_logger
                    .info("Fail to get 'Topic Template' info for direction id "
                            + dirId
                            + " , please check if the proMt server is configured correctly.");
            s_logger.error(ex.getMessage(), ex);
            throw new Exception(ex.getMessage());
        }

        if (s_logger.isDebugEnabled())
        {
            s_logger.info(tplList);
        }

        return tplList;
    }

    public String translateText(long dirId, String tplId, String text)
            throws Exception
    {
        String result = null;
        try
        {
            int intDirId = (int) dirId;
            result = proxy.translateFormattedText(intDirId,
                    tplId, text, MachineTranslator.PROMT_PTS9_FILE_TYPE);
            
            /**
             * 
            // As PROMT PTS9 won't decode " ', decode the source text and result
            // to compare.
            XmlEntities xe = new XmlEntities();
            // Decode one time for "result"
            String resultCopy = xe.decodeStringBasic(result);
            // Decode twice for "origin"
            String origin = xe.decodeStringBasic(text);
            origin = xe.decodeStringBasic(origin);
            if (origin != null && origin.equals(resultCopy))
            {
                result = null;
            }
             */            
        }
        catch (Exception e)
        {
            s_logger.info("Fail to get target translated text!");
            s_logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }

        return result;
    }
    
    public static void main(String[] args)
    {
        String url = "http://ptsdemo.promt.ru/ptsxliff9/ptsxlifftranslator.asmx";
        String username = "ptsdemo\\promtamericas";
        String password = "300%gisting";
        PTSXLIFFTranslatorSoapProxy proxy = new PTSXLIFFTranslatorSoapProxy(
                url, username, password);
        try
        {
            Object[] directions = proxy.directions();
            if (directions != null && directions.length > 0)
            {
                for (int i = 0; i < directions.length; i++)
                {
                    DictionaryEntry de = (DictionaryEntry) directions[i];
                    Integer dirId = (Integer) de.getKey();
                    String lpName = (String) de.getValue();
                    System.out.println(i + " :: " + dirId + " :: " + lpName);
                    
                    Object[] templates = proxy.templates(dirId);
                    for (int j=0; j<templates.length; j++)
                    {
                        Object template = templates[j];
                        System.out.println("Templates " + j + " :: " + template.toString());
                    }
                }
            }
            
            String text = "<it i=\"1\" type=\"font\" pos=\"begin\" x=\"1\">&lt;font size=&quot;2&quot; face=&quot;Arial, Helvetica, sans-serif&quot;&gt;</it>HiSpeed Networks is revolutionizing the economics of global information exchange.";
//            <it i="1" type="font" pos="begin" x="1">&lt;font size=&quot;2&quot; face=&quot;Arial, Helvetica, sans-serif&quot;&gt;</it>HiSpeed Networks is revolutionizing the economics of global information exchange. </segment>, <segment segmentId="1" wordcount="33"><bpt i="1" type="font" x="1">&lt;font size=&quot;2&quot; face=&quot;Arial, Helvetica, sans-serif&quot;&gt;</bpt>Service providers, enterprises, governments, and research and education institutions worldwide rely on the company to deliver products for building networks that are tailored to the individual needs of their users, services, and applications.<ept i="1">&lt;/font&gt;</ept></segment>, <segment segmentId="1" wordcount="15"><it i="1" type="font" pos="begin" x="1">&lt;font size=&quot;2&quot; face=&quot;Arial, Helvetica, sans-serif&quot;&gt;</it>HiSpeed Networks concentrate our team effort on all our key customers and their technology requirements. </segment>, <segment segmentId="1" wordcount="2">Sample Document</segment>, <segment segmentId="1" wordcount="9"><bpt i="1" type="font" x="1">&lt;font size=&quot;3&quot; face=&quot;Arial, Helvetica, sans-serif&quot;&gt;</bpt><bpt erasable="yes" i="2" type="strong" x="2">&lt;strong&gt;</bpt>This is a sample page for &amp; &quot; &apos; &amp;lt; &gt; Demo purposes.<ept i="2">&lt;/strong&gt;</ept><ept i="1">&lt;/font&gt;</ept></segment>, <segment segmentId="2" wordcount="19">These customers constantly face trade-offs in their attempts to deliver a secure and dependable experience for their users: <it i="1" pos="end" x="2">&lt;/font&gt;</it></segment>, <segment segmentId="2" wordcount="18">Our purpose-built, high-performance IP platforms enable customers to support many different services and applications at scale. <it i="1" pos="end" x="2">&lt;/font&gt;</it>
//            <it i="1" type="font" pos="begin" x="1">&lt;font size=&quot;2&quot; face=&quot;Arial, Helvetica, sans-serif&quot;&gt;</it>HiSpeed Networks concentrate our team effort on all our key customers and their technology requirements.
            
//            String translation = proxy.translateFormattedText(33555457,
//                    "General lexicon", text, "text/xliff");
//            System.out.println(translation);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
    }
    
}
