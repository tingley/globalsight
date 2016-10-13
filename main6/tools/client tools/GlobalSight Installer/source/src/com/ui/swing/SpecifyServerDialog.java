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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.Main;
import com.config.properties.Resource;
import com.util.ServerUtil;
import com.util.UIUtil;
import com.util.UpgradeUtil;

/**
 * A dialog that let user specify the path of GlobalSight server. <br>
 * If no path is specified, system will exit.
 * 
 */
public class SpecifyServerDialog extends JDialog
{
    private static final long serialVersionUID = -2158159981016286935L;

    private static Logger log = Logger.getLogger(SpecifyServerDialog.class);

    private JTextField pathField = new JTextField();
    private JButton browserButton = new JButton(Resource.get("button.browser"));
    private JButton selectButton = new JButton(Resource.get("button.select"));
    private JButton cancelButton = new JButton(Resource.get("button.cancel"));
    private JButton exitButton = new JButton(Resource.get("button.notInstall"));
    private JFileChooser fileChooser = new JFileChooser(new File("./../.."));

    private static final int BUTTON_WIDTH = 80;
    private static final int NUTTON_HEIGHT = 22;
    private static final int DIALOG_WIDTH = 520;
    private static final int DIALOG_HEIGHT = 160;

    /**
     * Init.
     */
    public SpecifyServerDialog()
    {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocation();
        setResizable(false);
        setTitle(Resource.get("title.specifyServerPath"));

        initUI();
        initAction();

        this.setModal(true);
        this.setVisible(true);
    }

    /**
     * Init the UI.
     */
    private void initUI()
    {
        this.setLayout(null);

        String key = Main.isPatch() ? "msg.patch.specifyServer"
                : "msg.build.specifyServer";
        JLabel msg = new JLabel(Resource.get(key));
        msg.setSize(380, 30);
        msg.setLocation(20, 10);
        this.add(msg);

        JLabel pathLabel = new JLabel(Resource.get("input.server"));
        pathLabel.setSize(125, NUTTON_HEIGHT);
        pathLabel.setLabelFor(pathField);
        pathLabel.setLocation(20, 50);
        this.add(pathLabel);

        pathField.setSize(245, NUTTON_HEIGHT);
        pathField.setLocation(135, 50);
        this.add(pathField);

        browserButton.setToolTipText(Resource.get("tip.chooser"));
        browserButton.setSize(BUTTON_WIDTH + 20, NUTTON_HEIGHT);
        browserButton.setLocation(385, 50);
        this.add(browserButton);

        cancelButton.setSize(BUTTON_WIDTH, NUTTON_HEIGHT);
        cancelButton.setLocation(20, 85);
        this.add(cancelButton);

        selectButton.setSize(BUTTON_WIDTH, NUTTON_HEIGHT);
        selectButton.setLocation(105, 85);
        this.add(selectButton);

        exitButton.setSize(295, NUTTON_HEIGHT);
        exitButton.setLocation(190, 85);
        this.add(exitButton);

        fileChooser.setDialogTitle(Resource.get("title.specifyServerPath"));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    private void setLocation()
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Point location = new Point((screen.width - DIALOG_WIDTH) / 2,
                (screen.height - DIALOG_HEIGHT) / 2);

        setLocation(location);
    }

    public String getServerPath()
    {
        return pathField.getText();
    }

    private void initAction()
    {
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                exit();
            }
        });

        browserButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                specefyServer();
            }
        });

        exitButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });

        selectButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String path = pathField.getText();
                if (!ServerUtil.isServerPath(path))
                {
                    JOptionPane.showMessageDialog(null, Resource
                            .get("msg.wrongServerPath"));
                    specefyServer();
                    return;
                }
                
                if (!Main.isPatch())
                {
                    try
                    {
                        String oldPath = new File(path).getCanonicalPath();
                        String newPath = UpgradeUtil.newInstance().getPath();
                        oldPath = oldPath.replace("\\", "/");
                        newPath = newPath.replace("\\", "/");
                        if (oldPath.equalsIgnoreCase(newPath))
                        {
                            JOptionPane.showMessageDialog(null, Resource
                                    .get("path.upgrade.same"));
                            
                            return;
                        }
                    }
                    catch (IOException e1)
                    {
                        log.error(e1.getMessage(), e1);
                    }
                }

                log.info("Select path: " + pathField.getText());
                finish();
            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                exit();
            }
        });

    }

    private void finish()
    {
        this.setVisible(false);
    }

    private void exit()
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

    private void specefyServer()
    {
        fileChooser.showOpenDialog(null);
        File file = fileChooser.getSelectedFile();
        if (file != null)
        {
            pathField.setText(file.getAbsolutePath());
        }
    }

    public static void main(String[] args)
    {
        UIUtil.setLookAndFeel();
        new SpecifyServerDialog();
    }
}
