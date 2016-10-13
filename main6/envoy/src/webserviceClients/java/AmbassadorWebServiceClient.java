/* Copyright (c) 2004-2005, GlobalSight Corporation.  All rights reserved.
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.globalsight.www.webservices.AmbassadorServiceLocator;
import com.globalsight.www.webservices.AmbassadorService;
import com.globalsight.www.webservices.Ambassador;

import org.apache.axis.AxisProperties;

/**
 * Simple Java EXAMPLE client that shows how to use
 * the web services from java. This program should not
 * actually be used itself by anyone.
 * 
 * This code should be given out as an example of how
 * one could write Java code to call our web services.
 */
public class AmbassadorWebServiceClient
{
    private String m_wsdlUrl = null;
    private String m_accessToken = null;
    private Ambassador m_ambassador = null;

    /**
     * Creates an AmbassadorWebServiceClient object
     * 
     * @param p_wsdlUrl  WSDL URL to access Ambassador web services
     */
    public AmbassadorWebServiceClient(String p_wsdlUrl)
    throws Exception
    {
        m_wsdlUrl = p_wsdlUrl;
        AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
        m_ambassador = loc.getAmbassadorWebService(new URL(m_wsdlUrl));
    }

    /**
     * Main test program.
     * See usage message below from printUsage()
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        if (args.length == 0 || args.length < 5)
        {
            printUsage();
            return;
        }
        String hostname = args[0];
        String port = args[1];
        String username = args[2];
        String password = args[3];
        String command = args[4];
        String ssl = null;
        if (port.equalsIgnoreCase("7002"))
        {
            ssl = "ssl";
        } 
        String wsdlUrl;
        if ((ssl != null) && (ssl.equalsIgnoreCase("ssl"))) {
            wsdlUrl = "https://" + hostname + ":" + port +
            "/globalsight/services/AmbassadorWebService?wsdl";
        } else {
            wsdlUrl = "http://" + hostname + ":" + port +
            "/globalsight/services/AmbassadorWebService?wsdl";
        }
        
        try
        {
            AxisProperties.setProperty("axis.socketSecureFactory","org.apache.axis.components.net.SunFakeTrustSocketFactory");
            AmbassadorWebServiceClient client =
            new AmbassadorWebServiceClient(wsdlUrl);

            if (command.equals("hello"))
            {
                client.helloWorld();
                return;
            }

            //everything else needs to login first
            client.login(username,password);

            if (command.equals("queryFP"))
            {
                client.getFileProfileInformation();
            }
            else if (command.equals("submitDoc"))
            {
                client.submitDocument(args);
            }
            else if (command.equals("getStatus"))
            {
                client.getStatus(args);
            }
            else if (command.equals("getJobAndWorkflowInfo"))
            {
                client.getJobAndWorkflowInfo(args);
            }
            else if (command.equals("getLocalizedDocs"))
            {
                client.getLocalizedDocuments(args);
            }
            else if (command.equals("cancelWorkflow"))
            {
                client.cancelWorkflow(args);
            }
            else if (command.equals("exportWorkflow"))
            {
                client.exportWorkflow(args);
            }
            else if (command.equals("cancelJob"))
            {
                client.cancelJob(args);
            }
            else if (command.equals("cancelJobById"))
            {
                client.cancelJobById(args);
            }
            else if (command.equals("exportJob"))
            {
                client.exportJob(args);
            }
            else if (command.equals("getAllLocalePairs"))
            {
                client.getAllLocalePairs();
            }
            else if (command.equals("getAllActivityTypes"))
            {
                client.getAllActivityTypes();
            }
            else if (command.equals("getAllProjects"))
            {
                client.getAllProjects();
            }
            else if (command.equals("getAllProjectsByUser"))
            {
                client.getAllProjectsByUser();
            }
            else if (command.equals("getAllUsers"))
            {
                client.getAllUsers();
            }
            else if (command.equals("getUserInfo"))
            {
                client.getUserInfo(args);
            }
            else if (command.equals("getAcceptedTasks"))
            {
                client.getAcceptedTasksInWorkflow(args);
            }
            else if (command.equals("getTasksInJob"))
            {
                client.getTasksInJob(args);
            }
            else if (command.equals("getCurrentTasksInWorkflow"))
            {
                client.getCurrentTasksInWorkflow(args);
            }
            else if (command.equals("addComment"))
            {
                client.addComment(args);
            }
            else if (command.equals("getUserUnavailabilityReport"))
            {
                client.getUserUnavailabilityReport(args);
            } 
            else if (command.equals("saveDCTMAccount"))
            {
                client.saveDCTMAccount(args);
            }
            else if (command.equals("getFileProfileInfoEx"))
            {
                client.getFileProfileInfoEx(args);
            }
            else if(command.equals("createDocumentumJob"))
            {
                client.createDocumentumJob(args);
            }
            else if(command.equals("cancelDocumentumJob"))
            {
                client.cancelDocumentumJob(args);
            }
            else
            {
                printUsage();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Failed to run web service client with exception " + e);
        }
    }


    private static void printUsage()
    {
        System.out.println("USAGE: AmbassadorWebServiceClient <hostname> <port> <username> <password> <command>");
        System.out.println("\tWhere command is one of the following:");
        System.out.println("\thello -- Causes the web service to say hello. Simple connectivity test.");
        System.out.println("\tqueryFP -- queries file profile information.Returns XML describing the file profiles.");
        System.out.println("\tgetAllLocalePairs - queries all locale pairs information.Returns the locale pairs");
        System.out.println("\tgetAllProjects - queries all projects and returns the projects in the system");
        System.out.println("\tgetAllProjectsByUser - queries for those projects with logged in user.Returns those project for the given user");
        System.out.println("\tgetAllActivityTypes - queries all the activities in the system.\r\n\t\tReturns all the activities in the system");
        System.out.println("\tgetAllUsers - queries all the users in the system");
        System.out.println("\tgetUserInfo <userId> - returns basic information about the specified user.");
        System.out.println("\tsubmitDoc <jobname> <path> <fileProfileId> -- submits the given doc identified\r\n\t\tby full path for l10n with the file profile.");
        System.out.println("\tgetStatus <jobname> -- queries job status for the given job.");
        System.out.println("\tgetJobAndWorkflowInfo <jobId> -- returns general information about a job and its workflows.");
        System.out.println("\tgetLocalizedDocs <jobname>-- returns XML describing URLs that can\r\n\t\tbe used to download the localized docs.");
        System.out.println("\texportWorkflow <jobname> <locale> -- exports all pages of the\r\n\t\tgiven workflow for the job.");
        System.out.println("\texportJob <jobname> -- exports all pages of all workflows for the job.");
        System.out.println("\tcancelWorkflow <jobname> <locale> -- cancels the given workflow\r\n\t\tfor the job.");
        System.out.println("\tcancelJob <jobname> -- cancels the given job.");
        System.out.println("\tcancelJobById < jobId> -- cancels the given job.");
        System.out.println("\tgetAcceptedTasks <workflowId> - returns all the tasks that have been accepted in the specified workflow.");
        System.out.println("\tgetTasksInJob <jobId> <taskName> - returns all the tasks with the given name for the job with the specified id.");
        System.out.println("\tgetCurrentTasksInWorkflow <workflowId> - returns all the current tasks (active and accepted) for the workflow with the specified id.");
        System.out.println("\taddComment <objectId> <objectType> <user id> <comment text> [<fileAttachment1> <R|G> <fileAttachment2> <R|G> ...]");
        System.out.println("             - adds the comment and any attachments to the task or job specified.");
        System.out.println("             - objecType (1=Job, 3=Task)");
        System.out.println("\tgetUserUnavailabilityReport <activityName> <source locale> <target Locale> <month (0 to 11 for Jan-Dec)> <year> - returns unavailability of the users for the specified activity and locale pair.");
        System.out.println("\tsaveDTCMAccount <docBse> <dctmUserName> <dctmUserPassword> -- save Documentum user account in Ambassador. [for *Documentum*webtop]");
        System.out.println("\tgetFileProfileInfoEx -- Queries file profile information,[for *Documentum*webtop]");
        System.out.println("\tcreateDocumentumJob <jobName> <fileProfileId> <objectId> <userId> -- create job. [for *Documentum*webtop]");
        System.out.println("\tcancelDocumentumJob <objectId> <jobId> <userId> -- cancel job. [for *Documentum*webtop]");
        System.out.println("Each command except hello first automatically\r\n\t\tinvokes Login() on the webservice.");
        System.out.println("*NOTE* -- you must use file profile ID, and not name.\r\nAn example ID would be 1001.");
    }

    /**
     * Calls the helloWorld web service
     * 
     * @return String
     */
    private void helloWorld() throws Exception
    {
        String rv = m_ambassador.helloWorld();
        System.out.println(rv);
    }

    /**
     * Logs into the web service
     * 
     * @param p_username
     * @param p_password
     * @return 
     * @exception Exception
     */
    private void login (String p_username, String p_password) throws Exception
    {
        m_accessToken =  m_ambassador.login(p_username,p_password);
    }

    /**
     * Submits the given document for localization
     * 
     * @param p_accessToken
     * @param p_filename
     * @param p_jobName
     * @param p_fileBytes
     * @param p_fileProfileId
     * @return status xml
     * @exception Exception
     */
    private void submitDocument(String[] p_args) throws Exception
    {
        if (p_args.length != 8)
        {
            printUsage();
            return;
        }
        String jobName = p_args[5];
        String filename = p_args[6];
        File file = new File(filename);
        int len = (int)file.length();
        byte[] fileBytes = new byte[len];
        FileInputStream fis = new FileInputStream(file);
        fis.read(fileBytes,0,len);
        fis.close();
        String fileProfileId = p_args[7];

        String rv = m_ambassador.submitDocument(
            m_accessToken, filename, jobName, fileBytes, fileProfileId);
        System.out.println("Document submitted. Your status is:\r\n" + rv);
    }

    /**
     * Calls the getStatus web service
     * 
     * @param p_accessToken
     * @param p_jobname
     * @return 
     * @exception Exception
     */
    private void getStatus(String[] p_args)
    throws Exception
    {
        if (p_args.length != 6)
        {
            printUsage();
            return;
        }
        String jobname = p_args[5];
        String rv = m_ambassador.getStatus(m_accessToken,jobname);
        System.out.println(rv);
    }

    /**
     * Calls the getLocalziedDocuments web service
     * 
     * @param p_args
     */
    private void getLocalizedDocuments(String[] p_args) throws Exception
    {
        if (p_args.length != 6)
        {
            printUsage();
            return;
        }
        String jobname = p_args[5];
        String rv = m_ambassador.getLocalizedDocuments(m_accessToken,jobname);
        System.out.println(rv);
    }

    /**
     * Calls the cancelWorkflow web service
     * 
     * @param p_args
     */
    private void cancelWorkflow(String[] p_args) throws Exception
    {
        if (p_args.length != 7)
        {
            printUsage();
            return;
        }
        String jobname = p_args[5];
        String workflow= p_args[6];
        String rv = m_ambassador.cancelWorkflow(m_accessToken,jobname,workflow);
        System.out.println(rv);
    }

    /**
 * Calls the exportWorkflow web service
 * 
 * @param p_args
 */

    private void exportWorkflow(String[] p_args) throws Exception
    {
        if (p_args.length != 7)
        {
            printUsage();
            return;
        }
        String jobname = p_args[5];
        String workflow= p_args[6];
        String rv = m_ambassador.exportWorkflow(m_accessToken,jobname,workflow);
        System.out.println(rv);
    }

    /**
     * Calls the cancelJob web service
     * 
     * @param p_args
     * @exception Exception
     */
    private void cancelJob(String[] p_args) throws Exception
    {
        if (p_args.length !=6)
        {
            printUsage();
            return;
        }
        String jobname = p_args[5];
        String rv = m_ambassador.cancelJob(m_accessToken,jobname);
        System.out.println(rv);
    }

    /**
     * Calls the cancelJobById web service
     * 
     * @param p_args
     * @exception Exception
     */
    private void cancelJobById(String[] p_args) throws Exception
    {
        if (p_args.length != 6)
        {
            printUsage();
            return;
        }
        long jobId = Long.parseLong(p_args[5]);
        String rv = m_ambassador.cancelJobById(m_accessToken,jobId);
        System.out.println(rv);
    }

    /**
     * Calls the exportJob web service
     * 
     * @param p_args
     * @exception Exception
     */
    private void exportJob(String[] p_args) throws Exception
    {
        if (p_args.length != 6)
        {
            printUsage();
            return;
        }
        String jobname = p_args[5];
        Object[] wsargs = new Object[] {m_accessToken,jobname};
        String rv = m_ambassador.exportJob(m_accessToken,jobname);
        System.out.println(rv);
    }

    /**
     * Calls getUserInfo web service
     * 
     * @param p_args
     * @exception Exception
     */
    private void getUserInfo(String[] p_args) throws Exception
    {
        if (p_args.length !=6)
        {
            printUsage();
            return;
        }
        String userId = p_args[5];
        Object[] wsargs = new Object[] {m_accessToken,userId};
        String rv = m_ambassador.getUserInfo(m_accessToken,userId);
        System.out.println(rv);
    }


    /**
     * Calls getAcceptedTasksInWorkflow
     * 
     * @param p_args
     * @exception Exception
     */
    private void getAcceptedTasksInWorkflow(String[] p_args) throws Exception
    {
        if (p_args.length != 6)
        {
            printUsage();
            return;
        }
        long workflowId = Long.parseLong(p_args[5]);
        String rv = m_ambassador.getAcceptedTasksInWorkflow(m_accessToken,workflowId);
        System.out.println(rv);
    }

    /**
     * Calls getCurrentTasksInWorkflow
     * 
     * @param p_args
     * @exception Exception
     */ 
    private void getCurrentTasksInWorkflow(String[] p_args) throws Exception
    {
        if (p_args.length != 6)
        {
            printUsage();
            return;
        }
        long workflowId = Long.parseLong(p_args[5]);
        String rv = m_ambassador.getCurrentTasksInWorkflow(m_accessToken,workflowId);
        System.out.println(rv);
    }

    /**
     * Calls getTasksInJob
     * 
     * @param p_args
     * @exception Exception
     */ 
    private void getTasksInJob(String[] p_args) throws Exception
    {
        if (p_args.length != 7)
        {
            printUsage();
            return;
        }
        long jobId = Long.parseLong(p_args[5]);
        String taskName = p_args[6];
        String rv = m_ambassador.getTasksInJob(m_accessToken,jobId,taskName);
        System.out.println(rv);
    }

    /**
     * Calls the add comment web service
     * 
     * @param p_args
     * @exception Exception
     */
    private void addComment(String[] p_args) throws Exception
    {
        if (p_args.length  < 9)
        {
            printUsage();
            return;
        }

        long objectId = Long.parseLong(p_args[5]);
        int objectType = Integer.parseInt(p_args[6]); 
        String userId = p_args[7];
        String commentText = p_args[8];
        List files = new ArrayList();
        List accessList = new ArrayList();
        // get the files and their access
        int index = 9;
        while (index < p_args.length )
        {
            String filePath = p_args[index];
            File f = new File(filePath);
            // just verify that the file exists before adding
            // the file name to the argument list
            if (f.exists())
            {
                files.add(filePath);
                index++;
                if (index < p_args.length)
                {
                    String access = p_args[index];
                    if ("R".equals(access))
                    {
                        accessList.add("Restricted");
                    }
                    else
                    {
                        accessList.add("General");
                    }
                }
                else
                {
                    accessList.add("General");
                }
            }
            else
            {
                System.err.println("File " + filePath + " doesn't exist to attach to comment.");
                return;
            } 
            index++;
        }

        String rv = m_ambassador.addComment(
            m_accessToken, objectId, objectType, userId, commentText,
            files.toArray(), accessList.toArray());
        System.out.println(rv);
    }

    /**
    * Gets the Job and Workflow info
    */
    private void getJobAndWorkflowInfo(String[] p_args)
    throws Exception
    {
        if (p_args.length != 6)
        {
            printUsage();
            return;
        }
        long jobId = Long.parseLong(p_args[5]);
        String rv = m_ambassador.getJobAndWorkflowInfo(m_accessToken,jobId);
        System.out.println(rv);
    }

    private void getFileProfileInformation()
    throws Exception
    {
        System.out.println(m_ambassador.getFileProfileInformation(m_accessToken));
    }

    private void getAllLocalePairs()
    throws Exception
    {
        System.out.println(m_ambassador.getAllLocalePairs(m_accessToken));
    }

    private void getAllActivityTypes()
    throws Exception
    {
        System.out.println(m_ambassador.getAllActivityTypes(m_accessToken));
    }

    private void getAllProjects()
    throws Exception
    {
        System.out.println(m_ambassador.getAllProjects(m_accessToken));
    }

    private void getAllProjectsByUser()
    throws Exception
    {
        System.out.println(m_ambassador.getAllProjectsByUser(m_accessToken));
    }

    private void getAllUsers()
    throws Exception
    {
        System.out.println(m_ambassador.getAllUsers(m_accessToken));
    }

    /**
     * Calls getUserUnavailabilityReport
     * 
     * @param p_args
     * @exception Exception
     */ 
    private void getUserUnavailabilityReport(String[] p_args) throws Exception
    {
        if (p_args.length != 10)
        {
            printUsage();
            return;
        }
        String activityName = p_args[5];
        String sourceLocale = p_args[6];
        String targetLocale = p_args[7];
        int month = Integer.parseInt(p_args[8]);
        int year = Integer.parseInt(p_args[9]);
        String result = m_ambassador.getUserUnavailabilityReport(
            m_accessToken,activityName, sourceLocale, targetLocale,
            month, year);
        System.out.println(result);
    }

    private void createDocumentumJob(String[] p_args) throws Exception
    {
        if (p_args.length != 9 )
        { 
           printUsage();
           return;
        }
        String  jobName = p_args[5];
        String fileProfileId = p_args[6];
        String objectId = p_args[7];
        String userId = p_args[8];
        String result = "create sucess"; 
            m_ambassador.createDocumentumJob(m_accessToken,
                jobName, fileProfileId, objectId, userId);
        System.out.println(result);
        
    }

    private void getFileProfileInfoEx(String[] p_args) throws Exception
    {
        System.out.println(m_ambassador.getFileProfileInfoEx(m_accessToken));      
    }

    private void saveDCTMAccount(String[] p_args) throws Exception
    {
        if (p_args.length !=8 )
        { 
           printUsage();
           return;
        }
        String  docBase = p_args[5];
        String userName = p_args[6];
        String userPassword = p_args[7];
        String result = m_ambassador.passDCTMAccount(m_accessToken,
                docBase, userName, userPassword);
        System.out.println(result);
    }
    
    private void cancelDocumentumJob(String[] p_args) throws Exception
    {
        if (p_args.length !=8 )
        {
            printUsage();
            return;
        }
        String objectId = p_args[5];
        String jobId = p_args[6];
        String userId = p_args[7];
        String result = "cancel sucess";
            m_ambassador.cancelDocumentumJob(m_accessToken, 
                objectId, jobId, userId);
        System.out.println(result);
    }
}

