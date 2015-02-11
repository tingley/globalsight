package com.globalsight.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class MaxLengthDocument extends PlainDocument
{
    private static final long serialVersionUID = 4524789108435105301L;
    private int maxLength = 20;
    
    public MaxLengthDocument(int length)
    {
        super();
        maxLength = length;
    }

    public void insertString(int offset, String s,
            AttributeSet attributeSet) throws BadLocationException
    {
        if (s == null || offset < 0)
        {
            return;
        }

        for (int i = 0; i < s.length(); i++)
        {
            if (getLength() > maxLength - 1)
            {
                break;
            }
            super.insertString(offset + i, s.substring(i, i + 1),
                    attributeSet);
        }
        return;
    }
}
