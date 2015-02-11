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
package com.globalsight.cxe.adapter.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.entity.xmldtd.XmlDtd;
import com.globalsight.cxe.entity.xmldtd.XmlDtdImpl;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.everest.cvsconfig.CVSUtil;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobEditionInfo;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.DtdException;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.XmlDtdManager;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowManagerLocal;
import com.globalsight.ling.common.MapOfHtmlEntity;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.ProcessRunner;
import com.globalsight.util.file.XliffFileUtil;

/**
 * Helper class used by the FileSystemAdapter for exporting
 */
public class Exporter
{
    private String m_cxeDocsDir;
    private CxeMessage m_cxeMessage;
    private org.apache.log4j.Logger m_logger;
    private String m_localeSubDir = null;
    private String m_formatType = null;
    private String m_sourceFileName = null;
    private String m_exportLocation = null;
    private String m_filename;
    private String m_dataSourceId = null;
    private String m_displayName = null;
    private String m_fileProfileId = null;
    private String m_batchId = null;
    private Integer m_pageCount = null;
    private Integer m_pageNumber = null;
    private FileProfile fileProfile = null;
    private XmlDtdImpl xmlDtd = null;
    private String m_messageId = null;

    private static Map<String, FileState[]> FILE_STATES = new HashMap<String, FileState[]>();

    public static final String XML_DTD_ID = "sendEmail";

    private static String lineSeparator = (String) java.security.AccessController
            .doPrivileged(new sun.security.action.GetPropertyAction(
                    "line.separator"));

    static private final org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(Exporter.class);

    /**
     * Creates an Exporter object
     * 
     * @param p_cxeMessage
     *            a CxeMessage to work from
     */
    Exporter(CxeMessage p_cxeMessage, org.apache.log4j.Logger p_logger)
            throws GeneralException
    {
        m_cxeMessage = p_cxeMessage;
        m_logger = p_logger;
        m_cxeDocsDir = SystemConfiguration.getInstance().getStringParameter(
                SystemConfigParamNames.CXE_DOCS_DIR);
    }

    /**
     * Actually performs the write-back to the file system.
     * <p>
     * For automatic export (when submitting job via DesktopIcon), GS will check
     * associated File Profile for "Enable Unicode Escape" setting (dis-regard
     * previous comment about hiding this feature)
     * <ul>
     * <li>a. If it is checked, GS will Unicode escape ONLY the EXTENDED
     * CHARACTERS for ALL target encoding.</li>
     * <li>b. If it is not checked, GS WILL NOT Unicode escape the extended
     * characters IF the target encoding is UTF-8. For other target encoding
     * like ISO-8859-1, it will works as usual which GS will Unicode escape the
     * extended characters.</li>
     * </ul>
     * 
     * For manual export (in Job: Workflow locale/Export), the behavior will
     * remains the same.
     * <p>
     * I) If "Enable Unicode Escape" is checked in File Profile, GS will Unicode
     * escape ONLY the EXTENDED CHARACTERS for ALL target encoding (including
     * UTF-8, UTF-16).
     * <p>
     * II) If "Enable Unicode Escape" is NOT checked in File Profile then check
     * the following:
     * <ul>
     * <li>1. If one selects "UTF-8" for Character Encoding: No Unicode escape
     * for extended characters.</li>
     * <li>2. If one selects others like ISO-8859-1: Only extended characters
     * will be Unicode escaped.</li>
     * <li>3. If one selects Unicode Escape: ALL Characters will be Unicode
     * escaped.</li>
     * </ul>
     * 
     * @return New CxeMessage result
     */
    CxeMessage export()
    {
        CxeMessage exportStatusMsg;
        String finalFileName = null;

        try
        {
            Logger.writeDebugFile("fsta_ef.xml", m_cxeMessage.getEventFlowXml());

            parseEventFlowXml();

            finalFileName = determineFinalFileName();

            File finalFile = new File(finalFileName);
            finalFile.getParentFile().mkdirs();

            if (m_formatType.equals("xptag"))
            {
                m_logger.debug("Special export handling for xptag file.");
                FileMessageData fmd = (FileMessageData) m_cxeMessage
                        .getMessageData();
                fmd.operatingSystemSafeCopyTo(finalFile);
            }
            else
            {
                m_cxeMessage.getMessageData().copyTo(finalFile);
            }

            String fileTargetEncoding = "UTF-8";
            Object messageTargetCharset = m_cxeMessage.getParameters().get(
                    "TargetCharset");

            if (messageTargetCharset != null)
            {
                fileTargetEncoding = (String) messageTargetCharset;
            }

            // added by Walter, In GSEdition, the ServerB's exported xliff file
            // needed
            // to be uploaded in serverA, if the xliff file have no bom format,
            // it will throw exception when check.
            WorkflowManagerLocal wmanager = new WorkflowManagerLocal();
            Workflow wf = wmanager.getWorkflowById((Long.parseLong(m_cxeMessage
                    .getParameters().get("WorkflowId").toString())));

            if ("xlz".equals(m_formatType) || "xlf".equals(m_formatType))
            {
                String fileEncoding = FileUtil.guessEncoding(finalFile);
                if (fileEncoding == null)
                {
                    fileEncoding = fileTargetEncoding;
                }

                changeTargetLocale(wf.getTargetLocale().toString(), finalFile,
                        fileEncoding);

                JobEditionInfo je = getGSEditionJobByJobID(wf.getJob().getId());
                if (je != null)
                {
                    FileUtil.addBom(finalFile, fileTargetEncoding);
                }
            }

            exportStatusMsg = makeExportSuccessMessage(finalFile);
            BaseAdapter.preserveOriginalFileContent(
                    m_cxeMessage.getMessageData(),
                    exportStatusMsg.getParameters());

            m_cxeMessage.setDeleteMessageData(true);

            // Retrieve the scripts on Import&Export in the file profile.
            FileProfile fp = HibernateUtil.get(FileProfileImpl.class,
                    Long.parseLong(m_dataSourceId), false);

            // Check if unicode escape for "properties" and "js" files
            if ("javaprop".equals(m_formatType)
                    || "javascript".equals(m_formatType))
            {
                String targetEncoding = fileTargetEncoding;
                targetEncoding = targetEncoding.toLowerCase();

                // At this moment,only extended characters are unicode escaped
                if (!fp.getUnicodeEscape())
                {
                    Object unicodeEscape = m_cxeMessage.getParameters().get(
                            "UnicodeEscape");
                    // the "UnicodeEscape" parameter is from the export page's
                    // "Character Encoding"
                    if (unicodeEscape != null
                            && "true".equalsIgnoreCase((String) unicodeEscape)
                            && finalFileName.endsWith(".properties"))
                    {
                        unicodeAllEscape(finalFileName, targetEncoding);
                    }
                    else
                    {
                        notUnicodeEscape(finalFileName, targetEncoding);
                        if (finalFileName.endsWith(".properties"))
                        {
                            handleExtraEscapeCharacter(finalFileName,
                                    targetEncoding);
                        }
                    }
                }
                else if (finalFileName.endsWith(".properties"))
                {
                    handleExtraEscapeCharacter(finalFileName, targetEncoding);
                }
            }

            FileInputStream fis = null;
            try
            {
                if (FileUtil.isNeedBOMProcessing(finalFileName))
                {
                    // BOM Processing
                    int bomType = ((Integer) m_cxeMessage.getParameters().get(
                            "BOMType")).intValue();
                    if (bomType == ExportConstants.NOT_SELECTED)
                    {
                        bomType = fp.getBOMType();
                    }
                    int sourcePageBomType = ((Integer) m_cxeMessage
                            .getParameters().get("SourcePageBomType"))
                            .intValue();
                    byte[] fileContent = null;
                    if (FileUtil.isUTFFormat(fileTargetEncoding))
                    {
                        fis = new FileInputStream(finalFile);
                        fileContent = new byte[fis.available()];
                        fis.read(fileContent);

                        switch (sourcePageBomType)
                        {
                            case ExportConstants.UTF8_WITH_BOM:
                                if (FileUtil.UTF8.equals(fileTargetEncoding))
                                {
                                    if (bomType == ExportConstants.UTF_BOM_PRESERVE
                                            || bomType == ExportConstants.UTF_BOM_ADD)
                                    {
                                        FileUtil.addBom(finalFile,
                                                fileTargetEncoding);
                                    }
                                    else if (bomType == ExportConstants.UTF_BOM_REMOVE)
                                    {
                                        String encoding = FileUtil
                                                .guessEncoding(finalFile);
                                        if (FileUtil.UTF8.equals(encoding))
                                        {
                                            FileUtil.writeFile(
                                                    finalFile,
                                                    new String(
                                                            fileContent,
                                                            3,
                                                            fileContent.length - 3),
                                                    fileTargetEncoding);
                                        }
                                    }
                                }
                                break;
                            case ExportConstants.NO_UTF_BOM:
                            case ExportConstants.UTF16_LE:
                            case ExportConstants.UTF16_BE:
                                if (bomType == ExportConstants.UTF_BOM_ADD)
                                {
                                    // Current output file has no BOM info
                                    FileUtil.addBom(finalFile,
                                            fileTargetEncoding);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            catch (Exception e1)
            {
                logger.error("File output error when exporting file with BOM processing. "
                        + e1.toString());
            }
            finally
            {
                if (fis != null)
                    fis.close();
            }

            // gbs-742
            KnownFormatType kf = null;
            kf = ServerProxy.getFileProfilePersistenceManager()
                    .getKnownFormatTypeById(fp.getKnownFormatTypeId(), false);
            if ("jsp".equals(kf.getFormatType()))
            {
                String targetEncoding = "iso-8859-1";
                Object targetCharset = m_cxeMessage.getParameters().get(
                        "TargetCharset");
                if (targetCharset != null)
                {
                    targetEncoding = (String) targetCharset;
                }

                if (fp.getEntityEscape())
                {
                    Object entityEscape = m_cxeMessage.getParameters().get(
                            "EntityEscape");
                    if (targetEncoding.equalsIgnoreCase("iso-8859-1"))
                    {
                        convertToHtmlEntity(finalFileName, targetEncoding);
                    }
                    else if ((entityEscape != null && "true"
                            .equalsIgnoreCase((String) entityEscape)))
                    {
                        convertToHtmlEntity(finalFileName, targetEncoding);
                    }
                }
                else
                {
                    Object entityEscape = m_cxeMessage.getParameters().get(
                            "EntityEscape");
                    if (entityEscape != null
                            && "true".equalsIgnoreCase((String) entityEscape))
                    {
                        convertToHtmlEntity(finalFileName, targetEncoding);
                    }
                }
            }

            String scriptOnExport = fp.getScriptOnExport();

            if (scriptOnExport != null && scriptOnExport.length() > 0)
            {
                // Call the script on export to revert the exported files.
                String targetFolder = finalFileName.substring(0,
                        finalFileName.lastIndexOf(File.separator));
                // If all the files were exported in a target folder,
                // then execute the script on export to revert them back to
                // original file.
                String companyId = String.valueOf(fp.getCompanyId());
                if (readyForScript(targetFolder, companyId))
                {
                    try
                    {
                        // Copy the original source files to target folder.
                        copyOriFilesToTargetFolder(targetFolder, companyId);
                        // execute script
                        String cmd = "cmd.exe /c " + scriptOnExport + " \""
                                + targetFolder + "\" -r";
                        ProcessRunner pr = new ProcessRunner(cmd);
                        Thread t = new Thread(pr);
                        t.start();
                        try
                        {
                            t.join();
                        }
                        catch (InterruptedException ie)
                        {
                        }
                        m_logger.info("Script on Export " + scriptOnExport
                                + " is called to handle " + targetFolder);

                    }
                    catch (Exception e)
                    {
                        m_logger.error("Could not revert the exported files back "
                                + "with errors when executing the script on export.");

                        String errorArgs[] = new String[1];
                        errorArgs[0] = finalFileName;
                        FileSystemAdapterException fsae = new FileSystemAdapterException(
                                "ScriptOnExport", errorArgs, e);

                        exportStatusMsg = makeExportErrorMessage(fsae);

                        return exportStatusMsg;
                    }
                    finally
                    {
                        // FileUtils.deleteSilently(finalFileName);
                        FileUtils.deleteAllFilesSilently(targetFolder);
                    }
                }
            }

            // Added by Vincent Yan
            HashMap<String, String> infos = CVSUtil.seperateFileInfo(
                    finalFile.getAbsolutePath(), m_exportLocation);
            if (infos != null && CVSUtil.isCVSJob(infos.get("jobName")))
            {
                CVSUtil.saveCVSFile(
                        infos,
                        m_displayName.substring(0,
                                m_displayName.indexOf(File.separator)));
            }

            synchronized (FILE_STATES)
            {
                m_batchId = (String) m_cxeMessage.getParameters().get(
                        "ExportBatchId");
                m_pageCount = (Integer) m_cxeMessage.getParameters().get(
                        "PageCount");
                m_pageNumber = (Integer) m_cxeMessage.getParameters().get(
                        "PageNum");

                XmlDtd xmlDtd = getXmlDtd();
                if (xmlDtd == null)
                {
                    if (m_sourceFileName != null && wf != null)
                    {
                        Vector<TargetPage> ps = wf.getAllTargetPages();
                        if (ps != null)
                        {
                            int n = 0;
                            for (TargetPage p : ps)
                            {
                                String s = p.getExternalPageId();
                                if (s.endsWith(m_sourceFileName))
                                {
                                    n++;
                                }
                            }

                            if (n == 0)
                                n++;

                            mark(FileState.IGNORE, n);
                        }
                    }
                    else
                    {
                        mark(FileState.IGNORE);
                    }
                }
                else
                {
                    try
                    {
                        XmlDtdManager
                                .validateXmlFile(xmlDtd.getId(), finalFile);
                        mark(FileState.VALIDATION_SUCCESSFUL);
                    }
                    catch (DtdException e)
                    {
                        if (xmlDtd.isAddComment())
                        {
                            mark(FileState.VALIDATION_FAILED);
                        }
                        else
                        {
                            mark(FileState.IGNORE);
                        }

                        String errorArgs[] = new String[2];
                        errorArgs[0] = m_logger.getName();
                        errorArgs[1] = finalFileName;
                        FileSystemAdapterException fsae = new FileSystemAdapterException(
                                "XmlDtdValidateEx", errorArgs, e);

                        exportStatusMsg = makeExportErrorMessage(fsae,
                                xmlDtd.getId());
                    }
                }

                if (isLastFile())
                {
                    addDtdValidationFailedComment();
                    FILE_STATES.remove(m_batchId);

                    XliffFileUtil.processXliffFiles(wf);
                    XliffFileUtil.processXLZFiles(wf);
                }
            }
        }
        catch (FileSystemAdapterException fsae)
        {
            exportStatusMsg = makeExportErrorMessage(fsae);
        }
        catch (Exception e)
        {
            m_logger.error("Could not write file back.", e);

            String errorArgs[] = new String[1];
            errorArgs[0] = m_logger.getName();
            FileSystemAdapterException fsae = new FileSystemAdapterException(
                    "CxeInternalEx", errorArgs, e);

            exportStatusMsg = makeExportErrorMessage(fsae);
        }

        return exportStatusMsg;
    }

    private String replaceFileLocale(String content, String targetLocale)
    {
        String regex = "<file[^>]*(target-language=[\"']([^\"']*?)[\"'])[^>]*>";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(content);
        while (m.find())
        {
            String f = m.group();
            String target = m.group(1);
            String locale = m.group(2);
            String rTarget = target.replace(locale, targetLocale);
            String rf = f.replace(target, rTarget);
            content = content.replace(f, rf);
        }

        return content;
    }

    private void changeTargetLocale(String targetLocale, File xliffFile,
            String encoding)
    {
        targetLocale = targetLocale.replace("_", "-");
        try
        {
            String content = FileUtil.readFile(xliffFile, encoding);
            content = replaceFileLocale(content, targetLocale);
            content = replaceTransUnit(content, targetLocale);

            FileUtil.writeFile(xliffFile, content, encoding);
        }
        catch (Exception e)
        {
            m_logger.error(e.getMessage(), e);
        }
    }

    private String replaceTransUnit(String content, String targetLocale)
    {
        String regex = "<trans-unit[\\s\\S]*?(<target[^>]*xml:lang=[\"']([^\"']*?)[\"'][\\s\\S]*?</target>)[\\s\\S]*?</trans-unit>";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(content);
        while (m.find())
        {
            String f = m.group();
            String target = m.group(1);
            String locale = m.group(2);
            String rTarget = target.replace(locale, targetLocale);
            String rf = f.replace(target, rTarget);
            content = content.replace(f, rf);
        }

        return content;
    }

    private void addDtdValidationFailedComment()
    {
        long pageId = Long.parseLong(m_messageId);
        List<String> files = new ArrayList<String>();
        for (FileState fileState : getFileStates())
        {
            if (FileState.VALIDATION_FAILED.equals(fileState.getState()))
            {
                files.add(fileState.getFile());
            }
        }

        try
        {
            TargetPage tp = ServerProxy.getPageManager().getTargetPage(pageId);
            if (tp != null)
            {
                Workflow workflow = tp.getWorkflowInstance();
                Job job = workflow.getJob();
                XmlDtdManager.addComment(job, files, XmlDtdManager.EXPORT);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    private FileProfile getFileProfile()
    {
        if (fileProfile == null && m_fileProfileId != null)
        {
            fileProfile = HibernateUtil.get(FileProfileImpl.class,
                    Long.parseLong(m_fileProfileId), false);
        }

        return fileProfile;
    }

    private void mark(String state)
    {
        mark(state, 1);
    }

    private void mark(String state, int pageCount)
    {
        FileState fileState = getFileStates()[m_pageNumber];
        if (fileState.getId() == -1)
        {
            fileState.setId(Long.parseLong(m_messageId));
            fileState.setFile(m_localeSubDir + "/" + m_filename);
        }
        fileState.setState(state);
        fileState.setPageCount(pageCount);
    }

    private FileState[] getFileStates()
    {
        FileState[] files = FILE_STATES.get(m_batchId);
        if (files == null)
        {
            files = new FileState[m_pageCount];
            for (int i = 0; i < files.length; i++)
            {
                files[i] = new FileState();
            }
            FILE_STATES.put(m_batchId, files);
        }

        return files;
    }

    private boolean isLastFile()
    {
        FileState[] files = getFileStates();
        int p = 0;
        for (FileState f : files)
        {
            if (f.getState().length() > 0)
            {
                p += f.getPageCount();
            }
        }

        return p >= files.length;
    }

    private XmlDtd getXmlDtd()
    {
        if (xmlDtd == null && getFileProfile() != null)
        {
            xmlDtd = getFileProfile().getXmlDtd();
        }

        return xmlDtd;
    }

    public static boolean newLine(String s)
    {
        int i = 0;
        for (; i < s.length(); i++)
        {
            char c = s.charAt(s.length() - i - 1);
            if (c != '\\')
            {
                break;
            }
        }

        return i % 2 == 0;
    }

    /*
     * Change all string to unicode.
     */
    private void unicodeAllEscape(String fileName, String encoding)
            throws Exception
    {
        String tempFileName = fileName + ".tmp";
        File sourceFile = new File(fileName);
        File tempfile = new File(tempFileName);

        sourceFile.renameTo(tempfile);
        if (sourceFile.exists())
        {
            sourceFile.delete();
        }

        BufferedReader in = null;
        FileOutputStream fos = null;

        try
        {
            in = new BufferedReader(new FileReader(tempFileName));
            fos = new FileOutputStream(sourceFile);

            String s = in.readLine();
            String s2 = null;

            while (s != null)
            {

                s2 = null;
                if (s.startsWith("#") || s.startsWith("!"))
                {
                    fos.write(s.getBytes(encoding));
                    fos.write(lineSeparator.getBytes(encoding));
                }
                else
                {
                    while (!newLine(s))
                    {
                        s = s.substring(0, s.length() - 1);
                        s2 = in.readLine();
                        if (s2 == null)
                        {
                            break;
                        }
                        s += s2;
                    }

                    int index = s.indexOf("=");
                    if (index == -1)
                    {
                        index = s.indexOf(":");
                    }

                    if (index > 0)
                    {
                        String key = loadConvert(s.substring(0, index + 1));
                        String value = loadConvert(s.substring(index + 1));
                        fos.write(key.getBytes(encoding));
                        fos.write(toUnicode(value).getBytes(encoding));
                        fos.write(lineSeparator.getBytes(encoding));
                    }
                    else
                    {
                        fos.write(loadConvert(s).getBytes(encoding));
                        fos.write(lineSeparator.getBytes(encoding));
                    }
                }

                s = s2;
                if (s == null)
                {
                    s = in.readLine();
                }
            }
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
            }

            if (in != null)
            {
                in.close();
            }

            tempfile.delete();
        }
    }

    private String toUnicode(String str)
    {
        char[] arChar = str.toCharArray();
        int iValue = 0;
        String uStr = "";
        for (int i = 0; i < arChar.length; i++)
        {
            iValue = str.charAt(i);
            if (iValue <= 256)
            {
                uStr += "\\u00" + Integer.toHexString(iValue);
            }
            else
            {
                uStr += "\\u" + Integer.toHexString(iValue);
            }
        }
        return uStr;
    }

    private void notUnicodeEscape(String fileName, String encoding)
            throws Exception
    {
        String tempFileName = fileName + ".tmp";
        File sourceFile = new File(fileName);
        File tempfile = new File(tempFileName);

        sourceFile.renameTo(tempfile);
        if (sourceFile.exists())
        {
            sourceFile.delete();
        }

        BufferedReader reader = null;
        FileOutputStream fos = null;
        try
        {
            String fileEncoding = FileUtil.guessEncoding(tempfile);
            if (fileEncoding == null)
                fileEncoding = encoding;

            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(tempfile), fileEncoding));
            fos = new FileOutputStream(sourceFile);
            String s = reader.readLine();
            while (s != null)
            {
                if (!s.startsWith("#") && !s.startsWith("!"))
                {
                    s = loadConvert(s);
                }

                s += lineSeparator;
                fos.write(s.getBytes(encoding));
                s = reader.readLine();
            }

            fos.flush();
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
            }

            if (reader != null)
            {
                reader.close();
            }

            tempfile.delete();
        }
    }

    /**
     * Replace the "\ " on the begining of string to " "
     * 
     * @param fileName
     * @param encoding
     * @throws Exception
     */
    private void handleExtraEscapeCharacter(String fileName, String encoding)
            throws Exception
    {
        String tempFileName = fileName + ".tmp";
        File sourceFile = new File(fileName);
        File tempfile = new File(tempFileName);

        sourceFile.renameTo(tempfile);

        if (sourceFile.exists())
        {
            sourceFile.delete();
        }

        BufferedReader reader = null;
        FileOutputStream fos = null;

        try
        {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(
                    tempfile), encoding);
            reader = new BufferedReader(isr);

            fos = new FileOutputStream(sourceFile);
            String s = reader.readLine();

            while (s != null)
            {
                if (!s.trim().equals(""))
                {
                    if (!s.startsWith("#") && !s.startsWith("!"))
                    {
                        if (s.indexOf("=") != (s.length() - 1))
                        {
                            // replace the "\ " on the begining of string to " "
                            String tempStr = s.substring(s.indexOf("=") + 1);
                            int num = 0;

                            if (tempStr.startsWith("\\ "))
                            {

                                while (tempStr.startsWith("\\ "))
                                {
                                    tempStr = tempStr.substring("\\ ".length());
                                    num++;
                                }
                            }

                            for (int i = 0; i < num; i++)
                            {
                                tempStr = " " + tempStr;
                            }

                            s = s.substring(0, s.indexOf("=") + 1) + tempStr;
                        }
                    }
                }

                s += lineSeparator;
                fos.write(s.getBytes(encoding));
                s = reader.readLine();
            }

            fos.flush();
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
            }

            if (reader != null)
            {
                reader.close();
            }

            tempfile.delete();
        }
    }

    private String loadConvert(String s)
    {
        char[] in = s.toCharArray();
        int off = 0;
        int len = s.length();
        char[] convtBuf = s.toCharArray();

        char aChar;
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;

        while (off < end)
        {
            aChar = in[off++];
            if (aChar == '\\')
            {
                boolean isConvert = true;
                aChar = in[off++];
                if (aChar == 'u')
                {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++)
                    {
                        aChar = in[off++];
                        switch (aChar)
                        {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed \\uxxxx encoding.");
                        }
                    }
                    out[outLen++] = (char) value;
                }
                else
                {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    else
                        isConvert = false;

                    if (isConvert)
                    {
                        out[outLen++] = aChar;
                    }
                    else
                    {
                        out[outLen++] = '\\';
                        out[outLen++] = aChar;
                    }
                }
            }
            else
            {
                out[outLen++] = aChar;
            }
        }
        return new String(out, 0, outLen);
    }

    // ////////////////////////////////////
    // Private Methods //
    // ////////////////////////////////////

    private void copyOriFilesToTargetFolder(String p_targetFolder,
            String p_companyId)
    {
        String sourceFolder = determineSourceFolder(p_companyId);
        File targetFile = new File(p_targetFolder).getParentFile();
        File sourceFile = new File(sourceFolder).getParentFile();
        final String fileName = new File(sourceFolder).getName();
        File[] fs = sourceFile.listFiles(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.isFile()
                        && FileUtils.getBaseName(pathname.getName())
                                .startsWith(fileName + ".");
            }
        });
        for (int i = 0; i < fs.length; i++)
        {
            File file = fs[i];
            FileCopier.copyFile(file, targetFile);
        }
    }

    private boolean readyForScript(String p_targetFolder, String p_companyId)
    {
        String sourceFolder = determineSourceFolder(p_companyId);
        File filesInTargetFolder = new File(p_targetFolder);
        File filesInSourceFolder = new File(sourceFolder);
        if (filesInTargetFolder.listFiles() != null
                && filesInSourceFolder.listFiles() != null
                && filesInTargetFolder.listFiles().length == filesInSourceFolder
                        .listFiles().length)
        {
            return true;
        }

        return false;
    }

    private String determineSourceFolder(String p_companyId)
    {
        String filteredDispalyName = SourcePage.filtSpecialFile(m_displayName);
        String sourceFolder = filteredDispalyName.substring(0,
                filteredDispalyName.lastIndexOf(File.separator));
        StringBuffer sb = new StringBuffer(
                AmbFileStoragePathUtils.getCxeDocDirPath(p_companyId));
        sb.append(File.separator).append(sourceFolder);

        return sb.toString();
    }

    /**
     * Prepares the export success status message for sending to the next
     * adapter.
     */
    private CxeMessage makeExportSuccessMessage(File p_finalFile)
    {
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.CXE_EXPORT_STATUS_EVENT);

        CxeMessage newCxeMessage = new CxeMessage(type);
        newCxeMessage.setEventFlowXml(m_cxeMessage.getEventFlowXml());

        HashMap newParams = new HashMap();
        // currently dynamic preview for file system is not supported
        newParams.put("PreviewUrlXml", "");
        newParams.put("ExportedTime", new Long(p_finalFile.lastModified()));
        newParams.put("Exception", null);

        // copy parameters that were preset by other adapters
        // (office adapter calls this code once per batch)
        // (quark and frame call this code once per file)
        String isComp = (String) m_cxeMessage.getParameters().get(
                "IsComponentPage");

        if (isComp != null)
        {
            // copy to new params
            newParams.put("IsComponentPage", isComp);
        }

        String absoluteExportPath = (String) m_cxeMessage.getParameters().get(
                "AbsoluteExportPath");

        if (absoluteExportPath != null)
        {
            // copy to new params
            newParams.put("AbsoluteExportPath", absoluteExportPath);
        }

        // for all other files, we set the absolute path here
        if (isComp == null || isComp.equalsIgnoreCase("false"))
        {
            newParams.put("AbsoluteExportPath", p_finalFile.getAbsolutePath());
        }

        newCxeMessage.setParameters(newParams);

        return newCxeMessage;
    }

    /**
     * Prepares the export error message for sending to the next adapter
     * 
     * @param p_fsae
     *            a File System Adapter Exception
     * @return CxeMessage
     */
    private CxeMessage makeExportErrorMessage(FileSystemAdapterException p_fsae)
    {
        return makeExportErrorMessage(p_fsae, null);
    }

    /**
     * Prepares the export error message for sending to the next adapter
     * 
     * @param p_fsae
     *            a File System Adapter Exception
     * @param sendEmail
     *            send email or not.
     * @return CxeMessage
     */
    private CxeMessage makeExportErrorMessage(
            FileSystemAdapterException p_fsae, Long xmlDtdId)
    {
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.CXE_EXPORT_STATUS_EVENT);

        CxeMessage newCxeMessage = new CxeMessage(type);
        newCxeMessage.setEventFlowXml(m_cxeMessage.getEventFlowXml());

        HashMap newParams = new HashMap();
        newParams.put("Exception", p_fsae);
        newParams.put(XML_DTD_ID, xmlDtdId);
        newCxeMessage.setParameters(newParams);

        return newCxeMessage;
    }

    private String getNodeValue(Element elem, String nodeName)
    {
        String value = null;

        NodeList nl = elem.getElementsByTagName(nodeName);
        if (nl != null)
        {
            Element e = (Element) nl.item(0);
            if (e != null)
            {
                value = e.getFirstChild().getNodeValue();
            }
        }

        return value;
    }

    /**
     * Reads the Event Flow Xml for some needed values
     * 
     * @exception Exception
     */
    private void parseEventFlowXml() throws Exception
    {
        StringReader sr = new StringReader(m_cxeMessage.getEventFlowXml());
        InputSource is = new InputSource(sr);
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false);
        parser.parse(is);

        Element elem = parser.getDocument().getDocumentElement();
        NodeList nl;
        NodeList attributeList;

        // get the original filename
        nl = elem.getElementsByTagName("source");
        Element sourceElement = (Element) nl.item(0);
        m_formatType = sourceElement.getAttribute("formatType");
        m_dataSourceId = sourceElement.getAttribute("dataSourceId");

        NodeList sDa = sourceElement.getElementsByTagName("da");
        for (int k = 0; k < sDa.getLength(); k++)
        {
            Element attrElement = (Element) sDa.item(k);
            String name = attrElement.getAttribute("name");
            if (name != null && name.equals("Filename"))
            {
                NodeList values = attrElement.getElementsByTagName("dv");
                Element valElement = (Element) values.item(0);
                m_sourceFileName = valElement.getFirstChild().getNodeValue();
            }
        }

        nl = elem.getElementsByTagName("displayName");
        Element e = (Element) nl.item(0);
        m_displayName = e.getFirstChild().getNodeValue();

        m_fileProfileId = getNodeValue(elem, "fileProfileId");

        // get the target Element
        nl = elem.getElementsByTagName("target");
        Element targetElement = (Element) nl.item(0);
        attributeList = targetElement.getElementsByTagName("da");

        boolean foundExportLoc = false;
        boolean foundLocaleSubDir = false;
        boolean foundFileName = false;

        for (int k = 0; k < attributeList.getLength(); k++)
        {
            Element attrElement = (Element) attributeList.item(k);
            String name = attrElement.getAttribute("name");
            if (name != null && name.equals("ExportLocation"))
            {
                NodeList values = attrElement.getElementsByTagName("dv");
                Element valElement = (Element) values.item(0);
                m_exportLocation = makeDirectoryNameOperatingSystemSafe(valElement
                        .getFirstChild().getNodeValue());
                foundExportLoc = true;

                m_logger.debug("export location:" + m_exportLocation);
            }

            else if (name != null && name.equals("LocaleSubDir"))
            {
                NodeList values = attrElement.getElementsByTagName("dv");
                Element valElement = (Element) values.item(0);
                m_localeSubDir = valElement.getFirstChild().getNodeValue();

                if (m_localeSubDir.startsWith("\\")
                        || m_localeSubDir.startsWith("/"))
                {
                    m_localeSubDir = m_localeSubDir.substring(1);
                }

                foundLocaleSubDir = true;

                m_logger.debug("locale sub dir:" + m_localeSubDir);
            }
            else if (name != null && name.equals("Filename"))
            {
                NodeList values = attrElement.getElementsByTagName("dv");
                Element valElement = (Element) values.item(0);
                m_filename = valElement.getFirstChild().getNodeValue();

                foundFileName = true;

                m_logger.debug("target file:" + m_filename);
            }

            if (foundLocaleSubDir && foundExportLoc && foundFileName)
            {
                break;
            }
        }

        // the messageId is optional in the EventFlowXml
        nl = elem.getElementsByTagName("capMessageId");
        if (nl.getLength() > 0)
        {
            e = (Element) nl.item(0);
            m_messageId = e.getFirstChild().getNodeValue();
        }
        else
        {
            m_messageId = null;
        }
    }

    /**
     * Figures out what the file name to write the content to should be.
     * 
     * @return String
     */
    private String determineFinalFileName() throws IOException
    {
        if ("passolo".equals(m_formatType))
        {
            String name = m_sourceFileName.replace("\\", "/");
            return m_exportLocation + "/passolo"
                    + name.substring(name.indexOf("/"));
        }

        StringBuffer fullpath = new StringBuffer(m_exportLocation);
        fullpath.append(File.separator).append(m_localeSubDir);
        fullpath.append(File.separator).append(m_filename);

        // File size is in KB.
        long fileSize = m_cxeMessage.getMessageData().getSize() / 1024L;

        String finalFileName = fullpath.toString();

        m_logger.info("Writing: " + finalFileName + ", size: " + fileSize + "k");

        return finalFileName;
    }

    /**
     * Takes in the directory name and replaces "/" and "\" with the appropriate
     * File.separator for the current operating system. Also removes the leading
     * and trailing separators.
     */
    private String makeDirectoryNameOperatingSystemSafe(String p_dirName)
    {
        // First check if it's a UNC pathname and we're on Windows.
        // If so leave it alone.
        String os = System.getProperty("os.name");
        if (os.startsWith("Windows") && p_dirName.startsWith("\\\\"))
        {
            return p_dirName;
        }

        String newName = p_dirName.replace('/', File.separatorChar);
        newName = newName.replace('\\', File.separatorChar);

        if (newName.endsWith(File.separator))
        {
            newName = newName.substring(0, newName.length() - 1);
        }

        return newName;
    }

    private static void convertToHtmlEntity(String fileName, String encoding)
            throws Exception
    {
        String tempFileName = fileName + ".tmp";
        File sourceFile = new File(fileName);
        File tempfile = new File(tempFileName);

        sourceFile.renameTo(tempfile);
        if (sourceFile.exists())
        {
            sourceFile.delete();
        }
        PrintWriter pw = null;
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(
                    tempFileName), encoding));
            // fos = new FileOutputStream(sourceFile);
            pw = new PrintWriter(sourceFile);
            String s1 = null;
            while ((s1 = in.readLine()) != null)
            {
                s1 = MapOfHtmlEntity.escapeHtmlFull(s1);
                pw.write(s1);
                pw.write("\n");
            }
            pw.flush();
        }
        finally
        {
            if (pw != null)
            {
                pw.close();
            }

            if (in != null)
            {
                in.close();
            }

            tempfile.delete();
        }
    }

    private JobEditionInfo getGSEditionJobByJobID(long jobID)
    {
        JobEditionInfo je = new JobEditionInfo();

        try
        {
            String hql = "from JobEditionInfo a where a.jobId = :id";
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("id", Long.toString(jobID));
            Collection servers = HibernateUtil.search(hql, map);
            Iterator i = servers.iterator();
            je = i.hasNext() ? (JobEditionInfo) i.next() : null;
        }
        catch (Exception pe)
        {
            // s_logger.error("Persistence Exception when retrieving JobEditionInfo",
            // pe);
        }

        return je;
    }

}
