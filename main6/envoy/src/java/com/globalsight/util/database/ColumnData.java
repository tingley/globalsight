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
package com.globalsight.util.database;

/* Copyright (c) 1999, Global Sight Corporation.  All rights reserved. */

// Import core Java classes
import java.io.Serializable;
import java.sql.Date;

/**
 * A database column and its data.
 * 
 * @version     1.0
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         03/08/1999   Ininitial version.
 */

public class ColumnData implements Serializable, Cloneable
{
	/*
	static final int OBJECT  = 1;
	static final int SHORT   = 2;
	static final int INT     = 3;
	static final int LONG    = 4;
	static final int DOUBLE  = 5;
	static final int FLOAT   = 6;
	static final int BYTE    = 7;
	static final int BOOLEAN = 8;
	static final int DATE    = 9;
	static final int STRING  = 10;
	*/
	String  m_name;         // Column name
	Object  m_data;         // Data of the column
/**
 * Constructor for byte data.
 *
 * @param name Column name.
 * @param data Data for the column.
 */
public ColumnData(String name, byte data)
{
	super();
	m_name = name;
	m_data = new Byte(data);
}
/**
 * Constructor for "double" data.
 *
 * @param name Column name.
 * @param data Data for the column.
 */
public ColumnData(String name, double data)
{
	super();
	m_name = name;
	m_data = new Double(data);
}
/**
 * Constructor for "float" data.
 *
 * @param name Column name.
 * @param data Data for the column.
 */
public ColumnData(String name, float data)
{
	super();
	m_name = name;
	m_data = new Float(data);
}
/**
 * Constructor for integer data.
 *
 * @param name Column name.
 * @param data Data for the column.
 */
public ColumnData(String name, int data)
{
	super();
	m_name = name;
	m_data = new Integer(data);
}
/**
 * Constructor for "long" data.
 *
 * @param name Column name.
 * @param data Data for the column.
 */
public ColumnData(String name, long data)
{
	super();
	m_name = name;
	m_data = new Long(data);
}
/**
 * Constructor for Object type.
 *
 * @param name Column name.
 * @param data Data for the column.
 */
public ColumnData(String name, Object data)
{
	super();
	m_name = name;
	m_data = data;
}
/**
 * Constructor for String data.
 *
 * @param name Column name.
 * @param data Data for the column.
 */
public ColumnData(String name, String data)
{
	super();
	m_name = name;
	m_data = data;
}
/**
 * Constructor for Date data.
 *
 * @param name Column name.
 * @param data Data for the column.
 */
public ColumnData(String name, java.sql.Date date)
{
	super();
	m_name = name;
	m_data = date;
}
/**
 * Constructor for "short" data.
 *
 * @param name Column name.
 * @param data Data for the column.
 */
public ColumnData(String name, short data)
{
	super();
	m_name = name;
	m_data = new Short(data);
}
/**
 * Constructor for boolean data.
 *
 * @param name Column name.
 * @param data Data for the column.
 */
public ColumnData(String name, boolean data)
{
	super();
	m_name = name;
	m_data = new Boolean(data);
}
/**
 * Clone the current instance to create a new instance.
 * @return java.lang.Object new instance of current instance.
 */
public Object clone()
{
	ColumnData thisClone = null;
	try
	{
		thisClone = (ColumnData)super.clone();
	}
	catch (CloneNotSupportedException e)
	{
		e.printStackTrace();
		return null;
	}
	return thisClone;
}
/**
 * Determines whether the given target instance is equal to this instance.
 * Two instances of this class are equal if their column names and values
 * are equal.
 *
 * @return 0 if equal, non-zero otherwise.
 * @param target Target instance to compare to.
 */
public boolean equals(ColumnData target)
{
	return (m_name.equals(target.name()) && m_data.equals(target.value()));
}
/**
 * Get the name of the column.
 * @return java.lang.String name of the column
 */
public String name()
{
	return m_name;
}
/**
 * Get the string value of the data in this column,
 * with each "'" replaced by two "'" so that it
 * can be used in a SQL statement.
 */
public String sqlString()
{
	String str = stringValue();
	if (str.length() > 1)
	{
		char[] orgChars = str.toCharArray();
		int orgLength = orgChars.length;
		int i, count, newLength;
		for (i = 0, count = 0; i < orgLength; i++)
		{
			if (orgChars[i] == '\'')
			{
				count++;
			}
		}
		if (count > 0)
		{
			newLength = orgLength + count;
			char[] newChars = new char[newLength];
			int orgIdx, newIdx;
			for (orgIdx = 0, newIdx = 0; orgIdx < orgLength; orgIdx++)
			{
				if ((newChars[newIdx++] = orgChars[orgIdx]) == '\'')
				{
					newChars[newIdx++] = '\'';
					if (newIdx >= newLength)
					{
						break;
					}
				}
			}
			str = new String(newChars);
		}
	}
	str = "'" + str + "'";
	return str;
}
/**
 * Get the data for the column in the format of a String.
 * @return java.lang.String data for the column.
 */
public String stringValue()
{
	if (m_data != null)
	{
		return m_data.toString();
	}
	else
	{
		return "";
	}
}
/**
 * Format this column into a String that can be used in a SQL statement.
 * The format of the returned String is "column_name='column_value'"
 * where the "column_value" is the column's value converted to a text
 * string.
 *
 * @return Formatted string representation of this column.
 */
public String toSqlSpec()
{
	//return m_name + "='" + stringValue() + "'";
	return m_name + "=" + sqlString();
}
/**
 * Get the data for the column in the format of an Object.
 * @return data for the column.
 */
public Object value()
{
	return m_data;
}
}
