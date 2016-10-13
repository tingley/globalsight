package com.globalsight.everest.projecthandler;

import java.util.HashSet;

import junit.framework.TestCase;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class TranslationMemoryProfileTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from TranslationMemoryProfile";

	public void testSave()
	{
		TranslationMemoryProfile translationMemoryProfile = new TranslationMemoryProfile();
		translationMemoryProfile.setName("name");
		translationMemoryProfile.setDescription("p_description");
		translationMemoryProfile.setProjectTmIdForSave(123456);
		translationMemoryProfile.setSaveUnLocSegToProjectTM(true);
		translationMemoryProfile.setSaveUnLocSegToPageTM(true);
		translationMemoryProfile.setJobExcludeTuTypesStr("ddd");
		translationMemoryProfile.setLeverageLocalizable(true);
		translationMemoryProfile.setIsExactMatchLeveraging(true);
		translationMemoryProfile.setIsTypeSensitiveLeveraging(true);
		translationMemoryProfile.setTypeDifferencePenalty(456789);
		translationMemoryProfile.setIsCaseSensitiveLeveraging(true);
		translationMemoryProfile.setCaseDifferencePenalty(11111);
		translationMemoryProfile.setIsWhiteSpaceSensitiveLeveraging(true);
		translationMemoryProfile.setWhiteSpaceDifferencePenalty(System.currentTimeMillis());
		translationMemoryProfile.setIsCodeSensitiveLeveraging(true);
		translationMemoryProfile.setCodeDifferencePenalty(System.currentTimeMillis());
		translationMemoryProfile.setIsMultiLingualLeveraging(true);
		translationMemoryProfile.setMultipleExactMatches("LATEST"); /// need in {LATEST,OLDEST,DEMOTED}
		translationMemoryProfile.setMultipleExactMatchesPenalty(System.currentTimeMillis());
		translationMemoryProfile.setFuzzyMatchThreshold(System.currentTimeMillis());
		translationMemoryProfile.setNumberOfMatchesReturned(System.currentTimeMillis());
		translationMemoryProfile.setIsLatestMatchForReimport(true);
		translationMemoryProfile.setIsTypeSensitiveLeveragingForReimp(true);
		translationMemoryProfile.setTypeDifferencePenaltyForReimp(System.currentTimeMillis());
		translationMemoryProfile.setIsMultipleMatchesForReimp(true);
		translationMemoryProfile.setMultipleMatchesPenalty(System.currentTimeMillis());
		translationMemoryProfile.setDynLevFromGoldTm(true);
		translationMemoryProfile.setDynLevFromInProgressTm(true);
		translationMemoryProfile.setDynLevFromPopulationTm(true);
		translationMemoryProfile.setDynLevFromReferenceTm(true);
		
		HashSet projectTMsToLeverageFromSet = new HashSet();
		LeverageProjectTM leverageProjectTM = new LeverageProjectTM();
//		leverageProjectTM.setId(123456789);
		translationMemoryProfile.setProjectTMsToLeverageFromSet(projectTMsToLeverageFromSet);
		try
		{
			HibernateUtil.save(leverageProjectTM);
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(translationMemoryProfile);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = translationMemoryProfile.getId();
			}
			else
			{
				assertFalse(true);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}

	}

	public void testGet()
	{
		try
		{
			TranslationMemoryProfile translationMemoryProfile = (TranslationMemoryProfile) HibernateUtil.get(TranslationMemoryProfile.class,
					obj_id);
			if (translationMemoryProfile != null)
			{
				System.out.println("Geted object id is " + translationMemoryProfile.getId());
			}
			else
			{
				assertFalse(true);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}

	}

	public void testDelete()
	{
		int count = 0;
		try
		{
			TranslationMemoryProfile translationMemoryProfile = (TranslationMemoryProfile) HibernateUtil.get(TranslationMemoryProfile.class,
					obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(translationMemoryProfile);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
