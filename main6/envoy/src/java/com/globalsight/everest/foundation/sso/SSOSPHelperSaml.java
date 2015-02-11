package com.globalsight.everest.foundation.sso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.sun.identity.saml2.assertion.Assertion;
import com.sun.identity.saml2.assertion.AssertionFactory;
import com.sun.identity.saml2.assertion.Attribute;
import com.sun.identity.saml2.assertion.AttributeStatement;
import com.sun.identity.saml2.assertion.Conditions;
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
 * Utility that uses OpenSSO/OpenAM to carry out common SAML tasks.
 */

public class SSOSPHelperSaml extends SSOSPHelper
{
    private static AssertionFactory assFactory;
    private static ProtocolFactory protocolFactory;

    static
    {
        assFactory = AssertionFactory.getInstance();
        protocolFactory = ProtocolFactory.getInstance();
    }

    /**
     * Initialize
     * 
     * @throws Exception
     */
    public SSOSPHelperSaml()
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
        Assertion ass = createSSOAssertion(userName, pwd, backTo);
        String saml = ass.toXMLString(true, true);
        String base64 = encodeContent(saml);
        
        String inResponseTo = ass.getID();
        storeParameter(inResponseTo, companyName, userName);
        
        return base64;
    }

    /**
     * Handle SSO Response
     * @param response
     * @return SSOResponse
     * @throws Exception
     */
    public SSOResponse handleSSOResponse(String response) throws Exception
    {
        // 1 decode
        String saml = decodeContent(response);
        
        // 2 read response
        Response resp = null;
        try
        {
            resp = protocolFactory.createResponse(saml);
        }
        catch (Exception e)
        {
            resp = null;
        }

        // 3 handle response
        SSOResponse result = new SSOResponse();

        if (resp != null)
        {
            Status status = resp.getStatus();
            StatusCode code = status.getStatusCode();
            Issuer issuer = resp.getIssuer();
            String inResponseTo = resp.getInResponseTo();
            
            result.setLoginSuccess(SAML2Constants.SUCCESS.equals(code.getValue()));
            result.setStatusCode(code.getValue());
            result.setStatusMessage(status != null ? status.getStatusMessage() : "");
            result.setInResponseTo(inResponseTo != null ? inResponseTo : "");
            
            List<Assertion> ass = resp.getAssertion();
            Assertion as = (ass != null && ass.size() > 0) ? ass.get(0) : null;
            String companyName = null;
            String userId = "";
            
            if (as != null)
            {
                Subject sub = as.getSubject();
                if (sub != null)
                {
                    NameID nameId = sub.getNameID();
                    userId = nameId != null ? nameId.getValue() : "";
                }
                
                for (Object obj : as.getAttributeStatements())
                {
                    if (obj instanceof AttributeStatement)
                    {
                        for (Object att : ((AttributeStatement) obj).getAttribute())
                        {
                            if (att instanceof Attribute)
                            {
                                Attribute attribute = (Attribute) att;
                                String attributeName = attribute.getName();

                                if ("CompanyName".equals(attributeName))
                                {
                                    companyName = getAttributeValue(attribute);
                                }
                            }
                        }
                    }
                }
            }
            
            result.setUserId(userId);
            result.setCompanyName(companyName);
        }
        else
        {
            result.setLoginSuccess(false);
        }

        return result;
    }

    private Assertion createSSOAssertion(String userName, String pwd, String backTo)
            throws Exception
    {
        DateTime now = new DateTime();
        Issuer issuer = createIssuer();

        NameID nameID = assFactory.createNameID();
        nameID.setValue(userName);

        Subject subject = assFactory.createSubject();
        subject.setNameID(nameID);

        Conditions conditions = assFactory.createConditions();
        conditions.setNotBefore(now.minusMinutes(15).toDate());
        conditions.setNotOnOrAfter(now.plusMinutes(30).toDate());

        // Build attribute values
        Attribute attribute1 = assFactory.createAttribute();
        attribute1.setName("Password");
        attribute1.setAttributeValueString(createListWithObject(pwd));

        Attribute attribute2 = assFactory.createAttribute();
        attribute2.setName("BackTo");
        attribute2.setAttributeValueString(createListWithObject(backTo));

        AttributeStatement statement = assFactory.createAttributeStatement();
        statement.setAttribute(createListWithObject(attribute1));
        statement.getAttribute().add(attribute2);

        Assertion assertion = assFactory.createAssertion();
        assertion.setVersion(SAML2Constants.VERSION_2_0);
        assertion.setID(SAML2SDKUtils.generateID());
        assertion.setIssueInstant(now.toDate());
        assertion.setIssuer(issuer);
        assertion.setSubject(subject);
        assertion.setConditions(conditions);
        assertion.setAttributeStatements(createListWithObject(statement));

        return assertion;
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

    private Issuer createIssuer() throws SAML2Exception
    {
        Issuer issuer = assFactory.createIssuer();
        issuer.setValue("http://globalsight.com/sso");
        return issuer;
    }
}
