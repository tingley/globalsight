package com.globalsight.everest.util.comparator;

import java.util.Locale;

import com.globalsight.cxe.entity.gitconnector.GitConnectorFileMapping;

public class GitConnectorFileMappingComparator extends StringComparator
{
	private static final long serialVersionUID = 6119352480197119220L;
	// types of comparison
    public static final int ID = 0;
    public static final int SOURCE_LOCALE = 1;
    public static final int SOURCE_MAPPING_PATH = 2;
    public static final int TARGET_LOCALE = 3;
    public static final int TARGET_MAPPING_PATH = 4;
    public static final int COMPANY_NAME = 5;

    public GitConnectorFileMappingComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two MindTouchConnectorComparator objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
    	GitConnectorFileMapping a = (GitConnectorFileMapping) p_A;
    	GitConnectorFileMapping b = (GitConnectorFileMapping) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        default:
        case ID:
            rv = (int)(a.getId() - b.getId());
            break;
        case SOURCE_LOCALE:
            aValue = a.getSourceLocale();
            bValue = b.getSourceLocale();
            rv = this.compareStrings(aValue, bValue);
            break;
        case SOURCE_MAPPING_PATH:
            aValue = a.getSourceMappingPath();
            bValue = b.getSourceMappingPath();
            rv = this.compareStrings(aValue, bValue);
            break;
        case TARGET_LOCALE:
            aValue = a.getTargetLocale();
            bValue = b.getTargetLocale();
            rv = this.compareStrings(aValue, bValue);
            break;
        case TARGET_MAPPING_PATH:
            aValue = a.getTargetMappingPath();
            bValue = b.getTargetMappingPath();
            rv = this.compareStrings(aValue, bValue);
            break;
        case COMPANY_NAME:
            aValue = a.getCompanyName();
            bValue = b.getCompanyName();
            rv = this.compareStrings(aValue, bValue);
            break;
        }
        return rv;
    }
}
