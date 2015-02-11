/**
 * VendorManagementServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package com.globalsight.www.webservices;

public class VendorManagementServiceLocator extends org.apache.axis.client.Service implements com.globalsight.www.webservices.VendorManagementService {

    public VendorManagementServiceLocator() {
    }


    public VendorManagementServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public VendorManagementServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for VendorManagementWebService
    private java.lang.String VendorManagementWebService_address = "http://localhost:7001/globalsight/services/VendorManagementWebService";

    public java.lang.String getVendorManagementWebServiceAddress() {
        return VendorManagementWebService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String VendorManagementWebServiceWSDDServiceName = "VendorManagementWebService";

    public java.lang.String getVendorManagementWebServiceWSDDServiceName() {
        return VendorManagementWebServiceWSDDServiceName;
    }

    public void setVendorManagementWebServiceWSDDServiceName(java.lang.String name) {
        VendorManagementWebServiceWSDDServiceName = name;
    }

    public com.globalsight.www.webservices.VendorManagement getVendorManagementWebService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(VendorManagementWebService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getVendorManagementWebService(endpoint);
    }

    public com.globalsight.www.webservices.VendorManagement getVendorManagementWebService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.globalsight.www.webservices.VendorManagementWebServiceSoapBindingStub _stub = new com.globalsight.www.webservices.VendorManagementWebServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getVendorManagementWebServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setVendorManagementWebServiceEndpointAddress(java.lang.String address) {
        VendorManagementWebService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.globalsight.www.webservices.VendorManagement.class.isAssignableFrom(serviceEndpointInterface)) {
                com.globalsight.www.webservices.VendorManagementWebServiceSoapBindingStub _stub = new com.globalsight.www.webservices.VendorManagementWebServiceSoapBindingStub(new java.net.URL(VendorManagementWebService_address), this);
                _stub.setPortName(getVendorManagementWebServiceWSDDServiceName());
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
        if ("VendorManagementWebService".equals(inputPortName)) {
            return getVendorManagementWebService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.globalsight.com/webservices/", "VendorManagementService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.globalsight.com/webservices/", "VendorManagementWebService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        if ("VendorManagementWebService".equals(portName)) {
            setVendorManagementWebServiceEndpointAddress(address);
        }
        else { // Unknown Port Name
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
