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
package com.globalsight.everest.costing;

import java.math.BigDecimal;

/**
 * This class provids some helper methods for the converting from float to bigdecimal.
 * 
 * The costing engine does everything in float. This is bad as floating point
 * math can lead to rounding errors and therefore should not be used for
 * financial calculations. Ideall the costing engine should do everything in
 * java.math.BigDecimal. That would affect toplink mappings, schema, etc. and
 * may not be needed after all.
 * 
 * If the places where the calculations are done in the costing engine are done
 * in BigDecimal, then the fact that the internal values are stored as float
 * should not be as relevant.
 * 
 * The areas where calculations are done (multiply, add, subtract) should be
 * changed to use BigDecimal for the actual calculation.
 *
 * 
 */
public class BigDecimalHelper 
{
	/**
	 * Convert two float parameters into bigdecimal, multiply together 
	 * and return float value.
	 * @param a value1 to be multiplied
	 * @param b value2 to be multiplied
	 * @return float value <tt>a * b</tt>
	 */
	public static float multiply(float a, float b) 
	{
		BigDecimal a_bd = new BigDecimal(Float.toString(a));
		BigDecimal b_bd = new BigDecimal(Float.toString(b));
		
		return a_bd.multiply(b_bd).floatValue();		
	}
	
    /**
	 * Convert two float parameters into bigdecimal, divide together 
	 * and return float value.
	 * @param a value1 to be divide
	 * @param b value2 to be divide
	 * @return float value <tt>a * b</tt>
	 */
	public static float divide(float a, float b) 
	{
		BigDecimal a_bd = new BigDecimal(Float.toString(a));
		BigDecimal b_bd = new BigDecimal(Float.toString(b));
		
		return a_bd.divide(b_bd,2).floatValue();		
	}
	
	/**
	 * Convert two float value into bigdecimal, add and return float value.
	 * @param a value1 to be added
	 * @param b value2 to be added
	 * @return float value <tt>a + b</tt>
	 */
	public static float add(float a, float b) 
	{
		BigDecimal a_bd = new BigDecimal(Float.toString(a));
		BigDecimal b_bd = new BigDecimal(Float.toString(b));
		
		return a_bd.add(b_bd).floatValue();		
	}
	
	/**
	 * Convert the float value members in array to bigdecimal, 
	 * add and return float value.
	 * @param param the float value members in array
	 * @return float value
	 */
	public static float add(float[] param) 
	{
		if(param == null)
                                {
			return 0f ;
                                }
		BigDecimal start = new BigDecimal(Float.toString(param[0]));
		for(int i=1; i<param.length; i++) 
                                {
			BigDecimal second = new BigDecimal(Float.toString(param[i]));
			start = start.add(second);
		}
		return start.floatValue();		
	}
	
	/**
	 *  Convert two float parameters into bigdecimal,
	 *  Subtract and return float value.
	 * @param a value1 to be subtracted
	 * @param b value2 to be subtracted
	 * @return float value <tt>a - b</tt>
	 */
	public static float subtract(float a, float b)
	{
		BigDecimal a_bd = new BigDecimal(Float.toString(a));
		BigDecimal b_bd = new BigDecimal(Float.toString(b));
		
		return a_bd.subtract(b_bd).floatValue();		
	}
    
	/**
 	* Convert the float parameter into <code>BigDecimal</code>, 
 	* subtract and return <code>BigDecimal</code>
 	* @param a Value1 to be subtracted.
 	* @param b_bd value2 to be subtracted.
 	* @return BigDecimal value <tt>a - b_bd</tt>
 	*/
	public static BigDecimal subtract(float a, BigDecimal b_bd)
	{
		BigDecimal a_bd = new BigDecimal(Float.toString(a));
		return a_bd.subtract(b_bd);
	}

}
