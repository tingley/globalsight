package com.globalsight.webservices.client2;

import java.io.File;
import java.io.FileInputStream;

public class Client2Tester
{
    private static int MAX_SEND_SIZE = 5 * 1000 * 1024;// 5M
    private static String HOST_NAME = "10.10.215.20";
    private static String HOST_PORT = "8080";
    private static String userName = "yorkadmin";
    private static String password = "password";

    public static Ambassador2 getAmbassador() throws Exception
    {
        Ambassador2 ambassador = WebService2ClientHelper.getClientAmbassador2(
                HOST_NAME, HOST_PORT, userName, password, false);
        return ambassador;
    }

    public static Ambassador2 getAmbassador(String userName, String password)
            throws Exception
    {
        Ambassador2 ambassador = WebService2ClientHelper.getClientAmbassador2(
                HOST_NAME, HOST_PORT, userName, password, false);
        return ambassador;
    }

    public static void main(String[] args)
    {
        try
        {
            Ambassador2 ambassador = getAmbassador(userName, password);
            String fullAccessToken = ambassador.dummyLogin(userName, password);
            System.out.println("fullAccessToken : " + fullAccessToken);

            byte[] content = null;
            File file = new File("c:/Welocalize1.html");
            content = new byte[(int) file.length()];
            FileInputStream fin = new FileInputStream(file);
            fin.read(content, 0, (int) file.length());
            ambassador.uploadFiles(
                            fullAccessToken,
                            "2",
                            1,
                            "createJob_tmp\\201211161722-911724879\\Documents and Settings\\york.jin\\Desktop\\2694\\Welocalize_Company1.html",
                            content);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
}
