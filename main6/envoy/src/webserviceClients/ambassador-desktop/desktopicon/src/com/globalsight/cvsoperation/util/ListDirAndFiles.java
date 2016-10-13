package com.globalsight.cvsoperation.util;

import java.io.File;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.log4j.Logger;

import com.globalsight.cvsoperation.entity.CVSFile;
import com.globalsight.util.Constants;

public class ListDirAndFiles 
{
	static Logger log = Logger.getLogger(ListDirAndFiles.class.getName());
	
    public static void list(File pathname, DefaultMutableTreeNode parentNode)
    {	
    	if (!pathname.exists())
    	{
    		log.error("The path '" + pathname + "' doesn't exist!");
    	}
        else
        {
            if (pathname.isFile())
            {
            	CVSFile file = new CVSFile(Constants.NODE_TYPE_FILE);
            	file.setFile(pathname);
            	DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file);
            	parentNode.add(childNode);
            }
            else
            {
            	//ignore "CVS" files
            	if ( !pathname.getName().equalsIgnoreCase("CVS") )
            	{
            		CVSFile dirFile = new CVSFile(Constants.NODE_TYPE_FILE);
            		dirFile.setFile(pathname);
                	DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(dirFile);
                	parentNode.add(childNode);
                    File[] files = pathname.listFiles();
                    if ( files != null && files.length > 0 )
                    {
                    	for (int i = 0; i < files.length; i++ )
                        {
        		             list(files[i], childNode);
                        }	
                    }
            	}
            }
        }
    }
    
    public static void list(File pathname, DefaultMutableTreeNode parentNode, boolean listFile)
    {	
    	if (!pathname.exists())
    	{
    		log.error("The path '" + pathname + "' doesn't exist!");
    	}
        else
        {
            if (pathname.isFile())
            {
            	if (listFile) {
	            	CVSFile file = new CVSFile(Constants.NODE_TYPE_FILE);
	            	file.setFile(pathname);
	            	DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file);
	            	parentNode.add(childNode);
            	}
            }
            else
            {
            	//ignore "CVS" files
            	if ( !pathname.getName().equalsIgnoreCase("CVS") )
            	{
            		CVSFile dirFile = new CVSFile(Constants.NODE_TYPE_FILE);
            		dirFile.setFile(pathname);
                	DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(dirFile);
                	parentNode.add(childNode);
                    File[] files = pathname.listFiles();
                    if ( files != null && files.length > 0 )
                    {
                    	for (int i = 0; i < files.length; i++ )
                        {
        		             list(files[i], childNode, listFile);
                        }	
                    }
            	}
            }
        }
    }
    
}
