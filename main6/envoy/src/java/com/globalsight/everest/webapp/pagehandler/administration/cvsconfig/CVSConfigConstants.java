package com.globalsight.everest.webapp.pagehandler.administration.cvsconfig;

public interface CVSConfigConstants {
	//CVS server configuration
	public static final String CVS_SERVER = "cvsserver";
	public static final String CVS_SERVER_LIST = "cvsservers";
	public static final String CVS_SERVER_KEY = "cvsserver";
	
	public static final String CVS_FILE_PROFILE = "cvsfileprofile"; 
	
	//fields -- CVS Server
	public static final String SERVER_NAME = "servername";
	public static final String HOST_IP = "hostIP";
	public static final String HOST_PORT = "hostPort";
	public static final String PROTOCOL = "protocol";
	public static final String SANDBOX = "sandbox";

	//CVS repository configuration
	public static final String CVS_REPOSITORY = "repository";
	public static final String CVS_REPOSITORY_LIST = "repositories";
	public static final String CVS_REPOSITORY_KEY = "repository";
	public static final String CVS_REPOSITORY_LOGIN_USER = "loginUser";
	public static final String CVS_REPOSITORY_LOGIN_PASSWORD = "loginPwd";
	public static final String CVS_REPOSITORY_LOGIN_PASSWORD_CONFIRM = "loginPwdCfm";
	public static final String CVS_REPOSITORY_CVSROOT_ENV = "cvsRootEnv";
	
	//fields -- CVS Repository
    public static final String REPOSITORY_NAME = "repositoryName";
    public static final String REPOSITORY_SERVER = "server";
    public static final String REPOSITORY_CVS = "repository";
    public static final String REPOSITORY_FOLDERNAME = "folderName";
    
    //fields -- CVS Repository
    public static final String MODULE_NAME = "selfname";
    public static final String MODULE_REPOSITORY = "repository";
    public static final String MODULE_MODULENAME = "moduleName";
    public static final String CVS_MODULE_LIST = "moduleList";
    public static final String CVS_MODULE_KEY = "moduleKey";
	
	public static final String CVS_MODULE = "cvsmodule";
	
	public static final String CVS_SERVERS = "cvsservers";
	public static final String CVS_REPOSITORYS = "cvsrepositories";
	public static final String CVS_MODULES = "cvsmodules";

	//fields -- CVS File Profile
	public static final String CVS_FILE_PROFILE_PROJECT = "project";
	public static final String CVS_FILE_PROFILE_MODULE = "module";
	public static final String CVS_FILE_PROFILE_EXTENSIONS = "ext";
	public static final String CVS_FILE_PROFILE_FP = "fp";
	public static final String CVS_FILE_PROFILE_LIST = "cvsfileprofiles";
	public static final String CVS_FILE_PROFILE_KEY = "cvsfileprofile";
	
	//actions
	public static final String CREATE = "create";
	public static final String UPDATE = "update";
	public static final String REMOVE = "remove";
	public static final String CANCEL = "cancel";
	public static final String CHECKOUT = "checkout";
	
	
}
