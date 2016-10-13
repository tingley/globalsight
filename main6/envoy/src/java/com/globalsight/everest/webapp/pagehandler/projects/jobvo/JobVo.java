package com.globalsight.everest.webapp.pagehandler.projects.jobvo;

public class JobVo {
	private String priority;
	private String id;
	private String groupId;
	private String name;
	private String project;
	private String sourceLocale;
	private String wordcount;
	private String createDate;
	private String plannedCompletionDate;
	private String statues;
	private String displayStatues;
	private String estimatedTranslateCompletionDate;
	private String style;
	private boolean hasDetail = true;
	
	private String textType;

	public String getWordcount() {
		return wordcount;
	}

	public void setWordcount(String wordcount) {
		this.wordcount = wordcount;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGroupId()
	{
		return groupId;
	}

	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getSourceLocale() {
		return sourceLocale;
	}

	public void setSourceLocale(String sourceLocale) {
		this.sourceLocale = sourceLocale;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getPlannedCompletionDate() {
		return plannedCompletionDate;
	}

	public void setPlannedCompletionDate(String plannedCompletionDate) {
		this.plannedCompletionDate = plannedCompletionDate;
	}

	public String getStatues() {
		return statues;
	}

	public void setStatues(String statues) {
		this.statues = statues;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getDisplayStatues() {
		return displayStatues;
	}

	public void setDisplayStatues(String displayStatues) {
		this.displayStatues = displayStatues;
	}

	public String getTextType() {
		return textType;
	}

	public void setTextType(String textType) {
		this.textType = textType;
	}

	public String getEstimatedTranslateCompletionDate() {
		return estimatedTranslateCompletionDate;
	}

	public void setEstimatedTranslateCompletionDate(
			String estimatedTranslateCompletionDate) {
		this.estimatedTranslateCompletionDate = estimatedTranslateCompletionDate;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public boolean getHasDetail() {
		return hasDetail;
	}

	public void setHasDetail(boolean hasDetail) {
		this.hasDetail = hasDetail;
	}
}
