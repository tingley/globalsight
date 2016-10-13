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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import com.globalsight.ling.common.URLEncoder;

public class Utility
{
	private Utility() {}
	
	/////////////////////////////////////////////////
	// add a single quote to a sql string
  	static public String quote (String p_sql)
  	{
  		String singleQuote = "'";
  		String sql = singleQuote;
  		int index = p_sql.indexOf(singleQuote);  		
  		
  		// Is there a single quote in the string		
  		
	  	while ( index != -1 )
	  	{
	  		// yes, copy the sub-string
	  		sql += p_sql.substring(0, index+1);
	  		// add an extra quote - escape character
	  		sql += singleQuote;
	  		// are there more characters
	  		if (p_sql.length() > index+1)
	  		{
	  			// yes
	  			p_sql = p_sql.substring(index+1);
	  			index = p_sql.indexOf(singleQuote);  			
	  		}
	  		else
	  		{
	  			// no, exit the while loop - we're finished
	  			index = -1;
	  			p_sql = "";
	  		}
	  	}	
	  	
	  	sql += p_sql;
	  	
  		// add the end quote
  		sql += singleQuote;
  		return sql;
  	}
	
  	/////////////////////////////////////////////////
  	// String substitution of %%variable_name%%
  	static public String variableSubstitution (String p_fileName, Hashtable p_replacement)
  		throws IOException
  	{
  		// create an empty hash table if it doesn't exists
  		if (p_replacement == null)
  			p_replacement = new Hashtable();
  			
		// open the file
		FileInputStream	file = new FileInputStream (p_fileName);
		// retrieve the size of the file
		int	fileSize = file.available();
		// allocate space 
		byte[] fileChar = new byte[fileSize];
		// read the file
		file.read(fileChar);
		// convert to a string
		String fileContent = new String(fileChar);
		
		// retrieve all keys from the hash
		Enumeration keys = p_replacement.keys();
		// loop until we don't have anymore keys
		while ( keys.hasMoreElements() ) 
		{
			// retrieve a key
			String key = (String) keys.nextElement();
			// convert key to our standard variable pattern
			String tagKey = "%%" + key + "%%";
			// find the first position of a matching text
			int position = fileContent.indexOf(tagKey);
			// do we have a match?
			while (position != -1) 
			{
				// we have a match
				// retrieve the string before the variable
				String startString = fileContent.substring(0, position);
				// add the replacement string
				startString = startString + (String)p_replacement.get(key);
				// retrieve the other half of our string without the variable
				String endString = fileContent.substring(position + tagKey.length());
				// concatenate the string
				fileContent = startString + endString;
				// find more variables with the same key
				position = fileContent.indexOf(tagKey);		
			}
		}
		return fileContent;	
  	}

  	static public String URLEncodePath(String p_str)
        {
            StringTokenizer st = new StringTokenizer(p_str, "/");
            StringBuffer test = new StringBuffer("");
            while (st.hasMoreTokens())
                test.append("/" + URLEncoder.encode(st.nextToken()));
            return test.toString();
        }

  	public static void main(String[] args)
	{
		System.out.println("Filename: " + args[0]);
		
  		Hashtable hash = new Hashtable();
		hash.put ("message", "This is a test!!");
		try 
		{
			String output = Utility.variableSubstitution (args[0], hash);
			System.out.println(output);	
		}
		catch (IOException e) 
		{
			System.out.println (e);
		}
	}
}