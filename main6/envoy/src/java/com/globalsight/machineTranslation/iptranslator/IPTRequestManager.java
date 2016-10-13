package com.globalsight.machineTranslation.iptranslator;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.globalsight.machineTranslation.iptranslator.request.Request;
import com.globalsight.machineTranslation.iptranslator.request.TranslationRequest;

public class IPTRequestManager
{
    private static final String ENCODING = "UTF-8";
    // api accept type
    private static final String CONTENTTYPE = "application/json";
    // JSON mapper
    public static ObjectMapper mapper = new ObjectMapper();

    public StringEntity checkTranslateParams(String key, String from,
            String to, String[] segments) throws IOException
    {
        TranslationRequest translationRequest = new TranslationRequest();
        translationRequest.setKey(key);
        translationRequest.setInput(segments);
        translationRequest.setFrom(from);
        translationRequest.setTo(to);

        return makeParams(translationRequest);
    }

    public StringEntity checkMonitorParams(String key)
            throws IOException
    {
        Request monitorRequest = new Request();
        monitorRequest.setKey(key);
        return makeParams(monitorRequest);
    }

    private StringEntity makeParams(Request request)
            throws JsonGenerationException, JsonMappingException,
            UnsupportedEncodingException, IOException
    {
        StringEntity params = new StringEntity(
                mapper.writeValueAsString(request), ENCODING);
        params.setContentType(CONTENTTYPE);
        return params;
    }

    public String checkMonitorBack(HttpResponse response)
            throws IllegalStateException, IOException
    {
        InputStream inputStream = response.getEntity().getContent();
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, ENCODING);
        return writer.toString();
    }

}
