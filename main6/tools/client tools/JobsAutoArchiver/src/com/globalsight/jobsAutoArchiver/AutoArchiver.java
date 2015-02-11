/**
 *  Copyright 2014 Welocalize, Inc. 
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
package com.globalsight.jobsAutoArchiver;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.globalsight.util.ConfigUtil;
import com.globalsight.util.ConfigBO;
import com.globalsight.util.LogUtil;
import com.globalsight.util.WebServiceClientHelper;
import com.globalsight.www.webservices.Ambassador;

import java.util.Iterator;

/**
 * Used for Archive Exported Jobs.
 */
public class AutoArchiver
{
	public void doWork() throws Exception
    {
        ConfigBO config = ConfigUtil.getConfigBO();

        String hostName = config.getHostName();
        int port = config.getPort();
        String userName = config.getUserName();
        String password = config.getPassword();
        boolean isUseHTTPS = config.isHttpsEnabled();
        int intervalTimeForArchive = config.getIntervalTimeForArchive();

        Ambassador ambassador = WebServiceClientHelper.getClientAmbassador(
                hostName, port, userName, password, isUseHTTPS);

        String accessToken = ambassador.login(userName, password);
        LogUtil.info("Connect to Server: " + config.getHostName());

        String result = ambassador.fetchJobsByState(accessToken,
                Constants.JOB_STATE_EXPORTED, 0, 100, false);
        if (result == null)
        {
            LogUtil.info("No jobs that are in exported state.");
            return;
        }

        LogUtil.info("Returning of fetchJobsByState API:\n" + result);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
        long diffInHours = 0;
        long now = (new Date()).getTime();

        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new ByteArrayInputStream(result
                .getBytes("UTF-8")));
        Element rootElt = document.getRootElement();

        Set<Long> jobIds = new HashSet<Long>();
        Iterator iter = rootElt.elementIterator("Job");
        while (iter.hasNext())
        {
            String jobId = null;
            String completedDateStr = null;
            try
            {
                Element jobElement = (Element) iter.next();
                jobId = jobElement.elementTextTrim("id");
                completedDateStr = jobElement
                        .elementTextTrim("completedDate");

                boolean isMorning = true;
                int index = completedDateStr.toUpperCase().indexOf("AM");
                if (index == -1)
                {
                    index = completedDateStr.toUpperCase().indexOf("PM");
                    isMorning = false;
                }
                Date completedDate = sdf.parse(completedDateStr.substring(
                        0, index).trim());

                if (!isMorning)
                {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(completedDate);
                    cal.add(Calendar.HOUR_OF_DAY, +12);
                    completedDate = cal.getTime();
                }
                long completedDateLong = completedDate.getTime();

                diffInHours = (now - completedDateLong) / 1000 / 60 / 60;
                if (diffInHours >= intervalTimeForArchive)
                {
                    jobIds.add(Long.parseLong(jobId));
                }
            }
            catch (Exception e)
            {
                LogUtil.info("Error to check job with jobID: " + jobId
                        + " and completedDate '" + completedDateStr + "'.", e);
            }
        }

        StringBuffer jobs = new StringBuffer();
        for (long id : jobIds)
        {
            jobs.append(id).append(",");
        }

        if (jobs.length() > 0 && jobs.toString().endsWith(","))
        {
            String jobs2 = jobs.toString().substring(0, jobs.length() - 1);
            LogUtil.info("Jobs that can be archived: " + jobs2);

            ambassador.archiveJob(accessToken, jobs2);
        }
        else
        {
            LogUtil.info("No jobs that can be archived.");
        }
	}

    public static void main(String[] args) throws Exception
    {
        ConfigBO config = ConfigUtil.getConfigBO();
        int intervalTime = config.getIntervalTime();

        AutoArchiver test = new AutoArchiver();
        while (true)
        {
            try 
            {
				test.doWork();
			} 
            catch (Exception e) 
            {
            	LogUtil.info("Error: "+e);
			}

            Thread.sleep(1000 * 60 * 60 * intervalTime);
            LogUtil.info("===================================================");
            LogUtil.info("===================================================");
        }
    }
}
