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

package com.globalsight.everest.glossaries;

import java.io.File;
import java.io.FileFilter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GlobalSightLocale;

/**
 * <p>
 * Implementation of glossary file persistence server.
 * </p>
 * 
 * <p>
 * Files are stored under <filestorage>/GlobalSight/SupportFiles. Each source
 * locale is a sub-directory, each target locale is a sub-directory within the
 * source locale:
 * </p>
 * 
 * <pre>
 * /SupportFiles/en_US/de_DE/glossar.txt
 * /SupportFiles/en_US/fr_FR/glossair.txt
 * /SupportFiles/de_DE/en_US/glossary.txt
 * /SupportFiles/de_DE/fr_FR/more_info.txt
 * </pre>
 */
public class GlossaryManagerLocal implements GlossaryManager
{
	private static final Logger CATEGORY = Logger
			.getLogger(GlossaryManagerLocal.class.getName());

	public GlossaryManagerLocal()
	{
	}

	/**
     * Persists a new glossary file in data store.
     */
	public void createGlossary(GlossaryFile p_file) throws RemoteException,
			GlossaryException
	{
		// relevant only when saving to database
	}

	/**
     * Deletes the specified glossary file.
     */
	public void deleteGlossary(GlossaryFile p_file) throws RemoteException,
			GlossaryException
	{
		try
		{
			File file = new File(getFilename(p_file, null));

			if (file.exists())
			{
				file.delete();
			}
		}
		catch (Exception ex)
		{
			String[] arg = { p_file.getFilename() };

			throw new GlossaryException(
					GlossaryException.MSG_FAILED_TO_DELETE_FILE, arg, ex);
		}
	}

	/**
     * Returns the absolute file name for the specified glossary file.
     */
	public String getFilename(GlossaryFile p_file, String companyId) throws RemoteException
	{
		StringBuffer sb = new StringBuffer();

		sb.append(GlossaryUpload.createInstance(companyId).getUploadBaseDir());

		if (p_file.isForAnySourceLocale())
		{
			sb.append(p_file.getGlobalSourceLocaleName());
		}
		else
		{
			sb.append(p_file.getSourceLocale().toString());
		}

		sb.append("/");

		if (p_file.isForAnyTargetLocale())
		{
			sb.append(p_file.getGlobalTargetLocaleName());
		}
		else
		{
			sb.append(p_file.getTargetLocale().toString());
		}

		sb.append("/");
		sb.append(p_file.getFilename());

		return sb.toString();
	}

	/**
     * Returns a list of absolute file names for the glossary files specified in
     * the argument (as Strings).
     */
	public ArrayList getFilenames(Collection p_files) throws RemoteException
	{
		ArrayList result = new ArrayList(p_files.size());

		for (Iterator it = p_files.iterator(); it.hasNext();)
		{
			GlossaryFile file = (GlossaryFile) it.next();

			result.add(getFilename(file, null));
		}

		return result;
	}

	/**
     * Retrieves a glossary file descriptor based on in its id.
     */
	public GlossaryFile getGlossary(long p_id) throws RemoteException,
			GlossaryException
	{
		return null;
	}

	/**
     * Retrieves a list of glossary file descriptors matching the given
     * source/target locale (and category). For now, leave category null or the
     * empty string.
     */
	public ArrayList /* of GlossaryFile */getGlossaries(
			GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale,
			String p_category, String companyId) throws RemoteException, GlossaryException
	{
		ArrayList result;

		result = readGlossaryDirectory(GlossaryUpload.createInstance(companyId)
				.getUploadBaseDir(), p_sourceLocale != null ? p_sourceLocale
				.toString() : null, p_targetLocale != null ? p_targetLocale
				.toString() : null, p_category);

		// add files (any source locale / any target locale)
		/*
         * result.addAll( readGlossaryDirectory(
         * GlossaryUpload.UPLOAD_BASE_DIRECTORY +
         * GlossaryUpload.UPLOAD_DIRECTORY,
         * GlossaryUpload.KEY_ANY_SOURCE_LOCALE,
         * GlossaryUpload.KEY_ANY_TARGET_LOCALE, p_category)); // add files (ANY
         * source locale / THIS target locale) result.addAll(
         * readGlossaryDirectory( GlossaryUpload.UPLOAD_BASE_DIRECTORY +
         * GlossaryUpload.UPLOAD_DIRECTORY,
         * GlossaryUpload.KEY_ANY_SOURCE_LOCALE, p_targetLocale != null ?
         * p_targetLocale.toString(): null, p_category)); // add files (THIS
         * source locale / ANY target locale) result.addAll(
         * readGlossaryDirectory( GlossaryUpload.UPLOAD_BASE_DIRECTORY +
         * GlossaryUpload.UPLOAD_DIRECTORY, p_sourceLocale != null ?
         * p_sourceLocale.toString(): null,
         * GlossaryUpload.KEY_ANY_TARGET_LOCALE, p_category));
         */
		return result;
	}

	//
	// Private methods
	//

	/**
     * Helper function to verify locale.
     */
	private GlobalSightLocale getLocale(String p_locale)
	{
		LocaleManager manager;

		try
		{
			manager = ServerProxy.getLocaleManager();
			return manager.getLocaleByString(p_locale);
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	/**
     * Reads a directory structure and converts files to GlossaryFile objects.
     * Category is currently ignored. If source and target locale are null, all
     * files are returned.
     */
	private ArrayList readGlossaryDirectory(String p_root,
			final String p_sourceLocale, final String p_targetLocale,
			String p_category)
	{
		ArrayList result = new ArrayList();

		File root = new File(p_root);

		if (!root.exists() || !root.isDirectory())
		{
			if (CATEGORY.isDebugEnabled())
			{
				CATEGORY.debug("Not a glossary directory: " + p_root);
			}

			return result;
		}

		// directory structure:
		// root/srclocale/trglocale/file

		// Read source locale directories. Filter by source locale if
		// that is requested.
		File[] srcDirs = root.listFiles(new FileFilter()
		{
			public boolean accept(File p_path)
			{
				String name = p_path.getName();

				if (p_path.isDirectory()
						&& ((name.length() == 5 && name.charAt(2) == '_') || name
								.equals(GlossaryUpload.KEY_ANY_SOURCE_LOCALE)))
				{
					if (p_sourceLocale != null
							&& !p_sourceLocale.equalsIgnoreCase(name)
							&& !name
									.equals(GlossaryUpload.KEY_ANY_SOURCE_LOCALE))
					{
						return false;
					}

					return true;
				}

				return false;
			}
		});

		for (int i = 0; i < srcDirs.length; ++i)
		{
			boolean anySourceLocale = false;
			GlobalSightLocale sourceLocale = getLocale(srcDirs[i].getName());
			if (sourceLocale == null)
			{
				// Directory name did not represent a valid locale.
				// Did the user selected Any_SourceLocale as source locale?
				if (srcDirs[i].getName().equals(
						GlossaryUpload.KEY_ANY_SOURCE_LOCALE))
				{
					anySourceLocale = true;
					// create dummy locale
					sourceLocale = new GlobalSightLocale("en", "US", false);
				}
				else
				{
					continue;
				}
			}

			// Read target locale directories. Filter by target locale
			// if that is requested.
			File[] trgDirs = srcDirs[i].listFiles(new FileFilter()
			{
				public boolean accept(File p_path)
				{
					String name = p_path.getName();

					if (p_path.isDirectory()
							&& ((name.length() == 5 && name.charAt(2) == '_') || name
									.equals(GlossaryUpload.KEY_ANY_TARGET_LOCALE)))
					{
						if (p_targetLocale != null
								&& !p_targetLocale.equalsIgnoreCase(name)
								&& !name
										.equals(GlossaryUpload.KEY_ANY_TARGET_LOCALE))
						{
							return false;
						}

						return true;
					}

					return false;
				}
			});

			for (int j = 0; j < trgDirs.length; ++j)
			{
				boolean anyTargetLocale = false;
				GlobalSightLocale targetLocale = getLocale(trgDirs[j].getName());
				if (targetLocale == null)
				{
					// Directory name did not represent a valid locale
					// Did the user selected Any_TargetLocale as source locale?
					if (trgDirs[j].getName().equals(
							GlossaryUpload.KEY_ANY_SOURCE_LOCALE))
					{
						anyTargetLocale = true;
						// create dummy locale
						targetLocale = new GlobalSightLocale("en", "US", false);
					}
					else
					{
						continue;
					}
				}

				// Read all files in the targetlocale directory
				File[] files = trgDirs[j].listFiles(new FileFilter()
				{
					public boolean accept(File p_path)
					{
						if (!p_path.isDirectory())
						{
							return true;
						}

						return false;
					}
				});

				for (int k = 0; k < files.length; ++k)
				{
					GlossaryFile g = new GlossaryFile();

					g.setAnySourceLocale(anySourceLocale);
					g.setSourceLocale(sourceLocale);
					g.setAnyTargetLocale(anyTargetLocale);
					g.setTargetLocale(targetLocale);
					g.setCategory(p_category);
					g.setFileSize(files[k].length());
					g.setLastModified(files[k].lastModified());
					g.setFilename(files[k].getName());

					result.add(g);
				}
			}
		}

		return result;
	}
}
