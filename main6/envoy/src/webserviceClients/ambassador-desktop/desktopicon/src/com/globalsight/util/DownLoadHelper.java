package com.globalsight.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import com.globalsight.entity.User;

public class DownLoadHelper
{

	static Logger log = Logger.getLogger(DownLoadHelper.class.getName());

	public static File downloadHttps(String downloadURL, User p_owner, String commonPathURL) throws Exception
	{
		enableHttpsConnection();
		
		String urlDecode = URLDecoder.decode(downloadURL, "UTF-8").replaceAll("\\\\", "/").replace(" ", "%20");
		String realPath = URLDecoder.decode(commonPathURL, "UTF-8").replaceAll("\\\\", "/").replace(" ", "%20");
		log.info("The https URL1 is " + urlDecode);
		URL url = new URL(urlDecode);
		HttpsURLConnection hurl = (HttpsURLConnection) url.openConnection();
		hurl.connect();
		InputStream is = hurl.getInputStream();

		urlDecode = urlDecode.replace("%20", " ");
		realPath = realPath.replace("%20", " ");
		String fullPath = getFileName(realPath, p_owner);
		
		File file = new File(fullPath);
		saveFile(is, file);
		return file;
	}

    public static void enableHttpsConnection() throws NoSuchAlgorithmException,
            KeyManagementException
    {
        MyX509TrustManager xtm = new MyX509TrustManager();
		MyHostnameVerifier hnv = new MyHostnameVerifier();
		SSLContext sslContext = null;
		sslContext = SSLContext.getInstance("TLS");
		X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };
		sslContext.init(null, xtmArray, new java.security.SecureRandom());
		if (sslContext != null)
		{
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
					.getSocketFactory());
		}
		HttpsURLConnection.setDefaultHostnameVerifier(hnv);
    }

	public static File downloadHttp(String downloadURL, User p_owner, String commonPathURL) throws Exception
	{
		log.info("The http URL1 is " + downloadURL);
		String urlDecode = URLDecoder.decode(downloadURL, "UTF-8").replaceAll("\\\\", "/");
		log.info("The http URL2 is " + urlDecode);
		urlDecode = urlDecode.replace(" ", "%20");
		log.info("The http URL3 is " + urlDecode);
		String realPath = URLDecoder.decode(commonPathURL, "UTF-8").replaceAll("\\\\", "/").replace(" ", "%20");
        
		URL url = new URL(downloadURL);
		HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
		hurl.connect();
		InputStream is = hurl.getInputStream();
		
		urlDecode = urlDecode.replace("%20", " ");
		realPath = realPath.replace("%20", " ");
        
		String fullPath = getFileName(realPath, p_owner);
		File file = new File(fullPath);
		saveFile(is, file);
		
		return file;
	}

	private static void saveFile(InputStream is, File file) throws IOException,
			FileNotFoundException
	{
		file.getParentFile().mkdirs();
		file.createNewFile();
		FileOutputStream outstream = new FileOutputStream(file);
		int c;
		while ((c = is.read()) != -1)
		{
			outstream.write(c);
		}
		outstream.close();
		is.close();
		if (file.length() == 0)
		{
			file.delete();
		}
	}

	private static String getFileName(String url, User p_owner) throws Exception
	{
		String[] url1 = url.split("cxedocs");
		String after = url1[1];
		String before = p_owner.getSavepath();
		if (before == null)
		{
			before = new File(".").getAbsolutePath();
		}
		String fileName = before + after;
		return fileName;
	}
}
