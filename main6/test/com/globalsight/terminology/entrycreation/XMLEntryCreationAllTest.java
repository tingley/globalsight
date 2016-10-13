package com.globalsight.terminology.entrycreation;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.terminology.Termbase.SyncOptions;

/**
 * Import the Terminology XML Data with the option 
 * "Add all concepts as new concepts"
 * 
 * @see EntryCreationAll
 */
public class XMLEntryCreationAllTest extends EntryCreationBasic
{
    @Test
    public void testBatchAddEntriesAsNew()
    {
        m_instance.batchAddEntriesAsNew(m_tbid, m_entries, m_session);
        
        List<?> conceptList = getTBConceptList();
        assertTrue("The m_entries size is " + m_entries.size() 
                   + " but the conceptList size is " + conceptList.size(),
                   m_entries.size() == conceptList.size());
    }

    protected void setFileType()
    {
        m_fileType = WebAppConstants.TERMBASE_XML;
    }
    
    protected void initData()
    {
        setFileType();
        m_instance = new EntryCreationAll(m_fileType);
        m_instance.setFileType(m_fileType);
        
        m_options = new SyncOptions();
        m_options.setSyncMode(SyncOptions.SYNC_BY_NONE); 
        m_instance.setSynchronizeOption(m_options);
    }
}
