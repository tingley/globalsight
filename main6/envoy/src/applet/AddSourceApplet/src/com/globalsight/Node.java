package com.globalsight;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

public class Node
{
    private final Node parent;
    private final File self;
    private final int indexInParent;
    private final String displayName;
    
    private static File[] EMPTY_CHILDREN_ARRAY = {};
    public static FileComparator m_fileComparator = new FileComparator(
            0, null, true);;

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
            if (!file.isHidden())
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

    public Node getParent()
    {
        return parent;
    }

    public int getIndexInParent()
    {
        return indexInParent;
    }

    public File getSelf()
    {
        return self;
    }
}
