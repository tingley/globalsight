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

package com.globalsight.everest.page.pageimport;

// globalsight
import java.sql.Connection;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.persistence.pageimport.UpdateUnextractedFileImportCommand;


/**
 * Performs the persistence calls and makes the connections
 * to persist/update the UnextractedFile information during import.
 */
public class UnextractedFileImportPersistenceHandler
{
    private static Logger c_logger =
        Logger.getLogger(
                UnextractedFileImportPersistenceHandler.class);

    //
    // Constructor
    //
    public UnextractedFileImportPersistenceHandler()
    {
    }

    /**
     * Persists the updated information in the un-extracted file within
     * the source page.  Also update the target pages with the same
     * exact information, since it is the same upon import.
     */
    public void persistObjects(SourcePage p_sourcePage)
      throws FileImportException
    {
        Connection connection = null;
        try
        {
            connection = PersistenceService.getInstance().getConnectionForImport();
            connection.setAutoCommit(false);
            UpdateUnextractedFileImportCommand command = 
                           new UpdateUnextractedFileImportCommand(p_sourcePage);
            command.persistObjects(connection);
            connection.commit();
        }
        catch (Exception sqle)
        {
            try
            {
                c_logger.error("Failed to update the unextracted file of source page " +
                               p_sourcePage.getId() + " during import.", sqle); 
                connection.rollback();
                throw new FileImportException(sqle);
            }
            catch(Exception se)
            {
                c_logger.error("Failed to rollback the failed update of an unextracted file.",
                               se);
                String args[] = {Long.toString(p_sourcePage.getId())};
                throw new FileImportException(
                    FileImportException.MSG_FAILED_TO_UPDATE_UNEXTRACTED_FILE_INFO,
                    args, se);
            }
        }
        finally
        {
            // return connection to connection pool
            try
            { 
                PersistenceService.getInstance().returnConnection(connection);
            }
            catch (Exception pe)
            {
                c_logger.error("Failed to return a connection back to the connection pool", pe);
                // don't bother throwing an exception for this.  
            }
        }
    }
}
