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
package com.globalsight.cxe.util.fileImport;

import com.globalsight.cxe.message.CxeMessage;

public class FileImportRunnable implements Runnable
{
    private CxeMessage cxeMessage;
    
    public FileImportRunnable(CxeMessage cxeMessage)
    {
        super();
        this.cxeMessage = cxeMessage;
    }
    
    @Override
    public void run()
    {
        FileImportUtil.importFile(getCxeMessage());
    }

    /**
     * @return the cxeMessage
     */
    public CxeMessage getCxeMessage()
    {
        return cxeMessage;
    }

    /**
     * @param cxeMessage the cxeMessage to set
     */
    public void setCxeMessage(CxeMessage cxeMessage)
    {
        this.cxeMessage = cxeMessage;
    }
}
