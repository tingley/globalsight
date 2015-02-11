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
package com.globalsight.ling.docproc.extractor.html;

import java.util.*;
import java.io.*;

class TestHtmlParser extends Parser
{
	private java.io.Writer m_Output = null;
		public TestHtmlParser(java.io.InputStream stream)
				{
						super(stream);
				}
		public TestHtmlParser(java.io.Reader stream)
				{
						super(stream);
				}
		public TestHtmlParser(java.io.Reader stream, java.io.Writer output)
				{
						super(stream);
						m_Output = output;
				}
		protected void comment(String p_strComment)
				{writeOut("<!--"+p_strComment+"-->");}
		protected void declaration(String p_strDelcaration)
				{writeOut("<!"+p_strDelcaration+">");}
		protected void endTag(String p_strTag)
				{writeOut("</"+p_strTag+">");}
		protected void finish()
				{System.out.print("finish:\n");
					if(m_Output!=null){try {m_Output.flush();} catch(Exception e){}}}
		public static void main(String args[]) throws ParseException
				{
						TestHtmlParser parser = new TestHtmlParser(System.in);
						parser.parse();
				}
		protected void newline(String p_strNewLine)
				{writeOut(p_strNewLine);}
		protected void processingInstruction(String p_stProcessingInstruction)
				{/* skip */}
		protected void script(String p_strTag, AttributeList p_AttributeList, String p_strScript)
				{
						writeOut(p_strTag);
				}
		protected void start()
				{	System.out.print("start:\n");}
		protected void startTag(String p_strTag, String p_strTagName, AttributeList p_AttributeList)
				{
						writeOut(p_strTag);
				}
		protected void text(String p_strText)
				{writeOut(p_strText);}
/**
 * Method comments goes here
 * @author Thierry Sourbier
 * @param p_strText java.lang.String
 */
public void writeOut(String p_strText) 
{
	if(m_Output == null)
	{
		System.out.print(p_strText);
	}
	else
	{
		try{
		m_Output.write(p_strText);
		}
		catch(Exception e)
		{
			System.out.print(e.getMessage());
		}
	}
}
}
