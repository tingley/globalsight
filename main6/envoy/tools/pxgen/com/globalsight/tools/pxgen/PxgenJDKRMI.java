package com.globalsight.tools.pxgen;

/* Copyright (c) 2000, GlobalSight Corporation.  All rights reserved./
/**
 * This is a concrete subclass of pxgen that generates extension
 * types for the JDK implementation of RMI.
 * 
 * @version     1.0, (3/27/00 1:53:33 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         03/25/2000   Initial version.
 */

public class PxgenJDKRMI extends pxgen
{
/**
 * Construct an instance that can be used to generate
 * remote extension for the given type.
 */
public PxgenJDKRMI()
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
	return "JDKRMIImpl";
}
/**
 * Get the suffix of the name of the interface to generate.
 *
 * @return The suffix of the name of the interface to generate.
 */
String getInterfaceSuffix()
{
	return "JDKRemote";
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
	return "java.rmi.server.UnicastRemoteObject";
}
}
