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

package galign.data;

import galign.Setup;
import galign.helpers.AlignmentPackage;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A ProjectNode is a node in the project tree
 */
public class ProjectNode extends DefaultMutableTreeNode
{
    public static final int NO_PROJECT_ROOT_NODE = 1;
    public static final int PROJECT_ROOT_NODE = 2;
    public static final int PAGE_NODE = 3;
    public static final int PROJECT_FILE_NODE = 4;

    public static final String NO_PROJECT_NAME = Setup.getString("label.noproject");

    private int nodeType;
    private String nodeName;
    private AlignmentPackage.File file;

    public ProjectNode(int nodeType, String nodeName)
    {
        super();
        this.nodeType = nodeType;
        this.nodeName = nodeName;
    }

    public ProjectNode(int nodeType, String nodeName, AlignmentPackage.File file)
    {
        super();
        this.nodeType = nodeType;
        this.nodeName = nodeName;
        this.file = file;
    }
    
    public int getNodeType()
    {
        return nodeType;
    }

    public AlignmentPackage.File getFileInfo()
    {
        return file;
    }

    public String toString()
    {
        return nodeName;
    }
}

