package com.globalsight.terminology;

public class TermbaseTestHelper
{
    public static Termbase getTermbase(com.globalsight.terminology.java.Termbase p_javaTB)
    {
        return new Termbase(p_javaTB.getId(), p_javaTB.getName(), p_javaTB.getDescription(), 
                            p_javaTB.getDefination(), "1");
    }
}
