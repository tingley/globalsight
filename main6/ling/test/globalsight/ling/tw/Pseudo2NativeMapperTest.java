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
package test.globalsight.ling.tw;

/*
Copyright (c) 2000 GlobalSight Corporation. All rights reserved.

THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.globalsight.ling.tw.*;

import java.util.Hashtable;
public class Pseudo2NativeMapperTest extends TestCase
{

    /**
    */
    public Pseudo2NativeMapperTest(String p_name)
    {
        super(p_name);
    }

    /**
    */
    public void setUp()
    {
    
    }

    /**
    * Insert the method's description here.
    * Creation date: (8/16/2000 10:40:43 AM)
    */
    public static Test suite()
    {
        return new TestSuite(Pseudo2NativeMapperTest.class);
    }

    /**
    * Test compact errors
    *
    */
    public void testResourceLoad()
    {
        TmxPseudo cvt = null;
        PseudoData PD = null;
        
        String test = "<ph type=\"x-formfeed\">\f</ph>leadspace<ph type=\"x-space\"> </ph>leadspace<ph type=\"x-tab\">\t</ph>real tab<ph type=\"x-tab\">\t</ph>real tab value 22";
        try
        {
            PD = new PseudoData();
            // force it to go to defaults
            //PD.setLocale("en_XX");
            PD = cvt.tmx2Pseudo(test,PD);
        }
        catch(Exception e)
        {
            fail(e.toString());
        }
    
        Hashtable map = PD.getPseudo2NativeMap();
        if( (map == null) )
        {
            fail("Map error");
        }
        
            
    } 		
}
