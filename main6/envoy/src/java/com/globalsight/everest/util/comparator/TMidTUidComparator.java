package com.globalsight.everest.util.comparator;

import java.util.Locale;

import com.globalsight.ling.tm2.segmenttm.TMidTUid;

public class TMidTUidComparator extends StringComparator
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5482311245107082620L;

    /**
     * Creates a RatingComparator with the given type and locale. If the type is
     * not a valid type, then the default comparison is done by displayName
     */
    public TMidTUidComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two Rating objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        TMidTUid a = (TMidTUid) p_A;
        TMidTUid b = (TMidTUid) p_B;
        float aScore = a.getMatchScore();
        float bScore = b.getMatchScore();

        int rv;
        if (aScore > bScore)
            rv = -1;
        else if (aScore == bScore)
            rv = 0;
        else
            rv = 1;
        return rv;
    }
}
