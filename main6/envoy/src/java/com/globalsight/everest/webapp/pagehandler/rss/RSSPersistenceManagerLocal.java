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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;

/**
 * RSSPersistenceManagerLocal provides the main implementation of the
 * RSSPersistenceManager interface.
 */

public class RSSPersistenceManagerLocal implements RSSPersistenceManager
{
    private static final Logger s_logger = Logger
            .getLogger(RSSPersistenceManagerLocal.class);

    /**
     * Save RSS feed info into data store.
     * 
     * @param feed
     * @throws RemoteException
     * @return int 0 -- Existed, 1 -- Succ, -1 -- Fail
     */
    public int subscribe(Feed feed) throws RemoteException
    {
        try
        {
            boolean isExsited = false;

            List allRssUrls = getAllRSSFeedURLs();
            if (allRssUrls != null && allRssUrls.size() > 0)
            {
                for (int i = 0; i < allRssUrls.size(); i++)
                {
                    String rssUrl = (String) allRssUrls.get(i);
                    if (feed.getRssUrl().equals(rssUrl))
                    {
                        isExsited = true;
                        break;
                    }
                }
            }

            // The newly subscribed RSS is set as default,
            // update all the previous RSS "is_default" to "N".
            if (isExsited == false)
            {
                HibernateUtil.save(feed);
                // updateAllToNotDefault();
                return 1;
            }
            else
                return 0;

        }
        catch (Exception e)
        {
            String msg = "Failed to subscribe RSS for :" + feed.getRssUrl();
            // throw new GeneralException(msg, e);
            return -1;
        }
    }

    public boolean refresh(Feed feed, ArrayList<Item> items)
            throws RemoteException
    {
        try
        {
            for (Item item : items)
            {
                HibernateUtil.save(item);
            }
            return true;
        }
        catch (Exception e)
        {
            String msg = "Failed to refresh RSS for :" + feed.getRssUrl();
            // throw new GeneralException(msg, e);
            return false;
        }
    }

    /**
     * Update all feed "IS_DEFAULT" to "N".
     */
    public void updateAllToNotDefault()
    {
        try
        {
            List allFeeds = getAllRSSFeeds();
            if (allFeeds != null && allFeeds.size() > 0)
            {
                for (int i = 0; i < allFeeds.size(); i++)
                {
                    Feed feed = (Feed) allFeeds.get(i);
                    feed.setIsDefault(false);
                }
            }

            HibernateUtil.update(allFeeds);

        }
        catch (Exception e)
        {

        }

    }

    /**
     * Unsubscribe the RSS specified by the parameter URL.
     * 
     * @param rssUrl
     * @throws RemoteException
     */
    public void unsubscribe(String rssUrl) throws RemoteException
    {
        Feed feed = getFeedByRssUrl(rssUrl);

        if (feed != null)
        {
            try
            {
                HibernateUtil.delete(feed);
            }
            catch (Exception e)
            {
                String msg = "Failed to subscribe RSS for :" + rssUrl;
                throw new GeneralException(msg, e);
            }
        }
    }

    /**
     * Get feed by RSS url.
     * 
     * @param rssUrl
     * @return Feed
     * @throws RemoteException
     */
    public Feed getFeedByRssUrl(String rssUrl) throws RemoteException
    {
        Feed feed = null;

        String hql = "from Feed f where f.rssUrl = '" + rssUrl + "'";
        HashMap map = null;
        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " and f.companyId = :companyId";
            map = new HashMap();
            map.put("companyId", Long.parseLong(currentId));
        }

        try
        {
            List rssFeeds = null;
            if (map == null)
                rssFeeds = HibernateUtil.search(hql);
            else
                rssFeeds = HibernateUtil.search(hql, map);
            if (rssFeeds != null && rssFeeds.size() > 0)
            {
                feed = (Feed) rssFeeds.get(0);
            }
        }
        catch (Exception e)
        {
            String msg = "Failed to get RSS feed by :" + rssUrl;
            throw new GeneralException(msg, e);
        }

        return feed;
    }

    /**
     * Get all RSS feed info. Note:this may use much memory if item data is
     * huge.
     * 
     * @return List: Feed
     */
    public List getAllRSSFeeds() throws RemoteException
    {
        List allRSSFeeds = null;
        String hql = "from Feed f";

        HashMap map = null;
        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " where f.companyId = :companyId";
            map = new HashMap();
            map.put("companyId", Long.parseLong(currentId));
        }

        try
        {
            if (map == null)
                allRSSFeeds = HibernateUtil.search(hql);
            else
                allRSSFeeds = HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            String msg = "Failed to get all RSS feed URL info";
            throw new GeneralException(msg, e);
        }

        return allRSSFeeds;
    }

    /**
     * Get all RSS feed info according to the query condition
     * 
     * @param p_str
     *            content of RSS_URL
     * @return List : Feed
     */
    public List getAllRSSFeeds(String p_str)
    {
        List result = null;
        String hql = "from Feed f";
        try
        {
            if (p_str == null || p_str.trim().equals(""))
                return getAllRSSFeeds();

            hql += " where f.rssUrl LIKE '%" + p_str + "%'";

            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and f.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }
            if (map == null)
                result = HibernateUtil.search(hql);
            else
                result = HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            String msg = "Failed to get queried RSS feed.";
            throw new GeneralException(msg, e);
        }

        return result;
    }

    public Feed getFeed(long p_id)
    {
        Feed feed = null;
        try
        {
            String hql = "from Feed f where f.id = :id";
            Map map = new HashMap();
            map.put("id", p_id);
            Collection feeds = HibernateUtil.search(hql, map);
            Iterator i = feeds.iterator();
            feed = i.hasNext() ? (Feed) i.next() : null;
        }
        catch (Exception pe)
        {
            throw new GeneralException("Failed to get Feed with id is " + p_id,
                    pe);
        }
        return feed;
    }

    /**
     * Get all RSS feed url list.
     * 
     * @return List: String
     */
    public List getAllRSSFeedURLs() throws RemoteException
    {
        List allRSSFeedURLs = new ArrayList();
        ;

        List allRSSFeeds = getAllRSSFeeds();
        if (allRSSFeeds != null && allRSSFeeds.size() > 0)
        {
            for (int i = 0; i < allRSSFeeds.size(); i++)
            {
                Feed feed = (Feed) allRSSFeeds.get(i);
                allRSSFeedURLs.add(feed.getRssUrl());
            }
        }

        return allRSSFeedURLs;
    }

    /**
     * Get <Item> data for current page.
     * 
     * @param rssUrl
     * @param currentPageNum
     *            : The page num to go to.
     * @param itemNumPerPage
     *            : Item number per page
     * @return List: Item
     * @throws RemoteException
     */
    public ArrayList getItemsForPage(String rssUrl, int currentPageNum,
            int itemNumPerPage) throws RemoteException
    {
        List itemList = new ArrayList();
        List resultItemList = new ArrayList();
        ArrayList result = new ArrayList();

        if (rssUrl != null)
        {
            Feed feed = getFeedByRssUrl(rssUrl);
            Set items = feed.getItems();
            int totalItemNum = items.size();
            int totalPageNum = Math.round(totalItemNum / itemNumPerPage) + 1;
            if (totalItemNum % itemNumPerPage == 0)
                totalPageNum--;
            if (currentPageNum <= 0)
            {
                currentPageNum = 1;
            }
            else if (currentPageNum > totalPageNum)
            {
                currentPageNum = totalPageNum;
            }

            Object[] itemsArray = items.toArray();
            // todo:------------------------------------
            // Arrays.sort(itemsArray);
            List itemsList = Arrays.asList(itemsArray);
            SortUtil.sort(itemsList, new RSSItemComparator());

            int startItemNum = itemNumPerPage * (currentPageNum - 1);
            int endItemNum = itemNumPerPage * currentPageNum;
            if (endItemNum > totalItemNum)
            {
                endItemNum = totalItemNum;
            }

            resultItemList = itemsList.subList(startItemNum, endItemNum);

            result.add(resultItemList);

            int[] params = new int[5];
            params[0] = totalItemNum; // Total item count
            params[1] = totalPageNum; // Total page count
            params[2] = currentPageNum; // Current page number
            params[3] = startItemNum + 1;
            params[4] = endItemNum;
            result.add(params);
        }

        return result;
    }

    /**
     * Get the RSS URL whose 'IS_DEFAULT' is 'Y'. There should only one 'Y' in
     * all RSS feeds.
     * 
     * @return String :RSS URL
     */
    public String getDefaultFeedUrl()
    {
        String defaultFeedUrl = null;

        String hql = "from Feed f where f.isDefault = 'Y'";
        try
        {
            List feedList = HibernateUtil.search(hql);
            if (feedList != null && feedList.size() > 0)
            {
                Feed feed = (Feed) feedList.get(0);
                defaultFeedUrl = feed.getRssUrl();
            }
            else
            {
                // if there is no default url,get the one updated recently.
                String sql = "select * from rss_feed where ID in (select feed_id from rss_item where id in (select max(id) from rss_item))";
                List feeds = HibernateUtil.searchWithSql(Feed.class, sql);
                if (feeds != null && feeds.size() > 0)
                {
                    defaultFeedUrl = ((Feed) feeds.get(0)).getRssUrl();
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Failed to get 'IS_DEFAULT' RSS URL.";
            s_logger.error("msg", e);
        }

        return defaultFeedUrl;
    }

    public Item getItem(long p_id)
    {
        Item item = null;
        try
        {
            item = (Item) HibernateUtil.get(Item.class, p_id);
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
        }
        return item;
    }

    public boolean markRead(Item p_item)
    {
        if (p_item == null)
            return false;
        try
        {
            p_item.setIsRead(1);
            HibernateUtil.save(p_item);
            return true;
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
            return false;
        }
    }
}
