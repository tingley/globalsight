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
package com.globalsight.machineTranslation.asiaOnline;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import AOAPI.Translate;

import com.globalsight.everest.projecthandler.AsiaOnlineLP2DomainInfo;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.edit.GxmlUtil;

public class AsiaOnlineProxy extends AbstractTranslator implements MachineTranslator
{
    private static final GlobalSightCategory s_logger =
        (GlobalSightCategory) GlobalSightCategory.getLogger(AsiaOnlineProxy.class);
    
    private static final String ENGINE_NAME = "Asia_Online";
    
    public AsiaOnlineProxy() throws MachineTranslationException
    {
    }
    
    public String getEngineName()
    {
        return ENGINE_NAME;
    }
    
    protected String doTranslation(Locale p_sourceLocale, Locale p_targetLocale,
            String p_string) throws MachineTranslationException
    {
        String result = null;
        
        // 1. Prepare the result file
        String directory = AmbFileStoragePathUtils.getFileStorageDirPath();
        String filepath = directory + File.separator + ENGINE_NAME
                + File.separator + p_targetLocale;

        // For single segment, only "Result" is required
        File resultPath = new File(filepath + File.separator + "results");
        if (!resultPath.exists())
        {
            resultPath.mkdirs();
        }
        java.util.Date now = new java.util.Date();
        File resultFile = new File(resultPath, now.getTime() + ".txt");

        // 2. Translate the single segment
        Translate translate = new Translate();
        AsiaOnlineLP2DomainInfo lp2DC = 
            findAOLP2DCBySrcTrgLang(p_sourceLocale, p_targetLocale);
        translate.setLanguagePair((int)lp2DC.getLanguagePairCode());
        translate.setDomainCombination((int)lp2DC.getDomainCombinationCode());
        // projectNo default 1
        translate.setProjectNo(1);
        // "PHRASE" for single translation
        translate.setDocumentType("PHRASE");
        translate.setDebugLog(true);
        translate.setPriority(String.valueOf(20));

        AsiaOnlineMtInvoker aoInvoker = getAOMtInvoker();
        aoInvoker.setAOTranslation(translate);
        // 2.2 Translate the XLIFF file
        aoInvoker.translationAndGetResult(p_string, resultFile
                .getAbsolutePath());
        
        // 3. get result string
        if (resultFile.exists())
        {
            try
            {
                FileInputStream file = new FileInputStream(resultFile);
                BufferedReader buffReader = new BufferedReader(
                        new InputStreamReader(file, "UTF-8"));
                String tempString = buffReader.readLine();

                StringBuffer allFileContents = new StringBuffer();
                while (tempString != null)
                {
                    allFileContents.append(tempString).append("\n");
                    tempString = buffReader.readLine();
                }
                
                result = allFileContents.toString();

                resultFile.deleteOnExit();
            }
            catch (Exception e)
            {
                s_logger.error(e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * Batch translation.
     * 
     * @param p_sourceLocale
     * @param p_targetLocale
     * @param p_string[]
     * @return
     * @throws MachineTranslationException
     */
    protected String[] doBatchTranslation(Locale p_sourceLocale,
            Locale p_targetLocale, String[] p_segments)
            throws MachineTranslationException
    {
        String[] results = null;
        
        // 1.Write all segments into one XLIFF file
        String directory = AmbFileStoragePathUtils.getFileStorageDirPath();
        String filepath = directory + File.separator + ENGINE_NAME
                + File.separator + p_targetLocale;
        // Original path
        File originalPath = new File(filepath + File.separator + "original");
        if (!originalPath.exists())
        {
            originalPath.mkdirs();
        }
        // Result path
        File resultPath = new File(filepath + File.separator + "results");
        if (!resultPath.exists())
        {
            resultPath.mkdirs();
        }
        // Generate the XLIFF file
        HashMap paramMap = getMtParameterMap();
        long sourcePageID = ((Long)paramMap.get(SOURCE_PAGE_ID)).longValue();
        File originalFile = new File(originalPath, sourcePageID + "_1.xlf");
        File resultFile = new File(resultPath, sourcePageID + "_1.xlf");
        // If this has been existed, this should be the second time to translate
        // full text values, so ensure the files won't be overridden.
        if (originalFile.exists())
        {
            originalFile = new File(originalPath, sourcePageID + "_2.xlf");
            resultFile = new File(resultPath, sourcePageID + "_2.xlf");
        }
		
        try 
        {
            FileOutputStream fos = new FileOutputStream(originalFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos, 4096);
            OutputStreamWriter osw = new OutputStreamWriter(bos, FileUtil.UTF8);
            writeAsiaOnlineXliffFile(osw, p_sourceLocale, p_targetLocale,
                    p_segments);
            osw.flush();
            osw.close();
            bos.close();
            fos.close();
        }
        catch (Exception ex)
        {
            
        }
        
        // 2.Translate the XLIFF file
        // 2.1 Set "translate" object for invoker.
        Translate translate = new Translate();
        AsiaOnlineLP2DomainInfo lp2DC = 
            findAOLP2DCBySrcTrgLang(p_sourceLocale, p_targetLocale);
        translate.setLanguagePair((int)lp2DC.getLanguagePairCode());
        translate.setDomainCombination((int)lp2DC.getDomainCombinationCode());
        // projectNo default 1
        translate.setProjectNo(1);
        // "XLIFF" for batch translation
        translate.setDocumentType("XLIFF");
        translate.setDebugLog(true);
        translate.setPriority(String.valueOf(20));

        AsiaOnlineMtInvoker aoInvoker = getAOMtInvoker();
        aoInvoker.setAOTranslation(translate);
        // 2.2 Translate the XLIFF file
        s_logger.info("****** Translation from " + p_sourceLocale + " to "
                + p_targetLocale + "******");
        aoInvoker.translationAndGetResult(originalFile.getAbsolutePath(),
                resultFile.getAbsolutePath());
        
        // 3. Extract the returned XLIFF file
        if (resultFile.exists())
        {
            FileInputStream file;
            try
            {
                file = new FileInputStream(resultFile);
                List aoResultList = extractResultFile(file);
                results = new String[aoResultList.size()];
                if (aoResultList != null && aoResultList.size() > 0)
                {
                    for (int i=0; i<aoResultList.size(); i++)
                    {
                        results[i] = (String) aoResultList.get(i);
                    }
                }
            }
            catch (FileNotFoundException e)
            {
                s_logger.error(e.getMessage());
            }
        }
        
        return results;
    }

    /**
     * Judge if current locale pair is supported by Asia Online.
     * 
     * @param p_SourceLocale
     *            - the source locale to be translated from.
     * @param p_TargetLocale
     *            - the target locale to be translated to.
     */
    public boolean supportsLocalePair(Locale p_sourceLocale,
            Locale p_targetLocale) throws MachineTranslationException
    {
        boolean isSupportLocalePair = false;

        AsiaOnlineLP2DomainInfo lp2DC = 
            findAOLP2DCBySrcTrgLang(p_sourceLocale, p_targetLocale);
        if (lp2DC != null)
        {
            isSupportLocalePair = true;
        }

        return isSupportLocalePair;
    }
    
    /**
     * get "AsiaOnlineMtInvoker" object.
     * 
     * @return AsiaOnlineMtInvoker
     */
    private AsiaOnlineMtInvoker getAOMtInvoker()
    {
        HashMap paramMap = getMtParameterMap();
        
        String aoMtUrl = (String) paramMap.get(MachineTranslator.AO_URL);
        long aoMtPort = ((Long) paramMap.get(MachineTranslator.AO_PORT)).longValue();
        String aoMtUserName = (String) paramMap.get(MachineTranslator.AO_USERNAME);
        String aoMtPassword = (String) paramMap.get(MachineTranslator.AO_PASSWORD);
        long aoMtAccountNumber = ((Long) paramMap
                .get(MachineTranslator.AO_ACCOUNT_NUMBER)).longValue();
        
        AsiaOnlineMtInvoker ao_mt = new AsiaOnlineMtInvoker(aoMtUrl,
                (int) aoMtPort, aoMtUserName, aoMtPassword,
                (int) aoMtAccountNumber);

        return ao_mt;
    }
    
    private void writeAsiaOnlineXliffFile(OutputStreamWriter m_outputStream,
            Locale p_soruceLocale, Locale p_targetLocale, String[] p_segments)
            throws IOException
    {
        writeXlfHeader(m_outputStream);
        writeTDADocumentHeader(m_outputStream, p_soruceLocale.toString(),
                p_targetLocale.toString());
        writeTranslationUnit(m_outputStream, p_segments);
        writeXlfEnd(m_outputStream);
    }
    
    private void writeXlfHeader(OutputStreamWriter m_outputStream)
            throws IOException
    {
        String m_strEOL = "\r\n";
        m_outputStream.write("<?xml version=\"1.0\"?>");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("<xliff version=\"1.2\">");
        m_outputStream.write(m_strEOL);
    }
    
    private void writeTDADocumentHeader(OutputStreamWriter m_outputStream,
            String sLocale, String tLocale) throws IOException
    {
        String m_strEOL = "\r\n";
        String m_space = "  ";
        m_outputStream.write("<file ");
        m_outputStream.write("original=" + str2DoubleQuotation("None"));
        m_outputStream.write(m_space);
        m_outputStream.write("source-language="
                + str2DoubleQuotation(sLocale.replace("_", "-")));
        m_outputStream.write(m_space);
        m_outputStream.write("target-language="
                + str2DoubleQuotation(tLocale.replace("_", "-")));
        m_outputStream.write(m_space);
        m_outputStream.write(">");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("<body>");
        m_outputStream.write(m_strEOL);
    }
    
    private void writeTranslationUnit(OutputStreamWriter m_outputStream,
            String[] p_segments) throws IOException
    {
        int size = 0;
        if (p_segments != null)
        {
            size = p_segments.length;
        }

        for (int i = 0; i < size; i++)
        {
            String segment = p_segments[i];
            if (segment != null && segment.startsWith("<segment")
                    && segment.endsWith("</segment>"))
            {
                segment = GxmlUtil.stripRootTag(segment);
            }
            String m_strEOL = "\r\n";
            m_outputStream.write("<trans-unit");
            m_outputStream.write(">");
            m_outputStream.write(m_strEOL);
            m_outputStream.write("<source>");
            m_outputStream.write(segment);
            m_outputStream.write("</source>");
            m_outputStream.write(m_strEOL);
            m_outputStream.write("<target></target>");
            m_outputStream.write(m_strEOL);
            m_outputStream.write("</trans-unit>");
            m_outputStream.write(m_strEOL);
        }
    }
    
    private void writeXlfEnd(OutputStreamWriter m_outputStream)
            throws IOException
    {
        String m_strEOL = "\r\n";
        m_outputStream.write("</body>");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("</file>");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("</xliff>");
    }
    
    /*
     * parse string to "string"
     */
    private String str2DoubleQuotation(String str)
    {
        String result = null;
        result = new StringBuffer().append("\"").append(str).append("\"")
                .toString();
        return result;
    }
    
    /**
     * Find the locale pair-domain combination by source and target languages.
     * 
     * @param p_sourceLocale
     * @param p_targetLocale
     * 
     * @return
     */
    private AsiaOnlineLP2DomainInfo findAOLP2DCBySrcTrgLang(
            Locale p_sourceLocale, Locale p_targetLocale)
    {
        AsiaOnlineLP2DomainInfo result = null;
        if (p_sourceLocale == null || p_targetLocale == null)
        {
            return null;
        }
        
        String lp = p_sourceLocale.getLanguage() + "-"
                + p_targetLocale.getLanguage();
        HashMap paramMap = getMtParameterMap();
        Long tmProfileID = (Long) paramMap
                .get(MachineTranslator.TM_PROFILE_ID);
        try
        {
            TranslationMemoryProfile tmp = ServerProxy.getProjectHandler()
                    .getTMProfileById(tmProfileID.longValue(), false);
            Set lp2DomainCombinations = tmp.getTmProfileAoInfoSet();
            if (lp2DomainCombinations != null
                    && lp2DomainCombinations.size() > 0)
            {
                Iterator lp2DCIt = lp2DomainCombinations.iterator();
                while (lp2DCIt.hasNext())
                {
                    AsiaOnlineLP2DomainInfo aoLP2DC = 
                        (AsiaOnlineLP2DomainInfo) lp2DCIt.next();
                    String lpName = aoLP2DC.getLanguagePairName();
                    if (lpName != null && lpName.equalsIgnoreCase(lp))
                    {
                        result = aoLP2DC;
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
        }
        // Currently AO supports zh-CN, not support zh-HK and zh-TW.
        if (p_sourceLocale.getLanguage().equalsIgnoreCase("ZH")
                && !p_sourceLocale.getCountry().equalsIgnoreCase("CN"))
        {
            result = null;
        }
        if (p_targetLocale.getLanguage().equalsIgnoreCase("ZH")
                && !p_targetLocale.getCountry().equalsIgnoreCase("CN"))
        {
            result = null;
        }
        
        return result;
    }
    
    /**
     * Extract the XLIFF file to get all target segments in it.
     * 
     * @param file
     *            - file to be translated.
     * 
     * @return target segments list
     */
    private List extractResultFile(FileInputStream file)
    {
        List resultList = new ArrayList();
        
        try
        {
            BufferedReader buffReader = new BufferedReader(
                    new InputStreamReader(file, "UTF-8"));
            // Read all file contents to a string.
            StringBuffer allFileContents = new StringBuffer();
            String tempString = buffReader.readLine();
            while (tempString != null) 
            {
                allFileContents.append(tempString).append("\n");
                tempString = buffReader.readLine();
            }
            String str = allFileContents.toString();
            // Get the list of "target"
            resultList = getXMLForObject(str, "target");
        }
        catch (Exception e)
        {
            String msg = "Failed to get target segments from AO returned xliff file";
            s_logger.error(msg + ": " + e.getMessage());
        }

        return resultList;
    }
    
    /**
     * Get part XML string, which used for string2Objects.
     */
    public static List getXMLForObject(String xml, String tag)
    {
        List<String> list = new ArrayList<String>();
        String preTag = "<" + tag;
        String endTag = "</" + tag + ">";

        int prfPos = 0, endPos = 0;
        while (prfPos >= 0 && endPos >= 0 && prfPos < xml.length()
                && endPos < xml.length())
        {
            prfPos = xml.indexOf(preTag, endPos);
            endPos = xml.indexOf(endTag, prfPos) + endTag.length();
            if (prfPos >= 0)
            {
                String temp = xml.substring(prfPos, endPos);
                // Remove tag wraper such as <tag...> and </tag>
                int beginIndex = temp.indexOf(">") + 1;
                int endIndex = temp.lastIndexOf("<");
                temp = temp.substring(beginIndex, endIndex);
                temp = "<segment>" + temp + "</segment>";
                list.add(temp);
            }
        }
        return list;
    }
    
    public static void main(String[] args) throws Exception
    {
        AsiaOnlineProxy aoProxy = new AsiaOnlineProxy();
        FileInputStream file;
        try
        {
            String filePath = "C:\\47.xlf";
            file = new FileInputStream(new File(filePath));
            List aoResultList = aoProxy.extractResultFile(file);
            if (aoResultList != null && aoResultList.size() > 0)
            {
                for (int i=0; i<aoResultList.size(); i++)
                {
                    String result = (String) aoResultList.get(i);
                    System.out.println(i + ":: " + result);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
