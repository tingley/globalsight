package com.globalsight.terminology.entrycreation;

import com.globalsight.terminology.Termbase.SyncOptions;
import com.globalsight.terminology.java.TbConcept;

public class NosyncAction implements INosyncAction
{
    private SyncOptions options;
    
    public NosyncAction(SyncOptions Options) {
        this.options = Options;
    }
    
    @Override
    public TbConcept doAction(TbConcept tc)
    {
        if(options.getNosyncAction() == options.NOSYNC_ADD) {
            return tc;
        }
        
        return null;
    }

    @Override
    public void setOption(SyncOptions Options)
    {
        this.options = Options;

    }

    @Override
    public boolean isNeedDoWork()
    {
        if(options.getNosyncAction() == options.NOSYNC_DISCARD) {
            return false;
        }
        else {
            return true;
        }
    }

}
