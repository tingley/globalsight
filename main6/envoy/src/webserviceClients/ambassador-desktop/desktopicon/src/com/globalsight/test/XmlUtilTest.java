package com.globalsight.test;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.entity.TM;
import com.globalsight.util.XmlUtil;

public class XmlUtilTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testString2Objects();
	}

	public static void testString2Objects() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<ProjectTMInformation>"
				+ "<ProjectTM><id>1000</id><name>TM</name><domain></domain><organization></organization><description></description></ProjectTM>"
				+ "<ProjectTM><id>1001</id><name>TM-Test001</name><domain></domain><organization></organization><description></description></ProjectTM>"
				+ "</ProjectTMInformation>";
		String tag = "ProjectTM";
		List<TM> tmList = new ArrayList<TM>();

		tmList = XmlUtil.string2Objects(TM.class, xml, tag);

		printTM(tmList);
	}

	public static void printTM(List list) {
		for (int i = 0; i < list.size(); i++) {
			TM tm = (TM) list.get(i);
			System.out.println(tm + "\t\t" + tm.getName());
		}
	}
}
