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
package com.globalsight.everest.webapp.pagehandler.administration.systemActivities.offlineUploadState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.offline.OfflineEditManagerLocal;
import com.globalsight.everest.edit.offline.OfflineUploadForm;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.comparator.OfflineUploadRequestComparator;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.webapp.pagehandler.administration.systemActivities.RequestAbstractHandler;

public class RunningRequestHandler extends RequestAbstractHandler
{

    @Override
    protected void cancelRequest(String key)
    {
//        FileExportUtil.cancelUnimportFile(key);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getAllVos()
    {
        return getAllRequestVos();
    }
    
    private List<Vo> getAllRequestVos()
    {
        List<Vo> forms = new ArrayList<Vo>();
        List<OfflineUploadForm> fs = new ArrayList<OfflineUploadForm>();
        fs.addAll(OfflineEditManagerLocal.RUNNING_FORMS);
        for (OfflineUploadForm f : fs)
        {
            
            Vo v = new Vo();
            Task t = f.getTask();
            if (t != null)
            {
                String companyName = CompanyWrapper.getCompanyNameById(t.getCompanyId());
                v.setCompany(companyName);
            }
            
            v.setFileName(f.getFileName());
            v.setFileSize("" + f.getTmpFile().length());
            if (f.getUser() != null)
            {
                v.setUser(f.getUser().toString());
            }
            
            forms.add(v);
        }

        return forms;
    }

    @Override
    protected StringComparator getComparator(Locale uiLocale)
    {
        return new OfflineUploadRequestComparator(uiLocale);
    }

}
