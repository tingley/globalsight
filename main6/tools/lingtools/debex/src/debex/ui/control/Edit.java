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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import debex.Setup;
import debex.helpers.Callback;
import debex.helpers.FileUtil;
import debex.data.ExtractorSettings;
import debex.ui.view.Debex;
import debex.ui.view.EditorPnl;

public class Edit
{
    private JDialog m_dialog;
    private EditorPnl m_editor;
    private Callback m_onSuccess;
    private Callback m_onFailure;

    private ExtractorSettings m_settings;

    public void init(Debex p_debex,
        Callback p_success, Callback p_failure,
        ExtractorSettings p_settings)
    {
        m_settings = p_settings;
        m_onSuccess = p_success;
        m_onFailure = p_failure;

        m_dialog = new JDialog(p_debex, true);
        m_dialog.setTitle("Editor");
        m_dialog.setDefaultCloseOperation(
            javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        m_dialog.addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                doCancel();
            }
        });

        m_editor = new EditorPnl();
        m_editor.btn_save.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doSave();
            }
        });

        m_editor.btn_cancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doCancel();
            }
        });

        Throwable ex = m_settings.m_exception;
        m_editor.result.setText(ex.getMessage());
        m_editor.result.setCaretPosition(1);

        m_editor.editor.setDragEnabled(false);
        //m_editor.editor.setFont(new Font("monospaced", Font.PLAIN, 12));

        final Callback onLoad = new Callback()
        {
            public void call()
            {
                documentLoaded();
            }
        };

        // Load the document.
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                //m_editor.editor.setDocument(new PlainDocument());
                loadDocument(m_settings, m_editor.editor.getDocument(), onLoad);
            }
        };
        SwingUtilities.invokeLater(runnable);

        m_dialog.getContentPane().add(m_editor);
        m_dialog.pack();
        // "null" centers dialog on the screen...
        m_dialog.setLocationRelativeTo(p_debex);
        m_dialog.show();
    }

    protected void doSave()
    {
        boolean success = true;

        try
        {
            String content = m_editor.editor.getText();
            String fileName = m_settings.m_fileName;
            String encoding = m_settings.m_encoding;

            FileUtil.writeFile(fileName, encoding, content);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(m_dialog,
                "Couldn't save file " + m_settings.m_fileName + ": " +
                ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);

            doCancel();
        }

        dispose(success);
    }

    protected void doCancel()
    {
        boolean success = false;

        dispose(success);
    }

    protected void dispose(final boolean success)
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                if (success)
                {
                    m_onSuccess.call();
                }
                else
                {
                    m_onFailure.call();
                }

                m_dialog.setVisible(false);
                m_dialog.dispose();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    protected void loadDocument(ExtractorSettings p_settings,
        Document p_document, final Callback p_callback)
    {
        String fileName = p_settings.m_fileName;
        String encoding = p_settings.m_encoding;

        try
        {
            String content = FileUtil.readFile(fileName, encoding);

            p_document.insertString(0, content, null);
        }
        catch (Throwable ex)
        {
            if (Setup.DEBUG)
            {
                ex.printStackTrace();
            }
        }

        Runnable runnable = new Runnable()
        {
            public void run()
            {
                p_callback.call();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    protected void documentLoaded()
    {
        // TODO: parse the error message and move the cursor or
        // selection to the error location.

        String message = m_editor.result.getText();

        // Embedded JavaScript parse exception between (5:1) and (9:9):
        // ...
        // Lexical error at line 5, column 0.

        int line = Math.max(getErrorLine(message) - 1, 0);
        int col = Math.max(getErrorColumn(message) - 1, 0);

        int embeddedLine = Math.max(getEmbeddedErrorLine(message) - 2, 0);

        line += embeddedLine;

        try
        {
            int lineOffset = m_editor.editor.getLineStartOffset(line);
            int offset = lineOffset + col;

            if (Setup.DEBUG)
            {
                System.err.println("Moving caret to [" + line + "," +
                    col + "] offset=" + offset);
            }

            m_editor.editor.requestFocus();
            m_editor.editor.setCaretPosition(offset);
        }
        catch (Exception ex)
        {
            // ignore
        }
    }

    private int getEmbeddedErrorLine(String p_text)
    {
        int result = 0;

        Pattern r = Pattern.compile("between\\s+\\((\\d+):\\d+\\)");
        Matcher m = r.matcher(p_text);

        if (m.find())
        {
            String res = p_text.substring(m.start(1), m.end(1));

            result = Integer.parseInt(res);
        }

        return result;
    }

    private int getErrorLine(String p_text)
    {
        int result = 0;

        Pattern r = Pattern.compile("line\\s+(\\d+)");
        Matcher m = r.matcher(p_text);

        if (m.find())
        {
            String res = p_text.substring(m.start(1), m.end(1));

            result = Integer.parseInt(res);
        }

        return result;
    }

    private int getErrorColumn(String p_text)
    {
        int result = 0;

        Pattern r = Pattern.compile("column\\s+(\\d+)");
        Matcher m = r.matcher(p_text);

        if (m.find())
        {
            String res = p_text.substring(m.start(1), m.end(1));

            result = Integer.parseInt(res);
        }

        return result;
    }
}
