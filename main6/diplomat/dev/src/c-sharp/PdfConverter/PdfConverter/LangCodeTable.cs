using System;
using System.Collections;
using System.Diagnostics;
using GlobalSight.Common;

namespace GlobalSight.PdfConverter
{
	/// <summary>
	/// Hashtable of OmniPage12 Language Codes
	/// </summary>
	public class LangCodeTable : System.Collections.Hashtable
	{
		public LangCodeTable() : base()
		{
			try 
			{
				base.Add("en",0); //english
				base.Add("de",1); //german
				base.Add("fr",2); //french
				base.Add("nl",3); //dutch
				base.Add("no",4); //norwegian
				base.Add("sv",5); //swedish
				base.Add("fi",6); //finnish
				base.Add("da",7); //danish
				base.Add("is",8); //icelandic
				base.Add("pt",9); //portuguese
				base.Add("es",10); //spanish
				base.Add("ca",11); //catalan
				base.Add("it",12); //italian
				//maltese, no java lang code
				base.Add("el",14); //greek
				base.Add("pl",15); //polish
				base.Add("cs",16); //czech
				base.Add("sk",17); //slovak
				base.Add("hu",18); //hungarian
				base.Add("sl",19); //slovenian
				base.Add("hr",20); //croat
				base.Add("ro",21); //romanian
				base.Add("sq",22); //albanian
				base.Add("tr",23); //turkish
				base.Add("et",24); //estonian
				base.Add("lv",25); //latvian
				base.Add("lt",26); //lithuanian
				base.Add("sr",28); //serbian
				base.Add("mk",29); //macedonia
				base.Add("bg",31); //bulgarian
				base.Add("be",32); //byelorussian
				base.Add("uk",33); //ukranian
				base.Add("ru",34); //russian
			}
			catch (Exception e)
			{
				string msg = "ERROR: Could not fill lang codes table: " + e.Message + "\r\n" + e.StackTrace;
				Logger.LogWithoutException(msg);
				throw e;
			}
		}
	}
}
