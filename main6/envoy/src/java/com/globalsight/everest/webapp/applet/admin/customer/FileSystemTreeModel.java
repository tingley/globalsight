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
package com.globalsight.everest.webapp.applet.admin.customer;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.globalsight.everest.util.comparator.FileComparator;


/**
 * A tree model implemented for displaying the folders of 
 * the file system for a logged in user with Customer access group.
 */

public class FileSystemTreeModel
        extends DefaultTreeCellRenderer
        implements TreeModel
{
    private static File[] EMPTY_CHILDREN_ARRAY = {};
    private final FileSystemView m_fileSystemView;
    private final HashMap m_fileToNode;
    private final Node m_root;
    private int m_listenerCount;
    private FileComparator m_fileComparator = null;
    private TreeModelListener[] m_treeModelListener;
    
    /**
     * Constructor -- Creates a new instance of FileSystemTreeModel
     */
    public FileSystemTreeModel(FileSystemView p_fileSystemView,
                               Locale p_userLocale)
    {
        m_treeModelListener = new TreeModelListener[4];
        m_fileSystemView = p_fileSystemView;
        m_root = new Node(m_fileSystemView.getRoots());
        m_fileToNode = new HashMap();

        m_fileComparator = new FileComparator(
            0, p_userLocale, true);
    }

    /**
     * Add a listener for the TreeModelEvent posted after the tree changes.
     */
    public void addTreeModelListener(TreeModelListener l)
    {
        if (m_listenerCount == m_treeModelListener.length)
        {
            TreeModelListener[] newData = 
                new TreeModelListener[m_listenerCount * 2];
            System.arraycopy(m_treeModelListener, 0, newData, 
                             0, m_listenerCount);
            m_treeModelListener = newData;
        }
        m_treeModelListener[m_listenerCount++] = l;
    }

    /**
     * Remove a listener previously added with addTreeModelListener().
     */
    public void removeTreeModelListener(TreeModelListener l)
    {
        for (int i=0 ; i<m_listenerCount ; ++i)
            if (m_treeModelListener[i] == l)
            {
                System.arraycopy(m_treeModelListener, i+1, m_treeModelListener, i,
                                 --m_listenerCount - i);
                return;
            }
    }

    /**
     * Returns the child of parent at index index in the parent's child 
     * array. parent must be a node previously obtained from this data 
     * source. This should not return null if index is a valid index for 
     * parent (that is index >= 0 && index < getChildCount(parent))
     */
    public Object getChild(Object parent, int index)
    {
        File file = ((Node)parent).getChild(index, m_fileSystemView);
        Node node = (Node)m_fileToNode.get(file);
        if (node == null)
        {
            node = new Node((Node)parent, file, index, m_fileSystemView);
            m_fileToNode.put(file, node);
        }
        
        return node;
    }

    /**
     * Get a list of children for the given node (directory).
     */
    public File[] getChildren(Object node)
    {
        return ((Node)node).getChildren();        
    }

    /**
     * Returns the number of children of parent. Returns 0 if 
     * the node is a leaf or if it has no children. parent must 
     * be a node previously obtained from this data source.
     */
    public int getChildCount(Object parent)
    {
        return((Node)parent).populateChildren(m_fileSystemView);
    }

    /**
     * Returns the index of child in parent.
     */
    public int getIndexOfChild(Object parent, Object child)
    {
        return((Node)child).indexInParent;
    }

    /**
     * Returns the root of the tree. Returns null only if the 
     * tree has no nodes.
     */
    public Object getRoot()
    {
        return m_root;
    }

    /**
     * Note -- It always return false since only folders are displayed.
     */
    public boolean isLeaf(Object node)
    {
        return false;
    }

    /**
     * This sets the user object of the TreeNode identified by path 
     * and posts a node changed. If you use custom user objects in the 
     * TreeModel you're going to need to subclass this and set the user
     * object of the changed node to something meaningful
     */
    public void valueForPathChanged(TreePath path, Object newValue)
    {
    }

    /**
     * DefaultTreeCellRenderer override -- A renderer that will display
     * the icons just like the file system view.
     */
    public java.awt.Component getTreeCellRendererComponent(
        JTree tree, Object value, boolean selected, boolean expanded,
        boolean leaf, int row, boolean hasFocus)
    {

        super.getTreeCellRendererComponent(
            tree, value, selected, expanded, leaf, row, hasFocus);

        Icon icon = m_fileSystemView.getSystemIcon(((Node)value).self);        
        setIcon(icon);

        return this;
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Inner Class
    //////////////////////////////////////////////////////////////////////
    /**
     * The representation of each file node.
     */
    private class Node
    {
        private final Node parent;
        private final File self;
        private final int indexInParent;
        private final String displayName;

        private File[] children;

        public Node(File[] children)
        {
            this.parent = null;
            this.self = null;
            this.children = children;
            this.indexInParent = 0;
            this.displayName = "";
        }

        public Node(Node parent, File self, int indexInParent, 
                    FileSystemView p_fileSystemView)
        {
            this.parent = parent;
            this.self = self;
            this.indexInParent = indexInParent;

            displayName = p_fileSystemView.getSystemDisplayName(self);
        }

        /**
         * populate the children of this node.
         */
        public int populateChildren(FileSystemView p_fileSystemView)
        {
            if (children != null)
                return children.length;

            File[] allChildren = p_fileSystemView.getFiles(self, true);
            // now sort the table based on the specifiec column index.
            java.util.Arrays.sort(allChildren, m_fileComparator);

            int numDirs = 0;
            for (int i=0 ; i<allChildren.length ; ++i)
            {
                File file = allChildren[i];                
                if (!file.isHidden() && file.isDirectory())
                    allChildren[numDirs++] = file;
            }

            if (numDirs == 0)
                children = EMPTY_CHILDREN_ARRAY;
            else
            {
                children = new File[numDirs];
                System.arraycopy(allChildren, 0, children, 0, numDirs);
            }

            return numDirs;
        }

        /**
         * Get the child at the given index.
         */
        public File getChild(int index, FileSystemView p_fileSystemView)
        {
            populateChildren(p_fileSystemView);
            return children[index];
        }

        /**
         * Show the display name of the file.
         */
        public String toString()
        {
            return displayName;
        }

        /**
         * Get the children of a folder.
         */
        public File[] getChildren()
        {
            if (self.isFile())
            {
                return null;
            }

            File[] allChildren = self.listFiles();
            java.util.Arrays.sort(allChildren, m_fileComparator);
            
            return allChildren;
        }

    }
    //////////////////////////////////////////////////////////////////////
    //  End: Inner Class
    //////////////////////////////////////////////////////////////////////
}



