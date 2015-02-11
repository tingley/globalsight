package com.globalsight.everest.foundation.sso;

import java.io.UnsupportedEncodingException;

import com.sun.identity.shared.encode.Base64;

public abstract class SSOIdPHelper
{
    /**
     * 
     * @param request
     * @param accountManager
     * @return String array, which index 0 is sso response and index 1 is backTo
     *         url
     * @throws Exception
     */
    public abstract String[] handleSSORequest(String request, SSOAccountManager accountManager)
            throws Exception;

    public abstract String createLoginResponse(String userId, String companyName) throws Exception;
    
    public static SSOIdPHelper createInstance(boolean useSaml) throws Exception
    {
        if (useSaml)
        {
            return new SSOIdPHelperSaml();
        }
        else
        {
            return new SSOIdPHelperSimple();
        }
    }
    
    protected String encodeContent(String content) throws UnsupportedEncodingException
    {
        byte[] data = content.getBytes("utf-8");
        String base64 = Base64.encode(data);
        return base64;
    }
    
    protected String decodeContent(String content) throws UnsupportedEncodingException
    {
        byte[] data = Base64.decode(content);
        String result = new String(data, "utf-8");
        return result;
    }
}
