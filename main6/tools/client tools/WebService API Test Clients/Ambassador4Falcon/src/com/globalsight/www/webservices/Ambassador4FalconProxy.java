package com.globalsight.www.webservices;

public class Ambassador4FalconProxy implements com.globalsight.www.webservices.Ambassador4Falcon {
  private String _endpoint = null;
  private com.globalsight.www.webservices.Ambassador4Falcon ambassador4Falcon = null;
  
  public Ambassador4FalconProxy() {
    _initAmbassador4FalconProxy();
  }
  
  public Ambassador4FalconProxy(String endpoint) {
    _endpoint = endpoint;
    _initAmbassador4FalconProxy();
  }
  
  private void _initAmbassador4FalconProxy() {
    try {
      ambassador4Falcon = (new com.globalsight.www.webservices.Ambassador4FalconServiceLocator()).getAmbassador4Falcon();
      if (ambassador4Falcon != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)ambassador4Falcon)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)ambassador4Falcon)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (ambassador4Falcon != null)
      ((javax.xml.rpc.Stub)ambassador4Falcon)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.globalsight.www.webservices.Ambassador4Falcon getAmbassador4Falcon() {
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon;
  }
  
  public int modifyUser(java.lang.String p_accessToken, java.lang.String p_userId, java.lang.String p_password, java.lang.String p_firstName, java.lang.String p_lastName, java.lang.String p_email, java.lang.String[] p_permissionGrps, java.lang.String p_status, java.lang.String p_roles, boolean p_isInAllProject, java.lang.String[] p_projectIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.modifyUser(p_accessToken, p_userId, p_password, p_firstName, p_lastName, p_email, p_permissionGrps, p_status, p_roles, p_isInAllProject, p_projectIds);
  }
  
  public int createUser(java.lang.String p_accessToken, java.lang.String p_userId, java.lang.String p_password, java.lang.String p_firstName, java.lang.String p_lastName, java.lang.String p_email, java.lang.String[] p_permissionGrps, java.lang.String p_status, java.lang.String p_roles, boolean p_isInAllProject, java.lang.String[] p_projectIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.createUser(p_accessToken, p_userId, p_password, p_firstName, p_lastName, p_email, p_permissionGrps, p_status, p_roles, p_isInAllProject, p_projectIds);
  }
  
  public java.lang.String getWorkflowTemplateInfo(java.lang.String p_accessToken, java.lang.String p_workflowTemplateName, java.lang.String p_companyName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.getWorkflowTemplateInfo(p_accessToken, p_workflowTemplateName, p_companyName);
  }
  
  public java.lang.String taskReassign(java.lang.String p_accessToken, java.lang.String p_taskId, java.lang.String[] p_users) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.taskReassign(p_accessToken, p_taskId, p_users);
  }
  
  public java.lang.String getJobIDsWithStatusChanged(java.lang.String p_accessToken, int p_intervalInMinute) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.getJobIDsWithStatusChanged(p_accessToken, p_intervalInMinute);
  }
  
  public java.lang.String getDetailedWordcounts(java.lang.String p_accessToken, java.lang.String[] p_jobIds, java.lang.Boolean p_includeMTData) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.getDetailedWordcounts(p_accessToken, p_jobIds, p_includeMTData);
  }
  
  public java.lang.String getWorkflowTemplateNames(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.getWorkflowTemplateNames(p_accessToken);
  }
  
  public java.lang.String modifyWorkflowTemplateAssignees(java.lang.String p_accessToken, java.lang.String p_workflowTemplateName, java.lang.String p_companyName, java.lang.String p_activityAssigneesInJson) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.modifyWorkflowTemplateAssignees(p_accessToken, p_workflowTemplateName, p_companyName, p_activityAssigneesInJson);
  }
  
  public java.lang.String getWorkOfflineFiles(java.lang.String p_accessToken, java.lang.Long p_taskId, int p_workOfflineFileType) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.getWorkOfflineFiles(p_accessToken, p_taskId, p_workOfflineFileType);
  }
  
  public java.lang.String uploadWorkOfflineFiles(java.lang.String p_accessToken, java.lang.Long p_taskId, int p_workOfflineFileType, java.lang.String p_fileName, byte[] bytes) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.uploadWorkOfflineFiles(p_accessToken, p_taskId, p_workOfflineFileType, p_fileName, bytes);
  }
  
  public java.lang.String importWorkOfflineFiles(java.lang.String p_accessToken, java.lang.Long p_taskId, java.lang.String p_identifyKey, int p_workOfflineFileType) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.importWorkOfflineFiles(p_accessToken, p_taskId, p_identifyKey, p_workOfflineFileType);
  }
  
  public boolean isInstalled() throws java.rmi.RemoteException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.isInstalled();
  }
  
  public java.lang.String login(java.lang.String p_username, java.lang.String p_password) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.login(p_username, p_password);
  }
  
  public java.lang.String helloWorld() throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.helloWorld();
  }
  
  public java.lang.String getUsernameFromSession(java.lang.String p_accessToken) throws java.rmi.RemoteException{
    if (ambassador4Falcon == null)
      _initAmbassador4FalconProxy();
    return ambassador4Falcon.getUsernameFromSession(p_accessToken);
  }
  
  
}