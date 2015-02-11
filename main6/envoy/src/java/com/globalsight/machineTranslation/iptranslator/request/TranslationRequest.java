package com.globalsight.machineTranslation.iptranslator.request;

import java.util.Arrays;

public class TranslationRequest extends Request{

	@Override
	public String toString() {
		return "TranslationRequest [text=" + Arrays.toString(text) + ", xliff="
				+ Arrays.toString(xliff) + ", from=" + from + ", to=" + to
				+ "]";
	}
	public String[] getText() {
		return text;
	}
	public void setText(String[] text) {
		this.text = text;
	}
	public String[] getXliff() {
		return xliff;
	}
	public void setXliff(String[] xliff) {
		this.xliff = xliff;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String text [];
	private String xliff [];
	private String from;
	private String to;
}
