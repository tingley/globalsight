package com.globalsight.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.globalsight.util2.ConfigureHelperV2;

public class PreferencesDialog extends JDialog
{
	private static final long serialVersionUID = 1411149034385532534L;

	static Logger log = Logger.getLogger(PreferencesDialog.class.getName());

	private static String font_name= "Default";//"Arial"
	private static int  font_style = Font.BOLD;
	private static int  font_size  = 13;
	public  static Font font = new Font(font_name,font_style,font_size);
	private JList menuList;
	private Box centerContainer, box_job_split;
	private JCheckBox jobSplitCheck;
	private JCheckBox jobPriorityCheck;
	private JButton jButtonOK, jButtonCancel;
	
	private static String[] m_menus           = {"   Create Job   "};
	private static String lableNameJobSplit   = "Enable Job Splitting";
	private static String lableNameJobPriority = "Enable Job Priority";
	private static String buttonNameOK        = "  OK  ";
	private static String buttonNameCancel    = "Cancel";
	private static String successSaveMessage  = "Save Preferences successfully";
	private static String failSaveMessage     = "Logon successfully, but save Preferences unsuccessfully. " +
			                                    "\nPlease view log";
	private static int pDialogWidth=400,pDialogHeight=260;
	
	public PreferencesDialog(Frame p_owner)
	{
		super(p_owner, "Preferences ");
		this.setResizable(false);
		
		initPanel();                   //print panel		
		setSizeAndLocation(p_owner);   //set size and location		
		action();                      //add listener
		setFields();                   //set value
		
		menuList.setSelectedIndex(0);  //select first menu item		
	}
	
	private void initPanel()
	{
		Container cPane = this.getContentPane();
		cPane.setLayout(new BorderLayout());
		

		// West: menu list
		Box west = Box.createVerticalBox();
		west.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		cPane.add(west, BorderLayout.WEST);
		west.add(Box.createVerticalStrut(20));
		menuList = new JList(m_menus);
		Color c = cPane.getBackground();
		menuList.setBackground(c);
		menuList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		menuList.putClientProperty("JTree.lineStyle", "Horizontal");
		JScrollPane treeView = new JScrollPane(menuList);
		west.add(treeView);
		
		
		// Center: 
		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());
		cPane.add(center, BorderLayout.CENTER);

		centerContainer = Box.createVerticalBox();
		center.add(centerContainer, BorderLayout.CENTER);
		centerContainer.setBorder(BorderFactory.createEtchedBorder());
		
		// A part of details: create job option 
		box_job_split = Box.createVerticalBox();
		jobSplitCheck = new JCheckBox();
		jobSplitCheck.setFont(font);
		jobSplitCheck.setText("  "+lableNameJobSplit);
		
		jobPriorityCheck= new JCheckBox();
        jobPriorityCheck.setFont(font);
        jobPriorityCheck.setText("  "+lableNameJobPriority);
        
		box_job_split.add(jobSplitCheck, BorderLayout.PAGE_START);
		box_job_split.add(jobPriorityCheck, BorderLayout.PAGE_START);
		

		// buttons
		jButtonOK     = new JButton(buttonNameOK);
		jButtonCancel = new JButton(buttonNameCancel);
		int width1=300;
		int width2=20;
		Box box_buttons = Box.createHorizontalBox();
		box_buttons.add(Box.createHorizontalStrut(pDialogWidth-width1-width2));
		box_buttons.add(jButtonOK);
		box_buttons.add(Box.createHorizontalStrut(width2));
		box_buttons.add(jButtonCancel);	
		box_buttons.add(Box.createVerticalStrut(40));
		
		center.add(box_buttons, BorderLayout.SOUTH);
	}

	private void action()
	{
		// menu tree action
		menuList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				Object obj = menuList.getSelectedValue();
				if (m_menus[0].equals(obj.toString()))
				{
					showUserInfo();
				}
				/*else if (m_menus[1].equals(obj.toString()))
				{
					showDownloadInfo();
				}*/
			}
		});
		
		jButtonOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					String strJobSplit = getPrefShowJobSplit();
					ConfigureHelperV2.writePrefJobSplitting(strJobSplit);
					String strJobPriority = getPrefShowJobPriority();
					ConfigureHelperV2.writePrefJobPriority(strJobPriority);
					setFields();
					AmbOptionPane.showMessageDialog(successSaveMessage,"Success", JOptionPane.INFORMATION_MESSAGE);
					setVisible(false);
				}
				catch (Exception ex)
				{
					log.error("Error when save Preferences : " + ex);
					AmbOptionPane.showMessageDialog(failSaveMessage,
							"Success", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		jButtonCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
	}
	
    private void setFields()
    {
        boolean isShowJobSplit = false;
        boolean isShowJobPriority = false;
        try
        {
            isShowJobSplit = ConfigureHelperV2.readPrefJobSplitting();
            isShowJobPriority = ConfigureHelperV2.readPrefJobPriority();
        }
        catch (Exception e)
        {
            log.error("Error in getting  Job Splitting or Job Priority: " + e);
        }
        jobSplitCheck.setSelected(isShowJobSplit);
        jobPriorityCheck.setSelected(isShowJobPriority);

    }
	
	private void showUserInfo()
	{
		centerContainer.removeAll();
		centerContainer.add(Box.createVerticalStrut(20));
		centerContainer.add(box_job_split);
		
		centerContainer.repaint();
		centerContainer.validate();
	}
	
	private String getPrefShowJobSplit()
	{
		String isShowJobSplit = "false";
		if(jobSplitCheck.isSelected())
		{
			isShowJobSplit = "true";
		}
		System.out.println(isShowJobSplit);
		
		return isShowJobSplit;
	}
	
	private String getPrefShowJobPriority()
    {
        String isShowJobPriority = "false";
        if(jobPriorityCheck.isSelected())
        {
            isShowJobPriority = "true";
        }
        System.out.println(isShowJobPriority);
        
        return isShowJobPriority;
    }
	
	private void setSizeAndLocation(Frame p_owner)
	{
		setSize(new Dimension(pDialogWidth,pDialogHeight));
		Point p = p_owner.getLocation();
		int x = (int) p.getX() + (p_owner.getWidth() - this.getWidth()) / 2;
		int y = (int) p.getY() + (p_owner.getHeight() - this.getHeight()) / 2;
		setLocation(x , y);
		
		//System.out.println(x+":"+p.getX()+","+p_owner.getWidth()+","+this.getWidth());
	}
}
