/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.workflow;

// IO
import java.awt.Point;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jbpm.graph.def.Node;

import com.globalsight.util.StringUtil;

/**
 * <code>WorkflowNodeParameter</code> converts the xml stype string to the
 * standard format xml stream, and parse it using <code>dom4j</code>. <br>
 * It provides some basic method to get the attributes from the xml file. You
 * can also get the sub element by method
 * {@code WorkflowNodeParameter#getsubNodeParameter(String)}
 * 
 * @see org.dom4j.io.SAXReader
 * @version 1.2
 * 
 */
public class WorkflowNodeParameter
{
	// ////////////////////////////////////////////////////////
	// Field
	// ////////////////////////////////////////////////////////
	private Element rootElement;

	private static final Logger c_category = Logger
			.getLogger(WorkflowNodeParameter.class);

	/**
     * Add the root element to the configue to use the dom4j to parse the
     * string.
     */
	public static final String XML_ROOT_PRE = "<root>";

	public static final String XML_ROOT_CONTENT = "@content@";

	public static final String XML_ROOT_SUF = "</root>";

	public static final String XML_ROOT = XML_ROOT_PRE + XML_ROOT_CONTENT
			+ XML_ROOT_SUF;

	public static final int DEFAULT_INT_VALUE = 0;

	private SAXReader reader = new SAXReader();

	// ////////////////////////////////////////////////////////
	// Construct/init method
	// ////////////////////////////////////////////////////////

	public static WorkflowNodeParameter createInstance(String configure)
	{
		if (StringUtil.isEmpty(configure))
		{
			return new WorkflowNodeParameter(StringUtil.EMPTY_STRING);
		}
		else
		{
			return new WorkflowNodeParameter(configure);
		}
	}

	public static WorkflowNodeParameter createInstance(Node node)
	{
		return createInstance(WorkflowJbpmUtil.getConfigure(node));
	}

	private WorkflowNodeParameter(String configure)
	{
		init(configure);
	}

	/**
     * Constructor the field with the given {@code Element}
     * 
     * @param element
     */
	private WorkflowNodeParameter(Element element)
	{
		if (element == null)
		{
			c_category.error("The rootelement is null when constructor ");
			c_category
					.error("The root element is initialized by passed Element ");
		}

		this.rootElement = element;
	}

	/**
     * Converts the configure file to standard format and parse the xml stream
     * to the document.
     * 
     * @param configure
     *            The configure string to be read.
     * @return The {@code Document}
     */
	private void init(String configure)
	{
		String value = XML_ROOT.replaceAll(XML_ROOT_CONTENT, configure);
		Document document = null;
		try
		{
			document = reader.read(new StringReader(value));
			this.rootElement = document.getRootElement();
		}
		catch (DocumentException e)
		{
			c_category.error("Error occured when read the string " + configure);
			c_category.error("The string " + configure, e);
		}

	}

	// ////////////////////////////////////////////////////////
	// Begin: Pulibc Method
	// ////////////////////////////////////////////////////////
	/**
     * Gets the text of the given name. <br>
     * The default value will be returned when the element is not exist or the
     * text value is null.
     * 
     * @param name
     *            The name of the parameter.
     * @param defaultValue
     *            The default value when the element or the text is null.
     * @return The attribute value.
     */
	public String getAttribute(String name, String defaultValue)
	{
		Element element = rootElement.element(name);
		if (element != null)
		{
			String text = element.getText();
			return text == null ? defaultValue : text;
		}
		else
		{
			return defaultValue;
		}
	}

	/**
     * Gets the text of the given name. <br>
     * The NULL value will be returned when the element is not exist or the text
     * value is null.
     * 
     * @param name
     *            The name of the parameter.
     * @return The attribute value.
     */
	public String getAttribute(String name)
	{
		return getAttribute(name, "");
	}

	/**
     * Gets the point value by the given name.
     * 
     * @param name
     * @return
     */
	public Point getPointAttribute(String name)
	{
		String pointValue = getAttribute(name);
		if (StringUtil.isEmpty(pointValue))
		{
			/* for the intermediate-points considition */
			return new Point(0, 0);
		}

		String[] xy = pointValue.split(WorkflowConstants.POINT_SEPARATOR);
		if (xy.length < 2)
		{
			c_category.error("The value of the xy array is " + xy);
			throw new IllegalArgumentException(
					"the point format is illegal, and the value is "
							+ pointValue);
		}

		return new Point(Float.valueOf(xy[0]).intValue(), Float.valueOf(xy[1])
				.intValue());

	}

	/**
     * Gets the point value array by the given name.
     * 
     * @param name
     * @return
     */
	public Point[] getPointsAttribute(String name)
	{
		String pointValue = getAttribute(name);
		if (StringUtil.isEmpty(pointValue))
		{
			/* for the intermediate-points considition */
			return new Point[0];
		}

		String[] xy = pointValue.split(",");
		Point[] points = new Point[xy.length];
		for (int i = 0; i < xy.length; i++)
		{
			String[] xyz = xy[i].split(WorkflowConstants.POINT_SEPARATOR);
			if (xyz.length < 2)
			{
				c_category.error("The value of the xy array is " + xy);
				throw new IllegalArgumentException(
						"the point format is illegal, and the value is "
								+ pointValue);
			}
			points[i] = new Point(Float.valueOf(xy[0]).intValue(), Float
					.valueOf(xy[1]).intValue());
		}

		return points;

	}

	/**
     * Gets the boolean value of the given parameter.
     * 
     * @param name
     *            The name of the parameter.
     * @return the boolean value
     */
	public boolean getBooleanAttribute(String name)
	{
		String booleanValue = getAttribute(name);

		if (StringUtil.isEmpty(booleanValue))
		{
		    return false;
		}

		return new Boolean(booleanValue).booleanValue();
	}

	/**
     * Gets the int value of the given parameter.
     * 
     * @param name
     *            The name of the parameter.
     * @param defaultValue
     *            The default value of the int.
     * @return The int value.
     */
	public int getIntAttribute(String name, int defaultValue)
	{

		String intValue = getAttribute(name);

		if (StringUtil.isEmpty(intValue))
		{
			return defaultValue;
		}

		return Integer.parseInt(intValue);
	}

	/**
     * Gets the int value of the given parameter.
     * 
     * @param name
     *            The name of the parameter.
     * @param defaultValue
     *            The default value of the int.
     * @return The int value.
     */
	public int getIntAttribute(String name)
	{
		return getIntAttribute(name, DEFAULT_INT_VALUE);
	}

	/**
     * Gets the long value of the given parameter.
     * 
     * @param name
     *            The name of the parameter.
     * @param defaultValue
     *            The default value of the long.
     * @return The value .
     */
	public long getLongAttribute(String name, long defaultValue)
	{

		String value = getAttribute(name);

		if (StringUtil.isEmpty(value))
		{
			return defaultValue;
		}

		return Long.parseLong(value);
	}

	/**
     * Gets the array of the given name.
     * 
     * @param name
     *            The name of the parameter.
     * @return The value.
     */
	public String[] getArrayAttribute(String name)
	{
		String value = getAttribute(name);

		if (StringUtil.isEmpty(value))
		{
			return null;
		}

		return value.split(",");

	}

	/**
     * Gets the sub {@code WorkflowNodeParameter} for the given name.
     * 
     * @param name
     *            The name of the element.
     * @return The sub workflowNodeParameter.
     */
	public WorkflowNodeParameter getsubNodeParameter(String name)
	{
		Element element = rootElement.element(name);

		if (element == null)
		{
			c_category.error("There is no element under the name " + name);
			c_category.error("The content of the rootElement is "
					+ restore(rootElement));
		}

		return new WorkflowNodeParameter(rootElement.element(name));
	}

	/**
     * Gets the sub <code>WorkflowNodeParameter</code>. <br>
     * If the sub element doesn't exist, create a new one.
     * 
     * @param name
     *            The name of the sub element.
     * @return The {@code WorkflowNodeParametr}.
     */
	public WorkflowNodeParameter subNodeparameter(String name)
	{
		if (rootElement.element(name) == null)
		{
			return new WorkflowNodeParameter(rootElement.addElement(name));
		}
		else
		{
			return new WorkflowNodeParameter(rootElement.element(name));
		}
	}
	
	/**
     * Gets the sub <code>WorkflowNodeParameter</code>. <br>
     * If the sub element doesn't exist, return null.
     * 
     * @param name
     *            The name of the sub element.
     * @return The {@code WorkflowNodeParametr}.
     */
	public WorkflowNodeParameter subNodeparameterDefaultNull(String name)
	{
		if (rootElement.element(name) == null)
		{
			return null;
		}
		else
		{
			return new WorkflowNodeParameter(rootElement.element(name));
		}
	}
	
	

	/**
     * Sets the text value for the given attribute.
     * 
     * @param name
     *            The name of the attribute.
     * @param value
     *            The text value of the attribute.
     * @return The rootElement.
     */
	public Element setAttribute(String name, String value)
	{
		if (value == null)
		{
			c_category.warn("The value for the elment named " + name
					+ " is null ");
			return rootElement;
		}

		Element element = rootElement.element(name);

		if (element == null)
		{
			/* If the node doesn't exit, create the element directy */
			element = rootElement.addElement(name);
		}

		element.setText(value);
		return rootElement;
	}

	/**
     * Restores the <code>Element</code> to the original string.
     * 
     * @param element
     *            The Element.
     * @return The original string.
     */
	public String restore(Element element)
	{
		StringWriter s = new StringWriter();
		XMLWriter reader = new XMLWriter(s);
		try
		{
			reader.write(element);
		}
		catch (IOException e)
		{
			c_category
					.error("Error occured when write the element, the element is "
							+ element.toString());
			c_category.error(
					"The stack of the error when write the element is ", e);
		}
		return s.toString().replaceAll(XML_ROOT_PRE, StringUtil.EMPTY_STRING)
				.replaceAll(XML_ROOT_SUF, StringUtil.EMPTY_STRING);
	}

	/**
     * Restores the <code>WorkflowNodeParameter</code> to the original string.
     * 
     * @param workflowNodeParameter
     *            {@code WorkflowNodeParameter}.
     * @return The original string.
     */
	public String restore(WorkflowNodeParameter workflowNodeParameter)
	{
		return restore(workflowNodeParameter.getRootElement());
	}

	/**
     * Restores the <code>WorkflowNodeParameter</code> to the original string.
     * 
     * @return The original string.
     */
	public String restore()
	{
		return restore(this.getRootElement());
	}

	/**
     * Removes the element with the specified name. <br>
     * If remove the element successfully, return the rootElement, otherwise
     * return null.
     * 
     * @param name
     *            The name of the element.
     * @return Return the rootElement when success and return null when fail.
     */
	public void removeElement(String name)
	{
	    Element ele = rootElement.element(name);
	    if (ele != null)
	    {
	        rootElement.remove(ele);
	    }
	}

	/**
     * Returns the root Elemetn.
     * 
     * @return
     */
	public Element getRootElement()
	{
		return rootElement;
	}

	/**
     * Adds the element to the original one.
     * 
     * @param name
     *            The name of the element.
     * @param value
     *            the text value of the element.
     * @return The {@WorkflowNodeParameter}
     */
	public WorkflowNodeParameter addElementText(String name, String value)
	{

		rootElement.addElement(name).addText(value);

		return this;
	}

	public Element element(String name)
	{
		return this.rootElement.addElement(name);
	}

	// ////////////////////////////////////////////////////////
	// End: Pulibc Method
	// ////////////////////////////////////////////////////////

}
