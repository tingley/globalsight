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

package com.globalsight.util.progress;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * ProcessStatus is a generic object for tracking progress of a time consuming
 * background process.
 * 
 * The object holds a number of expected entries, completion percentage, error
 * and warning messages, and a result object.
 * 
 * This object also allows to interrupt the background process, i.e. cancel the
 * operation.
 * 
 * Message retrieval works in two ways: 1) progress bars that refresh the entire
 * window: --> these need to retrieve the full list of messages: getMessage() 2)
 * progress bars that show only updates: --> these need to retrieve the current
 * messages and clear the list.
 */
public class ProcessStatus implements IProcessStatusListener, Serializable
{
	// Total Number of records processed.
	protected int m_counter;

	// Percentage complete (0-100)
	protected int m_percentage;

	// Error or information messages
	protected ArrayList m_messages = new ArrayList();

	// Interrupt flag
	protected boolean m_interruped = false;

	// Generic result object.
	protected Object m_results;
	
	protected ResourceBundle m_bundle = null;
	
	protected boolean isMultiTasks = false;
	
	protected int taskCounter = 0;

    protected int taskTotalSize = 0;
    
    protected boolean errorOccured = false;

	//
	// Constructor
	//
    /** Initializes the ProcessStatus object. */
	public ProcessStatus()
	{
		m_counter = 0;
		m_percentage = 0;
	}

	//
	// Public Methods
	//

	/**
	 * Method for setting the counter value which represents the overall number
	 * of objects (entries, files, etc...) to be processed.
	 */
	public synchronized void setCounter(int p_counter)
	{
		m_counter = p_counter;
	}

	/** Method for getting counter value. */
	public int getCounter()
	{
		return m_counter;
	}

	/** Method for setting percentage complete information. */
	public synchronized void setPercentage(int p_percentage)
	{
		m_percentage = p_percentage;
	}

	/** Method for getting percentage complete information. */
	public int getPercentage()
	{
		return m_percentage;
	}

	/** Method for adding an informational or error message. */
	synchronized public void addMessage(String p_message)
	{
		if (p_message != null && p_message.length() > 0)
		{
			m_messages.add(p_message);
		}
	}

	/**
	 * Method for getting info/error messages.
	 */
	// public ArrayList getMessages()
	// {
	// return m_messages;
	// }
	/** Method for getting info/error messages incrementally. */
	synchronized public ArrayList giveMessages()
	{
		if (m_messages.size() == 0)
		{
			return null;
		}

		ArrayList result = new ArrayList(m_messages);

		m_messages.clear();

		return result;
	}

	/**
	 * Method for getting info/error messages as a single string.
	 * 
	 * @return the concatenated messages if there are any, or else the empty
	 *         string.
	 */
	synchronized public String getMessage()
	{
		String result;

		if (m_messages != null)
		{
			result = m_messages.toString();
		}
		else
		{
			result = "";
		}

		return result;
	}
	
	/**
	 * Method for setting the resource bundle
	 * 
	 * @return
	 */
	public void setResourceBundle(ResourceBundle p_bundle)
    {
        m_bundle = p_bundle;
    }
	
	/**
	 * Method for getting the resource bundle
	 */
	public ResourceBundle getResourceBundle()
    {
        return m_bundle;
    }
	
	/**
	 * Get string from bundle, if bundle or p_key is null, return the defaultMsg
	 * @param p_key
	 * @param p_defaultMsg
	 * @return the string from bundle, or the p_defaultMsg if p_key or {@link #getResourceBundle()} is null
	 */
	public String getStringFromBundle(String p_key, String p_defaultMsg)
	{
	    if (m_bundle == null || p_key == null)
	    {
	        return p_defaultMsg;
	    }
	    
        return (m_bundle.containsKey(p_key)) ? m_bundle.getString(p_key) : p_defaultMsg;
	}
	
	/**
	 * Get the string from resource bundle if the p_status is ProcessStatus. See 
	 * {@link #getStringFromBundle(String, String)}
	 * @param p_status
	 * @param p_key
	 * @param p_defaultMsg
	 * @return
	 */
    public static String getStringFromResBundle(IProcessStatusListener p_status, String p_key,
            String p_defaultMsg)
    {
        if (p_status instanceof ProcessStatus)
        {
            return ((ProcessStatus) p_status).getStringFromBundle(p_key, p_defaultMsg);
        }

        return p_defaultMsg;
    }

    /**
     * Get the string from resource bundle if the p_status is ProcessStatus and format it with java.text.MessageFormat.
     * @see {@link #getStringFromResBundle(IProcessStatusListener, String, String, String)}
     * @param p_status
     * @param p_key
     * @param p_defaultPattern
     * @param arguments
     * @return
     */
    public static String getStringFormattedFromResBundle(IProcessStatusListener p_status,
            String p_key, String p_defaultPattern, Object... arguments)
    {
        String pattern = p_defaultPattern;
        if (p_status instanceof ProcessStatus)
        {
            pattern = ((ProcessStatus) p_status).getStringFromBundle(p_key, p_defaultPattern);
        }

        String result = java.text.MessageFormat.format(pattern, arguments);
        return result;
    }

	/**
	 * Method for getting info/error messages as an HTML string with messages
	 * separated by &lt;BR&gt;.
	 * 
	 * @return the concatenated messages if there are any, or else the empty
	 *         string.
	 */
	synchronized public String getMessageHtml()
	{
		StringBuffer result = new StringBuffer();

		if (m_messages != null && m_messages.size() > 0)
		{
			for (int i = 0, max = m_messages.size(); i < max; i++)
			{
				String msg = (String) m_messages.get(i);

				result.append(msg);

				if (i < max - 1)
				{
					result.append("<BR>");
				}
			}
		}

		return result.toString();
	}

	/** Set the results. */
	public void setResults(Object p_results)
	{
		m_results = p_results;
	}

	/** Get the results. */
	public Object getResults()
	{
		return m_results;
	}

	/**
	 * Interrupt the background process by setting this flag to true.
	 */
	public void interrupt()
	{
		m_interruped = true;
	}

	/**
	 * For the background process: update a progress update with a new
	 * "percentage complete" and an optional status message.
	 * 
	 * @param p_message
	 *            a message string or null for no message.
	 * 
	 * @throws IOException
	 *             when the background process should be interrupted.
	 */
	public void listen(int p_counter, int p_percentage, String p_message)
			throws IOException
	{
		m_counter = p_counter;
		m_percentage = p_percentage;

		addMessage(p_message);

		if (m_interruped == true)
		{
			// stop backend processing.
            throw new ClientInterruptException();
		}
	}

	/**
	 * For the background process: update a progress update with a new
	 * "percentage complete" and an optional plain text error message which is
	 * formatted internally by this method. The resulting HTML text is then
	 * added to the buffer. Note: innerHTML should be used to display these
	 * messages (usually in a jsp page).
	 * 
	 * @param p_msgCaption
	 *            optional caption that will appear above the message. For
	 *            example: "Error" or "warning".
	 * @param p_message
	 *            a error message string or null for no message.
	 * @param p_style
	 *            optional complete style attribute, example: STYLE='color:
	 *            red;'
	 * @throws IOException
	 *             when the background process should be interrupted.
	 */
	public void listenHTML(int p_counter, int p_percentage, String p_message,
			String p_msgCaption, String p_style) throws IOException
	{
		StringBuffer sb = new StringBuffer("<P ");

		if (p_style != null)
		{
			sb.append(p_style);
		}

		sb.append(">");

		if (p_msgCaption != null && p_msgCaption.length() > 0)
		{
			sb.append(p_msgCaption);
			sb.append("<BR><BR>");
		}

		if (p_message != null)
		{
			sb.append(p_message);
		}

		sb.append("</P>");

		listen(p_counter, p_percentage, sb.toString());

	}

	public boolean isMultiTasks()
    {
        return isMultiTasks;
    }

    public void setMultiTasks(boolean isMultiTasks)
    {
        this.isMultiTasks = isMultiTasks;
    }

    public int getTaskCounter()
    {
        return taskCounter;
    }

    public void setTaskCounter(int taskCounter)
    {
        this.taskCounter = taskCounter;
    }

    public int getTaskTotalSize()
    {
        return taskTotalSize;
    }

    public void setTaskTotalSize(int taskTotalSize)
    {
        this.taskTotalSize = taskTotalSize;
    }
    
    public int getTaskPercentage()
    {
        int curTaskCount = this.getTaskCounter();
        int tasksSize = this.getTaskTotalSize();
        return (int)(((curTaskCount * 1.0)/(tasksSize * 1.0)) * 100.0);
    }
    
    public boolean isErrorOccured()
    {
        return errorOccured;
    }

    public void setErrorOccured(boolean errorOccured)
    {
        this.errorOccured = errorOccured;
    }
}
