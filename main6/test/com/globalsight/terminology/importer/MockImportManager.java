package com.globalsight.terminology.importer;

import java.util.ArrayList;

import com.globalsight.importer.ImporterException;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Termbase;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.SessionInfo;

public class MockImportManager extends ImportManager
{
    private static final long serialVersionUID = -8105336404421416266L;
    
    //
    // Constructor
    //
    public MockImportManager(Termbase p_database, SessionInfo p_session)
            throws ImporterException
    {
        super(p_database, p_session);
    }

    public MockImportManager(Termbase p_database, SessionInfo p_session,
            String p_filename, String p_options) throws ImporterException
    {
        super(p_database, p_session, p_filename, p_options);
    }
    
    public ArrayList<Entry> getEntryList()
    {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        ReaderResult result;
        while (m_reader.hasNext())
        {
            result = m_reader.next();
            entries.add((Entry) result.getResultObject());
        }

        return entries;
    }

}
