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
package com.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Used to add jar files to server.
 */
public class JarManager
{
    private static Logger log = Logger.getLogger(JarManager.class);

    private static String EAR = GitUtil.SERVER_NAME + "/jboss/server/standalone/deployments/globalsight.ear";
    private static String LIB = EAR + "/deploy/globalsight.ear/lib/";
    private static String APPLET_LIB = EAR + "/deploy/globalsight.ear/"
            + "globalsight-web.war/applet/lib/";
    private static String SERVER_LIB = GitUtil.SERVER_NAME
            + "/jboss/jboss_server/lib/";

    private static String THIRD_PARTY2_JAR = BuildUtil.ROOT
            + "/main6/tools/build/capclasses/globalsight.ear/lib/thirdParty2.jar";
    private static String NTHIRD_PARTY2_NEW_JAR = LIB + "thirdParty2.jar";

    private static String THIRD_PARTY1_JAR = BuildUtil.ROOT
            + "/main6/tools/build/capclasses/globalsight.ear/lib/thirdParty1.jar";
    private static String NTHIRD_PARTY1_NEW_JAR = LIB + "thirdParty1.jar";

    private static String AXIS_JAR = BuildUtil.ROOT
            + "/main6/tools/lib/ApacheAxis/axis.jar";
    private static String AXIS_NEW_JAR = LIB + "axis.jar";

    private static String PROMT_UTIL_JAR = BuildUtil.ROOT
            + "/main6/tools/lib/common/promtUtil.jar";
    private static String PROMT_UTIL_NEW_JAR = LIB + "promtUtil.jar";

    private static String INSTALL_JAR_PATH = BuildUtil.ROOT
            + "/main6/tools/build/installclasses/installer.jar";
    private static String INSTALL_JAR_NEW_PATH = GitUtil.SERVER_NAME
            + "/install/installer.jar";

    private static String GRAPHICAL_WF_JAR_PATH = BuildUtil.ROOT
            + "/main6/tools/build/capclasses/globalsight.ear/globalsight-web.war/applet/lib/graphicalWf.jar";
    private static String GRAPHICAL_WF_JAR_NEW_PATH = APPLET_LIB
            + "graphicalWf.jar";

    private static String ONLINE_JAR_PATH = BuildUtil.ROOT
            + "/main6/tools/build/capclasses/globalsight.ear/globalsight-web.war/applet/lib/online.jar";
    private static String ONLINE_JAR_NEW_PATH = APPLET_LIB + "online.jar";

    private static String CUSTOMER_JAR_PATH = BuildUtil.ROOT
            + "/main6/tools/build/capclasses/globalsight.ear/globalsight-web.war/applet/lib/customer.jar";
    private static String CUSTOMER_JAR_NEW_PATH = APPLET_LIB + "customer.jar";

    private static String MAIL_JAR_PATH = BuildUtil.ROOT
            + "/main6/tools/j2eeVendor/jboss/jboss_server/server/default/lib/mail.jar";
    private static String MAIL_JAR_NEW_PATH = EAR + "/lib/mail.jar";

    private static String ADD_SOURCE_JAR_PATH = BuildUtil.ROOT
            + "/main6/tools/build/capclasses/globalsight.ear/globalsight-web.war/applet/lib/SelectFilesApplet.jar";
    private static String ADD_SOURCE_JAR_NEW_PATH = APPLET_LIB
            + "/SelectFilesApplet.jar";
    
    private static String XML_APIS_JAR_PATH = BuildUtil.ROOT
            + "/main6/tools/j2eeVendor/jboss/jboss_server/lib/endorsed/xml-apis.jar";
    private static String XML_APIS_JAR_NEW_PATH = SERVER_LIB + "endorsed/xml-apis.jar";
    
    private static String XERCESIMPL_JAR_PATH = BuildUtil.ROOT
            + "/main6/tools/j2eeVendor/jboss/jboss_server/lib/endorsed/xercesImpl.jar";
    private static String XERCESIMPL_JAR_NEW_PATH = SERVER_LIB
            + "endorsed/xercesImpl.jar";
    
    private static String XERCES_XML_JAR_PATH = BuildUtil.ROOT
            + "/main6/tools/lib/ApacheAxis/xerces-xml.jar";
    private static String XERCES_XML_JAR_NEW_PATH = LIB + "axis/xerces-xml.jar";
    
    private static String SSO_LIB_JAR_PATH = BuildUtil.ROOT
            + "/main6/tools/build/ssoLibs.jar";
    private static String SSO_LIB_JAR_NEW_PATH = LIB + "ssoLibs.jar";
    
	private static String DB_PROFILE_JAR_PATH = BuildUtil.ROOT
			+ "/main6/tools/build/capclasses/globalsight.ear/globalsight-web.war/applet/lib/dbProfile.jar";
	private static String DB_PROFILE_JAR_NEW_PATH = APPLET_LIB
			+ "dbProfile.jar";
	
	private static String CREATE_JOB_JAR_PATH = BuildUtil.ROOT
			+ "/main6/tools/build/capclasses/globalsight.ear/globalsight-web.war/applet/lib/createJob.jar";
	private static String CREATE_JOB_JAR_NEW_PATH = APPLET_LIB
			+ "createJob.jar";
	
	private static String PLUG_IN_JAR_PATH = BuildUtil.ROOT
			+ "/main6/tools/lib/common/plugin.jar";
	private static String PLUG_IN_JAR_NEW_PATH = LIB
			+ "plugin.jar";
    

    private static Set<Jar> ADDED_JARS = new HashSet<Jar>();
    private static Set<Jar> ALL_JARS = new HashSet<Jar>();

    /**
     * Check a new jar will be added to server or not.
     * 
     * @param path
     * @return
     */
    public static boolean accept(String path)
    {
    	boolean isAccept = false;
        for (Jar jar : getAllJars())
        {
            if (jar.accept(path))
            {
                ADDED_JARS.add(jar);
                isAccept = true;
            }
        }

        return isAccept;
    }

    private static Set<Jar> getAllJars()
    {
        if (ALL_JARS.size() == 0)
        {
            ALL_JARS.add(new Jar(THIRD_PARTY2_JAR, NTHIRD_PARTY2_NEW_JAR,
                    "/thirdParty2"));
            ALL_JARS.add(new Jar(THIRD_PARTY1_JAR, NTHIRD_PARTY1_NEW_JAR,
                    "/thirdParty1"));
            ALL_JARS.add(new Jar(INSTALL_JAR_PATH, INSTALL_JAR_NEW_PATH,
                    "/tools/install/"));
            ALL_JARS.add(new Jar(GRAPHICAL_WF_JAR_PATH,
                    GRAPHICAL_WF_JAR_NEW_PATH, "/gui/planview/", "/everest/webapp/applet/common/", "/SortUtil"));
            ALL_JARS.add(new Jar(CUSTOMER_JAR_PATH, CUSTOMER_JAR_NEW_PATH,
                    "/everest/webapp/applet/", "/zip/ZipIt"));
            ALL_JARS.add(new Jar(PROMT_UTIL_JAR, PROMT_UTIL_NEW_JAR,
                    "/promtUtil.jar"));
            ALL_JARS.add(new Jar(ONLINE_JAR_PATH, ONLINE_JAR_NEW_PATH,
                    "/ling/com/globalsight/ling"));
            ALL_JARS.add(new Jar(ONLINE_JAR_PATH, ONLINE_JAR_NEW_PATH,
                    "/edit/SegmentUtil"));
            ALL_JARS
                    .add(new Jar(AXIS_JAR, AXIS_NEW_JAR, "/ApacheAxis/axis.jar"));
            ALL_JARS.add(new Jar(MAIL_JAR_PATH, MAIL_JAR_NEW_PATH,
                    "/server/default/lib/mail.jar"));
            ALL_JARS.add(new Jar(ADD_SOURCE_JAR_PATH, ADD_SOURCE_JAR_NEW_PATH,
                    "/src/applet/AddSourceApplet/"));
            ALL_JARS.add(new Jar(XML_APIS_JAR_PATH, XML_APIS_JAR_NEW_PATH,
                    "/lib/endorsed/xml-apis.jar"));
            ALL_JARS.add(new Jar(XERCESIMPL_JAR_PATH, XERCESIMPL_JAR_NEW_PATH,
                    "/lib/endorsed/xercesImpl.jar"));
            ALL_JARS.add(new Jar(XERCES_XML_JAR_PATH, XERCES_XML_JAR_NEW_PATH,
                    "/lib/ApacheAxis/xerces-xml.jar"));
            ALL_JARS.add(new Jar(SSO_LIB_JAR_PATH, SSO_LIB_JAR_NEW_PATH,
                    "/main6/tools/lib/ssoLibs"));
			ALL_JARS.add(new Jar(DB_PROFILE_JAR_PATH, DB_PROFILE_JAR_NEW_PATH,
					"/applet/admin/dbprofile/"));
			ALL_JARS.add(new Jar(CREATE_JOB_JAR_PATH, CREATE_JOB_JAR_NEW_PATH,
					"/applet/createjob/"));
			ALL_JARS.add(new Jar(PLUG_IN_JAR_PATH, PLUG_IN_JAR_NEW_PATH,
					"/plugin.jar"));
        }

        return ALL_JARS;
    }

    public static Set<Jar> getAddedJars()
    {
        return ADDED_JARS;
    }

    public static void printAddedJars()
    {
        if (ADDED_JARS.size() > 0)
        {
            log("");
            log("Jars:");
            for (Jar f : ADDED_JARS)
            {
                log(f.getName());
            }
        }
    }

    private static void log(String msg)
    {
        System.out.println(msg);
        log.info(msg);
    }

    /**
     * Adds all jars to server.
     */
    public static void addJars()
    {
        for (Jar jar : ADDED_JARS)
        {
            jar.add();
        }
    }
}
