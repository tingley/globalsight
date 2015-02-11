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

// Core Java classes
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Parses a result set for names and data for each column in the
 * result set.
 * 
 * @version     1.0
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         05/17/1999   Ininitial version.
 */

public class JDBCResultSetParser
{
	int      m_columnCount;
	String[] m_columnNames;  // Names of all the columns in the result set
	int[]    m_columnTypes;  // SQL types of all the columns in the result set
/**
 * Constructor an instance using the given ResultSetMetaData to intialize
 * the column information (name and type).
 *
 * @param resultInfo The information of the result set to parse.
 */
public JDBCResultSetParser(ResultSetMetaData resultInfo) throws SQLException
{
	super();
	int columnCount = resultInfo.getColumnCount();
	m_columnCount = columnCount;
	if (columnCount > 0)
	{
		m_columnNames = new String[columnCount];
		m_columnTypes = new int[columnCount];
		for (int i=0; i<columnCount; i++)
		{
			m_columnNames[i] = resultInfo.getColumnName(i+1);
			m_columnTypes[i] = resultInfo.getColumnType(i+1);
		}
	}
}
/**
 * Get all the columns in the given ResultSet.
 * @return All the columns in the given ResultSet.
 * @param result The result set to get column data from.
 * @exception java.sql.SQLException Database exception.
 */
public ColumnData[] getColumns(ResultSet result) throws SQLException
{
	ColumnData[] columns = null;
	int columnCount;
	if ((columnCount = m_columnCount) > 0)
	{
		columns = new ColumnData[columnCount];
		for (int i = 0; i < columnCount; i++)
		{
			columns[i] = new ColumnData(m_columnNames[i], result.getObject(i+1));
		}
	}
	return columns;
}
}
