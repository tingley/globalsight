package com.globalsight.everest.foundation.sso;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.sun.identity.shared.encode.Base64;

/**
 * Utility that uses OpenSAML to carry out common SAML tasks.
 */

public abstract class SSOSPHelper
{
    private static HashMap<String, SSOParameter> ssoStore;

    /**
     * Any use of this class assures that OpenSAML is bootstrapped. Also
     * initializes an ID generator.
     */
    static
    {
        try
        {
            ssoStore = new HashMap<String, SSOParameter>();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Create SSO Assertion String
     * 
     * @param userName
     * @param pwd
     * @param backTo
     * @return SSO Assertion
     * @throws Exception
     */
    public abstract String createSSOAssertion(String userName, String pwd, String companyName, String backTo)
            throws Exception;
    /**
     * Handle SSO Response
     * @param response
     * @return SSOResponse
     * @throws Exception
     */
    public abstract SSOResponse handleSSOResponse(String response) throws Exception;
    
    public static SSOParameter getParameter(String inResponseTo)
    {
        return ssoStore.get(inResponseTo);
    }
    
    protected void storeParameter(String inResponseTo, String companyName, String ssoUser)
    {
        SSOParameter para = new SSOParameter();
        para.setParameter(SSOParameter.COMPANY_NAME, companyName);
        para.setParameter(SSOParameter.SSO_USER_NAME, ssoUser);
        ssoStore.put(inResponseTo, para);
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
    
    public static SSOSPHelper createInstance(boolean useSaml) throws Exception
    {
        if (useSaml)
        {
            return new SSOSPHelperSaml();
        }
        else
        {
            return new SSOSPHelperSimple();
        }
    }
}
