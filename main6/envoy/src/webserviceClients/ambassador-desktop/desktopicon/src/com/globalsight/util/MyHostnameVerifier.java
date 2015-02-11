package com.globalsight.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class MyHostnameVerifier implements HostnameVerifier{

    public MyHostnameVerifier(){}
    public boolean verify(String hostname,SSLSession session) 
    {
    	return true;
    }
}
