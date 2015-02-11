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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

//import com.globalsight.everest.workflow.*;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.WindowUtil;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;

//for testing 
import java.util.Vector;



class DirectoryServicesPanel extends JPanel implements TreeSelectionListener, TreeExpansionListener, ActionListener, MouseListener
{
    private boolean hasTargetFile = false;
    private boolean firstSelection = true;
    private static final int ROOT_PATH = 0;
    private static final int FIRST_PATH = 1;
    private Object _selectedPath[];
    private DirectoryDialog _dd;
    private JTextField fileField;
    private JTextField pathField;
    private JTextField titleField;
    private JTree _directoryTreeView;
    private DefaultMutableTreeNode _twf;
    private DefaultMutableTreeNode _users;
    private DefaultMutableTreeNode _member;
    private JScrollPane _directoryViewScrollPane;
    private Dir dir;
    private MessageCatalog msgCat;
    private boolean m_bNodeIsLeaf = false;

    /**
       @roseuid 372F6DD70249
     */
    private Object[] trimEndPath(Object[] values)
    {
        int count = values.length;
        Object[] new_values = new Object[count-1];
        for (int i = 0; i < count-1; i++)
        {
            new_values[i] = values[i];
        }
        return new_values;
    }

    /**
       @roseuid 372F6DD7024B
     */
    protected void setTarget(boolean has_target)
    {
        hasTargetFile = has_target;
    }

    /**
       @roseuid 372F6DD7024D
     */
    public void updateFileField(TreePath path)
    {
        //##begin DirectoryServicesPanel::updateFileField%354963ED0298.body preserve=yes
        _selectedPath = path.getPath();
        if (path.getLastPathComponent() instanceof DefaultMutableTreeNode)
        {
            DefaultMutableTreeNode _node = (DefaultMutableTreeNode)(path.getLastPathComponent());
            if (!hasTargetFile)
            {
                m_bNodeIsLeaf = false;
                if ( _node.isLeaf() )
                {
                    _selectedPath = trimEndPath(_selectedPath);
                    String _name = path.getLastPathComponent().toString();
                    fileField.setEnabled(true);
                    fileField.setText(_name);
                    m_bNodeIsLeaf = true;
                }
                else if (_node.isRoot())
                {
                    return;
                }
                else
                {
                    fileField.setEnabled(true);
                    fileField.setText("");
                }
            }
            else
            {
                if ( _node.isLeaf() )
                {
                    _selectedPath = trimEndPath(_selectedPath);

                    fileField.setEnabled(true);
                }
                else if (_node.isRoot())
                {
                    return;
                }
                else
                {
                    fileField.setEnabled(true);
                    fileField.setText(fileField.getText()); //trigger the update in the path field
                    pathField.requestFocus();
                }
            }
        }

        String _path = getSelectedPath();
        if ( _path == null )
        {
            pathField.setText(null);
        }
        //else if ( _path.endsWith(DMS._pathSeparator) )  //Parag

        //commented June 8 2002
       /* else if ( _path.endsWith(WorkflowConstants._pathSeparator) )
        {
            pathField.setText(_path + fileField.getText());
        }
        else
        {
            // pathField.setText(_path + DMS._pathSeparator + fileField.getText());   //Parag
            pathField.setText(_path + WorkflowConstants._pathSeparator + fileField.getText());
        }*/

        _dd.setTitle(_path);
        //##end DirectoryServicesPanel::updateFileField%354963ED0298.body

    }

    public boolean isSelectedItemLeaf()
    {
        return m_bNodeIsLeaf;
    }

    /**
       @roseuid 372F6DD7024F
     */
    public void actionPerformed(ActionEvent e)
    {
        //comments June 8 2002
        /*  if (e.getSource() == fileField)
        {
            //if ( getSelectedPath().endsWith(DMS._pathSeparator) ) //Parag
            if ( getSelectedPath().endsWith(WorkflowConstants._pathSeparator) )
            {
                pathField.setText(getSelectedPath() + fileField.getText());
            }
            else
            {
                //pathField.setText(getSelectedPath() + DMS._pathSeparator + fileField.getText());
                pathField.setText(getSelectedPath() + WorkflowConstants._pathSeparator + fileField.getText());
            }

            titleField.setText(fileField.getText());
        }
        else if (e.getSource() == pathField)
        {
            String _path = pathField.getText();
            //if ( !_path.endsWith(DMS._pathSeparator) && (_path.indexOf(DMS._pathSeparator) != -1) )
            if ( !_path.endsWith(WorkflowConstants._pathSeparator) && (_path.indexOf(WorkflowConstants._pathSeparator) != -1) )
            {
                //titleField.setText(_path.substring(_path.lastIndexOf(DMS._pathSeparator) + 1));//Parag
                titleField.setText(_path.substring(_path.lastIndexOf(WorkflowConstants._pathSeparator) + 1));
            }
        }*/
    }

    /**
       @roseuid 372F6DD70251
     */
    private String getSelectedPath()
    {
        //##begin DirectoryDialog::getSelectedPath%35495C170343.body preserve=yes
       /* if (_selectedPath.length == 1 )
        {
            return null;
        }

        StringBuffer buf = new StringBuffer();
        //if ( (_selectedPath[1].toString()).endsWith(DMS._pathSeparator) ) //Parag
        if ( (_selectedPath[1].toString()).endsWith(WorkflowConstants._pathSeparator) )
        {
            buf.append(_selectedPath[1].toString());
        }
        else
        {
            // buf.append(_selectedPath[1].toString()).append(DMS._pathSeparator); //Parag
            buf.append(_selectedPath[1].toString()).append(WorkflowConstants._pathSeparator);
        }

        int count = _selectedPath.length;
        for (int i = 2; i < count-1; i++)
        {  // assumption: _path is always absolute
            //buf.append(_selectedPath[i].toString()).append(DMS._pathSeparator);
            buf.append(_selectedPath[i].toString()).append(WorkflowConstants._pathSeparator);
        }
        if (count > 2)
        {
            buf.append(_selectedPath[count-1].toString());
        }
        return buf.toString();
        //##end DirectoryDialog::getSelectedPath%35495C170343.body   */
        return "";

    }

    /**
       @roseuid 372F6DD70252
     */
    public DirectoryServicesPanel(DirectoryDialog dd)
    { //Dummy function returned by Parag
        msgCat = new MessageCatalog("com.globalsight.everest.webapp.applet.common.AppletResourceBundle");

        _dd = dd;
        fileField = _dd.getFileField();
        fileField.setEnabled(false);
        fileField.addActionListener(this);
        pathField = _dd.getPathField();
        pathField.addActionListener(this);
        titleField = _dd.getTitleField();
        setLayout( new GridLayout( 1, 0 ));

       // dir = new Dir(doc_repos);
        dir = new Dir();
        try
        {
            dir.loadDirectory();
        }
        catch (Exception e)
        {
        }

        _directoryTreeView = new JTree(dir.getNode());
        _directoryTreeView.addTreeSelectionListener(this);
        _directoryTreeView.addTreeExpansionListener(this);
        _directoryTreeView.addMouseListener(this);
        // allow single selection only
        _directoryTreeView.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        _directoryViewScrollPane = new JScrollPane(_directoryTreeView);
        add(_directoryViewScrollPane);

        _directoryTreeView.collapseRow(FIRST_PATH);
        _directoryTreeView.fireTreeExpanded(_directoryTreeView.getPathForRow(ROOT_PATH));
//    	_directoryTreeView.setSelectionPath(_directoryTreeView.getPathForRow(FIRST_PATH));
        validate();
        setVisible( true );
    } 


    /*public DirectoryServicesPanel(DocRepository doc_repos, DirectoryDialog dd) {
        msgCat = new MessageCatalog("com.fujitsu.iflow.common.locale.Planview");

        _dd = dd;
        fileField = _dd.getFileField();
        fileField.setEnabled(false);
        fileField.addActionListener(this);
        pathField = _dd.getPathField();
        pathField.addActionListener(this);
        titleField = _dd.getTitleField();
        setLayout( new GridLayout( 1, 0 ));

        dir = new Dir(doc_repos);
        try
        {
            dir.loadDirectory();
        }
        catch (ModelInternalException e)
        {
            Log.println(Log.LEVEL0, "DirectoryServicesPanel.DirectoryServicesPanel(): " + e.getMessage()); 
            WindowUtil.showMsgDlg(WindowUtil.getFrame(this), 
                                  msgCat.getMsg( ErrorCodes.getMsg((e.getErrorCode())) ), 
                                  msgCat.getMsg("Internal Error!"), 
                                  WindowUtil.TYPE_ERROR);
        }
        catch (DMSException e)
        {
            Log.println(Log.LEVEL0, "DirectoryServicesPanel.DirectoryServicesPanel(): " + e.getMessage()); 
            WindowUtil.showMsgDlg(WindowUtil.getFrame(this), 
                                  msgCat.getMsg( ErrorCodes.getMsg((e.getErrorCode())) ), 
                                  msgCat.getMsg("DMS Error!"), 
                                  WindowUtil.TYPE_ERROR);
        }

        _directoryTreeView = new JTree(dir.getNode());
        _directoryTreeView.addTreeSelectionListener(this);
        _directoryTreeView.addTreeExpansionListener(this);
        _directoryTreeView.addMouseListener(this);
        // allow single selection only
        _directoryTreeView.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        _directoryViewScrollPane = new JScrollPane(_directoryTreeView);
        add(_directoryViewScrollPane);

        _directoryTreeView.collapseRow(FIRST_PATH);
        _directoryTreeView.fireTreeExpanded(_directoryTreeView.getPathForRow(ROOT_PATH));
//    	_directoryTreeView.setSelectionPath(_directoryTreeView.getPathForRow(FIRST_PATH));
        validate();
        setVisible( true );
    }  */

    /**
       @roseuid 372F6DD70255
     */
    public void mousePressed(MouseEvent e)
    {
    }

    /**
       @roseuid 372F6DD70257
     */
    public void mouseReleased(MouseEvent e)
    {
    }

    /**
       @roseuid 372F6DD70259
     */
    public void mouseEntered(MouseEvent e)
    {
    }

    /**
       @roseuid 372F6DD7025B
     */
    public void mouseExited(MouseEvent e)
    {
    }

    /**
       @roseuid 372F6DD7025D
     */
    public void mouseClicked(MouseEvent e)
    {
        //since the first directory under root is selected by default,
        //when there is a targetFile to be appended (e.g., from new attachment)
        //if user does not select any other tree path but the first sub-dir,
        //no TreeSelectionEvent will be fired and received by this listener
        //therefore, this mouseEvent had to be added for this special case
        if ( !firstSelection || !hasTargetFile )
        {
            return;
        }

        int selRow = _directoryTreeView.getRowForLocation(e.getX(), e.getY());
        TreePath selPath = _directoryTreeView.getPathForLocation(e.getX(), e.getY());
        if (selRow == FIRST_PATH)
        {

            updateFileField(selPath);
            firstSelection = false;
        }
    }

    /**
       @roseuid 372F6DD7025F
     */
    public Dimension getMinimumSize()
    {
        //##begin DirectoryServicesPanel::getMinimumSize%354963ED02A1.body preserve=yes
        return new Dimension( 400, 300 );
        //##end DirectoryServicesPanel::getMinimumSize%354963ED02A1.body

    }

    /**
       @roseuid 372F6DD70260
     */
    public Dimension getPreferredSize()
    {
        //##begin DirectoryServicesPanel::getPreferredSize%354963ED02A2.body preserve=yes
        return getMinimumSize();
        //##end DirectoryServicesPanel::getPreferredSize%354963ED02A2.body

    }

    /**
       @roseuid 372F6DD70261
     */
    public void valueChanged(TreeSelectionEvent tse)
    {
        //##begin DirectoryServicesPanel::valueChanged%354963ED02A3.body preserve=yes
        updateFileField(tse.getNewLeadSelectionPath());
        repaint();
        //##end DirectoryServicesPanel::valueChanged%354963ED02A3.body

    }


    /**
       @roseuid 372F6DD70263
     */
    public void treeExpanded(TreeExpansionEvent _tee)
    {
        if ((_tee.getPath()).getLastPathComponent() instanceof DefaultMutableTreeNode)
        {
            DefaultMutableTreeNode _node = (DefaultMutableTreeNode)((_tee.getPath()).getLastPathComponent());
            //Folder _folder = (Folder)(_node.getUserObject());
            Vector _folder = (Vector)(_node.getUserObject());

             try
            {
                //dir.loadDirectory(_folder.getContents(), 2, _node);
               
                dir.loadDirectory(_folder.elements(), 2, _node);
            }
            catch (Exception e)
            {
                /*//WindowUtil.showMsgDlg(WindowUtil.getFrame(this), 
                //msgCat.getMsg( ErrorCodes.getMsg((e.getErrorCode())) ), 
                msgCat.getMsg("Internal Error!"), 
                WindowUtil.TYPE_ERROR); */
            }
            /*catch (ModelInternalException e)
        {
            Log.println(Log.LEVEL0, "DirectoryServicesPanel.treeExpanded(): ModelInternalException " + e.getMessage()); 
            WindowUtil.showMsgDlg(WindowUtil.getFrame(this), 
                                  msgCat.getMsg( ErrorCodes.getMsg((e.getErrorCode())) ), 
                                  msgCat.getMsg("Internal Error!"), 
                                  WindowUtil.TYPE_ERROR);
            Log.printStack(Log.LEVEL0, e); 
        }
        catch (DMSException e)
        {
            Log.println(Log.LEVEL0, "DirectoryServicesPanel.treeExpanded(): MDSException " + e.getMessage()); 
            WindowUtil.showMsgDlg(WindowUtil.getFrame(this), 
                                  msgCat.getMsg( ErrorCodes.getMsg((e.getErrorCode())) ), 
                                  msgCat.getMsg("DMS Error!"), 
                                  WindowUtil.TYPE_ERROR);
            Log.printStack(Log.LEVEL0, e); 
        } */
        }
    }

    /**
       @roseuid 372F6DD70265
     */
    public void treeCollapsed(TreeExpansionEvent tee)
    {
        _directoryViewScrollPane.validate();
    }

    /**
       @roseuid 372F6DD70267
     */
    public Object[] getPath()
    {
        //##begin DirectoryServicesPanel::getPath%354963ED02A5.body preserve=yes
        return _selectedPath;
        //##end DirectoryServicesPanel::getPath%354963ED02A5.body

    }
}
