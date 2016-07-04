/**
 * AmbassadorService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.globalsight.www.webservices;

public interface AmbassadorService extends javax.xml.rpc.Service {
    public java.lang.String getAmbassadorWebServiceAddress();

    public com.globalsight.www.webservices.Ambassador getAmbassadorWebService() throws javax.xml.rpc.ServiceException;

    public com.globalsight.www.webservices.Ambassador getAmbassadorWebService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
