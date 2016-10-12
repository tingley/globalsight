//                              -*- Mode: Java -*-
//
// Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
//

package debex.ui.view;

public class AboutDlg extends javax.swing.JDialog
{

    /** Creates new form AboutDlg */
    public AboutDlg(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        lb_content = new javax.swing.JLabel();

        getContentPane().setLayout(new java.awt.BorderLayout(5, 5));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About Debex");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                closeDialog(evt);
            }
        });

        lb_content.setText("jLabel1");
        getContentPane().add(lb_content, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void closeDialog (java.awt.event.WindowEvent evt)//GEN-FIRST:event_closeDialog
    {//GEN-HEADEREND:event_closeDialog
        // TODO add your handling code here:
    }//GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel lb_content;
    // End of variables declaration//GEN-END:variables

}
