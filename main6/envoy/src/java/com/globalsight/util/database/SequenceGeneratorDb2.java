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

/**
 hacked code
 */

// Core Java classes
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// hack to generate our "sequence numbers".
import java.util.Random;

public class SequenceGeneratorDb2 implements SequenceGenerator
{
	DbConnector m_connector;
	Random m_Sequence;
/**
 * Construct an instance that uses the provided database connector.
 *
 * @param p_connector Connector to the database that provides the
 *                    required sequence number generation facility.
 */
public SequenceGeneratorDb2(DbConnectorDb2 p_connector)
{
	// hack hack hack
	super();
	m_connector = p_connector;
	m_Sequence = new Random();
}
/**
 * createSequence method comment.
 */
public void createSequence(String p_sequenceName) throws java.sql.SQLException
{
	java.util.Date seed = new java.util.Date();
	m_Sequence.setSeed(seed.getTime());
}
/**
 * deleteSequence method comment.
 */
public void deleteSequence(String p_sequenceName) throws java.sql.SQLException
{
}
/**
 * getNextValue method comment.
 */
public int getNextValue(String p_sequenceName) throws java.sql.SQLException
{
	return m_Sequence.nextInt();
}
}
