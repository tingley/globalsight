package com.globalsight.everest.foundation.sso;

public class SSOIdPHelperSimple extends SSOIdPHelper
{
    public SSOIdPHelperSimple() throws Exception
    {
    }

    /**
     * 
     * @param request
     * @param accountManager
     * @return String array, which index 0 is sso response and index 1 is backTo
     *         url
     * @throws Exception
     */
    public String[] handleSSORequest(String request, SSOAccountManager accountManager)
            throws Exception
    {
        if (request == null || "".equals(request.trim()) || accountManager == null)
        {
            throw new NullPointerException("Argument can not be null or empty.");
        }

        String username = "";
        String password = "";
        String backTo = "";
        String inResponseTo = "";

        // 1 read request
        // inResponseTo|username|password|backTo
        String[] reqs = request.split("\\|");
        inResponseTo = reqs[0];
        username = decodeContent(reqs[1]);
        password = decodeContent(reqs[2]);
        backTo = decodeContent(reqs[3]);
        
        // 2 login
        int loginResult = accountManager.loginUser(username, password);
        boolean loginSuccess = 1 == loginResult;
        String message = accountManager.getLoginResultMessage(loginResult);

        // 3 create response
        String resp = createResponseString(loginSuccess, message, inResponseTo, username, null);

        String[] result = new String[2];
        result[0] = resp;
        result[1] = backTo;

        return result;
    }

    public String createLoginResponse(String userId, String companyName) throws Exception
    {
        if (userId == null || companyName == null)
        {
            throw new NullPointerException("Argument can not be null or empty.");
        }

        String samlResp = createResponseString(true, null, null, userId, companyName);

        return samlResp;
    }

    /**
     * Helper method to generate a shell response with a given status code,
     * status message, and query ID.
     */
    private String createResponseString(boolean login, String msg, String inResponseTo,
            String userId, String companyName) throws Exception
    {
        // inResponseTo or 0|userId|login|msg
        // 1|userId|companyname
        
        if (userId != null && companyName != null)
        {
            return "1|" + encodeContent(userId) + "|" + encodeContent(companyName);
        }
        
        String s1 = "0";
        if (inResponseTo != null)
        {
            s1 = inResponseTo;
        }
        String s2 = encodeContent(userId);
        String s3 = encodeContent(login + "");
        String s4 = msg != null ? encodeContent(msg) : "";
        
        return s1 + "|" + s2 + "|" + s3 + "|" + s4;
    }
}
