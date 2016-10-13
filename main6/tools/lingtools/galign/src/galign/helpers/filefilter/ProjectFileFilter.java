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

package galign.helpers.filefilter;

import java.io.File;

/**
 * A file filter that choses GAP and ZIP files.
 */
public class ProjectFileFilter
    implements java.io.FilenameFilter
{
    public boolean accept(File f, String name)
    {
        boolean result = false;


        if (name.endsWith(".gap"))
        {
            result = true;
        }

        return result;
    }

    public String getDescription()
    {
        return "Project Files (*.gap)";
    }
}
