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
package com.globalsight.everest.util.comparator;    

import java.util.Locale;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;

/**
* This class can be used to compare TMProfile objects
*/
public class MTProfileComparator extends StringComparator
{
    private static final long serialVersionUID = -5449390968155714127L;

    //types of TMProfile comparison
	public static final int NAME = 0;
	public static final int DESCRIPTION = 1;
	public static final int ASC_COMPANY = 2;
    public static final int MT_ENGINE = 3;
    public static final int MT_CONFIDENCE_SCORE = 4;
    public static final int MT_ACTIVE = 5;
    public static final int SHOW_IN_EDITOR = 6;

	/**
	* Creates a TMProfileComparator with the given type and locale.
	* If the type is not a valid type, then the default comparison
	* is done by displayName
	*/
	public MTProfileComparator(int p_type, Locale p_locale)
	{
            super(p_type, p_locale);
	}

	public MTProfileComparator(Locale p_locale)
	{
            super(p_locale);
	}

	/**
	* Performs a comparison of two TMProfileInfo objects.
	*/
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        MachineTranslationProfile a = (MachineTranslationProfile) p_A;
        MachineTranslationProfile b = (MachineTranslationProfile) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
            case NAME:
                aValue = a.getMtProfileName();
                bValue = b.getMtProfileName();
                rv = this.compareStrings(aValue, bValue);
                break;
            case DESCRIPTION:
                aValue = a.getDescription();
                bValue = b.getDescription();
                rv = this.compareStrings(aValue, bValue);
                break;
            case ASC_COMPANY:
                aValue = null;
                bValue = null;
                try
                {
                    aValue = CompanyWrapper
                            .getCompanyNameById(a.getCompanyid());
                    bValue = CompanyWrapper
                            .getCompanyNameById(b.getCompanyid());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                rv = this.compareStrings(aValue, bValue);
                break;

            case MT_ENGINE:
                aValue = null;
                bValue = null;
                // If "useMT" is disabled, should display and compare with empty
                // string.
                aValue = a.getMtEngine();
                bValue = b.getMtEngine();
                rv = this.compareStrings(aValue, bValue);
                break;
            case MT_CONFIDENCE_SCORE:
                long aMtConfidenceScore = a.getMtConfidenceScore();
                long bMtConfidenceScore = b.getMtConfidenceScore();
                if (aMtConfidenceScore > bMtConfidenceScore)
                    rv = 1;
                else if (aMtConfidenceScore == bMtConfidenceScore)
                    rv = 0;
                else
                    rv = -1;
                break;
            case MT_ACTIVE:
                rv = a.isActive() == true ? 1 : -1;
                break;
            case SHOW_IN_EDITOR:
                rv = a.isShowInEditor() == true ? 1 : -1;
                break;
            default:
                aValue = a.getMtProfileName();
                bValue = b.getMtProfileName();
                rv = aValue.compareTo(bValue);
                break;
        }
        return rv;
    }
}

