package com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity;

import org.junit.Test;

import com.globalsight.everest.company.CompanyThreadLocal;

public class PerplexityManagerTest
{

    @Test
    public void testGetAllPerplexity()
    {
        CompanyThreadLocal.getInstance().setIdValue(1l);
        PerplexityManager.getAllPerplexity();
    }

}
