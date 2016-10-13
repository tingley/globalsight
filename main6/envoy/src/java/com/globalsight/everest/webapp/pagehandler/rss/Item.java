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

import com.globalsight.everest.persistence.PersistentObject;


public class Item extends PersistentObject
{
	private static final long serialVersionUID = 516577955463352566L;

	private Feed feed = null;
	private String title = null;
    private String link = null;
    private String description = null;
    private String author = null;
    private String pubDate = null;
    private int status = 0;
    private java.util.Calendar publishedDate = null;
    private int isRead = 0;

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getPubDate() {
		return pubDate;
	}
	
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}
	
	public boolean equals(Object obj) {
		boolean isEquals = false;
		if (obj != null && obj instanceof Item) {
			Item item = (Item) obj;
			isEquals = (this.title.equals(item.title)
					&& this.pubDate.equals(item.pubDate));
		}
		
		return isEquals;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public java.util.Calendar getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(java.util.Calendar publishedDate) {
		this.publishedDate = publishedDate;
	}

	public int getIsRead() {
		return isRead;
	}

	public void setIsRead(int isRead) {
		this.isRead = isRead;
	}
	
	
}
