package com.adobe.cq.sling;

import java.util.ArrayList;
import java.util.List;

public class TranslationFile {

	private String GSDescription;
	private String GSTargetLanguage;
	private String GSSourceLanguage;
	private List<GSPageTranslation> GSPageTranslationList = new ArrayList<GSPageTranslation>();

	public String getGSDescription() {
		return GSDescription;
	}

	public void setGSDescription(String gSDescription) {
		GSDescription = gSDescription;
	}

	public String getGSTargetLanguage() {
		return GSTargetLanguage;
	}

	public void setGSTargetLanguage(String gSTargetLanguage) {
		GSTargetLanguage = gSTargetLanguage;
	}

	public String getGSSourceLanguage() {
		return GSSourceLanguage;
	}

	public void setGSSourceLanguage(String gSSourceLanguage) {
		GSSourceLanguage = gSSourceLanguage;
	}

	public List<GSPageTranslation> getGSPageTranslationList() {
		return GSPageTranslationList;
	}

	public void setGSPageTranslationList(
			List<GSPageTranslation> gSPageTranslationList) {
		GSPageTranslationList = gSPageTranslationList;
	}

}
