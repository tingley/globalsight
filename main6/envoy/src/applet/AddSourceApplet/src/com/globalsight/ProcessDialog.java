package com.globalsight;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EtchedBorder;

import com.globalsight.table.RowVo;
import com.globalsight.util.XmlUtil;
import com.globalsight.vo.AddFileVo;

public class ProcessDialog extends JDialog implements Runnable
{

    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    private JProgressBar jProgressBar = null;
    private JLabel jLabel = null;
    private JPanel jPanel = null;
    private JButton jButton1 = null;
    private JButton jButton = null;

    private List<RowVo> rows = null;  //  @jve:decl-index=0:
    private EditSourceApplet applet = null;
    private JLabel jLabel1 = null;
    /**
     * @param owner
     */
    public ProcessDialog(Frame owner, EditSourceApplet applet)
    {
        super(owner);
        this.applet = applet;
        initialize();
    }

    
    private void setLocation()
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Point location = new Point((screen.width - getWidth()) / 2,
                (screen.height - getHeight()) / 2);

        setLocation(location);
    }
    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        this.setTitle(applet.getLable("lb_applet_title", "Upload Files"));
        this.setSize(509, 351);
        this.setContentPane(getJContentPane());
        setLocation();
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane()
    {
        if (jContentPane == null)
        {
            jLabel1 = new JLabel();
            jLabel1.setBounds(new Rectangle(58, 57, 363, 31));
            jLabel1.setText(applet.getLable("lb_upldate_applet_msg", "Please wait until upload has finished."));
            jLabel = new JLabel();
            jLabel.setText("");
            jLabel.setBounds(new Rectangle(57, 144, 363, 23));
            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            jContentPane.add(jLabel, null);
            jContentPane.add(getJProgressBar(), null);
            jContentPane.add(getJButton1(), null);
            jContentPane.add(getJPanel(), null);
            jContentPane.add(getJButton(), null);
            jContentPane.add(jLabel1, null);
        }
        return jContentPane;
    }

    /**
     * This method initializes jProgressBar
     * 
     * @return javax.swing.JProgressBar
     */
    private JProgressBar getJProgressBar()
    {
        if (jProgressBar == null)
        {
            jProgressBar = new JProgressBar();
            jProgressBar.setStringPainted(true);
            jProgressBar.setBounds(new Rectangle(57, 169, 367, 32));
        }
        return jProgressBar;
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel()
    {
        if (jPanel == null)
        {
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.setBorder(BorderFactory
                    .createEtchedBorder(EtchedBorder.LOWERED));
            jPanel.setBounds(new Rectangle(12, 245, 480, 2));
        }
        return jPanel;
    }

    /**
     * This method initializes jButton1
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton1()
    {
        if (jButton1 == null)
        {
            jButton1 = new JButton();
            jButton1.setBounds(new Rectangle(299, 267, 76, 24));
            jButton1.setText(applet.getLable("lb_cancel", "Cancel"));
            jButton1.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    try
                    {
                        applet.getAppletContext()
                                .showDocument(
                                        new URL(
                                                "javascript:dijit.byId('addSourceDiv').hide()"));
                        System.exit(0);
                    }
                    catch (MalformedURLException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            });
            
        }
        return jButton1;
    }

    /**
     * This method initializes jButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getJButton()
    {
        if (jButton == null)
        {
            jButton = new JButton();
            jButton.setText(applet.getLable("lb_finish", "Finish"));
            jButton.setBounds(new Rectangle(386, 267, 83, 25));
            
            jButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    try
                    {
                        applet.getAppletContext().showDocument(
                                new URL("javascript:refreshJobPage()"));
                    }
                    catch (MalformedURLException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            });
        }
        return jButton;
    }

    public List<RowVo> getRows()
    {
        return rows;
    }

    public void setRows(List<RowVo> rows)
    {
        this.rows = rows;
    }

    public void uploadFiles()
    {
        jButton.setEnabled(false);
        jButton1.setEnabled(true);
        Thread t = new Thread(this);
        t.start();
    }

    public EditSourceApplet getApplet()
    {
        return applet;
    }

    public void setApplet(EditSourceApplet applet)
    {
        this.applet = applet;
    }


    @Override
    public void run()
    {
        int n = 1;
        int total = rows.size();
        jProgressBar.setValue(0);
        jProgressBar.setMaximum(total);
        
        AddFileVo vo = new AddFileVo();
        List<String> paths = new ArrayList<String>();
        List<Long> fileProfileIds = new ArrayList<Long>();
        for (RowVo row : rows)
        {
            File file = row.getFile();
            String path = file.getAbsolutePath();
            path = path.replace("\\", "/");
            path = path.substring(path.indexOf("/") + 1);

            Map<String, String> args = new HashMap<String, String>();
            args.put("jobId", applet.getJobId());
            args.put("path", path);
            
            jLabel.setText(applet.getLable("lb_processing_upload_file", "Processing upload file") + ": " + file.getName() + "  [" + n + "/" + total + "]");
            applet.execute("uploadFile", null, file, args);

            paths.add(path);
            row.getSelectedFileProfile();
            fileProfileIds.add(row.getSelectedFileProfile().getId());
            jProgressBar.setValue(n++);
        }

        vo.setFilePaths(paths);
        vo.setFileProfileIds(fileProfileIds);
        vo.setJobId(Long.parseLong(applet.getJobId()));

        Map<String, String> args = new HashMap<String, String>();
        args.put("xml", XmlUtil.object2String(vo));
        applet.execute("addSouceFile", null, null, args);
        
        jButton.setEnabled(true);
        jButton1.setEnabled(false);
        jProgressBar.setValue(rows.size());
        jLabel.setText(total + " / " + total);
        jLabel1.setText(applet.getLable("lb_upload_successful", "Upload successfully"));
        
    }

} // @jve:decl-index=0:visual-constraint="10,10"
