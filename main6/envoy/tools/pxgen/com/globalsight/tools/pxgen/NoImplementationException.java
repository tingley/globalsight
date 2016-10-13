package com.globalsight.tools.pxgen;

/* Copyright (c) 2000, GlobalSight Corporation.  All rights reserved./

/**
 * This exception signals that the request remote implementation
 * is not supported.
 * 
 * @version     1.0, (4/19/00)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         04/19/2000   Initial version.
 */

public class NoImplementationException extends Exception
{
/**
 * NoImplementationException constructor comment.
 */
public NoImplementationException() {
	super();
}
/**
 * NotImplementationException constructor comment.
 * @param s java.lang.String
 */
public NoImplementationException(String s) {
	super(s);
}
}
