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
import com.documentum.ambassador.webservice.AmbassadorWebServiceClient;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.formext.action.LaunchComponent;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;

/**
 * Launch <code>Translate</code> component for each selected Doucmentum
 * object.
 */
public class TranslateLaunch extends LaunchComponent {

    public final static String ARG_FILE_FROFILES = "fileProfiles";

    /**
     * Get necessary parameters.
     * 
     * @return String[]
     */
	public String[] getRequiredParams() {
		return new String[] {  SysobjAttributes.DM_OBJECT_ID  };
	}

	public boolean execute(String strAction, IConfigElement config,
			ArgumentList args, Context context, Component component,
			Map completionArgs) {
		try {
            AmbassadorWebServiceClient ambWSC = AmbassadorWebServiceClient
                    .getInstance();
            ambWSC.loadFileProfileInfoEx();

            
		} catch (Exception e) {
			throw new WrapperRuntimeException( e.getLocalizedMessage() );
		}
        
        return super.execute(strAction, config, args, context, component,
                completionArgs);
	}
    
}