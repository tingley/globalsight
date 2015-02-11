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

package com.globalsight.everest.snippet;

// globalsight
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.everest.persistence.PersistentObject;

/**
 * The implementation of the Snippet interface. Snippet names are normalized to
 * uppercase.
 * 
 * @see Snippet class
 */
public class SnippetImpl extends PersistentObject implements Snippet
{
	// public constants USED BY TOPLINK FOR QUERIES
	static final public String LOCALE = "m_locale";

	// Max column with of SNIPPET.NAME column in database.
	static final public int MAX_NAME_LEN = 100;

	// Max column with of SNIPPET.DESCRIPTION column in database.
	static final public int MAX_DESC_LEN = 4000;

	// m_id and m_name are inherited from persistent object.

	private String m_description;

	private GlobalSightLocale m_locale;

	// Only one of these will be filled in it depends if it is bigger
	// than 4000 or not. If under 4000 using a string speeds up
	// performance. These get mapped to database fields.
	private String m_contentString = null;

	private String m_contentClob = null;

	/**
	 * Default constructor used by TOPLink when retrieving snippets from the
	 * database.
	 */
	public SnippetImpl()
	{
		super();
	}

	/**
	 * Constructs a Snippet.
	 * 
	 * @param p_name:
	 *            Name of the snippet.
	 * @param p_description:
	 *            Description of the snippet or NULL if no description.
	 * @param p_locale:
	 *            The locale the snippet is associated with or NULL if a generic
	 *            snippet that can be used in any locale.
	 * @param p_content:
	 *            The content of the snippet.
	 */
	public SnippetImpl(String p_name, String p_description,
			GlobalSightLocale p_locale, String p_content)
	{
		super();

		setName(p_name);
		setDescription(p_description);
		m_locale = p_locale;
		setContent(p_content);
	}

	/**
	 * Constructs a Snippet. This constructor should NOT be used for adding a
	 * new snippet - only for modifying an existing one with an id.
	 * 
	 * @param p_id:
	 *            The id of the snippet. It should already exist.
	 * @param p_name:
	 *            Name of the snippet.
	 * @param p_description:
	 *            Description of the snippet or NULL if no description.
	 * @param p_locale:
	 *            The locale the snippet is associated with or NULL if a generic
	 *            snippet that can be used in any locale.
	 * @param p_content:
	 *            The content of the snippet.
	 */
	public SnippetImpl(long p_id, String p_name, String p_description,
			GlobalSightLocale p_locale, String p_content)
	{
		super();

		setId(p_id);
		setName(p_name);
		setDescription(p_description);
		m_locale = p_locale;
		setContent(p_content);
	}

	/**
	 * Create a copy of the Snippet by copying all the pieces of the snippet
	 * except for its id. The id must be reset, in order to be set to a unique
	 * number.
	 * 
	 * @param p_snippet
	 *            The snippet to make a copy from.
	 */
	public SnippetImpl(Snippet p_snippet)
	{
		super();

		setName(p_snippet.getName());
		setDescription(p_snippet.getDescription());
		m_locale = p_snippet.getLocale();
		setContent(p_snippet.getContent());
	}

	/**
	 * @see Snippet.getDescription()
	 */
	public String getDescription()
	{
		// TOPLink has direct access to the "m_description"
		// to set it to NULL, so need to return an empty string
		// and not null
		if (m_description == null)
		{
			setDescription("");
		}

		return m_description;
	}

	/**
	 * Sets name - truncates and uppercases the name.
	 */
	public void setName(String p_name)
	{
		String name = truncateString(p_name, MAX_NAME_LEN).toUpperCase();
		super.setName(name);
	}

	/**
	 * @see Snippet.setDescription(String)
	 */
	public void setDescription(String p_description)
	{
		if (p_description == null)
		{
			m_description = "";
		}
		else
		{
			m_description = truncateString(p_description, MAX_DESC_LEN);
		}
	}

	/**
	 * @see Snippet.getLocale()
	 */
	public GlobalSightLocale getLocale()
	{
		return m_locale;
	}

	/**
	 * @see Snippet.getContent()
	 */
	public String getContent()
	{
		String s = m_contentString;

		if (s == null)
		{
			s = m_contentClob;
		}

		return s == null ? "" : s;
	}

	/**
	 * @see Snippet.setContent(String)
	 */
	public void setContent(String p_content)
	{
		m_contentClob = null;
		m_contentString = null;

		if (p_content != null)
		{
			if (EditUtil.getUTF8Len(p_content) > CLOB_THRESHOLD)
			{
				m_contentClob = p_content;
			}
			else
			{
				m_contentString = p_content;
			}
		}
	}

	/**
	 * @see Snippet.isGeneric()
	 */
	public boolean isGeneric()
	{
		if (m_locale == null)
		{
			return true;
		}

		return false;
	}

	/**
	 * Override the toString method
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("Snippet: ");

		sb.append("m_id=");
		sb.append(getId());
		sb.append(", m_name=");
		sb.append(getName());
		sb.append(", m_description=");
		sb.append(getDescription());
		sb.append(", m_locale=");
		sb.append(m_locale == null ? "null" : m_locale.getDisplayName());

		return sb.toString();
	}

	/**
	 * Snippets have a logical key. If the snippet is generic (no locale), the
	 * key is the name. If the snippet is specific, both the locale and the ID
	 * must be the same.
	 * 
	 * Note: anonymous snippets are not equal.
	 */
	public boolean equals(Object p_other)
	{
		if (p_other instanceof SnippetImpl)
		{
			SnippetImpl other = (SnippetImpl) p_other;

			if (isGeneric() && other.isGeneric())
			{
				String name1 = getName();
				String name2 = other.getName();

				if (name1 != null && name2 != null)
				{
					return name1.equals(name2);
				}
			}
			else
			{
				GlobalSightLocale loc1 = getLocale();
				GlobalSightLocale loc2 = other.getLocale();

				if (loc1 != null && loc2 != null)
				{
					return loc1.equals(loc2) && getId() == other.getId();
				}
			}
		}

		return false;
	}

	public int hashCode()
	{
		if (isGeneric())
		{
			String name = getName();

			if (name != null)
			{
				return name.hashCode();
			}
			else
			{
				return super.hashCode();
			}
		}

		return (getName() + getLocale() + getId()).hashCode();
	}

	//
	// TOPLINK SUPPORT METHODS
	//

	/**
	 * Return the contents of the attribute that is mapped to the segment clob
	 * field.
	 */
	public String getContentClob()
	{
		return m_contentClob;
	}

	public void setLocale(GlobalSightLocale m_locale)
	{
		this.m_locale = m_locale;
	}

	public String getContentString()
	{
		return m_contentString;
	}

	public void setContentString(String string)
	{
		m_contentString = string;
	}

	public void setContentClob(String clob)
	{
		m_contentClob = clob;
	}
}
