/**
 * Copyright 2009,2012 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.everest.edit.offline;

import java.util.HashMap;
import java.util.Hashtable;

/**
 * @author VincentYan
 *
 */
public class OfflineFileUploadStatus
{
    private static OfflineFileUploadStatus fileStatusInstance = null;
    private static Hashtable<Long, HashMap<String, String>> taskFileStates = new Hashtable<Long, HashMap<String, String>>();
    private static Hashtable<String, String> filenameAlias = new Hashtable<String, String>();
    
    private OfflineFileUploadStatus() {
    }
    
    public static OfflineFileUploadStatus getInstance() {
        if (fileStatusInstance == null)
            fileStatusInstance = new OfflineFileUploadStatus();
        return fileStatusInstance;
    }
    
    public static void addFilenameAlias(String ori, String alias) {
        filenameAlias.put(alias, ori);
    }
    
    public static void addFileState(Long taskId, String filename, String state) {
        HashMap<String, String> fileStates = null;
        if (filenameAlias.containsKey(filename))
            filename = filenameAlias.get(filename);
        
        if (taskFileStates.containsKey(taskId)) {
            fileStates = taskFileStates.get(taskId);
            fileStates.put(filename, state);
        } else {
            fileStates = new HashMap<String, String>();
            fileStates.put(filename, state);
        }
        taskFileStates.put(taskId, fileStates);
    }
    
    public static HashMap<String, String> getFileStates(Long taskId) {
        return taskFileStates.get(taskId);
    }
    
    public static String getFilestate(Long taskId, String filename) {
        HashMap<String, String> fileStates = null;
        fileStates = taskFileStates.get(taskId);
        
        return (fileStates == null) ? null : fileStates.get(filename);
    }
    
    public static void clearTaskFileState(Long taskId) {
        if (taskFileStates.containsKey(taskId))
            taskFileStates.remove(taskId);
    }
}
