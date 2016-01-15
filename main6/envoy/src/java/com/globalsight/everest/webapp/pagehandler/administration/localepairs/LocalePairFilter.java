package com.globalsight.everest.webapp.pagehandler.administration.localepairs;

import java.util.Vector;

import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;

public class LocalePairFilter
{
	public Vector<LocalePair> filter(HttpSession p_session,
			Vector<LocalePair> lps)
    {
    	Vector<LocalePair> result = new Vector<LocalePair>();
		SessionManager sessionManager = (SessionManager) p_session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
        String lpSourceFilterValue = (String) sessionManager
                .getAttribute(LocalePairConstants.FILTER_SOURCELOCALE);
        String lpTargetFilterValue = (String) sessionManager
                .getAttribute(LocalePairConstants.FILTER_TARGETLOCALE);
        String lpCompanyFilterValue = (String) sessionManager
				.getAttribute(LocalePairConstants.FILTER_COMPANY);   
        for (LocalePair lp : lps)
        {
            if (!like(lpSourceFilterValue, lp.getSource().getDisplayName()))
            {
                continue;
            }

            if (!like(lpTargetFilterValue, lp.getTarget().getDisplayName()))
            {
                continue;
            }

            String comName = CompanyWrapper.getCompanyNameById(
					lp.getCompanyId());
            if (!like(lpCompanyFilterValue, comName))
            {
                continue;
            }
            
            result.add(lp);
        }
        
        return result;
    }

    private boolean like(String filterValue, String candidateValue)
    {
        if (filterValue == null)
            return true;

        filterValue = filterValue.trim();
        if (filterValue.length() == 0)
            return true;

        if (candidateValue == null)
            return false;

        filterValue = filterValue.toLowerCase();
        candidateValue = candidateValue.toLowerCase();

        return candidateValue.indexOf(filterValue) > -1;
    }
}
