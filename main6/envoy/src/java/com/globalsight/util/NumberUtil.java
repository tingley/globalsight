/**
 *  Copyright 2011 Welocalize, Inc. 
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

package com.globalsight.util;

/**
 * Utility class for number converting
 * @author Vincent Yan
 * @since 8.2
 * @date 2011/08/08
 *
 */
public class NumberUtil
{
    public static int DEFAULT_INTEGER_VALUE = -1;
    
    public static int convertToInt(String p_param) {
        return convertToInt(p_param, DEFAULT_INTEGER_VALUE);
    }
    
    public static int convertToInt(String p_param, int p_default) {
        if (StringUtil.isEmpty(p_param))
            return p_default;
        
        try
        {
            int result = Integer.parseInt(p_param);
            return result;
        }
        catch (Exception e)
        {
            return p_default;
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

}
