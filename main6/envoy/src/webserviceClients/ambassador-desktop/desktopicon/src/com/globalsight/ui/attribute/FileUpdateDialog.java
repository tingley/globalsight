package com.globalsight.ui.attribute;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.globalsight.ui.AmbOptionPane;
import com.globalsight.ui.FileTransferHandler;
import com.globalsight.ui.attribute.vo.FileJobAttributeVo;
import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.Constants;
import com.globalsight.util.SwingHelper;

public class FileUpdateDialog extends JDialog
{

    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    private JPanel jPanel = null;
    private JButton jButton = null;
    private JButton jButton1 = null;
    private JScrollPane jScrollPane = null;
    private JList jList = null;
    private DefaultListModel model = new DefaultListModel();
    
    private FileJobAttributeVo fileAttribute;
    private JButton jButton2 = null;
    private JButton jButton3 = null;
    
    private List<FileUpdateLister> listers = new ArrayList<FileUpdateLister>();  //  @jve:decl-index=0:

    /**
     * @param owner
     */
    public FileUpdateDialog(Frame owner, FileJobAttributeVo fileAttribute)
    {
        super(owner);
        this.fileAttribute = fileAttribute;
        setModal(true);
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        this.setTitle("Select files for " + fileAttribute.getDisplayName());
        this.setSize(598, 356);
        this.setMinimumSize(new Dimension(350,240));
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
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJPanel(), BorderLayout.SOUTH);
            jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
        }
        return jContentPane;
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
            jPanel.setLayout(new FlowLayout());
            jPanel.add(getJButton(), null);
            jPanel.add(getJButton1(), null);
            jPanel.add(getJButton2(), null);
            jPanel.add(getJButton3(), null);
        }
        return jPanel;
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
            jButton.setText("Remove Files");
            jButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    int[] indexs = jList.getSelectedIndices();
                    for (int i = indexs.length - 1; i >= 0; i--)
                    {
                        model.remove(indexs[i]);
                    }
                }
            });
        }
        return jButton;
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
            jButton1.setText("Add File");
            jButton1.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    JFileChooser fileChooser = new JFileChooser(ConfigureHelper
                            .getBaseFolder());
                    fileChooser.setDialogTitle("Select The File");
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                    Point location = new Point((screen.height - fileChooser.getHeight()) / 2,
                            (screen.width - fileChooser.getWidth()) / 2);
                    fileChooser.setLocation(location);
                    
                    int status = fileChooser.showOpenDialog(jContentPane.getRootPane());
                    if (status == JFileChooser.APPROVE_OPTION)
                    {
                        File selectedFile = fileChooser.getSelectedFile();                        
                        model.addElement(selectedFile.getAbsolutePath());
                    }
                }
            });
        }
        return jButton1;
    }

    /**
     * This method initializes jScrollPane  
     *  
     * @return javax.swing.JScrollPane  
     */
    private JScrollPane getJScrollPane()
    {
        if (jScrollPane == null)
        {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJList());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jList    
     *  
     * @return javax.swing.JList    
     */
    private JList getJList()
    {
        if (jList == null)
        {
            for (String file : fileAttribute.getFiles())
            {
                model.addElement(file);
            }
            
            jList = new JList(model);
            AttFileTransfer handler = new AttFileTransfer(jList);
            jList.setTransferHandler(handler);
        }
        
        return jList;
    }

    /**
     * This method initializes jButton2 
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton2()
    {
        if (jButton2 == null)
        {
            jButton2 = new JButton();
            jButton2.setText("OK");
            jButton2.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    try
                    {
                        fireFileUpdate();
                        setVisible(false);
                    }
                    catch(Exception e2)
                    {
                        JOptionPane.showMessageDialog(null, e2.getMessage(),
                                "Warning", JOptionPane.ERROR_MESSAGE);
                    }

                }
            });
        }
        return jButton2;
    }

    /**
     * This method initializes jButton3 
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton3()
    {
        if (jButton3 == null)
        {
            jButton3 = new JButton();
            jButton3.setText("Cancel");
            jButton3.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    setVisible(false);
                }
            });
        }
        return jButton3;
    }

    public void addFileUpdateLister(FileUpdateLister lister)
    {
        listers.add(lister);
    }
    
    public void fireFileUpdate()
    {
        int size = model.getSize();
        List<String> files = new ArrayList<String>();
        for (int i = 0; i < size; i++)
        {
            files.add((String)(model.get(i)));
        }
        for (FileUpdateLister lister : listers)
        {
            lister.update(files);
        }
    }
    
    private void setLocation()
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Point location = new Point((screen.height - getHeight()) / 2,
                (screen.width - getWidth()) / 2);
        setLocation(location);
        
    }
}  //  @jve:decl-index=0:visual-constraint="10,10"
