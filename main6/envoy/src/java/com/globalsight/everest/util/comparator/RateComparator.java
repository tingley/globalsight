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
import java.util.ResourceBundle;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Rate;

/**
* This class can be used to compare Rate objects
*/
public class RateComparator extends StringComparator
{
	//types of comparison
	public static final int NAME = 0;
	public static final int ACTIVITY = 1;
	public static final int LP = 2;
	public static final int CURRENCY = 3;
	public static final int RATE_TYPE = 4;
    public static final int ASC_COMPANY = 5;
    public static final int SOURCE_LOCALE = 6;
    public static final int TARGET_LOCALE = 7;

    String m_fixed;
    String m_hourly;
    String m_page;
    String m_wc;
    String m_wcby;

    public RateComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

	public RateComparator(Locale p_locale, ResourceBundle p_bundle)
	{
	    super(p_locale);
        m_fixed =  p_bundle.getString("lb_rate_type_1");
        m_hourly =  p_bundle.getString("lb_rate_type_2");
        m_page =  p_bundle.getString("lb_rate_type_3");
        m_wc =  p_bundle.getString("lb_rate_type_4");
        m_wcby = p_bundle.getString("lb_rate_type_5");
	}

	/**
	* Performs a comparison of two Rate objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		Rate a = (Rate) p_A;
		Rate b = (Rate) p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
		default:
		case NAME:
			aValue = a.getName();
			bValue = b.getName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case ACTIVITY:
			aValue = a.getActivity().getActivityName();
			bValue = b.getActivity().getActivityName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case LP:
			aValue = a.getLocalePair().getSource().getDisplayName(m_locale) +
                " -> " + a.getLocalePair().getTarget().getDisplayName(m_locale);
			bValue = b.getLocalePair().getSource().getDisplayName(m_locale) +
                " -> " + b.getLocalePair().getTarget().getDisplayName(m_locale);
			rv = this.compareStrings(aValue,bValue);
			break;
		case CURRENCY:
			aValue = a.getCurrency().getDisplayName();
			bValue = b.getCurrency().getDisplayName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case RATE_TYPE:
            aValue = getRateType(a);
            bValue = getRateType(b);
			rv = this.compareStrings(aValue,bValue);
			break;
        case ASC_COMPANY:
             aValue = CompanyWrapper.getCompanyNameById(a.getActivity().getCompanyId());
             bValue = CompanyWrapper.getCompanyNameById(b.getActivity().getCompanyId());
             rv = this.compareStrings(aValue,bValue);
             break;
        case SOURCE_LOCALE:
            aValue = a.getLocalePair().getSource().getDisplayName(m_locale);
            bValue = b.getLocalePair().getSource().getDisplayName(m_locale);
            rv = this.compareStrings(aValue, bValue);
            break;
        case TARGET_LOCALE:
            aValue = a.getLocalePair().getTarget().getDisplayName(m_locale);
            bValue = b.getLocalePair().getTarget().getDisplayName(m_locale);
            rv = this.compareStrings(aValue, bValue);
            break;
		}
		return rv;
	}

    private String getRateType(Rate rate)
    {
        if (rate != null)
        {
            Integer type = rate.getRateType();
            switch (type.intValue())
            {
                case 1:
                    return m_fixed;
                case 2:
                    return m_hourly;
                case 3:
                    return m_page;
                case 4:
                    return m_wc;
                case 5:
                	return m_wcby;
            }
        }
        return null;
    }
}
