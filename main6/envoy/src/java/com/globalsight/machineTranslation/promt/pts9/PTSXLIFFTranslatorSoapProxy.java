/**
 *  Copyright 2009 Welocalize, Inc. 
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
package com.globalsight.machineTranslation.promt.pts9;

import javax.xml.rpc.Stub;

public class PTSXLIFFTranslatorSoapProxy implements PTSXLIFFTranslatorSoap
{
    private String _endpoint = null;
	private String _username = null;
    private String _password = null;
    private PTSXLIFFTranslatorSoap pTSXLIFFTranslatorSoap = null;

    public PTSXLIFFTranslatorSoapProxy()
    {
        _initPTSXLIFFTranslatorSoapProxy();
    }

    public PTSXLIFFTranslatorSoapProxy(String endpoint)
    {
        _endpoint = endpoint;
        _initPTSXLIFFTranslatorSoapProxy();
    }
    public PTSXLIFFTranslatorSoapProxy(String endpoint, String username, String password)
    {
        _endpoint = endpoint;
        _username = username;
        _password = password;
        _initPTSXLIFFTranslatorSoapProxy();
    }

    private void _initPTSXLIFFTranslatorSoapProxy()
    {
        try
        {
            pTSXLIFFTranslatorSoap = (new PTSXLIFFTranslatorLocator())
                    .getPTSXLIFFTranslatorSoap();
            if (pTSXLIFFTranslatorSoap != null)
            {
                // endpoint
                if (_endpoint != null)
                {
                    ((javax.xml.rpc.Stub) pTSXLIFFTranslatorSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
                }
                else
                {
                    _endpoint = (String) ((javax.xml.rpc.Stub) pTSXLIFFTranslatorSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
                }
                // username
                if (_username != null)
                {
                    ((javax.xml.rpc.Stub) pTSXLIFFTranslatorSoap)._setProperty(Stub.USERNAME_PROPERTY, _username);
                }
                else
                {
                    _username = (String) ((javax.xml.rpc.Stub) pTSXLIFFTranslatorSoap)._getProperty(Stub.USERNAME_PROPERTY);
                }
                // password
                if (_password != null)
                {
                    ((javax.xml.rpc.Stub) pTSXLIFFTranslatorSoap)._setProperty(Stub.PASSWORD_PROPERTY, _password);
                }
                else
                {
                    _password = (String) ((javax.xml.rpc.Stub) pTSXLIFFTranslatorSoap)._getProperty(Stub.PASSWORD_PROPERTY);
                }

            }

        }
        catch (javax.xml.rpc.ServiceException serviceException)
        {
        }
    }

    public String getEndpoint()
    {
        return _endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        _endpoint = endpoint;
        if (pTSXLIFFTranslatorSoap != null)
            ((javax.xml.rpc.Stub) pTSXLIFFTranslatorSoap)._setProperty(
                    "javax.xml.rpc.service.endpoint.address", _endpoint);

    }

    public PTSXLIFFTranslatorSoap getPTSXLIFFTranslatorSoap()
    {
        if (pTSXLIFFTranslatorSoap == null)
            _initPTSXLIFFTranslatorSoapProxy();
        return pTSXLIFFTranslatorSoap;
    }

    public java.lang.String translateFormattedText(int dirId,
            java.lang.String tplId, java.lang.String strText,
            java.lang.String fileType) throws java.rmi.RemoteException
    {
        if (pTSXLIFFTranslatorSoap == null)
            _initPTSXLIFFTranslatorSoapProxy();
        return pTSXLIFFTranslatorSoap.translateFormattedText(dirId, tplId,
                strText, fileType);
    }

    public void translateFormattedText2(int iDirId, java.lang.String strTplId,
            java.lang.String strSourceText, java.lang.String strFileType,
            javax.xml.rpc.holders.StringHolder translateFormattedText2Result,
            javax.xml.rpc.holders.StringHolder strError)
            throws java.rmi.RemoteException
    {
        if (pTSXLIFFTranslatorSoap == null)
            _initPTSXLIFFTranslatorSoapProxy();
        pTSXLIFFTranslatorSoap.translateFormattedText2(iDirId, strTplId,
                strSourceText, strFileType, translateFormattedText2Result,
                strError);
    }

    public java.lang.Object[] directions() throws java.rmi.RemoteException
    {
        if (pTSXLIFFTranslatorSoap == null)
            _initPTSXLIFFTranslatorSoapProxy();
        return pTSXLIFFTranslatorSoap.directions();
    }

    public java.lang.Object[] templates(int iDirID)
            throws java.rmi.RemoteException
    {
        if (pTSXLIFFTranslatorSoap == null)
            _initPTSXLIFFTranslatorSoapProxy();
        return pTSXLIFFTranslatorSoap.templates(iDirID);
    }

}