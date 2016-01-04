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

package com.globalsight.webservices.attribute;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeManager;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.action.AttributeAction;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.webservices.WebServicesLog;

public class AddJobAttributeThread extends Thread
{
    static private final Logger s_logger = Logger
            .getLogger(AddJobAttributeThread.class);

    private String jobUuid;
    private String companyId;
    private List<JobAttributeVo> vos;
    private List<JobAttribute> jobAttributes = new ArrayList<JobAttribute>();
    public static Map<String, byte[]> LOCKS = new HashMap<String, byte[]>();

    public AddJobAttributeThread(String jobUuid, String companyId)
    {
        this.jobUuid = jobUuid;
        this.companyId = companyId;
    }

    public void createJobAttributes()
    {
        if (jobAttributes.size() == 0 && vos != null)
        {
            for (JobAttributeVo vo : vos)
            {
                jobAttributes.add(AttributeUtil.createJobAttribute(vo));
            }
        }

        this.start();
    }

    public static byte[] getLock(String uuid)
    {
        if (uuid == null)
            return new byte[1];

        byte[] lock = LOCKS.get(uuid);
        if (lock == null)
        {
            lock = new byte[1];
            LOCKS.put(uuid, lock);
        }

        return lock;
    }

    @Override
    public void run()
    {
        synchronized (getLock(jobUuid))
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put(CompanyWrapper.CURRENT_COMPANY_ID, companyId);
            activityArgs.put("jobUuid", jobUuid);
            WebServicesLog.Start activityStart = WebServicesLog.start(
                    AddJobAttributeThread.class, "run", activityArgs);
            try
            {
                String hql = "from JobImpl j where j.uuid = :uuid and j.companyId = :companyId";
                Map map = new HashMap();
                map.put("uuid", jobUuid);
                map.put("companyId", Long.parseLong(companyId));
                int i = 0;
                while (i < 600)
                {
                    try
                    {
                        sleep(3000);
                    }
                    catch (InterruptedException e1)
                    {
                        s_logger.error(e1.getMessage(), e1);
                    }

                    i++;
                    JobImpl job = (JobImpl) HibernateUtil.getFirst(hql, map);

                    if (job != null)
                    {
                        List<AttributeAction> actions = AttributeManager
                                .getAttributeActions();
                        CompanyThreadLocal.getInstance().setIdValue(companyId);
                        for (JobAttribute jobAttribute : jobAttributes)
                        {
                            HibernateUtil.save(jobAttribute.getAttribute());
                            jobAttribute.setJob(job);
                            HibernateUtil.save(jobAttribute);

                            if (Attribute.TYPE_FILE.equals(jobAttribute
                                    .getType()))
                            {
                                String path = jobUuid + "/"
                                        + jobAttribute.getAttribute().getName();
                                File root = new File(
                                        AmbFileStoragePathUtils
                                                .getJobAttributeDir(),
                                        path);
                                if (root.exists())
                                {
                                    File[] files = root.listFiles();
                                    for (File file : files)
                                    {
                                        jobAttribute.addFile(file);
                                    }
                                }

                                root.delete();
                            }

                            for (AttributeAction action : actions)
                            {
                                action.run(jobAttribute);
                            }
                        }

                        break;
                    }
                }
            }
            catch (Exception e)
            {
                s_logger.error(e.getMessage(), e);
            }
            finally
            {
                HibernateUtil.closeSession();
                activityStart.end();
            }
        }
    }

    public void setJobAttributeVos(List<JobAttributeVo> vos)
    {
        this.vos = vos;
    }

    public void setJobAttributes(List<JobAttribute> jobAttributes)
    {
        this.jobAttributes = jobAttributes;
    }
}
