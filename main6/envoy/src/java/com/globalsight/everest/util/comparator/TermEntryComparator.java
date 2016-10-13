/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.globalsight.everest.util.comparator;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import com.globalsight.everest.util.comparator.StringComparator;

/**
* This class can be used to compare term data <HashMap> types
*/
public class TermEntryComparator extends StringComparator
{
    private static final long serialVersionUID = 3364617308581693917L;

    //types of term comparison
	public static final int TBNAME_ASC = 1;
	public static final int TBNAME_DESC = 2;
    public static final int SRC_LANG_ASC = 3;
    public static final int SRC_LANG_DESC = 4;
    public static final int SRC_TERM_ASC = 5;
    public static final int SRC_TERM_DESC = 6;
    public static final int TARGET_LANG_ASC = 7;
    public static final int TARGET_LANG_DESC = 8;
    public static final int TARGET_TERM_ASC = 9;
    public static final int TARGET_TERM_DESC = 10;
    
    ResourceBundle m_bundle = null;

	/**
	* Creates a UserComparator with the given locale.
	*/
	public TermEntryComparator(Locale p_locale, ResourceBundle p_bundle)
	{
	    super(p_locale);
        m_bundle = p_bundle;
	}
	
	public TermEntryComparator( int p_type, Locale p_locale )
	{
	    super(p_type, p_locale);
	}

	/**
	* Performs a comparison of two term objects <HashMap type>.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B)
        {
            HashMap a = (HashMap) p_A;
            HashMap b = (HashMap) p_B;

            String aValue = "";
            String bValue = "";
            int rv;
            switch (m_type)
            {
            case TBNAME_ASC:
                aValue = (String) a.get("tbname");
                bValue = (String) b.get("tbname");
                rv = this.compareStrings(aValue,bValue);
                break;
            case TBNAME_DESC:
                aValue = (String) a.get("tbname");
                bValue = (String) b.get("tbname");
                rv = - (this.compareStrings(aValue,bValue));
                break;
            case SRC_LANG_ASC:
                aValue = (String) a.get("src_lang");
                bValue = (String) b.get("src_lang");
                rv = this.compareStrings(aValue,bValue);
                break;
            case SRC_LANG_DESC:
                aValue = (String) a.get("src_lang");
                bValue = (String) b.get("src_lang");
                rv = - (this.compareStrings(aValue,bValue));
                break;
            case SRC_TERM_ASC:
                aValue = (String) a.get("src_term");
                bValue = (String) b.get("src_term");
                rv = this.compareStrings(aValue,bValue);
                break;
            case SRC_TERM_DESC:
                aValue = (String) a.get("src_term");
                bValue = (String) b.get("src_term");
                rv = -(this.compareStrings(aValue,bValue));
                break;
            case TARGET_LANG_ASC:
                aValue = (String) a.get("target_lang");
                bValue = (String) b.get("target_lang");
                rv = this.compareStrings(aValue,bValue);
                break;
            case TARGET_LANG_DESC:
                aValue = (String) a.get("target_lang");
                bValue = (String) b.get("target_lang");
                rv = -(this.compareStrings(aValue,bValue));
                break;
            case TARGET_TERM_ASC:
                aValue = (String) a.get("target_term");
                bValue = (String) b.get("target_term");
                rv = this.compareStrings(aValue,bValue);
                break;
            case TARGET_TERM_DESC:
                aValue = (String) a.get("target_term");
                bValue = (String) b.get("target_term");
                rv = -(this.compareStrings(aValue,bValue));
                break;
            default:
                aValue = (String) a.get("src_lang");
                bValue = (String) b.get("src_lang");
                rv = this.compareStrings(aValue,bValue);
                break;
            }

            return rv;
        }
}
