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
package com.globalsight.everest.webapp.pagehandler.administration.imp;
// java
import java.io.File;
import java.io.FileFilter;
import java.util.Hashtable;
import java.util.Vector;
// com.globalsight
import com.globalsight.cxe.entity.fileextension.FileExtension;
/**
 * This class is used to filter during search operation for particular files with
 * specified file extensions in the filesystem in correlation with Import File UI.  
 */
public class ImportFileFilter implements FileFilter 
{
    private Hashtable m_fileExtensions = new Hashtable();
    
    public ImportFileFilter(Vector p_fileExtensions) 
    {
	setUpHashtable(p_fileExtensions);
    }
    
    // override the abstract method to specify
    // if the file should be included in the list or not.
    public boolean accept(File p_pathName) 
    {
        // only run through the next piece if the hashtable contains some extensions
        if (m_fileExtensions.size() > 0)
        {
            if (p_pathName.isFile()) 
            {
	        String fileName = p_pathName.getName();
	        int x = fileName.lastIndexOf(".");
	        if (x > 0) 
                {
                    String ext = fileName.substring(x+1);
		    String extL = ext.toLowerCase();
		    String extU = ext.toUpperCase();

		    if (m_fileExtensions.containsKey(ext) ||
		        m_fileExtensions.containsKey(extL) ||
		        m_fileExtensions.containsKey(extU))
		    {
		        return true;
		    }
		    else
		        return false;
	        }
	    } else if (p_pathName.isDirectory()) 
            {
 		// No longer recursively scan directories
 		return true;
            }
            return false;
	}
        // no file extensions specified - so all allowed
	return true;
    }

    // place all the file extension objects from the VECTOR into the HASHTABLE.
    private void setUpHashtable(Vector p_fileExtensions) 
    {
	for (int i=0; i < p_fileExtensions.size(); i++) 
        {
	    m_fileExtensions.put(
                ((FileExtension)p_fileExtensions.elementAt(i)).getName(), 
                p_fileExtensions.elementAt(i)); 
	}
    }
}

