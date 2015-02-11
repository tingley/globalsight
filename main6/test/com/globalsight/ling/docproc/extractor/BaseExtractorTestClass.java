package com.globalsight.ling.docproc.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.everest.unittest.util.FileUtil;
import com.globalsight.ling.common.TranscoderException;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.CtrlCharConverter;
import com.globalsight.ling.docproc.DiplomatMerger;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.L10nContent;
import com.globalsight.ling.docproc.LocalizableElement;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.merger.PostMergeProcessor;

public abstract class BaseExtractorTestClass
{
    public static String SOURCE = "source";
    public static String ANSWERS = "answers";
    public static String ROUNDTRIP = "roundtrip";
    
    private CxeMessage cxeMessage = null;
    private boolean ConvertHtmlEntityForHtml = false;
    
    public boolean isConvertHtmlEntityForHtml()
    {
        return ConvertHtmlEntityForHtml;
    }

    public void setConvertHtmlEntityForHtml(boolean convertHtmlEntityForHtml)
    {
        ConvertHtmlEntityForHtml = convertHtmlEntityForHtml;
    }

    public CxeMessage getCxeMessage()
    {
        return cxeMessage;
    }

    public void setCxeMessage(CxeMessage cxeMessage)
    {
        this.cxeMessage = cxeMessage;
    }

    /**
     * Initialize the extractor
     * @return
     */
    public abstract AbstractExtractor initExtractor();
    
    /**
     * <p>For unit tests of filter configurations and extractors, need define
     * "source","answers" and "roundtrip" file combinations for different filter
     * combinations.</p> 
     * 
     * How to use:
     * In sub class extratorTest, need override this method to define all the 
     * file combinations for cases.
     * 1. Put all the file combinations in array like this:
     *     String[][] a = { 
     *         {"testMethodName1", "sourceFile1", "answerFile1", "roundtripFile1"},
     *         {"testMethodName1", "sourceFile1", "answerFile2", "roundtripFile2"}, 
     *         {"testMethodName2", "sourceFile3", "answerFile3", "roundtripFile3"}
     *         ...
     *     };
     * The first value is the method name, the second is the source file name, 
     * the third is the answer file name, the fourth is the round trip file name.
     * 2. Send the array to "formFileSets(...)" method to get a wrapped HashMap.
     * 
     * @return HashMap - test method name as key, ArrayList as value which 
     * includes all file set for this case.
     */
    public abstract HashMap initFileSet();
    
    /**
     * Get file content in String. Keep this as abstract to enforce to override
     * it.User can select to use this one or "doExtract(...)" method.
     */
    public abstract String getFileContent(File file, AbstractExtractor extractor,
            String encoding);
    
    /**
     * Get extract result in output. Keep this as abstract to enforce to
     * override it.User can select to use this one or "getFileContent(...)"
     * method.
     */
    public abstract Output doExtract(File file, AbstractExtractor extractor,
            String encoding);

    /**
     * Get class of file directory
     * @param c
     * @param relativePath
     * @return
     */
    public static String getResourcePath(Class c, String relativePath)
    {
        return FileUtil.getResourcePath(c, relativePath);
    }

    /**
     * Generate a file with the given content and encoding
     * @param file
     * @param content
     * @param encoding
     * @throws IOException
     */
    protected void generateFile(File file, String content, String encoding)
            throws IOException
    {
        FileUtil.generateFile(file, content, encoding);
    }
    
    /**
     * Compare the content of two files
     * @param File A
     * @param File B
     * @return Returns true if file A is equal to file B,
     * else return false
     */
    protected boolean fileCompare(File a, File b)
    {
        return FileUtil.fileCompare(a, b);
    }
    
    protected boolean fileCompareNoCareEndLining(File expected, File actual)
    {
        return FileUtil.fileCompareNoCareEndLining(expected, actual);
    }
    
    /**
     * Generate target file with the given gxml
     * @param gxml
     * @return
     * @throws TranscoderException
     */
    protected byte[] getTargetFileContent(String gxml, String encoding) throws TranscoderException
    {
        L10nContent l10ncontent = new L10nContent();
        DiplomatMerger merger = new DiplomatMerger();
        merger.setFilterId(-1);
        merger.init(gxml, l10ncontent);
        merger.setKeepGsa(false);
        merger.setTargetEncoding(encoding);
        
        boolean isUseSecondaryFilter = false;
        boolean convertHtmlEntry = false;
        merger.setIsUseSecondaryFilter(isUseSecondaryFilter);
        merger.setConvertHtmlEntryFromSecondFilter(convertHtmlEntry);
        merger.setCxeMessage(cxeMessage);
        merger.setConvertHtmlEntityForHtml(ConvertHtmlEntityForHtml);
        merger.merge();
        
        String gxml1 = l10ncontent.getL10nContent();
        l10ncontent.setL10nContent(CtrlCharConverter.convertToCtrl(gxml1));
        String processed = postMergeProcess(l10ncontent.getL10nContent(),
                merger.getDocumentFormat(), encoding);

        if (processed != null)
        {
            l10ncontent.setL10nContent(processed);
        }
        return l10ncontent.getTranscodedL10nContent(encoding);
    }
    
    private String postMergeProcess(String p_content, String p_format,
            String p_ianaEncoding) throws DiplomatMergerException
    {
        ExtractorRegistry registry = ExtractorRegistry.getObject();

        int formatId = registry.getFormatId(p_format);
        if (formatId == -1)
        {
            return null;
        }

        String strClass = registry.getPostMergeClasspath(formatId);

        PostMergeProcessor processor = null;
        try
        {
            processor = (PostMergeProcessor) Class.forName(strClass)
                    .newInstance();
        }
        catch (Exception e)
        {
            throw new DiplomatMergerException(
                    "PostMergeProcessorCreationFailure", null, e);
        }
        return processor.process(p_content, p_ianaEncoding);
    }

    /**
     * Get all translatable text content from specified GXML. Commonly, this
     * method is used with "getFileContent(...)" together,which returns the
     * GXML.
     */
    protected String getTranslatableTextContent(String gxml)
            throws DocumentException
    {
        StringBuffer content = new StringBuffer();
        Document document = DocumentHelper.parseText(gxml);
        Element root = document.getRootElement();
        List translatableNodes = root.elements("translatable");
        
        int n = 1;
        for (int j = 0; j < translatableNodes.size();j++)
        {
            Element translatableNode = (Element) translatableNodes.get(j);
            
            List segmentNodes = translatableNode.elements("segment");
            for (int z = 0; z < segmentNodes.size(); z++)
            {
                Element segmentNode = (Element) segmentNodes.get(z);
                List contentList = segmentNode.content();
                for (Object e : contentList)
                {
                    if (e instanceof DefaultText)
                    {
                        String tmp = ((DefaultText)e).getText();
                        content.append(tmp);
                    } 
                    else if (e instanceof DefaultElement)
                    {
                        content.append("[x").append(n++).append("]");
                    }
                }
                content.append("\n");
            }
        }
        return content.toString();
    }
    
    /**
     * Get all translatable text content from specified output. Commonly, this
     * method is used with "doExtract(...)" together,which returns the output
     * object.
     */
    public String getTranslatableTextContent(Output p_output)
    {
        if (p_output == null) {
            return null;
        }

        StringBuffer resultContent = new StringBuffer();
        
        Iterator eleIter = p_output.documentElementIterator();
        while (eleIter.hasNext())
        {
            DocumentElement de = (DocumentElement) eleIter.next();
            if (de instanceof TranslatableElement)
            {
                Iterator it = 
                    ((TranslatableElement) de).getSegments().iterator();
                while (it.hasNext())
                {
                    SegmentNode sn = (SegmentNode) it.next();
                    if (sn.getSegment() != null)
                    {
                        resultContent.append(sn.getSegment());
                        resultContent.append(System
                                .getProperty("line.separator"));
                        resultContent.append(System
                                .getProperty("line.separator"));
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

    /**
     * Wrap the fileSet definition in array into HashMap. Default, the files
     * used should be in sub folders "source", "answers" and "roundtrip"
     * relative to current path.
     */
    public static HashMap formFileSets(String[][] fileSet, Class p_class)
    {
        if (fileSet == null || fileSet.length < 1 || p_class == null)
        {
            return null;
        }
        
        HashMap result = new HashMap();
        ArrayList list = new ArrayList();

        int length = fileSet.length;
        for (int i=0; i < length; i++)
        {
            String[] lineData = fileSet[i];
            String methodName = lineData[0];
            String sourceFileName = lineData[1];
            String answerFileName = lineData[2];
            String roundtripFileName = lineData[3];
            
            String sourceRoot = getResourcePath(p_class, SOURCE);
            String answerRoot = getResourcePath(p_class, ANSWERS);
            String roundtripRoot = getResourcePath(p_class, ROUNDTRIP);

            File sourceFile = 
                new File(sourceRoot + File.separator + sourceFileName);
            File answerFile = 
                new File(answerRoot + File.separator + answerFileName);
            File roundtripFile = 
                new File(roundtripRoot + File.separator + roundtripFileName);
            
            FileSet fs = 
                new FileSet(methodName, sourceFile, answerFile, roundtripFile);
            
            list = (ArrayList) result.get(methodName);
            if (list == null)
            {
                list = new ArrayList();
            }
            list.add(fs);
            result.put(methodName, list);
        }
        
        return result;
    }
}
