package com.globalsight.tools.pxgen;

/* Copyright (c) 2000, GlobalSight Corporation.  All rights reserved./

/**
 * This exception signals that the type that is being operated on
 * is not an interface.
 * 
 * @version     1.0, (3/27/00 12:55:11 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         03/25/2000   Initial version.
 */

public class NotInterfaceException extends Exception
{
/**
 * NotInterfaceException constructor comment.
 */
public NotInterfaceException() {
	super();
}
/**
 * NotInterfaceException constructor comment.
 * @param s java.lang.String
 */
public NotInterfaceException(String s) {
	super(s);
}
}
