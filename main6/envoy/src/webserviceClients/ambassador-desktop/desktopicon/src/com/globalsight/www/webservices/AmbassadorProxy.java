package com.globalsight.www.webservices;

public class AmbassadorProxy implements com.globalsight.www.webservices.Ambassador {
  private String _endpoint = null;
  private com.globalsight.www.webservices.Ambassador ambassador = null;
  
  public AmbassadorProxy() {
    _initAmbassadorProxy();
  }
  
  public AmbassadorProxy(String endpoint) {
    _endpoint = endpoint;
    _initAmbassadorProxy();
  }
  
  private void _initAmbassadorProxy() {
    try {
      ambassador = (new com.globalsight.www.webservices.AmbassadorServiceLocator()).getAmbassadorWebService();
      if (ambassador != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)ambassador)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)ambassador)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (ambassador != null)
      ((javax.xml.rpc.Stub)ambassador)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.globalsight.www.webservices.Ambassador getAmbassador() {
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador;
  }
  
  public java.lang.String getUserInfo(java.lang.String p_accessToken, java.lang.String p_userId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getUserInfo(p_accessToken, p_userId);
  }
  
  public java.lang.String getVersion(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getVersion(p_accessToken);
  }
  
  public java.lang.String getConnection(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getConnection(p_accessToken);
  }
  
  public java.lang.String addComment(java.lang.String p_accessToken, long p_objectId, int p_objectType, java.lang.String p_userId, java.lang.String p_comment, byte[] p_file, java.lang.String p_fileName, java.lang.String p_access) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.addComment(p_accessToken, p_objectId, p_objectType, p_userId, p_comment, p_file, p_fileName, p_access);
  }
  
  public boolean isInstalled() throws java.rmi.RemoteException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.isInstalled();
  }
  
  public java.lang.String rejectTask(java.lang.String p_accessToken, java.lang.String p_taskId, java.lang.String p_rejectComment) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.rejectTask(p_accessToken, p_taskId, p_rejectComment);
  }
  
  public java.lang.String acceptTask(java.lang.String p_accessToken, java.lang.String p_taskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.acceptTask(p_accessToken, p_taskId);
  }
  
  public java.lang.String login(java.lang.String p_username, java.lang.String p_password) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.login(p_username, p_password);
  }
  
  public java.lang.String getStatus(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getStatus(p_accessToken, p_jobName);
  }
  
  public java.lang.String getServerVersion(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getServerVersion(p_accessToken);
  }
  
  public java.lang.String getTargetLocales(java.lang.String p_accessToken, java.lang.String p_sourceLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getTargetLocales(p_accessToken, p_sourceLocale);
  }
  
  public java.lang.String addJobComment(java.lang.String p_accessToken, java.lang.String p_jobName, java.lang.String p_userId, java.lang.String p_comment, byte[] p_file, java.lang.String p_fileName, java.lang.String p_access) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.addJobComment(p_accessToken, p_jobName, p_userId, p_comment, p_file, p_fileName, p_access);
  }
  
  public java.lang.Object getUserTimeZone(java.lang.String p_accessToken, java.lang.String p_userId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getUserTimeZone(p_accessToken, p_userId);
  }
  
  public int createUser(java.lang.String p_accessToken, java.lang.String p_userId, java.lang.String p_password, java.lang.String p_firstName, java.lang.String p_lastName, java.lang.String p_email, java.lang.String[] p_permissionGrps, java.lang.String p_status, java.lang.String p_roles, boolean p_isInAllProject, java.lang.String[] p_projectIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.createUser(p_accessToken, p_userId, p_password, p_firstName, p_lastName, p_email, p_permissionGrps, p_status, p_roles, p_isInAllProject, p_projectIds);
  }
  
  public int modifyUser(java.lang.String p_accessToken, java.lang.String p_userId, java.lang.String p_password, java.lang.String p_firstName, java.lang.String p_lastName, java.lang.String p_email, java.lang.String[] p_permissionGrps, java.lang.String p_status, java.lang.String p_roles, boolean p_isInAllProject, java.lang.String[] p_projectIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.modifyUser(p_accessToken, p_userId, p_password, p_firstName, p_lastName, p_email, p_permissionGrps, p_status, p_roles, p_isInAllProject, p_projectIds);
  }
  
  public java.lang.String cancelWorkflow(java.lang.String p_accessToken, java.lang.String p_jobName, java.lang.String p_workflowLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.cancelWorkflow(p_accessToken, p_jobName, p_workflowLocale);
  }
  
  public void updateTaskState(java.lang.String p_accessToken, java.lang.String p_taskId, java.lang.String p_state) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.updateTaskState(p_accessToken, p_taskId, p_state);
  }
  
  public java.lang.String completeTask(java.lang.String p_accessToken, java.lang.String p_taskId, java.lang.String p_destinationArrow) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.completeTask(p_accessToken, p_taskId, p_destinationArrow);
  }
  
  public java.lang.String getAllProjects(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAllProjects(p_accessToken);
  }
  
  public java.lang.String getAllProjectTMs(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAllProjectTMs(p_accessToken);
  }
  
  public java.lang.String getAllTMProfiles(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAllTMProfiles(p_accessToken);
  }
  
  public java.lang.String cancelJob(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.cancelJob(p_accessToken, p_jobName);
  }
  
  public java.lang.String getCountsByJobState(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getCountsByJobState(p_accessToken);
  }
  
  public java.lang.String getJobAttribute(java.lang.String accessToken, long jobId, java.lang.String attInternalName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getJobAttribute(accessToken, jobId, attInternalName);
  }
  
  public void setJobAttribute(java.lang.String accessToken, long jobId, java.lang.String attInternalName, java.lang.Object value) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.setJobAttribute(accessToken, jobId, attInternalName, value);
  }
  
  public java.lang.String saveEntry(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String sid, java.lang.String p_sourceLocale, java.lang.String p_sourceSegment, java.lang.String p_targetLocale, java.lang.String p_targetSegment, boolean isEscape) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.saveEntry(p_accessToken, p_tmProfileName, sid, p_sourceLocale, p_sourceSegment, p_targetLocale, p_targetSegment, isEscape);
  }
  
  public void saveEntry(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_sourceLocale, java.lang.String p_sourceSegment, java.lang.String p_targetLocale, java.lang.String p_targetSegment) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.saveEntry(p_accessToken, p_tmProfileName, p_sourceLocale, p_sourceSegment, p_targetLocale, p_targetSegment);
  }
  
  public java.lang.String saveEntry(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String sid, java.lang.String p_sourceLocale, java.lang.String p_sourceSegment, java.lang.String p_targetLocale, java.lang.String p_targetSegment, java.lang.String escapeString) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.saveEntry(p_accessToken, p_tmProfileName, sid, p_sourceLocale, p_sourceSegment, p_targetLocale, p_targetSegment, escapeString);
  }
  
  public java.lang.String dispatchWorkflow(java.lang.String p_accessToken, java.lang.String p_wfIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.dispatchWorkflow(p_accessToken, p_wfIds);
  }
  
  public java.lang.String isSupportCurrentLocalePair(java.lang.String p_accessToken, java.lang.String p_fileProfileId, java.lang.String p_srcLangCountry, java.lang.String p_trgLangCountry) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.isSupportCurrentLocalePair(p_accessToken, p_fileProfileId, p_srcLangCountry, p_trgLangCountry);
  }
  
  public java.lang.String getUniqueJobName(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getUniqueJobName(args);
  }
  
  public java.lang.String getUniqueJobName(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getUniqueJobName(p_accessToken, p_jobName);
  }
  
  public void uploadOriginalSourceFile(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.uploadOriginalSourceFile(args);
  }
  
  public void uploadFile(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.uploadFile(args);
  }
  
  public void uploadFile(java.lang.String accessToken, java.lang.String jobName, java.lang.String filePath, java.lang.String fileProfileId, byte[] content) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.uploadFile(accessToken, jobName, filePath, fileProfileId, content);
  }
  
  public void uploadFile(java.lang.String accessToken, java.lang.String jobName, java.lang.String filePath, java.lang.String fileProfileId, java.lang.String content) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.uploadFile(accessToken, jobName, filePath, fileProfileId, content);
  }
  
  public void createEditionJob(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.createEditionJob(args);
  }
  
  public void sendSegmentCommentBack(java.lang.String p_accessToken, java.util.HashMap p_segmentComments) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.sendSegmentCommentBack(p_accessToken, p_segmentComments);
  }
  
  public void discardJob(java.lang.String p_accessToken, java.lang.String p_userIdToDiscardJob, java.lang.String p_taskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.discardJob(p_accessToken, p_userIdToDiscardJob, p_taskId);
  }
  
  public void uploadEditionFileBack(java.lang.String p_accessToken, java.lang.String p_originalTaskId, java.lang.String p_fileName, byte[] p_bytes) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.uploadEditionFileBack(p_accessToken, p_originalTaskId, p_fileName, p_bytes);
  }
  
  public void importOfflineTargetFiles(java.lang.String p_accessToken, java.lang.String p_originalTaskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.importOfflineTargetFiles(p_accessToken, p_originalTaskId);
  }
  
  public java.lang.String cancelJobById(java.lang.String p_accessToken, long p_jobId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.cancelJobById(p_accessToken, p_jobId);
  }
  
  public java.lang.String getAcceptedTasksInWorkflow(java.lang.String p_accessToken, long p_workflowId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAcceptedTasksInWorkflow(p_accessToken, p_workflowId);
  }
  
  public java.lang.String getCurrentTasksInWorkflow(java.lang.String p_accessToken, long p_workflowId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getCurrentTasksInWorkflow(p_accessToken, p_workflowId);
  }
  
  public java.lang.String getTasksInJob(java.lang.String p_accessToken, long p_jobId, java.lang.String p_taskName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getTasksInJob(p_accessToken, p_jobId, p_taskName);
  }
  
  public java.lang.String getAllProjectsByUser(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAllProjectsByUser(p_accessToken);
  }
  
  public void createJob(java.lang.String accessToken, java.lang.String jobName, java.lang.String comment, java.lang.String filePaths, java.lang.String fileProfileIds, java.lang.String targetLocales, java.lang.String attributeXml) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.createJob(accessToken, jobName, comment, filePaths, fileProfileIds, targetLocales, attributeXml);
  }
  
  public void createJob(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.createJob(args);
  }
  
  public void createJob(java.lang.String accessToken, java.lang.String jobName, java.lang.String comment, java.lang.String filePaths, java.lang.String fileProfileIds, java.lang.String targetLocales) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.createJob(accessToken, jobName, comment, filePaths, fileProfileIds, targetLocales);
  }
  
  public java.lang.String getJobAndWorkflowInfo(java.lang.String p_accessToken, long p_jobId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getJobAndWorkflowInfo(p_accessToken, p_jobId);
  }
  
  public java.lang.String getLocalizedDocuments(java.lang.String p_accessToken, java.lang.String p_jobName, java.lang.String p_wfId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WorkflowManagerException, com.globalsight.www.webservices.WebServiceException, com.globalsight.www.webservices.GeneralException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getLocalizedDocuments(p_accessToken, p_jobName, p_wfId);
  }
  
  public java.lang.String getLocalizedDocuments(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getLocalizedDocuments(p_accessToken, p_jobName);
  }
  
  public java.lang.String exportWorkflow(java.lang.String p_accessToken, java.lang.String p_jobName, java.lang.String p_workflowLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.exportWorkflow(p_accessToken, p_jobName, p_workflowLocale);
  }
  
  public java.lang.String exportJob(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.exportJob(p_accessToken, p_jobName);
  }
  
  public java.lang.String getUserUnavailabilityReport(java.lang.String p_accessToken, java.lang.String p_activityName, java.lang.String p_sourceLocale, java.lang.String p_targetLocale, int p_month, int p_year) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getUserUnavailabilityReport(p_accessToken, p_activityName, p_sourceLocale, p_targetLocale, p_month, p_year);
  }
  
  public java.lang.String passDCTMAccount(java.lang.String p_accessToken, java.lang.String docBase, java.lang.String dctmUserName, java.lang.String dctmPassword) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.passDCTMAccount(p_accessToken, docBase, dctmUserName, dctmPassword);
  }
  
  public void createDocumentumJob(java.lang.String p_accessToken, java.lang.String jobName, java.lang.String fileProfileId, java.lang.String objectId, java.lang.String userId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.createDocumentumJob(p_accessToken, jobName, fileProfileId, objectId, userId);
  }
  
  public void cancelDocumentumJob(java.lang.String p_accessToken, java.lang.String objectId, java.lang.String jobId, java.lang.String userId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.cancelDocumentumJob(p_accessToken, objectId, jobId, userId);
  }
  
  public java.lang.String getDownloadableJobs(java.lang.String p_accessToken, java.lang.String p_msg) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getDownloadableJobs(p_accessToken, p_msg);
  }
  
  public java.lang.String helloWorld() throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.helloWorld();
  }
  
  public java.lang.String getFileProfileInformation(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getFileProfileInformation(p_accessToken);
  }
  
  public java.lang.String getAllActivityTypes(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAllActivityTypes(p_accessToken);
  }
  
  public java.lang.String getAllUsers(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAllUsers(p_accessToken);
  }
  
  public java.lang.String getAllLocalePairs(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAllLocalePairs(p_accessToken);
  }
  
  public void createJobOnInitial(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.createJobOnInitial(args);
  }
  
  public java.lang.String uploadFileForInitial(java.util.HashMap args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.uploadFileForInitial(args);
  }
  
  public java.lang.String getJobExportFiles(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getJobExportFiles(p_accessToken, p_jobName);
  }
  
  public java.lang.String getLocalizedDocuments_old(java.lang.String p_accessToken, java.lang.String p_jobName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getLocalizedDocuments_old(p_accessToken, p_jobName);
  }
  
  public java.lang.String cancelJobs(java.lang.String p_accessToken, java.lang.String p_jobIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.cancelJobs(p_accessToken, p_jobIds);
  }
  
  public java.lang.String getFileProfileInfoEx(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getFileProfileInfoEx(p_accessToken);
  }
  
  public java.lang.String getGSVersion(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getGSVersion(p_accessToken);
  }
  
  public java.lang.String searchEntries(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_string, java.lang.String p_sourceLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.searchEntries(p_accessToken, p_tmProfileName, p_string, p_sourceLocale);
  }
  
  public java.lang.String searchEntries(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_string, java.lang.String p_sourceLocale, java.lang.String escapeString) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.searchEntries(p_accessToken, p_tmProfileName, p_string, p_sourceLocale, escapeString);
  }
  
  public java.lang.String searchEntries(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_string, java.lang.String p_sourceLocale, boolean isEscape) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.searchEntries(p_accessToken, p_tmProfileName, p_string, p_sourceLocale, isEscape);
  }
  
  public java.util.HashMap searchEntriesInBatch(java.lang.String p_accessToken, java.lang.Long p_remoteTmProfileId, java.util.HashMap p_segmentMap, java.lang.Long p_sourceLocaleId, java.util.HashMap p_btrgLocal2LevLocalesMap, java.lang.Boolean p_translatable, java.lang.Boolean p_escapeString) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.searchEntriesInBatch(p_accessToken, p_remoteTmProfileId, p_segmentMap, p_sourceLocaleId, p_btrgLocal2LevLocalesMap, p_translatable, p_escapeString);
  }
  
  public void editEntry(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_orgSid, java.lang.String p_newSid, java.lang.String p_sourceLocale, java.lang.String p_sourceSegment, java.lang.String p_targetLocale, java.lang.String p_targetSegment, java.lang.String escapeString) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.editEntry(p_accessToken, p_tmProfileName, p_orgSid, p_newSid, p_sourceLocale, p_sourceSegment, p_targetLocale, p_targetSegment, escapeString);
  }
  
  public void editEntry(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_orgSid, java.lang.String p_newSid, java.lang.String p_sourceLocale, java.lang.String p_sourceSegment, java.lang.String p_targetLocale, java.lang.String p_targetSegment, boolean isEscape) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.editEntry(p_accessToken, p_tmProfileName, p_orgSid, p_newSid, p_sourceLocale, p_sourceSegment, p_targetLocale, p_targetSegment, isEscape);
  }
  
  public void editEntry(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_sourceLocale, java.lang.String p_sourceSegment, java.lang.String p_targetLocale, java.lang.String p_targetSegment) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.editEntry(p_accessToken, p_tmProfileName, p_sourceLocale, p_sourceSegment, p_targetLocale, p_targetSegment);
  }
  
  public void deleteSegment(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_string, java.lang.String p_sourceLocale, java.lang.String p_deleteLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.deleteSegment(p_accessToken, p_tmProfileName, p_string, p_sourceLocale, p_deleteLocale);
  }
  
  public void deleteSegment(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_string, java.lang.String p_sourceLocale, java.lang.String p_deleteLocale, boolean isEscape) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.deleteSegment(p_accessToken, p_tmProfileName, p_string, p_sourceLocale, p_deleteLocale, isEscape);
  }
  
  public void deleteSegment(java.lang.String p_accessToken, java.lang.String p_tmProfileName, java.lang.String p_string, java.lang.String p_sourceLocale, java.lang.String p_deleteLocale, java.lang.String escapeString) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.deleteSegment(p_accessToken, p_tmProfileName, p_string, p_sourceLocale, p_deleteLocale, escapeString);
  }
  
  public java.lang.String getAllTermbases(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAllTermbases(p_accessToken);
  }
  
  public void saveTBEntry(java.lang.String p_accessToken, java.lang.String p_termbaseName, java.lang.String p_sourceLocale, java.lang.String p_sourceTerm, java.lang.String p_targetLocale, java.lang.String p_targetTerm) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.saveTBEntry(p_accessToken, p_termbaseName, p_sourceLocale, p_sourceTerm, p_targetLocale, p_targetTerm);
  }
  
  public void editTBEntry(java.lang.String p_accessToken, java.lang.String p_termbaseName, java.lang.String p_sourceLocale, java.lang.String p_sourceTerm, java.lang.String p_targetLocale, java.lang.String p_targetTerm) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.editTBEntry(p_accessToken, p_termbaseName, p_sourceLocale, p_sourceTerm, p_targetLocale, p_targetTerm);
  }
  
  public java.lang.String searchTBEntries(java.lang.String p_accessToken, java.lang.String p_termbaseName, java.lang.String p_searchString, java.lang.String p_sourceLocale, java.lang.String p_targetLocale, double p_matchType) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.searchTBEntries(p_accessToken, p_termbaseName, p_searchString, p_sourceLocale, p_targetLocale, p_matchType);
  }
  
  public void deleteTBEntry(java.lang.String p_accessToken, java.lang.String p_termbaseName, java.lang.String p_searchString, java.lang.String p_sourceLocale, java.lang.String p_targetLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.deleteTBEntry(p_accessToken, p_termbaseName, p_searchString, p_sourceLocale, p_targetLocale);
  }
  
  public java.lang.String getFirstTu(java.lang.String accessToken, java.lang.String tmName, java.lang.String companyName, java.lang.String sourceLocale, java.lang.String targetLocale) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getFirstTu(accessToken, tmName, companyName, sourceLocale, targetLocale);
  }
  
  public java.lang.String nextTus(java.lang.String accessToken, java.lang.String sourceLocale, java.lang.String targetLocale, java.lang.String maxSize, java.lang.String tuId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.nextTus(accessToken, sourceLocale, targetLocale, maxSize, tuId);
  }
  
  public java.lang.String editTu(java.lang.String accessToken, java.lang.String tmx) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.editTu(accessToken, tmx);
  }
  
  public java.lang.String getAllPermissionsByUser(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAllPermissionsByUser(p_accessToken);
  }
  
  public java.lang.String getSourceLocales(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getSourceLocales(p_accessToken);
  }
  
  public java.lang.String getPriorityByID(java.lang.String p_accessToken, java.lang.String p_l10NID) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getPriorityByID(p_accessToken, p_l10NID);
  }
  
  public java.lang.String getAttributesByJobId(java.lang.String p_accessToken, java.lang.Long p_jobId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAttributesByJobId(p_accessToken, p_jobId);
  }
  
  public java.lang.String getAttributesByJobId(java.lang.String p_accessToken, long p_jobId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAttributesByJobId(p_accessToken, p_jobId);
  }
  
  public java.lang.String getAttributesByProjectId(java.lang.String p_accessToken, long p_projectId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getAttributesByProjectId(p_accessToken, p_projectId);
  }
  
  public long getProjectIdByFileProfileId(java.lang.String p_accessToken, long p_fileProfileId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getProjectIdByFileProfileId(p_accessToken, p_fileProfileId);
  }
  
  public long getProjectIdByFileProfileId(java.lang.String p_accessToken, java.lang.Long p_fileProfileId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getProjectIdByFileProfileId(p_accessToken, p_fileProfileId);
  }
  
  public void uploadAttributeFiles(java.lang.String p_accessToken, java.lang.String jobName, java.lang.String attInternalName, java.lang.String fileName, byte[] bytes) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.uploadAttributeFiles(p_accessToken, jobName, attInternalName, fileName, bytes);
  }
  
  public java.util.HashMap getXliffFileProfile(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getXliffFileProfile(p_accessToken);
  }
  
  public void uploadCommentReferenceFiles(java.util.HashMap p_args) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.uploadCommentReferenceFiles(p_args);
  }
  
  public java.lang.String isExistedPermission(java.lang.String p_accessToken, java.lang.String p_permissionName) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.isExistedPermission(p_accessToken, p_permissionName);
  }
  
  public java.lang.String fetchCompanyInfo(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.fetchCompanyInfo(p_accessToken);
  }
  
  public java.lang.String fetchJobIdsPerCompany(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.fetchJobIdsPerCompany(p_accessToken);
  }
  
  public java.lang.String fetchJobsByRange(java.lang.String p_accessToken, int p_offset, int p_count, boolean p_isDescOrder) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.fetchJobsByRange(p_accessToken, p_offset, p_count, p_isDescOrder);
  }
  
  public java.lang.String fetchJobsPerCompany(java.lang.String p_accessToken, java.lang.String[] p_jobIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.fetchJobsPerCompany(p_accessToken, p_jobIds);
  }
  
  public java.lang.String fetchJobsPerCompany(java.lang.String p_accessToken) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.fetchJobsPerCompany(p_accessToken);
  }
  
  public java.lang.String fetchJobsByState(java.lang.String p_accessToken, java.lang.String p_state, int p_offset, int p_count, boolean p_isDescOrder) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.fetchJobsByState(p_accessToken, p_state, p_offset, p_count, p_isDescOrder);
  }
  
  public java.lang.String fetchWorkflowRelevantInfo(java.lang.String p_accessToken, java.lang.String p_workflowId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.fetchWorkflowRelevantInfo(p_accessToken, p_workflowId);
  }
  
  public java.lang.String fetchFileForPreview(java.lang.String p_accessToken, java.lang.String p_jobId, java.lang.String p_targetLocaleId, java.lang.String p_sourcePageId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.fetchFileForPreview(p_accessToken, p_jobId, p_targetLocaleId, p_sourcePageId);
  }
  
  public java.lang.String getCommentFiles(java.lang.String p_accessToken, java.lang.String p_commentObjectType, java.lang.String p_jobOrTaskId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getCommentFiles(p_accessToken, p_commentObjectType, p_jobOrTaskId);
  }
  
  public void uploadTmxFile(java.lang.String p_accessToken, java.lang.String p_fileName, java.lang.String p_tmName, byte[] p_contentsInBytes) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.uploadTmxFile(p_accessToken, p_fileName, p_tmName, p_contentsInBytes);
  }
  
  public void importTmxFile(java.lang.String p_accessToken, java.lang.String p_tmName, java.lang.String p_syncMode) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    ambassador.importTmxFile(p_accessToken, p_tmName, p_syncMode);
  }
  
  public java.lang.String jobsSkipActivity(java.lang.String p_accessToken, java.lang.String p_workflowId, java.lang.String p_activity) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.jobsSkipActivity(p_accessToken, p_workflowId, p_activity);
  }
  
  public java.lang.String jobsReassign(java.lang.String p_accessToken, java.lang.String p_workflowId, java.lang.String p_targetLocale, java.lang.String[] p_users) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.jobsReassign(p_accessToken, p_workflowId, p_targetLocale, p_users);
  }
  
  public java.lang.String jobsAddLanguages(java.lang.String p_accessToken, long p_jobId, java.lang.String p_wfInfos) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.jobsAddLanguages(p_accessToken, p_jobId, p_wfInfos);
  }
  
  public java.lang.String jobsWorkflowCanBeAdded(java.lang.String p_accessToken, long p_jobId) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.jobsWorkflowCanBeAdded(p_accessToken, p_jobId);
  }
  
  public java.lang.String fetchSegmentsZipped(java.lang.String p_accessToken, java.lang.String p_workflowId, java.lang.String p_sourcePageIds) throws java.rmi.RemoteException, com.globalsight.www.webservices.WebServiceException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.fetchSegmentsZipped(p_accessToken, p_workflowId, p_sourcePageIds);
  }
  
  public java.lang.String getUsernameFromSession(java.lang.String p_accessToken) throws java.rmi.RemoteException{
    if (ambassador == null)
      _initAmbassadorProxy();
    return ambassador.getUsernameFromSession(p_accessToken);
  }
  
  
}