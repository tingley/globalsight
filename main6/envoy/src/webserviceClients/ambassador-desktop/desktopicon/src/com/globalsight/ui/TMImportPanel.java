package com.globalsight.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.globalsight.action.TMImportAction;
import com.globalsight.action.QueryAction;
import com.globalsight.entity.FileProfile;
import com.globalsight.entity.TM;
import com.globalsight.entity.User;
import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.Constants;
import com.globalsight.util.SortHelper;
import com.globalsight.util.StringHelper;
import com.globalsight.util.SwingHelper;
import com.globalsight.util.zip.ZipIt;
import com.globalsight.util2.CacheUtil;
import com.globalsight.util2.ConfigureHelperV2;

public class TMImportPanel extends JPanel
{
    private static final long serialVersionUID = -3712155455260994094L;

	static Logger logger = Logger.getLogger(TMImportPanel.class.getName());
	
	private static String labelTMName 		= "TM Name :";
	private static String labelFiles 		= "File for Import :";
	private static String labelSynOption 	= "Synchronization options :";
	private static String msgTMName 		= "Please Select a TM for Import";
	private static String importButtonName 	= "Import";
	private static String cancelButtonName 	= "Cancel";
	private static String addButtonName 	= "Add File(s)";
	private static String delButtonName 	= "Remove File(s)";
	private static String TM_SYN_MERGE 		= "Merge TUs with existing TUs"; 
	private static String TM_SYN_OVERWRITE 	= "Overwrite existing concepts";
	private static String TM_SYN_DISCARD 	= "Discard TUs"; 
	private static TMImportAction tmImportAction = new TMImportAction();
	public static final String SYNC_MERGE 		= "merge";
	public static final String SYNC_OVERWRITE 	= "overwrite";
    public static final String SYNC_DISCARD 	= "discard";
        
    private JList m_filesList;
    private JComboBox jComboBoxTMName, jComboBoxSynOption;
    private DefaultListModel m_filesModel;
    private JButton createButton, cancelButton, addFileButton, delFileButton;
 
    private JScrollPane jScrollPaneFiles;

    private boolean showIfAddSamefile = true;

    private boolean addSameFile = true;

    //private FileProfileThread fpThread = null;

    // ///////////////////////////////////////////////////////////////
    // constructor
    // ///////////////////////////////////////////////////////////////
    public TMImportPanel()
    {
        this(Constants.TM_IMPORT_TITLE);
    }
    
    public TMImportPanel(String panelName)
    {
    	super.setName(panelName);
    	m_filesModel = new DefaultListModel();
        initPanel();
        initActions();
    }

    // ///////////////////////////////////////////////////////////////
    // public methods
    // ///////////////////////////////////////////////////////////////
    public void addAllFiles(File[] p_files)
    {
        if (p_files != null)
        {
            for (int i = 0; i < p_files.length; i++)
            {
                addFile(p_files[i]);
            }
        }

        if (m_filesModel.size() == 1)
        {
            m_filesList.setSelectedIndex(0);
        }
        /*else
        {
            if (m_filesList.getSelectedIndex() == -1)
            {
                jComboBoxTMName.removeAllItems();
                jComboBoxTMName.addItem(Constants.PLEASE_SELECT_FILE);
            }
        }*/

        //setMaxFileNumAndMaxJobSize();
    }

    public void delFile(int p_index)
    {
        if (p_index != -1)
        {
            m_filesModel.remove(p_index);
        }
    }

    // Delete them from decreasing order for avoid ArrayIndexOutOfBoundsException
    public void delFile(int[] p_indices)
    {
        int len = p_indices.length;
        for (int i = 1; i <= len; i++)
        {
            delFile(p_indices[len - i]);
        }
    }

    /**
     * Add blank to String
     * 
     * @param p_str			the string
     * @param p_blankNum	the blank number 		
     * @param p_isPref		the position of added blank(true is prefix, false is end)
     */
    public String addBlank(String p_str, int p_blankNum, boolean p_isPref){
		String blankStr = "";
		for(int i=0;i<p_blankNum;i++){
			blankStr = blankStr + " ";
		}		
		if(p_isPref){
			p_str = blankStr+p_str;
		}else{
			p_str = p_str+blankStr;
		}
		return p_str;
    }
    
    // ///////////////////////////////////////////////////////////////
    // private methods
    // ///////////////////////////////////////////////////////////////
    private void initPanel()
    {
        int panelWidth  = SwingHelper.getMainFrame().getWidth() / 3 - 10;
        //int panelHeight = SwingHelper.getMainFrame().getHeight() / 2 - 10;
        Container contentPane = this;
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints c1 = new GridBagConstraints();
        //c1.fill = GridBagConstraints.VERTICAL;
        c1.insets = new Insets(1, 2, 2, 2);
        //c1.weightx = 1;
        c1.weighty = 2;
        c1.anchor = GridBagConstraints.WEST;
        
        int blankNum = 32;
        boolean isPrefix = false;
        labelTMName 	= addBlank(labelTMName, 	blankNum, isPrefix);
        labelFiles 		= addBlank(labelFiles, 		blankNum, isPrefix);
        labelSynOption 	= addBlank(labelSynOption, 	blankNum, isPrefix);

        //TM Name for import
        JLabel label_TM_Name = new JLabel(labelTMName, Label.RIGHT);
        c1.gridx = 0;
        c1.gridy = 0;
        c1.gridwidth = 1;
        c1.gridheight = 1;        
        contentPane.add(label_TM_Name, c1);
        //Add TM Names in select list
        jComboBoxTMName = new JComboBox();
        jComboBoxTMName.addItem(msgTMName);
        List<TM> tmList = null;
        try 
        {
			tmList = tmImportAction.getAllProjectTMsByList();
		} 
        catch (Exception e) 
        {
			logger.info("Getting TM List error:",e);
		}
        
        if(null!=tmList && tmList.size()>0)
        {
        	for(int i=0;i<tmList.size();i++)
        	{
        		jComboBoxTMName.addItem(tmList.get(i).getName());
        	}
        }
        jComboBoxTMName.setPreferredSize(new Dimension(220, 30));
        JScrollPane scrollPaneTMName = new JScrollPane(jComboBoxTMName);
        scrollPaneTMName.setMinimumSize(new Dimension(250, 50));//220,50
        scrollPaneTMName.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPaneTMName.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        c1.gridx = 1;
        c1.gridy = 0;
        c1.gridwidth = 2;
        c1.gridheight = 1;
        contentPane.add(scrollPaneTMName, c1);
        
        //Add files for Import
        JLabel label_files = new JLabel(labelFiles, Label.RIGHT);
        c1.gridx = 0;
        c1.gridy = 3;
        c1.gridwidth = 1;
        c1.gridheight = 1;
        contentPane.add(label_files, c1);
        
        
        //Added files        
        m_filesList = new JList(m_filesModel);
        //m_filesList.setAutoscrolls(true);
        m_filesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        m_filesList.setTransferHandler(FileTransferHandler.install());
        jScrollPaneFiles = new JScrollPane(m_filesList);
        jScrollPaneFiles.setMinimumSize(new Dimension(panelWidth, 120));//100
        jScrollPaneFiles.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        //"Add File(s)" and "Remove File(s)" button
        addFileButton = new JButton(addButtonName);
        addFileButton.setToolTipText(addButtonName);
        delFileButton = new JButton(delButtonName);
        delFileButton.setToolTipText(delButtonName);
        
        JPanel fileButtonPanel = new JPanel();
        fileButtonPanel.add(addFileButton);
        fileButtonPanel.add(delFileButton);
        JSplitPane filesPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,jScrollPaneFiles,fileButtonPanel);
        filesPane.setDividerSize(0);
        filesPane.setBorder(null);
        filesPane.setPreferredSize(new Dimension(panelWidth+10, 200));
        c1.gridx = 1;
        c1.gridy = 3;
        c1.gridwidth = 1;
        c1.gridheight = 1;
        contentPane.add(filesPane,c1);
        

        //Synchronization options
        JLabel label_synOptions_Name = new JLabel(labelSynOption, Label.RIGHT);
        c1.gridx = 0;
        c1.gridy = 8;
        c1.gridwidth = 1;
        c1.gridheight = 1;        
        contentPane.add(label_synOptions_Name, c1);
        
        jComboBoxSynOption = new JComboBox();
        jComboBoxSynOption.addItem(TM_SYN_MERGE);
        jComboBoxSynOption.addItem(TM_SYN_OVERWRITE);
        jComboBoxSynOption.addItem(TM_SYN_DISCARD);
        jComboBoxSynOption.setPreferredSize(new Dimension(220, 30));
        JScrollPane scrollPanesynOptions = new JScrollPane(jComboBoxSynOption);
        scrollPanesynOptions.setMinimumSize(new Dimension(250, 50));//220,50
        scrollPanesynOptions.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPanesynOptions.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        c1.gridx = 1;
        c1.gridy = 8;
        c1.gridwidth = 2;
        c1.gridheight = 1;
        contentPane.add(scrollPanesynOptions, c1);
        
        
        //set "create" and "cancel" buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        createButton = new JButton(importButtonName);
        buttonPanel.add(createButton);
        cancelButton = new JButton(cancelButtonName);
        buttonPanel.add(cancelButton);
        
        c1.gridx = 1;
        c1.gridy = 15;
        c1.gridwidth = 3;
        c1.gridheight = 1;
        contentPane.add(buttonPanel, c1);
    }

    private void initActions()
    {
        addFileButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                File[] files = filesChooserAction();
                addAllFiles(files);
            }
        });

        delFileButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int[] indices = m_filesList.getSelectedIndices();
                delFile(indices);
            }
        });
       
        createButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                importTM();
            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cleanPanel();
            }
        });
    }

    private void importTM()
    {
    	if (canCreate())
        {
//    		print(m_filesModel);
            
            String[] files = getStringArrFromDefaultListModel(m_filesModel); 
            String tmName = jComboBoxTMName.getSelectedItem().toString();
            String syncMode = getSyncMode(jComboBoxSynOption.getSelectedItem().toString());
            Map importResult = null;			
            
            try 
            {
            	importResult = tmImportAction.uploadFile(files, tmName, syncMode);
    		} 
            catch (Exception e) 
            {
    			logger.error("There is some error when upload", e);
    		}
            
    		try 
    		{
    			String succ = TMImportAction.TM_IMPORT_STATUS_SUCC;
    			String importStatus = (String) importResult.get(TMImportAction.TM_IMPORT_STATUS);
    			if(succ.equals(importStatus))
    			{
    				tmImportAction.importTmxFile(tmName, syncMode);
    				print(tmName+"\t"+syncMode);
    				AmbOptionPane.showMessageDialog(Constants.TM_IMPORT_SUSS,
    	                    "TM Import", JOptionPane.INFORMATION_MESSAGE);
    			}
    			else
    			{
    				String mess = Constants.TM_IMPORT_FAIL + importResult.get(TMImportAction.TM_IMPORT_FAIL_FILES).toString();
    				logger.error(mess);
    				AmbOptionPane.showMessageDialog(Constants.TM_IMPORT_FAIL,
    	                    "TM Import", JOptionPane.ERROR_MESSAGE);
    			}
    		} 
    		catch (Exception e) 
    		{
    			logger.error("There is some error when import", e);
    		}
        }
    }
    
    private boolean canCreate()
    {
        // check if there is someone logon
        if (CacheUtil.getInstance().getCurrentUser() == null)
        {
            AmbOptionPane.showMessageDialog(Constants.ERROR_NOT_LOGON,
                    "Warning", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // check if select the TM Name
        if(msgTMName.equals(jComboBoxTMName.getSelectedItem().toString()))
        {
        	AmbOptionPane.showMessageDialog(Constants.TM_CHECK_TMNAME,
                    "Warning", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // check if added the file
        if(m_filesModel.size()==0)
        {
        	AmbOptionPane.showMessageDialog(Constants.TM_CHECK_FILES_EMPTY,
                    "Warning", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        else
        {
        	String[] files = getStringArrFromDefaultListModel(m_filesModel); 
        	String result = isFileType(files);
        	if(result.length()>0)
        	{
        		AmbOptionPane.showMessageDialog(result, "Warning", JOptionPane.ERROR_MESSAGE);
                return false;
        	}
        }
        
        return true;
    }
    
    private void cleanPanel()
    {
    	jComboBoxTMName.setSelectedIndex(0);
    	m_filesModel.removeAllElements();
    	m_filesList.removeAll();
    	jComboBoxSynOption.setSelectedIndex(0);
    }
        
	public String[] getStringArrFromDefaultListModel(DefaultListModel list) 
	{
		String temp = list.toString();

		if (temp.startsWith("[")) {
			temp = temp.substring(1);
		}
		if (temp.endsWith("]")) {
			temp = temp.substring(0, temp.length() - 1);
		}

		String[] result = temp.split(",");
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i].trim();
		}
		return result;
	}
        
	// Get the real synMode from display name of synMode
	public String getSyncMode(String syncModeDisplayName) {
		String result;

		if (TM_SYN_OVERWRITE.equalsIgnoreCase(syncModeDisplayName)) {
			result = SYNC_OVERWRITE;
		} else if (TM_SYN_DISCARD.equalsIgnoreCase(syncModeDisplayName)) {
			result = SYNC_DISCARD;
		} else {
			result = SYNC_MERGE;
		}

		return result;
	}

	/* */
	public static void print(Object obj)
	{ 
		boolean isPrint = true;
		if(isPrint)
		{
			System.out.println("**********"+obj.toString()+"**********"); 
		} 
	}
	
        
    private File[] filesChooserAction()
    {
        JFileChooser fileChooser = new JFileChooser(ConfigureHelper.getBaseFolder());
        fileChooser.setDialogTitle("Select The File(s)");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        int status = fileChooser.showOpenDialog(SwingHelper.getMainFrame());
        if (status == JFileChooser.APPROVE_OPTION)
        {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if (selectedFiles != null && selectedFiles.length > 0)
            {
                ConfigureHelper.setBaseFolder(selectedFiles[0].getParent());
            }
            return selectedFiles;
        }
        else
        {
            return null;
        }
    }

    private void addFile(File p_file)
    {
        //String tmpPath = "";
        if (p_file != null && p_file.exists())
        {
            if (p_file.isDirectory())
            {
                File[] files = p_file.listFiles();
                addAllFiles(files);
            }
            else if (isZipFile(p_file))
            {
                try
                {
                    String dir = ConfigureHelperV2.readDefaultSavepath();
                    if (dir == null)
                    {
                        dir = new File(".").getAbsolutePath();
                    }
                    String temp_zip_folder = dir + File.separator + "temp_zip"
                            + File.separator + p_file.getName() + "_"
                            + System.currentTimeMillis();
                    List files = ZipIt.unpackZipPackage(p_file
                            .getAbsolutePath(), temp_zip_folder);
                    
                    
                    File[] fileArray = new File[files.size()];
                    for (int i = 0; i < fileArray.length; i++)
                    {
                        fileArray[i] = new File(temp_zip_folder, (String) files.get(i));
                    }

                    addAllFiles(fileArray);
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                    AmbOptionPane.showMessageDialog("Unzip " + p_file
                            + " error!", "Warning", JOptionPane.ERROR_MESSAGE);
                }
            }
            else
            {
                boolean isEmptyFile = false;
                boolean isSame = false;
                boolean add = true;
                // ignore file size == 0
                if (p_file.length() <= 0)
                {
                    isEmptyFile = true;
                }// check if file is already in file list
                else if (ConfigureHelper.checkAddSameFile())
                {
                    for (int i = 0; i < m_filesModel.size(); i++)
                    {
                        File file = (File) m_filesModel.get(i);
                        if (file.getAbsolutePath().equals(
                                p_file.getAbsolutePath()))
                        {
                            isSame = true;
                            break;
                        }
                    }
                    if (isSame && showIfAddSamefile)
                    {
                        int value = AmbOptionPane.showConfirmDialog(
                                Constants.ADD_SAME_FILE + "\n"
                                        + p_file.getAbsolutePath(),
                                "Add same file?", JOptionPane.YES_NO_OPTION);
                        if (JOptionPane.NO_OPTION == value)
                        {
                            addSameFile = false;
                        }
                        else
                        {
                            addSameFile = true;
                        }
                        /**
                         * configure "resource/configure/configure.xml"
                         * <checkaddsamefiletimes>s</checkaddsamefiletimes> to
                         * show option pane just one time in a job or
                         * <checkaddsamefiletimes>m</checkaddsamefiletimes> to
                         * show option pane each time when add same file
                         */
                        if (ConfigureHelper.isCheckAddSameFileOnce())
                        {
                            showIfAddSamefile = false;
                        }
                    }
                }

                if (isEmptyFile)
                {
                    add = false;
                }
                else if (isSame && !addSameFile)
                {
                    add = false;
                }

                if (add)
                {
                    m_filesModel.addElement(p_file);
                    //m_fileProfilesModel.addElement(Constants.PLEASE_MAPPING);
                }
            }
        }
    }

    private boolean isZipFile(File p_file)
    {
        if (p_file.getPath().toLowerCase().endsWith(".zip"))
            return true;
        else
            return false;
    }
    
    public String isFileType(String[] files){
    	String result = "";
    	for(int i=0;i<files.length;i++)
    	{
    		if(!isFileType(files[i]))
    		{
    			if(result.length()==0){
    				result = Constants.TM_CHECK_FILE_TYPE;
    			}
    			result = result+files[i]+"\n";
    		}
    	}
    	
    	return result;
    }
    
    public boolean isFileType(String str){
    	String TMX = ".tmx";
    	str = str.toLowerCase();
    	if(str.endsWith(TMX))
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
}
