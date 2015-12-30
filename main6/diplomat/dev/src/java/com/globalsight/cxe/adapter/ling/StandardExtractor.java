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
package com.globalsight.cxe.adapter.ling;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.InputSource;

import com.globalsight.cxe.adapter.adobe.AdobeHelper;
import com.globalsight.cxe.adapter.adobe.InddRuleHelper;
import com.globalsight.cxe.adapter.idml.IdmlRuleHelper;
import com.globalsight.cxe.adapter.msoffice.OfficeXmlRuleHelper;
import com.globalsight.cxe.adapter.openoffice.OpenOfficeHelper;
import com.globalsight.cxe.adapter.openoffice.OpenOfficeRuleHelper;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.InternalTextHelper;
import com.globalsight.cxe.entity.filterconfiguration.MSOffice2010Filter;
import com.globalsight.cxe.entity.filterconfiguration.OpenOfficeFilter;
import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile;
import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileValidator;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.message.MessageDataReader;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.cxe.util.fileImport.eventFlow.Source;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.aligner.AlignerExtractor;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DiplomatSegmenterException;
import com.globalsight.ling.docproc.DiplomatWordCounter;
import com.globalsight.ling.docproc.DiplomatWordCounterException;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorRegistryException;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.LocalizableElement;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.extractor.msoffice2010.ExcelExtractor;
import com.globalsight.ling.docproc.extractor.msoffice2010.PptxExtractor;
import com.globalsight.ling.docproc.extractor.msoffice2010.WordExtractor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil;

/**
 * StandardExtractor is a helper class for the LingAdapter.
 * 
 * <p>
 * The StandardExtractor is a wrapper for calls made to the DiplomatAPI for
 * extracting any of the known formats as documented in the javadoc for
 * DiplomatAPI.
 */
@SuppressWarnings("deprecation")
public class StandardExtractor
{
    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////

    private org.apache.log4j.Logger m_logger;
    private String m_locale = null;
    private String m_encoding = null;
    private String m_formatType = null;
    private String m_formatName = null;
    private String m_ruleFile = null;
    private String m_displayName = "";
    private String m_fileProfile = null;
    private CxeMessage m_cxeMessage = null;
    private String[] m_errorArgs = new String[2];
    // For PPT issue
    private String m_bullets_MsOffice = null;
    // For Adobe Indesign Issue
    private int m_docPageNum;
    private int m_docPageCount;
    // for open office
    private String m_office_unPara = null;
    private String m_office_unChar = null;
    private String m_office_internalChar = null;
    private String m_xlsx_numStyleIds = null;
    private String m_xlsx_hiddenSharedSI = null;
    private String m_xlsx_sheetHiddenCell = null;
    private String m_xlsx_unextractableCellStyles = null;
    // GBS-3944
    private String m_xlsx_excelInternalTextCellStyles = null;
    private String m_isHeaderFooterTranslate = null;
    private String m_isToolTipsTranslate = null;
    private String m_isHiddenTextTranslate = null;
    private String m_isTableOfContentTranslate = null;
    // GBS-3054
    private String m_hyperlinkIds = null;
    private FileProfileImpl fileProfile = null;

    // private static final String SQL_SELECT_RULE =
    // "SELECT RULE_TEXT FROM FILE_PROFILE, XML_RULE"
    // + " WHERE FILE_PROFILE.ID=?"
    // + " and XML_RULE.ID=FILE_PROFILE.XML_RULE_ID";

    private static final String SQL_SELECT_RULE = "select xr.rule_text from "
            + "xml_rule xr, xml_rule_filter xrf, file_profile "
            + "fp where xr.id = xrf.xml_rule_id and xrf.id = "
            + "fp.filter_id and fp.filter_table_name='xml_rule_filter' and fp.id = ?";

    private static final String QUERY_FOR_FORMAT = "select k.format_type, k.pre_extract_event "
            + "from known_format_type k, file_profile f "
            + "where f.id=? and f.known_format_type_id = k.id";

    public static final String m_tag_start = "&lt;AllYourBaseAreBelongToUs&gt;";
    public static final String m_tag_end = "&lt;/AllYourBaseAreBelongToUs&gt;";
    public static final String m_tag_amp = "AmpersandOfGS";

    /**
     * Creates an Extractor
     * 
     * @param p_logger
     *            a logger to use
     * @param p_cxeMessage
     */
    StandardExtractor(org.apache.log4j.Logger p_logger, CxeMessage p_cxeMessage)
    {
        m_logger = p_logger;
        m_cxeMessage = p_cxeMessage;
    }

    /**
     * Extracts the given content held in the MessageData and returns a
     * MessageData containing GXML
     * 
     * @return MessageData corresponding to new GXML
     * @exception LingAdapterException
     */
    MessageData extract() throws LingAdapterException
    {
        parseEventFlowXml();
        String alignerExtractorName = (String) m_cxeMessage.getParameters()
                .get("AlignerExtractor");
        AlignerExtractor alignerExtractor = AlignerExtractor
                .getAlignerExtractor(alignerExtractorName);

        String fpId = (String) m_cxeMessage.getParameters()
                .get("FileProfileId");
        if (fpId != null)
        {
            fileProfile = HibernateUtil.get(FileProfileImpl.class,
                    Long.valueOf(fpId), false);
        }

        if (alignerExtractor == null)
        {
            m_logger.debug("aligner extractor is null - normal extraction");
            queryRuleFile();
        }
        else
        {
            m_logger.debug("aligner extractor is not null - aligner extraction");
            XmlRuleFile ruleFile = alignerExtractor.getXmlRule();
            if (ruleFile != null)
                m_ruleFile = ruleFile.getRuleText();
        }

        try
        {
            return createMessageData();
        }
        catch (Exception e)
        {
            m_logger.error("Failed to extract " + m_displayName, e);
            throw new LingAdapterException("Extraction", m_errorArgs, e);
        }
    }

    private FileMessageData createMessageData() throws Exception
    {
        long fileSizeBytes = m_cxeMessage.getMessageData().getSize();
        m_logger.info("Extracting: " + m_displayName + ", size: "
                + fileSizeBytes);
        String gxml = null;
        if (m_cxeMessage.getMessageType().getValue() == CxeMessageType.PRSXML_IMPORTED_EVENT)
            gxml = extractWithPrsXmlExtractor();
        else
            gxml = extractWithLingAPI();

        gxml = fixGxml(gxml);

        FileMessageData fmd = MessageDataFactory.createFileMessageData();
        OutputStreamWriter osw = new OutputStreamWriter(fmd.getOutputStream(),
                "UTF8");
        osw.write(gxml, 0, gxml.length());
        osw.close();
        m_logger.info("Done Extracting: " + m_displayName + ", result size: "
                + gxml.length());
        Logger.writeDebugFile("lae_gxml.xml", gxml);
        m_cxeMessage.setDeleteMessageData(true);
        // add here for GC
        gxml = null;

        return fmd;
    }

    private String fixGxml(String p_gxml)
    {
        if (m_formatType.equals(DiplomatAPI.FORMAT_WORD_HTML))
        {
            if (p_gxml != null && !"".equals(p_gxml.trim()))
            {
                // Removes all spell error tags and gram error tags added by
                // word.
                p_gxml = GxmlUtil.moveTages(p_gxml,
                        "&lt;span class=SpellE&gt;", "&lt;/span&gt;");
                p_gxml = GxmlUtil.moveTages(p_gxml, "&lt;span class=GramE&gt;",
                        "&lt;/span&gt;");
            }
        }
        else if (m_formatType.equals(DiplomatAPI.FORMAT_EXCEL_HTML)
                && !m_formatName.contains("2003"))
        {
            if (p_gxml != null && !"".equals(p_gxml.trim()))
            {
                // GBS-1016, Added by Vincent Yan 2010/07/02
                // Add some xml codes to make the outputting excel file can be
                // shown gridlines
                String regexStr = "&lt;/head&gt;";
                int index = p_gxml.indexOf(regexStr);
                if (index > 0)
                {
                    String preStr = p_gxml.substring(0, index - 1);
                    String lastStr = p_gxml.substring(index);
                    StringBuffer tmp = new StringBuffer();
                    tmp.append("&lt;!--[if gte mso 9]&gt;\n");
                    tmp.append("\t&lt;xml&gt;\n");
                    tmp.append("\t\t&lt;x:ExcelWorkbook&gt;\n");
                    tmp.append("\t\t\t&lt;x:ExcelWorksheets&gt;\n");
                    tmp.append("\t\t\t\t&lt;x:ExcelWorksheet&gt;\n");
                    tmp.append("\t\t\t\t\t&lt;x:WorksheetOptions&gt;\n");
                    tmp.append("\t\t\t\t\t\t&lt;x:Print&gt;\n");
                    tmp.append("\t\t\t\t\t\t\t&lt;x:ValidPrinterInfo /&gt;\n");
                    tmp.append("\t\t\t\t\t\t&lt;/x:Print&gt;\n");
                    tmp.append("\t\t\t\t\t&lt;/x:WorksheetOptions&gt;\n");
                    tmp.append("\t\t\t\t&lt;/x:ExcelWorksheet&gt;\n");
                    tmp.append("\t\t\t&lt;/x:ExcelWorksheets&gt;\n");
                    tmp.append("\t\t&lt;/x:ExcelWorkbook&gt;\n");
                    tmp.append("\t&lt;/xml&gt;\n");
                    tmp.append("&lt;![endif]--&gt;\n");

                    p_gxml = preStr + tmp.toString() + lastStr;
                }
            }
        }

        return p_gxml;
    }

    private void addHiddenRule(DiplomatAPI diplomat)
    {
        if (m_xlsx_hiddenSharedSI != null)
        {
            List<String> ids = MSOffice2010Filter.toList(m_xlsx_hiddenSharedSI);
            if (ids.size() > 0)
            {
                HiddenSharedStringRule rule = new HiddenSharedStringRule();
                rule.setHiddenSharedSI(ids);

                diplomat.addExtractRule(rule);
            }
        }

        if (m_xlsx_sheetHiddenCell != null)
        {
            List<String> cells = MSOffice2010Filter
                    .toList(m_xlsx_sheetHiddenCell);
            if (cells.size() > 0)
            {
                HiddenCellRule rule = new HiddenCellRule();
                rule.setHiddenCell(cells);

                diplomat.addExtractRule(rule);
            }
        }
    }

    private void addExcelStyleMap(DiplomatAPI diplomat)
    {
        Object excelStyleMap = m_cxeMessage.getParameters()
                .get("excelStyleMap");
        if (excelStyleMap != null)
        {
            diplomat.setExcelStyle((HashMap) excelStyleMap);
        }
    }

    private WordExtractor createWordExtractor()
    {
        WordExtractor e = new WordExtractor();
        e.addOptions("isToolTipsTranslate", m_isToolTipsTranslate);
        e.addOptions("unCharStyles", m_office_unChar);
        e.addOptions("unParaStyles", m_office_unPara);
        e.addOptions("internalCharStyles", m_office_internalChar);
        e.addOptions("isHiddenTextTranslate", m_isHiddenTextTranslate);
        e.addOptions("isTableOfContentTranslate", m_isTableOfContentTranslate);

        return e;
    }

    private PptxExtractor createPptxExtractor()
    {
        PptxExtractor e = new PptxExtractor();
        e.addOptions("isToolTipsTranslate", m_isToolTipsTranslate);

        return e;
    }

    private ExcelExtractor createExcelExtractor()
    {
        ExcelExtractor e = new ExcelExtractor();
        e.addOptions("m_xlsx_numStyleIds", m_xlsx_numStyleIds);
        e.addOptions("m_xlsx_hiddenSharedSI", m_xlsx_hiddenSharedSI);
        e.addOptions("m_xlsx_unextractableCellStyles",
                m_xlsx_unextractableCellStyles);
        e.addOptions("isHeaderFooterTranslate", m_isHeaderFooterTranslate);
        e.addOptions("m_xlsx_sheetHiddenCell", m_xlsx_sheetHiddenCell);
        e.addOptions("excelInternalTextCellStyles",
                m_xlsx_excelInternalTextCellStyles);

        return e;
    }

    /**
     * Prepares a DiplomatAPI object for extracting the given content, and then
     * does the extraction.
     * 
     * @return GXML As String
     */
    private String extractWithLingAPI() throws Exception
    {
        DiplomatAPI diplomat = new DiplomatAPI();
        diplomat.setCxeMessage(m_cxeMessage);
        // Blaise XLF file does not need segmentation even source equals target.
        try {
            String jobId = (String) m_cxeMessage.getParameters().get("JobId");
    		boolean isBlaiseJob = ServerProxy.getJobHandler()
    				.getJobById(Long.parseLong(jobId)).isBlaiseJob();
            if (isBlaiseJob) {
            	diplomat.setSentenceSegmentation(false);
            }
        } catch (Exception ignore) {

        }
        addExcelStyleMap(diplomat);

        // Now we get segmentationRuleFile through FileProfileId parameter
        // and then get segmentation rule text

        SegmentationRuleFile srf = null;
        // Not from aligner.
        if (fileProfile != null)
        {
            long lpId = fileProfile.getL10nProfileId();
            L10nProfile l10nProfile = ServerProxy.getProjectHandler()
                    .getL10nProfile(lpId);

            TranslationMemoryProfile tmp = l10nProfile
                    .getTranslationMemoryProfile();
            if (tmp != null)
            {
                srf = ServerProxy.getSegmentationRuleFilePersistenceManager()
                        .getSegmentationRuleFileByTmpid(
                                String.valueOf(tmp.getId()));
            }

            diplomat.setFileProfileId(String.valueOf(fileProfile.getId()));
            diplomat.setFilterId(fileProfile.getFilterId());
            diplomat.setFilterTableName(fileProfile.getFilterTableName());

            diplomat.setJsFilterRegex(fileProfile.getJavascriptFilterRegex());
        }

        // Set segmentation rule text
        if (srf == null)
        {
            // Using default system segmentation rule.
            srf = ServerProxy.getSegmentationRuleFilePersistenceManager()
                    .getDefaultSegmentationRuleFile();
        }
        if (srf == null)
        {
            m_logger.error("Could not get the Default segmentaion rule");
            throw new Exception("Could not get the Default segmentaion rule");
        }
        String ruleText = srf.getRuleText();
        // Validate xml rule text
        SegmentationRuleFileValidator val = new SegmentationRuleFileValidator();
        if (ruleText == null || ruleText.equalsIgnoreCase("default"))
        {
            // Use existing segmentaion function in GlobalSight.
            ruleText = "default";
            diplomat.setSegmentationRuleText(ruleText);
        }
        else
        {
            // Use segmentation rule configured.
            if (val.validate(ruleText, srf.getType()))
            {
                diplomat.setSegmentationRuleText(ruleText);
            }
            else
            {
                System.err.println("Error in segmentaion rule text");
                throw new Exception("segmentation rule text is not valid");
            }
        }

        diplomat.setEncoding(m_encoding);
        diplomat.setLocale(m_locale);
        diplomat.setInputFormat(m_formatType);

        if (WordExtractor.useNewExtractor(m_fileProfile))
        {
            if (m_displayName.toLowerCase().endsWith(".docx"))
            {
                WordExtractor extractor = createWordExtractor();
                diplomat.setExtractor(extractor);
            }
            else if (m_displayName.toLowerCase().endsWith(".pptx"))
            {
                PptxExtractor extractor = createPptxExtractor();
                diplomat.setExtractor(extractor);
            }
            else if (m_displayName.toLowerCase().endsWith(".xlsx"))
            {
                ExcelExtractor extractor = createExcelExtractor();
                diplomat.setExtractor(extractor);
            }
        }

        // For "indd", "inx" and "idml" files, use original xml "Extractor".
        if (fileProfile != null
                && getKnowFormatTypeIdsForIndd().contains(
                        fileProfile.getKnownFormatTypeId()))
        {
            AbstractExtractor extractor = new com.globalsight.ling.docproc.extractor.xml.Extractor();
            diplomat.setExtractor(extractor);
        }

        if (m_ruleFile != null)
        {
            createAdditionalRules();
            diplomat.setRules(m_ruleFile);
        }

        // For PPT issue
        diplomat.setBulletsMsOffice(m_bullets_MsOffice);

        FileMessageData fmd = null;
        boolean deleteFmd = false;
        if (m_cxeMessage.getMessageData() instanceof FileMessageData)
        {
            fmd = (FileMessageData) m_cxeMessage.getMessageData();
        }
        else
        {
            // create a secondary file message data object to hold the
            // data from this message data
            fmd = MessageDataFactory.createFileMessageData();
            fmd.copyFrom(m_cxeMessage.getMessageData());
            deleteFmd = true;
        }

        diplomat.setSourceFile(fmd.getFile());

        addHiddenRule(diplomat);

        // NOTE: This gets the whole String into memory...since this might be
        // huge,
        // the DiplomatAPI should be changed to return a filename or
        // InputStream.
        String gxml = diplomat.extract();

        if (fileProfile != null && fileProfile.isExtractWithSecondFilter())
        {
            String secondFilterTableName = fileProfile
                    .getSecondFilterTableName();
            long secondFilterId = fileProfile.getSecondFilterId();
            boolean isFilterExist = false;
            if (StringUtil.isNotEmpty(secondFilterTableName)
                    && secondFilterId > 0)
            {
                isFilterExist = FilterHelper.isFilterExist(
                        secondFilterTableName, secondFilterId);
            }

            if (isFilterExist)
            {
                Output extractedOutPut = diplomat.getOutput();
                Iterator it = extractedOutPut.documentElementIterator();
                extractedOutPut.clearDocumentElements();
                String dataType = extractedOutPut.getDiplomatAttribute()
                        .getDataType();
                boolean isPO = IFormatNames.FORMAT_PO.equals(dataType);

                if (isPO)
                {
                    doSecondFilterForPO(extractedOutPut, it, diplomat,
                            fileProfile, String.valueOf(fileProfile.getId()),
                            secondFilterId, secondFilterTableName);
                }
                else
                {
                    doSecondFilterForJP(extractedOutPut, it, diplomat,
                            fileProfile, String.valueOf(fileProfile.getId()),
                            secondFilterId, secondFilterTableName);
                }

                // re-calculate the total word-count after secondary parsing.
                Iterator extractedIt = extractedOutPut
                        .documentElementIterator();
                int newWordCount = 0;
                while (extractedIt.hasNext())
                {
                    DocumentElement element3 = (DocumentElement) extractedIt
                            .next();
                    if (element3 instanceof TranslatableElement)
                    {
                        int wc = ((TranslatableElement) element3)
                                .getWordcount();
                        newWordCount += wc;
                    }
                }
                int originalTotalWC = extractedOutPut.getWordCount();
                // set the total word count to 0 first
                extractedOutPut.setTotalWordCount(-originalTotalWC);
                extractedOutPut.setTotalWordCount(newWordCount);

                gxml = DiplomatWriter.WriteXML(extractedOutPut);
            }
        }

        if (deleteFmd)
        {
            fmd.delete();
        }

        return gxml;
    }

    private void createAdditionalRules()
    {
        if (!StringUtil.isEmpty(m_hyperlinkIds))
        {
            // GBS-3054, for idml
            String hyperlinkRule = createRuleForIdmlHyperlinks();
            if (!StringUtil.isEmpty(hyperlinkRule))
            {
                StringBuilder rule = new StringBuilder(m_ruleFile);
                String rulesetStr = "<ruleset schema=\"stories\">";
                int index = m_ruleFile.indexOf(rulesetStr);
                if ("".equals(m_ruleFile) || index < 0)
                {
                    rule.append("<?xml version=\"1.0\"?>");
                    rule.append("\r\n");
                    rule.append("<schemarules>");
                    rule.append("\r\n");
                    rule.append("<ruleset schema=\"stories\">");
                    rule.append("\r\n");
                    rule.append(hyperlinkRule);
                    rule.append("\r\n");
                    rule.append("</ruleset>");
                    rule.append("\r\n");
                    rule.append("</schemarules>");
                }
                else
                {
                    rule.insert(index + rulesetStr.length(), hyperlinkRule);
                }
                m_ruleFile = rule.toString();
            }
        }
        else
        {
            String styleRule = createRuleForStyles(m_office_unChar,
                    m_office_unPara, m_office_internalChar, m_formatType);

            if (styleRule != null && !"".equals(styleRule))
            {
                StringBuffer rsb = new StringBuffer(m_ruleFile);
                String rulesetStr = "</ruleset>";
                int index_ruleset = m_ruleFile.lastIndexOf(rulesetStr);

                if ("".equals(m_ruleFile) || index_ruleset < 0)
                {
                    rsb.append("<?xml version=\"1.0\"?>");
                    rsb.append("\r\n");
                    rsb.append("<schemarules>");
                    rsb.append("\r\n");
                    rsb.append(styleRule);
                    rsb.append("\r\n");
                    rsb.append("</schemarules>");
                }
                else
                {
                    rsb.insert(index_ruleset + rulesetStr.length(), styleRule);
                }
                m_ruleFile = rsb.toString();
            }
        }
    }

    /**
     * Apply secondary filter.
     */
    private void doSecondFilterForJP(Output extractedOutPut, Iterator it,
            DiplomatAPI diplomat, FileProfileImpl fp, String fpId,
            long secondFilterId, String secondFilterTableName)
            throws ExtractorException, DiplomatWordCounterException,
            DiplomatSegmenterException, ExtractorRegistryException, Exception
    {
        while (it.hasNext())
        {
            DocumentElement element = (DocumentElement) it.next();
            if (element instanceof TranslatableElement)
            {
                ArrayList segments = ((TranslatableElement) element)
                        .getSegments();
                if (segments != null && segments.size() > 0)
                {
                    for (int i = 0, max = segments.size(); i < max; i++)
                    {
                        diplomat.setIsSecondFilter(true);
                        // boolean needDecodeTwice = false;
                        diplomat.resetForChainFilter();

                        // Not from aligner.
                        if (fp != null)
                        {
                            diplomat.setFileProfile(fp);
                            diplomat.setFileProfileId(fpId);
                            diplomat.setFilterId(secondFilterId);
                            diplomat.setFilterTableName(secondFilterTableName);
                        }

                        String inputFormatName = getInputFormatName(secondFilterTableName);
                        diplomat.setInputFormat(inputFormatName);

                        SegmentNode node = (SegmentNode) segments.get(i);
                        XmlEntities xe = new XmlEntities();
                        String temp = node.getSegment();
                        boolean hasLtGt = temp.contains("&lt;")
                                || temp.contains("&gt;");
                        // protect "<" and ">" to ensure html filter will work
                        temp = temp.replace("&amp;lt;", "_leftAmpLt_");
                        temp = temp.replace("&amp;gt;", "_rightAmpGt_");
                        temp = temp.replace("&amp;quot;", "_ampQuot_");
                        temp = temp.replace("&amp;apos;", "_ampApos_");
                        // GBS-3906
                        temp = temp.replace("&amp;", m_tag_amp);
                        temp = temp.replace("_ampApos_", "&amp;apos;");
                        temp = temp.replace("_ampQuot_", "&amp;quot;");
                        temp = temp.replace("_rightAmpGt_", "&amp;gt;");
                        temp = temp.replace("_leftAmpLt_", "&amp;lt;");
                        List<String> internalTexts = new ArrayList<String>();
                        temp = InternalTextHelper.protectInternalTexts(temp,
                                internalTexts);
                        String segmentValue = xe.decodeStringBasic(temp);
                        // decode TWICE to make sure secondary parser can work
                        // as expected
                        if (segmentValue.indexOf("&") > -1)
                        {
                            segmentValue = xe.decodeStringBasic(segmentValue);
                        }

                        segmentValue = InternalTextHelper.restoreInternalTexts(
                                segmentValue, internalTexts);

                        if (inputFormatName != null
                                && inputFormatName.equals("html"))
                        {
                            segmentValue = checkHtmlTags(segmentValue);
                        }

                        diplomat.setSourceString(segmentValue);

                        if (m_logger.isDebugEnabled())
                        {
                            m_logger.info("Before extracted string : "
                                    + segmentValue);
                        }
                        // extract this segment
                        diplomat.setExtractor(null);
                        try
                        {
                            String str = diplomat.extract();
                            if (m_logger.isDebugEnabled())
                            {
                                m_logger.info("After extracted string : " + str);
                            }
                        }
                        catch (Exception ex)
                        {
                            TranslatableElement newElement = new TranslatableElement();
                            String text = node.getSegment();
                            text = text.replace(m_tag_amp, "&amp;");
                            node.setSegment(text);
                            newElement.addSegment(node);
                            extractedOutPut.addDocumentElement(newElement);
                            continue;
                        }

                        Output _output = diplomat.getOutput();
                        Iterator it2 = _output.documentElementIterator();
                        while (it2.hasNext())
                        {
                            DocumentElement element2 = (DocumentElement) it2
                                    .next();
                            if (element2 instanceof SkeletonElement)
                            {
                                String text = ((SkeletonElement) element2)
                                        .getSkeleton();
                                // fixing for GBS-1043
                                if (!hasLtGt)
                                {
                                    text = xe.encodeStringBasic(text);
                                }
                                text = text.replace(m_tag_amp, "&amp;");
                                ((SkeletonElement) element2).setSkeleton(text);
                            }
                            else if (element2 instanceof LocalizableElement)
                            {
                                String text = ((LocalizableElement) element2)
                                        .getChunk();
                                text = xe.encodeStringBasic(text);
                                ((LocalizableElement) element2).setChunk(text);
                            }
                            else if (element2 instanceof TranslatableElement)
                            {
                                List segs = ((TranslatableElement) element2)
                                        .getSegments();
                                String text;
                                for (int j = 0; j < segs.size(); j++)
                                {
                                    SegmentNode sn = (SegmentNode) segs.get(j);
                                    text = sn.getSegment();
                                    text = text.replace(m_tag_amp, "&amp;");
                                    sn.setSegment(text);
                                }
                            }
                            extractedOutPut.addDocumentElement(element2);
                        }

                        diplomat.setIsSecondFilter(false);
                    }
                }
            }
            else
            {
                extractedOutPut.addDocumentElement(element);
            }
        }
    }

    /**
     * Apply secondary filter for PO File. All HTML tags should be protected. 1)
     * If the target (msgstr) is empty or same with source (msgid), the data
     * will be parsed by the Secondary Extractor. 2) If the target (msgstr) is
     * different with source (msgid), the data will be parsed by
     * SegmentUtil.replaceHtmltagWithPH. 3) If the data is invalid for the
     * Secondary Extractor (HTML/XML Extractor), the data will be parsed by
     * SegmentUtil.replaceHtmltagWithPH.
     */
    @SuppressWarnings("rawtypes")
    private void doSecondFilterForPO(Output p_extractedOutPut, Iterator p_it,
            DiplomatAPI p_diplomat, FileProfileImpl p_fp, String p_fpId,
            long p_secondFilterId, String p_secondFilterTableName)
            throws ExtractorException, DiplomatWordCounterException,
            DiplomatSegmenterException, ExtractorRegistryException, Exception
    {
        String inputFormatName = getInputFormatName(p_secondFilterTableName);
        boolean isXML = FilterConstants.XMLRULE_TABLENAME
                .equals(p_secondFilterTableName);
        boolean isHTML = FilterConstants.HTML_TABLENAME
                .equals(p_secondFilterTableName);

        ArrayList segSource = new ArrayList();
        ArrayList segTarget = new ArrayList();
        TranslatableElement elemSource = new TranslatableElement();
        TranslatableElement elemTarget = new TranslatableElement();
        String xliffpart;
        XmlEntities xe = new XmlEntities();
        DiplomatWordCounter wc = new DiplomatWordCounter();
        while (p_it.hasNext())
        {
            DocumentElement element = (DocumentElement) p_it.next();
            if (element instanceof TranslatableElement)
            {
                ArrayList segments = ((TranslatableElement) element)
                        .getSegments();
                xliffpart = ((TranslatableElement) element)
                        .getXliffPartByName();
                if (xliffpart.equals("source"))
                {
                    segSource = segments;
                    elemSource = (TranslatableElement) element;
                    continue;
                }
                else if (xliffpart.equals("target"))
                {
                    segTarget = segments;
                    elemTarget = (TranslatableElement) element;
                }

                // It is always true as we have no way to know if "msgstr" is
                // for wanted language.
                boolean needSecondaryFilter = true;
                SegmentNode sn;
                String seg;
                if (segSource != null && segSource.size() > 0)
                {
                    // Modify Segment for HTML/XML Filter
                    for (int i = 0, max = segSource.size(); i < max; i++)
                    {
                        sn = (SegmentNode) segSource.get(i);
                        seg = sn.getSegment();
                        if (isXML && !checkIfXMLIsWellFormed(seg))
                        {
                            seg = seg.replace("&amp;", m_tag_amp);
                            seg = m_tag_start + seg + m_tag_end;
                            if (checkIfXMLIsWellFormed(seg))
                            {
                                sn.setSegment(seg);
                            }
                        }
                        else if (isHTML)
                        {
                            sn.setSegment(seg);
                        }
                    }

                    for (int i = 0, max = segSource.size(); i < max; i++)
                    {
                        p_diplomat.resetForChainFilter();
                        if (p_fp != null)
                        {
                            p_diplomat.setFileProfile(p_fp);
                            p_diplomat.setFileProfileId(p_fpId);
                            p_diplomat.setFilterId(p_secondFilterId);
                            p_diplomat
                                    .setFilterTableName(p_secondFilterTableName);
                        }
                        p_diplomat.setInputFormat(inputFormatName);

                        SegmentNode node = (SegmentNode) segSource.get(i);
                        String segmentValue = node.getSegment();
                        if ("html".equalsIgnoreCase(inputFormatName))
                        {
                            segmentValue = xe.decodeStringBasic(segmentValue);
                            // decode TWICE to make sure secondary parser can
                            // work as expected, but it will result in an entity
                            // issue, seems it can't be resolved in current
                            // framework of GS.
                            if (segmentValue.indexOf("&") > -1)
                            {
                                segmentValue = xe
                                        .decodeStringBasic(segmentValue);
                            }
                            segmentValue = checkHtmlTags(segmentValue);
                        }
                        p_diplomat.setSourceString(segmentValue);
                        p_diplomat.setExtractor(null);
                        // extract this segment
                        try
                        {
                            p_diplomat.extract();
                        }
                        catch (Exception e)
                        {
                            // Protected the data with PlaceHolder, and
                            // recount to reduce word-count number.
                            SegmentNode node01 = (SegmentNode) segSource.get(i);
                            String text = node01.getSegment();
                            text = text.replace(m_tag_amp, "&amp;");
                            text = text.replace(m_tag_start, "").replace(
                                    m_tag_end, "");
                            node01.setSegment(text);
                            if ("html".equalsIgnoreCase(inputFormatName))
                            {
                                SegmentUtil.replaceHtmltagWithPH(node01);
                                SegmentNode node02 = (SegmentNode) segTarget
                                        .get(i);
                                SegmentUtil.replaceHtmltagWithPH(node02);
                            }
                            wc.countDocumentElement(elemSource,
                                    p_extractedOutPut);
                            wc.countDocumentElement(elemTarget,
                                    p_extractedOutPut);

                            p_extractedOutPut.addDocumentElement(elemSource);
                            p_extractedOutPut.addDocumentElement(elemTarget);
                            break;
                        }

                        Output _output = p_diplomat.getOutput();
                        if (needSecondaryFilter)
                        {
                            Iterator it2 = _output.documentElementIterator();
                            while (it2.hasNext())
                            {
                                DocumentElement element2 = (DocumentElement) it2
                                        .next();
                                if (element2 instanceof SkeletonElement)
                                {
                                    String text = ((SkeletonElement) element2)
                                            .getSkeleton();
                                    if (m_tag_start.equals(text)
                                            || m_tag_end.equals(text))
                                        continue;
                                    if (isXML && text.startsWith(m_tag_start))
                                    {
                                        text = text.substring(m_tag_start
                                                .length());
                                    }
                                    else if (isXML && text.endsWith(m_tag_end))
                                    {
                                        text = text.substring(0, text.length()
                                                - m_tag_end.length());
                                    }

                                    text = text.replace(m_tag_amp, "&amp;");

                                    ((SkeletonElement) element2)
                                            .setSkeleton(text);
                                }
                                else if (element2 instanceof LocalizableElement)
                                {
                                    String text = ((LocalizableElement) element2)
                                            .getChunk();
                                    text = xe.encodeStringBasic(text);
                                    ((LocalizableElement) element2)
                                            .setChunk(text);
                                }
                                else if (element2 instanceof TranslatableElement)
                                {
                                    List segs = ((TranslatableElement) element2)
                                            .getSegments();
                                    String text;
                                    for (int j = 0; j < segs.size(); j++)
                                    {
                                        sn = (SegmentNode) segs.get(j);
                                        text = sn.getSegment();
                                        text = text.replace(m_tag_amp, "&amp;");
                                        sn.setSegment(text);
                                    }
                                }
                                p_extractedOutPut.addDocumentElement(element2);
                            }
                        }
                        else
                        {
                            // Protected the data with PlaceHolder, and recount
                            // source word count comes from HTML/XML filter
                            SegmentNode node01 = (SegmentNode) segSource.get(0);
                            String text = node01.getSegment();
                            text = text.replace(m_tag_amp, "&amp;");
                            if (isXML && text.startsWith(m_tag_start)
                                    && text.endsWith(m_tag_end))
                            {
                                text = text.substring(m_tag_start.length(),
                                        text.length() - m_tag_end.length());
                            }
                            node01.setSegment(text);
                            SegmentUtil.replaceHtmltagWithPH(node01);
                            SegmentNode node02 = (SegmentNode) segTarget.get(0);
                            SegmentUtil.replaceHtmltagWithPH(node02);

                            node01.setWordCount(_output.getWordCount());
                            wc.countDocumentElement(elemTarget,
                                    p_extractedOutPut);

                            p_extractedOutPut.addDocumentElement(elemSource);
                            p_extractedOutPut.addDocumentElement(elemTarget);
                            break;
                        }
                    }
                }
            }
            else
            {
                p_extractedOutPut.addDocumentElement(element);
            }
        }
    }

    /**
     * The XML data must be well-formed, otherwise SAX will throw exception.
     */
    private boolean checkIfXMLIsWellFormed(String p_xmlData)
    {
        boolean result = false;

        String xmlData = p_xmlData;
        if (xmlData.startsWith("&lt;") && xmlData.endsWith("&gt;"))
        {
            xmlData = xmlData.replace("&lt;", "<").replace("&gt;", ">");
        }

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            InputSource source = new InputSource(new ByteArrayInputStream(
                    xmlData.getBytes()));
            factory.newDocumentBuilder().parse(source);

            result = true;
        }
        catch (Exception e)
        {
        }

        return result;
    }

    private String createRuleForStyles(String unCharStyles,
            String unParaStyles, String internalCharStyles, String formatType)
    {
        if (DiplomatAPI.FORMAT_OPENOFFICE_XML.equals(formatType))
        {
            return createRuleForOOStyles(unCharStyles, unParaStyles);
        }
        else if (DiplomatAPI.FORMAT_OFFICE_XML.equals(formatType))
        {
            String styleRule = createRuleForOfficeStyles(unCharStyles,
                    unParaStyles, internalCharStyles);
            String numRule = createRuleForExcelNumber();
            String headerFooterRule = createRuleForHeaderFooter();
            String toolTipsRule = createRuleForToolTips();
            String unextractableExcelCellStyleTextRule = createRuleForUnextractableExcelCell();
            String excelInternalTextCellStyleRule = createRuleForExcelInternalTextCellStyles();
            String tableOfContentRule = createRuleForTableOfContent();

            StringBuffer result = new StringBuffer();
            result.append(styleRule != null ? styleRule : "");
            result.append(numRule != null ? numRule : "");
            result.append(headerFooterRule != null ? headerFooterRule : "");
            result.append(toolTipsRule != null ? toolTipsRule : "");
            result.append(tableOfContentRule != null ? tableOfContentRule : "");
            result.append(unextractableExcelCellStyleTextRule != null ? unextractableExcelCellStyleTextRule
                    : "");
            result.append(excelInternalTextCellStyleRule != null ? excelInternalTextCellStyleRule
                    : "");

            return result.toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Creates a translate rule for extracting idml hyperlinks.
     */
    private String createRuleForIdmlHyperlinks()
    {
        StringBuilder rule = new StringBuilder();

        StringTokenizer st = new StringTokenizer(m_hyperlinkIds, ",");
        while (st.hasMoreElements())
        {
            String id = st.nextToken();
            rule.append("<translate path='//*[local-name()=\"HyperlinkURLDestination\"][@DestinationUniqueKey=\""
                    + id + "\"]/@DestinationURL' />");
            if (st.hasMoreElements())
            {
                rule.append("\r\n");
            }
        }

        return rule.toString();
    }

    private String createRuleForOfficeStyles(String unCharStyles,
            String unParaStyles, String internalCharStyles)
    {
        String styleRule = null;

        List<String> unchar = MSOffice2010Filter.toList(unCharStyles);
        List<String> unpara = MSOffice2010Filter.toList(unParaStyles);
        List<String> internalChar = MSOffice2010Filter
                .toList(internalCharStyles);

        boolean added = false;
        StringBuffer styleSB = new StringBuffer();

        for (String style : unchar)
        {
            added = true;
            styleSB.append("<dont-translate path='//w:r/w:rPr/w:rStyle[@w:val=\""
                    + style + "\"]/../..' />");
            styleSB.append("\r\n");
            styleSB.append("<dont-translate path='//w:r/w:rPr/w:rStyle[@w:val=\""
                    + style + "\"]/../..//*' />");
            styleSB.append("\r\n");
        }

        for (String style : unpara)
        {
            added = true;
            styleSB.append("<dont-translate path='//w:p/w:pPr/w:pStyle[@w:val=\""
                    + style + "\"]/../..' />");
            styleSB.append("\r\n");
            styleSB.append("<dont-translate path='//w:p/w:pPr/w:pStyle[@w:val=\""
                    + style + "\"]/../..//*' />");
            styleSB.append("\r\n");
        }

        for (String style : internalChar)
        {
            added = true;
            styleSB.append("<internal path='//w:r/w:rPr/w:rStyle[@w:val=\""
                    + style + "\"]/../..' />");
            styleSB.append("\r\n");
            styleSB.append("<internal path='//w:r/w:rPr/w:rStyle[@w:val=\""
                    + style + "\"]/../../w:t' />");
            styleSB.append("\r\n");
        }

        if (added)
        {
            StringBuffer allStyleSB = new StringBuffer();
            allStyleSB.append("\r\n");
            allStyleSB.append("<ruleset schema=\"w:document\">");
            allStyleSB.append("\r\n");
            allStyleSB.append(styleSB);
            allStyleSB.append("</ruleset>");
            allStyleSB.append("\r\n");

            allStyleSB.append("\r\n");
            allStyleSB.append("<ruleset schema=\"w:hdr\">");
            allStyleSB.append("\r\n");
            allStyleSB.append(styleSB);
            allStyleSB.append("</ruleset>");
            allStyleSB.append("\r\n");

            allStyleSB.append("\r\n");
            allStyleSB.append("<ruleset schema=\"w:ftr\">");
            allStyleSB.append("\r\n");
            allStyleSB.append(styleSB);
            allStyleSB.append("</ruleset>");
            allStyleSB.append("\r\n");

            styleRule = allStyleSB.toString();
        }
        return styleRule;
    }

    private String createRuleForExcelNumber()
    {
        String styleRule = null;
        boolean added = false;
        StringBuffer styleSB = new StringBuffer();
        styleSB.append("\r\n");
        styleSB.append("<ruleset schema=\"worksheet\">");
        styleSB.append("\r\n");

        List<String> ids = MSOffice2010Filter.toList(m_xlsx_numStyleIds);

        for (String style : ids)
        {
            added = true;
            styleSB.append("<dont-translate path='//*[local-name()=\"c\"][@s=\""
                    + style + "\"]' />");
            styleSB.append("\r\n");
            styleSB.append("<dont-translate path='//*[local-name()=\"c\"][@s=\""
                    + style + "\"]//*' />");
            styleSB.append("\r\n");
        }

        styleSB.append("</ruleset>");
        styleSB.append("\r\n");

        if (added)
        {
            styleRule = styleSB.toString();
        }
        return styleRule;
    }

    // private String createRuleForExcelHiddenSheetCell()
    // {
    // String styleRule = null;
    // boolean added = false;
    // StringBuffer styleSB = new StringBuffer();
    // styleSB.append("\r\n");
    // styleSB.append("<ruleset schema=\"worksheet\">");
    // styleSB.append("\r\n");
    //
    // List<String> ids = MSOffice2010Filter.toList(m_xlsx_sheetHiddenCell);
    //
    // for (String id : ids)
    // {
    // added = true;
    // styleSB.append("<dont-translate path='//*[local-name()=\"c\"][@r=\""
    // + id + "\"]' />");
    // styleSB.append("\r\n");
    // styleSB.append("<dont-translate path='//*[local-name()=\"c\"][@r=\""
    // + id + "\"]//*' />");
    // styleSB.append("\r\n");
    // }
    //
    // styleSB.append("</ruleset>");
    // styleSB.append("\r\n");
    //
    // if (added)
    // {
    // styleRule = styleSB.toString();
    // }
    // return styleRule;
    // }

    // private String createRuleForExcelHiddenSharedXml()
    // {
    // String styleRule = null;
    // boolean added = false;
    // StringBuffer styleSB = new StringBuffer();
    // styleSB.append("\r\n");
    // styleSB.append("<ruleset schema=\"sst\">");
    // styleSB.append("\r\n");
    //
    // List<String> ids = MSOffice2010Filter.toList(m_xlsx_hiddenSharedSI);
    //
    // if (ids.size() > 0)
    // {
    // String s = ids.toString();
    // s = StringUtil.replace(s, ",", "|");
    // styleSB.append("<dont-translate path='//*[local-name()=\"si\"]"
    // + s + "' />");
    // styleSB.append("\r\n");
    // styleSB.append("<dont-translate path='//*[local-name()=\"si\"]"
    // + s + "//*' />");
    // styleSB.append("\r\n");
    // }
    //
    //
    // styleSB.append("</ruleset>");
    // styleSB.append("\r\n");
    //
    // if (added)
    // {
    // styleRule = styleSB.toString();
    // }
    // return styleRule;
    // }

    /**
     * Creates rules for the texts with unextractable cell style in excel 2010
     * documents.
     * <p>
     * for GBS-2618
     */
    private String createRuleForUnextractableExcelCell()
    {
        String styleRule = null;
        boolean added = false;
        StringBuffer styleSB = new StringBuffer();
        styleSB.append("\r\n");
        styleSB.append("<ruleset schema=\"sst\">");
        styleSB.append("\r\n");

        List<String> ids = MSOffice2010Filter
                .toList(m_xlsx_unextractableCellStyles);

        for (String idstr : ids)
        {
            int id = Integer.parseInt(idstr) + 1;
            added = true;
            styleSB.append("<dont-translate path='//*[local-name()=\"si\"]["
                    + id + "]' />");
            styleSB.append("\r\n");
            styleSB.append("<dont-translate path='//*[local-name()=\"si\"]["
                    + id + "]//*' />");
            styleSB.append("\r\n");
        }

        styleSB.append("</ruleset>");
        styleSB.append("\r\n");

        if (added)
        {
            styleRule = styleSB.toString();
        }
        return styleRule;
    }

    /**
     * Creates rules for the texts with Excel Internal Text Cell Styles in excel
     * 2010 documents.
     * 
     * @since GBS-3944
     */
    private String createRuleForExcelInternalTextCellStyles()
    {
        String styleRule = null;
        boolean added = false;
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n");
        sb.append("<ruleset schema=\"sst\">");
        sb.append("\r\n");

        List<String> ids = MSOffice2010Filter
                .toList(m_xlsx_excelInternalTextCellStyles);

        for (String idstr : ids)
        {
            int id = Integer.parseInt(idstr) + 1;
            added = true;
            sb.append("<internal path='//*[local-name()=\"si\"][" + id
                    + "]/*[local-name()=\"t\"]' />");
            sb.append("\r\n");
        }

        sb.append("</ruleset>");
        sb.append("\r\n");

        if (added)
        {
            styleRule = sb.toString();
        }
        return styleRule;
    }

    private String createRuleForOOStyles(String unCharStyles,
            String unParaStyles)
    {
        String ooStyleRule = null;
        boolean added = false;
        StringBuffer ooStyle = new StringBuffer();
        ooStyle.append("\r\n");
        ooStyle.append("<ruleset schema=\"office:document-content\">");
        ooStyle.append("\r\n");

        List<String> unchar = OpenOfficeFilter.toList(unCharStyles);
        List<String> unpara = OpenOfficeFilter.toList(unParaStyles);

        for (String style : unchar)
        {
            added = true;
            ooStyle.append("<dont-translate path='//text:span[@text:style-name=\""
                    + style + "\"]'/>");
            ooStyle.append("\r\n");
            ooStyle.append("<dont-translate path='//text:span[@text:style-name=\""
                    + style + "\"]//*'/>");
            ooStyle.append("\r\n");
            ooStyle.append("<dont-translate path='//table:table-cell[@table:style-name=\""
                    + style + "\"]'/>");
            ooStyle.append("\r\n");
            ooStyle.append("<dont-translate path='//table:table-cell[@table:style-name=\""
                    + style + "\"]//*'/>");
            ooStyle.append("\r\n");
        }

        for (String style : unpara)
        {
            added = true;
            ooStyle.append("<dont-translate path='//text:p[@text:style-name=\""
                    + style + "\"]'/>");
            ooStyle.append("\r\n");
            ooStyle.append("<dont-translate path='//text:p[@text:style-name=\""
                    + style + "\"]//*'/>");
            ooStyle.append("\r\n");
        }

        ooStyle.append("</ruleset>");
        ooStyle.append("\r\n");

        if (added)
        {
            ooStyleRule = ooStyle.toString();
        }
        return ooStyleRule;
    }

    /**
     * Creates rules for excel 2010 and ppt 2010 not to extract header and
     * footer.
     * <p>
     * for GBS-2476
     */
    private String createRuleForHeaderFooter()
    {
        StringBuffer rule = new StringBuffer();
        if (m_isHeaderFooterTranslate != null
                && !Boolean.parseBoolean(m_isHeaderFooterTranslate))
        {
            // this is rule for excel header&footer
            rule.append("\r\n");
            rule.append("<ruleset schema=\"worksheet\">");
            rule.append("\r\n");
            rule.append("<dont-translate path='//*[local-name()=\"headerFooter\"]//*' inline=\"no\" priority=\"9\"/>");
            rule.append("\r\n");
            rule.append("</ruleset>");
            rule.append("\r\n");
            // this is rule for ppt header&footer
            rule.append("\r\n");
            rule.append("<ruleset schema=\"p:sld\">");
            rule.append("\r\n");
            rule.append("<dont-translate path='//p:cNvPr[starts-with(@name,\"Footer\")]/ancestor::p:sp//a:r' inline=\"no\" priority=\"9\"/>");
            rule.append("\r\n");
            rule.append("<dont-translate path='//p:cNvPr[starts-with(@name,\"Footer\")]/ancestor::p:sp//a:r//*' inline=\"no\" priority=\"9\"/>");
            rule.append("\r\n");
            rule.append("</ruleset>");
            rule.append("\r\n");
        }

        return rule.toString();
    }

    /**
     * Creates rules for docx 2010, excel 2010 and ppt 2010 to extract tool
     * tips.
     * <p>
     * for GBS-2439
     */
    private String createRuleForToolTips()
    {
        StringBuffer rule = new StringBuffer();
        if (m_isToolTipsTranslate != null
                && Boolean.parseBoolean(m_isToolTipsTranslate))
        {
            // this is rule for docx tool tip
            rule.append("\r\n");
            rule.append("<ruleset schema=\"w:document\">");
            rule.append("\r\n");
            rule.append("<translate path='//wp:docPr/@descr'/>");
            rule.append("\r\n");
            rule.append("\r\n");
            rule.append("<translate path='//w:hyperlink/@w:tooltip'/>");
            rule.append("\r\n");
            rule.append("</ruleset>");
            rule.append("\r\n");
            // this is rule for excel tool tip
            rule.append("\r\n");
            rule.append("<ruleset schema=\"xdr:wsDr\">");
            rule.append("\r\n");
            rule.append("<translate path='//xdr:cNvPr/@descr'/>");
            rule.append("\r\n");
            rule.append("</ruleset>");
            rule.append("\r\n");
            // this is rule for ppt tool tip
            rule.append("\r\n");
            rule.append("<ruleset schema=\"p:sld\">");
            rule.append("\r\n");
            rule.append("<translate path='//p:cNvPr/@descr'/>");
            rule.append("\r\n");
            rule.append("</ruleset>");
            rule.append("\r\n");
        }

        return rule.toString();
    }

    /**
     * Creates rules for docx 2010 not to extract table of content.
     */
    private String createRuleForTableOfContent()
    {
        StringBuffer rule = new StringBuffer();
        if (m_isTableOfContentTranslate == null
                || !Boolean.parseBoolean(m_isTableOfContentTranslate))
        {
            // this is rule for docx table of content
            rule.append("\r\n");
            rule.append("<ruleset schema=\"w:document\">");
            rule.append("\r\n");
            rule.append("<dont-translate path='//w:hyperlink[starts-with(@w:anchor, \"_Toc\")]/w:r'/>");
            rule.append("\r\n");
            rule.append("<dont-translate path='//w:hyperlink[starts-with(@w:anchor, \"_Toc\")]/w:r//*'/>");
            rule.append("\r\n");
            rule.append("</ruleset>");
            rule.append("\r\n");
        }
        return rule.toString();
    }

    /**
     * Prepares a PrsXml object for extracting the given content, and then does
     * the extraction.
     * 
     * @return GXML As String
     */
    private String extractWithPrsXmlExtractor() throws Exception
    {
        Connection connection = null;

        try
        {
            connection = ConnectionPool.getConnection();
            StringTokenizer st = new StringTokenizer(m_locale, "_");
            String language = st.nextToken();
            String country = st.nextToken();
            Locale theLocale = new Locale(language, country);

            EFInputData input = new EFInputData();
            input.setLocale(theLocale);
            input.setUnicodeInput(readXmlFromMessageData());
            Output output = new Output();
            AbstractExtractor extractor = new com.globalsight.ling.docproc.extractor.paginated.Extractor(
                    connection);
            extractor.init(input, output);
            extractor.loadRules();
            extractor.extract();
            return ((com.globalsight.ling.docproc.extractor.paginated.Extractor) extractor)
                    .getDiplomatizedXml();
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException cpe)
            {
            }
        }
    }

    private String noNull(String s)
    {
        if (s == null)
            return "";

        return s;
    }

    /**
     * Gets needed values from the event flow xml
     * 
     * @exception LingAdapterException
     */
    private void parseEventFlowXml() throws LingAdapterException
    {
        EventFlowXml eventFlowObject = m_cxeMessage.getEventFlowObject();
        StringReader sr = null;
        try
        {
            Source s = eventFlowObject.getSource();
            // get format type
            m_formatType = s.getFormatType();
            // take rtf format as word format to convert.
            if (m_formatType.equals(IFormatNames.FORMAT_RTF))
            {
                m_formatType = IFormatNames.FORMAT_WORD_HTML;
            }

            // get format name, set it empty if can not get
            try
            {
                m_formatName = s.getFormatName();
            }
            catch (Exception e)
            {
                // ignore this exception
            }
            if (null == m_formatName)
            {
                m_formatName = "";
            }

            // get file profile id
            m_fileProfile = s.getDataSourceId();
            m_locale = s.getLocale();
            m_encoding = s.getCharset();

            // Get the display name to use when logging an extractor error.
            m_displayName = eventFlowObject.getDisplayName();
            m_errorArgs[0] = "Extractor";
            // the filename is the first arg to the error messages
            m_errorArgs[1] = m_displayName;

            // For PPT issue
            if (DiplomatAPI.isMsOfficePowerPointFormat(m_formatType))
            {
                m_bullets_MsOffice = eventFlowObject.getValue("css_bullet");
            }

            // For GBS-3054, idml is actually xml format
            if (IFormatNames.FORMAT_XML.equals(m_formatType))
            {
                m_hyperlinkIds = eventFlowObject.getValue("hyperlinkIds");
            }

            if (DiplomatAPI.isOpenOfficeFormat(m_formatType)
                    || DiplomatAPI.isOfficeXmlFormat(m_formatType))
            {
                m_office_unPara = noNull(eventFlowObject
                        .getValue("unParaStyles"));
                m_office_unChar = noNull(eventFlowObject
                        .getValue("unCharStyles"));
                m_office_internalChar = noNull(eventFlowObject
                        .getValue("internalCharStyles"));

                m_xlsx_numStyleIds = noNull(eventFlowObject
                        .getValue("numStyleIds"));
                m_xlsx_hiddenSharedSI = noNull(eventFlowObject
                        .getValue("hiddenSharedSI"));
                m_xlsx_sheetHiddenCell = noNull(eventFlowObject
                        .getValue("sheetHiddenCell"));
                m_xlsx_unextractableCellStyles = noNull(eventFlowObject
                        .getValue("unextractableExcelCellStyles"));
                m_xlsx_excelInternalTextCellStyles = noNull(eventFlowObject
                        .getValue("excelInternalTextCellStyles"));
                m_isHeaderFooterTranslate = noNull(eventFlowObject
                        .getValue("isHeaderFooterTranslate"));
                m_isToolTipsTranslate = noNull(eventFlowObject
                        .getValue("isToolTipsTranslate"));
                m_isHiddenTextTranslate = noNull(eventFlowObject
                        .getValue("isHiddenTextTranslate"));
                m_isTableOfContentTranslate = noNull(eventFlowObject
                        .getValue("isTableOfContentTranslate"));
            }

            m_docPageCount = eventFlowObject.getBatchInfo().getDocPageCount();
            m_docPageNum = eventFlowObject.getBatchInfo().getDocPageNumber();
        }
        catch (Exception e)
        {
            m_logger.error(
                    "Unable to parse EventFlowXml. Cannot determine locale, "
                            + "encoding, and format_type for extraction.", e);
            throw new LingAdapterException("CxeInternal", m_errorArgs, e);
        }
        finally
        {
            if (sr != null)
            {
                sr.close();
            }
        }
    }

    /**
     * Queries the rule file associated with the file profile out of the DB.
     * 
     * @exception LingAdapterException
     */
    private void queryRuleFile() throws LingAdapterException
    {
        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        try
        {
            // Retrieve the (XML) Rule File from the Database.
            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(SQL_SELECT_RULE);
            query.setString(1, m_fileProfile);
            results = query.executeQuery();

            if (results.next())
            {
                m_ruleFile = results.getString(1);
                Logger.writeDebugFile("ruleFile.xml", m_ruleFile);
            }
            else
            {
                if (m_formatName != null
                        && m_formatName.equalsIgnoreCase("resx"))
                {
                    try
                    {
                        m_ruleFile = FileUtils
                                .read(StandardExtractor.class
                                        .getResourceAsStream("/properties/ResxRule.properties"));
                    }
                    catch (Exception e)
                    {
                        m_ruleFile = "";
                    }
                }
                else
                {
                    m_ruleFile = null;
                }
            }
            doPostQueryRule(connection);
        }
        catch (ConnectionPoolException cpe)
        {
            m_logger.error(
                    "Unable to connect to database retrieve XML rule file"
                            + " for FileProfileID " + m_fileProfile, cpe);
            throw new LingAdapterException("DbConnection", m_errorArgs, cpe);
        }
        catch (SQLException sqle)
        {
            m_logger.error(
                    "Unable to retrieve XML rule file for FileProfileID "
                            + m_fileProfile, sqle);
            throw new LingAdapterException("SqlException", m_errorArgs, sqle);
        }
        finally
        {
            ConnectionPool.silentClose(results);
            ConnectionPool.silentClose(query);
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException cpe)
            {
            }
        }
    }

    /**
     * just for indd
     * 
     * @param connection
     * @throws LingAdapterException
     */
    private void doPostQueryRule(Connection connection)
            throws LingAdapterException
    {
        PreparedStatement query = null;
        ResultSet rs = null;
        try
        {
            query = connection.prepareStatement(QUERY_FOR_FORMAT);
            query.setString(1, m_fileProfile);
            rs = query.executeQuery();
            if (rs.next())
            {
                String rs1 = rs.getString(1);
                String event = rs.getString(2);

                if (InddRuleHelper.isIndd(rs1))
                {
                    if (m_docPageCount >= 2
                            && m_displayName
                                    .startsWith(AdobeHelper.XMP_DISPLAY_NAME_PREFIX))
                        m_ruleFile = InddRuleHelper.loadAdobeXmpRule();
                    else
                        m_ruleFile = InddRuleHelper.loadRule();
                }
                else if (OpenOfficeRuleHelper.isOpenOffice(rs1))
                {
                    if (m_docPageCount >= 2
                            && m_displayName
                                    .startsWith(OpenOfficeHelper.OO_HEADER_DISPLAY_NAME_PREFIX))
                        m_ruleFile = OpenOfficeRuleHelper.loadStylesRule();
                    else
                        m_ruleFile = OpenOfficeRuleHelper
                                .loadRule(m_displayName);
                }
                else if (OfficeXmlRuleHelper.isOfficeXml(rs1))
                {
                    m_ruleFile = OfficeXmlRuleHelper.loadRule(m_displayName,
                            m_docPageCount);
                }
                else if (IFormatNames.FORMAT_AUTHORIT_XML.equals(rs1))
                {
                    try
                    {
                        if (m_ruleFile != null)
                        {
                            StringBuilder rule = new StringBuilder(m_ruleFile);
                            String rulesetStr = "</ruleset>";
                            int index = m_ruleFile.indexOf(rulesetStr);
                            if ("".equals(m_ruleFile) || index < 0)
                            {
                                m_ruleFile = FileUtils
                                        .read(StandardExtractor.class
                                                .getResourceAsStream("/properties/AuthorITXmlRule.properties"));
                            }
                            else
                            {
                                String sidRile = "<sid path=\"//*[local-name()='Object']/*[local-name()='ID']\" root=\"Object\"/>";
                                rule.insert(index, sidRile);

                                m_ruleFile = rule.toString();
                            }
                        }
                        else
                        {
                            m_ruleFile = FileUtils
                                    .read(StandardExtractor.class
                                            .getResourceAsStream("/properties/AuthorITXmlRule.properties"));
                        }

                    }
                    catch (Exception e)
                    {
                        m_ruleFile = "";
                    }
                }
                else if (IdmlRuleHelper.isIdml(event))
                {
                    m_ruleFile = IdmlRuleHelper.loadRule(fileProfile);
                }
            }
        }
        catch (SQLException e)
        {
            m_logger.error("Unable to retrieve format_type  for FileProfileID "
                    + m_fileProfile, e);
            throw new LingAdapterException("SqlException", m_errorArgs, e);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(query);
        }
    }

    /**
     * Reads the content of the gxml or prsxml file and returns a String of XML
     * either GXML or PRSXML. This also deletes the file after reading its
     * contents
     * 
     * @param p_gxmlFileName
     *            filename containing the GXML (or PRSXML)
     * @exception IOException
     * @return String
     */
    private String readXmlFromMessageData() throws IOException
    {
        String s = MessageDataReader.readString(m_cxeMessage.getMessageData());
        m_cxeMessage.setDeleteMessageData(true);
        return s;
    }

    /**
     * Get its corresponding input format name by the filter table name.
     * 
     * If more filter is added in future, need add that in the list.
     * 
     * @param filterTableName
     * @return input format name
     */
    private String getInputFormatName(String filterTableName)
    {
        String inputFormatName = null;
        if (filterTableName != null)
        {
            filterTableName = filterTableName.trim();
            if ("html_filter".equalsIgnoreCase(filterTableName))
            {
                inputFormatName = IFormatNames.FORMAT_HTML;
            }
            else if ("java_properties_filter".equalsIgnoreCase(filterTableName))
            {
                inputFormatName = IFormatNames.FORMAT_JAVAPROP;
            }
            else if ("java_script_filter".equalsIgnoreCase(filterTableName))
            {
                inputFormatName = IFormatNames.FORMAT_JAVASCRIPT;
            }
            else if ("xml_rule_filter".equalsIgnoreCase(filterTableName))
            {
                inputFormatName = IFormatNames.FORMAT_XML;
            }
            else if ("jsp_filter".equalsIgnoreCase(filterTableName))
            {
                inputFormatName = IFormatNames.FORMAT_JSP;
            }
            else if ("ms_office_doc_filter".equalsIgnoreCase(filterTableName))
            {
                inputFormatName = IFormatNames.FORMAT_WORD_HTML;
            }
        }

        return inputFormatName;
    }

    /**
     * Check if the html snippet is valid. Commonly,for secondary
     * filter/parser,the input html content is snippet, maybe invalid,it will
     * cause parse error.To avoid this,encode the invalid "<" or ">" to entity.
     * 
     */
    public static String checkHtmlTags(String p_str)
    {
        if (p_str == null)
        {
            return null;
        }
        else
        {
            StringBuffer sb = new StringBuffer();
            StringBuffer sb_p = new StringBuffer(p_str);

            // replace comments into comments_timespan(index)
            int commentStartIndex = sb_p.indexOf("<!--");
            int commentEndIndex = sb_p.indexOf("-->", commentStartIndex);
            long timespan = (new java.util.Date()).getTime();
            String keyPre = "gshtmlcomments_" + timespan + "_";
            int index = 0;
            Map<String, String> comments = new HashMap<String, String>();
            while (commentStartIndex > -1 && commentEndIndex > -1)
            {
                String key = keyPre + index;
                String comment = sb_p.substring(commentStartIndex,
                        commentEndIndex + 3);
                comments.put(key, comment);
                sb_p.replace(commentStartIndex, commentEndIndex + 3, key);

                commentStartIndex = sb_p.indexOf("<!--");
                commentEndIndex = sb_p.indexOf("-->", commentStartIndex);
                index++;
            }
            p_str = sb_p.toString();

            HtmlEntities entities = new HtmlEntities();
            p_str = entities.decodeStringBasic(p_str);

            // check tags
            int ltIndex = p_str.indexOf("<");
            int gtIndex = p_str.indexOf(">");

            if (ltIndex == -1 && gtIndex == -1)
            {
                return restoreComments(p_str, comments);
            }
            while (ltIndex > -1 || gtIndex > -1)
            {
                String strA = "";
                // has "<", no ">"
                if (ltIndex > -1 && gtIndex == -1)
                {
                    p_str = p_str.replace("<", "&lt;");
                    sb.append(p_str);
                    ltIndex = -1;
                    gtIndex = -1;
                }
                // has ">", no "<"
                else if (ltIndex == -1 && gtIndex > -1)
                {
                    p_str = p_str.replace(">", "&gt;");
                    sb.append(p_str);
                    ltIndex = -1;
                    gtIndex = -1;
                }
                else if (ltIndex > -1 && gtIndex > -1)
                {
                    if (gtIndex < ltIndex)
                    {
                        strA = p_str.substring(0, gtIndex);
                        p_str = p_str.substring(gtIndex + 1);
                        sb.append(strA).append("&gt;");

                        ltIndex = p_str.indexOf("<");
                        gtIndex = p_str.indexOf(">");
                    }
                    else
                    {
                        strA = p_str.substring(0, gtIndex + 1);
                        p_str = p_str.substring(gtIndex + 1);

                        int left = strA.lastIndexOf("<");
                        String leftStr = strA.substring(0, left);
                        leftStr = leftStr.replace("<", "&lt;");
                        String rightStr = strA.substring(left);

                        // replace tag like < a> to <a>
                        if (rightStr.matches("<\\s+.+"))
                        {
                            rightStr = rightStr.replaceFirst("<\\s+", "<");
                        }

                        sb.append(leftStr).append(rightStr);

                        ltIndex = p_str.indexOf("<");
                        gtIndex = p_str.indexOf(">");

                        if (ltIndex == -1 && gtIndex == -1)
                        {
                            sb.append(p_str);
                        }
                    }
                }
            }
            return restoreComments(sb.toString(), comments);
        }
    }

    private static String restoreComments(String pStr,
            Map<String, String> comments)
    {
        if (comments == null || comments.isEmpty())
        {
            return pStr;
        }

        Set<String> keys = comments.keySet();
        for (String key : keys)
        {
            pStr = pStr.replace(key, comments.get(key));
        }

        return pStr;
    }

    /**
     * "indd", "inx" and "idml" should use original "Extractor" for XML-based
     * files.
     */
    private static Set<Long> getKnowFormatTypeIdsForIndd()
    {
        Set<Long> ids = new HashSet<Long>();

        ids.add(31L);// INDD (CS2)
        ids.add(32L);// Illustrator
        ids.add(36L);// INDD (CS3)
        ids.add(37L);// INX (CS2)
        ids.add(38L);// INX (CS3)
        ids.add(40L);// INDD (CS4)
        ids.add(47L);// INDD (CS5)
        ids.add(52L);// INDD (CS5.5)

        ids.add(46L);// InDesign Markup (IDML)

        return ids;
    }
}
