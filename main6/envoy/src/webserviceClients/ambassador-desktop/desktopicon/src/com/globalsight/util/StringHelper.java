package com.globalsight.util;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.entity.FileProfile;
import com.globalsight.util2.StringUtil;

public class StringHelper
{

	private static String ID = "id";

	private static String NAME = "name";

	private static String L10N = "l10nprofile";

	private static String DESCRIPTION = "description";

	private static String FILEEXINFO = "fileExtensionInfo";

	private static String FILEEX = "fileExtension";

	private static String LOCALEINFO = "localeInfo";

	private static String SOURCELOCALE = "sourceLocale";

	private static String TARGETLOCALE = "targetLocale";

	/**
     * Get the result of the attribute in the string like this: <id>1001</id><name>l10nfileprofile</name><description>N/A</description>
     * The attributes used in the icon application are fileProfileInfo,
     * fileProfile, id, name, description, fileExtensionInfo, fileExtension,
     * localeInfo, sourceLocale,targetLocale.
     * 
     * @param source
     * @param attribute
     * @return
     */
	public static List split(String source, String attribute)
	{
		String startToken = "<" + attribute + ">";
		String endToken = "</" + attribute + ">";
		String[] first = source.split(startToken);
		List result = new ArrayList();
		if (first.length == 1)
		{
			return result;
		}
		else
		{
			for (int i = 1; i < first.length; i++)
			{
				String second = first[i];
				String[] third = second.split(endToken);
				result.add(third[0]);
			}
			return result;
		}
	}

	/**
     * Based on the fileExtension, get the suitable file profiles.
     * 
     * @param list,
     *            the fileProfile information list. Use split(source,
     *            "fileProfile") to get the list.
     * @param fileExtension
     * @return
     */
	public static List getFileProfiles(List list, String l10n,
			String fileExtension)
	{
		List fileProfiles = new ArrayList();
		for (int i = 0; i < list.size(); i++)
		{
			String source = list.get(i).toString();
			FileProfile fileProfile = getFileProfile(source, l10n,
					fileExtension);
			if (fileProfile != null)
			{
				fileProfiles.add(fileProfile);
			}
		}

		return fileProfiles;
	}

	private static FileProfile getFileProfile(String source, String l10n,
			String fileExtension)
	{
		FileProfile fileProfile = null;
		List listExtensions = getChildNode(source, FILEEXINFO, FILEEX);
		boolean isSuitable = false;
		// all extensions
		if (listExtensions.size() == 0)
		{
			isSuitable = true;
		}
		for (int i = 0; i < listExtensions.size(); i++)
		{
			String extension = listExtensions.get(i).toString();
			if (extension.equalsIgnoreCase(fileExtension))
			{
				isSuitable = true;
				break;
			}
		}
		String l10nprofile = getSingleResult(split(source, L10N));
		if (l10n != null && !"".equals(l10n) && !l10nprofile.equals(l10n))
		{
			isSuitable = false;
		}

		if (isSuitable)
		{
			fileProfile = createFileProfile(source);
		}
		return fileProfile;
	}

	private static FileProfile createFileProfile(String source)
	{
		FileProfile fileProfile = new FileProfile();

		// Set id
		String id = getSingleResult(split(source, ID));
		fileProfile.setId(id);

		// Set name
		String name = getSingleResult(split(source, NAME));
		fileProfile.setName(name);

		// Set name
		String l10n = getSingleResult(split(source, L10N));
		fileProfile.setL10nprofile(l10n);

		// Set description
		String description = getSingleResult(split(source, DESCRIPTION));
		fileProfile.setDescription(description);

		// Set fileExtension[]
		List listExtensions = getChildNode(source, FILEEXINFO, FILEEX);
		String[] fileExtension = getMultiResult(listExtensions);
		fileProfile.setFileExtension(fileExtension);

		// Set sourceLocale
		List sourceLocales = getChildNode(source, LOCALEINFO, SOURCELOCALE);
		String sourceLocale = getSingleResult(sourceLocales);
		fileProfile.setSourceLocale(sourceLocale);

		// Set targetLocale
		List targetLocales = getChildNode(source, LOCALEINFO, TARGETLOCALE);
		String[] targetLocale = getMultiResult(targetLocales);
		fileProfile.setTargetLocales(targetLocale);
		fileProfile.setUsedTargetLocales(targetLocale);

		return fileProfile;
	}

	private static List getChildNode(String source, String parentNode,
			String childNode)
	{
		List parent = split(source, parentNode);
		String parentString = parent.get(0).toString();
		List child = split(parentString, childNode);
		return child;
	}

	private static String getSingleResult(List list)
	{
		if (list.size() == 0)
		{
			return "";
		}
		else
		{
			return list.get(0).toString();
		}
	}

	private static String[] getMultiResult(List list)
	{
		if (list.size() == 0)
		{
			return new String[0];
		}
		else
		{
			int size = list.size();
			String[] result = new String[size];
			for (int i = 0; i < size; i++)
			{
				result[i] = list.toArray()[i].toString();
			}
			return result;
		}
	}

	public static void main(String[] args)
	{
		try
		{
			testPassword();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private static void testPassword()
	{
		String pwd = "password";

		String s = StringUtil.encryptString(pwd);

		System.out.println(s);

		String n = StringUtil.decryptString(s);

		System.out.println(n);
	}
}
