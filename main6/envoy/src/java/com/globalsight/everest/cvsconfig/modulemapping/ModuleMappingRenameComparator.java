package com.globalsight.everest.cvsconfig.modulemapping;

import java.util.Locale;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.comparator.StringComparator;

public class ModuleMappingRenameComparator extends StringComparator {
	public static final int SOURCE_NAME = 0;
	public static final int TARGET_NAME = 1;
	
	public ModuleMappingRenameComparator(Locale pLocale) {
		super(pLocale);
	}

	public ModuleMappingRenameComparator(int pType, Locale pLocale) {
		super(pType, pLocale);
	}

    public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
    	ModuleMappingRename a = (ModuleMappingRename) p_A;
    	ModuleMappingRename b = (ModuleMappingRename) p_B;
        
        String aValue;
        String bValue;
        int rv;
        
        switch (m_type)
        {
            case SOURCE_NAME:
                aValue = a.getSourceName();
                bValue = b.getTargetName();
                rv = this.compareStrings(aValue,bValue);
                break;
            case TARGET_NAME:
                aValue = a.getSourceName();
                bValue = b.getTargetName();
                rv = this.compareStrings(aValue,bValue);
                break;
            default:
                aValue = a.getSourceName();
                bValue = b.getTargetName();
                rv = this.compareStrings(aValue,bValue);
                break;
            }
            return rv;
	}

}
