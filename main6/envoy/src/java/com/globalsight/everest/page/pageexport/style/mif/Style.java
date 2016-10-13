package com.globalsight.everest.page.pageexport.style.mif;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A abstract class that to handle the style node. For example <b> node in DOCX
 * document.xml file.
 * 
 */
public abstract class Style
{
	protected abstract String getStyleName();
	
	public abstract void handleStyle(String style, List<Style> styles);
	
	protected abstract String getFontName();
	protected abstract String getFontValue();
	
	/**
	 * For <FWeight `Bold'> <FAngle `Italic'> <FUnderlining FSingle> <FTag
	 * `Subscript'> <FTag `Superscript'>
	 * 
	 * @param font
	 */
	public String getUpdateFont(String font) 
	{
		if (font == null)
		{
			return "<Font \r\n  <FTag `'> \r\n " + getFontTag() + "\r\n> # end of Font";
		}
		
		Pattern p = Pattern.compile("<"+  getFontName() + " [^>]*>");
		Matcher m = p.matcher(font);
		if (m.find())
		{
			font = font.replace(m.group(), getFontTag());
		}
		else
		{
			font = font.replace("> # end of Font", getFontTag() + "\r\n> # end of Font");
		}
		
		return font;
	}
	
	/**
	 * Get the attribute note for font. Like <FWeight `Bold'> <FAngle `Italic'>
	 * and so on.
	 * 
	 * @return String
	 */
	private String getFontTag()
	{
		return "<" + getFontName() + " " + getFontValue() + ">";
	}
}
