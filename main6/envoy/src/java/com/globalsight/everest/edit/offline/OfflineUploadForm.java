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
package com.globalsight.everest.edit.offline;

import java.io.File;
import java.io.Serializable;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.taskmanager.Task;

public class OfflineUploadForm implements Serializable
{
    private static final long serialVersionUID = 2950709159545138762L;
    private File tmpFile;
    private User user;
    private Task task;
    private String fileName;
    private OEMProcessStatus status = null;

    public OEMProcessStatus getStatus()
    {
        return status;
    }

    public void setStatus(OEMProcessStatus status)
    {
        this.status = status;
    }

    public OfflineUploadForm(File tmpFile, User user, Task task, String fileName)
    {
        super();
        this.tmpFile = tmpFile;
        this.user = user;
        this.task = task;
        this.fileName = fileName;
    }

    /**
     * @return the tmpFile
     */
    public File getTmpFile()
    {
        return tmpFile;
    }

    /**
     * @param tmpFile
     *            the tmpFile to set
     */
    public void setTmpFile(File tmpFile)
    {
        this.tmpFile = tmpFile;
    }

    /**
     * @return the user
     */
    public User getUser()
    {
        return user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(User user)
    {
        this.user = user;
    }

    /**
     * @return the task
     */
    public Task getTask()
    {
        return task;
    }

    /**
     * @param task
     *            the task to set
     */
    public void setTask(Task task)
    {
        this.task = task;
    }

    /**
     * @return the fileName
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

}
