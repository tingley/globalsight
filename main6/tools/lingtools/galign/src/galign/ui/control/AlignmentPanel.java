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

import java.io.*;
import java.util.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import galign.Setup;
import galign.ui.view.SelectedAlignmentPnl;
import galign.ui.view.GAlignFrm;
import galign.helpers.AlignmentMapping;
import galign.helpers.tmx.TmxFile;
import galign.helpers.tmx.Tuv;
import galign.helpers.tmx.Tu;
import galign.helpers.ui.GridBagConstraints;
import galign.helpers.ui.GridBagLayout;
import galign.helpers.util.EditUtil;
import galign.helpers.util.GAlignException;
import galign.helpers.util.StopWatch;


/**
 * Implements the Alignment Panel.  It contains textareas and labels
 * (the images are labels with images.)
 */
public class AlignmentPanel
    extends JPanel
    implements MouseListener, ActionListener, DocumentListener,
               MouseMotionListener
{
    //
    // Private classes
    //

    public class AlignmentTextArea extends JTextArea
    {
        public Tu tu;

        public AlignmentTextArea(Tu tu)
        {
            super();
            setBackground(Color.lightGray);
            setEditable(false);
            setColumns(32);
            setLineWrap(true);
            setWrapStyleWord(true);

            this.tu = tu;
            // only 1 tuv allowed in this release
            Tuv tuv = (Tuv) tu.getTuvs().get(0);

            if (EditUtil.isRTLLocale(tuv.getLanguage()))
            {
                setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            }
            else
            {
                setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            }

            setText(tuv.getText());
            // The following is for performance reasons
            setCaret (new javax.swing.text.DefaultCaret() {
                    protected void adjustVisibility(java.awt.Rectangle nloc) {}
                });
        }

        // overriding getPreferredSize allows the component to get smaller
        public Dimension getPreferredSize() {
            return new Dimension(200, super.getPreferredSize().height);
        }
    }

    public class AlignmentImage extends JLabel implements Comparable
    {
        public static final String SOURCE = "S";
        public static final String TARGET = "T";

        public static final int STATE_PENDING_APPROVAL = 0;
        public static final int STATE_APPROVED = 1;
        public static final int STATE_NOTHING = 2;

        public Tu m_data;
        public AlignmentTextArea m_ta;
        // either source or target
        public String m_type;
        // number of approved alignments
        private int m_approved_count = 0;
        // number of pending approved alignments
        private int m_pending_approval_count = 0;
        private int m_state;

        public AlignmentImage(AlignmentTextArea ta, ImageIcon image, String type)
        {
            super(image);

            m_ta = ta;
            m_data = m_ta.tu;
            m_type = type;
            m_state = STATE_NOTHING;
        }

        public void incrementPendingApproval()
        {
            m_pending_approval_count++;
            updateState();
        }

        public void decrementPendingApproval()
        {
            m_pending_approval_count--;
            updateState();
        }

        public void incrementApproved()
        {
            m_approved_count++;
            updateState();
        }

        public void decrementApproved()
        {
            m_approved_count--;
            updateState();
        }

        public void clearState()
        {
            m_approved_count = 0;
            m_pending_approval_count = 0;
            updateState();
        }

        private void updateState()
        {
            if (m_approved_count > 0)
            {
                m_state = STATE_APPROVED;
                setIcon(m_approvedImage);
            }
            else if (m_pending_approval_count > 0)
            {
                m_state = STATE_PENDING_APPROVAL;
                setIcon(m_notApprovedImage);
            }
            else
            {
                m_state = STATE_NOTHING;
                setIcon(m_notApprovedImage);
            }
        }

        public int getApprovedCount()
        {
            return m_approved_count;
        }

        public int getState()
        {
            return m_state;
        }

        public int compareTo(Object p_obj)
        {
            AlignmentImage bImage = (AlignmentImage)p_obj;
            int a =  Integer.parseInt(m_data.getId());
            int b =  Integer.parseInt(bImage.m_data.getId());
            if (a == b) return 0;
            if (a < b) return -1;
            return 1;
        }
    }

    public class Line implements Comparable
    {
        public AlignmentImage start;
        public AlignmentImage end;
        public boolean approved;
        public boolean hilite = false;

        public Line(AlignmentImage start, AlignmentImage end, boolean approved)
        {
            this.start = start;
            this.end = end;
            this.approved = approved;
            if (approved)
            {
                start.incrementApproved();
                end.incrementApproved();
            }
            else
            {
                start.incrementPendingApproval();
                end.incrementPendingApproval();
            }
        }

        public void connect()
        {
            start.incrementApproved();
            end.incrementApproved();
            if (!approved)
            {
                start.decrementPendingApproval();
                end.decrementPendingApproval();
            }
            approved = true;
        }

        public void hilite()
        {
            hilite = true;
            start.setBorder(redBorder);
            end.setBorder(redBorder);
        }

        public void removeHilite()
        {
            hilite = false;
            start.setBorder(null);
            end.setBorder(null);
        }

        public boolean equals(Object p_obj)
        {
            Line bLine = (Line)p_obj;
            return (start.m_data.getId().equals(bLine.start.m_data.getId()) &&
                end.m_data.getId().equals(bLine.end.m_data.getId()));
        }

        public int compareTo(Object p_obj)
        {
            Line bLine = (Line)p_obj;
            int rv = this.start.m_data.getId().compareTo(bLine.start.m_data.getId());
            if (rv == 0)
                return this.end.m_data.getId().compareTo(bLine.end.m_data.getId());
            else
                return rv;
        }

        public void draw(Graphics g)
        {
            Graphics2D g2 = (Graphics2D)g;
            if (!approved)
                g2.setColor(Color.gray);
            else
                g2.setColor(Color.green.darker());
            int startx = (new Double(start.getLocation().getX())).intValue();
            int starty = (new Double(start.getLocation().getY())).intValue();
            int endx = (new Double(end.getLocation().getX())).intValue();
            int endy = (new Double(end.getLocation().getY())).intValue();
            int startxpadding = (int)start.getSize().getWidth();
            int startypadding = (int)start.getSize().getHeight()/2;
            int endxpadding = (int)end.getSize().getWidth();
            int endypadding = (int)end.getSize().getHeight()/2;

            if (startx < endx)
            {
                g.drawLine(startx + startxpadding, starty + startypadding,
                    endx, endy + endypadding);
            }
            else
            {
                g.drawLine(startx, starty + startypadding,
                    endx + endxpadding, endy + endypadding);
            }
        }
    }

    private ImageIcon m_notApprovedImage;   // the "not approved" image
    private ImageIcon m_approvedImage;      // the "approved" image
    private Stroke m_basicStroke = new BasicStroke();  // stroke for solid line
    private Border redBorder = BorderFactory.createLineBorder(Color.red);
    private JPopupMenu m_popupMenu = new JPopupMenu(); // popup menu on images
    private ArrayList m_lines;              // list of lines
    private AlignmentImage m_start;         // user mousepressed down
    private AlignmentImage m_end;           // user mousepressed up
    private AlignmentImage m_selectedImage; // right mouse click
    private AlignmentMapping m_mapping;     // GAM file
    private HashMap m_srcImages;
    private HashMap m_targImages;
    private TreeSet hilited = new TreeSet();
    private SelectedAlignmentPnl m_selectedPanel;

    public boolean m_changed;

    public AlignmentPanel(SelectedAlignmentPnl p_selectedPanel)
    {
        setBackground(Color.white);
        m_selectedPanel = p_selectedPanel;

        GridBagLayout gridbag = new GridBagLayout();
        setLayout(gridbag);

        // Fetch alignment images
        getImages();

        // Create popup menu
        prepareMenus();

        // Listen for mouse events in order to draw lines
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Given the source and target Tmx Files and the GAM file, create textareas
     * and dotted lines for pre alignments.
     */
    public void init(AlignmentMapping p_mapping)
    {
        init(p_mapping, 0, 0);
    }

    public void init(AlignmentMapping p_mapping, int srcStart, int targStart)
    {
        m_mapping = p_mapping;
        m_start = null;
        m_end = null;
        m_selectedImage = null;
        m_changed = false;
        removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        ArrayList srcTus = m_mapping.getSourceTmx().getTus();
        ArrayList targTus = m_mapping.getTargetTmx().getTus();

        // add textareas and image for source segments
        initSource(srcTus, c, srcStart);

        // add textareas for source segments
        initTarget(targTus, c, targStart);

        // add padding between the two segments (hack!)
        for (int i = 0; i < srcTus.size(); i++)
        {
            JLabel blank = new JLabel("                          ");
            c.gridx = 2;
            c.gridy = i;
            add(blank, c);
        }

        // add pre alignment lines
        initLines();
    }

    /**
     * Save the gam file and the tmx files.
     */
    public void save()
        throws IOException, GAlignException
    {
        m_mapping.save();
        m_changed = false;
    }

    /**
     * Approve all unapproved alignments.
     */
    public void approveAll()
    {
        for (int i = 0; i < m_lines.size(); i++)
        {
            Line line = (Line)m_lines.get(i);
            line.start.clearState();
            line.end.clearState();
            line.connect();
            m_mapping.approveAlignRecord(line.start.m_data.getId(),
                line.end.m_data.getId());
        }
        m_changed = true;
    }

    /**
     * Save the gam file and the tmx files.
     */
    public void exportAsTmx(String p_fileName)
        throws IOException
    {
        m_mapping.exportAsTmx(p_fileName);
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < m_lines.size(); i++)
        {
            Line line = (Line)m_lines.get(i);
            line.draw(g);
        }
    }

    /**
     * Actions performed from popup menu
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals(Setup.getString("menu.disconnect")))
        {
            disconnect(m_selectedImage);
            removeHilites();
            m_changed = true;
        }
        else if (e.getActionCommand().equals(Setup.getString("menu.approve")))
        {
            removeHilites();
            TreeSet approved = new TreeSet();
            connect(m_selectedImage, approved);
            m_start = m_selectedImage;
            doHilites();
            m_selectedPanel.updateText(hilited);
            m_changed = true;
        }
        else if (e.getActionCommand().equals(Setup.getString("menu.removeSegment")))
        {
            // Remove Segment
            m_mapping.createRemoveRecord(m_selectedImage.m_data.getId(),
                m_selectedImage.m_type);
            init(m_mapping);  // to redraw layout or else space is left
            getParent().validate();
            removeHilites();
            m_changed = true;
        }
        else
        {
            // Line up segments
            lineUpSegments();
        }
        repaint();
    }

    /**
     * This is needed to determine where to scroll if the user wants to
     * line up the segments and some segments have been removed.  But it
     * only matters if the removed segments are above the ones to line up.
     */
    private int getRemovedRecordCount(int src, int targ)
    {
        int count = 0;

        ArrayList list = m_mapping.getRemoveRecords();
        for (int i =0; i < list.size(); i++)
        {
            AlignmentMapping.Remove r = (AlignmentMapping.Remove)list.get(i);
            if (r.isSource())
            {
                if (Integer.parseInt(r.m_id) < src)
                {
                    count++;
                }
            }
            else
            {
                if (Integer.parseInt(r.m_id) < targ)
                {
                    count--;
                }
            }
        }
        return count;
    }

    private void connect(AlignmentImage p_image, TreeSet approved)
    {
        // Update images and data to approved
        for (int i = 0; i < m_lines.size(); i++)
        {
            AlignmentImage next = null;
            Line line = (Line)m_lines.get(i);
            if (approved.contains(line)) continue;
            if (p_image.m_type.equals(AlignmentImage.SOURCE))
            {
                if (line.start.equals(p_image))
                {
                    next = line.end;
                    line.connect();
                    m_mapping.approveAlignRecord(line.start.m_data.getId(),
                        line.end.m_data.getId());
                }
            }
            else
            {
                if (line.end.equals(p_image))
                {
                    next = line.start;
                    line.connect();
                    m_mapping.approveAlignRecord(line.start.m_data.getId(),
                        line.end.m_data.getId());
                }
            }
            if (next != null)
            {
                line.start.setBorder(redBorder);
                line.end.setBorder(redBorder);
                approved.add(line);
                connect(next, approved);
            }
        }
    }

    /*
     * Follow all the lines starting at "p_image" and disconnect them.
     */
    private void disconnect(AlignmentImage p_image)
    {
        for (int i = 0; i < m_lines.size(); i++)
        {
            AlignmentImage next = null;
            Line line = (Line)m_lines.get(i);
            if (p_image.m_type.equals(AlignmentImage.SOURCE))
            {
                if (line.start.equals(p_image))
                {
                    next = line.end;
                    m_mapping.removeAlignment(line.start.m_data.getId(),
                        line.end.m_data.getId());
                }
            }
            else
            {
                if (line.end.equals(p_image))
                {
                    next = line.start;
                    m_mapping.removeAlignment(line.start.m_data.getId(),
                        line.end.m_data.getId());
                }
            }
            if (next != null)
            {
                line.start.setBorder(null);
                line.end.setBorder(null);
                line.start.clearState();
                line.end.clearState();
                m_lines.remove(line);
                i--;
                disconnect(next);
            }
        }
    }

    private void lineUpSegments()
    {
        // First determine how far down either the source or target data
        // must start in the page.
        boolean save_m_changed = m_changed;
        String saveId = m_selectedImage.m_data.getId();
        int start = 0;
        if (m_selectedImage.m_type.equals(AlignmentImage.TARGET))
        {
            for (int i = 0; i < m_lines.size(); i++)
            {
                Line line = (Line)m_lines.get(i);
                if (line.end.equals( m_selectedImage))
                {
                    int x = Integer.parseInt(line.start.m_data.getId());
                    int y = Integer.parseInt(m_selectedImage.m_data.getId());
                    start = x - y - getRemovedRecordCount(x, y);
                    break;
                }
            }
            // Redraw the page
            if (start < 0)
                init(m_mapping, Math.abs(start), 0);
            else
                init(m_mapping, 0, start);
            m_start = (AlignmentImage)m_targImages.get(saveId);
        }
        else
        {
            for (int i = 0; i < m_lines.size(); i++)
            {
                Line line = (Line)m_lines.get(i);
                if (line.start.equals(m_selectedImage))
                {
                    int x = Integer.parseInt(m_selectedImage.m_data.getId());
                    int y = Integer.parseInt(line.end.m_data.getId());
                    start = x - y - getRemovedRecordCount(x, y);
                    break;
                }
            }
            // Redraw the page
            if (start < 0)
                init(m_mapping, Math.abs(start), 0);
            else
                init(m_mapping, 0, start);
            m_start = (AlignmentImage)m_srcImages.get(saveId);
        }

        // Scroll so that the lined up segments are in the middle of the page
        Rectangle r = new Rectangle(2, 2, 1, 1);
        scrollRectToVisible(r);
        doHilites();
        // not sure why scrollRectToVisible must be called twice but
        // if you remove one, it won't work!
        r = new Rectangle(m_start.getX(), m_start.getY(), 1, 1);
        scrollRectToVisible(r);
        int center = new Double(getVisibleRect().getHeight()).intValue() / 2;
        r = new Rectangle(m_start.getX(), m_start.getY() + center, 1, 1);
        scrollRectToVisible(r);

        m_changed = save_m_changed;
    }

    /*
     * Begin listeners for MouseListener
     */

    public void mousePressed(MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON1)
        {
            m_start = getComponent(e);
            if (m_start == null) return;
        }
        else if (e.isPopupTrigger())
        {
            updateMenu();
            m_popupMenu.show(e.getComponent(), e.getX(), e.getY());
            m_selectedImage =  getComponent(e);
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        m_end = getComponent(e);
        if (m_end == null) return;

        if (e.getButton() == MouseEvent.BUTTON1)
        {
            if (validLine(m_start, m_end))
            {
                addLine();
                // update UI with new line
                m_changed = true;
                repaint();
                doHilites();
            }
            else if (sameImage(m_start, m_end))
            {
                doHilites();
            }
        }
        else if (e.isPopupTrigger())
        {
            updateMenu();
            m_popupMenu.show(e.getComponent(), e.getX(), e.getY());
            m_selectedImage = m_end;
        }
    }


    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    private void addLine()
    {
        if (m_start.m_type.equals(AlignmentImage.SOURCE))
        {
            for (int i = 0; i < m_lines.size(); i++)
            {
                Line line = (Line)m_lines.get(i);
                if (line.start.m_data.getId().equals(m_start.m_data.getId()) &&
                    line.end.m_data.getId().equals(m_end.m_data.getId()))
                {
                    // change from pending to approved
                    line.connect();
                    m_mapping.approveAlignRecord(line.start.m_data.getId(),
                        line.end.m_data.getId());
                    return;
                }
            }
            m_lines.add(new Line(m_start, m_end, true));
            m_mapping.approveAlignRecord(m_start.m_data.getId(), m_end.m_data.getId());
        }
        else
        {
            for (int i = 0; i < m_lines.size(); i++)
            {
                Line line = (Line)m_lines.get(i);
                if (line.start.m_data.getId().equals(m_end.m_data.getId()) &&
                    line.end.m_data.getId().equals(m_start.m_data.getId()))
                {
                    // change from pending to approved
                    line.connect();
                    m_mapping.approveAlignRecord(line.start.m_data.getId(),
                        line.end.m_data.getId());
                    return;
                }
            }
            m_lines.add(new Line(m_end, m_start, true));
            m_mapping.approveAlignRecord(m_end.m_data.getId(), m_start.m_data.getId());
        }
    }

    /*
     * Hilite the starting segment and any segments it's "connected" to.
     * Then for the "connected" to segments, see if they are connected to
     * others too.  All of them together make up one alignment.
     */
    private void doHilites()
    {
        removeHilites();
        hilited = new TreeSet();
        updateHiliteList(m_start);
        m_selectedPanel.updateText(hilited);
    }

    private void updateHiliteList(AlignmentImage image)
    {
        for (int i = 0; i < m_lines.size(); i++)
        {
            Line line = (Line)m_lines.get(i);
            if (image.m_type.equals(AlignmentImage.SOURCE))
            {
                if (line.start.equals(image))
                {
                    line.hilite();
                    if (hilited.add(line))
                    {
                        updateHiliteList(line.end);
                    }
                }
            }
            else
            {
                if (line.end.equals(image))
                {
                    line.hilite();
                    if (hilited.add(line))
                    {
                        updateHiliteList(line.start);
                    }
                }
            }
        }
    }

    /*
     * Remove the hilite around segments.
     */
    private void removeHilites()
    {
        Iterator iter = hilited.iterator();
        while (iter.hasNext())
        {
            Line line = (Line)iter.next();
            line.removeHilite();
        }
        m_selectedPanel.clear();
    }

    /*
     * End listeners for  MouseListener
     */

    /*
     * Begin listeners for MouseMotionListener
     */

    public void mouseDragged(MouseEvent e)
    {
        //The user is dragging us, so scroll!
        Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
        scrollRectToVisible(r);
    }

    public void mouseMoved(MouseEvent e) {}

    /*
     * End listeners for MouseMotionListener
     */

    /*
     * Listeners for the TextArea.  (not used at the moment because
     * we disabled editing.
     */
    public void insertUpdate(DocumentEvent e)
    {
        updateData(e);
    }

    public void removeUpdate(DocumentEvent e)
    {
        updateData(e);
    }

    public void changedUpdate(DocumentEvent e) {}

    private void updateData(DocumentEvent e)
    {
        try
        {
            Document doc = (Document)e.getDocument();
            Tu tu = (Tu) doc.getProperty("tu");
            Tuv tuv = (Tuv) tu.getTuvs().get(0); // only 1 tuv allowed in this release
            tuv.setText(doc.getText(0, doc.getLength()));
            m_changed = true;
        }
        catch (Exception ex)
        {
        }
    }

    /**
     *  Enable/disable Approve/Disconnect/Line Up/Remove appropriately.
     */
    private void updateMenu()
    {
        // update menu before showing
        MenuElement[] melements = m_popupMenu.getSubElements();

        //if (m_end.getState() == AlignmentImage.STATE_APPROVED)
        if (m_end.m_pending_approval_count > 0)
        {
            ((JMenuItem)melements[0]).setEnabled(true);
            ((JMenuItem)melements[1]).setEnabled(true);
            ((JMenuItem)melements[2]).setEnabled(true);
            ((JMenuItem)melements[3]).setEnabled(false);
        }
        else if (m_end.m_approved_count > 0)
        {
            ((JMenuItem)melements[0]).setEnabled(false);
            ((JMenuItem)melements[1]).setEnabled(true);
            ((JMenuItem)melements[2]).setEnabled(true);
            ((JMenuItem)melements[3]).setEnabled(false);
        }
        else
        {
            ((JMenuItem)melements[0]).setEnabled(false);
            ((JMenuItem)melements[1]).setEnabled(false);
            ((JMenuItem)melements[2]).setEnabled(false);
            ((JMenuItem)melements[3]).setEnabled(true);
        }
    }

    /*
     * Initialize all source text areas and images
     */
    private void initSource(ArrayList srcTus, GridBagConstraints c, int start)
    {
        c.gridx = 0;
        int gridy = 0;
        for (int i = 0; i < start; i++)
        {
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.gridy = i;
            gridy = i;
            c.insets = new Insets(2,2,2,2);
            add(new JLabel(" "), c);
        }
        if (start > 0) gridy++;
        m_srcImages = new HashMap(srcTus.size());
        for (int i = 0; i < srcTus.size(); i++)
        {
            Tu tu = (Tu)srcTus.get(i);
            if (m_mapping.isInRemoveList(tu.getId(), "S"))
            {
                continue;
            }
            AlignmentTextArea ta = createTextArea(c, tu, 0, gridy);
            AlignmentImage aImage = createImage(c, ta, AlignmentImage.SOURCE, 1, gridy);
            m_srcImages.put(aImage.m_data.getId(), aImage);
            gridy++;
        }
    }

    /*
     * Initialize all target text areas and images
     */
    private void initTarget(ArrayList targTus, GridBagConstraints c, int start)
    {
        c.gridx = 3;
        int gridy = 0;
        for (int i = 0; i < start; i++)
        {
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.gridy = i;
            gridy = i;
            c.insets = new Insets(2,2,2,2);
            add(new JLabel(" "), c);
        }
        if (start > 0) gridy++;
        m_targImages = new HashMap(targTus.size());
        for (int i = 0; i < targTus.size(); i++)
        {
            Tu tu = (Tu)targTus.get(i);
            if (m_mapping.isInRemoveList(tu.getId(), "T"))
            {
                continue;
            }
            AlignmentTextArea ta = createTextArea(c, tu, 4, gridy);
            AlignmentImage aImage = createImage(c, ta, AlignmentImage.TARGET, 3, gridy);
            m_targImages.put(aImage.m_data.getId(), aImage);
            gridy++;
        }
    }

    private AlignmentTextArea createTextArea(GridBagConstraints c, Tu tu,
        int gridx, int gridy)
    {
        AlignmentTextArea ta = new AlignmentTextArea(tu);
        /* don't need this because took out editing
           ta.getDocument().addDocumentListener(this);
           ta.getDocument().putProperty("tu", tu);
           JScrollPane jsp = new JScrollPane(ta);
        */
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridx = gridx;
        c.gridy = gridy;
        c.insets = new Insets(2,2,2,2);
        add(ta, c);
        return ta;
    }

    private AlignmentImage createImage(GridBagConstraints c, AlignmentTextArea ta,
        String type, int gridx, int gridy)
    {
        AlignmentImage aImage = new AlignmentImage
            (ta, m_notApprovedImage, type);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.5;
        c.gridx = gridx;
        c.gridy = gridy;
        add(aImage, c);
        return aImage;
    }

    private void initLines()
    {
        ArrayList alignList = m_mapping.getAlignRecords();
        m_lines = new ArrayList();
        for (int i = 0; i < alignList.size(); i++)
        {
            AlignmentMapping.Align align = (AlignmentMapping.Align)alignList.get(i);

            // Find the alignment images with these id's
            AlignmentImage src = (AlignmentImage)m_srcImages.get(align.m_source);
            AlignmentImage targ = (AlignmentImage)m_targImages.get(align.m_target);
            m_lines.add(new Line(src, targ, align.isApproved()));
        }
    }


    /**
     * A valid line is one that goes from a src to target.  Invalid is src
     * to src or target to target.
     */
    private boolean validLine(Component start, Component end)
    {
        int startx = (new Double(start.getLocation().getX())).intValue();
        int endx = (new Double(end.getLocation().getX())).intValue();
        if (startx == endx)
            return false;
        return true;
    }

    private boolean sameImage(Component start, Component end)
    {
        if (start.equals(end))
            return true;
        else
            return false;
    }

    private AlignmentImage getComponent(MouseEvent e)
    {
        Component c = getComponentAt(e.getX(), e.getY());
        if (c instanceof AlignmentImage)
        {
            AlignmentImage l = (AlignmentImage) c;
            if (l.getIcon() != null)
                return l;
        }
        return null;
    }

    private void prepareMenus()
    {
        JMenuItem mi = new JMenuItem(Setup.getString("menu.approve"));
        mi.addActionListener(this);
        m_popupMenu.add(mi);
        mi = new JMenuItem(Setup.getString("menu.disconnect"));
        mi.addActionListener(this);
        m_popupMenu.add(mi);
        mi = new JMenuItem("Line up Segments");
        mi.addActionListener(this);
        m_popupMenu.add(mi);
        mi = new JMenuItem(Setup.getString("menu.removeSegment"));
        mi.addActionListener(this);
        m_popupMenu.add(mi);
    }

    private void getImages()
    {

        m_notApprovedImage = new ImageIcon(
            getClass().getResource("/resources/alignment.gif"));
        m_approvedImage = new ImageIcon(
            getClass().getResource("/resources/alignmentApproved.gif"));
    }

    private Stroke createDashStroke()
    {
        Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND, 10,
            new float[] { 4, 4 }, 0);
        return stroke;
    }

}

