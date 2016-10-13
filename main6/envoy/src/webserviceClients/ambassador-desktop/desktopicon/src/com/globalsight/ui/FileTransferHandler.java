package com.globalsight.ui;

/**
 * FileTransferHandler.java is used by the 1.4
 * 
 */

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.globalsight.util.Constants;
import com.globalsight.util.SwingHelper;
import com.globalsight.util.UsefulTools;

public class FileTransferHandler extends TransferHandler
{
    private static final long serialVersionUID = 2844294537011985870L;

    private DataFlavor fileFlavor = DataFlavor.javaFileListFlavor;
    private static DataFlavor uriListFlavor;
    static {
         try {
             uriListFlavor = new DataFlavor("text/uri-list;class=java.io.Reader");
         } catch (ClassNotFoundException e) { // can't happen
             e.printStackTrace();
         }
    }


	private List<File> files;

	public FileTransferHandler()
	{
		super();
	}

	public static FileTransferHandler install()
	{

		return new FileTransferHandler();
	}

	public boolean importData(JComponent p_jc, Transferable p_t)
	{
		if (!canImport(p_jc, p_t.getTransferDataFlavors()))
		{
			return false;
		}

		try
		{
			if (hasFileFlavor(p_t.getTransferDataFlavors()))
			{
			    List<File> file;
			    if (UsefulTools.isLinux())
			    {   
			        Set<File> files = new HashSet<File>();
			        
			        DataFlavor[] flavors = p_t.getTransferDataFlavors();
			        for (DataFlavor flavor : flavors)
			        {
			            if (flavor.isRepresentationClassReader())
			            {
			                Reader reader = flavor.getReaderForText(p_t);
			                files.addAll(createFileArray(reader));
			            }
			        }
			        
			        file = new ArrayList<File>();
			        file.addAll(files);
			    }
			    else
			    {
			        file = (List<File>) p_t.getTransferData(fileFlavor);
			        
			    }
			    
			    setFiles(file);
				displayFiles(p_jc, getFiles());
			}
		}
		catch (UnsupportedFlavorException ufe)
		{
			System.out.println("importData: unsupported data flavor");
		}
		catch (IOException ieo)
		{
			System.out.println("importData: I/O exception");
		}
		return false;
	}
	
	private static String ZERO_CHAR_STRING = "" + (char)0;
	private static List<File> createFileArray(Reader reader)
    {
	    BufferedReader bReader = new BufferedReader(reader);
	    List<File> list = new ArrayList<File>();
       try { 
           
           java.lang.String line = null;
           while ((line = bReader.readLine()) != null) {
               try {
                   // kde seems to append a 0 char to the end of the reader
                   if(ZERO_CHAR_STRING.equals(line)) continue; 
                   
                   File file = new File(new java.net.URI(line));
                   list.add(file);
               } catch (Exception ex) {
                   ex.printStackTrace();
               }
           }

           return list;
       } catch (IOException ex) {
           ex.printStackTrace();
       }
       return list;
    }

	public boolean canImport(JComponent p_jc, DataFlavor[] p_flavors)
	{
		if (hasFileFlavor(p_flavors))
		{
			return true;
		}
		return false;
	}

	/**
	 * Desktop Icon handles one file only
	 * @param p_jc the JComponent which setTransferHanlder(FileTransferHandler.install())
	 * @param p_files
	 */
	private void displayFiles(JComponent p_jc, List<File> p_files)
	{
		try
		{
			if (p_files.size() > 0)
			{
			    File[] obs = new File[p_files.size()];
			    for (int i = 0; i < p_files.size(); i++)
			    {
			        obs[i] = p_files.get(i);
			    }
				SwingHelper.getMainFrame().addFiles(obs);
			}
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
		}
	}

	private boolean hasFileFlavor(DataFlavor[] p_flavors)
	{
		for (int i = 0; i < p_flavors.length; i++)
		{
			if (UsefulTools.isLinux())
			{
			    if (uriListFlavor.equals(p_flavors[i]))
			    {
			        return true;
			    }
			}
			else
			{
			    if (fileFlavor.equals(p_flavors[i]))
	            {
	                return true;
	            }
			}
		}
		return false;

	}

	public List<File> getFiles()
	{
		return files;
	}

	public void setFiles(List<File> P_files)
	{
	    Set<String> names = new HashSet<String>();
	    for (File file : P_files)
	    {
	        names.add(file.getName());
	    }
	    
	    if (names.size() != P_files.size())
	    {
	        throw new IllegalArgumentException(Constants.MSG_FILE_NAME_REPEAT);
	    }
	    
		this.files = P_files;
	}
}
