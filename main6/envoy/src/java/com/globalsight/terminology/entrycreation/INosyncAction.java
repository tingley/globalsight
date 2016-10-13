package com.globalsight.terminology.entrycreation;

import com.globalsight.terminology.Termbase.SyncOptions;
import com.globalsight.terminology.java.TbConcept;

public interface INosyncAction
{
    public void setOption(SyncOptions Options);
    public TbConcept doAction(TbConcept tc);
    public boolean isNeedDoWork();
}
