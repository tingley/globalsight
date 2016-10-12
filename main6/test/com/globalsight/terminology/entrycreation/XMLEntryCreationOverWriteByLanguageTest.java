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
import com.globalsight.terminology.java.TbTerm;

/**
 * Import the Terminology Data with the option 
 * "Synchronize on language" --> "Overwrite existing concepts"
 * 
 * @see EntryCreationOverWriteByLanguage
 */
public class XMLEntryCreationOverWriteByLanguageTest extends EntryCreationBasic
{
    @Test
    public void testGetTbConceptByEntry()
    {
        long cid = addEntry(m_entry_FR);
        String newEntryStr = "<conceptGrp>"
            + "<concept>" + cid + "</concept>" 
            + "<transacGrp><transac type=\"origination\">kkTest</transac><date>2011-01-14T17:54:08</date></transacGrp>"
            + "<languageGrp><language name=\"English\" locale=\"en\"/><termGrp><term>"+ m_termContentEN + "</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"French\" locale=\"fr\"/><termGrp><term>junitTestFR</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"Germany\" locale=\"de\"/><termGrp><term>"+ m_termContentDE +"</term></termGrp></languageGrp>"
            + "</conceptGrp>";
        Entry newEntry = new Entry();
        newEntry.setXml(newEntryStr);
        TbConcept tbc = ((EntryCreationOverWriteByLanguage) m_instance).getTbConceptByEntry(m_tbid, newEntry, m_session);
        
        // Validate the result
        int expectedLangSize = 3;
        Set langs = tbc.getLanguages();
        assertTrue("The actual language size is " + langs.size() + ", but the expected language size is " + expectedLangSize, 
                   langs.size() == expectedLangSize);
        
        int expectedTermSize = 3;
        int termSize = 0;
        Iterator it = langs.iterator();
        while (it.hasNext())
        {
            TbLanguage tbl = (TbLanguage) it.next();
            termSize += tbl.getTerms().size();
            
            String loc = tbl.getLocal();
            TbTerm term = (TbTerm) tbl.getTerms().iterator().next();
            String expectedTerm ;
            if ("en".equals(loc))
            {
                expectedTerm = m_termContentEN;
                assertTrue("The "+loc+" term is " + term.getTermContent() + ", but the expected term is " + expectedTerm, 
                           expectedTerm.endsWith(term.getTermContent()));
            }
            else if ("fr".equals(loc))
            {
                expectedTerm = "junitTestFR";
                assertTrue("The "+loc+" term is " + term + ", but the expected term is " + expectedTerm, 
                           expectedTerm.endsWith(term.getTermContent()));
            }
            else if ("de".equals(loc))
            {
                expectedTerm = m_termContentDE;
                assertTrue("The "+loc+" term is " + term + ", but the expected term is " + expectedTerm, 
                           expectedTerm.endsWith(term.getTermContent()));
            }
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
        m_instance = new EntryCreationOverWriteByLanguage(m_fileType);
        m_instance.setFileType(m_fileType);
        
        m_options = new SyncOptions();
        m_options.setSyncMode(SyncOptions.SYNC_BY_LANGUAGE);
        m_options.setSyncLanguage("English");
        m_options.setSyncAction(SyncOptions.SYNC_OVERWRITE);
        m_options.setNosyncAction(SyncOptions.NOSYNC_ADD);  
        m_instance.setSynchronizeOption(m_options);
    }

}
