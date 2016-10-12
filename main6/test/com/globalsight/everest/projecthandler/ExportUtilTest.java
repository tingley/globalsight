package com.globalsight.everest.projecthandler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Before;

import com.globalsight.everest.projecthandler.exporter.ExportUtil;

public class ExportUtilTest 
{

	@Before
    public void setUp()
    {
		ExportUtil.WHITE_SPACE_EXPORT.put("zh*", "\" \":\"\"");
    }

	@Test
    public void testSegmentationTrimLeading() throws Exception
    {
		String content = "</skeleton><skeleton>     </skeleton><translatable wordcount=\"1\">test</translatable><skeleton> </skeleton>";
		String content2 = ExportUtil.replaceWhitespace(content, "zh_CN");
		assertEquals("</skeleton><skeleton></skeleton><translatable wordcount=\"1\">test</translatable><skeleton></skeleton>", content2);
    }
}
