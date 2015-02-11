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
package com.globalsight.ling.aligner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile;
import com.globalsight.everest.aligner.AlignerExtractor;
import com.globalsight.everest.aligner.AlignerExtractorResult;
import com.globalsight.everest.aligner.AlignmentStatus;
import com.globalsight.everest.corpus.CorpusDoc;
import com.globalsight.everest.corpus.CorpusManager;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.aligner.io.AlignmentPackageWriter;
import com.globalsight.ling.aligner.io.AlignmentProjectFileAccessor;
import com.globalsight.ling.aligner.io.GapReader;
import com.globalsight.ling.aligner.io.GapWriter;
import com.globalsight.ling.aligner.io.GxmlReader;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.corpusinterface.TuvMappingHolder;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * AlignmentProject holds all information that belongs to an alignment
 * process.
 */

public class AlignmentProject
{
    private static final Logger c_logger =
        Logger.getLogger(
            AlignmentProject.class.getName());

    static public final int BUFSIZE = 4096;

    // project name
    private String m_projectName;
    
    // source and target locales
    private GlobalSightLocale m_sourceLocale;
    private GlobalSightLocale m_targetLocale;
    
    // html, xml, doc, etc
    private KnownFormatType m_formatType;

    // source and target file character encoding
    private String m_sourceFileEncoding;
    private String m_targetFileEncoding;

    // xml rule file
    private XmlRuleFile m_xmlRule;
    
    // project tmp file storage directory
    private File m_projectTmpDirectory;

    // TM id to save segments
    private Tm m_tm;
    
    // TM save option
    private int m_tmSaveMode;
    
    // List of AlignmentUnit objects
    private List m_alignmentUnits;

    private User m_uploadUser = null;

    private static final String GAP_FILE_NAME = "PackageDescriptor.gap";

    // alignment package extension
    public static final String ALIGNMENT_PKG_EXT = ".alp";
    
//    public static final File ALIGNER_PACKAGE_DIRECTORY
//        = getAlignerPackageDirectory();
//    
//    private static final File ALIGNER_TMP_BASE_DIRECTORY
//        = getAlignerTmpBaseDirectory();
//
//    private static final File CXE_DOC_ROOT_DIRECTORY
//        = getCxeDocRootDirectory();
//    
//    
//    static private File getAlignerPackageDirectory()
//    {
//        File alignerRoot = null;
//
//        try
//        {
//            SystemConfiguration sc = SystemConfiguration.getInstance();
//
//            String docRoot = sc.getStringParameter(
//                SystemConfiguration.FILE_STORAGE_DIR);
//
//            alignerRoot = new File(docRoot, "GlobalSight");
//            alignerRoot = new File(alignerRoot, "AlignerPackages");
//            alignerRoot.mkdirs();
//        }
//        catch (Exception e)
//        {
//            throw new RuntimeException(e);
//        }
//
//        return alignerRoot;
//    }
//
//
//    static private File getAlignerTmpBaseDirectory()
//    {
//        File alignerRoot = null;
//
//        try
//        {
//            SystemConfiguration sc = SystemConfiguration.getInstance();
//
//            String docRoot = sc.getStringParameter(
//                SystemConfiguration.FILE_STORAGE_DIR);
//
//            alignerRoot
//                = new File(docRoot, "_Aligner_");
//            alignerRoot.mkdirs();
//        }
//        catch (Exception e)
//        {
//            throw new RuntimeException(e);
//        }
//
//        return alignerRoot;
//    }
//
//
//    static private File getCxeDocRootDirectory()
//    {
//        File docRoot = null;
//
//        try
//        {
//            SystemConfiguration sc = SystemConfiguration.getInstance();
//
//            String docRootStr = sc.getStringParameter(
//                SystemConfiguration.CXE_DOCS_DIR);
//
//            docRoot = new File(docRootStr);
//        }
//        catch (Exception e)
//        {
//            throw new RuntimeException(e);
//        }
//
//        return docRoot;
//    }


    public static File getProjectFile(String p_projectName)
    {
//        return new File(ALIGNER_PACKAGE_DIRECTORY,
        return new File(AmbFileStoragePathUtils.getAlignerPackageDir(),
            p_projectName + ALIGNMENT_PKG_EXT);
    }
    

    static public File getProjectTmpDirectory(String p_projectName)
    {
//        return new File(ALIGNER_TMP_BASE_DIRECTORY, p_projectName);
        return new File(AmbFileStoragePathUtils.getAlignerTmpDir(), p_projectName);
    }



    /**
     * Constructor for the batch alignment.
     */
    public AlignmentProject(String p_projectName,
        GlobalSightLocale p_sourceLocale,
        GlobalSightLocale p_targetLocale, KnownFormatType p_formatType,
        String p_sourceFileEncoding, String p_targetFileEncoding,
        XmlRuleFile p_xmlRule)
        throws Exception
    {
        m_projectName = p_projectName;
        
        m_sourceLocale = p_sourceLocale;
        m_targetLocale = p_targetLocale;

        m_formatType = p_formatType;

        m_sourceFileEncoding = p_sourceFileEncoding;
        m_targetFileEncoding = p_targetFileEncoding;

        m_xmlRule = p_xmlRule;
        
        m_projectTmpDirectory = createProjectTmpDir();
        m_alignmentUnits = new ArrayList();
    }
    

    /**
     * Constructor for the alignment package upload
     *
     * @param p_projectName Alignment project name
     * @param p_tmId TM id in which the segments get saved
     * @param p_tmSaveMode TM save mode. This must be either
     * TmCoreManager.SYNC_MERGE or TmCoreManager.SYNC_DISCARD
     */
    public AlignmentProject(
        String p_projectName, Tm p_tm, int p_tmSaveMode)
        throws Exception
    {
        m_projectName = p_projectName;
        m_tm = p_tm;
        m_tmSaveMode = p_tmSaveMode;
        
        m_projectTmpDirectory = createProjectTmpDir();
        m_alignmentUnits = new ArrayList();
    }
    

    /**
     * Align all file pairs. The method does the followings.
     * 1. Align all file pairs and write TMX and GAM files in a directory.
     * 2. Write a PackageDescriptor.gap file in the directory.
     * 3. Zip up all files and put it in the aligner package directory.
     */
    public void alignAll()
        throws Exception
    {
        // align and write TMX and GAM files
        Iterator it = m_alignmentUnits.iterator();
        while(it.hasNext())
        {
            AlignmentUnit alignmentUnit = (AlignmentUnit)it.next();
            if(alignmentUnit.getState().equals(AlignmentUnit.EXTRACTED))
            {
                alignmentUnit.alignFiles(m_sourceLocale, m_targetLocale,
                    m_formatType, m_sourceFileEncoding, m_targetFileEncoding,
                    m_xmlRule);
            }
        }

        // write GAP file
        File gapFile
            = new File(m_projectTmpDirectory, GAP_FILE_NAME);
        writeGapFile(gapFile);

        // zip up the files
//        File packageFile = new File(ALIGNER_PACKAGE_DIRECTORY, getProjectFileName());
        File packageFile = new File(
                AmbFileStoragePathUtils.getAlignerPackageDir(), 
                getProjectFileName());
        createAlignmentPackage(packageFile);

        // add this project to the project file
        AlignmentProjectFileAccessor.addProject(getAlignmentStatus());
    }
    
        
    /**
     * Save the aligned segments to TM. The alignment package must be
     * unzipped and the files must be written in the project tmp
     * directory before calling this method. This can be done by
     * AlignmentPackageReader.
     */
    public void saveToTm()
        throws Exception
    {
        // read GAP file and create AlignmentUnit objects
        GapReader gapReader = new GapReader(this);
        File gapFile = new File(m_projectTmpDirectory, GAP_FILE_NAME);
        Reader gapStream
            = new InputStreamReader(new FileInputStream(gapFile), "UTF-8");
        gapReader.read(gapStream);
        
        // Save segments to TM in each AlignmentUnit
        List alignmentUnits = getAlignmentUnits();
        Iterator it = alignmentUnits.iterator();
        while(it.hasNext())
        {
            AlignmentUnit unit = (AlignmentUnit)it.next();
            if(unit.getState().equals(AlignmentUnit.COMPLETED))
            {
                // read TMX and GAM files
                AlignmentResult alignmentResult
                    = unit.getAlignmentResult(m_sourceLocale, m_targetLocale);

                // save aligned segments to TM
                TuvMappingHolder mappingHolder
                    = saveAlignedSegments(alignmentResult);

                // populate corpus TM
                populateCorpusTm(unit, mappingHolder);
            }
        }

    }
    
        
    public void addAlignmentUnit(AlignmentUnit p_alignmentUnit)
    {
        m_alignmentUnits.add(p_alignmentUnit);
    }


    public void addAlignmentUnit(
        String p_sourceFileName, String p_targetFileName)
    {
        try
        {
            List sourceGxmls = AlignerExtractor.extract(
                10, p_sourceFileName, m_formatType,
                m_sourceLocale.toString(), m_sourceFileEncoding, m_xmlRule);

            List targetGxmls = AlignerExtractor.extract(
                10, p_targetFileName, m_formatType,
                m_targetLocale.toString(), m_targetFileEncoding, m_xmlRule);

            // create AlignmentUnit from a source and corresponding
            // target GXMLs
            int pageNum = Math.min(sourceGxmls.size(), targetGxmls.size());
            for(int i = 0; i < pageNum; i++)
            {
                AlignerExtractorResult sourceResult
                    = (AlignerExtractorResult)sourceGxmls.get(i);

                AlignerExtractorResult targetResult
                    = (AlignerExtractorResult)targetGxmls.get(i);

                AlignmentUnit unit = createAlignmentUnitFromExtractResult(
                    sourceResult, targetResult, p_sourceFileName,
                    p_targetFileName);

                addAlignmentUnit(unit);
            }
            
            // the number of source GXMLs and the target GXMLs are not
            // the same.  Create AlignmentUnit with error state just
            // to record what happened.
            if(pageNum < sourceGxmls.size())
            {
                for(int i = pageNum; i < sourceGxmls.size(); i++)
                {
                    AlignerExtractorResult sourceResult
                        = (AlignerExtractorResult)sourceGxmls.get(i);

                    AlignmentUnit unit = createAlignmentUnitFromExtractResult(
                        sourceResult, null, null, null);

                    addAlignmentUnit(unit);
                }
            }
            else if(pageNum < targetGxmls.size())
            {
                for(int i = pageNum; i < targetGxmls.size(); i++)
                {
                    AlignerExtractorResult targetResult
                        = (AlignerExtractorResult)targetGxmls.get(i);

                    AlignmentUnit unit = createAlignmentUnitFromExtractResult(
                        null, targetResult, null, null);

                    addAlignmentUnit(unit);
                }
            }
            
        }
        catch(Exception e)
        {
            // log the error
            c_logger.error(e.getMessage(), e);
            
            String errorMessage = null;
            // process error
            if(e instanceof GeneralException)
            {
                errorMessage
                    = ((GeneralException)e).getMessage(Locale.getDefault());
            }
            else
            {
                errorMessage = e.getMessage();
            }

            AlignmentUnit unit = new AlignmentUnit(
                p_sourceFileName, p_targetFileName, m_projectTmpDirectory);
            unit.setState(AlignmentUnit.ALIGN_FAILED);
            unit.setErrorMessage(errorMessage);
            
            addAlignmentUnit(unit);

            // exception is not re-throwed. The extraction must continue.
        }
    }
    

    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }
    

    public GlobalSightLocale getTargetLocale()
    {
        return m_targetLocale;
    }


    public void setSourceLocale(GlobalSightLocale p_locale)
    {
        m_sourceLocale = p_locale;
    }
    

    public void setTargetLocale(GlobalSightLocale p_locale)
    {
        m_targetLocale = p_locale;
    }
    

    public List getAlignmentUnits()
    {
        return m_alignmentUnits;
    }


    public File getProjectTmpDirectory()
        throws Exception
    {
        return m_projectTmpDirectory;
    }
    

    private String getProjectFileName()
    {
        return m_projectName + ALIGNMENT_PKG_EXT;
    }
    

    private File createProjectTmpDir()
        throws Exception
    {
//        File dir = new File(ALIGNER_TMP_BASE_DIRECTORY, m_projectName);
//         if(!dir.mkdir())
//         {
//             boolean created = false;
            
//             for(int i = 0; i < 999999; i++)
//             {
//                 dir= new File(ALIGNER_TMP_BASE_DIRECTORY, m_projectName + i);
//                 if(dir.mkdir())
//                 {
//                     created = true;
//                     break;
//                 }
//             }

//             if(!created)
//             {
//                 throw new LingManagerException(
//                     "AlignerProjectRootFailure", null, null);
//             }
//         }

        File dir = new File(AmbFileStoragePathUtils.getAlignerTmpDir(), m_projectName);
        dir.mkdir();
        
        return dir;
    }

    
    private void writeGapFile(File p_gapFile)
        throws Exception
    {
        PrintWriter output = new PrintWriter(
            new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(p_gapFile), "UTF-8")));
        
        GapWriter gapWriter = new GapWriter(output);
        gapWriter.write(this);

        output.close();
    }
        

    private void createAlignmentPackage(File p_packageFile)
        throws Exception
    {
        // create package writer
        OutputStream out = new FileOutputStream(p_packageFile);
        AlignmentPackageWriter writer
            = new AlignmentPackageWriter(out, m_projectTmpDirectory);

        // write GAP file
        writer.addFileToPackage(GAP_FILE_NAME);
        
        Iterator it = m_alignmentUnits.iterator();
        while(it.hasNext())
        {
            AlignmentUnit alignmentUnit = (AlignmentUnit)it.next();
            if(alignmentUnit.getState().equals(AlignmentUnit.PRE_ALIGNED))
            {
                // write source and target TMX files
                writer.addFileToPackage(alignmentUnit.getSourceTmxFileName());
                writer.addFileToPackage(alignmentUnit.getTargetTmxFileName());

                // write GAM file
                writer.addFileToPackage(alignmentUnit.getGamFileName());
            }
        }
        
        writer.close();
    }
    

    private TuvMappingHolder saveAlignedSegments(
        AlignmentResult p_alignmentResult)
        throws Exception
    {
        String userid = m_uploadUser == null ? "system" : m_uploadUser
                .getUserId();
        List tus = new ArrayList();

        Iterator it = p_alignmentResult.getAlignedSegments().iterator();
        while(it.hasNext())
        {
            AlignedSegments alignedSegments = (AlignedSegments)it.next();
            SegmentTmTu tu = alignedSegments.getAlignedSegment();
            List<BaseTmTuv> tuvs = tu.getTuvs();
            if (tuvs != null)
            {
                for (BaseTmTuv tuv : tuvs)
                {
                    if (tuv.getCreationUser() == null)
                    {
                        tuv.setCreationUser(userid);
                    }
                    if (tuv.getModifyUser() == null)
                    {
                        tuv.setModifyUser(userid);
                    }
                }
            }
            
            tus.add(tu);
        }

        TmCoreManager tmCoreManager = LingServerProxy.getTmCoreManager();
        return tmCoreManager.saveToSegmentTm(m_tm, tus, m_tmSaveMode);
    }
    

    private AlignmentStatus getAlignmentStatus()
    {
        AlignmentStatus status
            = new AlignmentStatus(m_projectName, "", getProjectFileName());

        int errorUnitNumber = 0;
        Iterator it = m_alignmentUnits.iterator();
        while(it.hasNext())
        {
            AlignmentUnit unit = (AlignmentUnit)it.next();
            String errorMessage = unit.getErrorMessage();
            if(errorMessage != null)
            {
                errorUnitNumber++;
                status.addErrorMessage(errorMessage);
            }
        }

        status.setTotalUnitNumber(m_alignmentUnits.size());
        status.setErrorUnitNumber(errorUnitNumber);
        
        return status;
    }
        

    private AlignmentUnit createAlignmentUnitFromExtractResult(
        AlignerExtractorResult p_sourceResult,
        AlignerExtractorResult p_targetResult,
        String p_sourceFileName, String p_targetFileName)
        throws Exception
    {
        String sourceDisplayName = "";
        if(p_sourceResult != null)
        {
            sourceDisplayName = p_sourceResult.getDisplayName();
        }
        
        //for ppt,pptx,xls,xlsx, only store one copy of original source files
        boolean canStoreNativeFormatDosc = true;
        if (sourceDisplayName.endsWith(".ppt") || sourceDisplayName.endsWith(".pptx"))
        {
        	if (!sourceDisplayName.startsWith("(slide0001)")) {
        		canStoreNativeFormatDosc = false;
        	}
        }
        if (sourceDisplayName.endsWith(".xls") || sourceDisplayName.endsWith(".xlsx"))
        {
        	if (!sourceDisplayName.startsWith("(tabstrip)")) {
        		canStoreNativeFormatDosc = false;
        	}
        }
        
        String targetDisplayName = "";
        if(p_targetResult != null)
        {
            targetDisplayName = p_targetResult.getDisplayName();
        }
        
        AlignmentUnit unit = new AlignmentUnit(
                sourceDisplayName, targetDisplayName, m_projectTmpDirectory);
                
        String errorMessage
            = getErrorMessage(p_sourceResult, p_targetResult);
        if(errorMessage != null)
        {
            unit.setErrorMessage(errorMessage);
            unit.setState(AlignmentUnit.ALIGN_FAILED);
        }
        else
        {
            unit.setSourceGxml(p_sourceResult.gxml, m_sourceLocale);
            unit.setTargetGxml(p_targetResult.gxml, m_targetLocale);

            CorpusManager corpusManager = ServerProxy.getCorpusManager();
            CorpusDoc sourceCorpusDoc
                = corpusManager.addNewSourceLanguageCorpusDoc(m_sourceLocale,
                    p_sourceFileName, p_sourceResult.gxml,
                    new File(AmbFileStoragePathUtils.getCxeDocDir(), p_sourceFileName),
                    canStoreNativeFormatDosc);
            
            CorpusDoc targetCorpusDoc 
                = corpusManager.addNewTargetLanguageCorpusDoc(sourceCorpusDoc,
                    m_targetLocale, p_targetResult.gxml, 
                    new File(AmbFileStoragePathUtils.getCxeDocDir(), p_targetFileName),
                    canStoreNativeFormatDosc);

            unit.setSourceCorpusDoc(sourceCorpusDoc);
            unit.setTargetCorpusDoc(targetCorpusDoc);
            
            unit.setState(AlignmentUnit.EXTRACTED);
        }

        return unit;
    }
    


    private String getErrorMessage(
        AlignerExtractorResult p_sourceResult,
        AlignerExtractorResult p_targetResult)
    {
        StringBuffer sb = new StringBuffer();
        
        if(p_sourceResult == null)
        {
            sb.append("There is no corresponding source document.\n\n");
        }
        else if(p_sourceResult.exception != null)
        {
            sb.append(p_sourceResult.exception
                .getMessage(Locale.getDefault()));
            sb.append("\n\n");
        }
        
        if(p_targetResult == null)
        {
            sb.append("There is no corresponding target document.\n\n");
        }
        else if(p_targetResult.exception != null)
        {
            sb.append(p_targetResult.exception
                .getMessage(Locale.getDefault()));
            sb.append("\n\n");
        }

        String result = null;
        if(sb.length() != 0)
        {
            result = sb.toString();
        }
        
        return result;
    }


    private void populateCorpusTm(
        AlignmentUnit p_alignmentUnit, TuvMappingHolder p_mappingHolder)
        throws Exception
    {
        CorpusManager corpusManager = ServerProxy.getCorpusManager();

        Map sourceCorpusSegments
            = p_mappingHolder.getMappingsByLocale(m_sourceLocale);
        Map targetCorpusSegments
            = p_mappingHolder.getMappingsByLocale(m_targetLocale);

        //add the source segments to corpus
        CorpusDoc sourceCorpusDoc = p_alignmentUnit.getSourceCorpusDoc();
        if (sourceCorpusSegments != null && sourceCorpusSegments.size() > 0)
        {
            List sourceCorpusSegmentList
                = new ArrayList(sourceCorpusSegments.values());
            addTuidToGxml(sourceCorpusDoc, sourceCorpusSegmentList);
            
            corpusManager.mapSegmentsToCorpusDoc(
                sourceCorpusSegmentList, sourceCorpusDoc);
        }

        //add the target segments to corpus
        CorpusDoc targetCorpusDoc = p_alignmentUnit.getTargetCorpusDoc();
        if (targetCorpusSegments != null && targetCorpusSegments.size() > 0)
        {
            List targetCorpusSegmentList
                = new ArrayList(targetCorpusSegments.values());
            addTuidToGxml(targetCorpusDoc, targetCorpusSegmentList);

            corpusManager.mapSegmentsToCorpusDoc(
                targetCorpusSegmentList, targetCorpusDoc);
        }
    }


    private void addTuidToGxml(CorpusDoc p_corpusDoc, List p_corpusMappings)
        throws Exception
    {
        byte[] gxml = ServerProxy.getNativeFileManager().getBytes(
            p_corpusDoc.getGxmlPath());

        GxmlReader gxmlReader = new GxmlReader();
        String newGxml = gxmlReader.processGxmlForCorpus(
            new String(gxml, "UTF-8"), p_corpusMappings);
        ServerProxy.getNativeFileManager().save(newGxml, "UTF8",
            p_corpusDoc.getGxmlPath());
    }


    public void setUploadUser(User user)
    {
        m_uploadUser = user;
    }
    
    
    
    
}
