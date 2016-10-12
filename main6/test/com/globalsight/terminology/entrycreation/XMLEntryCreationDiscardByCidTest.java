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
 * Import the Terminology XML Data with the option 
 * "Synchronize on concept id" --> "Discard new concepts"
 * 
 * @see EntryCreationDiscardByCid
 */
public class XMLEntryCreationDiscardByCidTest extends EntryCreationBasic
{
    @SuppressWarnings("unchecked")
    @Test
    public void testBatchAddEntriesAsNew()
    {
        initData();
        
        ArrayList entryList = new ArrayList();
        long cid = addEntry(m_entry_FR);
        String newEntryStr = "<conceptGrp>"
            + "<concept>" + cid + "</concept>" 
            + "<transacGrp><transac type=\"origination\">kkTest</transac><date>2011-01-14T17:54:08</date></transacGrp>"
            + "<languageGrp><language name=\"English\" locale=\"en\"/><termGrp><term>aaa</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"French\" locale=\"fr\"/><termGrp><term>junitTestFR</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"Germany\" locale=\"de\"/><termGrp><term>"+m_termContentDE+"</term></termGrp></languageGrp>"
            + "</conceptGrp>";
        Entry entry1 = new Entry(newEntryStr);
        entryList.add(entry1);
        newEntryStr = "<conceptGrp>"
            + "<concept>" + cid+1 + "</concept>" 
            + "<transacGrp><transac type=\"origination\">kkTest</transac><date>2011-01-14T17:54:08</date></transacGrp>"
            + "<languageGrp><language name=\"English\" locale=\"en\"/><termGrp><term>aaa231</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"French\" locale=\"fr\"/><termGrp><term>aaa_FR231</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"Germany\" locale=\"de\"/><termGrp><term>aaa_DE231</term></termGrp></languageGrp>"
            + "</conceptGrp>";
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
        m_fileType = WebAppConstants.TERMBASE_XML;
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
