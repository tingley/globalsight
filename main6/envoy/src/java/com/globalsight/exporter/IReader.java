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
import com.globalsight.util.ReaderResult;

/**
 * Interface for database readers. Modeled after iterators and the
 * Producer-Consumer pattern.
 */
public interface IReader
{
    /**
     * Sets new export options for this reader.
     */
    void setExportOptions(ExportOptions options);

    /**
     * Analyzes export options and termbase and returns an updated
     * ExportOptions object with a status whether the options are
     * syntactically correct, the number of expected entries to be
     * exported, and column descriptors in case of CSV files.
     */
    ExportOptions analyze();

    /**
     * Start reading termbase and producing entries.
     */
    void start();

    /**
     * Lets the reader read in the next entry and returns true if an
     * entry is available, else false.
     */
    boolean hasNext();

    /**
     * Retrieves the next ReaderResult, which is an Entry together
     * with a status code and error message.
     *
     * @see ReaderResult
     * @see Entry
     */
    ReaderResult next();

    /**
     * Stop reading and producing new entries.
     */
    void stop();
}
