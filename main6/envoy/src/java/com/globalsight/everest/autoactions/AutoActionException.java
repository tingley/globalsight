package com.globalsight.everest.autoactions;

import com.globalsight.util.GeneralException;

public class AutoActionException extends GeneralException {
    public final static String PROPERTIES_FILENAME = "AutoActionException";
    public final static String MSG_FAILED_TO_CREATE_action = 
        "Failed To Create Automatic Action";
    
    public AutoActionException(String pMessageKey, String[] pMessageArguments,
        Exception pOriginalException) {
        
        super(pMessageKey, pMessageArguments, 
              pOriginalException, PROPERTIES_FILENAME);
    }
}
