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

package com.globalsight.terminology.importer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.globalsight.importer.IImportManagerImpl;
import com.globalsight.importer.IReader;
import com.globalsight.importer.ImportOptions;
import com.globalsight.importer.ImporterException;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseImpl;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;
import com.globalsight.terminology.Termbase.SyncOptions;
import com.globalsight.terminology.audit.TermAuditEvent;
import com.globalsight.terminology.audit.TermAuditLog;
import com.globalsight.terminology.indexer.IIndexManager;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.progress.ClientInterruptException;
import com.globalsight.util.progress.ProcessStatus;
import com.globalsight.util.progress.ProcessStatus2;

/**
 * <p>
 * The RMI interface implementation for the Termbase Importer.
 * </p>
 */
public class ImportManager extends IImportManagerImpl implements
		TermbaseExceptionMessages, Serializable
{
	private static final Logger CATEGORY = Logger
			.getLogger(ImportManager.class);

	/**
	 * The size of batches that are sent to the termbase.
	 */
	static final private int BATCHSIZE = 40;

	//
	// Private Members
	//
	private Termbase m_database = null;

    // reindex_status

    private ProcessStatus2 reindex_status = null;

	// Constructor
	public ImportManager(Termbase p_database, SessionInfo p_session)
			throws ImporterException
	{
		super(p_session);

		m_database = p_database;

		CATEGORY.debug("Default import manager created.");
	}

	public ImportManager(Termbase p_database, SessionInfo p_session,
			String p_filename, String p_options) throws ImporterException
	{
		super(p_filename, p_options, p_session);

		m_database = p_database;
	}

	//
	// Overwritten Abstract Methods
	//
	protected ImportOptions createImportOptions() throws ImporterException
	{
		return new com.globalsight.terminology.importer.ImportOptions();
	}

	protected ImportOptions createImportOptions(String p_options)
			throws ImporterException
	{
		com.globalsight.terminology.importer.ImportOptions result = new com.globalsight.terminology.importer.ImportOptions();

		result.init(p_options);

		return result;
	}

	/**
	 * For CSV files: simulates the conversion of lines to entries and returns
	 * error messages, but doesn't write data to the database.
	 */
	public void runTestImport() throws ImporterException
	{
		CATEGORY.debug("**TESTING** import of file " + getFileName());

		ReaderResult result;

		int counter = 0;
		int errCounter = 0;
		int expectedEntries = getOptions().getExpectedEntryCount();

		try
		{
			while (m_reader.hasNext())
			{
				result = m_reader.next();
				++counter;

				if (result.getStatus() == result.ERROR)
				{
					// Couldn't read the entry, output message and continue.
					++errCounter;
					showStatus(counter, expectedEntries, result.getMessage());
					continue;
				}

				showStatus(counter, expectedEntries, "");
			}
		}
		catch (IOException ignore)
		{
			// Our client's web-client just died.
			CATEGORY.info("client died, aborting import ("
					+ ignore.getMessage() + ")");
		}
		catch (Throwable ignore)
		{
			CATEGORY.error("unexpected error, aborting import ("
					+ ignore.getMessage() + ")");
		}
		finally
		{
			// We're done, bump progress bar to 100%.
			String message = "test finished; " + counter
					+ " entries processed, " + errCounter + " error"
					+ (errCounter == 1 ? "" : "s");
			try
			{
				speak(counter, 100, message);
			}
			catch (Throwable ignore)
			{
			}
		}
	}

	/**
	 * With all ImportOptions set, start the actual import.
	 */
	public void runImport() throws ImporterException
	{
		CATEGORY.info("Starting import of file " + getFileName());

		TermAuditEvent auditEvent = new TermAuditEvent(new Date(), m_session
				.getUserName(), m_database.getName(), m_database.getName(),
				"ALL", "import termbase", null);
		auditEvent.details = "starting import of termbase file "
				+ getFileName();
		TermAuditLog.log(auditEvent);

		ArrayList entries = new ArrayList(BATCHSIZE);
		ReaderResult result;

		int counter = 0, errCounter = 0;

		ImportOptions options = getOptions();
		int expectedEntries = options.getExpectedEntryCount();
		String fileType = options.getFileType();

		Termbase.SyncOptions sync = null;
		if (isExcelImport(options))
		{
			sync = buildExcelDefaultSyncOptions();
		}
		else
		{
			sync = mapSyncOptions(options);
		}

		try
		{
			while (m_reader.hasNext())
			{
				result = m_reader.next();
				++counter;

				if (result.getStatus() == result.ERROR)
				{
					// Couldn't read the entry, output message and continue.
					++errCounter;
					showStatus(counter, expectedEntries, result.getMessage());
					continue;
				}

				entries.add(result.getResultObject());

				if (entries.size() == BATCHSIZE)
				{
					try
					{
						ArrayList failed = m_database.batchAddEntries(entries, sync, m_session, fileType);
						errCounter =  errCounter + failed.size();
					}
					catch (TermbaseException ex)
					{
						CATEGORY.error("error1 adding entries", ex);
					}

					entries.clear();
				}

				showStatus(counter, expectedEntries, "");
			}
	        CATEGORY.info("entries.size()."+entries.size());
			// flush remaining entries
			if (entries.size() > 0)
			{
				try
                {
				    ArrayList failed = m_database.batchAddEntries(entries, sync, m_session, fileType);
				    errCounter =  errCounter + failed.size();
					
                    //re-index the new added entry
					ITermbase itb = new ITermbaseImpl(m_database, m_session);
                    IIndexManager indexManager = itb.getIndexer();
                    indexManager.attachListener(reindex_status);
                    indexManager.doIndex();
				}
				catch (TermbaseException ex)
				{
					CATEGORY.error("error adding entries", ex);
				}
			}
		}
        catch (ClientInterruptException e)
        {
            CATEGORY.info("client error: user cancelled the tb import!");
        }
		catch (IOException ignore)
		{
			CATEGORY.error("client error", ignore);
			// Our client's web-client just died, return.
		}
		catch (Throwable ignore)
		{
			CATEGORY.error("client error", ignore);
			// Our client's web-client just died, return.
		}
		finally
		{
			// We're done, bump progress bar to 100%.
//			String message = "import finished; " + counter
//					+ " entries processed, " + errCounter + " error"
//					+ (errCounter == 1 ? "" : "s");
		    String pattern = "import finished; {0} entries processed, {1} error(s)";
            String message = ProcessStatus.getStringFormattedFromResBundle(m_listener,
                    "lb_import_result_pattern", pattern, counter, errCounter);
			try
			{
				speak(counter, 100, message);
			}
			catch (Throwable ignore)
			{
			}
			auditEvent.date = new Date();
			auditEvent.details = "finished import of termbase file "
					+ getFileName();
			TermAuditLog.log(auditEvent);
		}
	}

	//
	// Private Methods
	//

	protected IReader createReader(ImportOptions p_options)
			throws ImporterException
	{
		com.globalsight.terminology.importer.ImportOptions options = (com.globalsight.terminology.importer.ImportOptions) p_options;

		String type = options.getFileType();

		if (type != null && type.length() > 0)
		{
			if (type.equalsIgnoreCase(options.TYPE_XML))
			{
				CATEGORY.info("Import manager created of type XML");

				p_options.setFileType(options.TYPE_XML);
				return new GTXmlReader(options, m_database);
			}
			else if (type.equalsIgnoreCase(options.TYPE_MTF))
			{
				CATEGORY.info("Import manager created of type MTF");

				p_options.setFileType(options.TYPE_MTF);
				return new MtfReader(options, m_database);
			}
			else if (type.equalsIgnoreCase(options.TYPE_CSV))
			{
				CATEGORY.info("Import manager created of type CSV");

				p_options.setFileType(options.TYPE_CSV);
				return new CsvReader(options, m_database);
			}
			else if (type.equalsIgnoreCase(options.TYPE_EXCEL))
			{
				CATEGORY.info("Import manager created of type EXCEL");

				p_options.setFileType(options.TYPE_EXCEL);
				return new ExcelReader(options, m_database);
			}
			else if (type.equalsIgnoreCase(options.TYPE_TBX))
			{
				CATEGORY.info("Import manager created of type TBX");
				
				p_options.setFileType(options.TYPE_TBX);
				return new TbxReader(options, m_database);
			}
		}

		p_options.setFileType(options.TYPE_UNKNOWN);
		p_options.setError("unrecognized file type `" + type + "'");

		CATEGORY.error("SNH: NO IMPORT MANAGER CREATED");

		return null;
	}

	private Termbase.SyncOptions mapSyncOptions(ImportOptions p_options)
	{
		com.globalsight.terminology.importer.ImportOptions options = (com.globalsight.terminology.importer.ImportOptions) p_options;

		Termbase.SyncOptions result = new Termbase.SyncOptions();

		String mode = options.getSyncMode();

		if (mode.equals(options.SYNC_BY_NONE))
		{
			result.setSyncMode(SyncOptions.SYNC_BY_NONE);
		}
		else if (mode.equals(options.SYNC_BY_CONCEPTID))
		{
			result.setSyncMode(SyncOptions.SYNC_BY_CONCEPTID);
		}
		else if (mode.equals(options.SYNC_BY_LANGUAGE))
		{
			result.setSyncMode(SyncOptions.SYNC_BY_LANGUAGE);
		}

		String syncAction = options.getSyncAction();

		if (syncAction.equals(options.SYNC_OVERWRITE))
		{
			result.setSyncAction(SyncOptions.SYNC_OVERWRITE);
		}
		else if (syncAction.equals(options.SYNC_MERGE))
		{
			result.setSyncAction(SyncOptions.SYNC_MERGE);
		}
		else if (syncAction.equals(options.SYNC_DISCARD))
		{
			result.setSyncAction(SyncOptions.SYNC_DISCARD);
		}

		String nosyncAction = options.getNosyncAction();

		if (nosyncAction.equals(options.NOSYNC_ADD))
		{
			result.setNosyncAction(SyncOptions.NOSYNC_ADD);
		}
		else if (nosyncAction.equals(options.NOSYNC_DISCARD))
		{
			result.setNosyncAction(SyncOptions.NOSYNC_DISCARD);
		}

		String language = options.getSyncLanguage();
		result.setSyncLanguage(language);

		return result;
	}

	private boolean isExcelImport(ImportOptions p_options)
	{
		com.globalsight.terminology.importer.ImportOptions options = (com.globalsight.terminology.importer.ImportOptions) p_options;
		String fileType = p_options.getFileType();
		if (fileType.equalsIgnoreCase(options.TYPE_EXCEL)
				|| fileType.equalsIgnoreCase("xls")
				|| fileType.equalsIgnoreCase("xlsx"))
		{
			return true;
		}
		return false;
	}

	private Termbase.SyncOptions buildExcelDefaultSyncOptions()
	{

		Termbase.SyncOptions result = new Termbase.SyncOptions();
		result.setSyncMode(SyncOptions.SYNC_BY_LANGUAGE);
		result.setSyncAction(SyncOptions.SYNC_MERGE);
		result.setNosyncAction(SyncOptions.NOSYNC_ADD);

		com.globalsight.terminology.importer.ImportOptions options = (com.globalsight.terminology.importer.ImportOptions) getOptions();
		ArrayList columnDescriptors = options.getColumns();
		com.globalsight.terminology.importer.ImportOptions.ColumnDescriptor col = null;
		String syncLang = null;
		for (int i = 0; i < columnDescriptors.size(); i++)
		{
			col = (com.globalsight.terminology.importer.ImportOptions.ColumnDescriptor) columnDescriptors
					.get(i);
			if (syncLang == null && col.m_type.equals("term"))
			{
				// Firstly, we take the first term column's language as sync
				// language.
				syncLang = col.m_termLanguage;
			}
			if (col.m_type.equals("term")
					&& col.m_termLanguage.equalsIgnoreCase("English"))
			{
				// If we found a term column's language is 'English', we take it
				// as sync language.
				syncLang = col.m_termLanguage;
			}
		}
		if (syncLang != null)
		{
			result.setSyncLanguage(syncLang);
		}
		else
		{
			result.setSyncLanguage("English");
		}

		return result;

	}

    public void setReindexStatus(ProcessStatus2 reindexStatus)
    {
        // TODO Auto-generated method stub
        reindex_status = reindexStatus;
    }
}
