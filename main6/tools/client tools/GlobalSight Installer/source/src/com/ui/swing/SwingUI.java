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
package com.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.config.properties.Resource;
import com.ui.UI;
import com.util.ServerUtil;
import com.util.UIUtil;

public class SwingUI extends JFrame implements UI
{
    private static final long serialVersionUID = -8085368150814931021L;
    private static Logger log = Logger.getLogger(SwingUI.class);

    private JProgressBar progessBar = new JProgressBar();
    private JButton closeButton = new JButton(Resource.get("button.close"));
    private JLabel progessMessage = new JLabel();
    private JTextField serverPath = new JTextField();

    public SwingUI()
    {
        super();
        initUI();
        initAction();
    }

    protected void initUI()
    {
        this.setSize(400, 170);
        this.setResizable(false);
        this.setTitle(Resource.get("title.mainFrame"));
        this.setLayout(null);
        setLocation();

        JLabel pathLabel = new JLabel(Resource.get("msg.serverPath"));
        pathLabel.setSize(120, 22);
        pathLabel.setLocation(20, 15);
        this.add(pathLabel);

        serverPath.setEditable(false);
        serverPath.setSize(255, 22);
        serverPath.setLocation(125, 15);
        this.add(serverPath);

        progessMessage.setText("begin");
        progessMessage.setSize(370, 25);
        progessMessage.setLocation(20, 45);
        progessMessage.setBackground(this.getBackground());
        this.add(progessMessage);

        progessBar.setSize(360, 20);
        progessBar.setLocation(20, 70);
        progessBar.setStringPainted(true);
        progessBar.setMaximum(MAX_PROCESS);
        this.add(progessBar);

        closeButton.setSize(UIUtil.DEFAULT_WIDTH, UIUtil.DEFAULT_HEIGHT);
        closeButton.setLocation(300, 100);
        closeButton.setEnabled(false);
        this.add(closeButton);
    }

    protected void initAction()
    {
        closeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });

        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                onClose();
            }
        });
    }

    private void onClose()
    {
        if (JOptionPane.showConfirmDialog(this, Resource.get("confirm.exit"),
                Resource.get("title.exit"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }
        else
        {
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
    }

    public void error(String msg)
    {
        progessMessage.setForeground(Color.red);
        JOptionPane.showMessageDialog(this, msg, Resource
                .get("title.errorDialog"), JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }

    public void infoError(String msg)
    {
        progessMessage.setForeground(Color.red);
        JOptionPane.showMessageDialog(this, msg, Resource
                .get("title.errorDialog"), JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    private void setLocation()
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Point location = new Point((screen.width - getWidth()) / 2,
                (screen.height - getHeight()) / 2);

        setLocation(location);
    }

    @Override
    public void addProgress(int addRate, String msg)
    {
        int value = progessBar.getValue() + addRate;        
        progessBar.setValue(value);
        progessMessage.setText(msg);
        
        String printPath = serverPath.getText();
        if (printPath == null || printPath.trim().length() == 0)
        {
            serverPath.setText(ServerUtil.getPath());
        }
    }

    @Override
    public void showWelcomePage()
    {
        WelcolmPage page = new WelcolmPage();
        page.setVisible(true);
        this.setVisible(true);
    }

    public static void main(String[] args)
    {
        UIUtil.setLookAndFeel();
        SwingUI page = new SwingUI();
        page.setVisible(true);
    }

    /**
     * @see com.install.Interface#specifyServer()
     */
    @Override
    public String specifyServer()
    {
        SpecifyServerDialog dialog = new SpecifyServerDialog();
        String path = dialog.getServerPath();
        serverPath.setText(path);
        return path;
    }

    /**
     * @see com.ui.UI#showMessage(java.lang.String)
     */
    @Override
    public void showMessage(String msg)
    {
        JOptionPane.showMessageDialog(null, msg, Resource
                .get("title.mainFrame"), JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void finish()
    {
        progessBar.setValue(UI.MAX_PROCESS);
        progessMessage.setText(Resource.get("process.finish"));
        closeButton.setEnabled(true);
        log.info("Upgrade is done.");
    }

    @Override
    public boolean confirmRewrite(String path)
    {
        String msg = MessageFormat.format(Resource.get("confirm.rewrite"),
                path);
        msg += "\n\n" + Resource.get("confirm.rewrite2");
        return JOptionPane.showConfirmDialog(this, msg, Resource
                .get("title.confirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @Override
    public void confirmContinue(String msg)
    {
        msg = msg + "\n" + Resource.get("confirm.continue");
        if (JOptionPane.showConfirmDialog(this, msg, Resource
                .get("title.confirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
        {
            log.info("Exit");
            System.exit(0);
        }
    }
    
    @Override
    public void confirmUpgradeAgain()
    {
        String msg = Resource.get("version.upgrade.same");
        msg = msg + "\n\n" + Resource.get("confirm.upgradeAgain");
        if (JOptionPane.showConfirmDialog(this, msg, Resource
                .get("title.confirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
        {
            log.info("Not upgrade again");
            System.exit(0);
        }       
    }

    @Override
    public void tryAgain(String msg)
    {
        new TryAgainDialog(msg);
    }

	@Override
	public void upgradeJdk() {
		new JdkDialog();
	}
}
