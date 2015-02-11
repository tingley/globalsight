package com.globalsight.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClosableTabbedPane extends JTabbedPane {

    public ClosableTabbedPane() {
        super();
        super.addMouseListener(new TabbedPaneListener());
    }

    public Component add(int index, String title, Component component) {
        super.add(title, component);
        super.setIconAt(index, new CloseIcon());
        super.setSelectedIndex(index);
        return component;
    }

}
