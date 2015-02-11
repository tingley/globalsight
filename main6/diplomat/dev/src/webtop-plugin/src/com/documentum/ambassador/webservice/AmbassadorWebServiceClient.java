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
package com.documentum.ambassador.webservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.IOException;

import org.apache.axis.AxisProperties;

import javax.xml.rpc.ServiceException;

import org.xml.sax.SAXException;

import com.documentum.ambassador.util.FileProfile;
import com.documentum.ambassador.util.ParseFileProfileXml;
import com.globalsight.www.webservices.Ambassador;
import com.globalsight.www.webservices.AmbassadorServiceLocator;
import com.globalsight.webservices.WebServiceException;

/**
 * The <code>AmbassadorWebServiceClient</code> class acts as web service
 * client, accesses web service located in the GlobalSight side, and provides
 * necessary functionalities for Webtop plug-in to use.
 * 
 */
public class AmbassadorWebServiceClient {

    private String m_accessToken = null;
    private String m_userId = null;
    private Ambassador provider = null;
    private static AmbassadorWebServiceClient wsClient = null;
    
    /**
     * Used to save all the file profiles. (key=fpId, value=FileProfile)
     */
    private static Map fpMap = null;

    //Exception Message
    private static final String SERVICE_EXCEPTION_MSG = "Error: Can't initialize the web service client.";

    private static final String REMOTE_EXCEPTION_MSG = "Error: Can't locate the web service provider.";
    
    private static final String URL_EXCEPTION_MSG = "Error: The url is wrong to access the web service provider.";

    private static final String WEBSERVICE_EXCEPTION_MSG = "Error: Exception occurs when excuting the remote method.";

    private static final String HTTP_PROTOCOL = "http://";

    private static final String HTTPS_PROTOCOL = "https://";

    private static final String DELIMITER = ":";
    
    /**
     * Constructor, initialize the web service client.
     */
    private AmbassadorWebServiceClient() {
        login();        
    }
    
    /**
     * Locate the web service and login the GlobalSight via web service.
     */
    private void login() {
        
        //Read the configuration file.
        AmbassadorConfiguration ambConf = AmbassadorConfiguration.getInstance();
        String hostName = ambConf
                .getPropertyValue(AmbassadorConfiguration.AMB_HOSTNAME);
        String httpPort = ambConf
                .getPropertyValue(AmbassadorConfiguration.AMB_HTTP_PORT);
        String httpsPort = ambConf
                .getPropertyValue(AmbassadorConfiguration.AMB_HTTPS_PORT);
        String wsURL = ambConf
                .getPropertyValue(AmbassadorConfiguration.AMB_WEBSERVICE_URL);
        String ambUserName = ambConf
                .getPropertyValue(AmbassadorConfiguration.AMB_USERNAME);
        String ambPassword = ambConf
                .getPropertyValue(AmbassadorConfiguration.AMB_PASSWORD);
        String dctmRepository = ambConf
                .getPropertyValue(AmbassadorConfiguration.DCTM_REPOSITORY);
        String dctmUserName = ambConf
                .getPropertyValue(AmbassadorConfiguration.DCTM_USERNAME);
        String dctmPassword = ambConf
                .getPropertyValue(AmbassadorConfiguration.DCTM_PASSWORD);

        String wsdlHttpURL = HTTP_PROTOCOL + hostName + DELIMITER + httpPort
                + wsURL;
        String wsdlHttpsURL = HTTPS_PROTOCOL + hostName + DELIMITER + httpsPort
                + wsURL;
        
        AxisProperties.setProperty("axis.socketSecureFactory",
                "org.apache.axis.components.net.SunFakeTrustSocketFactory");
        //Locate the web service and login GlobalSight server via web service.
        try {
            AmbassadorServiceLocator serviceLocator = 
            new AmbassadorServiceLocator();
            provider = serviceLocator.getAmbassadorWebService(
                    new URL(wsdlHttpURL) );
            Ambassador httpsProvider = null;
            try {
                httpsProvider = serviceLocator
                    .getAmbassadorWebService(new URL(wsdlHttpsURL));
            } catch (Exception e) {
                e.printStackTrace();
                httpsProvider = provider;    
            }
            m_accessToken = httpsProvider.login(ambUserName, ambPassword);            
            m_userId = httpsProvider.passDCTMAccount(m_accessToken,
                    dctmRepository, dctmUserName, dctmPassword);
        } catch (ServiceException svcex) {
            System.out.println(SERVICE_EXCEPTION_MSG);
            svcex.printStackTrace();
            throw new AmbassadorWebserviceException(svcex.getLocalizedMessage());
        } catch (WebServiceException wsex) {
            System.out.println(WEBSERVICE_EXCEPTION_MSG);
            wsex.printStackTrace();
            throw new AmbassadorWebserviceException(wsex.getLocalizedMessage());
        } catch (RemoteException rmtex) {
            System.out.println(REMOTE_EXCEPTION_MSG);
            rmtex.printStackTrace();
            throw new AmbassadorWebserviceException(rmtex.getLocalizedMessage());
        } catch(MalformedURLException urlex) {
            System.out.println(URL_EXCEPTION_MSG);
            urlex.printStackTrace();
            throw new AmbassadorWebserviceException(urlex.getLocalizedMessage());
        }
    }
    
    /**
     * Ensure to get the unique AmbassadorWebServiceClient Object.
     */
    public static AmbassadorWebServiceClient getInstance() {

        if (wsClient == null) {
            wsClient = new AmbassadorWebServiceClient();
        }        
        return wsClient;
    }

    /**
     * @see getFileProfileByFilter(String, Set, forceRefresh).
     */
    public List getFileProfileByFilter(String fileExt, 
            String sourceLocale, Set locales) {
        return getFileProfileByFilter(fileExt, sourceLocale, locales, false);
    }

    /**
     * Filter file profiles by file extension and target locales, and get a set
     * of file profiles which include the specified file extension and target
     * locales.
     * 
     * @param fileExt -
     *            Specify a file extension.
     * @param locales -
     *            Indicate a set of target locales.
     * @param forceRefresh -
     *            Set true to force webtop client to get file profile info from
     *            GlobalSight side again.
     * @return a set of file profiles.
     */
    public synchronized List getFileProfileByFilter(String fileExt,
            String sourceLocale, Set locales, boolean forceRefresh) {
        
        // if necessary, force to load the fileprofile info from GlobalSight side
        // again.
        if (fpMap == null || forceRefresh) {
            loadFileProfileInfoEx();
        }

        List fileProfiles = new ArrayList();
        Set fpIds = null;
        if( fpMap != null ) {
            fpIds = fpMap.keySet();
        } else {
            fpIds = new HashSet();
        }
        
        Iterator iter = fpIds.iterator();

        while (iter.hasNext()) {
            FileProfile fp = (FileProfile)fpMap.get(iter.next());
            Set fileExtensions = fp.getFileExtensions();
            Set targetLocales = fp.getTargetLocale();
            //All the target locales must match completely and exactly.
            // The file extensions of a file profile must include that which
            // passed in.
            if ((fp.getSourceLocale().equalsIgnoreCase(sourceLocale))
                    && (locales == null || targetLocales.equals(locales))
                    && (fileExt == null || fileExtensions.contains(fileExt) 
                            || fileExtensions.isEmpty())) {
                fileProfiles.add(fp);
            }
        }
        
        return fileProfiles;
    }

    /**
     * Get all the file profiles info as a XML String from GlobalSight side,
     * parse it into FileProfile object and save it into a HashMap.
     */
    public synchronized void loadFileProfileInfoEx() {

        try {
            String fpXmlStr = null;
            fpXmlStr = provider.getFileProfileInfoEx(m_accessToken);
            fpMap = ParseFileProfileXml.parseFPXml(fpXmlStr);
        } catch (WebServiceException wsex) {
            resetWebservice();
            System.out.println(WEBSERVICE_EXCEPTION_MSG);
            wsex.printStackTrace();
            throw new AmbassadorWebserviceException(wsex.getLocalizedMessage());
        } catch (RemoteException rmtex) {
            resetWebservice();
            System.out.println(REMOTE_EXCEPTION_MSG);
            rmtex.printStackTrace();
            throw new AmbassadorWebserviceException(rmtex.getLocalizedMessage());
        } catch(SAXException saxex) {
            saxex.printStackTrace();
            throw new AmbassadorWebserviceException(saxex.getLocalizedMessage());
        } catch(IOException ioex) {
            ioex.printStackTrace();
            throw new AmbassadorWebserviceException(ioex.getLocalizedMessage());
        }
    }
    
    /**
     * Create a documentum job via job name, fileprofile id, DCTM object id.
     * 
     * @param jobName -
     *            A documentum job name.
     * @param fpId -
     *            Fileprofile Id this job will use.
     * @param objectId -
     *            DCTM object Id provide the translatable content.
     */
    public void createDocumentumJob(String jobName, String fpId, String objectId) {

        try {
            provider.createDocumentumJob(m_accessToken, jobName, fpId,
                    objectId, m_userId);
        } catch (WebServiceException wsex) {
            resetWebservice();
            System.out.println(WEBSERVICE_EXCEPTION_MSG);
            wsex.printStackTrace();
            throw new AmbassadorWebserviceException(wsex.getLocalizedMessage());
        } catch (RemoteException rex) {
            resetWebservice();
            System.out.println(REMOTE_EXCEPTION_MSG);
            rex.printStackTrace();
            throw new AmbassadorWebserviceException(rex.getLocalizedMessage());
        }    
    }
    
    /**
     * Cancel a Documentum Job via job id.
     */
    public void cancelDocumentumJob(String objId, String jobId) {

        try {
            provider.cancelDocumentumJob(m_accessToken, objId, jobId, m_userId);
        } catch (WebServiceException wsex) {
            resetWebservice();
            System.out.println(WEBSERVICE_EXCEPTION_MSG);
            wsex.printStackTrace();
        } catch (RemoteException rex) {
            resetWebservice();
            System.out.println(REMOTE_EXCEPTION_MSG);
            rex.printStackTrace();
            throw new AmbassadorWebserviceException(rex.getLocalizedMessage());
        }
        
    }
    
    public static void  resetWebservice() {
        AmbassadorWebServiceClient.wsClient = null;
        System.out.println(" ##### The web service is set to NULL, it should reconnect next time.");
    }
}
