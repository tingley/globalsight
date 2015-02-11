package com.globalsight.machineTranslation.iptranslator.request;

import java.util.List;

public class DocumentTranslationRequest extends Request
{

    private static final long serialVersionUID = 1L;

    private String from;
    private String to;
    private List<Document> documents;

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public List<Document> getDocuments()
    {
        return documents;
    }

    public void setDocuments(List<Document> documents)
    {
        this.documents = documents;
    }

    @Override
    public String toString()
    {
        String toString = "DocumentTranslationRequest [key=" + super.getKey()
                + ", from=" + from + ", to=" + to + ", files: [";
        if (this.documents != null && !documents.isEmpty())
        {
            for (Document doc : this.documents)
            {
                toString += doc.toString();
            }
        }
        toString += "]]";
        return toString;
    }
}
