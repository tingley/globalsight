package com.adobe.cq.sling;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just a simple DS Component
 */
@Component(metatype=true)
@Service
public class SimpleDSComponent implements Runnable {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private BundleContext bundleContext;
    
    public void run() {
        logger.info("Running...");
    }
    
    protected void activate(ComponentContext ctx) {
        this.bundleContext = ctx.getBundleContext();
    }
    
    protected void deactivate(ComponentContext ctx) {
        this.bundleContext = null;
    }

}