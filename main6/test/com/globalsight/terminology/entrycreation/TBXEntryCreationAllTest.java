package com.globalsight.terminology.entrycreation;

import static org.junit.Assert.assertTrue;

import java.util.List;

import com.globalsight.everest.webapp.WebAppConstants;

/**
 * Import the Terminology TBX Data with the option 
 * "Add all concepts as new concepts"
 * 
 * @see EntryCreationAll
 */
public class TBXEntryCreationAllTest extends XMLEntryCreationAllTest
{
    @Override
    public void testBatchAddEntriesAsNew()
    {
        m_instance.batchAddEntriesAsNew(m_tbid, m_entries, m_session);
        
        List<?> conceptList = getTBConceptList();
        assertTrue("The m_entries size is " + m_entries.size() 
                   + " but the conceptList size is " + conceptList.size(),
                   m_entries.size() == conceptList.size());
    }
    
    @Override
    protected void setFileType()
    {
        m_fileType = WebAppConstants.TERMBASE_TBX;
    }
}
