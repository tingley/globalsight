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
package com.util.db;

import java.io.File;

import com.util.CmdUtil;

/**
 * A subclass of <code>DbUtil</code>. It is used only when the system is windows.
 * @see com.ui.DbUtil
 */
public class DbUtilInWindows extends DbUtil
{
    @Override
    public void execSqlFile(File file) throws Exception
    {
//        String[] sqlStmt =
//        { "cmd", "/c", "mysql", "-u" + getUser(), "-p" + getPassword(),
//                "-h" + getHost(), "-vvv" , "-P" + getPort(), "-D" + getDatabase(), "<",
//                file.getAbsolutePath() };
        String[] sqlStmt =
            { "cmd.exe", "/c", "script\\windows\\importSqlFile.bat", getHost(), getPort(),
                    getUser(), getPassword(), getDatabase(), file.getAbsolutePath() };
        CmdUtil.run(sqlStmt, false, true);
    }
}
