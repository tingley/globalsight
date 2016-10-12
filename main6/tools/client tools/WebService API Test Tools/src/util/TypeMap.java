package util;

import java.util.Map;
import java.util.HashMap;

public class TypeMap
{
    public static final Map<String, Class> TYPE_INFO = new HashMap<String, Class>()
    {
        {
            put("string", String.class);
            put("int", int.class);
            put("long", long.class);
            put("double", double.class);
            put("byte[]", byte[].class);
            put("base64binary", byte[].class);
            put("boolean", boolean.class);
            put("map", HashMap.class);
            put("arrayof_soapenc_string", String[].class);
            put("others", Object.class);
        }
    };

}
