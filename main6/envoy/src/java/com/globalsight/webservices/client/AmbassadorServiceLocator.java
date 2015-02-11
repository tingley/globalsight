/**
 * AmbassadorServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.globalsight.webservices.client;

public class AmbassadorServiceLocator extends org.apache.axis.client.Service implements com.globalsight.webservices.client.AmbassadorService {

    public AmbassadorServiceLocator() {
    }


    public AmbassadorServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public AmbassadorServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for AmbassadorWebService
    private java.lang.String AmbassadorWebService_address = "http://10.10.215.40:8080/globalsight/services/AmbassadorWebService";

    public java.lang.String getAmbassadorWebServiceAddress() {
        return AmbassadorWebService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AmbassadorWebServiceWSDDServiceName = "AmbassadorWebService";

    public java.lang.String getAmbassadorWebServiceWSDDServiceName() {
        return AmbassadorWebServiceWSDDServiceName;
    }

    public void setAmbassadorWebServiceWSDDServiceName(java.lang.String name) {
        AmbassadorWebServiceWSDDServiceName = name;
    }

    public com.globalsight.webservices.client.Ambassador getAmbassadorWebService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AmbassadorWebService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAmbassadorWebService(endpoint);
    }

    public com.globalsight.webservices.client.Ambassador getAmbassadorWebService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.globalsight.webservices.client.AmbassadorWebServiceSoapBindingStub _stub = new com.globalsight.webservices.client.AmbassadorWebServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getAmbassadorWebServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }
    
    public com.globalsight.webservices.client.Ambassador getAmbassadorWebService(java.net.URL portAddress, String userName, String password) 
        throws javax.xml.rpc.ServiceException 
    {
        try 
        {
            com.globalsight.webservices.client.AmbassadorWebServiceSoapBindingStub _stub = 
            	new com.globalsight.webservices.client.AmbassadorWebServiceSoapBindingStub(portAddress, this, userName, password);
            _stub.setPortName(getAmbassadorWebServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) 
        {
            return null;
        }
    }

    public void setAmbassadorWebServiceEndpointAddress(java.lang.String address) {
        AmbassadorWebService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.globalsight.webservices.client.Ambassador.class.isAssignableFrom(serviceEndpointInterface)) {
                com.globalsight.webservices.client.AmbassadorWebServiceSoapBindingStub _stub = new com.globalsight.webservices.client.AmbassadorWebServiceSoapBindingStub(new java.net.URL(AmbassadorWebService_address), this);
                _stub.setPortName(getAmbassadorWebServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("AmbassadorWebService".equals(inputPortName)) {
            return getAmbassadorWebService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.globalsight.com/webservices/", "AmbassadorService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.globalsight.com/webservices/", "AmbassadorWebService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("AmbassadorWebService".equals(portName)) {
            setAmbassadorWebServiceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
