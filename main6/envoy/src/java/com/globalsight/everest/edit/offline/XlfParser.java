package com.globalsight.everest.edit.offline;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class XlfParser
{
    @SuppressWarnings("rawtypes")
    public String getBody(Document doc) throws Exception
	{
		Element root = getRootNode(doc);
		List transUnitList = getTransunitList(getTransunitNodeList(root));
		Iterator iterator = transUnitList.iterator();
		StringBuffer result = new StringBuffer();
		while (iterator.hasNext())
		{
			TransUnitInner transUnit = (TransUnitInner) iterator.next();
			result.append(XliffConstants.WARN_SIGN).append(transUnit.getId());
            if (transUnit.getTargetState() != null
                    && transUnit.getTargetState().trim().length() > 0)
			{
    			result.append(XliffConstants.NEW_LINE);
                result.append(
                        AmbassadorDwUpConstants.SEGMENT_XLF_TARGET_STATE_KEY
                                + " ").append(transUnit.getTargetState());
			}
			result.append(XliffConstants.NEW_LINE);
			result.append(transUnit.getTarget());
			result.append(XliffConstants.NEW_LINE);
			result.append(XliffConstants.NEW_LINE);
		}

		return result.toString();
	}

    @SuppressWarnings("rawtypes")
    public Iterator getTransunitNodeList(Element root) {
		Iterator transUnits = null;
        LoopOut: for (Iterator iFile = root
                .elementIterator(XliffConstants.FILE); iFile.hasNext();) {
            Element file = (Element) iFile.next();
            for (Iterator iBody = file.elementIterator(XliffConstants.BODY); iBody
                    .hasNext();) {
                Element body = (Element) iBody.next();
                transUnits = body.elementIterator(XliffConstants.TRANS_UNIT);
                break LoopOut;
            }
        }
        return transUnits;
	}

    @SuppressWarnings("rawtypes")
    public List getTransunitList(Iterator transunitNodes) {
		TransUnitInner transUnitObj = null;
		List<TransUnitInner> list = new ArrayList<TransUnitInner>();

		while (transunitNodes.hasNext()) {
			Element transUnit = (Element) transunitNodes.next();
			Attribute translate = transUnit.attribute(XliffConstants.TRANSLATE);
			// do not upload segments which attribute : translate = no
			if (translate != null && "no".equalsIgnoreCase(translate.getValue()))
			{
			    continue;
			}
			
			Attribute id = transUnit.attribute(XliffConstants.ID);
//			Attribute matchType = transUnit.attribute(XliffConstants.EXTRADATA);
			Element source = transUnit.element(XliffConstants.SOURCE);
			Element target = transUnit.element(XliffConstants.TARGET);
			String targetState = target.attributeValue(XliffConstants.STATE);

			transUnitObj = new TransUnitInner();
			transUnitObj.setId(id.getText());
//			transUnitObj.setMatchType(matchType.getText());
			transUnitObj.setSource(source.getText());
			transUnitObj.setTarget(target.getText());
			if (targetState != null && targetState.trim().length() > 0)
			{
			    transUnitObj.setTargetState(targetState);
			}

			list.add(transUnitObj);
		}

		return list;
	}

	public String parseToTxt(Document doc) throws Exception {
		String result = "";
		String header = getHeader(doc);
		String body = getBody(doc);
		String end = getEnd();
		result = header + body + end;
		return result;
	}

	public Document getDocument(Reader reader) throws DocumentException {
		SAXReader saxReader = new SAXReader();
		Document document = (Document) saxReader.read(reader);
		return document;
	}

	public Document getDocument(File file) throws Exception {
		SAXReader saxReader = new SAXReader();
		Document document = (Document) saxReader.read(file);
		return document;
	}

	public Element getRootNode(Document doc) throws Exception {
		Element root = doc.getRootElement();
		return root;
	}

	public String getEnd() throws Exception {
		String result = "";
		result += "# END GlobalSight Download File";

		return result;
	}

	public String getAnnotation(Document doc) throws Exception {
		String result = doc.asXML();

		Element root = doc.getRootElement();

		for (Iterator iFile = root.elementIterator(XliffConstants.FILE); iFile
				.hasNext();) {
			Element file = (Element) iFile.next();
			for (Iterator iheader = file.elementIterator(XliffConstants.HEADER); iheader
					.hasNext();) {
				Element header = (Element) iheader.next();
				result = header.element(XliffConstants.NOTE).getText();
			}
		}

		return result;
	}

	public String getHeader(Document doc) throws Exception {
		String annotation = getAnnotation(doc);
		String encoding = getEncoding(annotation);
		String documentFormat = getDocumentFormat(annotation);
		String placeholderFormat = getPlaceholderFormat(annotation);
		String sourceLocale = getSourceLocale(annotation);
		String targetLocale = getTargetLocale(annotation);
		String pageId = getPageId(annotation);
		String workFlowId = getWorkFlowId(annotation);
		String taskId = getTaskId(annotation);
		String exactMatchWordCount = getExactMatchWordCount(annotation);
		String fuzzyMatchWordCount = getFuzzyMatchWordCount(annotation);
		String editAll = getEditAll(annotation);
		//GlobalSight Offline Toolkit Format:
		String gsToolkitFormat = getToolkitFormat(annotation);

		StringBuilder result = new StringBuilder();
		result.append("# GlobalSight Download File");
		result.append(XliffConstants.NEW_LINE);

		result.append(XliffConstants.WARN_SIGN).append(XliffConstants.EXCODING)
				.append(XliffConstants.ONE_SPACE).append(encoding);
		result.append(XliffConstants.NEW_LINE);

		result.append(XliffConstants.WARN_SIGN).append(
				XliffConstants.DOCUMENT_FORMAT)
				.append(XliffConstants.ONE_SPACE).append(documentFormat);
		result.append(XliffConstants.NEW_LINE);

		result.append(XliffConstants.WARN_SIGN).append(
				XliffConstants.PLACEHOLDER_FORMAT).append(
				XliffConstants.ONE_SPACE).append(placeholderFormat);
		result.append(XliffConstants.NEW_LINE);

		result.append(XliffConstants.WARN_SIGN).append(
				XliffConstants.SOURCE_LOCALE).append(XliffConstants.ONE_SPACE)
				.append(sourceLocale);
		result.append(XliffConstants.NEW_LINE);

		result.append(XliffConstants.WARN_SIGN).append(
				XliffConstants.TARGET_LOCALE).append(XliffConstants.ONE_SPACE)
				.append(targetLocale);
		result.append(XliffConstants.NEW_LINE);

		result.append(XliffConstants.WARN_SIGN).append(XliffConstants.PAGE_ID)
				.append(XliffConstants.ONE_SPACE).append(pageId);
		result.append(XliffConstants.NEW_LINE);

		result.append(XliffConstants.WARN_SIGN).append(
				XliffConstants.WORKFLOW_ID).append(XliffConstants.ONE_SPACE)
				.append(workFlowId);
		result.append(XliffConstants.NEW_LINE);

		result.append(XliffConstants.WARN_SIGN).append(XliffConstants.TASK_ID)
				.append(XliffConstants.ONE_SPACE).append(taskId);
		result.append(XliffConstants.NEW_LINE);

		result.append(XliffConstants.WARN_SIGN).append(
				XliffConstants.EXACT_MATCH_WORD_COUNT).append(
				XliffConstants.ONE_SPACE).append(exactMatchWordCount);
		result.append(XliffConstants.NEW_LINE);

		result.append(XliffConstants.WARN_SIGN).append(
				XliffConstants.FUZZY_MATCH_WORD_COUNT).append(
				XliffConstants.ONE_SPACE).append(fuzzyMatchWordCount);
		result.append(XliffConstants.NEW_LINE);

		result.append(XliffConstants.WARN_SIGN).append(XliffConstants.EDIT_ALL)
				.append(XliffConstants.ONE_SPACE).append(editAll);
		result.append(XliffConstants.NEW_LINE);
		
        result.append(XliffConstants.WARN_SIGN).append(XliffConstants.GS_TOOLKIT_FORMAT)
                .append(XliffConstants.ONE_SPACE).append(gsToolkitFormat);
        result.append(XliffConstants.NEW_LINE);

		result.append(XliffConstants.NEW_LINE);

		return result.toString();
	}

	public String getEncoding(String annotation) {
		return getInfoFromAnnotation(annotation, XliffConstants.EXCODING);
	}

	public String getDocumentFormat(String annotation) {
		return getInfoFromAnnotation(annotation, XliffConstants.DOCUMENT_FORMAT);
	}

	public String getSourceLocale(String annotation) {
		return getInfoFromAnnotation(annotation, XliffConstants.SOURCE_LOCALE);
	}

	public String getTargetLocale(String annotation) {
		return getInfoFromAnnotation(annotation, XliffConstants.TARGET_LOCALE);
	}

	public String getPageId(String annotation) {
		return getInfoFromAnnotation(annotation, XliffConstants.PAGE_ID);
	}

	public String getWorkFlowId(String annotation) {
		return getInfoFromAnnotation(annotation, XliffConstants.WORKFLOW_ID);
	}

	public String getTaskId(String annotation) {
		return getInfoFromAnnotation(annotation, XliffConstants.TASK_ID);
	}

	public String getExactMatchWordCount(String annotation) {
		return getInfoFromAnnotation(annotation,
				XliffConstants.EXACT_MATCH_WORD_COUNT);
	}

	public String getFuzzyMatchWordCount(String annotation) {
		return getInfoFromAnnotation(annotation,
				XliffConstants.FUZZY_MATCH_WORD_COUNT);
	}

	public String getPlaceholderFormat(String annotation) {
		return getInfoFromAnnotation(annotation,
				XliffConstants.PLACEHOLDER_FORMAT);
	}

	public String getNoMatchWordCount(String annotation) {
		return getInfoFromAnnotation(annotation,
				XliffConstants.NO_MATCH_WORD_COUNT);
	}

	public String getEditAll(String annotation) {
		return getInfoFromAnnotation(annotation, XliffConstants.EDIT_ALL);
	}
	
	public String getToolkitFormat(String annotation) {
	    if (!annotation.contains(XliffConstants.GS_TOOLKIT_FORMAT))
	    {
	        return "Xliff";
	    }
	    
        return getInfoFromAnnotation(annotation, XliffConstants.GS_TOOLKIT_FORMAT);
    }

	public String getInfoFromAnnotation(String annotation, String condition) {
		String documentFormat = "";
		int start = annotation.indexOf(condition);
		start = start + condition.length();
		documentFormat = annotation.substring(start);
		int end = documentFormat.indexOf("\n");
		return annotation.substring(start, start + end);
	}

	private class TransUnitInner {
		private String id;
		private String source;
		private String target;
		private String matchType;
		// "state" attribute of "target"
		private String targetState;

		public String getMatchType() {
			return matchType;
		}

		public void setMatchType(String matchType) {
			this.matchType = matchType;
		}

		public String getId() {
			return id;
		}

		public void setId(String string) {
			this.id = string;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}

		public String getTargetState()
        {
            return targetState;
        }

        public void setTargetState(String targetState)
        {
            this.targetState = targetState;
        }

        public String toString() {
			StringBuffer result = new StringBuffer();
			result.append("id:").append(id).append("\n");
			result.append("matchType:").append(matchType).append("\n");
			result.append("source:").append(source).append("\n");
			result.append("target:").append(target).append("\n");
			return result.toString();

		}

	}

}
