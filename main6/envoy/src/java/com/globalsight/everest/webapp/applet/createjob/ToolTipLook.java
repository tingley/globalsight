package com.globalsight.everest.webapp.applet.createjob;

import java.awt.Font;

import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class ToolTipLook extends MetalLookAndFeel
{
    protected void initSystemColorDefaults(UIDefaults table)
    {
        super.initSystemColorDefaults(table);
        table.put("info", new ColorUIResource(255, 255, 255));
    }

    protected void initComponentDefaults(UIDefaults table)
    {
        super.initComponentDefaults(table);
        Font font = new Font("Tahoma", Font.PLAIN, 12); 
        table.put("ToolTip.font", font);
    }
}