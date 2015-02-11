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

package com.globalsight.everest.projecthandler.importer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.importer.IImportManagerImpl;
import com.globalsight.importer.IReader;
import com.globalsight.importer.ImportOptions;
import com.globalsight.importer.ImporterException;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.progress.ProcessStatus;

/**
 * <p>
 * The RMI interface implementation for the Project Importer.
 * </p>
 */
public class ImportManager extends IImportManagerImpl implements Serializable
{
	private static final Logger CATEGORY = Logger
			.getLogger(ImportManager.class);

	/**
	 * The size of batches that are sent to the termbase.
	 */
	static final private int BATCHSIZE = 10;

	public static final int SYNC_OVERWRITE = 1;

	public static final int SYNC_MERGE = 2;

	public static final int SYNC_DISCARD = 3;

	//
	// Private Members
	//
	private Project m_project = null;

	//
	// Constructor
	//
	public ImportManager(Project p_project, SessionInfo p_session)
			throws ImporterException
	{
		super(p_session);

		m_project = p_project;

		CATEGORY.debug("Default Project import manager created.");
	}

	protected ImportManager(Project p_project, SessionInfo p_session,
			String p_filename, String p_options) throws ImporterException
	{
		super(p_filename, p_options, p_session);

		m_project = p_project;
	}

	//
	// Overwritten Abstract Methods
	//
	protected ImportOptions createImportOptions() throws ImporterException
	{
		return new com.globalsight.everest.projecthandler.importer.ImportOptions();
	}

	protected ImportOptions createImportOptions(String p_options)
			throws ImporterException
	{
		com.globalsight.everest.projecthandler.importer.ImportOptions result = new com.globalsight.everest.projecthandler.importer.ImportOptions();

		result.init(p_options);

		return result;
	}

	/**
	 * Simulates the conversion of lines to entries and returns error messages,
	 * but doesn't write data to the database.
	 */
	protected void runTestImport() throws ImporterException
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
	protected void runImport() throws ImporterException
	{
		CATEGORY.info("Starting import of file " + getFileName());

		ArrayList entries = new ArrayList(BATCHSIZE);
		ReaderResult result;

		int counter = 0, errCounter = 0;

		ImportOptions options = getOptions();
		int expectedEntries = options.getExpectedEntryCount();

		int syncMode = mapSyncMode(options);

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
						flushEntries(entries, syncMode);
					}
					catch (Throwable ex)
					{
						++errCounter;

						CATEGORY.error("error adding entries", ex);

                        String msg = ProcessStatus.getStringFormattedFromResBundle(m_listener,
                                "lb_import_cannot_add_entries_pattern",
                                "Can't add a batch of {0} entries", entries.size());
                        showStatus(counter, expectedEntries, msg + ": " + ex.getMessage());
					}

					entries.clear();
				}

				showStatus(counter, expectedEntries, "");
			}

			// flush remaining entries
			if (entries.size() > 0)
			{
				try
				{
					flushEntries(entries, syncMode);
				}
				catch (Exception ex)
				{
					++errCounter;

					CATEGORY.error("error adding entries", ex);

                    String msg = ProcessStatus.getStringFormattedFromResBundle(m_listener,
                            "lb_import_cannot_add_entries_pattern",
                            "Can't add a batch of {0} entries", entries.size());
                    showStatus(counter, expectedEntries, msg + ": " + ex.getMessage());
				}

				entries.clear();
			}
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
		}
	}

	protected IReader createReader(ImportOptions p_options)
			throws ImporterException
	{
		com.globalsight.everest.projecthandler.importer.ImportOptions options = (com.globalsight.everest.projecthandler.importer.ImportOptions) p_options;

		String type = options.getFileType();

		if (type != null && type.length() > 0)
		{
			if (type.equalsIgnoreCase(options.TYPE_XML))
			{
				CATEGORY.info("Import manager created of type XML");

				return new XmlReader(options, m_project);
			}
			else if (type.equalsIgnoreCase(options.TYPE_CSV))
			{
				CATEGORY.info("Import manager created of type CSV");

				return new CsvReader(options, m_project);
			}
		}

		p_options.setFileType(options.TYPE_UNKNOWN);
		p_options.setError("unrecognized file type `" + type + "'");

		CATEGORY.error("SNH: NO IMPORT MANAGER CREATED");

		return null;
	}

	private int mapSyncMode(ImportOptions p_options)
	{
		com.globalsight.everest.projecthandler.importer.ImportOptions options = (com.globalsight.everest.projecthandler.importer.ImportOptions) p_options;

		String mode = options.getSyncMode();

		if (mode
				.equals(com.globalsight.everest.projecthandler.importer.ImportOptions.SYNC_OVERWRITE))
		{
			return SYNC_OVERWRITE;
		}
		else if (mode
				.equals(com.globalsight.everest.projecthandler.importer.ImportOptions.SYNC_MERGE))
		{
			return SYNC_MERGE;
		}
		else if (mode
				.equals(com.globalsight.everest.projecthandler.importer.ImportOptions.SYNC_DISCARD))
		{
			return SYNC_DISCARD;
		}

		return -1;
	}

	private void flushEntries(ArrayList p_entries, int p_syncMode)
			throws Exception
	{
		ServerProxy.getCalendarManager().importEntries(p_entries);
	}
}
