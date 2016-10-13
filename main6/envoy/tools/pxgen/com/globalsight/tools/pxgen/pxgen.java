package com.globalsight.tools.pxgen;

/* Copyright (c) 1999, Global Sight Corporation.  All rights reserved.*/

// Core Java classes
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.util.Date;
import java.util.Locale;

/**
 * This class generates the remote extension and implementation of
 * an interface.
 * 
 * @version     1.0, (3/26/00 11:53:58 PM)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         03/25/2000   Initial version.
 */

public abstract class pxgen
{
	static final String INDENT = "    "; // 4 spaces
	static final String INDENT2 = INDENT + INDENT;
	static final String INDENT3 = INDENT2 + INDENT;
	static final String GENERATOR_PREFIX = "com.globalsight.tools.pxgen.Pxgen";
	static final String DEFAULT_GENERATOR = "JDKRMI";
	static final String ABSTRACT_MODIFIER = "abstract";
	static final String SERV_REG_REMOTEFULL = "com.globalsight.everest.util.server.EvRemote";
	static final String SERV_REG_REMOTE = "EvRemote";
	static final String LOCAL_REFERENCE = "m_localReference";
	Class m_type; // The type to generate remote extension for
	String m_destFilePrefix;
	String m_packageName;
	String m_typeName;
	boolean m_writeCreationInfo;
/**
 * Construct an instance that can be used to generate
 * remote extension for the given type.
 *
 */
public pxgen()
{
	super();
}
/**
 * Generate the header of the type.
 * 
 * @param p_writer java.io.PrintWriter
 * @exception java.io.IOException The exception description.
 */
void genHeader(PrintWriter p_writer) throws java.io.IOException
{
	// Get a US date formatter - we'll write all dates in US format
	DateFormat fmt = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
	Date dt = new Date(); // Get the current date/time

	// Format the date and extract the year
	FieldPosition yrField = new FieldPosition(DateFormat.YEAR_FIELD);
	StringBuffer dateBuff = new StringBuffer(128);
	dateBuff = fmt.format(dt, dateBuff, yrField);
	int yrBegin = yrField.getBeginIndex();
	int yrEnd = yrField.getEndIndex();
	char[] yrChar = new char[yrEnd - yrBegin];
	dateBuff.getChars(yrBegin, yrEnd, yrChar, 0); // Extract year field

	// Now write the module header
	String copyrightStr = "/* Copyright (c) " + new String(yrChar) + ", GlobalSight Corporation. All Rights Preserved. */";
	p_writer.println(copyrightStr);
	if (m_writeCreationInfo)
	{
		p_writer.println("/*");
		p_writer.println(" * This file was automatically generated.");
		p_writer.println(" * Generation date : " + dateBuff.toString());
		p_writer.println(" */");
	}
}
/**
 * Generate the class that implements the extended interface.  The
 * generated class extends remote object of the specified remote
 * architecture implementer.
 *
 * @exception FileNotFoundException Could not open the output file.
 * @exception IOException General exception while doing IO.
 */
void genImplementation() throws FileNotFoundException, IOException
{
	PrintWriter wrtr;
	//wrtr = new PrintWriter(System.out); // For testing only
	String outFileName = m_destFilePrefix + getImplementationSuffix() + ".java";
	FileOutputStream outStream = new FileOutputStream(outFileName);
	wrtr = new PrintWriter(outStream);
	genHeader(wrtr);
	genPackageLine(wrtr);
	genImportLines(wrtr);
	genImplementationBegin(wrtr);
	genImplementationFrameworkMethods(wrtr);
	genMethodLines(wrtr);
	wrtr.println("}");
	wrtr.flush();
}
/**
 * Generate the implementation class beginning section, including name
 * and member variables, i.e. everything up to and including the opening
 * bracket.
 *
 * @param wrtr The writer to write output to.
 */
void genImplementationBegin(PrintWriter p_writer)
{
	p_writer.println("");
	p_writer.println("public class "
		+ getImplementationClassName()
		//+ m_typeName + getImplementationSuffix()
		+ " extends " + getRemoteObjectName()
		+ " implements " + getInterfaceName()
		+ ", " + SERV_REG_REMOTE);
	p_writer.println("{");
	p_writer.println(INDENT + m_typeName + " " + LOCAL_REFERENCE + ";");
}
/**
 * Generate the constructor and other methods for the implementation class
 * as required by the Server Registry framework.
 * 
 * @param p_writer The output channel.
 * @exception java.io.IOException General IO exception
 */
void genImplementationFrameworkMethods(PrintWriter p_writer) throws IOException
{
	// Constructor
	p_writer.println("");
	p_writer.println(INDENT + getImplementationClassName()
		+ "(" + m_typeName + " p_localReference) throws "
		+ getRemoteExceptionName());
	p_writer.println(INDENT + "{");
	p_writer.println(INDENT2 + "super();");
	p_writer.println(INDENT2 + "m_localReference = p_localReference;");
	p_writer.println(INDENT + "}");
	//
	// getlocalReference
	p_writer.println("");
	p_writer.println(INDENT + "public Object getLocalReference()");
	p_writer.println(INDENT + "{");
	p_writer.println(INDENT2 + "return m_localReference;");
	p_writer.println(INDENT + "}");
}
/**
 * Generate the import lines.
 *
 * @param wrtr The writer to write output to.
 */
void genImportLines(PrintWriter p_writer)
{
	p_writer.println("");
	p_writer.println("import " + SERV_REG_REMOTEFULL + ";");
}
/**
 * Generate the interface that extends the given interface
 * for remote object implementation.
 *
 * @exception FileNotFoundException Could not open the output file.
 * @exception IOException General exception while doing IO.
 */
void genInterface() throws FileNotFoundException, IOException
{
	PrintWriter wrtr;
	//wrtr = new PrintWriter(System.out); // For testing only
	String outFileName = m_destFilePrefix + getInterfaceSuffix() + ".java";
	FileOutputStream outStream = new FileOutputStream(outFileName);
	wrtr = new PrintWriter(outStream);
	genHeader(wrtr);
	genPackageLine(wrtr);
	genInterfaceBegin(wrtr);
	wrtr.println("}");
	wrtr.flush();
}
/**
 * Generate the interface beginning section, from name to everything up
 * to and including the opening bracket.
 *
 * @param wrtr The writer to write output to.
 */
void genInterfaceBegin(PrintWriter p_writer)
{
	p_writer.println("");
	p_writer.println("public interface "
		+ getInterfaceName() + " extends "
		+ m_typeName + ", " + getRemoteInterfaceName());
	p_writer.println("{");
}
/**
 * Generate the lines for all the methods in the given interface.
 * 
 * @param p_writer Output channel.
 * @exception java.io.IOException General IO exception.
 */
public void genMethodLines(PrintWriter p_writer) throws IOException
{
	Method[] methods = m_type.getMethods();
	int cnt = methods.length;
	Method aMethod;
	String methodString;
	String modifiers;
	String newModifiers;
	Class returnType;
	String returnValue;
	Class[] params;
	Class aParam;
	int paramCnt;
	int i;
	int j;
	int absIdx;
	for (i = 0; i < cnt; i++)
	{
		aMethod = methods[i];
		methodString = aMethod.toString();
		p_writer.println("");
		//p_writer.print(INDENT + methodString.substring(0, methodString.indexOf("(") + 1));
		modifiers = Modifier.toString(aMethod.getModifiers());
		if ((absIdx = modifiers.indexOf(ABSTRACT_MODIFIER)) > -1)
		{
			// Remove the "abstract" modifier
			newModifiers = modifiers.substring(0, absIdx);
			if ((absIdx += ABSTRACT_MODIFIER.length() + 1) < modifiers.length())
			{
				newModifiers += modifiers.substring(absIdx);
			}
			modifiers = newModifiers;
		}
		//
		// Process return type
		returnType = aMethod.getReturnType();
		returnValue = returnType.getName();
		//
		p_writer.print(INDENT + modifiers + " " + returnValue + " " + aMethod.getName() + "(");
		//
		// Process the parameters
		params = aMethod.getParameterTypes();
		paramCnt = params.length;
		for (j = 0; j < paramCnt; j++)
		{
			aParam = params[j];
			if (j > 0)
			{
				p_writer.println(",");
				p_writer.print(INDENT3);
			}
			p_writer.print(aParam.getName() + " param" + Integer.toString(j + 1));
		}
		p_writer.println(methodString.substring(methodString.indexOf(")")));
		p_writer.println(INDENT + "{");
		//
		// Determine whether to return a value
		p_writer.print(INDENT + INDENT);
		if (!returnValue.equals("void"))
		{
			p_writer.print("return ");
		}
		p_writer.print("m_localReference." + aMethod.getName() + "(");
		//
		// Add parameters to method call if necessary
		for (j = 0; j < paramCnt; j++)
		{
			if (j > 0)
			{
				p_writer.println(", ");
				p_writer.print(INDENT3);
			}
			p_writer.print("param" + Integer.toString(j + 1));
		}
		p_writer.println(");");
		p_writer.println(INDENT + "}");
	}
}
/**
 * Generate the package line.
 *
 * @param wrtr The writer to write output to.
 */
void genPackageLine(PrintWriter p_writer)
{
	if (m_packageName != null)
	{
		p_writer.println();
		p_writer.println("package " + m_packageName + ";");
	}
}
/**
 * Get the name of the implementation class.
 * 
 * @return The name of the implementation class.
 */
String getImplementationClassName()
{
	return m_typeName + getImplementationSuffix();
}
/**
 * Get the suffix of the name of the implementation class to generate.
 *
 * @return The suffix of the name of the implementation class to generate.
 */
abstract String getImplementationSuffix();
/**
 * Construct an instance that can be used to generate
 * remote extension for the given type.
 *
 * @param p_generator The name of the code generator.
 * @param p_typeName The name of the type to generate
 *                   remote extension for.
 * @exception ClassNotFoundException The specified type is not found.
 * @exception NotInterfaceException The specified type is not an interface.
 * @exception NoImplementationException Problem with the implementation class.
 */
public static pxgen getInstance(String p_generator, String p_typeName) throws ClassNotFoundException, NotInterfaceException, NoImplementationException
{
	if (p_generator == null)
	{
		p_generator = DEFAULT_GENERATOR;
	}
	Class genClass = null;
	pxgen pxGenerator = null;
	try
	{
		genClass = Class.forName(p_generator);
		pxGenerator = (pxgen) genClass.newInstance();
	}
	catch (ClassNotFoundException cnfe)
	{
		throw new NoImplementationException();
	}
	catch (InstantiationException ie)
	{
		throw new NoImplementationException(ie.getMessage());
	}
	catch (IllegalAccessException iae)
	{
		throw new NoImplementationException(iae.getMessage());
	}
	pxGenerator.init(p_typeName);
	return pxGenerator;
}
/**
 * Get the name of the remote interface that extends the specified interface.
 * 
 * @return The name of the remote interface that extends the specified interface.
 */
String getInterfaceName()
{
	return m_typeName + getInterfaceSuffix();
}
/**
 * Get the suffix of the name of the interface to generate.
 *
 * @return The suffix of the name of the interface to generate.
 */
abstract String getInterfaceSuffix();
/**
 * Return the class name for the remote exception.
 * 
 * 	@return The full name of the remote exception class.
 * 
 */
abstract String getRemoteExceptionName();
/**
 * Get the name of the remote interface to extend.
 *
 * @return The name of the remote interface to extend.
 */
abstract String getRemoteInterfaceName();
/**
 * Get the name of the remote object to extend.
 *
 * @return The name of the remote object to extend.
 */
abstract String getRemoteObjectName();
/**
 * Initialize this instance.
 *
 * @param p_typeName The name of the type to generate
 *                   remote extension for.
 * @exception java.lang.ClassNotFoundException Failed to load the specified type.
 * @exception NotInterfaceException The specified type is not an interface.
 */
void init(String p_typeName) throws ClassNotFoundException, NotInterfaceException
{
	m_type = Class.forName(p_typeName);
	if (!m_type.isInterface())
	{
		throw new NotInterfaceException();
	}
	//
	//Package thePackage = m_type.getPackage();
	String fullName = m_type.getName();
	m_destFilePrefix = fullName.replace('.', System.getProperty("file.separator").charAt(0));
	int lastDotPos = fullName.lastIndexOf(".");
	if (lastDotPos < 1)
	{
		m_packageName = null;
		m_typeName = fullName;
	}
	else
	{
		m_packageName = fullName.substring(0, lastDotPos);
		m_typeName = fullName.substring(lastDotPos + 1);
	}
	m_writeCreationInfo = true;
}
/**
 * Main method that runs when this class is invoked from the
 * command line.
 * @param args java.lang.String[]
 */
public static void main(String[] args) throws IOException
{
	int argCount = args.length;
	String arg;
	int i;
	String generatorName = DEFAULT_GENERATOR;
	for (i = 0; i < argCount; i++)
	{
		arg = args[i];
		if (arg.charAt(0) == '-' && arg.length() > 1)
		{
			switch (arg.charAt(1))
			{
				case 'j' :
					generatorName = DEFAULT_GENERATOR;
				case 'w' :
					generatorName = "WLRMI";
				default :
					break;
			}
		}
		else
		{
			break;
		}
	}
	generatorName = GENERATOR_PREFIX + generatorName;
	if (argCount <= i)
	{
		showUsage();
		return;
	}
	String typeName = args[i];
	String classFile = typeName + ".class";
	// Create an instance of this class for the given type
	pxgen generator;
	try
	{
		generator = pxgen.getInstance(generatorName, typeName);
		System.err.println("Generating remote extension for interface " + typeName);
		generator.genInterface();
		generator.genImplementation();
	}
	catch (ClassNotFoundException cnfe)
	{
		System.err.println("Cannot find class file " + classFile);
		return;
	}
	catch (NotInterfaceException nie)
	{
		System.err.println("The type in " + classFile + " is not an interface");
		return;
	}
	catch (NoImplementationException nime)
	{
		System.err.println("There are problems with the implementation class");
		nime.printStackTrace(System.err);
	}
	//
}
/**
 * Show a "usage" message on the standard error channel.
 */
static void showUsage()
{
	System.err.println("Usage: pxgen type_name");
}
}
