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
package com.globalsight.everest.webapp.pagehandler.rss;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * The interface for RSS activities.
 */
public interface RSSPersistenceManager 
{
	/**
	 * RSS Reader/Writer manager service name.
	 */
	public static final String SERVICE_NAME = "RSSManager";
	
	/**
	 * Save RSS feed info into data store.
	 * @param feed
	 * @throws RemoteException
	 */
	public int subscribe(Feed feed) throws RemoteException;
	
	/**
	 * Unsubscribe the RSS specified by the parameter URL.
	 * @param rssUrl
	 * @throws RemoteException
	 */
	public void unsubscribe(String rssUrl) throws RemoteException;
	
	/**
	 * Get feed by RSS url.
	 * @param rssUrl
	 * @return Feed
	 * @throws RemoteException
	 */
	public Feed getFeedByRssUrl(String rssUrl) throws RemoteException;
	
	/**
	 * Get all RSS feed info.
	 * Note:this may use much memory if item data is huge.
	 * @return List: Feed
	 * @throws RemoteException 
	 */
	public List getAllRSSFeeds() throws RemoteException;

	/**
	 * Get all RSS feed info with special string.
	 * Note:this may use much memory if item data is huge.
	 * @return List: Feed
	 * @throws RemoteException 
	 */
	public List getAllRSSFeeds(String p_str) throws RemoteException;
	
	/**
	 * Get all RSS feed url list.
	 * @return List: String
	 */
	public List getAllRSSFeedURLs() throws RemoteException;
	
	/**
	 * Get <Item> data for current page.
	 * @param rssUrl
	 * @param currentPageNum: The page num to go to.
	 * @param itemNumPerPage: Item number per page
	 * @return List: Item
	 * @throws RemoteException
	 */
	public ArrayList getItemsForPage(String rssUrl, int currentPageNum, int itemNumPerPage)
	    throws RemoteException;

	/**
	 * Get the RSS URL whose 'IS_DEFAULT' is 'Y'.
	 * There should only one 'Y' in all RSS feeds.
	 * @return String :RSS URL
	 */
	public String getDefaultFeedUrl();
	
	/**
	 * Get the feed with speical id
	 * @return Feed
	 */
	public Feed getFeed(long p_id);
	
	public Item getItem(long p_id);
	
	public boolean refresh(Feed feed, ArrayList<Item> items) throws RemoteException;
	
	public boolean markRead(Item p_item);
}
