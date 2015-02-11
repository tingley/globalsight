package com.globalsight.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import com.globalsight.util.Constants;
import com.globalsight.util.ValidationHelper;

/**
 * <p>
 * <code>ProxyDialog</code> is a dialog that used to set proxy. It will be
 * show when user click the <code>Option</code>-><code>Proxy</code> link
 * or push down <code>'P'</code>.
 * 
 * <p>
 * This class will read the properties from the <code>proxy.properties</code>,
 * if the file is not exist, will create a new file. After user input enough
 * information and click the <code>Submit</code> button or push down
 * <code>'Enter'</code>, This class will write the information to
 * <code>proxy.properties</code>. and system will set proxy next time
 * according to it.
 */
public class ProxyDialog extends JDialog
{
	private static final long serialVersionUID = 2676340813087938006L;
	static Logger log = Logger.getLogger(ProxyDialog.class.getName());
	
	private JPanel jpanel;
	private JButton submit;
	private JButton test;
	private JTextField ipText;
	private JTextField portText;
	private JCheckBox useProxyBox;
	private Color noticeColor = new Color(200, 255, 200);
	private Color oriColor = null;

	static Properties properties = new Properties();
	private boolean useProxy = false;
	private String proxyIp = "";
	private String proxyPort = "";
	
	private static final String USE_PROXY = "USE_PROXY";
	private static final String HOST = "HOST";
	private static final String PORT = "PORT";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	
	private String oldIp = null;
	private String oldPort = null;
	private String oldUseProxy;

	public ProxyDialog(Frame p_owner)
	{
		super(p_owner, "Settings");
		this.setResizable(false);
		this.setSize(400, 260);
		init();
		addListeners();
	}

	/**
	 * Reads proxy properties from <code>proxy.properties</code> and init UI.
	 */
	private void init()
	{
		readProxy();
		
		Container contentPane = this.getContentPane();
		contentPane.setLayout(null);

		jpanel = new JPanel();
		jpanel.setLayout(null);
		jpanel.setSize(300, 125);
		jpanel.setLocation(50, 40);
		jpanel.setBorder(new TitledBorder("Set Proxy"));

		JLabel ipLabel = new JLabel("Host:");
		ipLabel.setLocation(50, 30);
		ipLabel.setSize(50, 20);
		jpanel.add(ipLabel);
		ipText = new JTextField();
		ipText.setLocation(100, 30);
		ipText.setSize(150, 20);
		ipText.setText(proxyIp);		
		oriColor = ipText.getBackground();
		jpanel.add(ipText);

		JLabel portLabel = new JLabel("Port:");
		portLabel.setLocation(50, 70);
		portLabel.setSize(50, 20);
		jpanel.add(portLabel);
		portText = new JTextField();
		portText.setSize(150, 20);
		portText.setLocation(100, 70);
		portText.setText(proxyPort);
		jpanel.add(portText);

		useProxyBox = new JCheckBox("Use Proxy");
		useProxyBox.setSelected(useProxy);
		useProxyBox.setSize(100, 15);
		useProxyBox.setLocation(180, 100);
		jpanel.add(useProxyBox);
		contentPane.add(jpanel);

		submit = new JButton("Submit");
		submit.setLocation(240, 180);
		submit.setSize(80, 25);
		submit.addActionListener(getSubmitListener());	
		submit.setEnabled(false);
		contentPane.add(submit);

		test = new JButton("Test");
		test.setLocation(90, 180);
		test.setSize(80, 25);
		test.setRequestFocusEnabled(false);
		test.addActionListener(gettestListener());
		contentPane.add(test);
		
		// Init states
		boolean useProxy = useProxyBox.isSelected();
		ipText.setEnabled(useProxy);
		portText.setEnabled(useProxy);
		test.setEnabled(useProxy);
		
		oldIp = ipText.getText();
		oldPort = portText.getText();
		oldUseProxy = String.valueOf(useProxyBox.isSelected());
	}

	/**
	 * <p>
	 * Reads proxy properties from <code>proxy.properties</code>.
	 * 
	 * <p>
	 * If file is not exist or some exceptions are throwed, do nothing.
	 */
	private void readProxy()
	{
		File file = new File(Constants.CONFIGURE_XML_PROXY);

		if (!file.exists())
		{
			return;
		}
		
		try
		{
			FileInputStream fis  = new FileInputStream(file);
			properties.load(fis);
			fis.close();
			useProxy = TRUE.equals(properties.getProperty("USE_PROXY").trim());
			proxyIp = properties.getProperty("HOST");
			proxyPort = properties.getProperty("PORT");			
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>
	 * Stores the information about the proxy to <code>proxy.properties</code>.
	 * 
	 * <p>
	 * If the file is not exist, create a new one.
	 * 
	 * <p>
	 * The following is the stored information format.
	 * <ul>
	 * <li>USE_PROXY=true</li>
	 * <li>HOST=60.28.30.40</li>
	 * <li>PORT=80</li>
	 * </ul>
	 */
	private void storeProxy()
	{		
		File file = new File(Constants.CONFIGURE_XML_PROXY);
		
		try
		{
			if (!file.exists()) 
			{
		
				file.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(file);
			properties.store(out, "Set proxy");
			out.close();		
			
			AmbOptionPane.showMessageDialog(
					"New configuration applied. Please restart Desktop Icon.",
					"Set Proxy", JOptionPane.INFORMATION_MESSAGE);
		}
		catch (Exception e)
		{
			AmbOptionPane.showMessageDialog(
					e.toString(),
					"Set Proxy", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * If click test button, test the host and port can be used or not.
	 * 
	 * @return
	 */
	private ActionListener gettestListener()
	{
		return new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String host = ipText.getText();
				String port = portText.getText();
				if (useProxyBox.isSelected())
				{
					if (checkFields())
					{
						String msg;
						int msgType;
						
						if (testProxy(host, port))
						{
							msg = "Testing connection successfully";
							msgType = JOptionPane.INFORMATION_MESSAGE;							
						}
						else
						{
							msg = "Testing connection failed";
							msgType = JOptionPane.ERROR_MESSAGE;	
						}
						
						AmbOptionPane.showMessageDialog(msg, "Test Proxy", msgType);
					}
				}
			}
		};
	}

	private ActionListener getSubmitListener()
	{
		return new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				submit();
			}
		};
	}

	/**
	 * This method is store proxy informations and close the dialog.
	 */
	private void submit()
	{
		if (useProxyBox.isSelected())
		{
			if (checkFields())
			{
				String ip = ipText.getText().trim();
				String port = portText.getText().trim();
				
				properties.setProperty(USE_PROXY, TRUE);
				properties.setProperty(HOST, ip);
				properties.setProperty(PORT, port);

				storeProxy();
				this.setVisible(false);
			}
		}
		else
		{		
			properties.setProperty(USE_PROXY, FALSE);
			storeProxy();
			this.setVisible(false);
		}
	}

	/**
	 * Checks the input information about the proxy is valid or not.
	 * 
	 * @return boolean
	 *     valid or not
	 */
	private boolean checkFields()
	{
		boolean flag = true;

		if (!ValidationHelper.validateEmptyString(ipText.getText()))
		{
			ipText.setBackground(noticeColor);
			flag = false;
		}

		if (!ValidationHelper.validateEmptyString(portText.getText()))
		{
			portText.setBackground(noticeColor);
			flag = false;
		}

		if (!flag)
		{
			AmbOptionPane.showMessageDialog(Constants.ERROR_FIELDS,
					"Warning", JOptionPane.ERROR_MESSAGE);				
		}
		
		return flag;
	}

	private void addListeners()
	{
		addKeyListeners();
		addFocuListeners();
		addCheckBoxListener();
		addDocumentListeners();
	}
	
	private void addKeyListeners()
	{
		portText.addKeyListener(getEnterKeyAdapter());
		ipText.addKeyListener(getEnterKeyAdapter());
		submit.addKeyListener(getEnterKeyAdapter());		
	}

	private void addFocuListeners()
	{
		portText.addFocusListener(getFocusListener());
		ipText.addFocusListener(getFocusListener());
	}
	
	private void addCheckBoxListener()
	{
		useProxyBox.addChangeListener(getUseProxyBoxListener());
	}

	private void addDocumentListeners()
	{
		ipText.getDocument().addDocumentListener(getDocumentListener());
		portText.getDocument().addDocumentListener(getDocumentListener());
	}
	
	private KeyAdapter getEnterKeyAdapter()
	{
		return new KeyAdapter()
		{
			public void keyReleased(KeyEvent e)
			{
				if (KeyEvent.VK_ENTER == e.getKeyCode())
				{
					submit();
				}
			}
		};
	}

	/**
	 * If state of useProxyBox changed, then change the <code>ipText</code>,
	 * <code>portText</code> and <code>test</code> according to the state.
	 * 
	 * @return
	 */
	private ChangeListener getUseProxyBoxListener()
	{
		return new ChangeListener()
		{
			public void stateChanged(ChangeEvent arg0)
			{
				boolean useProxy = useProxyBox.isSelected();
				ipText.setEnabled(useProxy);
				portText.setEnabled(useProxy);
				test.setEnabled(useProxy);				
				submit.setEnabled(isChanged());
			}
		};
	}
	
	/**
	 * If a text field get focus, then change the color to normal color.
	 * 
	 * @return
	 */
	private FocusListener getFocusListener()
	{
		return new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
				JTextField jTextFiled = (JTextField) e.getSource();
				jTextFiled.setBackground(oriColor);
			}

			public void focusLost(FocusEvent e)
			{
				submit.setEnabled(isChanged());
			}
		};
	}
	
	private DocumentListener getDocumentListener()
	{		
		return new DocumentListener() {

			public void changedUpdate(DocumentEvent e)
			{
				submit.setEnabled(isChanged());
			}

			public void insertUpdate(DocumentEvent e)
			{
				submit.setEnabled(isChanged());
			}

			public void removeUpdate(DocumentEvent e)
			{
				submit.setEnabled(isChanged());
			}
			
		};
	}
	
	/**
	 * Tests host and port can be connected or not.
	 * 
	 * @param host
	 *     The host of the proxy.
	 * @param port
	 *     The port of the proxy. 
	 * @return
	 *     Connecting is successful or failed.
	 */	
	private boolean testProxy(String host, String port)
	{
		try
		{
			int port2 = Integer.parseInt(port);
			
			// Try to connect the proxy.
			Socket socket = new Socket(host, port2);			
			socket.close();
			
			// If no exception happen, connecting successful.
			return true;
		}
		catch (Exception e)
		{
			// If any exception happened, connecting failed.
            return false;
		}
	}
	
	/**
	 * If any properties has been changed, updates the <code>submit</code> button states.
	 * 
	 * @return
	 */
	private boolean isChanged() 
	{
		String newIp = ipText.getText();
		String newPort = portText.getText();
		String useProxy = String.valueOf(useProxyBox.isSelected());
		
		// If don't use proxy.
		if (useProxy.equals(oldUseProxy)
				&& useProxy.equals(String.valueOf(false)))
		{
			return false;
		}
		
		// If no properties has been changed.
		if (newIp.equals(oldIp) && newPort.equals(oldPort)
				&& useProxy.equals(oldUseProxy))
		{
			return false;
		}
		
		return true;
	}
}
