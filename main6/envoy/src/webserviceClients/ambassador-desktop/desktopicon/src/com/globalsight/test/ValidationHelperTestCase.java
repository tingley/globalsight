package com.globalsight.test;

import com.globalsight.util.ValidationHelper;

import junit.framework.TestCase;

public class ValidationHelperTestCase extends TestCase
{

	public static void main(String[] args)
	{
		junit.swingui.TestRunner.run(ValidationHelperTestCase.class);
	}
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}
	
	public void testValidateHostName()
	{
		String hostname1 = "www.163.com";
		String hostname2 = "ygy+%@!";
		String hostname3 = "quincyzou2";
		assertEquals(true, ValidationHelper.validateHostName(hostname1));
		assertEquals(false, ValidationHelper.validateHostName(hostname2));
		assertEquals(true, ValidationHelper.validateHostName(hostname3));
	}

	public void testValidateJobName()
	{
		String jobName1 = "List+-_888";
		String jobName2 = "ygy+%@!";
		assertEquals(true, ValidationHelper.validateJobName(jobName1));
		assertEquals(false, ValidationHelper.validateJobName(jobName2));
	}

}
