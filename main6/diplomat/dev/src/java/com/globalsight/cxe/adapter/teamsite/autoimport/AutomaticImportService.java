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
package com.globalsight.cxe.adapter.teamsite.autoimport;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import com.globalsight.log.GlobalSightCategory;

public class AutomaticImportService {
    private static final GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory.getLogger("AutomaticImportService");
    public AutomaticImportService() {
        super();
        s_logger.debug("AutomaticImportService: instance "+this.hashCode()+
                           " created");
    }

    protected void finalize() {
        s_logger.debug("AutomaticImportService: instance "+this.hashCode()+
                           " garbage collected");
    }

    public static void main(String args[]) {
	try {
        int number=0;
        s_logger.debug("AutomaticImportService: started acceptor loop");
	AutomaticImportMonitor.initialize();
	s_logger.debug("AutomaticImportService: Initialized monitor");
	AutomaticImportMonitor aimonitor = AutomaticImportMonitor.getInstance();
	aimonitor.startup();
	s_logger.debug("AutomaticImportService: monitor started");
        } catch (IOException e) {
	    s_logger.debug("AutomaticImportService: Cannot start monitor" + e);
        }catch (Exception ex) {
	    s_logger.debug("AutomaticImportService: Cannot start monitor" + ex);
	}
    }
}
