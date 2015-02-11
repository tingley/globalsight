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
package util;

public class Utilities
{
    public static void requireJava14()
    {
        String javaVersion = System.getProperty("java.version");
        int firstDot = javaVersion.indexOf(".");
        int secondDot = javaVersion.indexOf(".", firstDot + 1);
        int majorVersion = Integer.parseInt(javaVersion.substring(0, firstDot));
        int minorVersion = Integer.parseInt(javaVersion.substring(firstDot + 1,
                secondDot));
        if (majorVersion < 1 || (majorVersion == 1 && minorVersion < 5))
        {
            System.out.println("The current version of java is " + javaVersion);
            System.out.println("This program requires java 1.5 or higher.");
            System.exit(1);
        }
    }
}
