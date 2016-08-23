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
package com.globalsight.ling.docproc.extractor.xliff;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatCtrlCharConverter;
import com.globalsight.ling.docproc.DiplomatPostProcessor;
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
import com.globalsight.ling.docproc.extractor.FileSet;
import com.globalsight.ling.docproc.extractor.xml.GsDOMParser;

public class TestExtractor extends BaseExtractorTestClass
{
    static final String ENCODING = "UTF-8";
    static final String UTF16LE = "UTF-16LE";
    private static final String extension = "xlf";
    public String lineSep = System.getProperty("line.separator");

    private Extractor extractor;

    @SuppressWarnings("unchecked")
    private HashMap fileSets = new HashMap();

    @Before
    public void setUp() throws Exception
    {
        initExtractor();
        fileSets = initFileSet();
    }

    @After
    public void clear()
    {
        extractor = null;
        fileSets = new HashMap();
    }

    /**
     * Test RC file extractor and merger.
     */
    @Test
    public void testExtractor()
    {
        try
        {
            ArrayList<?> fileSet = (ArrayList<?>) fileSets.get("testExtractor");
            if (fileSet != null && fileSet.size() > 0)
            {
                Iterator<?> it = fileSet.iterator();
                while (it.hasNext())
                {
                    FileSet fs = (FileSet) it.next();
                    File sourceFile = fs.getSourceFile();
                    File answerFile = fs.getAnswerFile();
                    File roundtripFile = fs.getRoundtripFile();

                    if (sourceFile.exists() && sourceFile.isFile())
                    {
                        // extract source file
                        Output output = doExtract(sourceFile, extractor,
                                ENCODING);
                        // get translatable text content
                        String resultContent = getTranslatableTextContent(output);
                        // generate result file for compare purpose
                        File tmpResultFile = new File(answerFile
                                .getParentFile().getAbsolutePath()
                                + File.separator
                                + sourceFile.getName()
                                + ".tmp");
                        generateFile(tmpResultFile, resultContent, ENCODING);

                        // compare result file to answer file
                        if (fileCompareNoCareEndLining(tmpResultFile,
                                answerFile))
                        {
                            tmpResultFile.delete();

                            // generate target file
                            delTranslabaleSource(output);
                            String gxml = DiplomatWriter.WriteXML(output);
                            byte[] mergeResult = getTargetFileContent(gxml,
                                    ENCODING);
                            String s = new String(mergeResult, ENCODING);
                            s = s.replace(" & ", " &amp; ");
                            generateFile(roundtripFile, s, ENCODING);
                            Assert.assertTrue(roundtripFile.exists());
                            fileCompareNoCareEndLining(roundtripFile,
                                    sourceFile);
                        }
                        else
                        {
                            fail("\n" + tmpResultFile + "\n and \n"
                                    + answerFile + " not equal");
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }

    }

    @Test
    public void testWsGetNodeInfo()
    {
        try
        {
            File wsxliff = new File(
                    getResourcePath(TestExtractor.class, SOURCE)
                            + File.separator + "WSXliff.xlf");
            FileInputStream in = new FileInputStream(wsxliff);
            InputStreamReader input = new InputStreamReader(in, "utf-8");
            GsDOMParser parser = new GsDOMParser();

            // parse and create DOM tree
            Document document = parser.parse(new InputSource(input));
            Node doc = document.getFirstChild().getFirstChild();

            while (!doc.getNodeName().equals("file"))
            {

                doc = doc.getNextSibling();
            }

            doc = doc.getFirstChild();

            while (!doc.getNodeName().equals("body"))
            {
                doc = doc.getNextSibling();
            }

            doc = doc.getFirstChild();

            while (!doc.getNodeName().equals("trans-unit"))
            {
                doc = doc.getNextSibling();
            }

            doc = doc.getFirstChild();

            while (!doc.getNodeName().equals("source"))
            {
                doc = doc.getNextSibling();
            }

            Node sourceNode = doc;
            WSNodeInfo wsn = new WSNodeInfo();

            HashMap<String, String> map = wsn.getNodeTierInfo(sourceNode);

            if (map.get(Extractor.IWS_TM_SCORE) == null)
            {
                fail("\n" + Extractor.IWS_TM_SCORE + " lost!");
            }
            else if (!map.get(Extractor.IWS_TM_SCORE).equals("99.67"))
            {
                fail("\n" + Extractor.IWS_TM_SCORE
                        + " is not the correct result!");
            }

            if (map.get(Extractor.IWS_SID) == null)
            {
                fail("\n" + Extractor.IWS_SID + " lost!");
            }
            else if (!map.get(Extractor.IWS_SID).equals("0001"))
            {
                fail("\n" + Extractor.IWS_SID + " is not the correct result!");
            }

            if (map.get(Extractor.IWS_WORDCOUNT) == null)
            {
                fail("\n" + Extractor.IWS_WORDCOUNT + " lost!");
            }
            else if (!map.get(Extractor.IWS_WORDCOUNT).equals("6"))
            {
                fail("\n" + Extractor.IWS_WORDCOUNT
                        + " is not the correct result!");
            }

            if (map.get(Extractor.IWS_TRANSLATION_TYPE) == null)
            {
                fail("\n" + Extractor.IWS_TRANSLATION_TYPE + " lost!");
            }
            else if (!map.get(Extractor.IWS_TRANSLATION_TYPE).equals(
                    "machine_translation"))
            {
                fail("\n" + Extractor.IWS_TRANSLATION_TYPE
                        + " is not the correct result!");
            }

            if (map.get(Extractor.IWS_SOURCE_CONTENT) == null)
            {
                fail("\n" + Extractor.IWS_SOURCE_CONTENT + " lost!");
            }
            else if (!map.get(Extractor.IWS_SOURCE_CONTENT).equals("repeated"))
            {
                fail("\n" + Extractor.IWS_SOURCE_CONTENT
                        + " is not the correct result!");
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
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
                    resultContent.append(lineSep);
                    resultContent.append(elemValue);
                    resultContent.append(lineSep);
                }
                else if ("target".equalsIgnoreCase(elemType))
                {
                    resultContent.append("\t\t\t---[");
                    resultContent.append(elemValue);
                    resultContent.append("]");
                    resultContent.append(lineSep);
                }
                else if ("altSource".equalsIgnoreCase(elemType))
                {
                    resultContent.append("\t\t\t---altSource#[");
                    resultContent.append(elemValue);
                    resultContent.append("]");
                    resultContent.append(lineSep);
                }
                else if ("altTarget".equalsIgnoreCase(elemType))
                {
                    resultContent.append("\t\t\t---altTarget#[");
                    resultContent.append(elemValue);
                    resultContent.append("]");
                    resultContent.append(lineSep);
                }
                else
                {
                    Iterator<?> it = ((TranslatableElement) de).getSegments()
                            .iterator();
                    while (it.hasNext())
                    {
                        SegmentNode sn = (SegmentNode) it.next();
                        if (sn.getSegment() != null)
                        {
                            resultContent.append(lineSep);
                            resultContent.append(sn.getSegment());
                            resultContent.append(lineSep);
                            resultContent.append("\t\t\t---[");
                            resultContent.append(sn.getSegment());
                            resultContent.append("]");
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
                String type = ((TranslatableElement) element)
                        .getXliffPartByName();
                if ("source".equalsIgnoreCase(type))
                {
                    // Deletes the source translatable elements.
                }
                else if ("altSource".equalsIgnoreCase(type)
                        || "altTarget".equalsIgnoreCase(type))
                {
                    // Deal with <alt-trans>
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

    /**
     * An empty method from parent class.
     */
    @Override
    public String getFileContent(File file, AbstractExtractor extractor,
            String encoding)
    {
        return null;
    }

    /**
     * Do extract, segmentation, handle "x","nbsp" etc.
     * @throws Exception 
     */
    @Override
    public Output doExtract(File file, AbstractExtractor extractor,
            String encoding) throws Exception
    {
        Output output = new Output();
        EFInputData input = new EFInputData();
        input.setCodeset(encoding);
        // Read from URL
        input.setURL(file.toURI().toString());
        input.setLocale(Locale.US);
        extractor.init(input, output);
        // RC loadRules() does nothing.
        extractor.loadRules();

        // Extract this file
        extractor.extract();

        // (Not required)
        DiplomatCtrlCharConverter dc = new DiplomatCtrlCharConverter();
        dc.convertChars(output);
        output = dc.getOutput();

        // Segment the output(Required)
        DiplomatSegmenter seg = new DiplomatSegmenter();
        seg.segment(output);

        // Word count recalculate.(Not required)
        // DiplomatWordCounter wc = new DiplomatWordCounter();
        // wc.setLocalizableWordcount(0);
        // wc.countDiplomatDocument(output);
        // output = wc.getOutput();

        // CvdL new step to wrap nbsp and fix the "x" attributes.(Not required)
        DiplomatPostProcessor pp = new DiplomatPostProcessor();
        pp.setFormatName(extension);
        pp.postProcess(output);
        output = pp.getOutput();

        return output;
    }

    /**
     * Initiate the RC extractor.
     */
    @Override
    public AbstractExtractor initExtractor()
    {
        extractor = new Extractor();
        return extractor;
    }

    @Override
    public HashMap initFileSet()
    {
        String[][] fileSet =
        {
        { "testExtractor", "sample.xlf", "sample.txt", "sample.xlf" } };

        return formFileSets(fileSet, TestExtractor.class);
    }
}
