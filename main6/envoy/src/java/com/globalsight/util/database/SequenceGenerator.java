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
import java.sql.SQLException;

/**
 * This interface defines a set of operation for generating sequence
 * numbers.  The methods defined here are database independent; the
 * classes that implement this interface may be (and usually are)
 * database specific.
 * 
 * @version     1.0, (12/6/99 11:26:32 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         12/06/1999   Initial version.
 */

public interface SequenceGenerator
{
/**
 * Create a new sequence.
 * 
 * @param p_sequenceName The name of the new sequence.
 * @exception java.sql.SQLException Failed to create the new sequence in the database.
 */
void createSequence(String p_sequenceName) throws SQLException;
/**
 * Delete an existing sequence.  Once deleted, the sequence will not be
 * available.  A new sequence created using the same name will start from
 * 1 again.
 * 
 * @param p_sequenceName The name of the sequence to delete.
 * @exception java.sql.SQLException Failed to delete sequence in the database.
 */
void deleteSequence(String p_sequenceName) throws SQLException;
/**
 * Get the next value in the specified sequence.
 * 
 * @return The next value in the specified sequence.
 * @param p_sequenceName Name of the sequence to get a value for.
 * @exception java.sql.SQLException Database related exception.
 */
int getNextValue(String p_sequenceName) throws java.sql.SQLException;
}
