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

package com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.dmsview;

//JDK
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JFrame;
//GlobalSight
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.WindowUtil;
import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;

/**
 * This class provides a browsing tool for a DMS object directory hierarchy
 */
public class DirectoryDialog extends JDialog implements ActionListener, WindowListener
{
    private boolean userCancel = false;
    private Object path[];
    private MessageCatalog msgCat;
    private DirectoryServicesPanel _dirPanel;
    private JButton _b_OK;
    private JButton _b_Cancel;
    private JLabel fileLabel;
    private JLabel titleLabel;
    private JLabel pathLabel;
    private JTextField fileField;
    private JTextField titleField;
    private JTextField pathField;
    private boolean m_bAllowLeafOnly = false;

    /* public DirectoryDialog(JFrame frame, boolean modal, DocRepository doc_repos, boolean p_bAllowLeafOnly)
 {   //shailaja. Changed Frame to JFrame
     this(frame, modal, doc_repos);
     m_bAllowLeafOnly = p_bAllowLeafOnly;
 }      */

    public DirectoryDialog(JFrame frame, boolean modal,  boolean p_bAllowLeafOnly)
    {   
        this(frame, modal);
        m_bAllowLeafOnly = p_bAllowLeafOnly;
    }      


    /**
     * Constructor.
     * @param frame the parent frame for this dialog.
     * @param modal whether this dialog is modal or not
     * @param doc_repos the DocRepository object reference
     * @roseuid 372F6DD10036
     */

    //Function commneted out becuase it uses DocRepository ,which we do not have  a current object
    //public DirectoryDialog(JFrame frame, boolean modal, DocRepository doc_repos) {//shailaja. Changed Frame to JFrame
    public DirectoryDialog(JFrame frame, boolean modal)
    {
        super(frame,modal);

        /*if ( doc_repos == null )
        {
            Log.println(Log.LEVEL0, "DirectoryDialog(): DocRepository passed is NULL!");
            return;
        } */

        msgCat = new MessageCatalog ("com.globalsight.everest.webapp.applet.common.AppletResourceBundle");

        getContentPane().setBackground(new Color(204, 204, 204));
        setSize(400, 400);                                        
        getContentPane().setLayout(new BorderLayout());

        JPanel bottom_panel = new JPanel();
        bottom_panel.setLayout(new GridLayout(3, 2, 0, 0));

        fileLabel = new JLabel(AppletHelper.getI18nContent("lb_file"), Label.RIGHT); 

        fileField = new JTextField(25);                           
        JPanel c2 = new JPanel();
        c2.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        pathLabel = new JLabel(AppletHelper.getI18nContent("lb_path"), Label.RIGHT);
        c2.add(pathLabel);
        pathField = new JTextField(25);
        c2.add(pathField);
        bottom_panel.add(c2);

        JPanel c3 = new JPanel();
        c3.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        titleLabel = new JLabel(AppletHelper.getI18nContent("lb_title"), Label.RIGHT);
        c3.add(titleLabel);
        titleField = new JTextField(msgCat.getMsg("docTitle"), 25);
        c3.add(titleField);
        bottom_panel.add(c3);

        JPanel _buttonPanel = new JPanel();
        _buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottom_panel.add(_buttonPanel);

        _b_OK = new JButton(AppletHelper.getI18nContent("lb_ok"));
        _b_OK.setActionCommand("OK");
        //	_b_OK.setBackground(new Color(204, 204, 204));
        _b_OK.addActionListener(this);
        _buttonPanel.add(_b_OK);

        _b_Cancel = new JButton(AppletHelper.getI18nContent("lb_cancel"));
        _b_Cancel.setActionCommand("Cancel");
        //	_b_Cancel.setBackground(new Color(204, 204, 204));
        _b_Cancel.addActionListener(this);
        _buttonPanel.add(_b_Cancel);

        //_dirPanel = new DirectoryServicesPanel(doc_repos, this);
        _dirPanel = new DirectoryServicesPanel( this);


        getContentPane().add("Center", _dirPanel);
        getContentPane().add("South", bottom_panel);

        validate();

        this.addWindowListener(this);
        path = null;

        setResizable(false);
    }   
    /**
     * returns the file name specified by the user
       @roseuid 372F6DD1003A
     */
    public String getSelectedName()
    {
        //##begin DirectoryDialog::getSelectedName%35495C170342.body preserve=yes
        return fileField.getText();
        //##end DirectoryDialog::getSelectedName%35495C170342.body

    }

    /**
     * returns the document title specified by the user
       @roseuid 372F6DD1003B
     */
    public String getSelectedTitle()
    {
        return titleField.getText();
    }

    /**
     * Sets the default text of the file field to the argumented name
     * @param _name the name to be set as the default text in the file field
     * @roseuid 372F6DD1003C
     */
    public void setTargetFileName(String _name)
    {
        fileField.setText(_name);
        _dirPanel.setTarget(true);
    }

    /**
       @roseuid 372F6DD1003E
     */
    public void hideTitleField()
    {
        titleLabel.setVisible(false);
        titleField.setVisible(false);
    }

    public void hidePathField()
    {
        pathLabel.setVisible(false);
        pathField.setVisible(false);
    }

    /**
       @roseuid 372F6DD1003F
     */
    public String getSelectedDirectory()
    {
        return getTitle();//w/o the file name if any
    }

    /**
     * Returns the string representation of the absolute path of the file selected by the user.
     * @return the string representation of the absolute path of the file selected by the user.
     * @roseuid 372F6DD10040
     */
    public String getSelectedPath() 
    {
        return pathField.getText();
    }

    /**
     * Invoked when this dialog is closed.
       @roseuid 372F6DD10041
     */
    public void windowClosed(WindowEvent e)
    {
        //##begin DirectoryDialog::windowClosed%35495C170347.body preserve=yes
        //##end DirectoryDialog::windowClosed%35495C170347.body

    }

    /**
     * Invoked when this dialog is opened.
       @roseuid 372F6DD10043
     */
    public void windowOpened(WindowEvent e)
    {
        //##begin DirectoryDialog::windowOpened%35495C170349.body preserve=yes
        //##end DirectoryDialog::windowOpened%35495C170349.body

    }

    /**
     * Invoked when this dialog is in the process of being closed.  Disposes this dialog.
       @roseuid 372F6DD10045
     */
    public void windowClosing(WindowEvent e)
    {
        //##begin DirectoryDialog::windowClosing%35495C17034B.body preserve=yes
        userCancel = true;
        dispose();
        //##end DirectoryDialog::windowClosing%35495C17034B.body

    }

    /**
     * Invoked when an action occurs.
       @roseuid 372F6DD10047
     */
    public void actionPerformed(ActionEvent e)
    {
        //##begin DirectoryDialog::actionPerformed%35495C17034D.body preserve=yes

        String action = e.getActionCommand();

        if (action.equals("OK"))
        {

            if (m_bAllowLeafOnly && !_dirPanel.isSelectedItemLeaf())
            {
                WindowUtil.showMsgDlg(WindowUtil.getFrame(this),
                                      AppletHelper.getI18nContent("msg_no_dir_selection"),
                                      AppletHelper.getI18nContent("msg_selection_error"),
                                      WindowUtil.TYPE_ERROR);
                return;
            }

            if ( (pathField.getText()).length() == 0 )
            {
                WindowUtil.showMsgDlg(WindowUtil.getFrame(this),
                        AppletHelper.getI18nContent("msg_path_field_blank"),
                        AppletHelper.getI18nContent("msg_path_field_error"),
                                      WindowUtil.TYPE_ERROR);
                return;
            }
            // comments today june 8 2002
            /* else if ( (pathField.getText()).endsWith(DMS._pathSeparator) )
             {
                 pathField.setText(pathField.getText() + fileField.getText());
             }
               */
            //in case user cancels at this time (e.g., while FTP client is transfering
            //attachment file, which may hang client
            _b_Cancel.setEnabled(false);
/*
            path = _dirPanel.getPath();
            
            if ( path == null )
            {
                WindowUtil.showMsgDlg(WindowUtil.getFrame(this),
                                        msgCat.getMsg("Invalid path field!"),
                                        msgCat.getMsg("msg_pathFieldError"),
                                        WindowUtil.TYPE_ERROR);
                return;
            }
            
            if (path.length == 1)
            {
                pathField.setText(null);
            }
*/
            dispose();
        }
        else if (action.equals("Cancel"))
        {
            path = null;
            userCancel = true;
            pathField.setText(null);
            dispose();
        }
    }

    /**
       @roseuid 372F6DD10049
     */
    public boolean isCancelled()
    {
        return userCancel;
    }

    /**
     * Invoked when this dialog is iconfied.
       @roseuid 372F6DD1004A
     */
    public void windowIconified(WindowEvent e)
    {
        //##begin DirectoryDialog::windowIconified%35495C170351.body preserve=yes
        //##end DirectoryDialog::windowIconified%35495C170351.body

    }

    /**
     * Invoked when this dialog is activated.
       @roseuid 372F6DD1004C
     */
    public void windowActivated(WindowEvent e)
    {
        //##begin DirectoryDialog::windowActivated%35495C170353.body preserve=yes
        //##end DirectoryDialog::windowActivated%35495C170353.body

    }

    /**
     * Invoked when this dialog is de-iconfied.
       @roseuid 372F6DD1004E
     */
    public void windowDeiconified(WindowEvent e)
    {
        //##begin DirectoryDialog::windowDeiconified%35495C170355.body preserve=yes
        //##end DirectoryDialog::windowDeiconified%35495C170355.body

    }

    /**
     * Invoked when this dialog is de-activated.
       @roseuid 372F6DD10050
     */
    public void windowDeactivated(WindowEvent e)
    {
        //##begin DirectoryDialog::windowDeactivated%35495C17035B.body preserve=yes
        //##end DirectoryDialog::windowDeactivated%35495C17035B.body

    }

    /**
       @roseuid 372F6DD10052
     */
    JTextField getFileField()
    {
        return fileField;
    }

    /**
       @roseuid 372F6DD10053
     */
    JTextField getPathField()
    {
        return pathField;
    }

    /**
       @roseuid 372F6DD10054
     */
    JTextField getTitleField()
    {
        return titleField;           
    }

    /**
     * Sets the argumented str to the file field of this dialog.
     * @param str the string to be set as the file field's text
     * @roseuid 372F6DD10055
     */
    public void setFileField(String str)
    {
        //##begin DirectoryDialog::setFileField%3558C5BE029A.body preserve=yes
        fileField.setText(str);
        //##end DirectoryDialog::setFileField%3558C5BE029A.body

    }
}
