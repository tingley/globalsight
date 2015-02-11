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

package com.globalsight.terminology.termleverager;

import java.util.Comparator;

/**
 * This class sorts TargetTerms by
 * - target locale
 * - term.status = "preferred" first, everything else alphabetically.
 */
public class TargetTermComparator
    implements Comparator
{
    public int compare(Object p_object1, Object p_object2)
    {
        TermLeverageResult.TargetTerm targetTerm1 =
            (TermLeverageResult.TargetTerm)p_object1;
        TermLeverageResult.TargetTerm targetTerm2 =
            (TermLeverageResult.TargetTerm)p_object2;

		String locale1 = targetTerm1.getLocale();
		String locale2 = targetTerm2.getLocale();

		int cmp = locale1.compareTo(locale2);
		if (cmp != 0)
		{
			return cmp;
		}

		// TODO: term penalties not implemented yet. Consider both
		// preferred terms, and approved terms (entries).

		// Target terms in same locale. Sort preferred term first.
		String usage1 = targetTerm1.getUsage();
		String usage2 = targetTerm2.getUsage();

		if (usage1.equals("preferred"))
		{
			return -1;
		}
		else if (usage2.equals("preferred"))
		{
			return 1;
		}

		// No preferred term, sort terms alphabetically.

		String term1 = targetTerm1.getMatchedTargetTerm();
		String term2 = targetTerm2.getMatchedTargetTerm();

		return term1.compareTo(term2);
    }
}
