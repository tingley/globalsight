/**
 *  Copyright 2009 Welocalize, Inc. 
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
package com.globalsight.everest.webapp.pagehandler.administration.workflow;

public interface WorkflowStatePostConstants
{
    public static final String NAME = "nameFiled";

    public static final String DESC = "descField";
    public static final String EMAIL = "emailField";
    public static final String SESSIONTIME = "sessionTimeField";

    public static final String NAME_FIELD = "nameField";
    public static final String DESCRIPTION_FIELD = "descField";
    public static final String LISTENERURL_FIELD = "listenerURLField";
    public static final String SECRETKEY_FIELD = "secretKeyField";
    public static final String TIMEOUT_FIELD = "timeoutField";
    public static final String RETRY_TIME_FIELD = "retryTimeField";
    public static final String NOTIFY_EMAIL_FIELD = "notifyEmailField";
    public static final String WF_STATE_POST_INFO = "wfStatePostInfo";
    public static final String WORKFLOW_STATE_POST_ID = "wfStatePostId";

    public static final String WFSPPROFILE_LIST = "wfStatePostProfiles";
    public static final String WFSPPROFILE_KEY = "wfStatePostProfile";
    public static final String DEPENDENCIES = "deps";

    public static final String ACTION = "action";
    public static final String CANCEL_ACTION = "cancel";
    public static final String SAVE_ACTION = "save";
    public static final String MODIFY_ACTION = "modify";

    public static final String WFSTATEPOSTNAME = "wfStatePostName";
    public static final String WFSTATEPOSTDESCRIPTION = "wfStatePostDescription";
    public static final String WFSTATEPOSTLISTENERURL = "wfStatePostListenerURL";
    public static final String WFSTATEPOSTSECRETKEY = "wfStatePostSecretKey";
    public static final String WFSTATEPOSTTIMEOUTPERIOD = "wfStatePostTimeoutPeriod";
    public static final String WFSTATEPOSTRETRYTIME = "wfStatePostRetryTime";
    public static final String WFSTATEPOSTNOTIFYEMAIL = "wfStatePostNotifyEmail";
}
