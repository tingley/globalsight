package com.globalsight.ui.attribute;

import javax.swing.JTextField;

import com.globalsight.ui.attribute.vo.FloatJobAttributeVo;

public class FloatTextField extends JTextField
{
    private static final long serialVersionUID = 3141541147951526027L;
    private FloatJobAttributeVo floatAttribute;

    public FloatJobAttributeVo getFloatAttribute()
    {
        return floatAttribute;
    }

    public void setFloatAttribute(FloatJobAttributeVo floatAttribute)
    {
        this.floatAttribute = floatAttribute;
    }
}
