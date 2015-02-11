package com.globalsight.everest.cvsconfig;

import java.util.Locale;

import com.globalsight.everest.util.comparator.StringComparator;

public class CVSFileProfileComparator extends StringComparator{

	private static final long serialVersionUID = 8246588753510092562L;
	
	public static final int PROJECT = 0;
	public static final int CVS_MODULE = 1;
	public static final int FILE_EXT = 2;
	public static final int FILE_PROFILE = 3;
	public static final int SOURCE_LOCALE = 4;

	public CVSFileProfileComparator(Locale pLocale) {
		super(pLocale);
	}

	public CVSFileProfileComparator(int pType, Locale pLocale) {
		super(pType, pLocale);
	}
	
    public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
        CVSFileProfile a = (CVSFileProfile) p_A;
        CVSFileProfile b = (CVSFileProfile) p_B;
        
        String aValue;
        String bValue;
        int rv;
        
        switch (m_type)
        {
            case PROJECT:
                aValue = a.getProject().getName();
                bValue = b.getProject().getName();
                rv = this.compareStrings(aValue,bValue);
                break;
            case CVS_MODULE:
                aValue = a.getModule().getName();
                bValue = b.getModule().getName();
                rv = this.compareStrings(aValue,bValue);
                break;
            case FILE_EXT:
                aValue = a.getFileExt();
                bValue = b.getFileExt();
                rv = this.compareStrings(aValue,bValue);
                break;
            case FILE_PROFILE:
            	aValue = a.getFileProfile().getName();
            	bValue = b.getFileProfile().getName();
            	rv = this.compareStrings(aValue, bValue);
            	break;
            case SOURCE_LOCALE:
            	aValue = a.getSourceLocale();
            	bValue = b.getSourceLocale();
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
