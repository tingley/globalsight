package com.globalsight.tools.pxgen;

/* Copyright (c) 2000, GlobalSight Corporation.  All rights reserved./
/**
 * This is a concrete subclass of pxgen that generates extension
 * types for the JDK implementation of RMI.
 */

public class PxgenWLRMI extends pxgen
{
/**
 * Construct an instance that can be used to generate
 * remote extension for the given type.
 */
public PxgenWLRMI()
{
	super();
}
/**
 * Get the suffix of the name of the implementation class to generate.
 *
 * @return The suffix of the name of the implementation class to generate.
 */
String getImplementationSuffix()
{
	return "WLRMIImpl";
}
/**
 * Get the suffix of the name of the interface to generate.
 *
 * @return The suffix of the name of the interface to generate.
 */
String getInterfaceSuffix()
{
	return "WLRemote";
}
/**
 * Return the class name for the remote exception.
 * 
 * 	@return The full name of the remote exception class.
 * 
 */
String getRemoteExceptionName()
{
	return "java.rmi.RemoteException";
}
/**
 * Get the name of the remote interface to extend.
 *
 * @return The name of the remote interface to extend.
 */
String getRemoteInterfaceName()
{
	return "java.rmi.Remote";
}
/**
 * Get the name of the remote object to extend.
 *
 * @return The name of the remote object to extend.
 */
String getRemoteObjectName()
{
	return "com.globalsight.everest.util.system.RemoteServer";
}
}
