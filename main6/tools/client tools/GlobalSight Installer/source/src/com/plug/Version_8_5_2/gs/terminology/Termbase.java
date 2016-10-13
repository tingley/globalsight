/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.plug.Version_8_5_2.gs.terminology;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;


public class Termbase
{
    private String TBID, TB_NAME, TB_DESCRIPTION, TB_DEFINITION, COMPANYID;

    public String getTBID()
    {
        return TBID;
    }

    public void setTBID(String tBID)
    {
        TBID = tBID;
    }

    public String getTB_NAME()
    {
        return TB_NAME;
    }

    public void setTB_NAME(String tB_NAME)
    {
        TB_NAME = tB_NAME;
    }

    public String getTB_DESCRIPTION()
    {
        return TB_DESCRIPTION;
    }

    public void setTB_DESCRIPTION(String tB_DESCRIPTION)
    {
        TB_DESCRIPTION = tB_DESCRIPTION;
    }

    public String getTB_DEFINITION()
    {
        return TB_DEFINITION;
    }

    public void setTB_DEFINITION(String tB_DEFINITION)
    {
        TB_DEFINITION = tB_DEFINITION;
    }

    public String getCOMPANYID()
    {
        return COMPANYID;
    }

    public void setCOMPANYID(String cOMPANYID)
    {
        COMPANYID = cOMPANYID;
    }
}
