package com.globalsight.connector.blaise.util;

import com.globalsight.connector.blaise.vo.TranslationInboxEntryVo;

import java.util.ArrayList;

/**
 * @author David Yan 2017/2/7
 */
public class BlaiseEntryCollect
{
    private ArrayList<TranslationInboxEntryVo> HDUEntries = new ArrayList<>();
    private ArrayList<TranslationInboxEntryVo> inSheetEntries = new ArrayList<>();
    private ArrayList<TranslationInboxEntryVo> otherEntries = new ArrayList<>();

    public synchronized void addHDUEntries(ArrayList<TranslationInboxEntryVo> entries,
            BlaiseEntryEnum type)
    {
        if (entries == null || entries.size() == 0)
            return;

        switch (type)
        {
            case HDU_WORKBOOK:
                HDUEntries.addAll(entries);
                break;
            case INSHEET:
                inSheetEntries.addAll(entries);
                break;
            case OTHERS:
            default:
                otherEntries.addAll(entries);
                break;
        }
    }

    public synchronized void clearEntries(BlaiseEntryEnum type)
    {
        switch (type)
        {
            case HDU_WORKBOOK:
                HDUEntries.clear();
                break;
            case INSHEET:
                inSheetEntries.clear();
                break;
            case OTHERS:
            default:
                otherEntries.clear();
                break;
        }
    }

    public synchronized void clearAll()
    {
        HDUEntries.clear();
        inSheetEntries.clear();
        otherEntries.clear();
    }
}
