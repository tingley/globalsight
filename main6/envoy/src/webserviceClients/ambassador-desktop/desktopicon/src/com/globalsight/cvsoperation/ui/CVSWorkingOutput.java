package com.globalsight.cvsoperation.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.globalsight.cvsoperation.util.SwingWorker;
import com.globalsight.util.Constants;
import com.globalsight.util.SwingHelper;

public class CVSWorkingOutput extends JFrame//implements Runnable
{
	private static final long serialVersionUID = -3406259158806353834L;
	
	static Logger log = Logger.getLogger(CVSWorkingOutput.class.getName());

	private JLabel lbOutput, lbStatus;
	
	private JTextArea jta;
	
	private JPanel panel;
	
	private JButton jbBegin, jbAbort, jbClose;
	
	private JScrollPane scrollPane;
	
	private static PrintStream ps = null;
	
	private HashMap parameterMap = null;
	
	private	int exitValue = -1;
	
	private SwingWorker worker = null;
	
	public CVSWorkingOutput()
	{
		initPanel();
		
		addActions();
	}
	
	private void initPanel()
	{
		setTitle("CVS Working Output");
		setResizable(true);
		setMaximumSize(new Dimension(600,400));
		setPreferredSize(new Dimension(600,400));
		Point p = SwingHelper.getMainFrame().getLocation();
		setLocation(p.x + 115, p.y + 90);
		
		Container contentPane = getContentPane();

		lbOutput = new JLabel("Output");
		lbOutput.setMaximumSize(new Dimension(30, 20));
		lbOutput.setMinimumSize(new Dimension(30, 20));
		lbOutput.setPreferredSize(new Dimension(30, 20));
		lbOutput.setFont(new Font("Arial",Font.PLAIN,12));
		
		jta = new JTextArea();
		jta.setAutoscrolls(true);
		jta.setLineWrap(false);
		scrollPane = new JScrollPane();
		scrollPane.getViewport().add(jta, null);
		scrollPane.setAutoscrolls(true);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		panel = new JPanel();
		jbBegin = new JButton("Begin");
		jbBegin.setMinimumSize(new Dimension(70,20));
		jbBegin.setMaximumSize(new Dimension(70,20));
		jbBegin.setPreferredSize(new Dimension(70,20));
		jbBegin.setFont(new Font("Arial",Font.PLAIN,12));
		jbBegin.setEnabled(true);
		if ( jbBegin.getText().equalsIgnoreCase("begin") )
		{
			jbBegin.setToolTipText("Begin checkout or update from server");			
		}
		else
		{
			jbBegin.setToolTipText("Close current window");
		}
		panel.add(jbBegin);
		
		jbAbort = new JButton("Abort");
		jbAbort.setMinimumSize(new Dimension(70,20));
		jbAbort.setMaximumSize(new Dimension(70,20));
		jbAbort.setPreferredSize(new Dimension(70,20));
		jbAbort.setFont(new Font("Arial",Font.PLAIN,12));
		jbAbort.setEnabled(false);
		jbAbort.setToolTipText("Abort current operation");
		panel.add(jbAbort);
//		jbAbort.setVisible(false);
		
		jbClose = new JButton("Close");
		jbClose.setMinimumSize(new Dimension(70,20));
		jbClose.setMaximumSize(new Dimension(70,20));
		jbClose.setPreferredSize(new Dimension(70,20));
		jbClose.setFont(new Font("Arial",Font.PLAIN,12));
		jbClose.setToolTipText("Close current window");
		panel.add(jbClose);
		
		lbStatus = new JLabel("Status");
		lbStatus.setFont(new Font("Arial",Font.PLAIN,12));
		lbStatus.setVisible(false);
		panel.add(lbStatus);
		
		contentPane.add(lbOutput, BorderLayout.NORTH);
		contentPane.add(panel, BorderLayout.SOUTH);
		
    	ps = new PrintStream(System.out) 
    	{
    		public void println(String str) 
    		{
    			jta.append( str + "\n");
    		}
    	};
		
		pack();
		setVisible(true);
	}
	
    /**
     * This method represents the application code that is run on a separate thread.
     */
	private Object doWork()
	{
		String backInfo = "";
        try 
        {
    		if ( parameterMap != null )
    		{
    			String workDir = (String) parameterMap.get(Constants.CVS_WORK_DIRECTORY);
    			List cmdList = (List) parameterMap.get(Constants.CVS_COMMAND);
    			
    			if ( cmdList != null && cmdList.size() > 0 )
    			{
    				for (int i=0; i<cmdList.size(); i++)
    				{
    					String[] cmd = (String[]) cmdList.get(i);
    					
    					String cmdInStr = "";
    					for (int m=0; m<cmd.length; m++) {
    						cmdInStr += " " + cmd[m];
    					}
    					
    					if ( "".equals(backInfo.trim())) {
        					backInfo = backInfo + "Running command: " + cmdInStr;    						
    					} else {
    						backInfo = backInfo + "\nRunning command: " + cmdInStr;
    					}

    					if ( i==0 ) {
        					updateTAContents("Running command: " + cmdInStr);
    					} else {
    						updateTAContents("\nRunning command: " + cmdInStr);
    					}

    					backInfo += "\n" + exeCvsCmd(cmd, workDir);
    				}
    			}
    		}
        }
        catch (InterruptedException e) 
        {
        	backInfo += "\nWarning: Current operation is interrupted";  // SwingWorker.get() returns this
        }
        catch (IOException ioe)
        {
        	updateTAContents(ioe.getMessage());
        	backInfo += "\n" + ioe.getMessage();
        	log.error(ioe.getMessage(), ioe);
        }
        catch (Exception ex)
        {
        	updateTAContents(ex.getMessage());
        	backInfo += "\n" + ex.getMessage();
        }
        
        return backInfo;         // or this
	}
	
	private String exeCvsCmd(String[] cmd, String workDirectory) throws IOException, InterruptedException
	{
		String result = "";
		String line = "";
		BufferedReader in = null;
		
		try 
		{
			ProcessBuilder builder = new ProcessBuilder(cmd);
			// set working directory
			if ( workDirectory != null ) {
				builder.directory(new File(workDirectory));
			}
			//set error stream
			builder.redirectErrorStream(true);
			//start ProcessBuilder
			Process process = builder.start();

			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ( (line=in.readLine()) != null ) 
			{
				//check if 'worker' is interrupted
	       		if (Thread.interrupted()) {
	       			updateTAContents("\nWarning: Current operation is interrupted!");
					jbClose.setEnabled(true);
	       			throw new InterruptedException();
	       		}
	       		
				updateTAContents(line);
				if ( "".equals(result.trim()) ) {
					result = line;
				} else {
					result += "\n" + line;
				}
			}
			//get exitValue
			try
			{
				exitValue = process.waitFor();
				line = "\n***** CVS exited normally with code " + exitValue + " *****\n\n";
			}
			catch (Exception e)
			{
				line = "\n***** CVS exited failure *****\n" + e.getMessage();
				log.error(e.getMessage(), e);
			}
			result += "\n" + line;
			updateTAContents(line);
			
		}
		catch (InterruptedException e) 
		{
			log.error(e.getMessage(), e);
			throw new InterruptedException(e.getMessage());
		}
		finally 
		{
            if (in != null)
            {
            	in.close();
            }
        }
		
		return result;
	}
	
    /**
     * When the worker needs to update the GUI, start a Runnable
     * for the event dispatching thread with SwingUtilities.invokeLater().
     */
    private void updateTAContents(final String info) 
    {
        Runnable refreshTAContents = new Runnable() {
            public void run() 
            {
            	jta.append(info + "\n\n");
				jta.setCaretPosition(jta.getDocument().getLength());
				jta.paintImmediately(jta.getBounds());
            }
        };
        
        SwingUtilities.invokeLater(refreshTAContents);
    }

	
	private void addActions()
	{
		jbBegin.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jbBegin.setEnabled(false);
				jbAbort.setEnabled(true);
				jbClose.setEnabled(false);

	            /* Invoking start() on the SwingWorker causes a new Thread
	             * to be created that will call construct(), and then
	             * finished().  Note that finished() is called even if
	             * the worker is interrupted because we catch the
	             * InterruptedException in doWork().
	             */
	            worker = new SwingWorker() 
	            {
	                public Object construct() 
	                {
	                    return doWork();
	                }
	                
	                public void finished() 
	                {
	    				jbBegin.setEnabled(false);
	    				jbAbort.setEnabled(false);
	    				jbClose.setEnabled(true);
	                }
	            };
	            worker.start();

			}
		});
		
		/**
		 * Interrupt the thread of 'worker'.
		 */
		jbAbort.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				worker.interrupt();
				jbBegin.setEnabled(false);
				jbAbort.setEnabled(false);
				jbClose.setEnabled(true);
			}
		});
		
		jbClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				CVSWorkingOutput.this.dispose();
			}
		});
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}
	
    public PrintStream getPs() 
    {
    	return ps;
    }
	
    public void setParameters(HashMap parameterMap)
    {
    	this.parameterMap = parameterMap;
    }
    
	public HashMap getParameterMap()
	{
		return this.parameterMap;
	}
	
	public JTextArea getJTextArea()
	{
		return this.jta;
	}
	
}
