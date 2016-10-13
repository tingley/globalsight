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
package com.documentum.ambassador.library.translate;

import java.util.ArrayList;
import com.documentum.ambassador.util.SysobjAttributes;
import com.documentum.ambassador.webservice.AmbassadorConfiguration;
import com.documentum.ambassador.webservice.AmbassadorWebServiceClient;
import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.formext.component.ComboContainer;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.docbase.ObjectCacheUtil;

/**
 * @ see <code>ComboContainer</code>
 *  
 */
public class TranslateContainer extends ComboContainer {

    /**
     * @see DialogContainer#onOk(Control, ArgumentList)
     */
    public void onOk(Control arg0, ArgumentList arg1) {
        super.onOk(arg0, arg1);
        
        ArrayList cmpList = this.getContainedComponents();
        for (int i = 0; i < cmpList.size(); i++) {
            Component validComponent = (Component) cmpList.get(i);
            // If any validation controls in any component is not valid, 
            // the onOk event should be aborted. 
            if (validComponent.getIsValid() == false) {
                return;
            }
        }

        IDfSession dfSession = null;
        try {
            IDfClientX m_clientx = new DfClientX();
            IDfClient client = m_clientx.getLocalClient();
            //Create a session manager object
            IDfSessionManager m_sessionMgr = client.newSessionManager();

            //Create an IDfLoginInfo object named loginInfoObj
            IDfLoginInfo loginInfoObj = m_clientx.getLoginInfo();
            AmbassadorConfiguration cfg = AmbassadorConfiguration.getInstance();
            String userName = cfg
                    .getPropertyValue(AmbassadorConfiguration.DCTM_USERNAME);
            String pwd = cfg
                    .getPropertyValue(AmbassadorConfiguration.DCTM_PASSWORD);
            loginInfoObj.setUser(userName);
            loginInfoObj.setPassword(pwd);
            loginInfoObj.setDomain(null);
            // Bind the session manager to the login info
            String curDocbaseName = this.getDfSession().getDocbaseName();
            m_sessionMgr.setIdentity(curDocbaseName, loginInfoObj);
            dfSession = m_sessionMgr.getSession(curDocbaseName);
        } catch (Exception e) {
            throw new WrapperRuntimeException(e.getLocalizedMessage());
        }

        for (int i = 0; i < cmpList.size(); i++) {
            Component curComponent = (Component) cmpList.get(i);
            
            // Get objectID of tranlating document in current component.
            ArgumentList curArgList = curComponent.getInitArgs();
            String curObjId = curArgList.get(SysobjAttributes.DM_OBJECT_ID);
            // Get value from control TEXT.           
            Text txtControl = (Text) curComponent.getControl(
                                      Translate.CTRL_TEXT_NAME, Text.class);       
            String strJobName = txtControl.getValue();
            
            // Get value from control DropDownList.
            DataDropDownList dropListControl = (DataDropDownList) curComponent
                    .getControl(Translate.CTRL_DROP_LIST_NAME,
                                             DataDropDownList.class);
            String selectedFileProfileId = dropListControl.getValue();
            
            try {
                IDfSysObject sysObject = (IDfSysObject) ObjectCacheUtil
                        .getObject(dfSession, curObjId);
                if (sysObject.isCheckedOut() == false) {
            // create a new job for the document in current component.
            createJob(curObjId, strJobName, selectedFileProfileId);

                    // Change object state into "checkout".
                    sysObject.checkout();
                }
            } catch (Exception e) {
                throw new WrapperRuntimeException(e.getLocalizedMessage());
            }
        }
    }
    
    /**
     * send necessary information to create a new job in Ambassador.
     * 
     * @param objectId -
     *            the objectId of Documentum object used to create job in
     *            Ambassador.
     * @param jobName -
     * @param fileProfileId
     * 
     * @throws WrapperRuntimeException
     */
    private void createJob(String objectId, String jobName, String fileProfileId)
            throws WrapperRuntimeException {

        // jobName, fileProfileId, objectId are all required to create a
        // documentum job.
        if (jobName == null || jobName.length() == 0 || fileProfileId == null
                || fileProfileId.length() == 0 || objectId == null
                || objectId.length() == 0) {
            return;
        }
        
        
        try {
        // Invoke web service to create a new job for current document.
        AmbassadorWebServiceClient ambWsc = AmbassadorWebServiceClient
                .getInstance();
        ambWsc.createDocumentumJob(jobName, fileProfileId, objectId);
            //            IDfSession dfSession = this.getDfSession();
            //            IDfSysObject sysObject = (IDfSysObject)
            // ObjectCacheUtil.getObject(dfSession, objectId);
        
        } catch (Exception e) {
            AmbassadorWebServiceClient.resetWebservice();
            throw new WrapperRuntimeException(e.getLocalizedMessage());
    }
    }
       
}