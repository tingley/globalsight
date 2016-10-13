package com.globalsight.machineTranslation.iptranslator.response;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.machineTranslation.iptranslator.request.Document;

public class DocumentTranslationResponse
{
    private List<Document> documents;

    public DocumentTranslationResponse()
    {
        documents = new ArrayList<Document>();
    }

    public List<Document> getDocuments()
    {
        return documents;
    }

    public void setDocuments(List<Document> documents)
    {
        this.documents = documents;
    }
}
