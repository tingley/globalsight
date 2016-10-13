package com.globalsight.ui.attribute;

import javax.swing.JTextField;

import com.globalsight.ui.attribute.vo.TextJobAttributeVo;

public class StringTextField extends JTextField
{
    private static final long serialVersionUID = -8997184097509009512L;
    private TextJobAttributeVo textAttribute;
    public TextJobAttributeVo getTextAttribute()
    {
        return textAttribute;
    }
    public void setTextAttribute(TextJobAttributeVo textAttribute)
    {
        this.textAttribute = textAttribute;
    }
}
