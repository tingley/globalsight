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

package com.globalsight.diplomat.util;

// Diplomat
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;

// SUN java imports
import java.util.Iterator;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// this is a singleton class
public class ConfigParameters
{
  // constants

  // states of the various parameters
  public static final String NEW_PARAMETER = "NEW";
  public static final String DIRTY_PARAMETER = "DIRTY";
  public static final String CLEAN_PARAMETER = "CLEAN";

  // a series of literal strings that identifies the names of various parameters

  // singleton instance
  static private ConfigParameters instance = null;

  // hash table for parameters
  private HashMap parameters = new HashMap();

  // hash table for dirty flags
  HashMap parameterStates = new HashMap();

  // private constructor enforces the Singleton pattern
  private ConfigParameters()
  {
  }

  // returns the sinleton instance of the class
  static public ConfigParameters instance()
  {
    if(instance == null)
    {
      instance = new ConfigParameters();
      // read configuration parameters from the DB
      readConfigurationParameters();
    }
    return instance;
  }

  // returns the value associated to the parameter specified by parameterName
  public String getValue(String parameterName)
  {
    return (String) parameters.get(parameterName);
  }

  // sets the value of a parameter
  public void setValue(String parameterName, String parameterValue)
  {
    // check to see if the parameter exists already
    String aValue = (String) getValue(parameterName);
    // and according to that, set its state
    if(aValue == null)
      setParameterState(parameterName, NEW_PARAMETER);
    else
      setParameterState(parameterName, DIRTY_PARAMETER);
    // set the parameter and its value
    parameters.put(parameterName, parameterValue);
  }

  // writes all the configuration parameters that have been marked as dirty
  // it needs to be synchronized to prevent further updates while the parameters are written to the data base
  synchronized public void writeConfigurationParameters()
  {
    // obtain a connection to GlobalSight's data base
    Connection conn = null;
    try
    {
       conn = ConnectionPool.getConnection();
    }
    catch(Exception exc)
    {
      conn = null;
    }
    if( conn == null )
    {
      // log an error message
      s_theLogger.println(Logger.ERROR,"Failed to write configuration parameters to the data base.");
      return;
    }
    PreparedStatement updateStatement = null;
    PreparedStatement insertStatement = null;
    try
    {
      conn.setAutoCommit(false);
      updateStatement = conn.prepareStatement("UPDATE CONFIG SET VALUE = ? WHERE NAME = ?");
      insertStatement = conn.prepareStatement("INSERT INT CONFIG (NAME, VALUE) VALUES(?, ?)");
      Iterator keys = parameters.keySet().iterator();
      while(keys.hasNext())
      {
        String parameterName = (String) keys.next();
        String parameterValue = (String) getValue(parameterName);
        if( isNew(parameterName))
        {
          insertStatement.setString(1, parameterName);
          insertStatement.setString(2, parameterValue);
          insertStatement.executeUpdate();
        }
        else if( isDirty(parameterName) )
        {
          updateStatement.setString(1, parameterValue);
          updateStatement.setString(2, parameterName);
          updateStatement.executeUpdate();
        }
      }
      conn.commit();
      // after the transaction is committed we reset all the dirty flags
      keys = parameters.keySet().iterator();
      while(keys.hasNext())
      {
        String parameterName = (String) keys.next();
        this.setParameterState(parameterName, CLEAN_PARAMETER);
      }
    }
    catch(SQLException exc)
    {
      s_theLogger.println(Logger.ERROR,"Failed to write configuration parameters to the data base.");
      return;
    }
    finally
    {
      try
      {
        if(updateStatement != null)
          updateStatement.close();
        if(insertStatement != null)
          insertStatement.close();
      }
      catch(SQLException exc2)
      {
      }
      if(conn != null)
      {
        try
        {
           ConnectionPool.returnConnection(conn);
        }
        catch(ConnectionPoolException exc1)
        {}
      }
    }
  }

  // read the configuration parameters from the data base
  static private void readConfigurationParameters()
  {
    // obtain a connection to GlobalSight's data base
    Connection conn = null;
    try
    {
      conn = ConnectionPool.getConnection();
    }
    catch(Exception exc)
    {
       // log an error message
       s_theLogger.printStackTrace(Logger.ERROR,"Failed to read configuration parameters from database:\n", exc);
      return;
    }
    
    PreparedStatement ps = null;
    try
    {
      ps = conn.prepareStatement("SELECT * FROM SYSTEM_PARAMETER");
      ResultSet rs = ps.executeQuery();
      while(rs.next())
      {
        String parameterName = rs.getString("NAME");
        String parameterValue = rs.getString("VALUE");
        instance.parameters.put(parameterName, parameterValue);
        instance.setParameterState(parameterName, CLEAN_PARAMETER);
      }
    }
    catch(SQLException exc)
    {
      s_theLogger.println(Logger.ERROR,"Failed to read configuration parameters from data base.");
      return;
    }
    finally
    {
      if(ps != null)
      {
        try
        {
          ps.close();
        }
        catch(SQLException exc2)
        {
        }
      }
      if(conn != null)
      {
        try
        {
           ConnectionPool.returnConnection(conn);
        }
        catch(ConnectionPoolException exc1)
        {}
      }
    }
  }

  // saves the parameter state
  private void setParameterState(String parameterName, String parameterState)
  {
    parameterStates.put(parameterName, parameterState);
  }

  // indicates whether the corresponding parameter is dirty or clean
  private boolean isDirty(String parameterName)
  {
    String state = (String) parameterStates.get(parameterName);
    if(DIRTY_PARAMETER.equals(parameterName))
      return true;
    else
      return false;
  }

  // indicates whether the corresponding parameter is a new parameter
  private boolean isNew(String parameterName)
  {
    String state = (String) parameterStates.get(parameterName);
    if(NEW_PARAMETER.equals(state))
      return true;
    else
      return false;
  }

  private static Logger s_theLogger = Logger.getLogger();

}
