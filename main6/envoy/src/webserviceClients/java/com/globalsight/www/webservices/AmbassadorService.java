/**
 * AmbassadorService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package com.globalsight.www.webservices;

public interface AmbassadorService extends javax.xml.rpc.Service {
    public java.lang.String getAmbassadorWebServiceAddress();

    public com.globalsight.www.webservices.Ambassador getAmbassadorWebService() throws javax.xml.rpc.ServiceException;

    public com.globalsight.www.webservices.Ambassador getAmbassadorWebService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
