/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
*/

package com.globalsight.tools.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Property;


import java.util.Vector;
import java.io.File;


/**
 * Loop through the given filesets and call a specified target for each file.
 *
 * <b>foreach</b> element has two required attributes, <b>target</b>
 * and <b>property</b>. <b>target</b> takes a target name to be
 * called. <b>property</b> takes a property name in which a file name
 * from the FileSet is stored when the target is
 * executed. <b>foreach</b> element takes at least one &lt;fileset&gt;
 * element. The following xml snippet shows the usage of &lt;foreach&gt;.

<pre>
  <target name="main">
    <foreach target="sub" property="filename" >
      <fileset dir="." includes="*.xml" />
    </foreach>
  </target>

  <target name="sub">
    <echo message="File: ${filename}" />
  </target>
</pre>
*/

public class ForEach extends Task
{
    private String m_target = null;
    private String m_property = null;
    private Vector filesets = new Vector();

    // The method executing the task
    public void execute()
	throws BuildException
    {
        if(m_target == null)
        {
            throw new BuildException("<foreach>: target attribute must be set!");
        }
        if(m_property == null)
        {
            throw new BuildException("<foreach>: property attribute must be set!");
        }
        
        // Loop through files
        for (int i=0; i<filesets.size(); i++)
        {
            FileSet fs = (FileSet) filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            String[] files = ds.getIncludedFiles();
            String[] dirs = ds.getIncludedDirectories();
            callTarget(fs.getDir(project), files, dirs);
        }
    }

    // The setter for the "target" attribute
    public void setTarget(String target)
    {
	m_target = target;
    }

    // The setter for the "property" attribute
    public void setProperty(String property)
    {
	m_property = property;
    }

    // The setter for the FileSet
    public void addFileset(FileSet set)
    {
        filesets.addElement(set);
    }

    private void callTarget(File d, String[] files, String[] dirs)
        throws BuildException
    {
        for (int i=0; i < files.length; i++)
        {
            File f = new File(d, files[i]);
            String pathname = f.getAbsolutePath();
            executeTarget(pathname);
        }

        for (int i=0; i < dirs.length; i++)
        {
            File f = new File(d, dirs[i]);
            String pathname = f.getAbsolutePath();
            executeTarget(pathname);
        }
        
    }
                            
    private void executeTarget(String pathname)
        throws BuildException
    {
        CallTarget newTarget = (CallTarget)project.createTask("antcall");
        newTarget.setOwningTarget(this.target);
        newTarget.setTaskName(getTaskName());
        newTarget.setLocation(location);
        newTarget.init();
        newTarget.setTarget(m_target);
        Property prop = newTarget.createParam();
        prop.setName(m_property);
        prop.setValue(pathname);
        newTarget.execute();
    }

}
