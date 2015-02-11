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
package com.globalsight.diplomat.servlet.ambassador;

//jdk
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.Security;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Enumeration;
import java.util.Date;


//JSSE
import javax.net.ssl.SSLSocketFactory;
import com.sun.net.ssl.X509TrustManager;
import com.sun.net.ssl.KeyManager;
import com.sun.net.ssl.TrustManager;
import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.HttpsURLConnection;

//Servlet
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//globalsight
import com.globalsight.diplomat.util.Logger;
import com.globalsight.everest.util.system.SystemConfiguration;

/**
 * The DynamicPreviewProxyServlet acts as a proxy to call the
 * CapExportServlet. This ensures that the call from the browser
 * first goes to the CXE webserver, and thus avoids security problems
 * with Netscape 6.
 * The usage should be the same as with the CapExportServlet.
 */
public class DynamicPreviewProxyServlet extends HttpServlet
{
public DynamicPreviewProxyServlet()
  throws ServletException
  {
      try {
	  theLogger.setLogname("CxeServlets");
      }catch (IOException e) {
	  throw new ServletException(e);
      }
      
      try {
         //set up to handle SSL
         System.setProperty("java.protocol.handler.pkgs",
                            "com.sun.net.ssl.internal.www.protocol");
         System.setProperty("ja    vax.net.debug",
                            "ssl,hands     hake,data,trustmanager");
         Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
         
         //use our own trust manager so we can always trust
         //the URL entered in the configuration.
         X509TrustManager tm = new MyX509TrustManager();
         KeyManager []km = null;
         TrustManager []tma = {tm};
         SSLContext sc = SSLContext.getInstance("ssl");
         sc.init(km,tma,new java.security.SecureRandom());
         SSLSocketFactory sf1 = sc.getSocketFactory();
         HttpsURLConnection.setDefaultSSLSocketFactory(sf1);
         theLogger.println (Logger.INFO,
                            "Using Java Secure Sockets Extension (JSSE) to handle SSL.");
      }
      catch (SecurityException se)
      {
         theLogger.printStackTrace(Logger.ERROR,
                                   "Cannot add Java Secure Sockets Extension (JSSE) security provider to handle SSL in java.", se);
         throw new ServletException(se.toString());
      }
      catch (NoClassDefFoundError ncdfe)
      {
         theLogger.printStackTrace(Logger.ERROR,
                                   "Cannot find Java Secure Sockets Extension (JSSE) security provider class to handle SSL in java.", ncdfe);
         throw new ServletException(ncdfe.toString());
      }
      catch (NoSuchAlgorithmException nsae)
      {
         theLogger.printStackTrace(Logger.ERROR,
                                   "Cannot use Java Secure Sockets Extension (JSSE) SSL Algorithm.", nsae);
         throw new ServletException(nsae.toString());
      }
      catch (KeyManagementException kme)
      {
         theLogger.printStackTrace(Logger.ERROR,
                                   "Cannot use Java Secure Sockets Extension (JSSE) Key Management.", kme);
         throw new ServletException(kme.toString());
      }
  }

   public void doGet(HttpServletRequest request,
                     HttpServletResponse response)
      throws IOException, ServletException
   {
      doPost(request,response);
   }

   public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
      throws IOException, ServletException
   {
      theLogger.println(Logger.DEBUG_D,
                        "DynamicPreviewProxyServlet: proxying.");
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();

      try {
          SystemConfiguration config = SystemConfiguration.getInstance();
          String nonSSLPort = config.getStringParameter("nonSSLPort"); 
          String prefix;
          
          //for proxing, use the non SSL port always
          prefix = "http://" + request.getServerName() + ":" + nonSSLPort;

         StringBuffer prsexportUrl = new StringBuffer (prefix);
         prsexportUrl.append("/globalsight/CapExportServlet?");

         //now append all the parameters the proxy was called with
         Enumeration names = request.getParameterNames();
         int i=0;
         while (names.hasMoreElements())
         {
            String p = (String) names.nextElement(); //get a parameter
            String[] values = request.getParameterValues(p);
            for (i=0; i < values.length; i++)
            {
               prsexportUrl.append(p + "=" + values[i] + "&");
            }
         }

         //now append a timestamp
         Date theDate = new Date();
         String timestamp = "cxeTimeStamp=" + Long.toString(theDate.getTime());

         theLogger.println(Logger.DEBUG_D,"DynamicPreviewProxyServlet: url is "
                           + prsexportUrl.toString());
         URL url = new URL (prsexportUrl.toString());

         //now call PrsExport and write out the response
         BufferedReader rd = new BufferedReader( new InputStreamReader(
            url.openStream()));
         String line = null;
         theLogger.println(Logger.DEBUG_D, "DynamicPreviewProxyServlet: reading export servlet output.");
         while ((line = rd.readLine()) != null)
            out.println(line);
         rd.close();
         theLogger.println(Logger.DEBUG_D, "DynamicPreviewProxyServlet: exiting.");
         out.close();
      }
      catch (Exception e)
      {
         theLogger.printStackTrace(Logger.ERROR,
                                   "Cannot proxy CapExportServlet:", e);
         out.println("<html>\n<body><p> ERROR: Cannot call CapExportServlet: " +
                     e.toString() + "</p>");
         out.println("</body></html>");
      }
   }

   private Logger theLogger = Logger.getLogger();

   //Always trust CAP
   class MyX509TrustManager implements X509TrustManager
   {
      public boolean isClientTrusted(X509Certificate[]
                                     chain)
      {
         return true;
      }
      
      public boolean isServerTrusted(X509Certificate[]
                                     chain)
      {
         return true;
      }
      
      public X509Certificate[] getAcceptedIssuers()
      {return null;}

      private void printCertificateDetails (X509Certificate[] chain)
      {
         theLogger.println(Logger.DEBUG_D, "There are " + chain.length
                           + " X509 certificates.");
         X509Certificate theCert = null;
         for (int i=0; i < chain.length; i++)
         {
            theCert = chain[i];
            try {
               theCert.checkValidity();
               theLogger.println(Logger.DEBUG_D, "Certificate " + i +
                                 " is valid.");
            }
            catch (CertificateExpiredException cee)
            {
               theLogger.println(Logger.ERROR, "Certificate  " + i +
                                 " is expired.");
            }
            catch (CertificateNotYetValidException cnyve)
            {
               theLogger.println(Logger.ERROR, "Certificate  " + i +
                                 "is not yet valid.");
            }
            
            theLogger.println(Logger.DEBUG_D, "Certificate number is: " +
                              theCert.getSerialNumber());
            theLogger.println(Logger.DEBUG_D, "Certificate version is: " +
                              theCert.getVersion());
            theLogger.println(Logger.DEBUG_D, "Issuer is " +
                              theCert.getIssuerDN());
            theLogger.println(Logger.DEBUG_D, "Subject is " +
                              theCert.getSubjectDN());
         }
      }
   }
}
