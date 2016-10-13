/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.terminology.command;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.globalsight.terminology.Definition;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.util.SessionInfo;

public interface EntryOperation
{
    public static final Logger CATEGORY = Logger
            .getLogger(Termbase.class);
    public final static String MSG_YOU_DONT_OWN_LOCK = "you_dont_own_lock";
    public final static String MSG_ENTRY_NOT_LOCKED = "entry_not_locked";

    public String getEntry(long p_entryId, long p_termId, String p_srcLang,
            String p_trgLang, SessionInfo p_session) throws TermbaseException;

    public String getEntry(long p_entryId, String fileType,
            SessionInfo p_session) throws TermbaseException;

    public String getEntryForBrowser(long p_entryId, SessionInfo p_session)
            throws TermbaseException;

    public String getEntryForExport(long tbid, long p_entryId, long p_termId,
            String p_srcLang, String p_trgLang, SessionInfo p_session)
            throws TermbaseException;

    public String getTbxEntryForExport(long tbid, long p_entryId, long p_termId,
            String p_srcLang, String p_trgLang, SessionInfo p_session)
            throws TermbaseException;

    public long addEntry(long tb_id, String p_entry, Definition m_definition,
            SessionInfo p_session) throws TermbaseException;

    public void updateEntry(long p_entryId, Entry p_newEntry,
            SessionInfo p_session) throws TermbaseException;

    public void deleteEntry(long p_entryId, SessionInfo p_session)
            throws TermbaseException;

    public String validateEntry(Definition m_definition, long tb_id,
            String p_entry, SessionInfo p_session) throws TermbaseException;

    public String lockEntry(long tb_id, long p_entryId, boolean p_steal,
            HashMap m_entryLocks, SessionInfo p_session)
            throws TermbaseException;

    public void unlockEntry(long p_entryId, String p_cookie,
            HashMap m_entryLocks, SessionInfo p_session)
            throws TermbaseException;
}
