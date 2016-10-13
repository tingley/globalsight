package com.globalsight.connector.eloqua;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class EloquaManager 
{

	 private static final Logger logger = Logger
	            .getLogger(EloquaManager.class);


	    public static List<?> getAllConnector()
	    {
	        String hql = "from EloquaConnector e ";
	        HashMap<String, Long> map = new HashMap<String, Long>();

	        String currentId = CompanyThreadLocal.getInstance().getValue();
	        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
	        {
	            hql += " where e.companyId = :companyId";
	            map.put("companyId", Long.parseLong(currentId));
	        }

	        return HibernateUtil.search(hql, map);
	    }
	    
	    public static List<?> getAllMails()
	    {
	        String hql = "from EloquaConnector e ";
	        HashMap<String, Long> map = new HashMap<String, Long>();

	        String currentId = CompanyThreadLocal.getInstance().getValue();
	        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
	        {
	            hql += " where e.companyId = :companyId";
	            map.put("companyId", Long.parseLong(currentId));
	        }

	        return HibernateUtil.search(hql, map);
	    }
}
