package com.globalsight.ui;

import javax.swing.JOptionPane;

import com.globalsight.entity.Job;
import com.globalsight.util.SwingHelper;

public class AmbOptionPane 
{
	public static void showMessageDialog(Object p_msg, String p_title, int p_msgType)
	{
		JOptionPane.showMessageDialog(SwingHelper.getMainFrame(), p_msg,
				p_title, p_msgType, null);
	}
	
	public static void showMessageDialog(Object p_msg, String p_title)
	{
		showMessageDialog(p_msg, p_title, JOptionPane.INFORMATION_MESSAGE);
	}
	/**
	 * 
	 * @param p_msg the Object to display
	 * @param p_title the title string for the dialog
	 * @param p_optionType an int designating the options available on the dialog:
     *                  <code>YES_NO_OPTION</code>,
     *			or <code>YES_NO_CANCEL_OPTION</code>
	 * @return an int indicating the option selected by the user
	 */
	public static int showConfirmDialog(Object p_msg, String p_title, int p_optionType)
	{
		return JOptionPane.showConfirmDialog(SwingHelper.getMainFrame(),
				p_msg, p_title, p_optionType, JOptionPane.QUESTION_MESSAGE,
				null);
	}
}
