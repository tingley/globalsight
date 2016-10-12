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
import com.globalsight.terminology.java.TbTerm;

/**
 * Import the Terminology Data with the option 
 * "Synchronize on language" --> "Merge new and existing concepts"
 * 
 * @see EntryCreationMergeByLanguage
 */
public class XMLEntryCreationMergeByLanguageTest extends EntryCreationBasic
{    
    @SuppressWarnings("unchecked")
    @Test
    public void testGetConceptsList()
    {
        long cid = addEntry(m_entry_FR);
        String newEntryStr = "<conceptGrp>"
            + "<concept>" + cid + "</concept>" 
            + "<transacGrp><transac type=\"origination\">kkTest</transac><date>2011-01-14T17:54:08</date></transacGrp>"
            + "<languageGrp><language name=\"English\" locale=\"en\"/><termGrp><term>"+ m_termContentEN + "</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"French\" locale=\"fr\"/><termGrp><term>junitTestFR</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"Germany\" locale=\"de\"/><termGrp><term>"+ m_termContentDE +"</term></termGrp></languageGrp>"
            + "</conceptGrp>";
        Entry newEntry = new Entry(newEntryStr);
        List list = ((EntryCreationMergeByLanguage) m_instance).getConceptsList(m_tbid, newEntry, m_session);
        
        // Validate the result
        int expectedLangSize = 3;
        Set langs = ((TbConcept) list.get(0)).getLanguages();
        assertTrue("The actual language size is " + langs.size() + ", but the expected language size is " + expectedLangSize, 
                   langs.size() == expectedLangSize);
        
        int expectedTermSize = 4;
        Iterator it = langs.iterator();
        List termList = new ArrayList();
        while (it.hasNext())
        {
            TbLanguage tbl = (TbLanguage) it.next();
            Iterator termIt = tbl.getTerms().iterator();
            while (termIt.hasNext())
            {
                termList.add(termIt.next());
            }
        }       
        assertTrue("The actual term size is " + termList.size() + ", but the expected term size is " + expectedTermSize,
                   termList.size() == expectedTermSize);
        
        List expectedList = new ArrayList();
        expectedList.add("English"  + m_termContentEN);
        expectedList.add("French"   + m_termContentFR);
        expectedList.add("French"   + "junitTestFR");
        expectedList.add("Germany"  + m_termContentDE);
        
        assertTrue(validateTermList(termList, expectedList));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testGetConceptsList2()
    {
        long cid = addEntry(m_entry_FR);
        String newEntryStr = "<conceptGrp>"
            + "<concept>" + cid + "</concept>" 
            + "<transacGrp><transac type=\"origination\">kkTest</transac><date>2011-01-14T17:54:08</date></transacGrp>"
            + "<languageGrp><language name=\"English\" locale=\"en\"/><termGrp><term>junitTestEnglish</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"French\" locale=\"fr\"/><termGrp><term>junitTestFR</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"Germany\" locale=\"de\"/><termGrp><term>"+ m_termContentDE +"</term></termGrp></languageGrp>"
            + "</conceptGrp>";
        Entry newEntry = new Entry(newEntryStr);
        List list = ((EntryCreationMergeByLanguage) m_instance).getConceptsList(m_tbid, newEntry, m_session);
        
        // Validate the result
        int expectedLangSize = 3;
        Set langs = ((TbConcept) list.get(0)).getLanguages();
        assertTrue("The actual language size is " + langs.size() + ", but the expected language size is " + expectedLangSize, 
                   langs.size() == expectedLangSize);
        
        int expectedTermSize = 3;
        Iterator it = langs.iterator();
        List termList = new ArrayList();
        while (it.hasNext())
        {
            TbLanguage tbl = (TbLanguage) it.next();
            Iterator termIt = tbl.getTerms().iterator();
            while (termIt.hasNext())
            {
                termList.add(termIt.next());
            }
        }       
        assertTrue("The actual term size is " + termList.size() + ", but the expected term size is " + expectedTermSize,
                   termList.size() == expectedTermSize);
        
        List expectedList = new ArrayList();
        expectedList.add("English"  + "junitTestEnglish");
        expectedList.add("French"   + "junitTestFR");
        expectedList.add("Germany"  + m_termContentDE);
        
        assertTrue(validateTermList(termList, expectedList));
    }
    
    /**
     * Validate the term result.
     * @param p_termList actural term list
     * @param p_expected expected term (language + content) list
     * @return
     */
    public boolean validateTermList(List<TbTerm> p_termList, List p_expected)
    {
        if (p_termList.size() != p_expected.size())
        {
            return false;
        }

        for (TbTerm tbt : p_termList)
        {
            int index = p_expected.indexOf(tbt.getLanguage()
                    + tbt.getTermContent());
            if (index > -1)
            {
                p_expected.remove(index);
            }
        }

        return p_expected.size() == 0 ? true : false;
    }
    
    protected void setFileType()
    {
        m_fileType = WebAppConstants.TERMBASE_XML;
    }
    
    protected void initData()
    {
        setFileType();
        m_instance = new EntryCreationMergeByLanguage(m_fileType);
        m_instance.setFileType(m_fileType);
        
        m_options = new SyncOptions();
        m_options.setSyncMode(SyncOptions.SYNC_BY_LANGUAGE);
        m_options.setSyncLanguage("English");
        m_options.setSyncAction(SyncOptions.SYNC_MERGE);
        m_options.setNosyncAction(SyncOptions.NOSYNC_ADD);  
        m_instance.setSynchronizeOption(m_options);
    }
}
