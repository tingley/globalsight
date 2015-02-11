package com.globalsight.machineTranslation.mstranslator;

import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.datacontract.schemas._2004._07.microsoft_mt_web_service.ArrayOfTranslateArrayResponse;
import org.datacontract.schemas._2004._07.microsoft_mt_web_service.TranslateArrayResponse;
import org.datacontract.schemas._2004._07.microsoft_mt_web_service.TranslateOptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.tempuri.LanguageService;
import org.tempuri.SoapService;

import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;

public class TestMSMT
{

    private static final String appId = "0E79EE1C580587CD7084B8E5CD763A907B18057E";
    private static final String category = "Tech";
    
    private LanguageService service;
    
    @Before
    public void setUp()
    {
        try
        {
            URL baseUrl = org.tempuri.SoapService.class.getResource(".");
            URL url = new URL(baseUrl,
                    "http://api.microsofttranslator.com/V2/Soap.svc");
            SoapService soap = new SoapService(url);
            service = soap.getBasicHttpBindingLanguageService();
            Assert.assertNotNull(service);
        }
        catch (MalformedURLException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testSingleMSMT()
    {
        try
        {
            String text = "Hello world";
            String result1 = service.translate(appId, text, "en", "fr",
                    "text/plain", category);
            Assert.assertNotNull(result1);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testMutiMSMT()
    {
        try
        {
            TranslateOptions options = new TranslateOptions();
            JAXBElement<String> categoryElement = new JAXBElement<String>(
                    new QName(
                            "http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2",
                            "category"), String.class, category);
            JAXBElement<String> contentType = new JAXBElement<String>(
                    new QName(
                            "http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2",
                            "contentType"), String.class, "text/plain");
            options.setCategory(categoryElement);
            options.setContentType(contentType);
            ArrayOfstring stringArray = new ArrayOfstring();
            String[] texts =
            {
                    "Press [<span style='font-weight: bold;'>Enter</span>]",
                    "press [<bpt type=\"x-span\" i=\"1\" x=\"1\">&lt;span style=&quot;font-weight: bold;&quot;&gt;</bpt>enter<ept i=\"1\">&lt;/span&gt;</ept>]",
                    "The Cards List window should be displayed." };
            List<String> list = Arrays.asList(texts);
            stringArray.getString().addAll(list);
            ArrayOfTranslateArrayResponse result = service.translateArray(
                    appId, stringArray, "en", "fr", options);
            List<TranslateArrayResponse> result2 = result
                    .getTranslateArrayResponse();
            Assert.assertNotNull(result2);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
}
