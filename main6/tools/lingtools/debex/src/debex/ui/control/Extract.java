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

import debex.Setup;
import debex.data.ExtractorSettings;
import debex.helpers.Callback;
import debex.helpers.Extractor;
import debex.helpers.threadpool.LiteThreadPool;
import debex.helpers.threadpool.PoolThread;
import debex.ui.view.SplashPnl;

public class Extract
{
    private static final int STEP_START = 0;
    private static final int STEP_END = 3;

    private ExtractorSettings m_settings;
    private Callback m_onSuccess;
    private Callback m_onFailure;
    private JDialog m_dialog;
    private SplashPnl m_splashPnl;
    private Extractor m_extractor;
    private volatile int m_progressStep;

    public void init(Component p_dlgOrFrm,
        Callback p_success, Callback p_failure,
        ExtractorSettings p_settings)
    {
        m_settings = p_settings;
        m_onSuccess = p_success;
        m_onFailure = p_failure;
        m_progressStep = 0;

        m_extractor = Setup.getExtractor();
        final Runnable target = new Runnable()
        {
            public void run()
            {
                extract();
            }
        };

        m_splashPnl = new SplashPnl();
        m_splashPnl.prb_progress.setMinimum(STEP_START);
        m_splashPnl.prb_progress.setMaximum(STEP_END);
        m_splashPnl.prb_progress.setValue(STEP_START);
        m_splashPnl.btn_cancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cancel();
            }
        });

        if (p_dlgOrFrm instanceof Dialog)
        {
            m_dialog = new JDialog((Dialog) p_dlgOrFrm, true);
        }
        else
        {
            m_dialog = new JDialog((Frame) p_dlgOrFrm, true);
        }

        m_dialog.setDefaultCloseOperation(
            javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        m_dialog.addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                cancel();
            }
        });

        LiteThreadPool threadPool = Setup.getLiteThreadPool();
        PoolThread thread1 = threadPool.getThread1();
        thread1.execute(target);

        m_dialog.getContentPane().add(m_splashPnl);
        m_dialog.pack();
        // "null" centers dialog on the screen...
        m_dialog.setLocationRelativeTo(p_dlgOrFrm);
        m_dialog.show();
    }

    protected void extract()
    {
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                m_splashPnl.prb_progress.setValue(m_progressStep);
            }
        };

        try
        {
            m_extractor.setSettings(m_settings);
            m_progressStep = 1;
            SwingUtilities.invokeLater(runnable);

            m_extractor.extract();
            m_progressStep = 2;
            SwingUtilities.invokeLater(runnable);

            String stylesheet = Setup.getStylesheet();

            m_settings.m_resultFileName = null;
            m_settings.m_resultFileName = m_extractor.saveResult(stylesheet);
            m_progressStep = STEP_END;
            SwingUtilities.invokeLater(runnable);
        }
        catch (Throwable ex)
        {
            JOptionPane.showMessageDialog(m_dialog,
                "Error occured while extracting: " + ex.getMessage(),
                "",
                JOptionPane.ERROR_MESSAGE);

            // Todo: find a better way to communicate the outcome
            m_settings.m_exception = ex;
        }

        cancel();
    }

    protected void cancel()
    {
        boolean success = true;

        try
        {
            if (m_progressStep != STEP_END)
            {
                // ??? m_extractor.cleanup();
                success = false;
            }
        }
        catch (Exception e)
        {
            if (Setup.DEBUG)
            {
                e.printStackTrace();
            }
        }

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
}
