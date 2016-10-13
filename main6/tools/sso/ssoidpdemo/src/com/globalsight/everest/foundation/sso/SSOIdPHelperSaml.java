package com.globalsight.everest.foundation.sso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sun.identity.saml2.assertion.Assertion;
import com.sun.identity.saml2.assertion.AssertionFactory;
import com.sun.identity.saml2.assertion.Attribute;
import com.sun.identity.saml2.assertion.AttributeStatement;
import com.sun.identity.saml2.assertion.Issuer;
import com.sun.identity.saml2.assertion.NameID;
import com.sun.identity.saml2.assertion.Subject;
import com.sun.identity.saml2.common.SAML2Constants;
import com.sun.identity.saml2.common.SAML2Exception;
import com.sun.identity.saml2.common.SAML2SDKUtils;
import com.sun.identity.saml2.protocol.ProtocolFactory;
import com.sun.identity.saml2.protocol.Response;
import com.sun.identity.saml2.protocol.Status;
import com.sun.identity.saml2.protocol.StatusCode;

/**
 * Read/generate SSO information by SAML
 * 
 */
public class SSOIdPHelperSaml extends SSOIdPHelper
{
    private static AssertionFactory assFactory;
    private static ProtocolFactory protocolFactory;

    static
    {
        assFactory = AssertionFactory.getInstance();
        protocolFactory = ProtocolFactory.getInstance();
    }

    public SSOIdPHelperSaml()
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

        // 1 decode request
        String saml = decodeContent(request);

        // 2 read saml
        Assertion assertion = assFactory.createAssertion(saml);
        NameID nameID = assertion.getSubject().getNameID();

        username = nameID.getValue();
        inResponseTo = assertion.getID();

        for (Object obj : assertion.getAttributeStatements())
        {
            if (obj instanceof AttributeStatement)
            {
                for (Object att : ((AttributeStatement) obj).getAttribute())
                {
                    if (att instanceof Attribute)
                    {
                        Attribute attribute = (Attribute) att;
                        String attributeName = attribute.getName();

                        if ("Password".equals(attributeName))
                        {
                            password = getAttributeValue(attribute);
                        }

                        if ("BackTo".equals(attributeName))
                        {
                            backTo = getAttributeValue(attribute);
                        }
                    }
                }
            }
        }

        // 3 login
        int loginResult = accountManager.loginUser(username, password);
        boolean loginSuccess = 1 == loginResult;
        String message = accountManager.getLoginResultMessage(loginResult);

        // 4 create response
        String samlResp = createResponseString(loginSuccess, message, inResponseTo, null);

        // 5 encode response
        String base64 = encodeContent(samlResp);

        String[] result = new String[2];
        result[0] = base64;
        result[1] = backTo;

        return result;
    }

    public String createLoginResponse(String userId, String companyName) throws Exception
    {
        if (userId == null || companyName == null)
        {
            throw new NullPointerException("Argument can not be null or empty.");
        }

        Assertion assertion = null;

        // 1 create assertion with attribute
        Date now = new Date();
        Issuer issuer = createIssuer();

        NameID nameID = assFactory.createNameID();
        nameID.setValue(userId);

        Subject subject = assFactory.createSubject();
        subject.setNameID(nameID);

        Attribute attribute1 = assFactory.createAttribute();
        attribute1.setName("CompanyName");
        attribute1.setAttributeValueString(createListWithObject(companyName));

        AttributeStatement statement = assFactory.createAttributeStatement();
        statement.setAttribute(createListWithObject(attribute1));

        assertion = assFactory.createAssertion();
        assertion.setVersion(SAML2Constants.VERSION_2_0);
        assertion.setID(SAML2SDKUtils.generateID());
        assertion.setIssueInstant(now);
        assertion.setIssuer(issuer);
        assertion.setSubject(subject);
        assertion.setAttributeStatements(createListWithObject(statement));

        // 2 create resposnse
        String samlResp = createResponseString(true, null, null, assertion);

        // 3 encode response
        String base64 = encodeContent(samlResp);

        return base64;
    }
    
    private List createListWithObject(Object obj)
    {
        List result = new ArrayList();
        result.add(obj);
        
        return result;
    }

    private String getAttributeValue(Attribute attribute)
    {
        String result = "";
        for (Object value : attribute.getAttributeValueString())
        {
            if (value instanceof String)
            {
                result = (String) value;
            }
        }

        return result;
    }

    /**
     * Helper method to generate a shell response with a given status code,
     * status message, and query ID.
     * 
     * @throws SAML2Exception
     */
    private String createResponseString(boolean login, String msg, String inResponseTo, Assertion as)
            throws SAML2Exception
    {
        Response response = protocolFactory.createResponse();
        response.setVersion(SAML2Constants.VERSION_2_0);
        response.setID(SAML2SDKUtils.generateID());

        Date now = new Date();
        response.setIssueInstant(now);

        Issuer issuer = createIssuer();
        if (issuer != null)
        {
            response.setIssuer(issuer);
        }

        String statusCode = login ? SAML2Constants.SUCCESS : SAML2Constants.AUTHN_FAILED;
        StatusCode statusCodeElement = protocolFactory.createStatusCode();
        statusCodeElement.setValue(statusCode);

        Status status = protocolFactory.createStatus();
        status.setStatusCode(statusCodeElement);
        response.setStatus(status);

        if (inResponseTo != null)
        {
            response.setInResponseTo(inResponseTo);
        }

        if (msg != null)
        {
            status.setStatusMessage(msg);
        }

        if (as != null)
        {
            response.setAssertion(createListWithObject(as));
        }

        return response.toXMLString(true, true);
    }

    private Issuer createIssuer() throws SAML2Exception
    {
        Issuer issuer = assFactory.createIssuer();
        issuer.setValue("http://localhost:8090/IdpDemo/");

        return issuer;
    }
}
