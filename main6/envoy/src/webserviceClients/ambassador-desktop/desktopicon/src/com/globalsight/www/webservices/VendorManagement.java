/**
 * VendorManagement.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package com.globalsight.www.webservices;

public interface VendorManagement extends java.rmi.Remote
{
    public java.lang.String login(java.lang.String in0, java.lang.String in1)
            throws java.rmi.RemoteException,
            com.globalsight.webservices.WebServiceException;

    public java.lang.String addVendor(java.lang.String in0, java.lang.String in1)
            throws java.rmi.RemoteException,
            com.globalsight.webservices.WebServiceException;

    public java.lang.String modifyVendor(java.lang.String in0,
            java.lang.String in1) throws java.rmi.RemoteException,
            com.globalsight.webservices.WebServiceException;

    public java.lang.String removeVendor(java.lang.String in0,
            java.lang.String in1) throws java.rmi.RemoteException,
            com.globalsight.webservices.WebServiceException;

    public java.lang.String helloWorld() throws java.rmi.RemoteException,
            com.globalsight.webservices.WebServiceException;

    public java.lang.String queryVendorBasicInfo(java.lang.String in0)
            throws java.rmi.RemoteException,
            com.globalsight.webservices.WebServiceException;

    public java.lang.String queryVendorDetails(java.lang.String in0,
            java.lang.String in1) throws java.rmi.RemoteException,
            com.globalsight.webservices.WebServiceException;
}
