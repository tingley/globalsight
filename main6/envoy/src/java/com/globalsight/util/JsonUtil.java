package com.globalsight.util;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

public class JsonUtil
{
    public static ObjectMapper objectMapper = new ObjectMapper().configure(
            SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
    private static final Logger logger = Logger.getLogger(JsonUtil.class);
    /**
     * Object converted to json string
     * 
     * @param bean
     * @return
     */
    public static String toJson(Object bean)
    {

        StringWriter sw = new StringWriter();
        try
        {
            objectMapper.writeValue(sw, bean);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        String json = sw.toString();
        try
        {
            sw.close();
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }
        return json;
    }
    
    /**
     * Encode the JSON special character, e.g. " \ /.
     */
    public static String encode(String p_msg)
    {
        String result = p_msg;
        result = result.replace("\"", "\\\"");
        result = result.replace("/", "\\/");
        
        // Only Encoded Single \
        if (!result.contains("\\\\"))
        {
            result = result.replace("\\", "\\\\");
        }
        
        return result;
    }
}
