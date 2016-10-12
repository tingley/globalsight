package com.globalsight.terminology.entrycreation;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Termbase.SyncOptions;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.terminology.java.TbLanguage;

/**
 * Import the Terminology Data with the option 
 * "Synchronize on concept id" --> "Merge new and existing concepts"
 * 
 * @see EntryCreationMergeByCid
 */
public class XMLEntryCreationMergeByCidTest extends EntryCreationBasic
{
    @SuppressWarnings("unchecked")
    @Test
    public void testGetTbConceptByEntry()
    {
        initData();
        
        long cid = addEntry(m_entry_FR);
        String newEntryStr = "<conceptGrp>"
            + "<concept>" + cid + "</concept>" 
            + "<transacGrp><transac type=\"origination\">kkTest</transac><date>2011-01-14T17:54:08</date></transacGrp>"
            + "<languageGrp><language name=\"English\" locale=\"en\"/><termGrp><term>aaa</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"French\" locale=\"fr\"/><termGrp><term>junitTestFR</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"Germany\" locale=\"de\"/><termGrp><term>"+m_termContentDE+"</term></termGrp></languageGrp>"
            + "</conceptGrp>";
        Entry newEntry = new Entry();
        newEntry.setXml(newEntryStr);
        TbConcept tbc = ((EntryCreation) m_instance).getTbConceptByEntry(m_tbid, newEntry, m_session);
        
        // Validate the result
        int expectedLangSize = 3;
        Set langs = tbc.getLanguages();
        assertTrue("The actual language size is " + langs.size() + ", but the expected language size is " + expectedLangSize, 
                   langs.size() == expectedLangSize);
        
        int expectedTermSize = 5;
        int termSize = 0;
        Iterator it = langs.iterator();
        while (it.hasNext())
        {
            TbLanguage tbl = (TbLanguage) it.next();
            termSize += tbl.getTerms().size();
        }       
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
        m_instance = new EntryCreationMergeByCid(m_fileType);
        m_instance.setFileType(m_fileType);        

        SyncOptions options = new SyncOptions();
        options.setSyncMode(SyncOptions.SYNC_BY_CONCEPTID);
        options.setSyncAction(SyncOptions.SYNC_MERGE);
        options.setNosyncAction(SyncOptions.NOSYNC_ADD);        
        m_instance.setSynchronizeOption(options);
    }
}
