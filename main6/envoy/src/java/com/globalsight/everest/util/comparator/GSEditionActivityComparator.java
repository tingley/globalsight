package com.globalsight.everest.util.comparator;

import java.util.Locale;

import com.globalsight.everest.gsedition.GSEditionActivity;

public class GSEditionActivityComparator extends StringComparator {
    public static final int NAME = 0;
    public static final int FILEPROFILE = 1;
    public static final int SOURCEFILE = 2;
    public static final int DESCRIPTION = 3;
    
    public GSEditionActivityComparator(Locale pLocale) {
        super(pLocale);
    }

    public GSEditionActivityComparator(int pType, Locale pLocale) {
        super(pType, pLocale);
    }

    public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
        GSEditionActivity a = (GSEditionActivity) p_A;
        GSEditionActivity b = (GSEditionActivity) p_B;
        
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
            case FILEPROFILE:
                aValue = a.getFileProfileName();
                bValue = b.getFileProfileName();
                rv = this.compareStrings(aValue,bValue);
                break;
            case SOURCEFILE:
                if(a.getSourceFileReference() == 0) {
                    aValue = "false";
                }
                else {
                    aValue = "true";
                }
                
                if(b.getSourceFileReference() == 0) {
                    bValue = "false";
                }
                else {
                    bValue = "true";
                }

                rv = this.compareStrings(aValue,bValue);
                break;
            case DESCRIPTION:
                aValue = a.getDescription();
                bValue = b.getDescription();
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
