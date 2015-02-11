package com.globalsight.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import com.globalsight.entity.Host;
import com.globalsight.entity.User;
import com.globalsight.util2.CacheUtil;
import com.globalsight.www.webservices.Ambassador;
import com.globalsight.www.webservices.AmbassadorServiceLocator;

public class WebClientHelper
{

    private static Logger log = Logger.getLogger(WebClientHelper.class.getName());

    public static Ambassador getAmbassador() throws Exception
    {
        User u = CacheUtil.getInstance().getLoginingUser();
        if (u == null)
        {
            u = CacheUtil.getInstance().getCurrentUser();
        }
        Host h = u.getHost();
        String userName = u.getName();
        String password = u.getPassword();
        String prefix = u.isUseSSL() ? "https://" : "http://";
        String wsdlUrl = prefix + h.getName() + ":" + h.getPort()
                + "/globalsight/services/AmbassadorWebService?wsdl";
        
        if (u.isUseSSL())
        {
            acceptAllCerts();
        }
        
        AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
        Ambassador service = loc.getAmbassadorWebService(new URL(wsdlUrl));
        setValue(service, "cachedUsername", userName);
        setValue(service, "cachedPassword", password);
        
        return service;
    }
    
    private static void setValue(Object obj, String name, String value)
    {
        setValue(obj, obj.getClass(), name, value);
    }
    
    private static void setValue(Object obj, Class clazz, String name, String value)
    {
        try
        {
            Field[] fs = clazz.getDeclaredFields();
            for (Field f : fs)
            {
                if (name.equals(f.getName()))
                {
                    f.setAccessible(true);
                    f.set(obj, value);
                    return;
                }
            }
            
            Class superClass = clazz.getSuperclass();
            if (superClass != null)
            {
                setValue(obj, superClass, name, value);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }

    public static Ambassador getAmbassador(String hostName, String port, boolean useSSL)
            throws Exception
    {
        User u = CacheUtil.getInstance().getLoginingUser();
        if (u == null)
        {
            u = CacheUtil.getInstance().getCurrentUser();
        }
        String userName = u == null ? "" : u.getName();
        String password = u == null ? "" : u.getPassword();
        String prefix = useSSL ? "https://" : "http://";
        String wsdlUrl = prefix + hostName + ":" + port
                + "/globalsight/services/AmbassadorWebService?wsdl";
        
        if (useSSL)
        {
            acceptAllCerts();
        }
        
        AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
        Ambassador service = loc.getAmbassadorWebService(new URL(wsdlUrl));
        setValue(service, "cachedUsername", userName);
        setValue(service, "cachedPassword", password);
        return service;
    }

    public static void acceptAllCerts() throws Exception
    {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
        {
            public java.security.cert.X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                    String authType)
            {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                    String authType)
            {
            }
        } };

        // Let's create the factory where we can set some parameters for the
        // connection
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    public static boolean installCert(String host, String port) throws Exception
    {
        int p = Integer.parseInt(port);
        return installCert(host, p);
    }

    public static boolean installCert(String host, int port) throws Exception
    {
        char[] passphrase = "changeit".toCharArray();
        char SEP = File.separatorChar;
        File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
        File file_jssecacerts = new File(dir, "jssecacerts");
        if (file_jssecacerts.isFile() == false)
        {
            file_jssecacerts = new File(dir, "cacerts");
        }

        log.info("Loading KeyStore " + file_jssecacerts + "...");
        InputStream in = new FileInputStream(file_jssecacerts);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, passphrase);
        in.close();

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory
                .getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[] { tm }, null);
        SSLSocketFactory factory = context.getSocketFactory();

        log.info("Opening ssl connection to " + host + ":" + port + "...");
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);
        try
        {
            log.info("Starting SSL handshake...");
            socket.startHandshake();
            socket.close();
            log.info("No errors, certificate is already trusted");
            return false;
        }
        catch (SSLException e)
        {
            log.error(e.getMessage(), e);
        }

        X509Certificate[] chain = tm.chain;
        if (chain == null)
        {
            log.info("Could not obtain server certificate chain");
            return false;
        }

        for (int i = 0; i < chain.length; i++)
        {
            X509Certificate cert = chain[i];
            String alias = host + "-" + (i + 1);
            ks.setCertificateEntry(alias, cert);

            log.info(cert);
            log.info("Added certificate to keystore 'jssecacerts' using alias '" + alias + "'");
        }

        OutputStream out = new FileOutputStream(new File(dir, "jssecacerts"));
        ks.store(out, passphrase);
        out.close();
        log.info("Save key store successfully! Please restart DesktopIcon.");
        return true;
    }
}

class SavingTrustManager implements X509TrustManager
{
    private final X509TrustManager tm;
    public X509Certificate[] chain;

    SavingTrustManager(X509TrustManager tm)
    {
        this.tm = tm;
    }

    public X509Certificate[] getAcceptedIssuers()
    {
        throw new UnsupportedOperationException();
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException
    {
        throw new UnsupportedOperationException();
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException
    {
        this.chain = chain;
        tm.checkServerTrusted(chain, authType);
    }
}
