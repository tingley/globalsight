package com.util;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.JList;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

public class HelpUtil {
	private static Logger log = Logger.getLogger(HelpUtil.class);
	
	private HelpSet masterHS; // the HelpSet on which to integrate
	private URL masterURL; // the base URL to the this HelpSet
	private ClassLoader myLoader; // our ClassLoader
	private JList hsList; // current HelpSets as List
	 // Main HelpSet & Broker
    private HelpSet mainHS = null;
    private HelpBroker mainHB;
    
    
    public HelpUtil()
    {
    	initializeMasterHS();
    }
    
	/*
	 * Initialize the HelpSets
	 */
	private void initializeMasterHS() {
		
		myLoader = getMyLoader();
		masterURL = HelpSet.findHelpSet(myLoader, "help/Master");
		try {
			masterHS = new HelpSet(myLoader, masterURL);
			mainHS = new HelpSet(myLoader, masterURL);
			mainHB = mainHS.createHelpBroker();
			
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			Point location = new Point((screen.width - 600) / 2,
					(screen.height - 600) / 2 - 20);
			mainHB.setLocation(location);
		} catch (Exception ex) {
			log.error(ex);
			log.error("Could not create the master HelpSet");
		}
	}

	/*
	 * Get the ClassLoader of this class. The spec of getClassLoader() has
	 * changed recently; verify what is the current specification..
	 */
	private ClassLoader getMyLoader() {
		ClassLoader back;
		back = this.getClass().getClassLoader();
		return back;
	}
	
	public void addHelp(JMenuItem menuHelp)
	{
		if (mainHB != null)
		{
			CSH.setHelpIDString(menuHelp,"main");
			menuHelp.addActionListener(new CSH.DisplayHelpFromSource(mainHB));
		}
	}
}
