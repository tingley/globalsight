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
package com.globalsight.terminology.searchreplace;

import com.globalsight.terminology.searchreplace.SearchReplaceConstants;

import java.util.ArrayList;

public class SearchReplaceParams
	implements SearchReplaceConstants
{
    private String m_searchText;
    private String m_searchType;
    private String m_replaceText;
    private boolean m_caseInsensitive;
    private boolean m_wholeword;
    private boolean m_smartReplace;
    private String m_level;
    private String m_language;
    private String m_field;
    private String m_fieldName;

	private int m_levelCode = -1;
	private int m_fieldCode = -1;

	private ArrayList m_replaceIndexes;
	
    public SearchReplaceParams(String p_searchText, String p_searchType, 
		String p_level, String p_language, String p_field, String p_fieldName,
		String p_caseInsensitive, String p_smartReplace, String p_wholeword)
    {
        m_searchText = p_searchText;
        m_searchType = p_searchType;
        m_caseInsensitive = Boolean.valueOf(p_caseInsensitive).booleanValue();
		m_smartReplace = Boolean.valueOf(p_smartReplace).booleanValue();
        m_level = p_level;
		m_language = p_language;
        m_field = p_field;
        m_fieldName = p_fieldName;
        m_wholeword = Boolean.valueOf(p_wholeword).booleanValue();
    }

    public String getSearchText()
    {
        return m_searchText;
    }
    
    public String getSearchType()
    {
        return m_searchType;
    }

    public String getReplaceText()
    {
        return m_replaceText;
    }

    public void setReplaceText(String p_replace)
    {
        m_replaceText = p_replace;
    }

    public boolean isCaseInsensitive()
    {
        return m_caseInsensitive;
    }
    
    public boolean isWholeWord()
    {
        return m_wholeword;
    }

    public boolean isSmartReplace()
    {
        return m_smartReplace;
    }

    public void setSmartReplace(String p_smartReplace)
    {
        m_smartReplace = Boolean.valueOf(p_smartReplace).booleanValue();
    }

    public String getLevel()
    {
        return m_level;
    }

    public String getLanguage()
    {
        return m_language;
    }

    public String getField()
    {
        return m_field;
    }

    public String getFieldName()
    {
        return m_fieldName;
    }

	public ArrayList getReplaceIndexes()
	{
		if (m_replaceIndexes == null)
		{
			m_replaceIndexes = new ArrayList();
		}

		return m_replaceIndexes;
	}

	public void clearReplaceIndexes()
	{
		if (m_replaceIndexes == null)
		{
			m_replaceIndexes = new ArrayList();
		}

		m_replaceIndexes.clear();
	}

	public void addReplaceIndex(Long p_id)
	{
		if (m_replaceIndexes == null)
		{
			m_replaceIndexes = new ArrayList();
		}

		m_replaceIndexes.add(p_id);
	}

	public void addAllReplaceIndexes(ArrayList p_ids)
	{
		if (m_replaceIndexes == null)
		{
			m_replaceIndexes = new ArrayList();
		}

		m_replaceIndexes.addAll(p_ids);
	}

    public int getLevelCode()
    {
        // The strings are the same as defined in WebappConstants
        // as TERMBASE_LEVEL_ENTRY, TERMBASE_LEVEL_CONCEPT etc.

		if (m_levelCode == -1)
		{
			if (m_level.equals("levelentry"))
			{
				m_levelCode = LEVEL_ENTRY;
			}
			else if (m_level.equals("levelconcept"))
			{
				m_levelCode = LEVEL_CONCEPT;
			}
			else if (m_level.equals("levellanguage"))
			{
				m_levelCode = LEVEL_LANGUAGE;
			}
			else if (m_level.equals("levelterm"))
			{
				m_levelCode = LEVEL_TERM;
			}
		}

		return m_levelCode;
    }

    public int getFieldCode()
    {
        // The strings should be defined in WebappConstants but are
        // really defined in the JSP layer
        // (terminology/maintenance/main.jsp)

		if (m_fieldCode == -1)
		{
			if (m_field.equals("text") || m_field.equals("attr"))
			{
				m_fieldCode = FIELD_SPECIFICFIELD;
			}
			else if (m_field.equals("alltexts"))
			{
				m_fieldCode = FIELD_ALLTEXT;
			}
			else if (m_field.equals("allattrs"))
			{
				m_fieldCode = FIELD_ALLATTR;
			}
			else if (m_field.equals("allsources"))
			{
				m_fieldCode = FIELD_ALLSOURCES;
			}
			else if (m_field.equals("allnotes"))
			{
				m_fieldCode = FIELD_ALLNOTES;
			}
			else if (m_field.equals("all"))
			{
				m_fieldCode = FIELD_ALL;
			}
		}

		return m_fieldCode;
    }

	public String toString()
	{
		StringBuffer result = new StringBuffer();

		result.append("<searchparams>");

		result.append("<searchtext>");
		result.append(m_searchText);
		result.append("</searchtext>");
		result.append("<replacetext>");
		result.append(m_replaceText);
		result.append("</replacetext>");
		result.append("<caseinsensitive>");
		result.append(m_caseInsensitive);
		result.append("</caseinsensitive>");
		result.append("<smartreplace>");
		result.append(m_smartReplace);
		result.append("</smartreplace>");
		result.append("<level>");
		result.append(m_level);
		result.append("</level>");
		result.append("<language>");
		result.append(m_language);
		result.append("</language>");
		result.append("<field>");
		result.append(m_field);
		result.append("</field>");
		result.append("<fieldName>");
		result.append(m_fieldName);
		result.append("</fieldName>");

		result.append("</searchparams>");

		return result.toString();
	}
}
