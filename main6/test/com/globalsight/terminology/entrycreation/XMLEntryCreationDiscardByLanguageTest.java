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

/**
 * Import the Terminology Data with the option 
 * "Synchronize on language" --> "Discard new concepts"
 * 
 * @see EntryCreationDiscardByLanguage
 */
public class XMLEntryCreationDiscardByLanguageTest extends EntryCreationBasic
{
    @Test
    public void testBatchAddEntriesAsNew()
    {
        basicTestBatchAddEntriesAsNew(SyncOptions.NOSYNC_ADD);
        
        // Validate the result
        List<?> conceptList = getTBConceptList();
        int expectedConceptLiseSize = 2;
        assertTrue("The actual concept size is " + conceptList.size() + ", but the expected concept size is " + expectedConceptLiseSize, 
                   conceptList.size() == expectedConceptLiseSize);
        
        int actualLangSize = 0;
        int expectedLangSize = 5;
        int termSize = 0;
        int expectedTermSize = 5;
        for (int i = 0; i < conceptList.size(); i++)
        {
            TbConcept tbc = (TbConcept) conceptList.get(i);
            Set<?> langs = tbc.getLanguages();
            actualLangSize += langs.size();
            Iterator<?> it = langs.iterator();
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
    
    @SuppressWarnings("unchecked")
    protected void basicTestBatchAddEntriesAsNew(int p_nosyncAction)
    {
        m_options.setNosyncAction(p_nosyncAction);
        m_instance.setSynchronizeOption(m_options);
        
        long cid = addEntry(m_entry_FR);
        
        ArrayList entryList = new ArrayList();
        String newEntryStr = "<conceptGrp>"
            + "<concept></concept>" 
            + "<transacGrp><transac type=\"origination\">kkTest</transac><date>2011-01-14T17:54:08</date></transacGrp>"
            + "<languageGrp><language name=\"English\" locale=\"en\"/><termGrp><term>"+m_termContentEN+"</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"French\" locale=\"fr\"/><termGrp><term>junitTestFR</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"Germany\" locale=\"de\"/><termGrp><term>"+m_termContentDE+"</term></termGrp></languageGrp>"
            + "</conceptGrp>";
        Entry entry1 = new Entry(newEntryStr);
        entryList.add(entry1);
        newEntryStr = "<conceptGrp>"
            + "<concept></concept>" 
            + "<transacGrp><transac type=\"origination\">kkTest</transac><date>2011-01-14T17:54:08</date></transacGrp>"
            + "<languageGrp><language name=\"English\" locale=\"en\"/><termGrp><term>aaa231</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"French\" locale=\"fr\"/><termGrp><term>aaa_FR231</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"Germany\" locale=\"de\"/><termGrp><term>aaa_DE231</term></termGrp></languageGrp>"
            + "</conceptGrp>";
        Entry entry2 = new Entry(newEntryStr);
        entryList.add(entry2);
        
        m_instance.batchAddEntriesAsNew(m_tbid, entryList, m_session);
    }
    
    protected void setFileType()
    {
        m_fileType = WebAppConstants.TERMBASE_XML;
    }
    
    protected void initData()
    {
        setFileType();
        m_instance = new EntryCreationDiscardByLanguage(m_fileType);
        m_instance.setFileType(m_fileType);
        
        m_options = new SyncOptions();
        m_options.setSyncMode(SyncOptions.SYNC_BY_LANGUAGE);
        m_options.setSyncLanguage("English");
        m_options.setSyncAction(SyncOptions.SYNC_DISCARD);
        m_options.setNosyncAction(SyncOptions.NOSYNC_ADD);  
        m_instance.setSynchronizeOption(m_options);
    }
}
