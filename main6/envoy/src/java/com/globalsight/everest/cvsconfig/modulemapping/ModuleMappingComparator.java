package com.globalsight.everest.cvsconfig.modulemapping;

import java.util.Locale;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.comparator.StringComparator;

public class ModuleMappingComparator extends StringComparator {
	public static final int SOURCE_LOCALE = 0;
	public static final int SOURCE_LOCALE_LONG = 1;
	public static final int TARGET_LOCALE = 2;
	public static final int TARGET_LOCALE_LONG = 3;
	public static final int COMPANY = 4;
	
	public ModuleMappingComparator(Locale pLocale) {
		super(pLocale);
	}

	public ModuleMappingComparator(int pType, Locale pLocale) {
		super(pType, pLocale);
	}

    public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
        ModuleMapping a = (ModuleMapping) p_A;
        ModuleMapping b = (ModuleMapping) p_B;
        
        String aValue;
        String bValue;
        int rv;
        
        switch (m_type)
        {
            case SOURCE_LOCALE:
                aValue = a.getSourceLocale();
                bValue = b.getSourceLocale();
                rv = this.compareStrings(aValue,bValue);
                break;
            case SOURCE_LOCALE_LONG:
                aValue = a.getSourceLocaleLong();
                bValue = b.getSourceLocaleLong();
                rv = this.compareStrings(aValue,bValue);
                break;
            case TARGET_LOCALE:
                aValue = a.getTargetLocale();
                bValue = b.getTargetLocale();
                rv = this.compareStrings(aValue,bValue);
                break;
            case TARGET_LOCALE_LONG:
                aValue = a.getTargetLocaleLong();
                bValue = b.getTargetLocaleLong();
                rv = this.compareStrings(aValue,bValue);
                break;
            case COMPANY:
                aValue = CompanyWrapper.getCompanyNameById(String.valueOf(a.getCompanyId()));
                bValue = CompanyWrapper.getCompanyNameById(String.valueOf(b.getCompanyId()));
                rv = this.compareStrings(aValue,bValue);
                break;
            default:
                aValue = a.getSourceLocale();
                bValue = b.getSourceLocale();
                rv = this.compareStrings(aValue,bValue);
                break;
            }
            return rv;
	}

}
