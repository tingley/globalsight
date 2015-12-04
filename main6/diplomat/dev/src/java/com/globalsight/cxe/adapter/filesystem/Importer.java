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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.cxe.util.fileImport.eventFlow.BatchInfo;
import com.globalsight.cxe.util.fileImport.eventFlow.Da;
import com.globalsight.cxe.util.fileImport.eventFlow.Dv;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.cxe.util.fileImport.eventFlow.Source;
import com.globalsight.cxe.util.fileImport.eventFlow.Target;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.aligner.AlignerExtractor;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectHandlerLocal;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

/**
 * Helper class used by the FileSystemAdapter for importing
 */
public class Importer
{
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(Importer.class);

	// ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    // private byte[] m_content = null;
    private String[] m_errorArgs = null;
    private String m_filename = null;
    private String m_displayName = null;
    private String m_relativePathName = null;
    private String m_baseHref = null;
    private String m_fileProfileId = null;
    private String m_jobName = null;
    private String m_jobId = "";
    private String m_batchId = null;
    private String m_codeset = null;
    private String m_locale = null;
    private String m_targetLocales = null;
    private String m_l10nProfileId = null;
    String m_formatName = null;
    private String m_formatType = null;
    private String m_preExtractEvent = null;
    private String m_preMergeEvent = null;
    private int m_pageCount;
    private int m_pageNum;
    private int m_docPageCount;
    private int m_docPageNum;
    private boolean m_overrideAsUnextracted = false;
    private org.apache.log4j.Logger m_logger = null;
    private String m_importRequestType = null;
    // private String m_cxeDocsDir = null;
    public AlignerExtractor m_alignerExtractor = null;
    private String m_importInitiatorId = null;
    private String m_priority = null;
    private String jobUuid = "";

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////
    /**
     * Creates an Importer object
     * 
     * process is invoked upon a failure for an extracted file (will override
     * the file profile's format type to unextracted).
     */
    public Importer(CxeMessage p_cxeMessage, org.apache.log4j.Logger p_logger)
    {
        m_logger = p_logger;
        HashMap params = p_cxeMessage.getParameters();
        String jobId = (String) params.get("JobId");
        String jobName = (String) params.get("JobName");
        jobUuid = (String) params.get("uuid");
        m_importInitiatorId = (String) params.get("ImportInitiator");
        String companyId = (String) params
                .get(CompanyWrapper.CURRENT_COMPANY_ID);
        if (jobId != null)
        {
            // For GBS-2137, the job was created at the upload stage
            Job job = JobCreationMonitor.loadJobFromDB(Long.parseLong(jobId));
            jobName = job.getJobName();
            jobUuid = ((JobImpl) job).getUuid();
            m_importInitiatorId = job.getCreateUserId();
            companyId = String.valueOf(job.getCompanyId());
            m_jobId = jobId;
            // update the job to "EXTRACTING" state
            if (Job.IN_QUEUE.equals(job.getState()))
            {
                JobCreationMonitor.updateJobState(job, Job.EXTRACTING);
            }
        }
        if (jobUuid == null)
        {
            jobUuid = JobImpl.createUuid();
        }
        CompanyThreadLocal.getInstance().setIdValue(companyId);
        String filename = (String) params.get("Filename");
        String batchId = (String) params.get("BatchId");
        String fileProfileId = (String) params.get("FileProfileId");
        int pageCount = ((Integer) params.get("PageCount")).intValue();
        int pageNumber = ((Integer) params.get("PageNum")).intValue();
        int docPageCount = ((Integer) params.get("DocPageCount")).intValue();
        int docPageNumber = ((Integer) params.get("DocPageNum")).intValue();
        String importRequestType = (String) params.get(CxeProxy.IMPORT_TYPE);
        Object targetLocales = params.get("TargetLocales");
        boolean overrideAsUnextracted = ((Boolean) params
                .get("OverrideFileProfileAsUnextracted")).booleanValue();
        String alignerExtractorName = (String) params.get("AlignerExtractor");
        m_alignerExtractor = AlignerExtractor
                .getAlignerExtractor(alignerExtractorName);
        String priority = (String) params.get("priority");

        String fullname = AmbFileStoragePathUtils.getCxeDocDirPath(companyId)
                + File.separator + filename;
        String displayName = filename;
        m_filename = fullname;
        m_displayName = displayName;
        m_relativePathName = filename;
        m_baseHref = getBaseHref(m_relativePathName);
        m_fileProfileId = fileProfileId;
        m_jobName = jobName;
        m_batchId = batchId;
        m_pageCount = pageCount;
        m_pageNum = pageNumber;
        m_docPageCount = docPageCount;
        m_docPageNum = docPageNumber;
        m_overrideAsUnextracted = overrideAsUnextracted;
        m_importRequestType = importRequestType;
        m_errorArgs = new String[3];
        m_errorArgs[0] = m_logger.getName();
        m_errorArgs[1] = m_filename;
        if (targetLocales != null)
        {
            m_targetLocales = (String) targetLocales;
        }
        m_priority = priority;
    }

    // ////////////////////////////////////
    // Public Methods //
    // ////////////////////////////////////

    /**
     * Reads the given file from the file system and returns a MessageData
     * containing the content in the file.<br>
     * NOTE: Since the file is already on the filesystem, it just copies it to a
     * temp location, and returns a MessageData based on that temp file.
     * 
     * @return MessageData
     * @exception FileSystemAdapterException
     */
    public MessageData readFile() throws FileSystemAdapterException
    {
        try
        {
            File f = new File(m_filename);
            m_logger.info("Reading file: " + m_filename + "; of size: "
                    + (f.length() / 1024L) + "KB");
            FileMessageData fmd = MessageDataFactory.createFileMessageData();
            fmd.copyFrom(f);
            return fmd;
        }
        catch (IOException ioe)
        {
            m_logger.error("Could not get read in file " + m_filename, ioe);
            throw new FileSystemAdapterException("InputOutputIm", m_errorArgs,
                    ioe);
        }
    }
    
    /**
     * Creates the EventFlowXml. Assume going back to FileSystemTargetAdapter.
     * 
     * @param isAutomaticImport
     *            -- false if manual import
     * @return the object of Event Flow Xml
     * @throws FileSystemAdapterException
     */
    public EventFlowXml makeEventFlowXmlObject(boolean p_isAutomaticImport)
            throws FileSystemAdapterException
    {
        if (m_alignerExtractor == null)
        {
            getEventFlowXmlData();
        }
        else
        {
            // assume this is for aligner import
            m_logger.info("Reading values for aligner import.");

            KnownFormatType knf = m_alignerExtractor.getFormat();
            m_formatType = knf.getFormatType();
            m_preExtractEvent = knf.getPreExtractEvent();
            m_preMergeEvent = knf.getPreMergeEvent();
            m_codeset = m_alignerExtractor.getEncoding();
            m_l10nProfileId = null;
            m_locale = m_alignerExtractor.getLocale();
        }

        // xml file is encoded as UTF-8.
        if ("xml".equalsIgnoreCase(m_formatType))
        {
            m_codeset = "UTF-8";
        }

        EventFlowXml object = new EventFlowXml();
        object.setPreMergeEvent(m_preMergeEvent);
        object.setPostMergeEvent(CxeMessageType.getCxeMessageType(
                CxeMessageType.FILE_SYSTEM_EXPORT_EVENT).getName());
        
        //batch info
        BatchInfo info = new BatchInfo();
        info.setL10NProfileId(m_l10nProfileId);
        info.setProcessingMode("automatic");
        info.setBatchId(m_batchId);
        info.setFileProfileId(m_fileProfileId);
        info.setPageCount(m_pageCount);
        info.setPageNumber(m_pageNum);
        info.setDocPageCount(m_docPageCount);
        info.setDocPageNumber(m_docPageNum);
        info.setDisplayName(m_displayName);
        info.setBaseHref(m_baseHref);
        info.setPriority(m_priority);
        info.setJobName(m_jobName);
        info.setJobId(m_jobId);
        info.setUuid(jobUuid);
        object.setBatchInfo(info);
        
        // source
        Source source = new Source();
        source.setName("FileSystemSourceAdapter");
        if (p_isAutomaticImport)
            source.setDataSourceType("fsAutoImport");
        else
            source.setDataSourceType("fs");
        source.setDataSourceId(m_fileProfileId);
        source.setFormatType(m_formatType);
        source.setFormatName(m_formatName);
        source.setPageIsCxePreviewable("false");
        source.setImportRequestType(m_importRequestType);
        source.setImportInitiatorId(m_importInitiatorId);
        source.setLocale(m_locale);
        source.setCharset(m_codeset);
        Da da = new Da();
        da.setName("Filename");
        Dv dv = new Dv();
        dv.setvalue(m_relativePathName);
        da.getDv().add(dv);
        source.getDa().add(da);
        object.setSource(source);
        
        //target
        Target target = new Target();
        target.setName("FileSystemTargetAdapter");

        target.setLocale(m_targetLocales);
        if (m_targetLocales == null || "".equals(m_targetLocales.trim()))
		{
			L10nProfile profile = null;
			if (m_l10nProfileId != null)
			{
				long l10nProfileId = Long.parseLong(m_l10nProfileId);
				try
				{
					ProjectHandler ph = new ProjectHandlerLocal();
					profile = ph.getL10nProfile(l10nProfileId);
				}
				catch (Exception e)
				{
					logger.error("Failed to get l10nProfile object by Id: "
							+ this.m_l10nProfileId, e);
				}
			}
			if (profile != null)
			{
				String locales = "";
				for (GlobalSightLocale gsl: profile.getTargetLocales())
				{
					locales += gsl + ",";
				}
				if (locales != "" && locales.endsWith(","))
				{
					m_targetLocales = locales.substring(0, locales.lastIndexOf(","));
					target.setLocale(m_targetLocales);
				}
			}
		}
        target.setCharset("unknown");
       
        Da da2 = new Da();
        da2.setName("ExportLocation");
        Dv dv2 = new Dv();
        dv2.setvalue("unknown");
        da2.getDv().add(dv2);        
        target.getDa().add(da2);
        
        Da da3 = new Da();
        da3.setName("LocaleSubDir");
        Dv dv3 = new Dv();
        dv3.setvalue("unknown");
        da3.getDv().add(dv3);        
        target.getDa().add(da3);
        object.setTarget(target);
       
        return object;
    }

    /**
     * Creates the EventFlowXml. Assume going back to FileSystemTargetAdapter.
     * 
     * @param isAutomaticImport
     *            -- false if manual import
     * @return the string of Event Flow Xml
     * @throws FileSystemAdapterException
     */
    public String makeEventFlowXml(boolean p_isAutomaticImport)
            throws FileSystemAdapterException
    {
        if (m_alignerExtractor == null)
        {
            getEventFlowXmlData();
        }
        else
        {
            // assume this is for aligner import
            m_logger.info("Reading values for aligner import.");

            KnownFormatType knf = m_alignerExtractor.getFormat();
            m_formatType = knf.getFormatType();
            m_preExtractEvent = knf.getPreExtractEvent();
            m_preMergeEvent = knf.getPreMergeEvent();
            m_codeset = m_alignerExtractor.getEncoding();
            m_l10nProfileId = null;
            m_locale = m_alignerExtractor.getLocale();
        }

        // xml file is encoded as UTF-8.
        if ("xml".equalsIgnoreCase(m_formatType))
        {
            m_codeset = "UTF-8";
        }

        String l10nProfileId = null;// Fenshid: this via is no use at all
        String eventFlowXml = null;
        StringBuffer b = new StringBuffer(XmlUtil.formattedEventFlowXmlDtd());
        b.append("<eventFlowXml>\n");
        b.append("<preMergeEvent>");
        b.append(m_preMergeEvent);
        b.append("</preMergeEvent>\n");

        b.append("<postMergeEvent>");
        // go back to the filesytem after merging
        b.append(CxeMessageType.getCxeMessageType(
                CxeMessageType.FILE_SYSTEM_EXPORT_EVENT).getName());
        b.append("</postMergeEvent>\n");

        b.append("<batchInfo l10nProfileId=\"");
        b.append(m_l10nProfileId);
        b.append("\" processingMode=\"automatic\">\n<batchId>");
        b.append(EditUtil.encodeXmlEntities(m_batchId));
        b.append("</batchId>\n");

        b.append("<fileProfileId>");
        b.append(EditUtil.encodeXmlEntities(m_fileProfileId));
        b.append("</fileProfileId>\n");

        b.append("<pageCount>");
        b.append(Integer.toString(m_pageCount));
        b.append("</pageCount>\n");
        b.append("<pageNumber>");
        b.append(Integer.toString(m_pageNum));
        b.append("</pageNumber>\n");
        b.append("<docPageCount>");
        b.append(Integer.toString(m_docPageCount));
        b.append("</docPageCount>\n");
        b.append("<docPageNumber>");
        b.append(Integer.toString(m_docPageNum));
        b.append("</docPageNumber>\n");
        b.append("<displayName>");
        b.append(EditUtil.encodeXmlEntities(m_displayName));
        b.append("</displayName>\n");

        // add in the base href so that the images will display properly
        // For bug AMB-177 that the file name has some special chars, such as
        // "&".
        b.append("<baseHref>");
        // "/en_US/"
        b.append(EditUtil.encodeXmlEntities(m_baseHref));
        b.append("/");
        b.append("</baseHref>\n");

        // Added by Vincent Yan
        b.append("<priority>");
        b.append(m_priority);
        b.append("</priority>\n");

        b.append("<jobName>");
        b.append(EditUtil.encodeXmlEntities(m_jobName));
        b.append("</jobName>\n");

        // from GBS-2137
        b.append("<jobId>");
        b.append(m_jobId);
        b.append("</jobId>\n");

        b.append("<uuid>");
        b.append(EditUtil.encodeXmlEntities(jobUuid));
        b.append("</uuid></batchInfo>\n");

        b.append("<source name=\"FileSystemSourceAdapter\" ");

        if (p_isAutomaticImport)
            b.append("dataSourceType=\"fsAutoImport\" dataSourceId=\"");
        else
            b.append("dataSourceType=\"fs\" dataSourceId=\"");

        b.append(m_fileProfileId);
        b.append("\" formatType=\"");
        b.append(m_formatType);
        b.append("\" formatName=\"");
        b.append(m_formatName);
        b.append("\" pageIsCxePreviewable=\"false\" importRequestType=\"");
        b.append(m_importRequestType);
        if (m_importInitiatorId != null)
        {
            b.append("\" importInitiatorId=\"");
            b.append(EditUtil.encodeXmlEntities(m_importInitiatorId));
        }
        b.append("\">\n<locale>");
        b.append(m_locale);
        b.append("</locale>\n");
        b.append("<charset>");
        b.append(m_codeset);
        b.append("</charset>\n");
        b.append("<da name=\"Filename\"><dv>");
        b.append(EditUtil.encodeXmlEntities(m_relativePathName));
        b.append("</dv></da>\n");
        b.append("</source>\n");
        b.append("<target name=\"FileSystemTargetAdapter\">\n");
        if (m_targetLocales == null || "".equals(m_targetLocales.trim()))
        {
            b.append("<locale>unknown</locale>\n");
        }
        else
        {
            b.append("<locale>").append(m_targetLocales).append("</locale>\n");
        }
        b.append("<charset>unknown</charset>\n");
        b.append("<da name=\"ExportLocation\"><dv>unknown</dv></da>\n");
        b.append("<da name=\"LocaleSubDir\"><dv>unknown</dv></da>\n");
        b.append("</target>\n</eventFlowXml>\n");

        eventFlowXml = b.toString();

        Logger.writeDebugFile("fssa_ef.xml", eventFlowXml);

        return eventFlowXml;
    }

    /**
     * Returns the CxeMessageType used as the pre-extract event. That is, this
     * is the event used to send the content to an extractor, or other format
     * converter.
     * 
     * @return CxeMessageType
     */
    public CxeMessageType getPreExtractEvent()
    {
        return CxeMessageType.getCxeMessageType(m_preExtractEvent);
    }

    // ////////////////////////////////////
    // Private Methods //
    // ////////////////////////////////////

    // gets some data needed for the EventFlowXml
    private void getEventFlowXmlData() throws FileSystemAdapterException
    {
        // now try to find the file data source profile type
        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        try
        {
            connection = ConnectionPool.getConnection();
            StringBuffer sql = new StringBuffer(
                    "SELECT KNOWN_FORMAT_TYPE.FORMAT_TYPE, KNOWN_FORMAT_TYPE.PRE_EXTRACT_EVENT, KNOWN_FORMAT_TYPE.PRE_MERGE_EVENT, FILE_PROFILE.CODE_SET, L10N_PROFILE.ID, LOCALE.ISO_LANG_CODE, LOCALE.ISO_COUNTRY_CODE,KNOWN_FORMAT_TYPE.NAME");
            sql.append(" FROM KNOWN_FORMAT_TYPE, FILE_PROFILE, L10N_PROFILE, LOCALE");
            sql.append(" WHERE FILE_PROFILE.ID=?");
            sql.append(" AND KNOWN_FORMAT_TYPE.ID=FILE_PROFILE.KNOWN_FORMAT_TYPE_ID");
            sql.append(" AND L10N_PROFILE.ID=FILE_PROFILE.L10N_PROFILE_ID");
            sql.append(" AND LOCALE.ID=L10N_PROFILE.SOURCE_LOCALE_ID");

            query = connection.prepareStatement(sql.toString());
            query.setString(1, m_fileProfileId);
            results = query.executeQuery();
            if (results.next())
            {
                m_formatType = results.getString(1);

                m_preExtractEvent = m_overrideAsUnextracted ? CxeMessageType
                        .getCxeMessageType(
                                CxeMessageType.UNEXTRACTED_IMPORTED_EVENT)
                        .getName() : results.getString(2);

                m_preMergeEvent = m_overrideAsUnextracted ? CxeMessageType
                        .getCxeMessageType(
                                CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT)
                        .getName() : results.getString(3);

                m_codeset = results.getString(4);
                m_l10nProfileId = results.getString(5);
                String lang = results.getString(6);
                String country = results.getString(7);
                m_locale = lang + "_" + country;
                m_formatName = results.getString(8);

            }
            else
            {
                m_logger.error("No file profile " + m_fileProfileId
                        + " exists in DB.");
                m_errorArgs[2] = m_fileProfileId;
                throw new FileSystemAdapterException("FileProfileIm",
                        m_errorArgs, null);
            }
        }
        catch (ConnectionPoolException cpe)
        {
            m_logger.error("Could not get connection to DB: ", cpe);

            throw new FileSystemAdapterException("DbConnectionIm", m_errorArgs,
                    cpe);
        }
        catch (SQLException sqle)
        {
            m_logger.error(
                    "Could not query file profile, and format type from the DB: ",
                    sqle);

            throw new FileSystemAdapterException("SqlExceptionIm", m_errorArgs,
                    sqle);
        }
        finally
        {
            ConnectionPool.silentClose(results);
            ConnectionPool.silentClose(query);
            ConnectionPool.silentReturnConnection(connection);
        }
    }

    /**
     * Create a basehref to use based on the filename. This gets the relative
     * path to the directory where the file is, and replaces backslash with
     * slash.
     * 
     * @param p_filename
     *            the filename
     * @return
     */
    private String getBaseHref(String p_filename)
    {
    	p_filename = p_filename.replace('\\', '/');
        int endIndex = p_filename.lastIndexOf("/");
        return p_filename.substring(0, endIndex);
    }
}
