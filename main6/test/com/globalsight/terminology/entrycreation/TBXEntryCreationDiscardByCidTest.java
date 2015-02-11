package com.globalsight.terminology.entrycreation;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Termbase.SyncOptions;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.terminology.java.TbLanguage;

public class TBXEntryCreationDiscardByCidTest extends EntryCreationBasic
{
    @SuppressWarnings("unchecked")
    @Test
    public void testBatchAddEntriesAsNew()
    {
        initData();
        
        ArrayList entryList = new ArrayList();
        long cid = addEntry(m_entry_FR_TBX);
        String newEntryStr = "<termEntry id=\"" + cid + "\">"
            + "<langSet xml:lang=\"en\"><ntig><termGrp><term>aaa</term></termGrp></ntig></langSet>"
            + "<langSet xml:lang=\"fr\"><ntig><termGrp><term>junitTestFR</term></termGrp></ntig></langSet>"
            + "<langSet xml:lang=\"de\"><ntig><termGrp><term>"+m_termContentDE+"</term></termGrp></ntig></langSet>"
            + "</termEntry>";
        Entry entry1 = new Entry(newEntryStr);
        entryList.add(entry1);
        newEntryStr = "<termEntry id=\"" + cid+1 + "\">"
            + "<langSet xml:lang=\"en\"><ntig><termGrp><term>aaa231</term></termGrp></ntig></langSet>"
            + "<langSet xml:lang=\"fr\"><ntig><termGrp><term>aaa_FR231</term></termGrp></ntig></langSet>"
            + "<langSet xml:lang=\"de\"><ntig><termGrp><term>aaa_DE231</term></termGrp></ntig></langSet>"
            + "</termEntry>";
        Entry entry2 = new Entry(newEntryStr);
        entryList.add(entry2);
        
        m_instance.batchAddEntriesAsNew(m_tbid, entryList, m_session);
        
        
        // Validate the result
        List<?> conceptList = getTBConceptList();
        int expectedConceptLiseSize = 2;
        assertTrue("The actual concept size is " + conceptList.size() + ", but the expected concept size is " + expectedConceptLiseSize, 
                   conceptList.size() == expectedConceptLiseSize);
        
        int expectedLangSize = 5;
        int actualLangSize = 0;
        int expectedTermSize = 5;
        int termSize = 0;
        for (int i = 0; i < conceptList.size(); i++)
        {
            TbConcept tbc = (TbConcept) conceptList.get(i);
            Set langs = tbc.getLanguages();
            actualLangSize += langs.size();
            Iterator it = langs.iterator();
            while (it.hasNext())
            {
                TbLanguage tbl = (TbLanguage) it.next();
                termSize += tbl.getTerms().size();
            }
        }
        assertTrue("The actual language size is " + actualLangSize + ", but the expected language size is " + expectedLangSize, 
                   actualLangSize == expectedLangSize);       
              
        assertTrue("The actual term size is " + termSize + ", but the expected term size is " + expectedTermSize,
                   termSize == expectedTermSize);
    }
    
    protected void setFileType()
    {
        m_fileType = WebAppConstants.TERMBASE_TBX;
    }
    
    protected void initData()
    {
        setFileType();
        m_instance = new EntryCreationDiscardByCid(m_fileType);
        m_instance.setFileType(m_fileType);
        
        SyncOptions options = new SyncOptions();
        options.setSyncMode(SyncOptions.SYNC_BY_CONCEPTID);
        options.setSyncAction(SyncOptions.SYNC_DISCARD);
        options.setNosyncAction(SyncOptions.NOSYNC_ADD);        
        m_instance.setSynchronizeOption(options);
    }
}
