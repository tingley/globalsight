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
package com.globalsight.connector.blaise.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import com.cognitran.blaise.translation.api.ClientFactory;
import com.cognitran.blaise.translation.api.TranslationAgencyClient;
import com.cognitran.client.IncompatibleVersionException;
import com.cognitran.translation.client.workflow.TranslationInboxEntry;
import com.cognitran.workflow.client.InboxEntry;
import com.cognitran.workflow.client.State;
import com.globalsight.connector.blaise.vo.TranslationInboxEntryVo;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;

public class BlaiseHelper
{
    static private final Logger logger = Logger
            .getLogger(BlaiseHelper.class);

    private static final String DASH = " - ";
    private BlaiseConnector blc = null;

    private static List<String> specialChars = new ArrayList<String>();
    {
    	specialChars.add("\\");
    	specialChars.add("/");
    	specialChars.add(":");
    	specialChars.add("*");
    	specialChars.add("?");
    	specialChars.add("\"");
    	specialChars.add("'");
    	specialChars.add("<");
    	specialChars.add(">");
    	specialChars.add("|");
    }
    
    public BlaiseHelper (BlaiseConnector blc)
    {
    	this.blc = blc;
    }

    public String doTest()
    {
    	try
    	{
			getTranslationClient();
		}
    	catch(UnknownHostException e)
    	{
    		logger.warn("Incorrect URL: " + blc.getUrl());
    		return "Incorrect URL!";
    	}
    	catch (SecurityException e)
    	{
    		logger.warn("Incorrect username or password: " + blc.getUsername() + "/" + blc.getPassword());
    		return "Incorrect username or password!";
    	}
    	catch (IncompatibleVersionException e)
    	{
    		logger.warn("Incorrect client core version: " + blc.getClientCoreVersion());
    		return e.getMessage().replace("'", "").replace("\"", "");
    	}
    	catch (Exception e)
    	{
    		String msg = e.getMessage();
    		logger.warn("Blaise connector test failure: " + e.getMessage());
    		if (msg.indexOf("Unexpected response status") > -1 || msg.indexOf("503") > -1)
    		{
    			msg = msg.replace("'", "").replace("\"", "");
    			return "Connection failed: " + msg;
    		}
    		return "Connection failed!";
		}

    	return "";
    }

    public List<TranslationInboxEntryVo> listInbox()
    {
    	return listInbox(null);
    }

    public List<TranslationInboxEntryVo> listInbox(State state)
	{
		List<TranslationInboxEntryVo> results = new ArrayList<TranslationInboxEntryVo>();
		try
		{
			TranslationAgencyClient client = getTranslationClient();
			List<InboxEntry> inboxEntries = null;
			if (state == null)
			{
				inboxEntries = client.listInbox();
			}
			else
			{
				inboxEntries = client.listInbox(state);
			}

			for (InboxEntry entry : inboxEntries)
			{
				TranslationInboxEntryVo vo = new TranslationInboxEntryVo(
						(TranslationInboxEntry) entry);
				results.add(vo);
			}
		}
		catch (Exception e)
		{
			logger.warn("Error when invoke listInbox: " + e.getMessage());
			if (logger.isDebugEnabled())
			{
				logger.error(e);
			}
		}

		return results;
	}

    /**
	 * Claim Blaise inbox entries. If it has been claimed, Blaise API will throw
	 * exception/warning.
	 * 
	 * As ONLY the first identifier is returned, we claim entry one by one.
	 * 
	 * @param ids
	 */
    public void claim(long id)
    {
    	try
		{
			TranslationAgencyClient client = getTranslationClient();
    		Set<Long> ids = new HashSet<Long>();
    		ids.add(id);
			client.claim(ids);
		}
		catch (Exception e)
		{
			if (e.getMessage().toLowerCase().contains("task already claimed"))
			{
				logger.warn("Warning when claim entry(" + id + "): "
						+ e.getMessage());
			}
			else
			{
				logger.error(e);
			}
		}
    }

    /**
     * Download XLIFF file. Note that current entry must be claimed already.
     * 
     * @param entry -- TranslationInboxEntryVo
     * @param storeFile -- storeFile
     */
	public void downloadXliff(TranslationInboxEntryVo entry, File storeFile)
	{
		GZIPInputStream is = null;
		OutputStream os = null;
    	try
		{
			TranslationAgencyClient client = getTranslationClient();
			is = (GZIPInputStream) client.downloadXliff(entry.getId());
	        os = new FileOutputStream(storeFile);

	        int count;
	        byte data[] = new byte[1024];
	        while ((count = is.read(data, 0, 1024)) != -1)
	        {
	            os.write(data, 0, count);
	        }

	        is.close();
	        os.close();
		}
		catch (Exception e)
		{
			logger.error("Failed to downloadXliff: " + entry.getId(), e);
		}
	}

	public void uploadXliff(long entryId, File file)
	{
		try
		{
			InputStream is = new FileInputStream(file);
			TranslationAgencyClient client = getTranslationClient();
			client.uploadXliff(entryId, is);
		}
		catch (Exception e)
		{
			logger.warn("Error when invoke uploadXliff: " + e.getMessage());
			if (logger.isDebugEnabled())
			{
				logger.error(e);
			}
		}
	}

	public void complete(Set<Long> ids)
    {
    	for (Long id : ids)
    	{
    		complete(id);
    	}
    }

    public void complete(long id)
    {
		try
		{
			TranslationAgencyClient client = getTranslationClient();
			client.complete(id);
		}
		catch (Exception e)
		{
			logger.error("Failed to complete entry: " + id, e);
		}
    }

    // is this a bit heavy operation?
    private TranslationAgencyClient getTranslationClient()
			throws SecurityException, IncompatibleVersionException,
			MalformedURLException, IOException, URISyntaxException
	{
    	if (blc.getClientCoreVersion() != null)
    	{
    		ClientFactory.setClientCoreVersion(blc.getClientCoreVersion());
    	}
		ClientFactory.setClientCoreRevision(blc.getClientCoreRevision());

		if (blc.getWorkflowId() == null)
		{
			return ClientFactory.createClient(new URL(blc.getUrl()),
					blc.getUsername(), blc.getPassword());
		}
		else
		{
			return ClientFactory.createClient(new URL(blc.getUrl()),
					blc.getUsername(), blc.getPassword(), blc.getWorkflowId());
		}
	}

	/**
	 * Blaise inbox entry file name is like
	 * "Blaise inbox entry - Alerts - 40768 - 2 - es_MX.xlf".
	 * 
	 * @param entry
	 *            - TranslationInboxEntry boject
	 * @return the file name which is used to create job in GlobalSight.
	 */
	public static String getEntryFileName(TranslationInboxEntryVo entry)
	{
		StringBuilder fileName = new StringBuilder();
		String des = entry.getDescription();
		if (des == null || des.trim().length() == 0)
		{
			des = "No Description";
		}
		else
		{
			des = des.trim();
			if (des.length() > 55)
			{
				des = des.substring(0, 50);
			}
		}

		fileName.append("Blaise Entry ")
				.append(entry.getId())
				.append(DASH)
				.append(des).append(DASH)
				.append(entry.getRelatedObjectId()).append(DASH)
				.append(entry.getSourceRevision()).append(DASH)
				.append(entry.getEntry().getTargetLocale().getLocaleCode())
				.append(".xlf").toString();
		String fileNameStr = fileName.toString();

		return handleFileName(fileNameStr);
	}

	public static String getEntryJobName(TranslationInboxEntryVo entry)
	{
		String fileName = getEntryFileName(entry);
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		return fileName + "_XLF";
	}

	private static String handleFileName(String fileName)
	{
		for (String specialChar : specialChars)
		{
			fileName = fileName.replace(specialChar, " ");
		}

		// Replace all continuous space to single space
		while (fileName.indexOf("  ") > -1)
		{
			fileName = fileName.replace("  ", " ");
		}
		return fileName;
	}
}
