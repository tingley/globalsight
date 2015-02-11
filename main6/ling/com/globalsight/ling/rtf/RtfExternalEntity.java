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
//===============================================================
// package : com.tetrasix.majix.rtf
// class : com.tetrasix.majix.RtfExternalEntity
//===============================================================
// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at
// http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
// License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is TetraSys code.
//
// The Initial Developer of the Original Code is TetraSys..
// Portions created by TetraSys are
// Copyright (C) 1998-2000 TetraSys. All Rights Reserved.
//
// Contributor(s):
//===============================================================
package com.globalsight.ling.rtf;


public class RtfExternalEntity
{
    String _name;
    String _pubid;
    String _sysid;

    RtfExternalEntity(String name, String sysid)
    {
        _name = name;
        _sysid = sysid;
    }

    RtfExternalEntity(String name, String pubid, String sysid)
    {
        _name = name;
        _pubid = pubid;
        _sysid = sysid;
    }

    public String getName()
    {
        return _name;
    }

    public String getSystemID()
    {
        return _sysid;
    }

    public String getPublicID()
    {
        return _pubid;
    }
}
