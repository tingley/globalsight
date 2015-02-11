package com.globalsight.cxe.adapter;

import com.globalsight.cxe.message.CxeMessage;

public interface IConverterHelper2
{
    public AdapterResult[] performConversion();
    public CxeMessage performConversionBack();
}
