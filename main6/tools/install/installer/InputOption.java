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

package installer;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputOption
{
    private String key;
    private String desplayName;
    private String regex;
    private Pattern pattern;

    public final static String BOOLEAN = "[TtFf]|TRUE|true|FALSE|false";
    private final static String PORT = "\\d{1,6}";
    private final static String LOG_LEVER = "[DdIiWwEeFf]|DEBUG|debug|INFO|info|WARN|warn|ERROR|error|FATAL|fatal";
    private final static String SORT = "ASC|asc|DESC|desc|DAFAULT|default";
    private final static String ZER0_ONE = "[01]";
    private final static String PURE_DECIMAL = "0?\\.?(\\d*)";
    private final static String TIME_ZONE = "[+-][1-9]|-0|-1[01]|\\+1[012]";
    public final static String YES_NO = "[YyNn]";

    private static Map ACCEPT_STRING = new HashMap();
    private static Map DEFAULT_VALUES_MAP = new HashMap();

    private static Map TYPES;

    private static ResourceBundle PROPERTIES_RESOURCE = ResourceBundle
            .getBundle("data/installDisplay");
    private static ResourceBundle INSTALL_OPTION_RESOURCE = ResourceBundle
            .getBundle("data/installAmbassador");
    private static ResourceBundle TYPE_PROPERTIES = ResourceBundle
            .getBundle("data/installValueTypes");
    private static ResourceBundle DEFAULT_VALUES = ResourceBundle
            .getBundle("data/installDefaultValues");

    public InputOption(String key, String desplayValue, String regex)
    {
        this.key = key;
        this.desplayName = desplayValue;
        this.regex = regex;
        pattern = Pattern.compile(regex);
    }

    private Map getTypes()
    {
        if (TYPES == null)
        {
            TYPES = new HashMap();
            TYPES.put("checkbox", BOOLEAN);
            TYPES.put("integer", PORT);
            TYPES.put("double", PURE_DECIMAL);
            TYPES.put("system4_timer_useWarningThresholds", ZER0_ONE);
            TYPES.put("system_logging_priority", LOG_LEVER);
            TYPES.put("system_time_zone", TIME_ZONE);
        }

        return TYPES;
    }

    public InputOption(String key, String regex)
    {
        this.key = key;
        this.desplayName = getDisplayName(key);
        if (regex != null)
        {
            this.regex = regex;
            this.pattern = Pattern.compile(regex);
        }
    }
    public InputOption(String key)
    {
        this.key = key;
        this.desplayName = getDisplayName(key);

        String type = null;
        try
        {
            type = TYPE_PROPERTIES.getString(key);
            Object validation = getTypes().get(type);
            if (validation == null)
            {
                validation = getTypes().get(key);
            }

            if (validation != null)
            {
                this.regex = (String) validation;
                this.pattern = Pattern.compile(regex);
            }
        }
        catch (Exception e)
        {
            // do nothing
        }

    }

    public boolean matches(String s)
    {
        boolean match = true;
        if (pattern != null)
        {
            Matcher matcher = pattern.matcher(s);
            match = matcher.matches();
        }

        return match;
    }

    public String getKey()
    {
        return key;
    }

    public String getDesplayValue()
    {
        return desplayName;
    }

    public String getRegex()
    {
        return regex;
    }

    private String getDisplayName(String key)
    {
        String name;
        try
        {
            name = PROPERTIES_RESOURCE.getString(key);
        }
        catch (MissingResourceException e)
        {
            name = INSTALL_OPTION_RESOURCE.getString(key);
        }

        return name;
    }

    public String checkValue(String value)
    {
        String returnValue = value;

        if (value != null && value.length() != 0)
        {
            if (TIME_ZONE.equalsIgnoreCase(regex))
            {
                value = value.replaceAll("GMT", "");
                value = value.replaceAll(":00", "");
            }

            if (matches(value))
            {
                if (!getValue(value).equals(value))
                {
                    returnValue = getValue(value);
                }
            }
            else
            {
                returnValue = geyDefaultValue();
            }
        }

        return returnValue;
    }

    private Map getDefaultValues()
    {
        if (DEFAULT_VALUES_MAP == null || DEFAULT_VALUES_MAP.size() == 0)
        {
            DEFAULT_VALUES_MAP = new HashMap();
            DEFAULT_VALUES_MAP.put(BOOLEAN, "false");
            DEFAULT_VALUES_MAP.put(PORT, "8080");
            DEFAULT_VALUES_MAP.put(LOG_LEVER, "info");
            DEFAULT_VALUES_MAP.put(SORT, "default");
            DEFAULT_VALUES_MAP.put(ZER0_ONE, "0");
            DEFAULT_VALUES_MAP.put(PURE_DECIMAL, ".75");
            DEFAULT_VALUES_MAP.put(TIME_ZONE, "GMT+8:00");
        }
        return DEFAULT_VALUES_MAP;
    }

    private String geyDefaultValue()
    {
        String defaultValue = null;
        try
        {
            defaultValue = DEFAULT_VALUES.getString(key);
        }
        catch (Exception e)
        {
            // do nothing.
        }
        if (defaultValue == null)
        {
            defaultValue = (String) getDefaultValues().get(this.regex);
        }
        return defaultValue;
    }

    public String getValue(String input)
    {
        if (regex != null && input != null && input.length() > 0)
        {
            if (BOOLEAN.equalsIgnoreCase(regex))
            {
                input = input.toLowerCase();
                if (input.startsWith("t"))
                {
                    input = "true";
                }
                else
                {
                    input = "false";
                }
            }
            else if (LOG_LEVER.equalsIgnoreCase(regex))
            {
                input = input.toUpperCase();
                if (input.equalsIgnoreCase("d"))
                {
                    input = "DEBUG";
                }
                else if (input.equalsIgnoreCase("i"))
                {
                    input = "INFO";
                }
                else if (input.equalsIgnoreCase("w"))
                {
                    input = "WARN";
                }
                else if (input.equalsIgnoreCase("e"))
                {
                    input = "ERROR";
                }
                else if (input.equalsIgnoreCase("f"))
                {
                    input = "FATAL";
                }
            }
            else if (SORT.equalsIgnoreCase(regex))
            {
                input = input.toLowerCase();
            }
            else if (PURE_DECIMAL.equalsIgnoreCase(regex))
            {
                Matcher matcher = pattern.matcher(input);
                matcher.matches();
                input = "." + matcher.group(1);
            }
            else if (TIME_ZONE.equalsIgnoreCase(regex))
            {
                input = "GMT" + input + ":00";
            }
            else if (YES_NO.equalsIgnoreCase(regex))
            {
                input = input.toLowerCase();
            }
        }

        return input;
    }

    public String getAcceptString()
    {
        String acceptString = "String";
        if (regex != null)
        {
            acceptString = (String) getAcceptStringMap().get(regex);
            if (acceptString == null)
            {
                acceptString = "String";
            }
        }

        return acceptString;
    }

    private Map getAcceptStringMap()
    {
        if (ACCEPT_STRING.size() == 0)
        {
            ACCEPT_STRING.put(BOOLEAN, "t, f, true, false");
            ACCEPT_STRING.put(PORT, "integer of one to six digits");
            ACCEPT_STRING.put(LOG_LEVER,
                    "d, i, w, e, f, debug, info, warn, error, fatal");
            ACCEPT_STRING.put(SORT, "asc, desc, default");
            ACCEPT_STRING.put(ZER0_ONE, "0, 1");
            ACCEPT_STRING.put(PURE_DECIMAL, "pure decimal");
            ACCEPT_STRING.put(TIME_ZONE, "-11 to +12 (-/+ is necessary)");
            ACCEPT_STRING.put(YES_NO, "Y or N");
        }

        return ACCEPT_STRING;
    }
}
