package com.globalsight.everest.util.system.migration;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.globalsight.config.SystemParameter;
import com.globalsight.config.SystemParameterEntityException;
import com.globalsight.config.SystemParameterPersistenceManagerLocal;

/**
 * abstract Migration Object
 * @author Wayne
 *
 */
public abstract class MigrateObj
{
    protected static final Logger CATEGORY = Logger.getLogger(MigrateObj.class);

    public abstract boolean checkIfDoMigration();

    public abstract void doMigration() throws Exception;
    
    protected boolean checkIfDoMigration(String keyname)
    {
        SystemParameterPersistenceManagerLocal sysParaManager = SystemParameterPersistenceManagerLocal
                .getInstance();
        SystemParameter sp = sysParaManager.getAdminSystemParameter(keyname);
        String doMigrationV = sp == null ? "false" : sp.getValue();
        boolean doMigration = "true".equalsIgnoreCase(doMigrationV);
        return doMigration;
    }

    protected SystemParameter updateMigrationKey(String keyname)
            throws SystemParameterEntityException, RemoteException
    {
        SystemParameterPersistenceManagerLocal sysParaManager = SystemParameterPersistenceManagerLocal
                .getInstance();
        SystemParameter sp = sysParaManager.getAdminSystemParameter(keyname);
        sp.setValue("false");
        return sysParaManager.updateAdminSystemParameter(sp);
    }
}
