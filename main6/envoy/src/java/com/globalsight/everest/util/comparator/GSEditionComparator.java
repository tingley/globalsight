package com.globalsight.everest.util.comparator;

import java.util.Locale;

import com.globalsight.everest.gsedition.GSEdition;;

public class GSEditionComparator extends StringComparator{
    public static final int NAME = 0;
    public static final int HOSTNAME = 1;
    public static final int HOSTPAORT = 2;
    public static final int DESCRIPTION = 3;
    
    public GSEditionComparator(Locale pLocale) {
        super(pLocale);
    }

    public GSEditionComparator(int pType, Locale pLocale) {
        super(pType, pLocale);
    }

    public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
        GSEdition a = (GSEdition) p_A;
        GSEdition b = (GSEdition) p_B;
        
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
            case HOSTNAME:
                aValue = a.getHostName();
                bValue = b.getHostName();
                rv = this.compareStrings(aValue,bValue);
                break;
            case HOSTPAORT:
                aValue = a.getHostPort();
                bValue = b.getHostPort();
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
