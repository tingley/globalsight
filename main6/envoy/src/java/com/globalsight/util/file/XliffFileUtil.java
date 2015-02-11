/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.util.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.offline.XliffConstants;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.zip.ZipIt;

/**
 * Utility class used to handle Xliff files
 * 
 * @author Vincent
 * @date 12/12/2011
 * @version 1.0
 * @since 8.2.2
 */
public class XliffFileUtil
{
    private static final Logger logger = Logger.getLogger(XliffFileUtil.class
            .getName());
    public static final int KNOWN_FILE_FORMAT_XLIFF = 39;
    public static final int KNOWN_FILE_FORMAT_XLZ = 48;
    public static final String XLIFF_EXTENSION = ".xlf";
    public static final String XLIFF_LONG_EXTENSION = ".xliff";
    public static final String XLZ_EXTENSION = ".xlz";
    public static final String SEPARATE_FLAG = ".sub";

    /**
     * Handle a list of files to verify if they are Xliff files and for each
     * file, 1.If it is single Xliff file with only one <File> tags, need not
     * process 2.If it is Xliff file with multiple <File> tags, it needs to be
     * separated into single Xliff files with content in each <File> tags. All
     * separated files will have the same header and footer as original file.
     * 3.If it is XLZ file, then unpack the file first and process each Xliff in
     * the package with step #2.
     * 
     * @param p_fileList
     *            A list of original uploaded or selected files and their
     *            corresponding file profile
     * @return java.util.Hashtable<String, FileProfile> List of files that are
     *         processed.
     * 
     * @version 1.0
     * @since 8.2.2
     */
    public static Hashtable<String, FileProfile> processXliffFiles(
            Hashtable<String, FileProfile> p_fileList)
    {
        Hashtable<String, FileProfile> files = new Hashtable<String, FileProfile>();
        if (p_fileList == null || p_fileList.size() == 0)
            return files;

        String filename = "";
        FileProfile fp = null;
        try
        {
            Iterator<String> fileKeys = p_fileList.keySet().iterator();

            while (fileKeys.hasNext())
            {
                filename = fileKeys.next();
                fp = p_fileList.get(filename);

                switch ((int) fp.getKnownFormatTypeId())
                {
                    case KNOWN_FILE_FORMAT_XLIFF:
                        processMultipleFileTags(files, filename, fp);
                        break;
                    case KNOWN_FILE_FORMAT_XLZ:
                        separateXlzFile(files, filename, fp);
                        break;
                    default:
                        files.put(filename, fp);
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        return files;
    }

    /**
     * Verify if current file is in Xliff file format
     * 
     * @param p_filename
     *            File name
     * @return boolean Return true if file is in Xliff file format, otherwise
     *         return false
     * 
     * @version 1.0
     * @since 8.2.2
     */
    public static boolean isXliffFile(String p_filename)
    {
        if (StringUtil.isEmpty(p_filename))
            return false;

        String tmp = p_filename.trim().toLowerCase();
        return tmp.endsWith(XLIFF_EXTENSION)
                || tmp.endsWith(XLIFF_LONG_EXTENSION);
    }

    /**
     * Process Xliff file which has multiple <File> tags
     * 
     * If current file contains multiple <File> tags, it needs to be separated
     * into sub files in single file level. And the list of files needs to add
     * all separated files with corresponding file profiles. Otherwise, file and
     * its file profile are just put into the list.
     * 
     * @param p_files
     *            List of files contained all processed files
     * @param p_filename
     *            File name
     * @param p_fp
     *            File profile of file
     * 
     * @version 1.0
     * @since 8.2.2
     */
    public static void processMultipleFileTags(
            Hashtable<String, FileProfile> p_files, String p_filename,
            FileProfile p_fp)
    {
        MultipleFileTagsXliff multipleFileTagsXliff = processMultipleFileTags(p_filename);
        if (multipleFileTagsXliff != null)
        {
            ArrayList<String> separatedFile = multipleFileTagsXliff
                    .getSeparatedFiles();
            for (int j = 0; j < separatedFile.size(); j++)
            {
                p_files.put(separatedFile.get(j), p_fp);
            }
        }
        else
        {
            p_files.put(p_filename, p_fp);
        }
    }

    /**
     * Process one Xliff file which has multiple <File> tags
     * 
     * @param p_relativeFilename
     *            The relative name of file
     * @return MultipleFileTagsXliff Object contains basic information and all
     *         extracted Xliff files
     * 
     * @version 1.0
     * @since 8.2.2
     */
    public static MultipleFileTagsXliff processMultipleFileTags(
            String p_relativeFilename)
    {
        String absoluteFilename = generateAbsolutePath(p_relativeFilename);
        boolean hasMultipleFileTags = XliffFileUtil
                .isMultipleFileTags(absoluteFilename);
        if (hasMultipleFileTags)
        {
            String fileContent = getFileContent(absoluteFilename);
            return separateMultipleFileTagsFile(absoluteFilename, fileContent);
        }
        else
            return null;
    }

    /**
     * Check if specified file contains multiple <File> tags
     * 
     * @param p_filename
     *            File name
     * @return Return true if file contains multiple <File> tags, otherwise
     *         return false
     * 
     * @version 1.0
     * @since 8.2.2
     */
    public static boolean isMultipleFileTags(String p_filename)
    {
        if (StringUtil.isEmpty(p_filename))
            return false;

        int numOfFileTags = 0;
        try
        {
            SAXReader saxReader = new SAXReader();
            Document document = null;
            Element rootElement = null;

            File file = new File(p_filename);
            if (file.exists() && file.isFile())
            {
                document = saxReader.read(file);
                rootElement = document.getRootElement();
                String tag = "";
                for (Iterator<Element> iterator = rootElement.elementIterator(); iterator
                        .hasNext();)
                {
                    tag = iterator.next().getName().trim().toLowerCase();
                    if ("file".equals(tag))
                        numOfFileTags++;
                }
            }
            return numOfFileTags > 1;
        }
        catch (Exception e)
        {
            logger.error(
                    "Can not verify if current file contains multiple file tags.",
                    e);
            return false;
        }
    }

    public static boolean containsFileTag(String p_filename)
    {
        if (StringUtil.isEmpty(p_filename))
            return false;

        try
        {
            SAXReader saxReader = new SAXReader();
            Document document = null;
            Element rootElement = null;

            File file = new File(p_filename);
            if (file.exists() && file.isFile())
            {
                document = saxReader.read(file);
                rootElement = document.getRootElement();
                String tag = "";
                List fileTags = rootElement.elements("file");
                if (fileTags != null)
                {
                    for (int i = 0; i < fileTags.size(); i++)
                    {
                        Element element = (Element) fileTags.get(i);
                        String attr = element.attributeValue("tool");
                        if (attr == null)
                            return false;
                        else
                        {
                            if (attr.toLowerCase().contains("worldserver"))
                                return true;
                        }
                    }
                }
            }
            return false;
        }
        catch (Exception e)
        {
            logger.error(
                    "Can not verify if current file contains multiple file tags.",
                    e);
            return false;
        }
    }

    /**
     * Process Xliff files in target pages, used in exporting process
     * 
     * @param p_workflow
     *            Work flow which contains all target pages
     * 
     * @version 1.0
     * @since 8.2.2
     */
    public static void processXliffFiles(Workflow p_workflow)
    {
        if (p_workflow == null || p_workflow.getAllTargetPages() == null)
            return;
        try
        {
            int index = -1;
            String sourceFilename = "";
            String filename = "";
            String tmp = "";
            String exportLocation = "";
            File file = null;
            ArrayList<String> subPaths = new ArrayList<String>();
            Vector<TargetPage> targetPages = p_workflow.getAllTargetPages();

            String baseDocDir = AmbFileStoragePathUtils.getCxeDocDirPath();
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(CompanyWrapper
                    .getCurrentCompanyId())
                    && !CompanyWrapper.SUPER_COMPANY_ID.equals(String
                            .valueOf(p_workflow.getJob().getCompanyId())))
            {
                baseDocDir += baseDocDir + File.separator;
            }

            // Get sub folders which are split by xliff file with multiple
            // <File> tags
            for (TargetPage targetPage : targetPages)
            {
                exportLocation = targetPage.getExportSubDir();
                if (exportLocation.startsWith("\\")
                        || exportLocation.startsWith("/"))
                    exportLocation = exportLocation.substring(1);
                sourceFilename = targetPage.getSourcePage().getExternalPageId();
                sourceFilename = sourceFilename.replace("/", File.separator);
                filename = sourceFilename.substring(
                        sourceFilename.indexOf(File.separator) + 1,
                        sourceFilename.lastIndexOf(File.separator));
                index = sourceFilename.toLowerCase().lastIndexOf(
                        SEPARATE_FLAG + File.separator);
                if (index != -1)
                {
                    tmp = baseDocDir + File.separator
                            + sourceFilename.substring(0, index);
                    file = new File(tmp);
                    if (file.exists() && file.isFile()
                            && !subPaths.contains(filename))
                        subPaths.add(filename);
                }
            }
            p_workflow.getJob().getSourceLocale().toString();
            String[] subFiles = null, sortedSubFiles = null;
            for (String subPath : subPaths)
            {
                tmp = baseDocDir + File.separator + exportLocation
                        + File.separator + subPath;
                file = new File(tmp);
                subFiles = file.list();
                if (subFiles == null)
                	continue;
                sortedSubFiles = sortSubFiles(subFiles);
                combineSeparatedXliffFiles(tmp, sortedSubFiles);
            }
        }
        catch (Exception e)
        {
            logger.error("Error found in processXliffFiles", e);
        }

    }

    public static String getSourceFile(String externalPageId, String companyId)
    {
    	if (!isXliffFile(externalPageId))
    		return null;
    	
    	 String baseCxeDocDir = AmbFileStoragePathUtils.getCxeDocDirPath()
                 .concat(File.separator);
    	 String companyName = CompanyWrapper.getCompanyNameById(companyId);

         if (CompanyWrapper.SUPER_COMPANY_ID.equals(CompanyWrapper
                 .getCurrentCompanyId())
                 && !CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
         {
             baseCxeDocDir += companyName + File.separator;
         }
         
         int index = externalPageId.lastIndexOf(SEPARATE_FLAG + File.separator);
         String tmp = "";
         String sourceFilename = "";
         File sourceFile = null;
         File sourcePath = null, targetPath = null;
         
         if (index != -1)
         {
             tmp = externalPageId.substring(0, index);
             sourceFilename = baseCxeDocDir + tmp;
             sourceFile = new File(sourceFilename);
             if (sourceFile.exists() && sourceFile.isFile())
             {
                 return sourceFile.getAbsolutePath();
             }
         }
         
         tmp = externalPageId.substring(0,
        		 externalPageId.lastIndexOf(File.separator));
         sourceFilename = baseCxeDocDir + tmp
                 + XliffFileUtil.XLZ_EXTENSION;
         sourceFile = new File(sourceFilename);
         if (sourceFile.exists() && sourceFile.isFile())
         {
        	 return sourceFile.getAbsolutePath();
         }
         
         return null;
    }
    
    
    /**
     * Process the files if the source file is with XLZ file format
     * 
     * @param p_workflow
     * 
     * @author Vincent Yan, 2011/01/27
     * @version 1.1
     * @since 8.1
     */
    public static void processXLZFiles(Workflow p_workflow)
    {
        if (p_workflow == null || p_workflow.getAllTargetPages().size() == 0)
            return;

        TargetPage tp = null;
        String externalId = "";
        String tmp = "", exportDir = "";
        String sourceFilename = "";
        String sourceDir = "", targetDir = "";
        File sourceFile = null;
        File sourcePath = null, targetPath = null;
        ArrayList<String> xlzFiles = new ArrayList<String>();

        try
        {
            Vector<TargetPage> targetPages = p_workflow.getAllTargetPages();
            String baseCxeDocDir = AmbFileStoragePathUtils.getCxeDocDirPath()
                    .concat(File.separator);

            Job job = p_workflow.getJob();
            String companyId = String.valueOf(job.getCompanyId());
            String companyName = CompanyWrapper.getCompanyNameById(companyId);

            if (CompanyWrapper.SUPER_COMPANY_ID.equals(CompanyWrapper
                    .getCurrentCompanyId())
                    && !CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
            {
                baseCxeDocDir += companyName + File.separator;
            }
            int index = -1;
            ArrayList<String> processed = new ArrayList<String>();
            for (int i = 0; i < targetPages.size(); i++)
            {
                tp = (TargetPage) targetPages.get(i);
                externalId = FileUtil.commonSeparator(tp.getSourcePage()
                        .getExternalPageId());
                index = externalId.lastIndexOf(SEPARATE_FLAG + File.separator);
                if (index != -1)
                {
                    tmp = externalId.substring(0, index);
                    sourceFilename = baseCxeDocDir + tmp;
                    sourceFile = new File(sourceFilename);
                    if (sourceFile.exists() && sourceFile.isFile())
                    {
                        // Current file is a separated file from big Xliff file
                        // with
                        // multiple <File> tags
                        externalId = tmp;
                    }
                }
                if (processed.contains(externalId))
                    continue;
                else
                    processed.add(externalId);

                if (isXliffFile(externalId))
                {
                    tmp = externalId.substring(0,
                            externalId.lastIndexOf(File.separator));
                    sourceFilename = baseCxeDocDir + tmp
                            + XliffFileUtil.XLZ_EXTENSION;
                    sourceFile = new File(sourceFilename);
                    if (sourceFile.exists() && sourceFile.isFile())
                    {
                        // source file is with xlz file format
                        exportDir = tp.getExportSubDir();
                        if (exportDir.startsWith("\\")
                                || exportDir.startsWith("/"))
                            exportDir = exportDir.substring(1);
                        targetDir = baseCxeDocDir + exportDir
                                + tmp.substring(tmp.indexOf(File.separator));
                        if (!xlzFiles.contains(targetDir))
                            xlzFiles.add(targetDir);

                        // Get exported target path
                        targetPath = new File(targetDir);

                        // Get source path
                        sourceDir = baseCxeDocDir + tmp;
                        sourcePath = new File(sourceDir);

                        // Copy all files extracted from XLZ file from source
                        // path to exported target path
                        // Because Xliff files can be exported by GS
                        // automatically, then ignore them and
                        // just copy the others file to target path
                        File[] files = sourcePath.listFiles();
                        for (File f : files)
                        {
                            if (f.isDirectory())
                                continue;
                            if (isXliffFile(f.getAbsolutePath()))
                                continue;
                            org.apache.commons.io.FileUtils
                                    .copyFileToDirectory(f, targetPath);
                        }
                    }
                }
            }

            // Generate exported XLZ file and remove temporary folders
            for (int i = 0; i < xlzFiles.size(); i++)
            {
                targetDir = xlzFiles.get(i);
                targetPath = new File(targetDir);
                File xlzFile = new File(targetDir + XLZ_EXTENSION);
                if (!targetPath.exists() || xlzFile.exists())
                {
                    continue;
                }
                ZipIt.addEntriesToZipFile(xlzFile, targetPath.listFiles(),
                        true, "");
            }
        }
        catch (Exception e)
        {
            logger.error("Error in WorkflowManagerLocal.processXLZFiles. ");
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Separate content in a Xliff file with multiple <File> tags into single
     * Xliff file level. For example, Original file named as
     * <i>Demo_Xliff.xlf</i> has 2 <File> tags in it, ...... <file ...
     * original='Test01_File.xml' ... > ... <iws:asset-data>
     * <iws:seg_asset_id>1522335</iws:seg_asset_id> ... </iws:asset-data> ....
     * </file> <file ... original='Test02_File.xml' ... > ... <iws:asset-data>
     * <iws:seg_asset_id>1522336</iws:seg_asset_id> ... </iws:asset-data> ....
     * </file> and separated files will be named as below format,
     * Demo_Xliff.xlf.sub\Test01_File.xml_1522335_1.xlf
     * Demo_Xliff.xlf.sub\Test02_File.xml_1522336_2.xlf
     * 
     * @param absoluteFilename
     *            File name with absolute path
     * @param fileContent
     * @return MultipleFileTagsXliff Object contains all separated files
     * 
     * @version 1.0
     * @since 8.2.2
     */
    private static MultipleFileTagsXliff separateMultipleFileTagsFile(
            String absoluteFilename, String fileContent)
    {
        MultipleFileTagsXliff multipleFileTagsXliff = new MultipleFileTagsXliff();
        try
        {
            separateFileContent(multipleFileTagsXliff, absoluteFilename,
                    fileContent);
        }
        catch (Exception e)
        {
            logger.error("Error found in splitMultipleFileTagsFile", e);
        }
        return multipleFileTagsXliff;
    }

    /**
     * Separate Xliff file content with multiple <File> tags
     * 
     * @param multipleFileTagsXliff
     *            Object contains data of separated files
     * @param absoluteFilename
     *            File name with absolute path
     * @param fileContent
     *            File content of original Xliff file
     * 
     * @version 1.0
     * @since 8.2.2
     */
    private static void separateFileContent(
            MultipleFileTagsXliff multipleFileTagsXliff,
            String absoluteFilename, String fileContent)
    {
        String header = "";
        String content = "";
        String footer = "";

        int contentBeginIndex = -1, contentEndIndex = -1;

        contentBeginIndex = fileContent.indexOf("<file ");
        // Get header of original Xliff file
        header = contentBeginIndex != -1 ? fileContent.substring(0,
                contentBeginIndex) : "";
        contentEndIndex = fileContent.lastIndexOf("</file>")
                + "</file>".length();
        // Get footer of original Xliff file
        footer = contentEndIndex != -1 ? fileContent.substring(contentEndIndex)
                : "";

        // Get main content of original Xliff file
        content = fileContent.substring(contentBeginIndex, contentEndIndex);

        multipleFileTagsXliff.setHeader(header);
        multipleFileTagsXliff.setContent(content);
        multipleFileTagsXliff.setFooter(footer);
        generateSeparatedFiles(multipleFileTagsXliff, absoluteFilename, header,
                content, footer);
    }

    /**
     * Generate separated files in single file level according with content in
     * orignal Xliff file
     * 
     * @param multipleFileTagsXliff
     *            Object contains all separated files
     * @param absoluteFilename
     *            File name with absolute path
     * @param header
     *            Header content of original Xliff file
     * @param content
     *            Main content of original Xliff file
     * @param footer
     *            Footer content of original Xliff file
     * 
     * @version 1.0
     * @since 8.2.2
     */
    private static void generateSeparatedFiles(
            MultipleFileTagsXliff multipleFileTagsXliff,
            String absoluteFilename, String header, String content,
            String footer)
    {
        try
        {
            String absolutePath = getBaseAbsoluatePath(absoluteFilename);
            String mainName = getMainFilename(absoluteFilename);
            String separatedDirPath = mainName + SEPARATE_FLAG;
            String absouateSeparatedPath = absolutePath + File.separator
                    + separatedDirPath;
            File file = new File(absouateSeparatedPath);
            file.mkdirs();

            int lengthOfFileEndTag = "</file>".length();
            int beginIndex = -1, endIndex = -1;
            String subContent = "";

            SAXReader saxReader = new SAXReader();
            Document document = null;
            Element rootElement = null, fileTagElement = null, fileHeaderElement = null;
            String originalSubFilename = "";
            String segAssetId = "";
            BufferedWriter fout = null;
            ArrayList<String> separatedFiles = new ArrayList<String>();
            String subFilename = "";
            int count = 1;
            while ((beginIndex = content.indexOf("<file ")) > -1)
            {
                endIndex = content.indexOf("</file>") + lengthOfFileEndTag;
                subContent = header + content.substring(beginIndex, endIndex)
                        + footer;

                // Use XML parser to get element value
                document = saxReader.read(new StringReader(subContent));
                rootElement = document.getRootElement();
                fileTagElement = rootElement.element("file");
                originalSubFilename = fileTagElement.attributeValue("original")
                        .replace("/", File.separator);
                originalSubFilename = originalSubFilename
                        .substring(originalSubFilename
                                .lastIndexOf(File.separator) + 1);
                fileHeaderElement = fileTagElement.element("header");
                if (fileHeaderElement != null
                        && fileHeaderElement.element("asset-data") != null)
                {
                    segAssetId = fileHeaderElement.element("asset-data")
                            .elementText("seg_asset_id");
                }

                // Generate file name of separated file
                subFilename = originalSubFilename + "_" + segAssetId + "_"
                        + count + ".xlf";

                // Write separated file into disk
                file = new File(absouateSeparatedPath, subFilename);
                fout = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));
                fout.write(subContent);
                fout.close();

                separatedFiles.add(getRelativePath(file.getAbsolutePath()));

                content = content.substring(endIndex);
                count++;
            }
            multipleFileTagsXliff.setCount(count);
            multipleFileTagsXliff.setSeparatedFiles(separatedFiles);
            multipleFileTagsXliff.setSeparatedFolderName(separatedDirPath);
        }
        catch (Exception e)
        {
            logger.error("Error in generateExtractedFile.", e);
        }
    }

    /**
     * Process file in XLZ file format. XLZ is special file format and a
     * collection of some Xliff files, termbase file, XML or other format files
     * as packed file. So if one file is in XLZ file format, it needs to be
     * unpacked all contained files and use these extracted files to create GS
     * job.
     * 
     * For now, GS just handle Xliff files in one XLZ file.
     * 
     * @param p_files
     *            List of files with corresponding file profile
     * @param p_relativeFilename
     *            The relative path of file
     * @param p_fp
     *            File profile of file
     * @throws Exception
     * @throws RemoteException
     * @throws NamingException
     * 
     * @version 1.0
     * @since 8.2.2
     */
    private static void separateXlzFile(Hashtable<String, FileProfile> p_files,
            String p_relativeFilename, FileProfile p_fp) throws Exception,
            RemoteException, NamingException
    {
        String xlzPath = "", extractedFile = "", baseXlzFilePath = "";
        String tmp = "";
        ArrayList<String> separatedFiles = null;
        long referenceFPId = 0l;

        String absoluteFilename = AmbFileStoragePathUtils.getCxeDocDirPath()
                + File.separator + p_relativeFilename;
        xlzPath = absoluteFilename.substring(0,
                absoluteFilename.lastIndexOf("."));
        baseXlzFilePath = p_relativeFilename.substring(0,
                p_relativeFilename.lastIndexOf("."));

        separatedFiles = ZipIt.unpackZipPackage(absoluteFilename, xlzPath);

        referenceFPId = p_fp.getReferenceFP();
        p_fp = ServerProxy.getFileProfilePersistenceManager()
                .getFileProfileById(referenceFPId, false);

        for (int i = 0; i < separatedFiles.size(); i++)
        {
            extractedFile = separatedFiles.get(i);
            if (XliffFileUtil.isXliffFile(extractedFile))
            {
                tmp = baseXlzFilePath.concat(File.separator).concat(
                        extractedFile);
                // Need to handle separated Xliff files from XLZ to sure if the
                // they have multiple <File> tags in each one
                processMultipleFileTags(p_files, tmp, p_fp);
            }
        }
    }

    /**
     * Get relative file path of specified file
     * 
     * @param p_absoluteFilename
     *            The absolute file path
     * @return String Return relative file path
     * 
     * @version 1.0
     * @since 8.2.2
     */
    private static String getRelativePath(String p_absoluteFilename)
    {
        String baseCxeDirPath = AmbFileStoragePathUtils.getCxeDocDirPath();

        return p_absoluteFilename.substring(baseCxeDirPath.length() + 1);
    }

    /**
     * Get parent path of file
     * 
     * @param p_absoluteFilename
     *            The file name with absolute path
     * @return String Parent path
     * 
     * @version 1.0
     * @since 8.2.2
     */
    private static String getBaseAbsoluatePath(String p_absoluteFilename)
    {
        return p_absoluteFilename.substring(0,
                p_absoluteFilename.lastIndexOf(File.separator));
    }

    /**
     * Get the main name of specified file
     * 
     * @param p_absoluteFilename
     *            The file name with absolute path
     * @return String Main name of file
     * 
     * @version 1.0
     * @since 8.2.2
     */
    private static String getMainFilename(String p_absoluteFilename)
    {
        return p_absoluteFilename.substring(p_absoluteFilename
                .lastIndexOf(File.separator) + 1);
    }

    /**
     * Get file content with UTF-8 encoding and Unix/Linux/Mac return method
     * 
     * @param p_filename
     *            File name
     * @return String Content of file
     * 
     * @version 1.0
     * @since 8.2.2
     */
    private static String getFileContent(String p_filename)
    {
        StringBuffer fileContent = new StringBuffer(4096);
        BufferedReader fin = null;

        try
        {
            fin = new BufferedReader(new InputStreamReader(new FileInputStream(
                    new File(p_filename)), "UTF-8"));
            String line = "";
            while ((line = fin.readLine()) != null)
            {
                fileContent.append(line).append("\n");
            }
            return fileContent.toString();
        }
        catch (Exception e)
        {
            logger.error("Can not get file content correctly. File -- "
                    + p_filename, e);
            return "";
        }
        finally
        {
            try
            {
                if (fin != null)
                    fin.close();
            }
            catch (IOException e2)
            {
                logger.error("Can not close file correctly.", e2);
            }
        }
    }

    /**
     * Generate absolute path according with specified relative path
     * 
     * @param p_relativePath
     *            The relative path
     * @return String Absolute path
     * 
     * @version 1.0
     * @since 8.2.2
     */
    private static String generateAbsolutePath(String p_relativePath)
    {
        String baseCxeDirPath = AmbFileStoragePathUtils.getCxeDocDirPath();
        String path = baseCxeDirPath.concat(File.separator).concat(
                p_relativePath);
        return path.replace("/", File.separator);
    }

    /**
     * Combine separated Xliff files into one big file and each file is as
     * <File> part in combined file.
     * 
     * @param p_subDir
     *            The folder path used to store separated files
     * @param sortedSubFiles
     *            Separated files in order
     * 
     * @version 1.0
     * @since 8.2.2
     */
    private static void combineSeparatedXliffFiles(String p_subDir,
            String[] sortedSubFiles)
    {
        File file = null, xliffFile = null;
        String fileContent = "";
        String header = "", footer = "";
        String xliffFilename = "";
        int beginIndex = -1, endIndex = -1;
        BufferedWriter fout = null;
        try
        {
            xliffFilename = p_subDir.substring(0,
                    p_subDir.lastIndexOf(SEPARATE_FLAG));
            xliffFile = new File(xliffFilename);
            fout = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(xliffFile), "UTF-8"));

            for (String f : sortedSubFiles)
            {
                file = new File(p_subDir, f);
                fileContent = XliffFileUtil.getFileContent(file
                        .getAbsolutePath());
                if (StringUtil.isEmpty(header))
                {
                    beginIndex = fileContent.indexOf("<file ");
                    header = fileContent.substring(0, beginIndex);
                    endIndex = fileContent.lastIndexOf("</file>") + 7;
                    footer = fileContent.substring(endIndex);
                    fout.write(header);
                    fout.write(fileContent.substring(beginIndex, endIndex));
                }
                else
                {
                    beginIndex = fileContent.indexOf("<file ");
                    endIndex = fileContent.lastIndexOf("</file>") + 7;
                    fout.write(fileContent.substring(beginIndex, endIndex));
                }
            }
            fout.write(footer);
            fout.close();

            org.apache.commons.io.FileUtils.deleteDirectory(new File(p_subDir));
        }
        catch (Exception e)
        {
            logger.error("Error found in combineSplitXliffFiles. ", e);
        }

    }

    /**
     * Sort the separated file by the suffix number in the filename
     * 
     * @param p_subFiles
     *            Separated files without order
     * @return String[] Sorted separated file array
     * 
     * @version 1.0
     * @since 8.2.2
     */
    private static String[] sortSubFiles(String[] p_subFiles)
    {
        if (p_subFiles == null || p_subFiles.length == 0)
            return p_subFiles;

        int length = p_subFiles.length;
        String[] sortedSubFiles = new String[length];
        String tmp = "";
        int index = -1;
        for (int i = 0; i < length; i++)
        {
            tmp = p_subFiles[i];
            tmp = tmp.substring(0, tmp.lastIndexOf("."));
            tmp = tmp.substring(tmp.lastIndexOf("_") + 1);
            index = Integer.parseInt(tmp) - 1;
            sortedSubFiles[index] = p_subFiles[i];
        }
        return sortedSubFiles;
    }

    public static String getSourceFileType(Document p_doc)
    {
        return getItemFromNote(p_doc, XliffConstants.DOCUMENT_FORMAT);
    }

    public static String getTaskId(Document p_doc)
    {
        return getItemFromNote(p_doc, XliffConstants.TASK_ID);
    }

    public static String getWorkflowId(Document p_doc)
    {
        return getItemFromNote(p_doc, XliffConstants.WORKFLOW_ID);
    }

    public static String getPageId(Document p_doc)
    {
        return getItemFromNote(p_doc, XliffConstants.PAGE_ID);
    }

    private static String getItemFromNote(Document p_doc, String item)
    {
        String result = null;
        try
        {
            Element root = p_doc.getRootElement();
            Element noteElement = root.element(XliffConstants.FILE)
                    .element(XliffConstants.HEADER)
                    .element(XliffConstants.NOTE);
            String notes = noteElement.getText();
            int index = notes.indexOf(item);
            notes = notes.substring(index);
            notes = notes.substring(0, notes.indexOf("#")).trim();
            notes = notes.substring(notes.indexOf(":") + 1).trim();
            result = notes;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }

        return result;
    }

    /**
     * For xlz/xlf job, get its original real source file instead of the
     * extracted file.
     * 
     * @param srcFile
     * @return File
     */
    public static File getOriginalRealSoureFile(File srcFile)
    {
        if (!srcFile.exists() || !srcFile.isFile())
            return null;

        if (!isXliffFile(srcFile.getName()))
            return srcFile;

        File result = srcFile;
        // Check if it is a sub xlf file from a xlf file that has multiple
        // <file> elements.
        HashMap<String, Object> map = getPossibleOriginalXlfSourceFile(result);
        Boolean isSubFile = (Boolean) map.get("isSubFile");
        if (isSubFile)
        {
            result = (File) map.get("sourceFile");
        }

        // Check if it is from a xlz file
        HashMap<String, Object> map2 = getPossibleOriginalXlzSourceFile(result);
        Boolean isFromXlzFile = (Boolean) map2.get("isFromXlzFile");
        if (isFromXlzFile)
        {
            result = (File) map2.get("sourceFile");
        }

        return result;
    }

    private static HashMap<String, Object> getPossibleOriginalXlfSourceFile(
            File xlfFile)
    {
        HashMap<String, Object> result = new HashMap<String, Object>();
        File wantedFile = xlfFile;
        Boolean isSubFile = false;
        try
        {
            String parentFolderName = xlfFile.getParentFile().getName();
            if (parentFolderName.endsWith(".sub"))
            {
                File grandFile = xlfFile.getParentFile().getParentFile();
                String fileName = parentFolderName.substring(0,
                        parentFolderName.length() - 4);
                File file = new File(grandFile, fileName);
                if (file.exists() && file.isFile()
                        && isXliffFile(file.getName()))
                {
                    wantedFile = file;
                    isSubFile = true;
                }
            }
        }
        catch (Exception e)
        {

        }

        result.put("sourceFile", wantedFile);
        result.put("isSubFile", isSubFile);
        return result;
    }

    private static HashMap<String, Object> getPossibleOriginalXlzSourceFile(
            File file)
    {
        HashMap<String, Object> result = new HashMap<String, Object>();
        File wantedFile = file;
        Boolean isFromXlzFile = false;

        try
        {
            File parentFile = file.getParentFile();
            File grandFile = parentFile.getParentFile();
            File f = new File(grandFile, parentFile.getName() + ".xlz");
            if (f.exists() && f.isFile())
            {
                wantedFile = f;
                isFromXlzFile = true;
            }
        }
        catch (Exception e)
        {

        }

        result.put("sourceFile", wantedFile);
        result.put("isFromXlzFile", isFromXlzFile);
        return result;
    }
}