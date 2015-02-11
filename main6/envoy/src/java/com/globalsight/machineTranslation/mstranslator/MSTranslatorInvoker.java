package com.globalsight.machineTranslation.mstranslator;

import java.rmi.RemoteException;
import java.util.Date;

import com.globalsight.log.GlobalSightCategory;
import com.microsoft.schemas.MSNSearch._2005._09.fex.LanguagePair;
import com.microsoft.schemas.MSNSearch._2005._09.fex.TItem;
import com.microsoft.schemas.MSNSearch._2005._09.fex.TranslationRequest;
import com.microsoft.schemas.MSNSearch._2005._09.fex.TranslationResponse;
import com.microsoft.schemas.MSNSearch._2005._09.fex.TranslationServiceSoapProxy;

public final class MSTranslatorInvoker
{
	private String endpoint = null;
	private TranslationServiceSoapProxy proxy = null;
	
	private static final GlobalSightCategory s_logger = 
		(GlobalSightCategory) GlobalSightCategory.getLogger(MSTranslatorInvoker.class);
	
	public MSTranslatorInvoker() {
		proxy = new TranslationServiceSoapProxy();
	}
	
	public MSTranslatorInvoker(String endpoint) {
		this.endpoint = endpoint;
		proxy = new TranslationServiceSoapProxy(endpoint);
	}

	public String[] translate(TranslationRequest translationRequest)
		throws RemoteException
	{
		String[] translated = null;
	
		TranslationResponse translationResponse = proxy.translate(translationRequest);
		if (translationResponse != null)
		{
			TItem[] items = translationResponse.getTranslations();
			if (items != null && items.length > 0)
			{
				translated = new String[items.length];
				for (int i=0;i<items.length; i++)
				{
					TItem item = items[i];
					String text = item.getText();
					translated[i] = text;
				}
			}
		}

		return translated;
	}
	
	public String getEndpoint()
	{
		return this.endpoint;
	}

	public static void main(String[] args)
	{
		MSTranslatorInvoker t= new MSTranslatorInvoker();  
        
		String endpoint = "http://mtloc.live-int.com:84/translate";
		MSTranslatorInvoker ms_mt = new MSTranslatorInvoker(endpoint);
		
		LanguagePair lp1 = new LanguagePair("en", "fr");
		LanguagePair lp2 = new LanguagePair("en", "de");
		LanguagePair lp3 = new LanguagePair("en", "zh-CN");
		LanguagePair lp4 = new LanguagePair("en", "zh-TW");
		LanguagePair lp5 = new LanguagePair("en", "ja");
		LanguagePair[] lps = new LanguagePair[]{lp1,lp2,lp3,lp4,lp5};
		
		for (int k=0; k<10; k++) 
		{
			TranslationRequest transRequest = new TranslationRequest();
//			String[] texts = {"Java 2 Platform SE 5.0.","All Packages", "All classes and interfaces (except non-static nested types).",
//					"Package, class and interface descriptions.","Frame Alert","This document is designed to be viewed using the frames feature.",
//					"If you see this message, you are using a non-frame-capable web client.","Link toNon-frame version."};
			String[] texts = {"I love this game.","Do you like this game?"}; 
			transRequest.setTexts(texts);
			for (int j=0; j<lps.length; j++)
			{
				transRequest.setLangPair(lps[j]);
				try {
					String[] results = ms_mt.translate(transRequest);
					if (results != null)
					{
						System.out.print(k + " :: " + lps[j] + " :: " + new Date() + " :: ");
						for (int i=0;i<results.length;i++)
						{
							System.out.print(results[i] + " :: ");
						}
						System.out.println();
					}
				} catch (RemoteException e) {
					System.out.println(e);
				}
			}
		}
	}
}
