package com.globalsight.everest.foundation.sso;

import com.sun.identity.saml2.common.SAML2SDKUtils;

/**
 * Utility that uses simple string to handle SSO tasks.
 */

public class SSOSPHelperSimple extends SSOSPHelper
{
    /**
     * Initialize JAXP DocumentBuilder instance for later use and reuse
     * 
     * @throws Exception
     */
    public SSOSPHelperSimple() throws Exception
    {
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
    public String createSSOAssertion(String userName, String pwd, String companyName, String backTo)
            throws Exception
    {
        String inResponseTo = SAML2SDKUtils.generateID();
        storeParameter(inResponseTo, companyName, userName);

        return inResponseTo + "|" + encodeContent(userName) + "|" + encodeContent(pwd) + "|"
                + encodeContent(backTo);
    }

    /**
     * Handle SSO Response
     * 
     * @param response
     * @return SSOResponse
     * @throws Exception
     */
    public SSOResponse handleSSOResponse(String response) throws Exception
    {
        SSOResponse result = new SSOResponse();

        if (response != null)
        {
            // inResponseTo or 0|login|msg
            // 1|userId|companyname
            String[] res = response.split("\\|");
            String s1 = res[0];
            String s2 = decodeContent(res[1]);
            String s3 = decodeContent(res[2]);

            boolean loginSuccess = false;
            String statusCode = "";
            String msg = "";
            String userId = "";
            String companyName = "";
            String inResponseTo = "";

            if ("1".equals(s1))
            {
                loginSuccess = true;
                userId = s2;
                companyName = s3;
            }
            else
            {
                String s4 = decodeContent(res[3]);
                if (!"0".equals(s1))
                {
                    inResponseTo = s1;
                }

                userId = s2;
                loginSuccess = "true".equalsIgnoreCase(s3);
                msg = s4;
            }

            result.setLoginSuccess(loginSuccess);
            result.setStatusCode(statusCode);
            result.setStatusMessage(msg != null ? msg : "");
            result.setUserId(userId);
            result.setInResponseTo(inResponseTo != null ? inResponseTo : "");
            result.setCompanyName(companyName);
        }
        else
        {
            result.setLoginSuccess(false);
        }

        return result;
    }
}
