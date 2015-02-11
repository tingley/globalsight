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

package com.globalsight.cxe.entity.filterconfiguration;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ValidateException extends IllegalArgumentException
{
    private static final long serialVersionUID = 3408173011210236071L;
    private String key = null;
    private List<String> args = null;
    private List<String> values = null;
    
    public String getMessage(ResourceBundle bundle)
    {
        if (key == null)
            return getMessage();
        
        String message = bundle.getString(key);
        if (args != null && args.size() > 0)
        {
            message = MessageFormat.format(message, args.toArray());
        }
        
        if (values != null)
        {
            message = MessageFormat.format(message, values.toArray());
        }
        
        return message;
    }
    
    public ValidateException()
    {
        // TODO Auto-generated constructor stub
    }

    public ValidateException(String s)
    {
        super(s);
    }

    public ValidateException(Throwable cause)
    {
        super(cause);
    }

    public ValidateException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public ValidateException(String s, String key, String... args)
    {
        super(s);
        this.key = key;
        this.args = new ArrayList<String>();
        
        for (String arg : args)
        {
            this.args.add(arg);
        }
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }


    public void addValue(String value)
    {
        if (values == null)
            values = new ArrayList<String>();
        
        values.add(value);
    }
}
