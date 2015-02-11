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
package com.globalsight.everest.edit.online.imagereplace;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * <P>
 * This is an implementation for file mapping for image replacement. It maps the
 * real image source file name to temporary image file name.
 * </P>
 * 
 * <P>
 * The temporary file is used during the localization process. When export is
 * done the temporary file name is replaced by the real file name defined in
 * this map. The transaction unit variant ID is used as the key for this
 * mapping. This ID is unique across the system and locales.
 * </P>
 * 
 * <P>
 * Here is an example of how this class is used:
 * </P>
 * 
 * <PRE>
 * {
 * 	ImageReplaceFileMap irfm = new ImageReplaceFileMap(target_id, tuv_id, s_id,
 * 			&quot;/en_US/gs.gif&quot;, &quot;temp/gs.gif&quot;);
 * }
 * </PRE>
 * 
 * <P>
 * An instance of this class is persisted to table IMAGE_REPLACE_FILE_MAP in
 * database.
 * </P>
 * 
 */
public class ImageReplaceFileMap extends PersistentObject
{
	private static final long serialVersionUID = -6919668935543976145L;

	//
	// PUBLIC CONSTANTS FOR USE BY TOPLINK
	//
	public static final String M_TARGET_PAGE_ID = "m_targetPageId";
	public static final String M_TUV_ID = "m_tuvId";
	public static final String M_SUB_ID = "m_subId";
	public static final String M_STATE = "m_state";
	public static final String M_TEMP_SOURCE_NAME = "m_tempSourceName";
	public static final String M_REAL_SOURCE_NAME = "m_realSourceName";

	//
	// Image export states
	//
	public static final String LOCALIZED = "LOCALIZED";
	public static final String EXPORTED = "EXPORTED";
	public static final String EXPORT_IN_PROGRESS = "EXPORT_IN_PROGRESS";
	public static final String EXPORT_FAILED = "EXPORT_FAILED";
	public static final String EXPORT_CANCELLED = "EXPORT_CANCELLED";

	//
	// PRIVATE MEMBER VARIABLES
	//
	private Long m_targetPageId; // Foreign key - TARGET_PAGE_ID
	private Long m_tuvId; // Foreign key - TUV_ID
	private Long subId; // sub id
	private String m_state; // Image Export state
	private String m_tempSourceName; // Temporary image file name
	private String m_realSourceName; // Real image file name

	/**
	 * Default constructor to be used by TopLink only. This is here solely
	 * because the persistence mechanism that persists instances of this class
	 * is using TopLink, and TopLink requires a public default constructor for
	 * all the classes that it handles persistence for.
	 */
	public ImageReplaceFileMap()
	{
		super();

		m_targetPageId = null;
		m_tuvId = null;
		subId = null;
		m_state = LOCALIZED;
		m_tempSourceName = null;
		m_realSourceName = null;
	}

	/**
	 * This constructor creates an entity object for mapping temporary image
	 * file name (one uploaded) and what would be the target image source
	 * location.
	 * 
	 * @param p_targetPageId
	 *            Target Page ID
	 * @param p_tuvId
	 *            Translation Variant Unit ID
	 * @param p_subId
	 *            sub ID
	 * @param p_realSourceName
	 *            real image source
	 * @param p_tempSourceName
	 *            temporary image source
	 */
	public ImageReplaceFileMap(long p_targetPageId, long p_tuvId, long p_subId,
			String p_realSourceName, String p_tempSourceName)
	{
		super();

		setTargetPageId(p_targetPageId);
		setTuvId(p_tuvId);
		setSubId(p_subId);
		m_state = LOCALIZED;
		m_realSourceName = p_realSourceName;
		m_tempSourceName = p_tempSourceName;
	}

	public ImageReplaceFileMap(Long p_targetPageId, long p_tuvId, long p_subId,
			String p_realSourceName, String p_tempSourceName)
	{
		super();

		setTargetPageId(p_targetPageId);
		setTuvId(p_tuvId);
		setSubId(p_subId);
		m_state = LOCALIZED;
		m_realSourceName = p_realSourceName;
		m_tempSourceName = p_tempSourceName;
	}

	/**
	 * Get the temporary image source file name.
	 * <p>
	 * 
	 * @return String The temporary image source file name.
	 */
	public String getTempSourceName()
	{
		return m_tempSourceName;
	}

	/**
	 * Set the temporary image source file name.
	 * <p>
	 * 
	 * @param p_tempSourceName
	 *            The temporary image source file name.
	 */
	public void setTempSourceName(String p_tempSourceName)
	{
		m_tempSourceName = p_tempSourceName;
	}

	/**
	 * Get the image source file name. This is the file path of uploaded file.
	 * <p>
	 * 
	 * @return String The image source file name.
	 */
	public String getFilename()
	{
		return getTempSourceName();
	}

	/**
	 * Set the uploaded image source file path.
	 * <p>
	 * 
	 * @param p_filename
	 *            The image source file name.
	 */
	public void setFilename(String p_filename)
	{
		setTempSourceName(p_filename);
	}

	/**
	 * Get the real image source file name.
	 * <p>
	 * 
	 * @return String The real image source file name.
	 */
	public String getRealSourceName()
	{
		return m_realSourceName;
	}

	/**
	 * Set the real image source file name.
	 * <p>
	 * 
	 * @param p_realSourceName
	 *            The real image source file name.
	 */
	public void setRealSourceName(String p_realSourceName)
	{
		m_realSourceName = p_realSourceName;
	}

	/**
	 * Get the translation unit variant (TUV id) for this mapping.
	 * 
	 * @return long The translation unit variant (TUV id) for this mapping.
	 */
	public long getTuvId()
	{
		if (m_tuvId == null)
		{
			return 0;
		}
		else
		{
			return m_tuvId.longValue();
		}
	}

	/**
	 * Get the translation unit variant (TUV id) for this mapping.
	 * 
	 * @return Long The translation unit variant (TUV id) for this mapping.
	 */
	public Long getTuvIdAsLong()
	{
		if (m_tuvId == null)
		{
			return new Long(0);
		}
		else
		{
			return m_tuvId;
		}
	}

	/**
	 * Set the translation unit variant ID for this mapping
	 * 
	 * @param p_tuvId
	 *            translation unit variant ID for this mapping
	 */
	public void setTuvId(long p_tuvId)
	{
		if (p_tuvId == 0)
		{
			m_tuvId = null;
		}
		else
		{
			m_tuvId = new Long(p_tuvId);
		}
	}

	/**
	 * Get the target page id for this mapping.
	 * 
	 * @return long The target page id for this mapping.
	 */
	public long getTargetPageId()
	{
		if (m_targetPageId == null)
		{
			return 0;
		}
		else
		{
			return m_targetPageId.longValue();
		}
	}

	/**
	 * Get the target page id for this mapping.
	 * 
	 * @return Long The target page id for this mapping.
	 */
	public Long getTargetPageIdAsLong()
	{
		if (m_targetPageId == null)
		{
			return new Long(0);
		}
		else
		{
			return m_targetPageId;
		}
	}

	/**
	 * Set the target page ID for this mapping
	 * 
	 * @param p_targetPageId
	 *            target page ID for this mapping
	 */
	public void setTargetPageId(long p_targetPageId)
	{
		if (p_targetPageId == 0)
		{
			m_targetPageId = null;
		}
		else
		{
			m_targetPageId = new Long(p_targetPageId);
		}
	}

	/**
	 * Set the target page ID for this mapping
	 * 
	 * @param p_targetPageId
	 *            target page ID for this mapping
	 */
	public void setTargetPageId(Long p_targetPageId)
	{
		m_targetPageId = p_targetPageId;
	}

	/**
	 * Get the sub id for this mapping.
	 * 
	 * @return long The sub id for this mapping.
	 */
	public long getSubId()
	{
		if (subId == null)
		{
			return 0;
		}
		else
		{
			return subId.longValue();
		}
	}

	/**
	 * Get the sub id for this mapping.
	 * 
	 * @return Long The sub id for this mapping.
	 */
	public Long getSubIdAsLong()
	{
		if (subId == null)
		{
			return new Long(0);
		}
		else
		{
			return subId;
		}
	}

	/**
	 * Set the sub ID for this mapping
	 * 
	 * @param p_subId
	 *            sub ID for this mapping
	 */
	public void setSubId(long p_subId)
	{
		subId = new Long(p_subId);
	}

	/**
	 * Get the exported state of this mapped image
	 * 
	 * @return exported state of this mapped image
	 */
	public String getState()
	{
		return m_state;
	}

	/**
	 * Set the exported state of this mapped image
	 * 
	 * @param p_state
	 *            new state of mapped image
	 */
	public void setState(String p_state)
	{
		m_state = p_state;
	}

	/**
	 * Return string representation of object
	 * 
	 * @return string representation of object
	 */
	public String toString()
	{
		return super.toString() + " m_targetPageId="
				+ (m_targetPageId == null ? "0" : m_targetPageId.toString())
				+ " m_tuvId=" + (m_tuvId == null ? "0" : m_tuvId.toString())
				+ " m_subId=" + (subId == null ? "0" : subId.toString())
				+ " m_state=" + (m_state == null ? "null" : m_state)
				+ " m_tempSourceName="
				+ (m_tempSourceName == null ? "null" : m_tempSourceName)
				+ " m_realSourceName="
				+ (m_realSourceName == null ? "null" : m_realSourceName);
	}
}
