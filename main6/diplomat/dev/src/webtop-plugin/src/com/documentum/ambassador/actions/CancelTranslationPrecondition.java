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


import com.documentum.ambassador.util.SysobjAttributes;
import com.documentum.ambassador.webservice.AmbassadorConfiguration;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.docbase.ObjectCacheUtil;

/**
 * 
 * Check whether the selected Documentum's object can be used to cancel a job of
 * Ambassador. If a object was used to create a job, its state attribute will be
 * changed into "in progress". Only ojbect has "in progress" state can be used
 * to cancel a job.
 */
public class CancelTranslationPrecondition implements IActionPrecondition {

    public final static String TRANSLATION_IN_PROGRESS_STATE = "in progress";

    /**
     * Return necessary parameters.
     * 
     * @retuen String[]
     */
	public String[] getRequiredParams() {
		return new String[] { SysobjAttributes.DM_OBJECT_ID };
	}

	public boolean queryExecute(String strAction, IConfigElement config,
			ArgumentList arg, Context context, Component component) {
		boolean bExecute = true;

		try {
			String strObjectId = arg.get( SysobjAttributes.DM_OBJECT_ID );
			String strLockOwner = arg.get( SysobjAttributes.DM_LOCK_OWNER );
			if ((strObjectId == null || strObjectId.length() == 0) 
                    || (strLockOwner == null || strLockOwner.length() == 0)) {
				bExecute = false;
            } else {
                IDfSession dfSession = component.getDfSession();
                IDfSysObject sysObject = (IDfSysObject) ObjectCacheUtil
                        .getObject(dfSession, strObjectId);
                try {
                    AmbassadorConfiguration cfg = AmbassadorConfiguration
                            .getInstance();
                    // the object to be canceled should have a job id
                    // or it cannot be canceled.
                    String translationJobLabel = cfg.getPropertyValue(
                            AmbassadorConfiguration.DM_OBJECT_TRANSLATION_JOB);
                    String strTranslationJob = sysObject.getString(translationJobLabel);
                    if(strTranslationJob == null 
                            || strTranslationJob.length() == 0 ) {
                        bExecute = false;
                    }
                    
                    String translationStateLabel = cfg
                            .getPropertyValue(AmbassadorConfiguration.DM_OBJECT_TRANSLATION_STATE);
                    String strAttrTranslation = sysObject
                            .getString(translationStateLabel);
                       
                    // "in progress" is hard code which defined by GlobalSight
                    // adapter or Documentum.
                    if (strAttrTranslation == null 
                            || !strAttrTranslation
                                    .equalsIgnoreCase(TRANSLATION_IN_PROGRESS_STATE)) {
                        bExecute = false;
                    }
                } catch (Exception e) {
                    bExecute = false;
                }
            }
            
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		return bExecute;
	}
}