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
package com.globalsight.everest.webapp.pagehandler.edit.online;

import java.io.File;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

public class DeleteOldPdfThread implements Runnable
{
    private String userid = null;
    private Logger logger = null;
    
    public DeleteOldPdfThread(String userid, Logger logger)
    {
        this.userid = userid;
        this.logger = logger;
    }

    @Override
    public void run()
    {
        try
        {
            User user = ServerProxy.getUserManager().getUser(userid);
            String companyName = user.getCompanyName();
            String company_id = CompanyWrapper.getCompanyIdByName(companyName);

            File previewDir = AmbFileStoragePathUtils.getPdfPreviewDir(company_id);
            String userPreviewDir = previewDir + File.separator + userid;
            FileUtils.deleteAllFilesSilently(userPreviewDir);
        }
        catch (Exception e)
        {
            logger.error("Could not delete all preview files for user : " + userid);
            throw new EnvoyServletException(e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}
