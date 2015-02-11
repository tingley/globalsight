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
package com.globalsight.everest.webapp.pagehandler.aligner;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * This class filters filenames when searching for files to align.
 */
public class ImportFileFilter
    implements FileFilter
{
    private ArrayList m_extensions;

    public ImportFileFilter(ArrayList p_extensions)
    {
        m_extensions = p_extensions;
    }

    /**
     * Overrides the abstract method to specify if a file should be
     * included in the list or not.
     */
    public boolean accept(File p_pathName)
    {
        // Only run through the next piece if the hashtable contains
        // any extensions.
        if (m_extensions.size() > 0)
        {
            if (p_pathName.isFile())
            {
                String fileName = p_pathName.getName();

                int x = fileName.lastIndexOf(".");
                if (x > 0)
                {
                    String ext = fileName.substring(x + 1);
                    String extL = ext.toLowerCase();

                    if (m_extensions.contains(extL))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            }
            else if (p_pathName.isDirectory())
            {
                // No longer recursively scan directories
                return true;
            }

            return false;
        }

        // no file extensions specified - so all allowed
        return true;
    }
}

