package com.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class AboutDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	JTextPane txtpnGlobalsightPatchInstaller = new JTextPane();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			AboutDialog dialog = new AboutDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public AboutDialog() {
		setFont(new Font("Corbel", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage(AboutDialog.class.getResource("/gif/GlobalSight_Icon.jpg")));
		setTitle("About GlobalSight Patch Installer");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			txtpnGlobalsightPatchInstaller.setFont(new Font("Cambria", Font.PLAIN, 12));
			txtpnGlobalsightPatchInstaller.setContentType("text/html");
			txtpnGlobalsightPatchInstaller.setEditable(false);
			txtpnGlobalsightPatchInstaller.setBackground(contentPanel.getBackground());
			String content = "<body>\r\nGlobalSight Patch Installer for managing patches on GlobalSight server.<br><br>\r\nVersion: 8.6.2<br>\u00A91998-year, Welocalize Inc\r\n<br><br>\r\nYou can get more information about GlobalSight from<br>\r\n<a href=\"http://www.GlobalSight.com\">www.GlobalSight.com</a>\r\n</body>";
			Calendar cal=Calendar.getInstance();
			int year=cal.get(Calendar.YEAR);
			content = content.replace("year", year + "");
			txtpnGlobalsightPatchInstaller.setText(content);
			contentPanel.add(txtpnGlobalsightPatchInstaller);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("    OK    ");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		
		addLink();
		setLocationRelativeTo(null);
	}

	public void addLink()
	{
		HyperlinkListener hlLsnr = new HyperlinkListener(){  
			  
	        @Override  
	        public void hyperlinkUpdate(HyperlinkEvent e) {  
	             if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)   
	                 return;  
	             URL linkUrl = e.getURL();  
	             if (linkUrl!=null){  
	                 try {  
	                    Desktop.getDesktop().browse(linkUrl.toURI());  
	                } catch (Exception e1) {  
	                    e1.printStackTrace();  

	                }  
	             } 
	        }  
	    }; 
	    
	    txtpnGlobalsightPatchInstaller.addHyperlinkListener(hlLsnr);
	}
}
