package com.globalsight.everest.webapp.pagehandler.administration.localepairs;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

public class LocalePairFilter {
	private String lpSourceFilter;
	private String lpTargetFilter;
	private String lpCompanyFilter;
	
	public Vector filterLocalePairsByName(HttpSession p_session, Vector p_lps)
	{
		SessionManager sessionManager = (SessionManager) p_session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
        String lpSourceFilterValue = (String) sessionManager
                .getAttribute(LocalePairConstants.FILTER_SOURCELOCALE);
        String lpTargetFilterValue = (String) sessionManager
                .getAttribute(LocalePairConstants.FILTER_TARGETLOCALE);
        String lpCompanyFilterValue = (String) sessionManager
				.getAttribute(LocalePairConstants.FILTER_COMPANY);

		if (lpSourceFilterValue == null)
		{
			lpSourceFilterValue = "";
		}
		sessionManager.setAttribute(LocalePairConstants.FILTER_SOURCELOCALE,
				lpSourceFilterValue.trim());
        
		if (lpTargetFilterValue == null)
		{
			lpTargetFilterValue = "";
		}
		sessionManager.setAttribute(LocalePairConstants.FILTER_TARGETLOCALE,
				lpTargetFilterValue.trim());
		
		if (lpCompanyFilterValue == null)
		{
			lpCompanyFilterValue = "";
		}
		sessionManager.setAttribute(LocalePairConstants.FILTER_COMPANY,
				lpCompanyFilterValue.trim());
		
        if (!StringUtil.isEmpty(lpSourceFilterValue))
        {
        	for(Iterator it = p_lps.iterator(); it.hasNext();)
        	{
        		LocalePair lp = (LocalePair) it.next();
        		GlobalSightLocale GSlp = lp.getSource();
                String displayName = GSlp.getDisplayName();
        		if(displayName.toLowerCase().indexOf(lpSourceFilterValue.toLowerCase()) == -1)
        		{
        			it.remove();
        		}
        	}
        }
        if (!StringUtil.isEmpty(lpTargetFilterValue))
        {
        	for(Iterator it = p_lps.iterator(); it.hasNext();)
        	{
        		LocalePair lp = (LocalePair) it.next();
        		GlobalSightLocale GSlp = lp.getTarget();
                String displayName = GSlp.getDisplayName();
        		if(displayName.toLowerCase().indexOf(lpTargetFilterValue.toLowerCase()) == -1)
        		{
        			it.remove();
        		}
        	}
        }
		if (!StringUtil.isEmpty(lpCompanyFilterValue))
		{
			for (Iterator it = p_lps.iterator(); it.hasNext();)
			{
				LocalePair lp = (LocalePair) it.next();
				String comName = CompanyWrapper.getCompanyNameById(
						lp.getCompanyId()).toLowerCase();
				if (comName.indexOf(lpCompanyFilterValue.trim().toLowerCase()) == -1)
				{
					it.remove();
				}
			}
		}
        return   p_lps;
	}
	public String getLpSourceFilter() 
	{
		return lpSourceFilter;
	}

	public void setLpSourceFilter(String lpSourceFilter) 
	{
		this.lpSourceFilter = lpSourceFilter;
	}

	public String getLpTargetFilter() 
	{
		return lpTargetFilter;
	}

	public void setLpTargetFilter(String lpTargetFilter) 
	{
		this.lpTargetFilter = lpTargetFilter;
	}

	public String getLpCompanyFilter() 
	{
		return lpCompanyFilter;
	}

	public void setLpCompanyFilter(String lpCompanyFilter) 
	{
		this.lpCompanyFilter = lpCompanyFilter;
	}
	
 
}
