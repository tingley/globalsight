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
 * Check whether a Documentum object can be used to create a translation job in
 * Ambassador.
 */
public class TranslatePrecondition implements IActionPrecondition {

    /**
     * Get necessary parameters.
     * 
     * @return String[]
     */
	public String[] getRequiredParams() {
		return new String[] { SysobjAttributes.DM_OBJECT_ID, 
                              SysobjAttributes.DM_LOCK_OWNER };
	}

	public boolean queryExecute(String strAction, IConfigElement config,
            ArgumentList arg, Context context, Component component) {
		boolean bExecute = true;

        String strObjectId = arg.get(SysobjAttributes.DM_OBJECT_ID);
        String strLockOwner = arg.get(SysobjAttributes.DM_LOCK_OWNER);
        
        if ((strLockOwner != null) && (strLockOwner.length() != 0)) {
				bExecute = false;
			} else {
				try {
                IDfSession dfSession = component.getDfSession();
                IDfSysObject sysObject = (IDfSysObject) ObjectCacheUtil
                        .getObject(dfSession, strObjectId);
                AmbassadorConfiguration cfg = AmbassadorConfiguration
                        .getInstance();
                
                String targetLanguagesLabel = cfg
                        .getPropertyValue(AmbassadorConfiguration.DM_OBJECT_TARGET_LANGUANGES);
                String strAttrTargetLanguage = sysObject
                        .getString(targetLanguagesLabel);
                
                String translationStateLabel = cfg
                        .getPropertyValue(AmbassadorConfiguration.DM_OBJECT_TRANSLATION_STATE);
                String strAttrTranslation = sysObject
                        .getString(translationStateLabel);
                
                if ((strAttrTargetLanguage == null || strAttrTargetLanguage.length() == 0)) {
						bExecute = false;
					}
                if (strAttrTranslation != null
                        && strAttrTranslation
                                .equalsIgnoreCase(CancelTranslationPrecondition.TRANSLATION_IN_PROGRESS_STATE)) {
                    bExecute = false;
                }

				} catch (Exception e) {
					bExecute = false;
				}
			}

		return bExecute;
	}
}