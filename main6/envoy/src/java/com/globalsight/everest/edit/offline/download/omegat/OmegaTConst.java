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
package com.globalsight.everest.edit.offline.download.omegat;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.ling.docproc.IFormatNames;

/**
 * const values for OmegaT
 * 
 * @author Wayzou
 * 
 */
public class OmegaTConst
{

    private static final Logger logger = Logger.getLogger(OmegaTConst.class);

    public static String omegat_project = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<omegat>\n"
            + "    <project version=\"1.0\">\n"
            + "        <source_dir>inbox</source_dir>\n"
            + "        <target_dir>__DEFAULT__</target_dir>\n"
            + "        <tm_dir>__DEFAULT__</tm_dir>\n"
            + "        <offline_mt_dir>__DEFAULT__</offline_mt_dir>\n"
            + "        <glossary_dir>terminology</glossary_dir>\n"
            + "        <glossary_file>terminology/new-glossary.txt</glossary_file>\n"
            + "        <dictionary_dir>__DEFAULT__</dictionary_dir>\n"
            + "        <source_lang>(source_lang)</source_lang>\n"
            + "        <target_lang>(target_lang)</target_lang>\n"
            + "        <sentence_seg>false</sentence_seg>\n"
            + "        <support_default_translations>true</support_default_translations>\n"
            + "        <remove_tags>false</remove_tags>\n"
            + "    </project>\n"
            + "</omegat>";

    public static String omegat_project_file = "omegat.project";
    public static String omegat_foldername = "omegat/";
    public static String dictionary_foldername = "dictionary/";
    public static String target_foldername = "target/";
    public static String omegat_filterxml_file = "omegat/filters.xml";
    
    public static String tu_type_ice = "x-ice";
    public static String tu_type_100 = "x-100pc";
            
    public static String filter_xml = "";

    public static String OMEGAT_QUICK_START = "OmegaT_Quick_Start.pdf";
    static
    {
        String fileName = "/properties/omegat_filters.xml.properties";
        String filterXml = "";

        try
        {
            URL url = OmegaTConst.class.getResource(fileName);
            File theFile = null;

            if (url != null)
            {
                try
                {
                    theFile = new File(url.toURI());
                }
                catch (Exception exx)
                {
                    theFile = new File(url.getPath());
                }
            }

            if (theFile != null && theFile.exists())
            {
                filterXml = FileUtils.read(theFile);
            }
            else
            {
                filterXml = FileUtils.read(
                        OmegaTConst.class.getResourceAsStream(fileName),
                        "utf-8");
            }
        }
        catch (Exception e)
        {
            StringBuffer sb = new StringBuffer(
                    "Error when loading OmegaT Filter xml :\n");
            sb.append(fileName);
            logger.error(sb.toString(), e);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("OmegaT Filter xml loaded:\n" + filterXml + "\n");
        }

        filter_xml = filterXml;
    }

}
