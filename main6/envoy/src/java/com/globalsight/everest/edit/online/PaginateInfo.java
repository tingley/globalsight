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

package com.globalsight.everest.edit.online;

import java.io.Serializable;

import com.globalsight.config.UserParamNames;

/**
 * <p>A data class for encapsulating pagination information for display in
 * the editor's Page Details dialog.</p>
 */
public class PaginateInfo implements Serializable
{
	private static final long serialVersionUID = -969850760809093354L;

	private int totalSegmentNum = 0;
	private int segmentNumPerPage = 
		Integer.parseInt(UserParamNames.EDITOR_SEGMENTS_MAX_NUM_DEFAULT);
	private int totalPageNum = 1;
	private int currentPageNum = 1;

	public PaginateInfo(int p_totalSegmentNum, int p_segmentNumPerPage, int p_currentPageNum)
	{
		if (p_totalSegmentNum > 0) {
			totalSegmentNum = p_totalSegmentNum;
		}
		
		if (p_segmentNumPerPage > 0) {
			segmentNumPerPage = p_segmentNumPerPage;
		} else {
			segmentNumPerPage = totalSegmentNum;
		}
		
		if (segmentNumPerPage == 0) {
			totalPageNum = 1;
		} else {
			if (totalSegmentNum % segmentNumPerPage > 0) {
				totalPageNum = Math.round(totalSegmentNum/segmentNumPerPage) + 1;
			} else {
				totalPageNum = Math.round(totalSegmentNum/segmentNumPerPage);
				if (totalPageNum == 0) {
					totalPageNum = 1;
				}
			}			
		}
		
		setCurrentPageNum(p_currentPageNum);
	}
	
	public int getTotalSegmentNum() {
		return totalSegmentNum;
	}
	
	public void setTotalSegmentNum(int iTotalSegmentNum) {
		totalSegmentNum = iTotalSegmentNum;
	}
	
	public int getSegmentNumPerPage() {
		return segmentNumPerPage;
	}
	
	public void setSegmentNumPerPage(int iSegmentNumPerPage) {
		segmentNumPerPage = iSegmentNumPerPage;
	}
	
	public int getTotalPageNum() {
		return totalPageNum;
	}

	/**
	 * The total page/batch number should be determined by total segment number 
	 * and maximum segment number per page,user should not modify it. 
	 * So remove this method.
	 * 
	 */
//	public void setTotalPageNums(int iTotalPageNum) {
//		totalPageNum = iTotalPageNum;
//	}
	
	public int getCurrentPageNum() {
		return currentPageNum;
	}
	
	/**
	 * Set current page number. 
	 * Make sure current page number is between 1 and the max page number.
	 * 
	 * @param iCurrentPageNum
	 */
	public void setCurrentPageNum(int iCurrentPageNum) {
		if (iCurrentPageNum < 1) 
		{
			currentPageNum = 1;
		}
		else if (iCurrentPageNum > totalPageNum) 
		{
			currentPageNum = totalPageNum;
		}
		else 
		{
			currentPageNum = iCurrentPageNum;			
		}
	}
	
}
