package com.globalsight.machineTranslation.iptranslator.response;

public class TranslateResponse {

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

	public int[] getText_status() {
		return text_status;
	}

	public void setText_status(int[] text_status) {
		this.text_status = text_status;
	}

	public int[] getXliff_status() {
		return xliff_status;
	}

	public void setXliff_status(int[] xliff_status) {
		this.xliff_status = xliff_status;
	}

	private String text [];
	private String xliff [];

	private int text_status [];
	private int xliff_status [];
	
	/**
	 * print out
	 */
	public void print() {
		
		if(text!=null){
			for(int i = 0; i< text.length ;i++){
				System.out.println("Translation: " + text[i] + ", status: " + text_status[i]);
			}
		}
		
		if(xliff!=null){
			for(int i = 0; i< xliff.length ;i++){
				System.out.println("Translation: " + xliff[i] + ", status: " + xliff_status[i]);
			}
		}			
	}
}


