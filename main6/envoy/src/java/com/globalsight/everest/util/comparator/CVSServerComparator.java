package com.globalsight.everest.util.comparator;

import java.util.Locale;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.cvsconfig.CVSServer;

public class CVSServerComparator extends StringComparator {
	public static final int NAME = 0;
	public static final int HOST_IP = 1;
	public static final int HOST_PORT = 2;
	public static final int PROTOCOL = 3;
	public static final int COMPANY = 4;
	public static final int REPOSITORY = 5;
	public static final int LOGIN_USER = 6;
	
	public CVSServerComparator(Locale pLocale) {
		super(pLocale);
	}

	public CVSServerComparator(int pType, Locale pLocale) {
		super(pType, pLocale);
	}

    public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
        CVSServer a = (CVSServer) p_A;
        CVSServer b = (CVSServer) p_B;
        
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
            case HOST_IP:
                aValue = a.getHostIP();
                bValue = b.getHostIP();
                rv = this.compareStrings(aValue,bValue);
                break;
            case HOST_PORT:
                aValue = String.valueOf(a.getHostPort());
                bValue = String.valueOf(b.getHostPort());
                rv = this.compareStrings(aValue,bValue);
                break;
            case PROTOCOL:
                aValue = String.valueOf(a.getProtocol());
                bValue = String.valueOf(b.getProtocol());
                rv = this.compareStrings(aValue,bValue);
                break;
            case COMPANY:
                aValue = CompanyWrapper.getCompanyNameById(String.valueOf(a.getCompanyId()));
                bValue = CompanyWrapper.getCompanyNameById(String.valueOf(b.getCompanyId()));
                rv = this.compareStrings(aValue,bValue);
                break;
            case REPOSITORY:
                aValue = a.getRepository();
                bValue = b.getRepository();
                rv = this.compareStrings(aValue,bValue);
            	break;
            case LOGIN_USER:
                aValue = a.getLoginUser();
                bValue = b.getLoginUser();
                rv = this.compareStrings(aValue,bValue);
            	break;
            default:
                aValue = a.getName();
                bValue = b.getName();
                rv = this.compareStrings(aValue,bValue);
                break;
            }
            return rv;
	}

}
