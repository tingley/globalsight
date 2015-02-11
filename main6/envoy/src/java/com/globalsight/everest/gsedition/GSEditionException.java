package com.globalsight.everest.gsedition;

import com.globalsight.util.GeneralException;

public class GSEditionException extends GeneralException {
    public final static String PROPERTIES_FILENAME = "GSEditionActionException";
    public final static String MSG_FAILED_TO_CREATE_action = 
        "Failed To Create GSEdition Action";
    
    public GSEditionException(String pMessageKey, String[] pMessageArguments,Exception pOriginalException) { 
        super(pMessageKey, pMessageArguments, pOriginalException, PROPERTIES_FILENAME);
    }
}
