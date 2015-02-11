package com.globalsight.everest.edit.online;

import java.util.ArrayList;
import java.util.List;

public class OnlineEditorConstants
{
    // Segments Filters in Popup/Inline Editor
    public static final String SEGMENT_FILTER_ALL = "segFilterAll";
    public static final String SEGMENT_FILTER_ALL_EXCEPT_ICE = "segFilterAllExceptICE";
    public static final String SEGMENT_FILTER_ALL_EXCEPT_ICE_AND_100 = "segFilterAllExceptICEand100";
    public static final String SEGMENT_FILTER_ICE = "segFilterICE";
    public static final String SEGMENT_FILTER_100 = "segFilter100";
    public static final String SEGMENT_FILTER_REPEATED = "segFilterRepeated";
    public static final String SEGMENT_FILTER_REPETITIONS = "segFilterRepetitions";
    public static final String SEGMENT_FILTER_MODIFIED = "segFilterModified";
    public static final String SEGMENT_FILTER_COMMENTED = "segFilterCommented";
    public static final String SEGMENT_FILTER_MACHINETRANSLATION = "segFilterMT";
    public static final String SEGMENT_FILTER_NO_TRANSLATED = "segFilterNoTrans";
    public static final String SEGMENT_FILTER_APPROVED = "segFilterApproved";
    
    public static List<String> SEGMENT_FILTERS;
    static
    {
        SEGMENT_FILTERS = new ArrayList<String>();
        SEGMENT_FILTERS.add(SEGMENT_FILTER_ALL);
        SEGMENT_FILTERS.add(SEGMENT_FILTER_ALL_EXCEPT_ICE);
        SEGMENT_FILTERS.add(SEGMENT_FILTER_ALL_EXCEPT_ICE_AND_100);
        SEGMENT_FILTERS.add(SEGMENT_FILTER_ICE);
        SEGMENT_FILTERS.add(SEGMENT_FILTER_100);
        SEGMENT_FILTERS.add(SEGMENT_FILTER_MACHINETRANSLATION);
        SEGMENT_FILTERS.add(SEGMENT_FILTER_REPEATED);
        SEGMENT_FILTERS.add(SEGMENT_FILTER_REPETITIONS);
        SEGMENT_FILTERS.add(SEGMENT_FILTER_MODIFIED);
        SEGMENT_FILTERS.add(SEGMENT_FILTER_NO_TRANSLATED);
        SEGMENT_FILTERS.add(SEGMENT_FILTER_APPROVED);
        SEGMENT_FILTERS.add(SEGMENT_FILTER_COMMENTED);
    }
}
