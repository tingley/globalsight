package com.globalsight.ling.docproc.extractor.fm;

import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;
import org.junit.Test;

import com.globalsight.ling.common.TranscoderException;
import com.globalsight.ling.docproc.CtrlCharConverter;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DiplomatMerger;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.L10nContent;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.merger.PostMergeProcessor;

public class ExtractorTest
{

    private static String sourceRoot = getResourcePath("source");
    private static String answerRoot = getResourcePath("answers");
    private static String roundtripRoot = getResourcePath("roundtrip");
    
    /**
     * If you want to see the inline content,
     * make this true
     */
    private boolean showInLineContent = false;
    /**
     * true: create answer files
     * false: don't create answer files
     */
    private boolean generateAnswerFiles = false;
    
    @Test
    public void compareAnswerFileAndGenerateTargetFile()
    {
        File source = new File(sourceRoot);
        File[] sourceFiles = source.listFiles(new myFileFilter());
        for (int i = 0; i < sourceFiles.length; i++)
        {
            try
            {
                // Read source files
                File sourceFile = sourceFiles[i];
//                System.out.println(sourceFile.getName());
                // Get file content
                String gxml = getFileContent(sourceFile);
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
                                if (showInLineContent)
                                {
                                    String tmp = ((DefaultElement)e).getText();
                                    content.append(tmp);
                                }
                                else
                                {
                                    content.append("[x").append(n++).append("]");
                                }
                            }
                        }
                        content.append("\n");
                    }
                }
                
                String fileName = sourceFile.getName();
                String answerFileName = fileName.substring(0, fileName
                        .lastIndexOf(".")) + ".txt";
                File answerFile = new File(answerRoot + File.separator
                        + answerFileName);
                File tmpFile = new File(answerRoot + File.separator
                        + answerFileName + ".tmp");
                if (generateAnswerFiles){
                    // Generate Answer files
                    generateFile(answerFile, content.toString());
                }
                if (!answerFile.exists())
                {
                    fail("Original file does not exist");
                }
                // Generate files for compare
                generateFile(tmpFile, content.toString());
                if (fileCompare(tmpFile, answerFile))
                {
                    tmpFile.delete();
                    
                    // generate target file
                    File rountTipFile = new File(roundtripRoot + File.separator + fileName);
                    
                    byte[] mergeResult = null;
                    DiplomatAPI diplomat = new DiplomatAPI();
                    try
                    {
                        diplomat.setFilterId(-1);
                        diplomat.setFilterTableName(null);
                        mergeResult = generateTargetFile(gxml);
                    }
                    catch (TranscoderException e) {}
                    String s = new String(mergeResult, "UTF-8");
                    generateTargetFile(rountTipFile, s);
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
     * Get simple file MD5 code
     * @param file
     * @return
     */
    private static String getFileMD5(File file)
    {
        if (!file.isFile())
        {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1)
            {
                digest.update(buffer, 0, len);
            }
            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }
    
    /**
     * Compare the content of two files
     * @param File A
     * @param File B
     * @return Returns true if file A is equal to file B,
     * else return false
     */
    private boolean fileCompare(File a, File b)
    {
        String codeOfA = getFileMD5(a);
        String codeOfB = getFileMD5(b);
        
        if (codeOfA.equals(codeOfB))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void generateFile(File file, String content) throws IOException
    {
        Writer fw = null;
        BufferedWriter bw = null;
        try
        {
            fw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            bw = new BufferedWriter(fw);
            bw.write(content);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bw != null) bw.close();
            if (fw != null) fw.close();
        }
    }
    
    private void generateTargetFile(File fileName, String content) throws IOException
    {
        generateFile(fileName, content);
    }
    
    private String getFileContent(File file)
    {
        Output output = new Output();
        EFInputData input = new EFInputData();
        input.setCodeset("UTF-8");
        input.setURL(file.toURI().toString());
        input.setLocale(Locale.US);
        Extractor extractor = new Extractor();
        extractor.init(input, output);
        extractor.extract();
        String gxml = DiplomatWriter.WriteXML(output);
        DiplomatSegmenter seg = new DiplomatSegmenter();
        gxml = seg.segment(gxml);
        gxml = gxml.replace("&apos;", "'");
        return gxml;
    }
    
    private byte[] generateTargetFile(String gxml) throws TranscoderException
    {
        L10nContent l10ncontent = new L10nContent();
        DiplomatMerger merger = new DiplomatMerger();
        merger.setFilterId(-1);
        merger.init(gxml, l10ncontent);
        merger.setKeepGsa(false);
        merger.setTargetEncoding("UTF8");
        
        boolean isUseSecondaryFilter = false;
        boolean convertHtmlEntry = false;
        merger.setIsUseSecondaryFilter(isUseSecondaryFilter);
        merger.setConvertHtmlEntryFromSecondFilter(convertHtmlEntry);
        merger.merge();
        
     // Convert PUA characters back to original C0 control codes
        String gxml1 = l10ncontent.getL10nContent();
        l10ncontent.setL10nContent(CtrlCharConverter.convertToCtrl(gxml1));
     // format specific post merge processing
        String processed = postMergeProcess(l10ncontent.getL10nContent(),
                merger.getDocumentFormat(), "UTF-8");

        // processed == null means the content doesn't need to be changed.
        if (processed != null)
        {
            l10ncontent.setL10nContent(processed);
        }
        return l10ncontent.getTranscodedL10nContent("UTF8");
    }
    
    private String postMergeProcess(String p_content, String p_format,
            String p_ianaEncoding) throws DiplomatMergerException
    {
        ExtractorRegistry registry = ExtractorRegistry.getObject();

        int formatId = registry.getFormatId(p_format);
        // p_format is not a known format. do nothing.
        if (formatId == -1)
        {
            return null;
        }

        // construct an post merge processor
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
    
    private static String getResourcePath(String relativePath) {
        return ExtractorTest.class.getResource(relativePath).getFile();
    }
    
    private class myFileFilter implements FileFilter
    {
        public boolean accept(File pathname)
        {
            String filename = pathname.getName().toLowerCase();
            if (filename.endsWith(".mif"))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
