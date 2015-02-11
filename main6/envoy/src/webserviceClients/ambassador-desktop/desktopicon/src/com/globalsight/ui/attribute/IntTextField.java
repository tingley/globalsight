package com.globalsight.ui.attribute;

import javax.swing.JTextField;

import com.globalsight.ui.attribute.vo.IntJobAttributeVo;

public class IntTextField extends JTextField
{
    private static final long serialVersionUID = -8997184097509009512L;
    private IntJobAttributeVo intAttribute;
    public IntJobAttributeVo getIntAttribute()
    {
        return intAttribute;
    }
    public void setIntAttribute(IntJobAttributeVo intAttribute)
    {
        this.intAttribute = intAttribute;
    }
}
