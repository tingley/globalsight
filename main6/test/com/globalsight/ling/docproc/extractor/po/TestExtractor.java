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
package com.globalsight.ling.docproc.extractor.po;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.cxe.adapter.ling.StandardExtractor;
import com.globalsight.cxe.adapter.ling.StandardExtractorTestHelper;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.LocalizableElement;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.Segmentable;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.extractor.BaseExtractorTestClass;
import com.globalsight.ling.docproc.extractor.SpecificFileFilter;
import com.globalsight.ling.docproc.extractor.html.HTMLFilterTestHelper;
import com.globalsight.util.ClassUtil;

public class TestExtractor extends BaseExtractorTestClass
{
    public static final String extension = "po";
    public final String UTF8 = "UTF-8";
    public String filterName = "Filter_";

    public String sourceRoot = null;
    public String answerRoot = null;
    public String roundtripRoot = null;

    public Extractor extractor;

    /**
     * The file list which need create answer/roundtrip files.
     */
    public List<String> newFileList = new ArrayList<String>();

    public String lineSep = System.getProperty("line.separator");

    @SuppressWarnings(
    { "unchecked" })
    @Before
    public void setUp()
    {
        // Sets some value for initial word count
        CompanyThreadLocal.getInstance().setIdValue(CompanyWrapper.SUPER_COMPANY_ID);
        SystemConfiguration dpsc = SystemConfiguration
                .getInstance("/properties/Wordcounter.properties");
        HashMap map = new HashMap();
        map.put("PROPERTIES/WORDCOUNTER.PROPERTIES", dpsc);
        SystemConfiguration.setDebugMap(map);

        initExtractor();

        sourceRoot = getResourcePath(TestExtractor.class, "source");
        answerRoot = getResourcePath(TestExtractor.class, "answers");
        roundtripRoot = getResourcePath(TestExtractor.class, "roundtrip");

        // Adds file name which need create answer/roundtrip files.
        // newFileList.add("sample.po");
        // newFileList.add("sample_FR.po");
        // newFileList.add("sample_DE.po");
        // newFileList.add("GBS-4467.po");
    }

    /**
     * Only test the extractor.
     */
    @Test
    public void testExtractor()
    {
        File source = new File(sourceRoot);
        File[] sourceFiles = source.listFiles(new SpecificFileFilter(extension));
        for (int i = 0; i < sourceFiles.length; i++)
        {
            try
            {
                // Read source files
                File sourceFile = sourceFiles[i];
                // extract source file
                Output output = doExtract(sourceFile, extractor, UTF8);
                // get translatable text content
                String answerContent = getTranslatableTextContent(output);

                String fileName = sourceFile.getName();
                String answerFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".txt";
                File answerFile = new File(answerRoot + File.separator + answerFileName);
                File tmpFile = new File(answerRoot + File.separator + answerFileName + ".tmp");

                // Generate Answer files
                if (newFileList != null && newFileList.contains(sourceFile.getName()))
                {
                    generateFile(answerFile, answerContent, UTF8);
                }

                if (!answerFile.exists())
                {
                    fail("The file compared to :" + answerFile.getName() + " doesn't exist");
                }

                // Generate files for compare
                generateFile(tmpFile, answerContent, UTF8);
                if (fileCompareNoCareEndLining(tmpFile, answerFile))
                {
                    tmpFile.delete();
                    // generate target file
                    File rountTipFile = new File(roundtripRoot + File.separator + fileName);
                    delTranslabaleSource(output);
                    String gxml = DiplomatWriter.WriteXML(output);
                    byte[] mergeResult = getTargetFileContent(gxml, UTF8);
                    String s = new String(mergeResult, UTF8);
                    generateFile(rountTipFile, s, UTF8);
                    Assert.assertTrue(rountTipFile.exists());
                }
                else
                {
                    fail("\n" + tmpFile + "\n and \n" + answerFile + " not equal");
                }
            }
            catch (Exception e)
            {
                fail(e.getMessage());
            }
        }
    }

    /**
     * Test the extractor and the filter.
     */
    @Test
    public void testExtractorWithFilter()
    {
        File source = new File(sourceRoot);
        File[] sourceFiles = source.listFiles(new SpecificFileFilter(extension));
        String answerContent = "There is no data.";
        FileProfileImpl fp = new FileProfileImpl();
        String fpId = "-1";
        HtmlFilter hFilter = HTMLFilterTestHelper.addHtmlFilter(1, "For PO", true);
        long secondFilterId = hFilter.getId();
        String secondFilterTableName = FilterConstants.HTML_TABLENAME;
        for (int i = 0; i < sourceFiles.length; i++)
        {
            try
            {
                // Read source files
                File sourceFile = sourceFiles[i];
                // extract source file
                Output output = doExtractWithFilter(sourceFile, extractor, UTF8, fp, fpId,
                        secondFilterId, secondFilterTableName);

                // get translatable text content
                answerContent = getTranslatableTextContent(output);

                String fileName = filterName + sourceFile.getName();
                String answerFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".txt";
                File answerFile = new File(answerRoot + File.separator + answerFileName);
                File tmpFile = new File(answerRoot + File.separator + answerFileName + ".tmp");

                // Generate Answer files
                if (newFileList != null && newFileList.contains(sourceFile.getName()))
                {
                    generateFile(answerFile, answerContent, UTF8);
                }

                if (!answerFile.exists())
                {
                    fail("The file compared to :" + answerFile.getName() + " doesn't exist");
                }

                // Generate files for compare
                generateFile(tmpFile, answerContent, UTF8);
                if (fileCompareNoCareEndLining(tmpFile, answerFile))
                {
                    tmpFile.delete();
                    // generate target file
                    File rountTipFile = new File(roundtripRoot + File.separator + fileName);
                    delTranslabaleSource(output);
                    String gxml = DiplomatWriter.WriteXML(output);
                    byte[] mergeResult = getTargetFileContent(gxml, UTF8);
                    String s = new String(mergeResult, UTF8);
                    generateFile(rountTipFile, s, UTF8);
                    Assert.assertTrue(rountTipFile.exists());
                }
                else
                {
                    fail("\n" + tmpFile + "\n and \n" + answerFile + " not equal");
                }
            }
            catch (Exception e)
            {
                fail(e.getMessage());
            }
        }

        HTMLFilterTestHelper.delHtmlFilterByID(hFilter);
    }

    public String getFileContent(File file, AbstractExtractor extractor, String encoding)
    {
        Output output = new Output();
        EFInputData input = new EFInputData();
        input.setCodeset(encoding);
        input.setURL(file.toURI().toString());
        input.setLocale(Locale.US);
        extractor.init(input, output);
        extractor.loadRules();
        extractor.extract();
        String gxml = DiplomatWriter.WriteXML(output);

        return gxml;
    }

    public AbstractExtractor initExtractor()
    {
        extractor = new Extractor();
        return extractor;
    }

    // Deletes the source translatable elements, for roundtrip.
    public void delTranslabaleSource(Output p_output)
    {
        Iterator<?> it = p_output.documentElementIterator();
        p_output.clearDocumentElements();
        while (it.hasNext())
        {
            DocumentElement element = (DocumentElement) it.next();
            if (element instanceof TranslatableElement)
            {
                String type = ((TranslatableElement) element).getXliffPartByName();
                if ("source".equalsIgnoreCase(type))
                {
                    // Deletes the source translatable elements.
                }
                else
                {
                    p_output.addDocumentElement(element);
                }
            }
            else
            {
                p_output.addDocumentElement(element);
            }
        }
    }

    @Override
    public String getTranslatableTextContent(Output p_output)
    {
        if (p_output == null)
        {
            return null;
        }

        StringBuffer resultContent = new StringBuffer();

        Iterator<?> eleIter = p_output.documentElementIterator();
        while (eleIter.hasNext())
        {
            DocumentElement de = (DocumentElement) eleIter.next();
            if (de instanceof TranslatableElement)
            {
                String elemValue = de.getText();
                String elemType = ((Segmentable) de).getXliffPartByName();

                if ("source".equalsIgnoreCase(elemType))
                {
                    resultContent.append(elemValue);
                    resultContent.append(lineSep);
                }
                else if ("target".equalsIgnoreCase(elemType))
                {
                    resultContent.append("\t\t\t---[");
                    resultContent.append(elemValue);
                    resultContent.append("]");
                    resultContent.append(lineSep);
                    resultContent.append(lineSep);
                }
                else
                {
                    Iterator<?> it = ((TranslatableElement) de).getSegments().iterator();
                    while (it.hasNext())
                    {
                        SegmentNode sn = (SegmentNode) it.next();
                        if (sn.getSegment() != null)
                        {
                            resultContent.append(sn.getSegment());
                            resultContent.append(lineSep);
                            resultContent.append("\t\t\t---[");
                            resultContent.append(sn.getSegment());
                            resultContent.append("]");
                            resultContent.append(lineSep);
                            resultContent.append(lineSep);
                        }
                    }
                }
            }
            else if (de instanceof LocalizableElement)
            {
                // Do not care localizable element for now.
            }
        }

        return resultContent.toString();
    }

    public Output doExtractWithFilter(File p_file, AbstractExtractor extractor, String p_encoding,
            FileProfileImpl p_fp, String p_fpId, long p_secondFilterId,
            String p_secondFilterTableName) throws Exception
    {
        Output output = new Output();
        EFInputData input = new EFInputData();
        input.setCodeset(p_encoding);
        input.setURL(p_file.toURI().toString());
        input.setLocale(Locale.US);
        extractor.init(input, output);
        extractor.loadRules();
        extractor.extract();

        DiplomatSegmenter seg = new DiplomatSegmenter();
        seg.segmentXliff(output);

        // Do Secondary Filter
        StandardExtractor se = StandardExtractorTestHelper.getInstance(null, null);
        DiplomatAPI diplomat = new DiplomatAPI();
        diplomat.setEncoding(UTF8);
        diplomat.setSegmentationRuleText("default");
        Iterator<?> it = output.documentElementIterator();
        output.clearDocumentElements();

        try
        {
            ClassUtil.testMethod(se, "doSecondFilterForPO", output, it, diplomat, p_fp, p_fpId,
                    p_secondFilterId, p_secondFilterTableName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return output;
    }

    @Override
    public Output doExtract(File p_file, AbstractExtractor extractor, String p_encoding)
            throws Exception
    {
        Output output = new Output();
        EFInputData input = new EFInputData();
        input.setCodeset(p_encoding);
        input.setURL(p_file.toURI().toString());
        input.setLocale(Locale.US);
        extractor.init(input, output);
        extractor.loadRules();
        extractor.extract();

        DiplomatSegmenter seg = new DiplomatSegmenter();
        seg.segmentXliff(output);

        return output;
    }

    @Override
    public HashMap initFileSet()
    {
        return null;
    }
}
