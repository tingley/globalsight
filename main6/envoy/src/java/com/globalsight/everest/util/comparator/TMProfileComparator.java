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

import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;

/**
* This class can be used to compare TMProfile objects
*/
public class TMProfileComparator extends StringComparator
{
    private static final long serialVersionUID = -5499939767397996208L;

    //types of TMProfile comparison
	public static final int NAME = 0;
	public static final int DESCRIPTION = 1;
	public static final int ASC_COMPANY = 2;
	public static final int LEVERAGE_MATCH_THRESHOLD = 3;
	public static final int STORAGE_TM = 4;
	public static final int REFERENCE_TMS = 5;
	public static final int SRX = 8;

	/**
	* Creates a TMProfileComparator with the given type and locale.
	* If the type is not a valid type, then the default comparison
	* is done by displayName
	*/
	public TMProfileComparator(int p_type, Locale p_locale)
	{
            super(p_type, p_locale);
	}

	public TMProfileComparator(Locale p_locale)
	{
            super(p_locale);
	}

	/**
	* Performs a comparison of two TMProfileInfo objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) 
        {
		TranslationMemoryProfile a = (TranslationMemoryProfile)p_A;
		TranslationMemoryProfile b = (TranslationMemoryProfile)p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
		case NAME:
			aValue = a.getName();
			bValue = b.getName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case DESCRIPTION:
			aValue = a.getDescription();
			bValue = b.getDescription();
			rv = this.compareStrings(aValue,bValue);
			break;
		case ASC_COMPANY:
			aValue = null;
			bValue = null;
			try {
				aValue = CompanyWrapper.getCompanyNameById(ServerProxy
						.getProjectHandler()
						.getProjectTMById(a.getProjectTmIdForSave(), false)
						.getCompanyId());
				bValue = CompanyWrapper.getCompanyNameById(ServerProxy
						.getProjectHandler()
						.getProjectTMById(b.getProjectTmIdForSave(), false)
						.getCompanyId());
			} catch (Exception e) {
				e.printStackTrace();
			}
			rv = this.compareStrings(aValue,bValue);
			break;
		case LEVERAGE_MATCH_THRESHOLD:
            long aThreshold = a.getFuzzyMatchThreshold();
            long bThreshold = b.getFuzzyMatchThreshold();
            if (aThreshold > bThreshold)
                rv = 1;
            else if (aThreshold == bThreshold)
                rv = 0;
            else
                rv = -1;
            break;
		case STORAGE_TM:
			aValue = null;
			bValue = null;
			try {
				aValue = ServerProxy.getProjectHandler()
						.getProjectTMById(a.getProjectTmIdForSave(), false)
						.getName();
				bValue = ServerProxy.getProjectHandler()
						.getProjectTMById(b.getProjectTmIdForSave(), false)
						.getName();
			} catch (Exception e) {
				e.printStackTrace();
			}
			rv = this.compareStrings(aValue,bValue);
			break;
		case REFERENCE_TMS:
			aValue = null;
			bValue = null;
			try {
				aValue = a.getProjectTMNamesToLeverageFrom();
				bValue = b.getProjectTMNamesToLeverageFrom();
			} catch (Exception e) {
				e.printStackTrace();
			}
			rv = this.compareStrings(aValue,bValue);
			break;
		case SRX:
			aValue = null;
			bValue = null;
			try {
				SegmentationRuleFile aRuleFile = ServerProxy.getSegmentationRuleFilePersistenceManager().getSegmentationRuleFileByTmpid(String.valueOf(a.getId()));
				aValue = (aRuleFile != null? aRuleFile.getName() : "Default");
				SegmentationRuleFile bRuleFile = ServerProxy.getSegmentationRuleFilePersistenceManager().getSegmentationRuleFileByTmpid(String.valueOf(b.getId()));
				bValue = (bRuleFile != null? bRuleFile.getName() : "Default");
			} catch (Exception e) {
				e.printStackTrace();
			}
			rv = this.compareStrings(aValue,bValue);
			break;
        default:
			aValue = a.getName();
			bValue = b.getName();        
			rv = aValue.compareTo(bValue);
			break;
		}
		return rv;
	}
}

