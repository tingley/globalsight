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
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.globalsight.util.LogUtil;
import com.globalsight.util.WebServiceClientHelper;
import com.globalsight.www.webservices.Ambassador;

import java.util.Iterator;

/**
 * Used for Archive Exported Jobs.
 */
public class AutoArchiver extends Thread
{
    static int intervalTime;
    static String runOnce;

    public void doWork() throws Exception
    {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader
                .read(new File(Constants.CONFIG_FILE_NAME));
        Element rootElt = document.getRootElement();
        runOnce = document.selectSingleNode("//runOnce").getText();
        intervalTime = Integer.valueOf(document.selectSingleNode(
                "//intervalTime").getText());
        Iterator serverIter = rootElt.elementIterator("server");
        while (serverIter.hasNext())
        {
            final Element serverElement = (Element) serverIter.next();

            Runnable runnable = new Runnable()
            {
                public void run()
                {
                    try
                    {
                        String hostName = serverElement.elementTextTrim("host");
                        int port = Integer.valueOf(serverElement
                                .elementTextTrim("port"));
                        boolean isUseHTTPS = Boolean.valueOf(serverElement
                                .elementTextTrim("https"));
                        int intervalTimeForArchive = Integer
                                .valueOf(serverElement
                                        .elementTextTrim("intervalTimeForArchive"));
                        Iterator usersIter = serverElement
                                .elementIterator("users");
                        while (usersIter.hasNext())
                        {
                            Element usersElement = (Element) usersIter.next();
                            Iterator userIter = usersElement
                                    .elementIterator("user");
                            while (userIter.hasNext())
                            {
                                Element userElement = (Element) userIter.next();
                                String userName = userElement
                                        .elementTextTrim("username");
                                String password = userElement
                                        .elementTextTrim("password");
                                autoArchive(hostName, port, isUseHTTPS,
                                        userName, password,
                                        intervalTimeForArchive);
                            }
                        }
                    }
                    catch (Throwable e)
                    {
                        LogUtil.info("error : " + e);
                    }
                }
            };
            Thread t = new Thread(runnable);
            t.start();
        }
    }

    private void autoArchive(String hostName, int port, boolean isUseHTTPS,
            String userName, String password, int intervalTimeForArchive)
    {
        try
        {
            Ambassador ambassador = WebServiceClientHelper.getClientAmbassador(
                    hostName, port, userName, password, isUseHTTPS);
            String accessToken = ambassador.login(userName, password);
            String result = ambassador.fetchJobsByState(accessToken,
                    Constants.JOB_STATE_EXPORTED, 0, 100, false);
            if (result == null)
            {
                LogUtil.info("server " + hostName + " , user " + userName
                        + " : no jobs that are in exported state.");
            }
            else
            {
                LogUtil.info("Returning of fetchJobsByState API:\n" + result);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                long diffInHours = 0;
                long now = (new Date()).getTime();

                SAXReader saxReader = new SAXReader();
                Document document2 = saxReader.read(new ByteArrayInputStream(
                        result.getBytes("UTF-8")));
                Element rootEltJob = document2.getRootElement();
                Iterator iterJob = rootEltJob.elementIterator("Job");
                Set<Long> jobIds = new HashSet<Long>();
                while (iterJob.hasNext())
                {
                    String jobId = null;
                    String completedDateStr = null;
                    try
                    {
                        Element jobElement = (Element) iterJob.next();
                        jobId = jobElement.elementTextTrim("id");
                        completedDateStr = jobElement
                                .elementTextTrim("completedDate");
                        Date completedDate = sdf.parse(completedDateStr
                                .substring(0, 18).trim());
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
                                + " and completedDate '" + completedDateStr
                                + "'.", e);
                    }
                }
                StringBuffer jobs = new StringBuffer();
                for (long id : jobIds)
                {
                    jobs.append(id).append(",");
                }
                if (jobs.length() > 0 && jobs.toString().endsWith(","))
                {
                    String jobs2 = jobs.toString().substring(0,
                            jobs.length() - 1);
                    ambassador.archiveJob(accessToken, jobs2);
                    String[] jobs2_array = jobs2.toString().split(",");
                    for (String job : jobs2_array)
                    {
                        LogUtil.info("server " + hostName + ", user "
                                + userName + " : the job " + job
                                + " can be archived");
                    }
                }
                else
                {
                    LogUtil.info("server " + hostName + ", user " + userName
                            + " : no jobs that can be archived.");
                }
            }
        }
        catch (Exception e)
        {
            LogUtil.info("server " + hostName + ", user " + userName
                    + ", error : " + e);
        }
    }

    public static void main(String[] args) throws Exception
    {
        AutoArchiver test = new AutoArchiver();

        while (true)
        {
            test.doWork();

            if (!"yes".equalsIgnoreCase(runOnce))
            {
                Thread.sleep(1000 * 60 * 60 * intervalTime);
            }
            else
            {
                break;
            }
        }
    }
}
