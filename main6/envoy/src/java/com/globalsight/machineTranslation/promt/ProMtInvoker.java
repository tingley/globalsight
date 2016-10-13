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
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.axis.message.MessageElement;

import com.globalsight.machineTranslation.promt.pts9.*;


/**
 * This is for PTS8.
 * 
 * @author York
 * 
 */
public final class ProMtInvoker
{
    private PTServiceSoapProxy proxy = null;

    private static final Logger s_logger = Logger
            .getLogger(ProMtInvoker.class);

    public ProMtInvoker(String ptsUrl)
    {
        proxy = new PTServiceSoapProxy(ptsUrl);
    }

    public ProMtInvoker(String ptsUrl, String username, String password)
    {
        proxy = new PTServiceSoapProxy(ptsUrl, username, password);
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
            GetPTServiceDataSetResponseGetPTServiceDataSetResult ptsServiceDataSet = proxy
                    .getPTServiceDataSet();
            int length = 0;
            MessageElement[] mes = ptsServiceDataSet.get_any();
            if (mes.length > 0)
            {
                length = mes.length;
            }
            for (int i = 0; i < length; i++)
            {
                Iterator it1 = mes[i].getChildElements();
                while (it1.hasNext())
                {
                    MessageElement me = (MessageElement) it1.next();
                    Iterator it2 = me.getChildElements();
                    while (it2.hasNext())
                    {
                        MessageElement me2 = (MessageElement) it2.next();
                        String name = me2.getName();
                        if (i == 1 && "Directions".equalsIgnoreCase(name))
                        {
                            Iterator it3 = me2.getChildElements();
                            while (it3.hasNext())
                            {
                                MessageElement me3 = (MessageElement) it3
                                        .next();
                                String me3Name = me3.getName();
                                if ("id".equalsIgnoreCase(me3Name))
                                {
                                    dirId = me3.getValue();
                                }
                                else if ("Name".equalsIgnoreCase(me3Name))
                                {
                                    dirName = me3.getValue();
                                }
                                if (dirId != null && dirName != null)
                                {
                                    directionsMap.put(dirName, dirId);
                                    dirId = null;
                                    dirName = null;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (RemoteException remoteEx)
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.info("Can't get directions information via PTS8 API.");                
            }
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
        int intDirId = Integer.parseInt(dirId);
        List tplList = new ArrayList();
        try
        {
            GetDirectionDataSetResponseGetDirectionDataSetResult ptsDirDataSet = proxy
                    .getDirectionDataSet(intDirId);
            MessageElement[] mes = ptsDirDataSet.get_any();
            int length = mes.length;
            for (int i = 0; i < length; i++)
            {
                Iterator it1 = mes[i].getChildElements();
                while (it1.hasNext())
                {
                    MessageElement me = (MessageElement) it1.next();
                    Iterator it2 = me.getChildElements();
                    while (it2.hasNext())
                    {
                        MessageElement me2 = (MessageElement) it2.next();
                        String name = me2.getName();
                        if (i == 1 && "Templates".equalsIgnoreCase(name))
                        {
                            Iterator it3 = me2.getChildElements();
                            while (it3.hasNext())
                            {
                                MessageElement me3 = (MessageElement) it3
                                        .next();
                                String me3Name = me3.getName();
                                if ("id".equalsIgnoreCase(me3Name))
                                {
                                    String tplId = me3.getValue();
                                    tplList.add(tplId);
                                }
                            }
                        }
                    }
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
        String strRtn = null;
        try
        {
            int intDirId = (int) dirId;
            strRtn = proxy.translateText(intDirId, tplId, text);
        }
        catch (Exception e)
        {
            s_logger.info("Fail to get target translated text!");
            s_logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }

        return strRtn;
    }
    
    public static void main(String[] args)
    {
        String url = "http://ptsdemo.promt.ru/pts8/services/ptservice.asmx";
        String username = "ptsadmin";
        String password = "ytrewq";

        // String url = "http://promtamericas.com/pts8/services/ptservice.asmx";
        // String username = "pts-demo\test";
        // String password = "test";

        ProMtInvoker invoker = new ProMtInvoker(url, username, password);
        // get all "Directions of Translation" promt server supports
        try
        {
            HashMap directionsMap = invoker.getDirectionsHashMap();
            System.out.println(directionsMap);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out
                .println("=============================================================");

        // get all "Topic Templates" info by direction id
        try
        {
            List directionsTplList = invoker.getTopicTemplateByDirId("524320");
            System.out.println(directionsTplList);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out
                .println("=============================================================");

        // translate text
        try
        {
            String text = invoker.translateText(524289, "General",
                    "hello world");
            System.out.println("text :: " + text);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
