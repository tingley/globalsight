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

package installer;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class ExtensionFilter 
    extends FileFilter
{
    private String extension;
    
    public ExtensionFilter(String ext)
    {
        extension = ext;
    }
    
    public boolean accept(File f)
    {
        if (extension.equals("directory"))
            return f.isDirectory();
        else if (f.isDirectory())
            return true;
        else
            return f.getName().endsWith("." + extension);
    }
    
    public String getDescription()
    {
        if (extension.equals("directory"))
            return "Directories";
        else if (extension.equals("exe"))
            return "Applications (*.exe)";
        else
            return extension + " Files (*." + extension + ")";
    }
    
}