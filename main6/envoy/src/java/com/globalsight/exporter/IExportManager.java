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

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.globalsight.util.progress.IProcessStatusListener;

/**
 * <p>The RMI interface for the Termbase Exporter.</p>
 */
public interface IExportManager
    extends Remote
{
    static final public String EXPORT_DIRECTORY = "_Exports_";

    /**
     * Attaches an export event listener.  Currently, only a single
     * listener is supported.
     */
    void attachListener(IProcessStatusListener p_listener)
        throws RemoteException;

    /**
     * Detaches an export event listener.  Currently, only a single
     * listener is supported.
     */
    void detachListener(IProcessStatusListener p_listener)
        throws RemoteException;

    /**
     * Sets the export options that guide the export process.
     * @param options an XML string.
     */
    void setExportOptions(String options)
        throws ExporterException, RemoteException;

    /**
     * Returns ExportOptions as XML string.
     */
    String getExportOptions()
        throws ExporterException, RemoteException;

    ExportOptions getExportOptionsObject() throws ExporterException,
            RemoteException;

    /**
     * Gets the name of the file to export to.
     */
    String getExportFile()
        throws ExporterException, RemoteException;

    /**
     * Analyzes export options and termbase and returns a count of how
     * many entries will be exported.
     *
     * For CSV files: also analyzes the columns and proposes column types.
     *
     * @return newly computed ExportOptions as XML string.
     */
    String analyze()
        throws ExporterException, RemoteException;
    
    String analyzeTm()
            throws ExporterException, RemoteException;
    
    /**
     * With all ExportOptions set, start the actual export.  During
     * export, a registered IProcessStatusListener receives status events and
     * error messages.
     */
    void doExport()
        throws ExporterException;
}

