package com.globalsight.action;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.swing.JOptionPane;

import org.apache.axis.AxisFault;

import com.globalsight.bo.TestConnectBO;
import com.globalsight.exception.NotSupportHttpsException;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.util.Constants;
import com.globalsight.util.WebClientHelper;

public class TestServerAction extends Action
{

    public String execute(String args[]) throws Exception
    {
        boolean useSSL = false;
        try
        {
            useSSL = Boolean.parseBoolean(args[2]);
        }
        catch (Exception e)
        {
            // do nothing
        }

        TestConnectBO testConnectBO = new TestConnectBO();
        try
        {
            return testConnectBO.test(args[0], args[1], useSSL);
        }
        catch (Exception e)
        {
            if (e instanceof SSLException || e.getCause() instanceof SSLException)
            {
                if (useSSL)
                {
                    try
                    {
                        testConnectBO.test(args[0], args[1], false);
                        throw new NotSupportHttpsException();
                    }
                    catch (NotSupportHttpsException e2)
                    {
                        throw e2;
                    }
                    catch (Exception e2)
                    {
                        //ignore
                    }
                }
                
                boolean reset = WebClientHelper.installCert(args[0], args[2]);
                if (reset)
                {
                    return restartDI;
                }
                else
                {
                    throw e;
                }
            }
            else
            {
                throw e;
            }
        }
    }
}
