package com.globalsight.cxe.adapter;

import com.globalsight.cxe.message.CxeMessage;

public interface IConverterHelper
{
    public CxeMessage[] performConversion();
    public CxeMessage performConversionBack();
}
