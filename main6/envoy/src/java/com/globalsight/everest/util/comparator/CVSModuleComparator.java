package com.globalsight.everest.util.comparator;

import java.util.Locale;
import com.globalsight.everest.cvsconfig.CVSModule;;

public class CVSModuleComparator extends StringComparator {
    public static final int NAME = 0;
    public static final int MODULENAME = 1;
    public static final int SERVER = 2;
    public static final int LAST_CHECKOUT = 3;
    public static final int BRANCH = 4;
    
    public CVSModuleComparator(Locale pLocale) {
        super(pLocale);
    }

    public CVSModuleComparator(int pType, Locale pLocale) {
        super(pType, pLocale);
    }

    public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
        CVSModule a = (CVSModule) p_A;
        CVSModule b = (CVSModule) p_B;
        
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
            case MODULENAME:
                aValue = a.getModulename();
                bValue = b.getModulename();
                rv = this.compareStrings(aValue,bValue);
                break;
            case SERVER:
                aValue = a.getServer().getName();
                bValue = b.getServer().getName();
                rv = this.compareStrings(aValue,bValue);
                break;
            case LAST_CHECKOUT:
            	aValue = a.getLastCheckout();
            	bValue = b.getLastCheckout();
            	rv = this.compareStrings(aValue, bValue);
            	break;
            case BRANCH:
            	aValue = a.getBranch();
            	bValue = b.getBranch();
            	rv = this.compareStrings(aValue, bValue);
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
