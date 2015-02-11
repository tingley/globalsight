/**
 * Ambassador4Falcon.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.globalsight.www.webservices;

public interface Ambassador4Falcon extends java.rmi.Remote {
    public int modifyUser(java.lang.String p_accessToken, java.lang.String p_userId, java.lang.String p_password, java.lang.String p_firstName, java.lang.String p_lastName, java.lang.String p_email, java.lang.String[] p_permissionGrps, java.lang.String p_status, java.lang.String p_roles, boolean p_isInAllProject, java.lang.String[] p_projectIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public int createUser(java.lang.String p_accessToken, java.lang.String p_userId, java.lang.String p_password, java.lang.String p_firstName, java.lang.String p_lastName, java.lang.String p_email, java.lang.String[] p_permissionGrps, java.lang.String p_status, java.lang.String p_roles, boolean p_isInAllProject, java.lang.String[] p_projectIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getWorkflowTemplateInfo(java.lang.String p_accessToken, java.lang.String p_workflowTemplateName, java.lang.String p_companyName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String taskReassign(java.lang.String p_accessToken, java.lang.String p_taskId, java.lang.String[] p_users) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getJobIDsWithStatusChanged(java.lang.String p_accessToken, int p_intervalInMinute) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getTranslationPercentage(java.lang.String p_accessToken, java.lang.String p_taskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getDetailedWordcounts(java.lang.String p_accessToken, java.lang.String[] p_jobIds, java.lang.Boolean p_includeMTData) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getWorkflowTemplateNames(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String modifyWorkflowTemplateAssignees(java.lang.String p_accessToken, java.lang.String p_workflowTemplateName, java.lang.String p_companyName, java.lang.String p_activityAssigneesInJson) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getWorkOfflineFiles(java.lang.String p_accessToken, java.lang.Long p_taskId, int p_workOfflineFileType) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String uploadWorkOfflineFiles(java.lang.String p_accessToken, java.lang.Long p_taskId, int p_workOfflineFileType, java.lang.String p_fileName, byte[] bytes) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String importWorkOfflineFiles(java.lang.String p_accessToken, java.lang.Long p_taskId, java.lang.String p_identifyKey, int p_workOfflineFileType) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public boolean isInstalled() throws java.rmi.RemoteException;
    public java.lang.String login(java.lang.String p_username, java.lang.String p_password) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String helloWorld() throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException;
    public java.lang.String getUsernameFromSession(java.lang.String p_accessToken) throws java.rmi.RemoteException;
}
