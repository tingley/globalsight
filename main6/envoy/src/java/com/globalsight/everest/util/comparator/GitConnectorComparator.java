package com.globalsight.everest.util.comparator;

import java.util.Locale;

import com.globalsight.cxe.entity.gitconnector.GitConnector;

public class GitConnectorComparator extends StringComparator
{
	private static final long serialVersionUID = 6119352480197119220L;
	// types of comparison
    public static final int NAME = 0;
    public static final int ID = 1;
    public static final int DESC = 2;
    public static final int URL = 3;
    public static final int USER_NAME = 4;
    public static final int COMPANY_NAME = 5;
    public static final int BRANCH = 6;
    public static final int EMAIL = 7;

    public GitConnectorComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two MindTouchConnectorComparator objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        GitConnector a = (GitConnector) p_A;
        GitConnector b = (GitConnector) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        case ID:
            rv = (int)(a.getId() - b.getId());
            break;
        case DESC:
            aValue = a.getDescription();
            bValue = b.getDescription();
            rv = this.compareStrings(aValue, bValue);
            break;
        case URL:
            aValue = a.getUrl();
            bValue = b.getUrl();
            rv = this.compareStrings(aValue, bValue);
            break;
        case USER_NAME:
            aValue = a.getUsername();
            bValue = b.getUsername();
            rv = this.compareStrings(aValue, bValue);
            break;
        case COMPANY_NAME:
            aValue = a.getCompanyName();
            bValue = b.getCompanyName();
            rv = this.compareStrings(aValue, bValue);
            break;
        case BRANCH:
            aValue = a.getBranch();
            bValue = b.getBranch();
            rv = this.compareStrings(aValue, bValue);
            break;
        case EMAIL:
        	 aValue = a.getEmail();
             bValue = b.getEmail();
             rv = this.compareStrings(aValue, bValue);
             break;
        default:
        case NAME:
            aValue = a.getName();
            bValue = b.getName();
            rv = this.compareStrings(aValue, bValue);
            break;
        }
        return rv;
    }
}
