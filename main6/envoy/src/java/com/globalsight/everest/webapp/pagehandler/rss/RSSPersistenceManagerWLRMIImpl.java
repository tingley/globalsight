package com.globalsight.everest.webapp.pagehandler.rss;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

public class RSSPersistenceManagerWLRMIImpl 
	extends RemoteServer
	implements RSSPersistenceManagerWLRemote
{
	RSSPersistenceManager m_localReference = null;

	public RSSPersistenceManagerWLRMIImpl() throws java.rmi.RemoteException
	{
		super(RSSPersistenceManager.SERVICE_NAME);
		m_localReference = new RSSPersistenceManagerLocal();
	}
	
    public Object getLocalReference()
    {
        return m_localReference;
    }

	public int subscribe(Feed feed) throws RemoteException 
	{
		return this.m_localReference.subscribe(feed);
	}
	
	public Feed getFeedByRssUrl(String rssUrl) throws RemoteException
	{
		return this.m_localReference.getFeedByRssUrl(rssUrl);
	}

	public void unsubscribe(String rssUrl) throws RemoteException
	{
		this.m_localReference.unsubscribe(rssUrl);
	}
	
	public List getAllRSSFeeds() throws RemoteException
	{
		return this.m_localReference.getAllRSSFeeds();
	}

	public List getAllRSSFeedURLs() throws RemoteException
	{
		return this.m_localReference.getAllRSSFeedURLs();
	}
	
	public ArrayList getItemsForPage(String rssUrl, int currentPageNum, int itemNumPerPage)
    throws RemoteException 
    {
		return this.m_localReference.getItemsForPage(rssUrl, currentPageNum, itemNumPerPage);
	}
	
	public String getDefaultFeedUrl()
	{
		return this.m_localReference.getDefaultFeedUrl();
	}
	
	public Feed getFeed(long p_id) {
		return this.m_localReference.getFeed(p_id);
	}
	
	public List getAllRSSFeeds(String p_str) throws RemoteException {
		return this.m_localReference.getAllRSSFeeds(p_str);
	}
	
	public Item getItem(long p_id) {
		return this.m_localReference.getItem(p_id);
	}
	
	public boolean refresh(Feed feed, ArrayList<Item> items) throws RemoteException {
		return this.m_localReference.refresh(feed, items);
	}
	
	public boolean markRead(Item p_item) {
		return this.m_localReference.markRead(p_item);
	}
}
