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
package galign.ui.control;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import galign.data.ProjectNode;
import galign.ui.control.Start;

public class TreePopupListener extends MouseAdapter {

    JPopupMenu popup = null;
    Start start;

    public TreePopupListener(JPopupMenu popup, Start start) 
    {
        this.popup = popup;
        this.start = start;
    }

    public void mousePressed(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e)
    {

        if (e.isPopupTrigger())
        {
            JTree tree = (JTree) e.getComponent();

            // get the node at location x, y
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if (path == null) return;

            ProjectNode node = (ProjectNode) path.getLastPathComponent();
            if (node.getNodeType() != ProjectNode.PAGE_NODE)
                return;
            start.setSelectedAlignment(node);
            start.updateAlignPopups();
            tree.addSelectionPath(path);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
