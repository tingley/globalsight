package com.globalsight.ling.docproc.extractor;

import java.io.File;

public class FileSet
{
    private String methodName = null;
    private File sourceFile = null;
    private File answerFile = null;
    private File roundtripFile = null;
    
    public FileSet()
    {
    }
    
    public FileSet(File sourceFile, File answerFile, File roundtripFile)
    {
        this.sourceFile = sourceFile;
        this.answerFile = answerFile;
        this.roundtripFile = roundtripFile;
    }
    
    public FileSet(String methodName, File sourceFile, File answerFile, File roundtripFile)
    {
        this.methodName = methodName;
        this.sourceFile = sourceFile;
        this.answerFile = answerFile;
        this.roundtripFile = roundtripFile;
    }
    
    public String getMethodName()
    {
        return methodName;
    }
    
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }
    
    public File getSourceFile()
    {
        return sourceFile;
    }
    public void setSourceFile(File sourceFile)
    {
        this.sourceFile = sourceFile;
    }
    public File getAnswerFile()
    {
        return answerFile;
    }
    public void setAnswerFile(File answerFile)
    {
        this.answerFile = answerFile;
    }
    public File getRoundtripFile()
    {
        return roundtripFile;
    }
    public void setRoundtripFile(File roundtripFile)
    {
        this.roundtripFile = roundtripFile;
    }

    
}
