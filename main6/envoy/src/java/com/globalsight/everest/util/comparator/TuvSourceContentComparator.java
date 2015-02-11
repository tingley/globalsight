package com.globalsight.everest.util.comparator;

import java.util.Comparator;

import com.globalsight.everest.tuv.Tuv;

public class TuvSourceContentComparator implements Comparator
{
    /**
     * the class is used to compare the tuv list 
     * according to the sourceContent
     */
    public int compare(Object o1, Object o2)
    {
        Tuv tuv1 = (Tuv) o1;
        Tuv tuv2 = (Tuv) o2;
        
        String sc1 = tuv1.getTu().getSourceContent();
        String sc2 = tuv2.getTu().getSourceContent();
        
        if (sc1 == null && sc2 == null)
        {
            return 0;
        }
        else if (sc1 == null)
        {
            return 1;
        }
        else if (sc2 == null) 
        {
            return -1;
        }
        else 
        {
            return sc1.compareTo(sc2);
        }
    }
}
