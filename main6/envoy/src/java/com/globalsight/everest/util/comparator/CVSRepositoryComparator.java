package com.globalsight.everest.util.comparator;

import java.util.Locale;
import com.globalsight.everest.cvsconfig.CVSRepository;;

public class CVSRepositoryComparator extends StringComparator {
    public static final int NAME = 0;
    public static final int REPOSITORY = 1;
    public static final int FOLDERNAME = 2;
    public static final int SERVERNAME = 3;
    
    public CVSRepositoryComparator(Locale pLocale) {
        super(pLocale);
    }

    public CVSRepositoryComparator(int pType, Locale pLocale) {
        super(pType, pLocale);
    }

    public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
        CVSRepository a = (CVSRepository) p_A;
        CVSRepository b = (CVSRepository) p_B;
        
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
            case REPOSITORY:
                aValue = a.getRepository();
                bValue = b.getRepository();
                rv = this.compareStrings(aValue,bValue);
                break;
            case FOLDERNAME:
                aValue = a.getFolderName();
                bValue = b.getFolderName();
                rv = this.compareStrings(aValue,bValue);
                break;
            case SERVERNAME:
                aValue = a.getServer().getName();
                bValue = a.getServer().getName();
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
