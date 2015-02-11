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
package com.documentum.ambassador.actions;

import java.util.Map;

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
import com.documentum.web.formext.action.IActionExecution;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.docbase.ObjectCacheUtil;

/**
 * Repond when cancel translation action is executed.
 *
 */
public class CancelTranslationAction implements IActionExecution {

    /**
     * Return necessary parameters.
     * 
     * @retuen String[]
     */
	public String[] getRequiredParams() {
		return new String[] { SysobjAttributes.DM_OBJECT_ID, 
                              SysobjAttributes.DM_LOCK_OWNER };
	}

	public boolean execute(String strAction, IConfigElement config,
			ArgumentList args, Context context, Component component,
			Map completionArgs) {
		boolean bExecute = true;

		try {
            String strObjectId = args.get( SysobjAttributes.DM_OBJECT_ID );

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
            loginInfoObj.setUser( userName );
            loginInfoObj.setPassword( pwd );
            loginInfoObj.setDomain(null);
     
            //Bind the session manager to the login info
            String curDocbaseName = component.getDfSession().getDocbaseName();
            m_sessionMgr.setIdentity( curDocbaseName, loginInfoObj );
            IDfSession dfSession = m_sessionMgr.getSession( curDocbaseName );
            IDfSysObject sysObject = (IDfSysObject) ObjectCacheUtil.getObject(
                    dfSession, strObjectId);
            
            String translationJobLabel = cfg
                    .getPropertyValue(AmbassadorConfiguration.DM_OBJECT_TRANSLATION_JOB);
            String strAttrJobId = sysObject.getString( translationJobLabel );

			// Cancel each selected document.            
            AmbassadorWebServiceClient ambWSC = AmbassadorWebServiceClient
                    .getInstance();
            ambWSC.cancelDocumentumJob( strObjectId, strAttrJobId );
             
		} catch (Exception e) {
            throw new WrapperRuntimeException( e.getLocalizedMessage() );
		}
        
		return bExecute;
	}
}