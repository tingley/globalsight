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

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;


/**
 * Get parent directory name of a file.
 *
 * <b>getparent</b> element takes two required attributes. <b>file</b>
 * takes a file name.  <b>property</b> takes a property name in which
 * the parent directory name is stored.  The following xml snippet
 * shows how to use this task.

<pre>
    <getparent file="c:\proj\build\data1.txt" property="dir" />
</pre>
 */

public class GetParent extends Task
{
    private File m_file = null;
    private String m_property = null;

    // The method executing the task
    public void execute()
	throws BuildException
    {
        if(m_file == null)
        {
            throw new BuildException("<getparent>: file attribute must be set!");
        }
        if(m_property == null)
        {
            throw new BuildException("<getparent>: property attribute must be set!");
        }
        
        project.setProperty(m_property, m_file.getParent());
    }

    // The setter for the "file" attribute
    public void setFile(File file)
    {
	m_file = file;
    }

    // The setter for the "tofile" attribute
    public void setProperty(String property)
    {
	m_property = property;
    }

}

