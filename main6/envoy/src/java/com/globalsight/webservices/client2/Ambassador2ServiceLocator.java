/**
 * Ambassador2ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.globalsight.webservices.client2;

public class Ambassador2ServiceLocator extends org.apache.axis.client.Service
        implements com.globalsight.webservices.client2.Ambassador2Service
{

    public Ambassador2ServiceLocator()
    {
    }

    public Ambassador2ServiceLocator(org.apache.axis.EngineConfiguration config)
    {
        super(config);
    }

    public Ambassador2ServiceLocator(java.lang.String wsdlLoc,
            javax.xml.namespace.QName sName)
            throws javax.xml.rpc.ServiceException
    {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for AmbassadorWebService2
    private java.lang.String AmbassadorWebService2_address = "http://localhost:8080/globalsight/services/AmbassadorWebService2";

    public java.lang.String getAmbassadorWebService2Address()
    {
        return AmbassadorWebService2_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AmbassadorWebService2WSDDServiceName = "AmbassadorWebService2";

    public java.lang.String getAmbassadorWebService2WSDDServiceName()
    {
        return AmbassadorWebService2WSDDServiceName;
    }

    public void setAmbassadorWebService2WSDDServiceName(java.lang.String name)
    {
        AmbassadorWebService2WSDDServiceName = name;
    }

    public com.globalsight.webservices.client2.Ambassador2 getAmbassadorWebService2()
            throws javax.xml.rpc.ServiceException
    {
        java.net.URL endpoint;
        try
        {
            endpoint = new java.net.URL(AmbassadorWebService2_address);
        }
        catch (java.net.MalformedURLException e)
        {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAmbassadorWebService2(endpoint);
    }

    public com.globalsight.webservices.client2.Ambassador2 getAmbassadorWebService2(
            java.net.URL portAddress) throws javax.xml.rpc.ServiceException
    {
        try
        {
            com.globalsight.webservices.client2.AmbassadorWebService2SoapBindingStub _stub = new com.globalsight.webservices.client2.AmbassadorWebService2SoapBindingStub(
                    portAddress, this);
            _stub.setPortName(getAmbassadorWebService2WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e)
        {
            return null;
        }
    }

    public com.globalsight.webservices.client2.Ambassador2 getAmbassadorWebService2(
            java.net.URL portAddress, String userName, String password)
            throws javax.xml.rpc.ServiceException
    {
        try
        {
            com.globalsight.webservices.client2.AmbassadorWebService2SoapBindingStub _stub = new com.globalsight.webservices.client2.AmbassadorWebService2SoapBindingStub(
                    portAddress, this, userName, password);
            _stub.setPortName(getAmbassadorWebService2WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e)
        {
            return null;
        }
    }

    public void setAmbassadorWebService2EndpointAddress(java.lang.String address)
    {
        AmbassadorWebService2_address = address;
    }

    /**
     * For the given interface, get the stub implementation. If this service has
     * no port for the given interface, then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface)
            throws javax.xml.rpc.ServiceException
    {
        try
        {
            if (com.globalsight.webservices.client2.Ambassador2.class
                    .isAssignableFrom(serviceEndpointInterface))
            {
                com.globalsight.webservices.client2.AmbassadorWebService2SoapBindingStub _stub = new com.globalsight.webservices.client2.AmbassadorWebService2SoapBindingStub(
                        new java.net.URL(AmbassadorWebService2_address), this);
                _stub.setPortName(getAmbassadorWebService2WSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t)
        {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException(
                "There is no stub implementation for the interface:  "
                        + (serviceEndpointInterface == null ? "null"
                                : serviceEndpointInterface.getName()));
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
        if ("AmbassadorWebService2".equals(inputPortName))
        {
            return getAmbassadorWebService2();
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
                "http://www.globalsight.com/webservices/", "Ambassador2Service");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts()
    {
        if (ports == null)
        {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName(
                    "http://www.globalsight.com/webservices/",
                    "AmbassadorWebService2"));
        }
        return ports.iterator();
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(java.lang.String portName,
            java.lang.String address) throws javax.xml.rpc.ServiceException
    {

        if ("AmbassadorWebService2".equals(portName))
        {
            setAmbassadorWebService2EndpointAddress(address);
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
