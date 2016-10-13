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

package galign.helpers.project;

import galign.Setup;
import galign.data.Project;
import galign.data.ProjectNode;
import galign.helpers.AlignmentPackage;

import java.io.File;
import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * This class builds a tree model (a UI data object) from an
 * AlignmentPackage. The aligned filenames from the GAP file are
 * copied into a JTree which gets displayed in the Project Files
 * panel.
 */
public class ProjectTreeHelper
{
    static public final String PAGE = Setup.getString("label.page");

    static public DefaultTreeModel createEmptyProjectTree()
    {
        ProjectNode root = new ProjectNode(ProjectNode.NO_PROJECT_ROOT_NODE,
                                           ProjectNode.NO_PROJECT_NAME);
        return new DefaultTreeModel(root);
    }

    static public DefaultTreeModel createProjectTree(Project p_project)
    {
        ProjectNode root = new ProjectNode(ProjectNode.PROJECT_ROOT_NODE,
                                           p_project.getName());

		ArrayList files = p_project.getAlignmentPackage().getFiles();

		for (int i = 0, max = files.size(); i < max; i++)
		{
			AlignmentPackage.File file = (AlignmentPackage.File)files.get(i);

            ProjectNode alignment = new ProjectNode(ProjectNode.PAGE_NODE,
                                               PAGE + " " + String.valueOf(i + 1),
                                                file);
            root.add(alignment);
                                               
            ProjectNode node = new ProjectNode(ProjectNode.PROJECT_FILE_NODE,
                                getFileName(file.getOriginalSourceFileName()));
            alignment.add(node);
            node = new ProjectNode(ProjectNode.PROJECT_FILE_NODE,
                            getFileName(file.getOriginalTargetFileName()));
            alignment.add(node);
		}

        return new DefaultTreeModel(root);
    }

	static private String getFileName(String p_filename)
	{
		int index = p_filename.lastIndexOf('/');
		if (index < 0)
		{
			p_filename.lastIndexOf('\\');
		}

		if (index > 0)
		{
			return p_filename.substring(index + 1);
		}

		return p_filename;
	}
}
