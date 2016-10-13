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
package test;

import java.util.StringTokenizer;
/**
 * Creates a filtered list of file objects from multiple directories.
 * Use the add() method to build the list.
 * Use the getNextFile() method to retreive items from the list.
 * Creation date: (8/16/2000 3:31:22 PM)
 * @author: Bill Brotherton
 */
 
import java.util.Vector;
import java.io.File;
import java.io.FileFilter;

public class FileListBuilder implements FileFilter {
	private java.lang.String m_Ext = ".txt";
	private java.lang.String m_Dir = "";
	private static java.util.Vector m_vTheList = new Vector();;
	private java.lang.String m_Description ="" ;
	private static int m_Idx = 0;
/**
 * Insert the method's description here.
 * Creation date: (8/16/2000 5:49:45 PM)
 */
public FileListBuilder()
{
	super();
	m_Idx = 0;
}
/**
 * FileList constructor comment.
 */
public FileListBuilder(String p_Ext) {
	super();
	m_Ext = p_Ext;
	m_Idx = 0;
}
/**
 * Part of the java FileFilter interface - called by invoking File.listFiles() in add().
 * Use the add() method instead of this method.
 * Creation date: (8/16/2000 4:04:30 PM)
 * @return boolean
 * @param param java.io.File
 */
public boolean accept(File f) {

	return f.getName().toLowerCase().endsWith(m_Ext) && !f.isDirectory();
}
/**
 * Adds a filtered directory of files to the list.
 * Creation date: (8/16/2000 5:00:24 PM)
 * @param p_aExt java.lang.String[]
 */
public void add(String p_Dir, String p_Ext) 
{

	File f = new File(p_Dir);
	
	if(!f.exists()) return;
	
	// filter for each extension...
	StringTokenizer st = new StringTokenizer(p_Ext,",");
	while (st.hasMoreTokens()) 
	{
		//... in the current dir
		File[] list = f.listFiles(new FileListBuilder(st.nextToken()));
		for(int j=0; j < list.length; j++ )
			m_vTheList.add(list[j]);
	}
}
/**
 * Filters and adds files from the root directory on down.
 * @param p_Dir java.lang.String
 * @param p_Ext java.lang.String
 */
public void addRecursive(String p_Dir, String p_Ext)
{

	File f = new File(p_Dir);
	File[] list;
	
	if(!f.exists()) return;

	// filter for each extension...
	StringTokenizer st = new StringTokenizer(p_Ext,",");
	while (st.hasMoreTokens()) 
	{
		// ... in current dir
		list = f.listFiles(new FileListBuilder(st.nextToken()) );
		for(int j=0; j < list.length; j++ )
			m_vTheList.add(list[j]);
	}

	// then recurse sub dir
	list = f.listFiles();
	for(int j=0; j < list.length; j++ )
		if(list[j].isDirectory())
			this.addRecursive(list[j].getPath(), p_Ext);
	
}
/**
 * Get the file list description.
 * Creation date: (8/16/2000 4:04:57 PM)
 * @return java.lang.String
 */
public String getDescription() {
	return m_Description; 
}
/**
 * Returns the next File object in the list.
 * @return java.io.File - or - null when no more files are left.
 */
public File getNextFile() {

	if(m_Idx < m_vTheList.size())
		return (File)(m_vTheList.elementAt(m_Idx++));
	else
		return null;
}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {


 FileListBuilder ff = new FileListBuilder();
 ff.add("c:\\work\\ling\\test\\javaprop",".properties");
 ff.add("c:\\work\\ling\\test\\javaprop",".txt");
 ff.setDescription("Properties & text Files");

 File f; 
 while( (f= ff.getNextFile()) != null )
	System.out.println(f.getPath());
 System.out.println(ff.getDescription());
}
/**
 * Set the file lists description.
 * Creation date: (8/16/2000 5:44:58 PM)
 * @param p_Desc java.lang.String
 */
public void setDescription(String p_Desc) 
{
	m_Description = p_Desc;	
}
}
