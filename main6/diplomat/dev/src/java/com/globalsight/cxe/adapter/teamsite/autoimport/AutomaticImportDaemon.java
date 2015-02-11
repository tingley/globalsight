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
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonController;
import org.apache.commons.daemon.DaemonContext;
import com.globalsight.log.GlobalSightCategory;

public class AutomaticImportDaemon implements Daemon, Runnable {
    private static final GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory.getLogger("AutomaticImportDaemon");
    private ServerSocket server=null;
    private Thread thread=null;
    private DaemonController controller=null;
    private boolean stopping=false;
    private String directory=null;
    public AutomaticImportDaemon() {
        super();
        s_logger.debug("AutomaticImportDaemon: instance "+this.hashCode()+
                           " created");
    }

    protected void finalize() {
        s_logger.debug("AutomaticImportDaemon: instance "+this.hashCode()+
                           " garbage collected");
    }

    /**
     * init and destroy were added in jakarta-tomcat-daemon.
     */
    public void init(DaemonContext context)
    throws Exception {
        s_logger.debug("AutomaticImportDaemon: instance "+this.hashCode()+
                           " init");
        /* Set up this simple daemon */
        this.controller=context.getController();
        this.thread=new Thread(this);
    }

    public void start() {
        /* Dump a message */
        s_logger.debug("AutomaticImportDaemon: starting");

        /* Start */
        this.thread.start();
    }

    public void stop()
    throws IOException, InterruptedException {
	/* Shutdown the AutomaticImportMonitor */
	boolean waitForThreadDeath = false;
	AutomaticImportMonitor.getInstance().shutdown(waitForThreadDeath);
        /* Dump a message */
        s_logger.debug("AutomaticImportDaemon: stopping");

        /* Close the ServerSocket. This will make our thread to terminate */
        this.stopping=true;
        this.server.close();

        /* Wait for the main thread to exit and dump a message */
        this.thread.join(5000);
        s_logger.debug("AutomaticImportDaemon: stopped");
    }

    public void destroy() {
        s_logger.debug("AutomaticImportDaemon: instance "+this.hashCode()+
                           " destroy");
    }

    public void run() {
	try {
        int number=0;
        s_logger.debug("AutomaticImportDaemon: started acceptor loop");
	AutomaticImportMonitor.initialize();
	s_logger.debug("AutomaticImportDaemon: Initialized monitor");
	AutomaticImportMonitor aimonitor = AutomaticImportMonitor.getInstance();
	aimonitor.startup();
	s_logger.debug("AutomaticImportDaemon: monitor started");
        } catch (IOException e) {
            /* Don't dump any error message if we are stopping. A IOException
               is generated when the ServerSocket is closed in stop() */
            if (!this.stopping) e.printStackTrace(System.out);
        }catch (Exception ex) {
            if (!this.stopping) ex.printStackTrace(System.out);
	}
    }
}
