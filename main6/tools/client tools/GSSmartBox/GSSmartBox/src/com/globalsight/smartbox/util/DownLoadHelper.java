/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
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
package com.globalsight.smartbox.util;

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

public class DownLoadHelper {
	public static File downloadHttps(String downloadURL, String basePath, String commonPathURL) throws Exception {
		enableHttpsConnection();

		String urlDecode = URLDecoder.decode(downloadURL, "UTF-8").replaceAll("\\\\", "/").replace(" ", "%20");

		URL url = new URL(urlDecode);
		HttpsURLConnection hurl = (HttpsURLConnection) url.openConnection();
		hurl.connect();
		InputStream is = hurl.getInputStream();

		String realPath = URLDecoder.decode(commonPathURL, "UTF-8").replaceAll("\\\\", "/").replace(" ", "%20");
		realPath = realPath.replace("%20", " ");
		String fullPath = getFileName(realPath, basePath);

		File file = new File(fullPath);
		saveFile(is, file);
		return file;
	}

	public static void enableHttpsConnection() throws NoSuchAlgorithmException, KeyManagementException {
		MyX509TrustManager xtm = new MyX509TrustManager();
		MyHostnameVerifier hnv = new MyHostnameVerifier();
		SSLContext sslContext = null;
		sslContext = SSLContext.getInstance("TLS");
		X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };
		sslContext.init(null, xtmArray, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(hnv);
	}

	public static File downloadHttp(String downloadURL, String basePath, String commonPathURL) throws Exception {
		String urlDecode = URLDecoder.decode(downloadURL, "UTF-8").replaceAll("\\\\", "/");
		urlDecode = urlDecode.replace(" ", "%20");

		URL url = new URL(urlDecode);
		HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
		hurl.connect();
		InputStream is = hurl.getInputStream();

		String realPath = URLDecoder.decode(commonPathURL, "UTF-8").replaceAll("\\\\", "/").replace(" ", "%20");
		realPath = realPath.replace("%20", " ");

		String fullPath = getFileName(realPath, basePath);
		File file = new File(fullPath);
		saveFile(is, file);

		return file;
	}

	private static void saveFile(InputStream is, File file) throws IOException, FileNotFoundException {
		file.getParentFile().mkdirs();
		file.createNewFile();
		FileOutputStream outstream = new FileOutputStream(file);
		int c;
		while ((c = is.read()) != -1) {
			outstream.write(c);
		}
		outstream.close();
		is.close();
		if (file.length() == 0) {
			file.delete();
		}
	}

	private static String getFileName(String url, String basePath) throws Exception {
		String[] url1 = url.split("cxedocs");
		String after = url1[1];
		String before = basePath;
		if (before == null) {
			before = new File(".").getAbsolutePath();
		}
		String fileName = before + after;
		return fileName;
	}
}
