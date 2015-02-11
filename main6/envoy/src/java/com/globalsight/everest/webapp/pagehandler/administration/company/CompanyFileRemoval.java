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
package com.globalsight.everest.webapp.pagehandler.administration.company;

import java.io.File;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.msoffice.OfficeXmlHelper;
import com.globalsight.cxe.adapter.openoffice.OpenOfficeHelper;
import com.globalsight.cxe.util.EventFlowXmlParser;
import com.globalsight.everest.company.Company;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;

/**
 * Helper of deleting files on converters while removing a company. This is
 * mainly using information from EVENT_FLOW_XML stored in job's request to
 * delete the files.
 */
public class CompanyFileRemoval
{
    private static final Logger CATEGORY = Logger
            .getLogger(CompanyFileRemoval.class.getName());

    private EventFlowXmlParser m_parser = null;

    public CompanyFileRemoval(String eventFlowXml) throws Exception
    {
        m_parser = new EventFlowXmlParser();
        m_parser.parse(eventFlowXml);
    }

    /**
     * Removes converter files based on company separated folder under the
     * conversion directory.
     */
    public static void removeConverterFiles(Company company)
    {
        String companyName = company.getCompanyName();
        removeWindowsPe(companyName);
    }

    /**
     * Removes files on converters.
     */
    public void removeConverterFile()
    {
        String safeBaseFileName = m_parser.getSafeBaseFileName();
        String formatType = m_parser.getSourceFormatType();
        if (safeBaseFileName == null || formatType == null)
        {
            return;
        }
        safeBaseFileName = safeBaseFileName.toLowerCase();
        formatType = formatType.toLowerCase();

        if (IFormatNames.FORMAT_OFFICE_XML.equals(formatType))
        {
            removeOffice2010();
        }
        else if (IFormatNames.FORMAT_WORD_HTML.equals(formatType)
                || IFormatNames.FORMAT_EXCEL_HTML.equals(formatType)
                || IFormatNames.FORMAT_POWERPOINT_HTML.equals(formatType)
                || IFormatNames.FORMAT_RTF.equals(formatType))
        {
            removeOffice20032007();
        }
        else if (IFormatNames.FORMAT_XML.equals(formatType))
        {
            if (safeBaseFileName.endsWith(".idml"))
            {
                removeIdml();
            }
            else if (safeBaseFileName.endsWith(".indd"))
            {
                removeIndd();
            }
        }
        else if (IFormatNames.FORMAT_MIF.equals(formatType)
                && safeBaseFileName.endsWith(".fm"))
        {
            removeFrameMaker9();
        }
        else if (IFormatNames.FORMAT_OPENOFFICE_XML.equals(formatType))
        {
            removeOpenOffice();
        }
    }

    public void removeConverterFile(String targetLocale)
    {
        String safeBaseFileName = m_parser.getSafeBaseFileName();
        String formatType = m_parser.getSourceFormatType();
        if (safeBaseFileName == null || formatType == null)
        {
            return;
        }
        safeBaseFileName = safeBaseFileName.toLowerCase();
        formatType = formatType.toLowerCase();

        if (IFormatNames.FORMAT_OFFICE_XML.equals(formatType))
        {
            removeOffice2010(targetLocale);
        }
        else if (IFormatNames.FORMAT_WORD_HTML.equals(formatType)
                || IFormatNames.FORMAT_EXCEL_HTML.equals(formatType)
                || IFormatNames.FORMAT_POWERPOINT_HTML.equals(formatType)
                || IFormatNames.FORMAT_RTF.equals(formatType))
        {
            removeOffice20032007(targetLocale);
        }
        else if (IFormatNames.FORMAT_XML.equals(formatType))
        {
            if (safeBaseFileName.endsWith(".idml"))
            {
                removeIdml(targetLocale);
            }
            else if (safeBaseFileName.endsWith(".indd"))
            {
                removeIndd(targetLocale);
            }
        }
        else if (IFormatNames.FORMAT_MIF.equals(formatType)
                && safeBaseFileName.endsWith(".fm"))
        {
            removeFrameMaker9(targetLocale);
        }
        else if (IFormatNames.FORMAT_OPENOFFICE_XML.equals(formatType))
        {
            removeOpenOffice(targetLocale);
        }
    }

    /**
     * Removes framemaker9 files from winfiles directory.
     */
    private void removeFrameMaker9()
    {
        String sourceLocale = m_parser.getSourceLocale();
        String targetLocale = m_parser.getTargetLocale();
        // delete files in source directory
        removeFrameMaker9(sourceLocale);
        String[] targetLocales = targetLocale.split(",");
        for (String target : targetLocales)
        {
            // delete files in target directories
            removeFrameMaker9(target);
        }
    }

    private void removeFrameMaker9(String locale)
    {
        String safeBaseFileName = m_parser.getSafeBaseFileName();
        StringBuilder path = new StringBuilder(
                AmbFileStoragePathUtils.getFrameMaker9ConversionPath());

        path.append(File.separator);
        path.append(locale);
        path.append(File.separator);
        path.append(safeBaseFileName);

        String fm = path.toString().replace("/", File.separator);
        CATEGORY.info("Deleting converter file " + fm);
        FileUtil.deleteFile(new File(fm));
        CATEGORY.info("Done deleting converter file " + fm);

        String pathWithoutExtension = fm.substring(0, fm.lastIndexOf("."));
        CATEGORY.info("Deleting converter file " + pathWithoutExtension
                + ".mif");
        FileUtil.deleteFile(new File(pathWithoutExtension + ".mif"));
        CATEGORY.info("Done deleting converter file " + pathWithoutExtension
                + ".mif");
    }

    /**
     * Removes idml files from Idml-Conv directory.
     */
    private void removeIdml()
    {
        String sourceLocale = m_parser.getSourceLocale();
        String targetLocale = m_parser.getTargetLocale();
        // delete files in source directory
        removeIdml(sourceLocale);
        String[] targetLocales = targetLocale.split(",");
        for (String target : targetLocales)
        {
            // delete files in target directories
            removeIdml(target);
        }
    }

    private void removeIdml(String locale)
    {
        String safeBaseFileName = m_parser.getSafeBaseFileName();
        StringBuilder path = new StringBuilder(
                AmbFileStoragePathUtils.getIdmlConversionPath());

        path.append(File.separator);
        path.append(locale);
        path.append(File.separator);
        path.append(safeBaseFileName);

        String filePath = path.toString().replace("/", File.separator);
        CATEGORY.info("Deleting converter file " + filePath);
        FileUtil.deleteFile(new File(filePath));
        CATEGORY.info("Done deleting converter file " + filePath);

        path.append(".unzip");
        filePath = path.toString().replace("/", File.separator);
        CATEGORY.info("Deleting converter file " + filePath);
        FileUtil.deleteFile(new File(filePath));
        CATEGORY.info("Done deleting converter file " + filePath);

        path.append("content.xml");
        filePath = path.toString().replace("/", File.separator);
        CATEGORY.info("Deleting converter file " + filePath);
        FileUtil.deleteFile(new File(filePath));
        CATEGORY.info("Done deleting converter file " + filePath);
    }

    /**
     * Removes indd files from winfiles directory.
     */
    private void removeIndd()
    {
        String sourceLocale = m_parser.getSourceLocale();
        String targetLocale = m_parser.getTargetLocale();
        // delete files in source directory
        removeIndd(sourceLocale);
        String[] targetLocales = targetLocale.split(",");
        for (String target : targetLocales)
        {
            // delete files in target directories
            removeIndd(target);
        }
    }

    private void removeIndd(String locale)
    {
        String safeBaseFileName = m_parser.getSafeBaseFileName();
        String formatType = m_parser.getFormatType().toLowerCase();
        String winfilesPath = AmbFileStoragePathUtils
                .getInddCs2ConversionPath();
        if (formatType.endsWith("cs3"))
        {
            winfilesPath = AmbFileStoragePathUtils.getInddCs3ConversionPath();
        }
        else if (formatType.endsWith("cs4"))
        {
            winfilesPath = AmbFileStoragePathUtils.getInddCs4ConversionPath();
        }
        else if (formatType.endsWith("cs5"))
        {
            winfilesPath = AmbFileStoragePathUtils.getInddCs5ConversionPath();
        }
        else if (formatType.endsWith("cs5.5"))
        {
            winfilesPath = AmbFileStoragePathUtils.getInddCs55ConversionPath();
        }

        StringBuilder path = new StringBuilder(winfilesPath);
        path.append(File.separator);
        path.append("indd");
        path.append(File.separator);
        path.append(locale);
        path.append(File.separator);
        path.append(safeBaseFileName);

        String indd = path.toString().replace("/", File.separator);
        String pathWithoutExtension = indd.substring(0, indd.lastIndexOf("."));
        CATEGORY.info("Deleting converter files named " + pathWithoutExtension);
        FileUtil.deleteFile(new File(indd));
        FileUtil.deleteFile(new File(pathWithoutExtension + ".xml"));
        FileUtil.deleteFile(new File(pathWithoutExtension + ".xmp"));
        FileUtil.deleteFile(new File(pathWithoutExtension + ".pdf"));
        CATEGORY.info("Done deleting converter files named "
                + pathWithoutExtension);
    }

    /**
     * Removes office 2010 files from OfficeXml-Conv directory.
     */
    private void removeOffice2010()
    {
        String sourceLocale = m_parser.getSourceLocale();
        String targetLocale = m_parser.getTargetLocale();
        // delete files in source directory
        removeOffice2010(sourceLocale);
        String[] targetLocales = targetLocale.split(",");
        for (String target : targetLocales)
        {
            // delete files in target directories
            removeOffice2010(target);
        }
    }

    /**
     * Removes office 2003 and 2007 files from winfiles directory.
     */
    private void removeOffice20032007()
    {
        String sourceLocale = m_parser.getSourceLocale();
        String targetLocale = m_parser.getTargetLocale();
        // delete files in source directory
        removeOffice20032007(sourceLocale);
        String[] targetLocales = targetLocale.split(",");
        for (String target : targetLocales)
        {
            // delete files in target directories
            removeOffice20032007(target);
        }
    }

    private void removeOffice2010(String locale)
    {
        String safeBaseFileName = m_parser.getSafeBaseFileName();
        StringBuilder path = new StringBuilder(
                AmbFileStoragePathUtils.getOffice2010ConversionPath());

        path.append(File.separator);
        path.append(locale);
        path.append(File.separator);
        path.append(safeBaseFileName);

        String filePath = path.toString().replace("/", File.separator);
        FileUtil.deleteFile(new File(filePath));

        path.append(".");
        if (safeBaseFileName.endsWith(".docx"))
        {
            path.append(OfficeXmlHelper.OFFICE_DOCX);
        }
        else if (safeBaseFileName.endsWith(".pptx"))
        {
            path.append(OfficeXmlHelper.OFFICE_PPTX);
        }
        else
        {
            path.append(OfficeXmlHelper.OFFICE_XLSX);
        }

        filePath = path.toString().replace("/", File.separator);
        CATEGORY.info("Deleting converter files in " + filePath + " directory");
        FileUtil.deleteFile(new File(filePath));
        CATEGORY.info("Done deleting converter files in " + filePath
                + " directory");
    }

    private void removeOffice20032007(String locale)
    {
        String safeBaseFileName = m_parser.getSafeBaseFileName();
        String formatType = m_parser.getSourceFormatType().toLowerCase();
        String formatName = m_parser.getSourceFormatName().toLowerCase();
        String winfilesPath = AmbFileStoragePathUtils
                .getOffice2007ConversionPath();
        if ("word2003".equals(formatName) || "excel2003".equals(formatName)
                || "powerpoint2003".equals(formatName))
        {
            winfilesPath = AmbFileStoragePathUtils
                    .getOffice2003ConversionPath();
        }

        StringBuilder path = new StringBuilder(winfilesPath);
        path.append(File.separator);
        if (IFormatNames.FORMAT_WORD_HTML.equals(formatType)
                || IFormatNames.FORMAT_RTF.equals(formatType))
        {
            path.append("word");
        }
        else if (IFormatNames.FORMAT_POWERPOINT_HTML.equals(formatType))
        {
            path.append("powerpoint");
        }
        else
        {
            path.append("excel");
        }
        path.append(File.separator);
        path.append(locale);
        path.append(File.separator);
        path.append(safeBaseFileName);

        String docx = path.toString().replace("/", File.separator);
        String pathWithoutExtension = docx.substring(0, docx.lastIndexOf("."));
        CATEGORY.info("Deleting converter files named " + pathWithoutExtension);
        FileUtil.deleteFile(new File(docx));
        FileUtil.deleteFile(new File(pathWithoutExtension + ".html"));
        FileUtil.deleteFile(new File(pathWithoutExtension + "_files"));
        FileUtil.deleteFile(new File(pathWithoutExtension + ".files"));
        CATEGORY.info("Done deleting converter files named "
                + pathWithoutExtension);
    }

    /**
     * Removes openoffice files from OpenOffice-Conv directory.
     */
    private void removeOpenOffice()
    {
        String sourceLocale = m_parser.getSourceLocale();
        String targetLocale = m_parser.getTargetLocale();
        // delete files in source directory
        removeOpenOffice(sourceLocale);
        String[] targetLocales = targetLocale.split(",");
        for (String target : targetLocales)
        {
            // delete files in target directories
            removeOpenOffice(target);
        }
    }

    private void removeOpenOffice(String locale)
    {
        String safeBaseFileName = m_parser.getSafeBaseFileName();
        StringBuilder path = new StringBuilder(
                AmbFileStoragePathUtils.getOpenOfficeConversionPath());

        path.append(File.separator);
        path.append(locale);
        path.append(File.separator);
        path.append(safeBaseFileName);

        String filePath = path.toString().replace("/", File.separator);
        FileUtil.deleteFile(new File(filePath));

        path.append(".");
        if (safeBaseFileName.endsWith(".odt"))
        {
            path.append(OpenOfficeHelper.OPENOFFICE_ODT);
        }
        else if (safeBaseFileName.endsWith(".ods"))
        {
            path.append(OpenOfficeHelper.OPENOFFICE_ODS);
        }
        else
        {
            path.append(OpenOfficeHelper.OPENOFFICE_ODP);
        }

        filePath = path.toString().replace("/", File.separator);
        CATEGORY.info("Deleting converter files in " + filePath + " directory");
        FileUtil.deleteFile(new File(filePath));
        CATEGORY.info("Done deleting converter files in " + filePath
                + " directory");
    }

    private static void removeWindowsPe(String companyName)
    {
        StringBuilder path = new StringBuilder(
                AmbFileStoragePathUtils.getWindowsPeConversionPath());
        path.append(File.separator);
        path.append("winpe");
        path.append(File.separator);
        path.append(companyName);

        String filePath = path.toString();
        CATEGORY.info("Deleting company " + companyName
                + " converter files in " + filePath + " directory");
        FileUtil.deleteFile(new File(filePath));
        CATEGORY.info("Done deleting company " + companyName
                + " converter files in " + filePath + " directory");
    }
}
