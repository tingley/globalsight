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
package com.globalsight.terminology.entrycreation;

import java.util.ArrayList;

import com.globalsight.terminology.Entry;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.Termbase.SyncOptions;
import com.globalsight.util.SessionInfo;

public interface IEntryCreation
{
    public void setFileType(String fileType);
    public void batchAddEntriesAsNew(long terbseId, ArrayList p_entries,
            SessionInfo p_session) throws TermbaseException;
    public long addEntry(long terbseId, Entry p_entry,
            SessionInfo p_session) throws TermbaseException;
    public void setSynchronizeOption(SyncOptions p_options);
    
    public ArrayList getFailedEntries();
}
