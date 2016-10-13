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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;

public class GlossaryUpload
{
	//
	// Public Member variables
	//

	// Attribute names expected in the http request. These correspond
	// to the field names in the html form used to post the data.

	/** The source locale of the uploaded file */
	public final static String KEY_SOURCE_LOCALE = "src";

	/** Indicates global usage of a given support file with any source locale */
	public final static String KEY_ANY_SOURCE_LOCALE = "All";

	/** One or more target locales of the uploaded file */
	public final static String KEY_TARGET_LOCALE = "trg";

	/** Indicates global usage of a given support file with any source locale */
	public final static String KEY_ANY_TARGET_LOCALE = "All";

	/** The categories assigned to the uploaded file */
	public final static String KEY_CATEGORY = "cat";

	/* The directory to which temp files are created. */
	private File uploadTmpDir = null;

	public String uploadBaseDir = "/";

	//
	// Private Member variables
	//

	private final static int MAX_LINE_LENGTH = 4096;

	private Hashtable<String, String> m_fields = new Hashtable<String, String>();

	private String m_filename = null;

	private String m_savedFilepath = null;

	private File m_tempFile = null;
	
	private String companyId = null;

	//
	// Methods
	//

	public GlossaryUpload()
	{
		init();
	}
	public GlossaryUpload(String companyId) {
		this.companyId = companyId;
		init();
	}

	public static GlossaryUpload createInstance()
	{
		return new GlossaryUpload();
	}
	
	public static GlossaryUpload createInstance(String companyId) {
		return new GlossaryUpload(companyId);
	}

	/**
	 * Initializes the upload base dir and the uplodatmpdir.
	 */
	private void init()
	{
		SystemConfiguration sc = SystemConfiguration.getInstance();

		uploadBaseDir = sc
				.getStringParameter(SystemConfiguration.FILE_STORAGE_DIR, this.companyId);

		if (!(uploadBaseDir.endsWith("/") || uploadBaseDir.endsWith("\\")))
		{
			uploadBaseDir = uploadBaseDir + "/";
		}

		// <filestorage>/GlobalSight/
		uploadBaseDir = uploadBaseDir + WebAppConstants.VIRTUALDIR_TOPLEVEL;

		// <filestorage>/GlobalSight/tmp/
		uploadTmpDir = new File(uploadBaseDir + "/tmp/");
		uploadTmpDir.mkdirs();

		// <filestorage>/GlobalSight/SupportFiles/
		uploadBaseDir += WebAppConstants.VIRTUALDIR_SUPPORTFILES;
	}

	/**
     * Main method: reads an uploaded file from a http request (result of a form
     * post) and saves it to the file system.
     */
	public void doUpload(HttpServletRequest p_request) throws GlossaryException
	{
		try
		{
			// read request and save uploaded file into a temp file
			readRequest(p_request);
			// verify that the upload contained full information;
			// throws exception if not
			verifyUpload();
			// rename the temp file to the first real file (src/trg)
			renameFile(p_request);
			// if requested, copy file to other src/trg locations
			copyToTargets();
		}
		catch (SecurityException se)
		{
			throw new GlossaryException(se);
		}
		catch (IOException ioe)
		{
			throw new GlossaryException(ioe);
		}
	}

	/**
     * Parses a HttpRequest into parameters and file data and saves the file
     * data into a temporary file.
     */
	private void readRequest(HttpServletRequest p_request)
			throws GlossaryException, IOException
	{
		byte[] inBuf = new byte[MAX_LINE_LENGTH];
		int bytesRead;
		ServletInputStream in;
		String contentType;
		String boundary;

		// Let's make sure that we have the right type of content
		//
		contentType = p_request.getContentType();
		if (contentType == null
				|| !contentType.toLowerCase().startsWith("multipart/form-data"))
		{
			String[] arg = { "form did not use ENCTYPE=multipart/form-data but `"
					+ contentType + "'" };

			throw new GlossaryException(
					GlossaryException.MSG_FAILED_TO_UPLOAD_FILE, arg, null);
		}

		// Extract the boundary string in this request. The
		// boundary string is part of the content type string
		//
		int bi = contentType.indexOf("boundary=");
		if (bi == -1)
		{
			String[] arg = { "no boundary string found in request" };

			throw new GlossaryException(
					GlossaryException.MSG_FAILED_TO_UPLOAD_FILE, arg, null);
		}
		else
		{
			// 9 := len("boundary=")
			boundary = contentType.substring(bi + 9);

			// The real boundary has additional two dashes in
			// front
			//
			boundary = "--" + boundary;
		}

		in = p_request.getInputStream();
		bytesRead = in.readLine(inBuf, 0, inBuf.length);

		if (bytesRead < 3)
		{
			String[] arg = { "incomplete request (not enough data)" };

			// Not enough content was send as part of the post
			throw new GlossaryException(
					GlossaryException.MSG_FAILED_TO_UPLOAD_FILE, arg, null);
		}

		while (bytesRead != -1)
		{
			String lineRead = new String(inBuf, 0, bytesRead, "utf-8");
			if (lineRead.startsWith("Content-Disposition: form-data; name=\""))
			{
				if (lineRead.indexOf("filename=\"") != -1)
				{
					// This is a file part

					// Get file name
					setFilename(lineRead.substring(0, lineRead.length() - 2));

					// Get content type line
					bytesRead = in.readLine(inBuf, 0, inBuf.length);
					lineRead = new String(inBuf, 0, bytesRead - 2, "utf-8");

					// Read and ignore the blank line
					bytesRead = in.readLine(inBuf, 0, inBuf.length);

					// Create a temporary file to store the
					// contents in it for now. We might not have
					// additional information, such as TUV id for
					// building the complete file path. We will
					// save the contents in this file for now and
					// finally rename it to correct file name.
					//
					m_tempFile = File.createTempFile("GSGlossaryUpload", null, uploadTmpDir);

					FileOutputStream fos = new FileOutputStream(m_tempFile);
					BufferedOutputStream bos = new BufferedOutputStream(fos,
							MAX_LINE_LENGTH * 4);

					// Read through the file contents and write
					// it out to a local temp file.
					boolean writeRN = false;
					while ((bytesRead = in.readLine(inBuf, 0, inBuf.length)) != -1)
					{
						// Let's first check if we are already on
						// boundary line
						if (bytesRead > 2 && inBuf[0] == '-' && inBuf[1] == '-')
						{
							lineRead = new String(inBuf, 0, bytesRead, "utf-8");
							if (lineRead.startsWith(boundary))
							{
								break;
							}
						}

						// Write out carriage-return, new-line
						// pair which might have been left over
						// from last write.
						//
						if (writeRN)
						{
							bos.write(new byte[] { (byte) '\r', (byte) '\n' });
							writeRN = false;
						}

						// The ServletInputStream.readline() adds
						// "\r\n" bytes for the last line of the
						// file contents. If we find these pair
						// as the last bytes we need to delay
						// writing it until the next go, since it
						// could very well be the last line of
						// file content.
						//
						if (bytesRead > 2 && inBuf[bytesRead - 2] == '\r'
								&& inBuf[bytesRead - 1] == '\n')
						{
							bos.write(inBuf, 0, bytesRead - 2);
							writeRN = true;
						}
						else
						{
							bos.write(inBuf, 0, bytesRead);
						}
					}

					bos.flush();
					bos.close();
					fos.close();
				}
				else
				{
					// This is the field part

					// First get the field name

					int start = lineRead.indexOf("name=\"");
					int end = lineRead.indexOf("\"", start + 7);
					String fieldName = lineRead.substring(start + 6, end);

					// Read and ignore the blank line
					bytesRead = in.readLine(inBuf, 0, inBuf.length);

					// String Buffer to keep the field value
					//
					StringBuffer fieldValue = new StringBuffer();

					boolean writeRN = false;
					while ((bytesRead = in.readLine(inBuf, 0, inBuf.length)) != -1)
					{
						lineRead = new String(inBuf, 0, bytesRead, "utf-8");

						// Let's first check if we are already on
						// boundary line
						//
						if (bytesRead > 2 && inBuf[0] == '-' && inBuf[1] == '-')
						{
							if (lineRead.startsWith(boundary))
							{
								break;
							}
						}

						// Write out carriage-return, new-line
						// pair which might have been left over
						// from last write.
						//
						if (writeRN)
						{
							fieldValue.append("\r\n");
							writeRN = false;
						}

						// The ServletInputStream.readline() adds
						// "\r\n" bytes for the last line of the
						// field value. If we find these pair as
						// the last bytes we need to delay
						// writing it until the next go, since it
						// could very well be the last line of
						// field value.
						//
						if (bytesRead > 2 && inBuf[bytesRead - 2] == '\r'
								&& inBuf[bytesRead - 1] == '\n')
						{
							fieldValue.append(lineRead.substring(0, lineRead
									.length() - 2));
							writeRN = true;
						}
						else
						{
							fieldValue.append(lineRead);
						}
					}

					// Add field to collection of field
					//
					setFieldValue(fieldName, fieldValue.toString());
				}
			}

			bytesRead = in.readLine(inBuf, 0, inBuf.length);
		}
	}

	/**
     * Verifies that the uploaded information was complete: source and target
     * locale must have been specified, and a temporary file must have been
     * successfully created from the uploaded file. If not, we throw an
     * exception.
     */
	private void verifyUpload() throws GlossaryException
	{
		if (!verifyLocales())
		{
			String[] arg = { "invalid source or target locale" };

			throw new GlossaryException(
					GlossaryException.MSG_FAILED_TO_UPLOAD_FILE, arg, null);
		}

		if (m_tempFile == null || (m_tempFile != null && !m_tempFile.exists()))
		{
			String[] arg = { "temporary file could not be created" };

			throw new GlossaryException(
					GlossaryException.MSG_FAILED_TO_UPLOAD_FILE, arg, null);
		}
	}

	/**
     * Verifies that source and target locales have been set and that they have
     * valid values, i.e. are of the form xx_YY or are equal to the global key
     * word.
     */
	private boolean verifyLocales()
	{
		if (getSourceLocale() == null || getTargetLocale() == null)
		{
			return false;
		}

		String s = getSourceLocale();
		if ((s.length() != 5 || s.charAt(2) != '_')
				&& !s.equalsIgnoreCase(KEY_ANY_SOURCE_LOCALE))
		{
			return false;
		}

		s = getTargetLocale();
		if ((s.length() != 5 || s.charAt(2) != '_')
				&& !s.equalsIgnoreCase(KEY_ANY_TARGET_LOCALE))
		{
			return false;
		}

		return true;
	}

	/**
     * Renames the temporary file to the real name; if that fails, the temporary
     * file is deleted.
     */
	private void renameFile(HttpServletRequest p_request)
			throws GlossaryException, SecurityException
	{
		// We should have all parameter values needed to construct a
		// correct filepath to save the uploaded file.
		if (m_tempFile != null && m_tempFile.exists())
		{
			try
			{
				// First, define and create upload directory structure,
				// if not already done so
				setSavedFilepath(uploadBaseDir + getSourceLocale() + "/"
						+ getTargetLocale() + "/");

				File savedDir = new File(getSavedFilepath());
				savedDir.mkdirs();

				// Create a destination file and rename/move the file
				// from temporary location to upload directory
				File finalFile = new File(savedDir, getFilename());
				m_tempFile.renameTo(finalFile);
			}
			catch (SecurityException ex)
			{
				try
				{
					m_tempFile.delete();
				}
				catch (Exception e)
				{
				}

				throw ex;
			}
		}
	}

	/**
     * Copy the file to other source/target locale directories
     */
	private void copyToTargets()
	{
		// to be done
	}

	private void setFieldValue(String p_fieldName, String p_value)
	{
		m_fields.put(p_fieldName, p_value);
	}

	private String getFieldValue(String p_fieldName)
	{
		if (p_fieldName == null)
		{
			return null;
		}
		else
		{
			return (String) m_fields.get(p_fieldName);
		}
	}

	private void setSavedFilepath(String p_filePath)
	{
		m_savedFilepath = p_filePath;
	}

	private String getSavedFilepath()
	{
		return m_savedFilepath;
	}

	private String getSourceLocale()
	{
		return getFieldValue(KEY_SOURCE_LOCALE);
	}

	private String getTargetLocale()
	{
		return getFieldValue(KEY_TARGET_LOCALE);
	}

	private void setFilename(String p_filenameLine)
	{
		int start = 0;

		if (p_filenameLine != null
				&& (start = p_filenameLine.indexOf("filename=\"")) != -1)
		{
			String filepath = p_filenameLine.substring(start + 10,
					p_filenameLine.length() - 1);

			// Handle Windows v/s Unix file path
			if ((start = filepath.lastIndexOf('\\')) > -1)
			{
				m_filename = filepath.substring(start + 1);
			}
			else if ((start = filepath.lastIndexOf('/')) > -1)
			{
				m_filename = filepath.substring(start + 1);
			}
			else
			{
				m_filename = filepath;
			}
		}
	}

	public String getFilename()
	{
		return m_filename;
	}

	public String getUploadBaseDir()
	{
		return uploadBaseDir;
	}

	public void setUploadBaseDir(String uploadBaseDir)
	{
		this.uploadBaseDir = uploadBaseDir;
	}

	public File getUploadTmpDir()
	{
		return uploadTmpDir;
	}

	public void setUploadTmpDir(File uploadTmpDir)
	{
		this.uploadTmpDir = uploadTmpDir;
	}

}
