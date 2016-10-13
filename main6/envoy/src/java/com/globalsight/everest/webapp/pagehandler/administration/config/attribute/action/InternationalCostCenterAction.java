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

package com.globalsight.everest.webapp.pagehandler.administration.config.attribute.action;

import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;

public class InternationalCostCenterAction implements AttributeAction
{
    static private final Logger logger = Logger
            .getLogger(InternationalCostCenterAction.class);

    private static final String NAME = "protect_international_cost_center";

    public void run(JobAttribute jobAttribute)
    {
        if (jobAttribute == null)
            return;

        if (NAME.equals(jobAttribute.getAttribute().getName()))
        {
            List<String> value = (List<String>) jobAttribute.getValue();
            if (value != null && value.indexOf("yes") > -1)
            {
                try
                {
                    JobImpl job = jobAttribute.getJob();
                    String companyIdStr = String.valueOf(job.getCompanyId());
                    String[] args =
                    { job.getName(), Long.toString(job.getId()),
                            UserUtil.getUserNameById(job.getCreateUserId()) };
                    User user = job.getL10nProfile().getProject()
                            .getProjectManager();
                    if (user != null)
                    {
                        ServerProxy.getMailer().sendMailFromAdmin(user, args,
                                "InternationalCostCenterSubject",
                                "InternationalCostCenterBody", companyIdStr);
                    }
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }
            }

        }
    }
}
