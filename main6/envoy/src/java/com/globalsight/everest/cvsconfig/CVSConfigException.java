package com.globalsight.everest.cvsconfig;

import com.globalsight.util.GeneralException;

public class CVSConfigException extends GeneralException {
	public final static String PROPERTIES_FILENAME = "CVSConfigException";
	
	public final static String MSG_FAILED_TO_FIND_USER_MANAGER = "FailedToFindUserManager";
	
	public final static String MSG_FAILED_TO_CREATE_CVS_SERVER = "FailedToCreateCVSServer";
	public final static String MSG_FAILED_TO_UPDATE_CVS_SERVER = "FailedToUpdateCVSServer";
	public final static String MSG_FAILED_TO_REMOVE_CVS_SERVER = "FailedToRemoveCVSServer";
	
	public CVSConfigException(String pMessageKey, String[] pMessageArguments,
			Exception pOriginalException) {
		super(pMessageKey, pMessageArguments, pOriginalException, PROPERTIES_FILENAME);
	}
}
