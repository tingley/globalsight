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
package debex.ui.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import java.io.IOException;
import java.io.File;
import java.util.Vector;

import debex.Setup;
import debex.data.ExtractorSettings;
import debex.data.FileTypes;
import debex.data.Locales;
import debex.data.Encodings;
import debex.helpers.Callback;
import debex.helpers.BrowserLauncher;
import debex.helpers.filefilter.GenericFileFilter;
import debex.helpers.filefilter.RuleFileFilter;
import debex.ui.control.Edit;
import debex.ui.control.Extract;
import debex.ui.view.AboutDlg;
import debex.ui.view.Debex;

public class Start
{
    private static final String ABOUT_STR =
        "<html>&nbsp;&nbsp;<font size='+1'><b>Debex v1.0</b></font><hr>" +
        "&nbsp;&nbsp;<b>GlobalSight Extractor Debugger</b><br><br>" +
        "&nbsp;&nbsp;<b>License</b>:&nbsp;&nbsp;GNU GPL<br>" +
        "&nbsp;&nbsp;<b>Author</b>:&nbsp;&nbsp;&nbsp;&nbsp;Cornelis Van Der Laan (<a href='mailto:nils@globalsight.com'>nils@globalsight.com</a>)<br>" +
        "&nbsp;&nbsp;<b>Website</b>:&nbsp;<a href='http://www.globalsight.com'>http://www.globalsight.com</a><br><br>" +
        "&nbsp;&nbsp;<b>Credits</b>:<br>" +
        "&nbsp;&nbsp;&nbsp;&nbsp;BrowserLauncher (<a href='http://browserlauncher.sourceforge.net'>http://browserlauncher.sourceforge.net</a>)" +
        "</html>";

    private Debex frmDebex;
    // private final Callback[] beforeExit = new Callback[1];

    private JFileChooser m_fileChooser = new JFileChooser();

    private ExtractorSettings m_settings;
    private DefaultComboBoxModel m_fileTypes;
    private DefaultComboBoxModel m_encodings;
    private DefaultComboBoxModel m_locales;

    public void init()
    {
        frmDebex = new Debex();

        m_settings = Setup.getExtractorSettings();

        prepareMenus();
        prepareControls();
        prepareButtons();

        // frmDebex.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frmDebex.setIconImage(Setup.getIcon().getImage());
        frmDebex.show();
    }

    protected void prepareMenus()
    {
        //Exit
        frmDebex.mi_fileExit.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                exit();
            }
        });

        //Exit
        frmDebex.setDefaultCloseOperation(
            javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        frmDebex.addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exit();
            }
        });

        //Help
        frmDebex.mi_helpHelp.addActionListener(new ActionListener()
        {
            String url = "";

            public void actionPerformed(ActionEvent ae)
            {
                try
                {
                    if (url.equals(""))
                    {
                        url = Setup.getHelpFile().toURL().toExternalForm();
                    }

                    BrowserLauncher.openURL(url);
                }
                catch (IOException e)
                {
                    if (Setup.DEBUG)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        //About
        frmDebex.mi_helpAbout.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                about();
            }
        });
    }

    protected void prepareControls()
    {
        // filenames
        frmDebex.filename.setText(m_settings.m_fileName);
        frmDebex.rulefilename.setText(m_settings.m_rulesFileName);

        // type
        FileTypes types = Setup.getFileTypes();
        Vector names = types.getFormatNames();
        m_fileTypes = new DefaultComboBoxModel(names);
        frmDebex.filetype.setModel(m_fileTypes);

        // locale
        Locales locales = Setup.getLocales();
        names = locales.getLocaleNames();
        m_locales = new DefaultComboBoxModel(names);
        frmDebex.locale.setModel(m_locales);

        // encoding
        Encodings encodings = Setup.getEncodings();
        names = encodings.getEncodingNames();
        m_encodings = new DefaultComboBoxModel(names);
        frmDebex.encoding.setModel(m_encodings);

        // segmentation
        boolean sentence = m_settings.m_sentenceSegmentation;
        if (sentence)
        {
            frmDebex.segmentationSentence.setSelected(true);
        }
        else
        {
            frmDebex.segmentationParagraph.setSelected(true);
        }
    }

    protected void prepareButtons()
    {
        //Browse Buttons
        frmDebex.btnBrowseFile.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                selectFile();
            }
        });

        //Browse Buttons
        frmDebex.btnBrowseRule.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                selectRuleFile();
            }
        });

        //Extract Button
        frmDebex.btnExtract.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                extractFile();
            }
        });

        //Edit Errors Button
        frmDebex.btnEditErrors.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                editErrors();
            }
        });

        //View GXML Button
        frmDebex.btnViewGxml.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                viewGxml();
            }
        });
    }

    protected void selectFile()
    {
        File cwd = Setup.getCurrentDirectory();

        m_fileChooser.setCurrentDirectory(cwd);
        m_fileChooser.setSelectedFile(null);
        // get rid of previously selected file
        m_fileChooser.updateUI();
        m_fileChooser.resetChoosableFileFilters();
        m_fileChooser.setAcceptAllFileFilterUsed(true);

        int ret = m_fileChooser.showOpenDialog(frmDebex);
        if (ret == JFileChooser.APPROVE_OPTION)
        {
            cwd = m_fileChooser.getCurrentDirectory();
            Setup.setCurrentDirectory(cwd);

            String filename =
                m_fileChooser.getSelectedFile().getAbsolutePath();

            frmDebex.filename.setText(filename);
        }
    }

    protected void selectRuleFile()
    {
        File cwd = Setup.getCurrentDirectory();
        FileFilter ruleFilter = new RuleFileFilter();

        m_fileChooser.setCurrentDirectory(cwd);
        m_fileChooser.setSelectedFile(null);
        // get rid of previously selected file
        m_fileChooser.updateUI();
        m_fileChooser.resetChoosableFileFilters();
        m_fileChooser.setFileFilter(ruleFilter);
        m_fileChooser.setAcceptAllFileFilterUsed(false);

        int ret = m_fileChooser.showOpenDialog(frmDebex);
        if (ret == JFileChooser.APPROVE_OPTION)
        {
            cwd = m_fileChooser.getCurrentDirectory();
            Setup.setCurrentDirectory(cwd);

            String filename =
                m_fileChooser.getSelectedFile().getAbsolutePath();

            frmDebex.rulefilename.setText(filename);
        }
    }

    protected ExtractorSettings getSettings()
    {
        m_settings.m_fileName = frmDebex.filename.getText();
        m_settings.m_rulesFileName = frmDebex.rulefilename.getText();
        m_settings.m_type = m_fileTypes.getSelectedItem().toString();
        m_settings.m_encoding = m_encodings.getSelectedItem().toString();
        m_settings.m_locale = m_locales.getSelectedItem().toString();
        m_settings.m_sentenceSegmentation =
            frmDebex.segmentationSentence.isSelected() ? true : false;

        return m_settings;
    }

    protected void editErrors()
    {
        final Callback onSuccess = new Callback()
        {
            public void call()
            {
            }
        };

        final Callback onFailure = new Callback()
        {
            public void call()
            {
            }
        };

        String fileName = frmDebex.filename.getText();
        if (!(new File(fileName).exists()))
        {
            JOptionPane.showMessageDialog(frmDebex,
                "File `" + fileName + "' does not exist.",
                "Error",
                JOptionPane.ERROR_MESSAGE);

            frmDebex.filename.requestFocus();

            return;
        }

        Runnable runnable = new Runnable()
        {
            public void run()
            {
                Edit edit = new Edit();
                edit.init(frmDebex, onSuccess, onFailure, getSettings());
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    protected void extractFile()
    {
        final Callback onSuccess = new Callback()
        {
            public void call()
            {
                frmDebex.btnEditErrors.setEnabled(false);
                frmDebex.btnViewGxml.setEnabled(true);

                frmDebex.result.setText("Extraction succeeded.");
            }
        };

        final Callback onFailure = new Callback()
        {
            public void call()
            {
                frmDebex.btnEditErrors.setEnabled(true);
                frmDebex.btnViewGxml.setEnabled(false);

                Throwable ex = m_settings.m_exception;
                if (ex != null)
                {
                    frmDebex.result.setText(ex.getMessage());
                    frmDebex.result.setCaretPosition(1);
                }
                else
                {
                    frmDebex.result.setText("Extraction failed");
                }
            }
        };

        frmDebex.result.setText("");

        String fileName = frmDebex.filename.getText();
        if (!(new File(fileName).exists()))
        {
            JOptionPane.showMessageDialog(frmDebex,
                "File `" + fileName + "' does not exist.",
                "Error",
                JOptionPane.ERROR_MESSAGE);

            frmDebex.filename.requestFocus();

            return;
        }

        Runnable runnable = new Runnable()
        {
            public void run()
            {
                Extract extract = new Extract();
                extract.init(frmDebex, onSuccess, onFailure, getSettings());
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    protected void viewGxml()
    {
        try
        {
            String url = m_settings.m_resultFileName;

            BrowserLauncher.openURL(url);
        }
        catch (IOException e)
        {
            if (Setup.DEBUG)
            {
                e.printStackTrace();
            }
        }
    }

    protected void about()
    {
        AboutDlg aboutDlg = new AboutDlg(frmDebex, true);
        aboutDlg.lb_content.setIcon(Setup.getIcon());
        aboutDlg.lb_content.setText(ABOUT_STR);

        aboutDlg.pack();
        aboutDlg.setLocationRelativeTo(null);
        aboutDlg.show();
    }

    protected void exit()
    {
        Setup.saveUserSettings();

        // int option = JOptionPane.showConfirmDialog(frmDebex,
        //   "Exit Debex?", "", JOptionPane.YES_NO_OPTION);
        // if (option != JOptionPane.YES_OPTION)
        // {
        //   return;
        // }

        System.exit(0);
    }

    public static void main(String args[])
        throws Exception
    {
        Setup.init1();
        Setup.initLF();

        //Splash screen
        // AboutWin aboutWin = new AboutWin();
        // aboutWin.lb_content.setIcon(Setup.getIcon());
        // aboutWin.lb_content.setText(ABOUT_STR);
        // aboutWin.pack();
        // aboutWin.setLocationRelativeTo(null);
        // aboutWin.show();

        Setup.init2();

        new Start().init();

        // aboutWin.requestFocusInWindow();
        // aboutWin.setVisible(false);
        // aboutWin.dispose();
    }
}
