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
package com.globalsight.everest.webapp.pagehandler.administration.reports;

import jxl.format.UnderlineStyle;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;

/**
 * The class is base class for xls report
 * Include any common method
 * 
 *
 */
public abstract class XlsReports
{

	/**
	 * Get red color format for wrong cell, set the format background color red 
	 * @return red color CellFormat
	 * @throws Exception
	 */
	protected jxl.format.CellFormat getRedColorFormat() throws Exception
	{
		WritableFont redFont = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,
				jxl.format.Colour.BLACK);
		WritableCellFormat redFormat = new WritableCellFormat(redFont);
		redFormat.setBackground(jxl.format.Colour.RED);
		redFormat.setWrap(false);

		return redFormat;
	}
		
	/**
	 *  Get a normal format for cell
	 * @return normal CellFormat
	 * @throws Exception
	 */
	 protected jxl.format.CellFormat getNormalFormat() throws Exception
  	{
  		WritableFont font = new WritableFont(WritableFont.ARIAL, 10,
  				WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,
  				jxl.format.Colour.BLACK);
  		WritableCellFormat format = new WritableCellFormat(font);
  		format.setWrap(false);

  		return format;
  	}
}
