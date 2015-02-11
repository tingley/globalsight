/**
 * Ambassador.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package com.globalsight.www.webservices;

public interface Ambassador extends java.rmi.Remote {
    public java.lang.String getUserInfo(java.lang.String p_accessToken, java.lang.String p_userId) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String login(java.lang.String p_username, java.lang.String p_password) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getStatus(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public boolean isInstalled() throws java.rmi.RemoteException;
    public java.lang.String getAllLocalePairs(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getAllProjects(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String cancelJob(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String cancelWorkflow(java.lang.String p_accessToken, java.lang.String p_jobName, java.lang.String p_workflowLocale) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String addComment(java.lang.String p_accessToken, long p_objectId, int p_objectType, java.lang.String p_userId, java.lang.String p_comment, java.lang.Object[] p_fileList, java.lang.Object[] p_accessList) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String helloWorld() throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getFileProfileInformation(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getAllActivityTypes(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getAllUsers(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getAllProjectsByUser(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String submitDocument(java.lang.String p_accessToken, java.lang.String p_fileName, java.lang.String p_jobNamePrefix, byte[] p_file, java.lang.String p_fileProfileId) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getJobAndWorkflowInfo(java.lang.String p_accessToken, long p_jobId) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getLocalizedDocuments(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String cancelJobById(java.lang.String p_accessToken, long p_jobId) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String exportWorkflow(java.lang.String p_accessToken, java.lang.String p_jobName, java.lang.String p_workflowLocale) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String exportJob(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getAcceptedTasksInWorkflow(java.lang.String p_accessToken, long p_workflowId) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getCurrentTasksInWorkflow(java.lang.String p_accessToken, long p_workflowId) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getTasksInJob(java.lang.String p_accessToken, long p_jobId, java.lang.String p_taskName) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getUserUnavailabilityReport(java.lang.String p_accessToken, java.lang.String p_activityName, java.lang.String p_sourceLocale, java.lang.String p_targetLocale, int p_month, int p_year) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String passDCTMAccount(java.lang.String p_accessToken, java.lang.String docBase, java.lang.String dctmUserName, java.lang.String dctmPassword) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public java.lang.String getFileProfileInfoEx(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public void createDocumentumJob(java.lang.String p_accessToken, java.lang.String jobName, java.lang.String fileProfileId, java.lang.String objectId, java.lang.String userId) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
    public void cancelDocumentumJob(java.lang.String p_accessToken, java.lang.String objectId, java.lang.String jobId, java.lang.String userId) throws java.rmi.RemoteException, com.globalsight.webservices.WebServiceException;
}
