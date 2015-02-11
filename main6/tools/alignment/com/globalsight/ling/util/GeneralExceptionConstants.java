package com.globalsight.ling.util;

/* Copyright (c) 2000, Global Sight Corporation.  All rights reserved. */

/**
 * Constants used with the GeneralException class.
 * 
 * @version     1.0, (1/3/00 1:15:58 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         01/02/2000   Initial version.
 */

public interface GeneralExceptionConstants
{
	// Component identifications
	/**
	 * Id for the "change detection" component.
	 */
	public static final int COMP_CHANGE      = 1;  // Change Detection
	/**
	 * Id for the "Ev Interface" component.
	 */
	public static final int COMP_EVI         = 2;  // EV Interface
	/**
	 * Id for the "Request Processor" component.
	 */
	public static final int COMP_REQUEST     = 3;  // Request Processor
	/**
	 * Id for the "User Manager" component.
	 */
	public static final int COMP_USERMANAGER = 4;  // User Manager
	/**
	 * Id for the "Web Data Source" component.
	 */
	public static final int COMP_WEBDATA     = 5;  // Web Data Source
	/**
	 * Id for the "Workflow" component.
	 */
	public static final int COMP_WORKFLOW    = 6;  // Work flow
	/**
	 * Id for the "Admin Interface" component.
	 */
	public static final int COMP_ADMIN       = 7;  // Admin Interface
	/**
	 * Id for the "Translation Interface" component.
	 */
	public static final int COMP_TRANSLATION = 8;  // Translation Interface
	/**
	 * Id for any "Linguistics" components that have no specific
	 * component identifications.
	 */
	public static final int COMP_LING        = 9;  // General Linguistics
	/**
	 * Id for the "Extractor Framework" component.
	 */
	public static final int COMP_EXTRACTOR   = 10; // Extractor framework
	/**
	 * Id for the "Translation Memory" component.
	 */
	public static final int COMP_TRANMEM     = 11; // Translation memory
	/**
	 * Id for the "Job Handler" component.
	 */
	 public static final int COMP_JOBS = 12;

	/**
	 * Id for the virtual component.
	 */
	 public static final int COMP_GENERAL = 113;
	 
	// Exception identification
	// General exception id is from 1900 to 1999 while component specific exception id is from 1000 to 1899;
	/**
	 * Exception id for "general exception", which means any exception that
	 * does not have a specific id.
	 */
	public static final int EX_GENERAL       = 1900;    // General exception
	/**
	 * Exception id for system or network level exception.
	 */
	public static final int EX_REMOTE        = 1901;    // Remote or network exception
	/**
	 * Exception id for missing resource exception
	 */
	public static final int EX_MISSING_RESOURCE_EXCEPTION = 1902; // Missing resource exception
	// Message identification
	// General error message id is from 1900 to 1999 while component specific error message id is from 1000 to 1899;
	// General info message id is from 2900 to 2999 while component speciifc info message id is from 2000 to 2899;
	// General debug message id is from 3900 to 3999 while component specific debug message id is from 3000 to 3899;
	/**
	 * Error message id for MissingResourceException
	 */
	public static final int MSG_ERR_NO_MESSAGE = 1900;
}
