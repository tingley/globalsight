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
package com.globalsight.ling.tw;

import java.util.ListResourceBundle;

/**
 * Contains all the segment level error messages that can occur in
 * this release.
 */
public class ErrorChecker
    extends ListResourceBundle
{

    static final Object [][] contents =
    {
    { "ErrorMissingTags", "The following mandatory tags are missing:\n\t{0}" },
    { "ErrorInvalidAdd", "The following tags cannot be added:\n\t{0} " },
    { "ErrorUnbalancedTags", "The following tags are unbalanced:\n\t{0} "},
    { "MaxLengthMsg", "Maximum length exceeded. Please reduce the length of the translation." },
    { "invalidXMLCharacter", "The segment contains an invalid control character (Unicode: {0}). The character position is shown below:\n\n{1}"},
    { "ErrorConstantChanged", "The following words cannot be changed:\n\n{0}"},
    { "ErrorTagMoved", "The following tag cannot be moved:\n\n{0}"},
    { "ErrorTagInside", "{0} is not allowed to be moved inside [{1}][/{1}]"},
    { "InternalTagsMoved", "The following tags have been moved:\n\n{0}"}
    };

    public Object[][] getContents()
    {
        return contents;
    }
}
