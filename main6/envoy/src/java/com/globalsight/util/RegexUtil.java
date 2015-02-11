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
package com.globalsight.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used for Regular expression 
 */
public class RegexUtil {

	// Regular expression string
	public static final String email_Expr = "^([a-zA-Z0-9_\\.\\-\\+])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";
	public static final String userId_Expr = "[^%&!#~*\\+()<>\\|:\\\"\\[\\]',;=?$\\x22]+";
	
	/**
	 * Validate the email address
	 * 
	 * @param p_mail 	the email address
	 */
	public static boolean validEmail(String p_mail){
		return validValueByExpression(p_mail,email_Expr);
	}
	
	/**
	 * Validate the value by the regular expression.
	 * 
	 * @param p_value		the value need to be validate by expression
	 * @param p_expr		regular expression
	 */
	public static boolean validValueByExpression(String p_value, String p_expr){
		boolean result = false;
		
		if(p_value == null || p_expr==null){
			return false;
		}
		
		CharSequence inputStr = p_value;  
		Pattern pattern = Pattern.compile(p_expr);
		Matcher matcher = pattern.matcher(inputStr);  
		if(matcher.matches()){  
			result = true;  
		}  
		return result; 
	}
	
    public static boolean validUserId(String p_userId) {
        return validValueByExpression(p_userId, userId_Expr);
    }
    
    public static void main(String[] args) {
        RegexUtil util = new RegexUtil();
        System.out.println("Test....");
        System.out.println(RegexUtil.validUserId("test"));
        System.out.println(RegexUtil.validUserId("test!"));
        System.out.println(RegexUtil.validUserId("test<"));
        System.out.println(RegexUtil.validUserId("test~"));
        System.out.println(RegexUtil.validUserId("test("));
        System.out.println(RegexUtil.validUserId("test)"));
        System.out.println(RegexUtil.validUserId("test["));
        System.out.println(RegexUtil.validUserId("test]"));
        System.out.println(RegexUtil.validUserId("test$"));
        System.out.println(RegexUtil.validUserId("test+"));
        System.out.println(RegexUtil.validUserId("test="));
        System.out.println(RegexUtil.validUserId("test|"));
        System.out.println(RegexUtil.validUserId("test&"));
        System.out.println(RegexUtil.validUserId("test#"));
        System.out.println(RegexUtil.validUserId("\\test?"));
    }
}
