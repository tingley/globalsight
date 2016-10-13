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

package com.globalsight.exporter;

import com.globalsight.exporter.ExportOptions;
import com.globalsight.util.SessionInfo;

import java.util.ArrayList;

import java.io.IOException;

/**
 * Interface for export file writers. Modeled after iterators and the
 * Producer-Consumer pattern.
 */
public interface IWriter
{
    /**
     * Sets new export options for this writer.
     */
    void setExportOptions(ExportOptions options);

    /**
     * Analyzes export options and returns an updated ExportOptions
     * object with a status whether the options are syntactically
     * correct.
     */
    ExportOptions analyze();

    /**
     * Writes the file header (eg for TBX).
     */
    void writeHeader(SessionInfo p_session)
        throws IOException;

    /**
     * Writes the file trailer (eg for TBX).
     */
    void writeTrailer(SessionInfo p_session)
        throws IOException;

    /**
     * Writes the next few entries to the file.
     *
     * @param p_entries ArrayList of Entry objects.
     * @see Entry
     */
    void write(ArrayList p_entries, SessionInfo p_session)
        throws IOException;

    /**
     * Writes a single entry to the export file.
     *
     * @see Entry
     */
    void write(Object p_entry, SessionInfo p_session)
        throws IOException;
}
