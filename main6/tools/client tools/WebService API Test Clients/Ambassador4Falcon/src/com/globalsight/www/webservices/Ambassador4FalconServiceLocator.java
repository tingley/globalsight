/**
 * Ambassador4FalconServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.globalsight.www.webservices;

public class Ambassador4FalconServiceLocator extends org.apache.axis.client.Service implements com.globalsight.www.webservices.Ambassador4FalconService {

    public Ambassador4FalconServiceLocator() {
    }


    public Ambassador4FalconServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public Ambassador4FalconServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for Ambassador4Falcon
    private java.lang.String Ambassador4Falcon_address = "http://localhost:8080/globalsight/services/Ambassador4Falcon";

    public java.lang.String getAmbassador4FalconAddress() {
        return Ambassador4Falcon_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String Ambassador4FalconWSDDServiceName = "Ambassador4Falcon";

    public java.lang.String getAmbassador4FalconWSDDServiceName() {
        return Ambassador4FalconWSDDServiceName;
    }

    public void setAmbassador4FalconWSDDServiceName(java.lang.String name) {
        Ambassador4FalconWSDDServiceName = name;
    }

    public com.globalsight.www.webservices.Ambassador4Falcon getAmbassador4Falcon() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(Ambassador4Falcon_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAmbassador4Falcon(endpoint);
    }

    public com.globalsight.www.webservices.Ambassador4Falcon getAmbassador4Falcon(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.globalsight.www.webservices.Ambassador4FalconSoapBindingStub _stub = new com.globalsight.www.webservices.Ambassador4FalconSoapBindingStub(portAddress, this);
            _stub.setPortName(getAmbassador4FalconWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public com.globalsight.www.webservices.Ambassador4Falcon getAmbassador4Falcon(java.net.URL portAddress, String userName, String password) throws javax.xml.rpc.ServiceException {
        try {
            com.globalsight.www.webservices.Ambassador4FalconSoapBindingStub _stub = new com.globalsight.www.webservices.Ambassador4FalconSoapBindingStub(portAddress, this, userName, password);
            _stub.setPortName(getAmbassador4FalconWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAmbassador4FalconEndpointAddress(java.lang.String address) {
        Ambassador4Falcon_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.globalsight.www.webservices.Ambassador4Falcon.class.isAssignableFrom(serviceEndpointInterface)) {
                com.globalsight.www.webservices.Ambassador4FalconSoapBindingStub _stub = new com.globalsight.www.webservices.Ambassador4FalconSoapBindingStub(new java.net.URL(Ambassador4Falcon_address), this);
                _stub.setPortName(getAmbassador4FalconWSDDServiceName());
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
        if ("Ambassador4Falcon".equals(inputPortName)) {
            return getAmbassador4Falcon();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.globalsight.com/webservices/", "Ambassador4FalconService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.globalsight.com/webservices/", "Ambassador4Falcon"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("Ambassador4Falcon".equals(portName)) {
            setAmbassador4FalconEndpointAddress(address);
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
