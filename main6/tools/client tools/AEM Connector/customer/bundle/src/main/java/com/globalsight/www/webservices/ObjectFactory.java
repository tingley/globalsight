
package com.globalsight.www.webservices;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.globalsight.www.webservices package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetFileProfileInfoEx_QNAME = new QName("http://webservices.globalsight.com/", "getFileProfileInfoEx");
    private final static QName _CreateJob_QNAME = new QName("http://webservices.globalsight.com/", "createJob");
    private final static QName _GetJobExportWorkflowFiles_QNAME = new QName("http://webservices.globalsight.com/", "getJobExportWorkflowFiles");
    private final static QName _GetFileProfileInfoExResponse_QNAME = new QName("http://webservices.globalsight.com/", "getFileProfileInfoExResponse");
    private final static QName _GetUniqueJobNameResponse_QNAME = new QName("http://webservices.globalsight.com/", "getUniqueJobNameResponse");
    private final static QName _WebServiceException_QNAME = new QName("http://webservices.globalsight.com/", "WebServiceException");
    private final static QName _GetJobExportFilesResponse_QNAME = new QName("http://webservices.globalsight.com/", "getJobExportFilesResponse");
    private final static QName _GetJobExportWorkflowFilesResponse_QNAME = new QName("http://webservices.globalsight.com/", "getJobExportWorkflowFilesResponse");
    private final static QName _GetUniqueJobName_QNAME = new QName("http://webservices.globalsight.com/", "getUniqueJobName");
    private final static QName _LoginResponse_QNAME = new QName("http://webservices.globalsight.com/", "loginResponse");
    private final static QName _UploadFileForInitialResponse_QNAME = new QName("http://webservices.globalsight.com/", "uploadFileForInitialResponse");
    private final static QName _GetJobStatusResponse_QNAME = new QName("http://webservices.globalsight.com/", "getJobStatusResponse");
    private final static QName _GetJobExportFiles_QNAME = new QName("http://webservices.globalsight.com/", "getJobExportFiles");
    private final static QName _AddJobComment_QNAME = new QName("http://webservices.globalsight.com/", "addJobComment");
    private final static QName _AddJobCommentResponse_QNAME = new QName("http://webservices.globalsight.com/", "addJobCommentResponse");
    private final static QName _GetImportExportStatus_QNAME = new QName("http://webservices.globalsight.com/", "getImportExportStatus");
    private final static QName _UploadFileResponse_QNAME = new QName("http://webservices.globalsight.com/", "uploadFileResponse");
    private final static QName _UploadFile_QNAME = new QName("http://webservices.globalsight.com/", "uploadFile");
    private final static QName _CreateJobResponse_QNAME = new QName("http://webservices.globalsight.com/", "createJobResponse");
    private final static QName _GetDownloadableJobs_QNAME = new QName("http://webservices.globalsight.com/", "getDownloadableJobs");
    private final static QName _GetJobStatus_QNAME = new QName("http://webservices.globalsight.com/", "getJobStatus");
    private final static QName _Login_QNAME = new QName("http://webservices.globalsight.com/", "login");
    private final static QName _UploadFileForInitial_QNAME = new QName("http://webservices.globalsight.com/", "uploadFileForInitial");
    private final static QName _GetDownloadableJobsResponse_QNAME = new QName("http://webservices.globalsight.com/", "getDownloadableJobsResponse");
    private final static QName _GetImportExportStatusResponse_QNAME = new QName("http://webservices.globalsight.com/", "getImportExportStatusResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.globalsight.www.webservices
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link WrapHashMap }
     * 
     */
    public WrapHashMap createWrapHashMap() {
        return new WrapHashMap();
    }

    /**
     * Create an instance of {@link WrapHashMap.InputData }
     * 
     */
    public WrapHashMap.InputData createWrapHashMapInputData() {
        return new WrapHashMap.InputData();
    }

    /**
     * Create an instance of {@link GetImportExportStatusResponse }
     * 
     */
    public GetImportExportStatusResponse createGetImportExportStatusResponse() {
        return new GetImportExportStatusResponse();
    }

    /**
     * Create an instance of {@link GetDownloadableJobsResponse }
     * 
     */
    public GetDownloadableJobsResponse createGetDownloadableJobsResponse() {
        return new GetDownloadableJobsResponse();
    }

    /**
     * Create an instance of {@link UploadFileForInitial }
     * 
     */
    public UploadFileForInitial createUploadFileForInitial() {
        return new UploadFileForInitial();
    }

    /**
     * Create an instance of {@link UploadFile }
     * 
     */
    public UploadFile createUploadFile() {
        return new UploadFile();
    }

    /**
     * Create an instance of {@link UploadFileResponse }
     * 
     */
    public UploadFileResponse createUploadFileResponse() {
        return new UploadFileResponse();
    }

    /**
     * Create an instance of {@link GetImportExportStatus }
     * 
     */
    public GetImportExportStatus createGetImportExportStatus() {
        return new GetImportExportStatus();
    }

    /**
     * Create an instance of {@link AddJobCommentResponse }
     * 
     */
    public AddJobCommentResponse createAddJobCommentResponse() {
        return new AddJobCommentResponse();
    }

    /**
     * Create an instance of {@link Login }
     * 
     */
    public Login createLogin() {
        return new Login();
    }

    /**
     * Create an instance of {@link GetJobStatus }
     * 
     */
    public GetJobStatus createGetJobStatus() {
        return new GetJobStatus();
    }

    /**
     * Create an instance of {@link GetDownloadableJobs }
     * 
     */
    public GetDownloadableJobs createGetDownloadableJobs() {
        return new GetDownloadableJobs();
    }

    /**
     * Create an instance of {@link CreateJobResponse }
     * 
     */
    public CreateJobResponse createCreateJobResponse() {
        return new CreateJobResponse();
    }

    /**
     * Create an instance of {@link UploadFileForInitialResponse }
     * 
     */
    public UploadFileForInitialResponse createUploadFileForInitialResponse() {
        return new UploadFileForInitialResponse();
    }

    /**
     * Create an instance of {@link GetUniqueJobName }
     * 
     */
    public GetUniqueJobName createGetUniqueJobName() {
        return new GetUniqueJobName();
    }

    /**
     * Create an instance of {@link LoginResponse }
     * 
     */
    public LoginResponse createLoginResponse() {
        return new LoginResponse();
    }

    /**
     * Create an instance of {@link AddJobComment }
     * 
     */
    public AddJobComment createAddJobComment() {
        return new AddJobComment();
    }

    /**
     * Create an instance of {@link GetJobExportFiles }
     * 
     */
    public GetJobExportFiles createGetJobExportFiles() {
        return new GetJobExportFiles();
    }

    /**
     * Create an instance of {@link GetJobStatusResponse }
     * 
     */
    public GetJobStatusResponse createGetJobStatusResponse() {
        return new GetJobStatusResponse();
    }

    /**
     * Create an instance of {@link GetFileProfileInfoExResponse }
     * 
     */
    public GetFileProfileInfoExResponse createGetFileProfileInfoExResponse() {
        return new GetFileProfileInfoExResponse();
    }

    /**
     * Create an instance of {@link GetJobExportWorkflowFiles }
     * 
     */
    public GetJobExportWorkflowFiles createGetJobExportWorkflowFiles() {
        return new GetJobExportWorkflowFiles();
    }

    /**
     * Create an instance of {@link GetFileProfileInfoEx }
     * 
     */
    public GetFileProfileInfoEx createGetFileProfileInfoEx() {
        return new GetFileProfileInfoEx();
    }

    /**
     * Create an instance of {@link CreateJob }
     * 
     */
    public CreateJob createCreateJob() {
        return new CreateJob();
    }

    /**
     * Create an instance of {@link GetJobExportFilesResponse }
     * 
     */
    public GetJobExportFilesResponse createGetJobExportFilesResponse() {
        return new GetJobExportFilesResponse();
    }

    /**
     * Create an instance of {@link GetJobExportWorkflowFilesResponse }
     * 
     */
    public GetJobExportWorkflowFilesResponse createGetJobExportWorkflowFilesResponse() {
        return new GetJobExportWorkflowFilesResponse();
    }

    /**
     * Create an instance of {@link WebServiceException }
     * 
     */
    public WebServiceException createWebServiceException() {
        return new WebServiceException();
    }

    /**
     * Create an instance of {@link GetUniqueJobNameResponse }
     * 
     */
    public GetUniqueJobNameResponse createGetUniqueJobNameResponse() {
        return new GetUniqueJobNameResponse();
    }

    /**
     * Create an instance of {@link HashMap }
     * 
     */
    public HashMap createHashMap() {
        return new HashMap();
    }

    /**
     * Create an instance of {@link WrapHashMap.InputData.Entry }
     * 
     */
    public WrapHashMap.InputData.Entry createWrapHashMapInputDataEntry() {
        return new WrapHashMap.InputData.Entry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetFileProfileInfoEx }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getFileProfileInfoEx")
    public JAXBElement<GetFileProfileInfoEx> createGetFileProfileInfoEx(GetFileProfileInfoEx value) {
        return new JAXBElement<GetFileProfileInfoEx>(_GetFileProfileInfoEx_QNAME, GetFileProfileInfoEx.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateJob }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "createJob")
    public JAXBElement<CreateJob> createCreateJob(CreateJob value) {
        return new JAXBElement<CreateJob>(_CreateJob_QNAME, CreateJob.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetJobExportWorkflowFiles }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getJobExportWorkflowFiles")
    public JAXBElement<GetJobExportWorkflowFiles> createGetJobExportWorkflowFiles(GetJobExportWorkflowFiles value) {
        return new JAXBElement<GetJobExportWorkflowFiles>(_GetJobExportWorkflowFiles_QNAME, GetJobExportWorkflowFiles.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetFileProfileInfoExResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getFileProfileInfoExResponse")
    public JAXBElement<GetFileProfileInfoExResponse> createGetFileProfileInfoExResponse(GetFileProfileInfoExResponse value) {
        return new JAXBElement<GetFileProfileInfoExResponse>(_GetFileProfileInfoExResponse_QNAME, GetFileProfileInfoExResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUniqueJobNameResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getUniqueJobNameResponse")
    public JAXBElement<GetUniqueJobNameResponse> createGetUniqueJobNameResponse(GetUniqueJobNameResponse value) {
        return new JAXBElement<GetUniqueJobNameResponse>(_GetUniqueJobNameResponse_QNAME, GetUniqueJobNameResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WebServiceException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "WebServiceException")
    public JAXBElement<WebServiceException> createWebServiceException(WebServiceException value) {
        return new JAXBElement<WebServiceException>(_WebServiceException_QNAME, WebServiceException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetJobExportFilesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getJobExportFilesResponse")
    public JAXBElement<GetJobExportFilesResponse> createGetJobExportFilesResponse(GetJobExportFilesResponse value) {
        return new JAXBElement<GetJobExportFilesResponse>(_GetJobExportFilesResponse_QNAME, GetJobExportFilesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetJobExportWorkflowFilesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getJobExportWorkflowFilesResponse")
    public JAXBElement<GetJobExportWorkflowFilesResponse> createGetJobExportWorkflowFilesResponse(GetJobExportWorkflowFilesResponse value) {
        return new JAXBElement<GetJobExportWorkflowFilesResponse>(_GetJobExportWorkflowFilesResponse_QNAME, GetJobExportWorkflowFilesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUniqueJobName }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getUniqueJobName")
    public JAXBElement<GetUniqueJobName> createGetUniqueJobName(GetUniqueJobName value) {
        return new JAXBElement<GetUniqueJobName>(_GetUniqueJobName_QNAME, GetUniqueJobName.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoginResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "loginResponse")
    public JAXBElement<LoginResponse> createLoginResponse(LoginResponse value) {
        return new JAXBElement<LoginResponse>(_LoginResponse_QNAME, LoginResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadFileForInitialResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "uploadFileForInitialResponse")
    public JAXBElement<UploadFileForInitialResponse> createUploadFileForInitialResponse(UploadFileForInitialResponse value) {
        return new JAXBElement<UploadFileForInitialResponse>(_UploadFileForInitialResponse_QNAME, UploadFileForInitialResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetJobStatusResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getJobStatusResponse")
    public JAXBElement<GetJobStatusResponse> createGetJobStatusResponse(GetJobStatusResponse value) {
        return new JAXBElement<GetJobStatusResponse>(_GetJobStatusResponse_QNAME, GetJobStatusResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetJobExportFiles }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getJobExportFiles")
    public JAXBElement<GetJobExportFiles> createGetJobExportFiles(GetJobExportFiles value) {
        return new JAXBElement<GetJobExportFiles>(_GetJobExportFiles_QNAME, GetJobExportFiles.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddJobComment }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "addJobComment")
    public JAXBElement<AddJobComment> createAddJobComment(AddJobComment value) {
        return new JAXBElement<AddJobComment>(_AddJobComment_QNAME, AddJobComment.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddJobCommentResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "addJobCommentResponse")
    public JAXBElement<AddJobCommentResponse> createAddJobCommentResponse(AddJobCommentResponse value) {
        return new JAXBElement<AddJobCommentResponse>(_AddJobCommentResponse_QNAME, AddJobCommentResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetImportExportStatus }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getImportExportStatus")
    public JAXBElement<GetImportExportStatus> createGetImportExportStatus(GetImportExportStatus value) {
        return new JAXBElement<GetImportExportStatus>(_GetImportExportStatus_QNAME, GetImportExportStatus.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadFileResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "uploadFileResponse")
    public JAXBElement<UploadFileResponse> createUploadFileResponse(UploadFileResponse value) {
        return new JAXBElement<UploadFileResponse>(_UploadFileResponse_QNAME, UploadFileResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadFile }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "uploadFile")
    public JAXBElement<UploadFile> createUploadFile(UploadFile value) {
        return new JAXBElement<UploadFile>(_UploadFile_QNAME, UploadFile.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateJobResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "createJobResponse")
    public JAXBElement<CreateJobResponse> createCreateJobResponse(CreateJobResponse value) {
        return new JAXBElement<CreateJobResponse>(_CreateJobResponse_QNAME, CreateJobResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDownloadableJobs }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getDownloadableJobs")
    public JAXBElement<GetDownloadableJobs> createGetDownloadableJobs(GetDownloadableJobs value) {
        return new JAXBElement<GetDownloadableJobs>(_GetDownloadableJobs_QNAME, GetDownloadableJobs.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetJobStatus }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getJobStatus")
    public JAXBElement<GetJobStatus> createGetJobStatus(GetJobStatus value) {
        return new JAXBElement<GetJobStatus>(_GetJobStatus_QNAME, GetJobStatus.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Login }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "login")
    public JAXBElement<Login> createLogin(Login value) {
        return new JAXBElement<Login>(_Login_QNAME, Login.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadFileForInitial }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "uploadFileForInitial")
    public JAXBElement<UploadFileForInitial> createUploadFileForInitial(UploadFileForInitial value) {
        return new JAXBElement<UploadFileForInitial>(_UploadFileForInitial_QNAME, UploadFileForInitial.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDownloadableJobsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getDownloadableJobsResponse")
    public JAXBElement<GetDownloadableJobsResponse> createGetDownloadableJobsResponse(GetDownloadableJobsResponse value) {
        return new JAXBElement<GetDownloadableJobsResponse>(_GetDownloadableJobsResponse_QNAME, GetDownloadableJobsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetImportExportStatusResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.globalsight.com/", name = "getImportExportStatusResponse")
    public JAXBElement<GetImportExportStatusResponse> createGetImportExportStatusResponse(GetImportExportStatusResponse value) {
        return new JAXBElement<GetImportExportStatusResponse>(_GetImportExportStatusResponse_QNAME, GetImportExportStatusResponse.class, null, value);
    }

}
