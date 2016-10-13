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

public class PTSXLIFFTranslatorLocator extends org.apache.axis.client.Service
        implements PTSXLIFFTranslator
{
    private static final long serialVersionUID = -7427929304240359773L;

    public PTSXLIFFTranslatorLocator()
    {
    }

    public PTSXLIFFTranslatorLocator(org.apache.axis.EngineConfiguration config)
    {
        super(config);
    }

    public PTSXLIFFTranslatorLocator(java.lang.String wsdlLoc,
            javax.xml.namespace.QName sName)
            throws javax.xml.rpc.ServiceException
    {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for PTSXLIFFTranslatorSoap12
    private java.lang.String PTSXLIFFTranslatorSoap12_address = "http://ptsdemo.promt.ru/ptsxliff9/ptsxlifftranslator.asmx";

    public java.lang.String getPTSXLIFFTranslatorSoap12Address()
    {
        return PTSXLIFFTranslatorSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String PTSXLIFFTranslatorSoap12WSDDServiceName = "PTSXLIFFTranslatorSoap12";

    public java.lang.String getPTSXLIFFTranslatorSoap12WSDDServiceName()
    {
        return PTSXLIFFTranslatorSoap12WSDDServiceName;
    }

    public void setPTSXLIFFTranslatorSoap12WSDDServiceName(java.lang.String name)
    {
        PTSXLIFFTranslatorSoap12WSDDServiceName = name;
    }

    public PTSXLIFFTranslatorSoap getPTSXLIFFTranslatorSoap12()
            throws javax.xml.rpc.ServiceException
    {
        java.net.URL endpoint;
        try
        {
            endpoint = new java.net.URL(PTSXLIFFTranslatorSoap12_address);
        }
        catch (java.net.MalformedURLException e)
        {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getPTSXLIFFTranslatorSoap12(endpoint);
    }

    public PTSXLIFFTranslatorSoap getPTSXLIFFTranslatorSoap12(
            java.net.URL portAddress) throws javax.xml.rpc.ServiceException
    {
        try
        {
            PTSXLIFFTranslatorSoap12Stub _stub = new PTSXLIFFTranslatorSoap12Stub(
                    portAddress, this);
            _stub.setPortName(getPTSXLIFFTranslatorSoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e)
        {
            return null;
        }
    }

    public void setPTSXLIFFTranslatorSoap12EndpointAddress(
            java.lang.String address)
    {
        PTSXLIFFTranslatorSoap12_address = address;
    }

    // Use to get a proxy class for PTSXLIFFTranslatorSoap
    private java.lang.String PTSXLIFFTranslatorSoap_address = "http://ptsdemo.promt.ru/ptsxliff9/ptsxlifftranslator.asmx";

    public java.lang.String getPTSXLIFFTranslatorSoapAddress()
    {
        return PTSXLIFFTranslatorSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String PTSXLIFFTranslatorSoapWSDDServiceName = "PTSXLIFFTranslatorSoap";

    public java.lang.String getPTSXLIFFTranslatorSoapWSDDServiceName()
    {
        return PTSXLIFFTranslatorSoapWSDDServiceName;
    }

    public void setPTSXLIFFTranslatorSoapWSDDServiceName(java.lang.String name)
    {
        PTSXLIFFTranslatorSoapWSDDServiceName = name;
    }

    public PTSXLIFFTranslatorSoap getPTSXLIFFTranslatorSoap()
            throws javax.xml.rpc.ServiceException
    {
        java.net.URL endpoint;
        try
        {
            endpoint = new java.net.URL(PTSXLIFFTranslatorSoap_address);
        }
        catch (java.net.MalformedURLException e)
        {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getPTSXLIFFTranslatorSoap(endpoint);
    }

    public PTSXLIFFTranslatorSoap getPTSXLIFFTranslatorSoap(
            java.net.URL portAddress) throws javax.xml.rpc.ServiceException
    {
        try
        {
            PTSXLIFFTranslatorSoapStub _stub = new PTSXLIFFTranslatorSoapStub(
                    portAddress, this);
            _stub.setPortName(getPTSXLIFFTranslatorSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e)
        {
            return null;
        }
    }

    public void setPTSXLIFFTranslatorSoapEndpointAddress(
            java.lang.String address)
    {
        PTSXLIFFTranslatorSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation. If this service has
     * no port for the given interface, then ServiceException is thrown. This
     * service has multiple ports for a given interface; the proxy
     * implementation returned may be indeterminate.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface)
            throws javax.xml.rpc.ServiceException
    {
        try
        {
            if (PTSXLIFFTranslatorSoap.class
                    .isAssignableFrom(serviceEndpointInterface))
            {
                PTSXLIFFTranslatorSoap12Stub _stub = new PTSXLIFFTranslatorSoap12Stub(
                        new java.net.URL(PTSXLIFFTranslatorSoap12_address),
                        this);
                _stub.setPortName(getPTSXLIFFTranslatorSoap12WSDDServiceName());
                return _stub;
            }
            if (PTSXLIFFTranslatorSoap.class
                    .isAssignableFrom(serviceEndpointInterface))
            {
                PTSXLIFFTranslatorSoapStub _stub = new PTSXLIFFTranslatorSoapStub(
                        new java.net.URL(PTSXLIFFTranslatorSoap_address), this);
                _stub.setPortName(getPTSXLIFFTranslatorSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t)
        {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException(
                "There is no stub implementation for the interface:  "
                        + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface
                                .getName()));
    }

    /**
     * For the given interface, get the stub implementation. If this service has
     * no port for the given interface, then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName,
            Class serviceEndpointInterface)
            throws javax.xml.rpc.ServiceException
    {
        if (portName == null)
        {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("PTSXLIFFTranslatorSoap12".equals(inputPortName))
        {
            return getPTSXLIFFTranslatorSoap12();
        }
        else if ("PTSXLIFFTranslatorSoap".equals(inputPortName))
        {
            return getPTSXLIFFTranslatorSoap();
        }
        else
        {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName()
    {
        return new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "PTSXLIFFTranslator");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts()
    {
        if (ports == null)
        {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName(
                    "http://Promt.Pits.Translator/Services/",
                    "PTSXLIFFTranslatorSoap12"));
            ports.add(new javax.xml.namespace.QName(
                    "http://Promt.Pits.Translator/Services/",
                    "PTSXLIFFTranslatorSoap"));
        }
        return ports.iterator();
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(java.lang.String portName,
            java.lang.String address) throws javax.xml.rpc.ServiceException
    {

        if ("PTSXLIFFTranslatorSoap12".equals(portName))
        {
            setPTSXLIFFTranslatorSoap12EndpointAddress(address);
        }
        else if ("PTSXLIFFTranslatorSoap".equals(portName))
        {
            setPTSXLIFFTranslatorSoapEndpointAddress(address);
        }
        else
        { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(
                    " Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(javax.xml.namespace.QName portName,
            java.lang.String address) throws javax.xml.rpc.ServiceException
    {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
