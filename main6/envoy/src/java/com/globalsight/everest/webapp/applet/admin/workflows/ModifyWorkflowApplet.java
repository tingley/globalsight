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

package com.globalsight.everest.webapp.applet.admin.workflows;

import com.globalsight.everest.webapp.applet.common.EnvoyApplet;

/**
 * ModifyWorkflowApplet is a subclass of EnvoyApplet and is strictly used for going from one applet
 * to ModifyWorkflowApplet.  In our case we only have one applet which is responsible
 * for displaying a particular panel.  Therefore, overriding the init method would
 * cause reloading the applet.
 * <p>
 * @author Brett Cashman
 */

public class ModifyWorkflowApplet extends EnvoyApplet {

    ///////////////////////////////////////////////////////////////////////////////
    // BEGIN: Init -- creating components
    ///////////////////////////////////////////////////////////////////////////////
    public void init() {
        super.init();
    }
    ///////////////////////////////////////////////////////////////////////////////
    // End: Init -- creating components
    ///////////////////////////////////////////////////////////////////////////////

    public void destroy() {        
        super.destroy();
    }
}
