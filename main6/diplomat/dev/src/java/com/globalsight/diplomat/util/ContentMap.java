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
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * The ContentMap class manages the mapping between file extensions
 * and the file types, and extract and merge events necessary for Diplomat.
 * It assumes the content map file has the following format:
 * # Comment for ContentMapFile
 * FileExtension | ContentType | ExtractEvent | MergeEvent
 */
public class ContentMap
{
    /** Constructor for ContentMap. Reads in the configuration file
     * and populates the hashtable*/
    public ContentMap(String p_mapfile)
	throws ContentMapException
    {
	FileReader theFileReader;
	BufferedReader theBufferedReader;

	//create the map to put the content map entries in
	_map = new HashMap();

	try {
	    theFileReader = new FileReader(p_mapfile);
	    theBufferedReader = new BufferedReader (theFileReader);
	    String line;
	    ContentMapEntry theContentMapEntry;
	    while (null != (line = theBufferedReader.readLine()))
		{
		    //first check for the comment character
		    if ('#' == line.charAt(0))
			{
			    //System.out.println("Skipping line.");
			    continue;
			}
		    
		    //tokenize the line's contents
		    StringTokenizer st = new StringTokenizer(line," \t\n\r\f|");
		    while (st.hasMoreTokens())
			{
			    String fileExtension = st.nextToken();
			    String contentType = st.nextToken();
			    String extractEvent = st.nextToken();
			    String mergeEvent = st.nextToken();
			    ContentMapEntry cme = new ContentMapEntry
				(fileExtension, contentType,
				 extractEvent, mergeEvent);
			    
			    //associate the file extension (key) with
			    //the content map entry (value)
			    _map.put (fileExtension, cme);
			    System.out.println("Added Key: " + fileExtension +
					       " Hashcode: " + fileExtension.hashCode());
			}
		}

	    System.out.println("Map now has " + _map.size() + " entries.");
	}
	catch (java.io.FileNotFoundException e) {
	    System.out.println("Problem finding mapfile " + p_mapfile + ": " + e);
	    throw new ContentMapException ("Cannot find mapfile.");
	}
	catch (java.io.IOException e) {
	    System.out.println("Problem reading mapfile " + p_mapfile + ": " + e);
	    throw new ContentMapException ("Cannot read mapfile.");
	}
    }

    /** Finds the ContentMapEntry in the content map for the given file extension*/
    public ContentMapEntry find (String p_fileExtension)
    {
	return (ContentMapEntry) _map.get(p_fileExtension);
    }

    //private members
    private Map _map;

    //main for testing
    public static void main (String[] args)
    {
	//create a content map
	System.out.println("Creating ContentMap");
	ContentMap theContentMap;
	try {
	    theContentMap = new ContentMap("mapfile.txt");
	    
	    //query entry
	    String key = new String(".html");
	    System.out.println("Querying an entry: " + key);
	    ContentMapEntry cme = theContentMap.find(key);
	    System.out.println ("Got: " + cme.toString());
	    System.exit(0);
	}
	catch (ContentMapException e) {
	    System.out.println("Caught ContentMapException: " + e);
	}
    }
}
