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
 * Append a file to another file.
 *
 * <b>appendfile</b> element takes two required
 * attributes. <b>fromfile</b> takes a file name which the data is
 * copied from. <b>tofile</b> takes a file name which the data is
 * append to. If the file doesn't exist, it will be created. The
 * following xml snippet shows how to use this task.

<pre>
    <appendfile fromfile="c:\proj\build\data1.txt" tofile="xx.output" />
</pre>
 */

public class AppendFile extends Task
{
    private static final int BUFFER_LEN = 4096;

    private File m_fromFile = null;
    private File m_toFile = null;

    // The method executing the task
    public void execute()
	throws BuildException
    {
        if(m_fromFile == null)
        {
            throw new BuildException("<appendfile>: fromfile attribute must be set!");
        }
        if(m_toFile == null)
        {
            throw new BuildException("<appendfile>: tofile attribute must be set!");
        }
        
	String toName = m_toFile.getAbsolutePath();

	byte[] readBuffer = new byte[BUFFER_LEN];
	int readLen = 0;
	try
        {
            InputStream in = new FileInputStream(m_fromFile);
            OutputStream out = new FileOutputStream(toName, true);

            while((readLen = in.read(readBuffer)) != -1)
            {
                out.write(readBuffer, 0, readLen);
            }
            in.close();
            out.close();
        }
	catch(Exception e)
        {
            throw new BuildException(e);
        }
    }

    // The setter for the "fromfile" attribute
    public void setFromfile(File file)
    {
	m_fromFile = file;
    }

    // The setter for the "tofile" attribute
    public void setTofile(File file)
    {
	m_toFile = file;
    }

}

