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

package com.globalsight.everest.page.pageupdate;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.log4j.Logger;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.edit.SynchronizationManager;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.PagePersistenceAccessor;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.PageTemplate;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.TemplatePart;
import com.globalsight.everest.page.pageimport.ExtractedFileImportPersistenceHandler;
import com.globalsight.everest.page.pageimport.TemplateGenerator;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImplVo;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImplVo;
import com.globalsight.everest.tuv.TuvJdbcQuery;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.persistence.pageupdate.DeleteTemplatePartPersistenceCommand;
import com.globalsight.persistence.pageupdate.DeleteTuPersistenceCommand;
import com.globalsight.persistence.pageupdate.UpdateGxmlPersistenceCommand;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.gxml.GxmlRootElement;
import com.globalsight.util.mail.MailerConstants;

/**
 * <p>
 * Updates an imported and extracted file. Parses through updated gxml and
 * updates an existing source page and all target pages, thereby preserving
 * existing translations.
 * </p>
 * 
 * <p>
 * Code Note: this is a battlefield. The main problem is that data from the
 * incoming GXML needs to update internal datastructures (GxmlElement, TU, TUV),
 * but then those changes (like setting the TU and TUV's order_num) trigger
 * changes in the incoming GXML because the GXML needs to be persisted as part
 * of the TemplateParts.
 * </p>
 * 
 * <p>
 * So this entire code tries to synchronize data in the new GXML, the in-memory
 * data structures, and the persisted datastructures, going back and forth in
 * the process.
 * </p>
 */
class ExtractedFileUpdater
{
    private static Logger CATEGORY = Logger
            .getLogger(ExtractedFileUpdater.class.getName());

    static private final Integer s_INTONE = new Integer(1);

    static final String EDIT_SUCCESS_MESSAGE = "messageGxmlEditSuccess";

    static final String EDIT_FAILURE_MESSAGE = "messageGxmlEditFailure";

    /** Debugging hook: delay the final commit by this many seconds. */
    static private int s_COMMIT_DELAY = 0;

    /** Debugging hook: add a delay of this many seconds after the final commit. */
    static private int s_AFTER_COMMIT_DELAY = 0;

    static
    {
        try
        {
            ResourceBundle res = ResourceBundle
                    .getBundle("properties/pageupdate");

            try
            {
                String value;

                value = res.getString("commit_delay");
                s_COMMIT_DELAY = Integer.parseInt(value);

                value = res.getString("after_commit_delay");
                s_AFTER_COMMIT_DELAY = Integer.parseInt(value);
            }
            catch (MissingResourceException e)
            {
            }
        }
        catch (MissingResourceException e)
        {
            // Do nothing if configuration file was not found.
        }
    }

    //
    // Members
    //

    private UpdateState m_state = null;

    private DiplomatAPI m_diplomat = null;

    private SynchronizationManager m_syncManager = null;

    private PersistenceService m_persistenceService = null;

    private PageManager m_pageManager = null;

    // Thread instance counter.
    static private int s_counter = 0;

    //
    // package methods
    //
    // All methods are accessed from within the package.
    // PageUpdateAPI provides the interface to classes outside the package.

    //
    // Constructor
    //

    ExtractedFileUpdater(SourcePage p_sourcePage, String p_gxml)
    {
        try
        {
            // retrieve the source page not as a clone "false"
            p_sourcePage = (SourcePage) HibernateUtil.get(SourcePage.class,
                    p_sourcePage.getIdAsLong());
        }
        catch (Exception e)
        {
            CATEGORY.error("Get source page failed with id "
                    + p_sourcePage.getIdAsLong());
            // just go on and use the cloned one passed in
            // tbd - clean up shouldn't be passing in a clone.
        }

        m_state = new UpdateState(p_sourcePage, p_gxml);
        init();
    }

    /**
     * Initializates RMI objects (only called from constructor).
     */
    private void init()
    {
        try
        {
            m_syncManager = ServerProxy.getSynchronizationManager();
            m_pageManager = ServerProxy.getPageManager();
        }
        catch (Exception ex)
        {
            CATEGORY.error("can't init remote objects", ex);
        }
    }

    //
    // Package Methods
    //

    /**
     * Updates a source page with new GXML.
     * 
     * @return list of error strings.
     */
    ArrayList updateSourcePageGxml()
    {
        // validate the gxml and update the UpdateState object
        ExtractedFileValidation validator = new ExtractedFileValidation(
                m_state.getSourcePage(), m_state.getGxml());
        m_state = validator.validateGxml();

        if (!m_state.getValidated())
        {
            return m_state.getValidationMessages();
        }

        if (!validateSourcePage())
        {
            return m_state.getValidationMessages();
        }

        // Start a new thread to perform the actual update in the
        // background so we can return control to the user.

        try
        {
            doUpdate();
        }
        catch (Exception e)
        {
            CATEGORY.error("Could not start background thread to update page",
                    e);
            m_state.setValidated(false);
            m_state.addValidationMessage(e.getMessage());
            return m_state.getValidationMessages();
        }

        return null;
    }

    //
    // Private Methods
    //

    /**
     * Checks if the source page is in the proper state to be updated.
     */
    private boolean validateSourcePage()
    {
        try
        {
            // Refresh the page object.
            SourcePage page = m_state.getSourcePage();
            page = m_pageManager.getSourcePage(page.getId());
            m_state.setSourcePage(page);

            String state = page.getPageState();

            if (state.equals(PageState.IMPORTING)
                    || state.equals(PageState.IMPORT_FAIL))
            {
                m_state.addValidationMessage("Page cannot be updated because "
                        + "it is in the wrong state (" + state + ").");
                m_state.setValidated(false);
            }
            else if (state.equals(PageState.OUT_OF_DATE))
            {
                m_state.addValidationMessage("Page cannot be updated because "
                        + "it was deleted (OUT_OF_DATE).");
                m_state.setValidated(false);
            }
            else if (state.equals(PageState.EXPORT_IN_PROGRESS))
            {
                m_state.addValidationMessage("Page cannot be updated because "
                        + "it is being exported.");
                m_state.setValidated(false);
            }
            else if (state.equals(PageState.UPDATING))
            {
                m_state.addValidationMessage("Page cannot be updated because "
                        + "it is already being updated.");
                m_state.setValidated(false);
            }
            else
            {
                m_state.setValidated(true);
                m_state.clearValidationMessages();
            }
        }
        catch (Throwable ex)
        {
            m_state.setValidated(false);
            m_state.addValidationMessage(ex.getMessage());
        }

        return m_state.getValidated();
    }

    /**
     * Delegates the page update to a background thread.
     */
    private void doUpdate() throws Exception
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    runUpdater();
                }
                finally
                {
                    HibernateUtil.closeSession();
                }
            }
        };

        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("GXMLUPDATER" + String.valueOf(s_counter++));
        t.start();
    }

    /**
     * Golden Rule: do not f*** up 70 target pages.
     * 
     * Therefore: compute all data in advance and commit it through a series of
     * PersistenceCommands in a single transaction. If that update fails,
     * rollback every single change.
     */
    private void runUpdater()
    {
        CATEGORY.info("Starting GXML update for source page "
                + getSourcePage().getExternalPageId() + " (ID "
                + getSourcePage().getId() + ")");

        // Remember the current page states so they can be reset later.
        ArrayList targetPages = null;
        ArrayList allpages = new ArrayList();

        try
        {
            targetPages = getTargetPages();

            // Tell editors that this page and its target pages are
            // being updated.
            synchronizeWithEditor(targetPages, true);

            allpages.add(getNonClonedSourcePage());
            allpages.addAll(targetPages);

            // Mark all pages as UPDATING.
            PagePersistenceAccessor.updateStateOfPages(allpages,
                    PageState.UPDATING);

            // Compute new data and persist it.
            ArrayList deletedTuvIds = computeData();
            commitData();

            // send the email now to notify that the page was updated properly.
            // do even before the states are changed back to their previous
            // state
            // just in case some error occurs beforehand - the user at least
            // should know
            // the states were updated properly
            sendSuccessEmail();

            // After the main data commit(), dispatch deletion of
            // additional data like segment issues. This call may fail
            // if the database or GlobalSight fails but can be repaired
            // manually. That's why we print out the TUV IDs.

            try
            {
                CATEGORY.info("Deleting additional data for TUVs of source page "
                        + getSourcePage().getId() + ": " + deletedTuvIds);

                HashMap map = new HashMap();
                CompanyWrapper.saveCurrentCompanyIdInMap(map, CATEGORY);
                map.put("command", "DeleteTuvIds");
                map.put("deletedTuvIds", deletedTuvIds);
                JmsHelper.sendMessageToQueue(map,
                        JmsHelper.JMS_TRASH_COMPACTION_QUEUE);
            }
            catch (Throwable ex)
            {
                // an error is logged by the comment manager
                //
                // just log the error - don't throw an exception since this
                // isn't
                // necessarily a failure. The page was updated successfully.
            }
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Source page " + getSourcePage().getExternalPageId()
                    + " (ID " + getSourcePage().getId()
                    + ") could not be updated.", ex);

            sendFailureEmail(ex);
        }
        finally
        {
            try
            {
                // Change the page states back to their previous state.
                PagePersistenceAccessor.resetPagesToPreviousState(allpages);
                // update m_state.getHasGsTags() too.

                // Let editors know that page update is complete.
                synchronizeWithEditor(targetPages, false);
            }
            catch (Exception ex)
            {
                CATEGORY.error(
                        "Source page "
                                + getSourcePage().getExternalPageId()
                                + " (ID "
                                + getSourcePage().getId()
                                + ") was updated successfully "
                                + "but the page state could not be reset. "
                                + "GlobalSight should be restarted to correct the page state.",
                        ex);

                // The page data is committed, only the page
                // state is wrong. Need to send special failure email.
                sendFailureEmail(ex);
            }

            CATEGORY.info("Completed GXML update for source page "
                    + getSourcePage().getId());
        }
    }

    /**
     * <p>
     * Computes all data needed to update an old source page to a new version of
     * the source page.
     * </p>
     * 
     * <p>
     * This method creates a list of JDBC statements and stores them in
     * UpdateState. The caller must create a transaction and commit all
     * statements.
     * </p>
     * 
     * @return ArrayList of deleted TUV IDs as Long objects. This is needed to
     *         delete additional data attached to TUVs which is not deleted in
     *         the main transaction to keep the space requirements small
     *         (rollback segments).
     */
    private ArrayList computeData() throws Throwable
    {
        // Load the original page data into memory.
        ArrayList originalTus = getOriginalTusTuvs();

        // Data migrated from pre-5.2 versions of System4/Ambassador
        // may have all PIDs set to 0 - need to compute the correct
        // PIDs from the export template.
        if (containsZeroPid(originalTus))
        {
            CATEGORY.warn("Migrating PID=0 in pre-5.2 page data to valid PIDs.");
            computePidFromTemplate(originalTus);
        }

        if (CATEGORY.isDebugEnabled())
        {
            Long localeId = getSourceLocaleId();
            for (int i = 0, max = originalTus.size(); i < max; i++)
            {
                TuImplVo tu = (TuImplVo) originalTus.get(i);

                for (Iterator it = tu.getTuvs().iterator(); it.hasNext();)
                {
                    TuvImplVo tuv = (TuvImplVo) it.next();

                    System.err
                            .println("Original Tu "
                                    + tu.getId()
                                    + " order "
                                    + tu.getOrder()
                                    + " pid "
                                    + tu.getPid()
                                    + " type "
                                    + (tu.isLocalizable() ? "L" : "T")
                                    + " tuv "
                                    + tuv.getId()
                                    + " order "
                                    + tuv.getOrder()
                                    + " locale "
                                    + (localeId.longValue() == tuv
                                            .getLocaleId() ? "src" : String
                                            .valueOf(tuv.getLocaleId()))
                                    + " seg " + truncate(tuv.getGxml(), 80));
                }

                System.err.println();
            }
        }

        // In old TUs, TUV ordernum is same as TU ordernum (1..num(TU)).
        // In new TUs TUV ordernum restarts at 1 for each translatable.
        // In order to map new to old TUs correctly, renumber the old TUVs.
        renumberTuvsBySegmentOrder(originalTus);

        // Load the new page data into memory and compute three lists:
        // all new TUs, modified and unmodified TUs (changed or added
        // segments by the user, or segments left untouched,
        // respectively). In subsequent code, all three lists refer
        // to the incoming new data regardless of what gets persisted
        // in the end.
        //
        // Unchanged TUs (all segments within an unchanged
        // translatable) carry their original PID, while modified TUs
        // carry a temporarily negative PID.
        //
        loadNewTuTuvData();

        // Find the old TUs that don't exist anymore and delete them
        // (do this before new block ids are assigned).
        ArrayList obsoleteTus = getObsoleteTus();

        ArrayList obsoleteTuvIds = new ArrayList();
        for (int i = 0, max = obsoleteTus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) obsoleteTus.get(i);
            Collection tuvs = tu.getTuvs();
            for (Iterator it = tuvs.iterator(); it.hasNext();)
            {
                Tuv tuv = (Tuv) it.next();
                obsoleteTuvIds.add(tuv.getIdAsLong());
            }
        }

        // Delete TUs and TUVs that are no longer part of the page.
        deleteObsoleteTus(obsoleteTus);

        // Fetch the modified TUVs so the CRC value can be updated.
        ArrayList modifiedTus = m_state.getModifiedTus();

        // Set the exact match key on the source TUVs so target TUVs
        // copied from the sources inherit it.
        setExactMatchKeysForSrcTUVs(modifiedTus);

        // Create target TUVs for all modified TUVs by copying the
        // source TUV to each target locale.
        // leverage the modified source TUVs.
        for (int i = 0, max = modifiedTus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) modifiedTus.get(i);

            copySourceTuvToTargets(tu);
        }

        // Allocate IDs for the modified TUs and TUVs. We do this here
        // so TemplateParts can refer to a valid TU ID and updating
        // the DB becomes easier.
        assignTuTuvIds(modifiedTus);

        // Copy the old TUs from the original page into the working list
        // of unmodified TUs. Because of object identity, if we update a
        // TU in one list, it is updated in all lists.
        ArrayList unmodifiedTus = buildUnmodifiedTuList();

        // Do the same for the new list: copy the old unmodified TUs
        // into a (newly created) list of all new TUs.
        ArrayList allNewTus = buildToSaveList();

        if (CATEGORY.isDebugEnabled())
        {
            System.err.println("After buildToSaveList");

            Long localeId = getSourceLocaleId();
            for (int i = 0, max = allNewTus.size(); i < max; i++)
            {
                TuImplVo tu = (TuImplVo) allNewTus.get(i);
                TuvImplVo tuv = (TuvImplVo) tu.getTuv(localeId);

                System.err.println("Tu " + tu.getId() + " order "
                        + tu.getOrder() + " pid " + tu.getPid() + " type "
                        + (tu.isLocalizable() ? "L" : "T") + " tuv "
                        + tuv.getId() + " order " + tuv.getOrder() + " seg "
                        + truncate(tuv.getGxml(), 80));
            }
        }

        // Reassign the PIDs on TUs - they will all be > 0 from here on.
        // Side effect: original TUs get modified as well.
        fixTuPidAndOrder(allNewTus);

        // TUV.order_num in the database equals the TU ordernum (does
        // not restart at 1 within each translatable).
        renumberTuvsByTuOrder(allNewTus);

        // Update the wordcount. This is not trivial because:
        // - source page wordcounts can be overwritten by the user
        // (override needs to be cleared)
        // - target page wordcount statistics change when segments
        // having matches get removed
        // - wordcounts are replicated on job and workflow
        // - pages and other objects are managed by TopLink which does
        // not participate in this transaction - need to invalidate the
        // TL cache or otherwise merge the JDBC change back into TopLink.
        computeWordCount(allNewTus);

        // Fix the GXML elements in the new GXML tree from which
        // templates get generated: the pid and wordcounts need to be
        // patched back in (this requires TUVs to have order_nums
        // starting at 1 in each translatable).
        patchGxmlElements(m_state.getGxmlRoot(), allNewTus);

        // Localizable TUVs contain TU attributes in their GXML
        // string. Since the TU attributes have changed, the GXML
        // string must be corrected.
        patchLocalizableBlockId(allNewTus);

        if (CATEGORY.isDebugEnabled())
        {
            System.err.println("After patchLocalizableBlockId");

            Long localeId = getSourceLocaleId();
            for (int i = 0, max = allNewTus.size(); i < max; i++)
            {
                TuImplVo tu = (TuImplVo) allNewTus.get(i);
                TuvImplVo tuv = (TuvImplVo) tu.getTuv(localeId);

                System.err.println("Tu " + tu.getId() + " order "
                        + tu.getOrder() + " pid " + tu.getPid() + " type "
                        + (tu.isLocalizable() ? "L" : "T") + " tuv "
                        + tuv.getId() + " order " + tuv.getOrder() + " seg "
                        + truncate(tuv.getGxml(), 80));
            }
        }

        // Get the localizables from the unmodifiedTu list so their
        // GXML string can be updated; their blockID may have changed.
        ArrayList localizableTus = getLocalizableTus(allNewTus);

        // Find the old templates (map indexed by PageTemplate.TYPE_XXX (Long)).
        Map oldTemplates = getPageTemplates();

        // Build the new template parts.
        ArrayList templates = generateTemplates(getSourcePage(),
                m_state.getGxmlRoot(), allNewTus, oldTemplates);

        assignTemplatePartIds(templates);

        // Delete the old template's template parts.
        deleteObsoleteTemplateParts(oldTemplates);

        // Persist all TU, TUV and template part changes in one go.
        createUpdateCommands(templates, unmodifiedTus, modifiedTus,
                localizableTus, allNewTus);

        // Persist all word count related changes (SP/TP/JOB/WF) in one go.
        // Also updates the source page gs tag flag.
        // createWordCountCommands(totalWordCount, m_state.getHasGsTags());

        return obsoleteTuvIds;
    }

    private void commitData() throws Throwable
    {
        Connection conn = getPersistenceService().getConnectionForImport();

        try
        {
            conn.setAutoCommit(false);

            ArrayList commands = m_state.getPersistenceCommands();
            while (commands.size() > 0)
            {
                PersistenceCommand cmd = (PersistenceCommand) commands
                        .remove(0);
                if (cmd instanceof DeleteTuPersistenceCommand)
                {
                    long jobId = m_state.getSourcePage().getJobId();
                    ((DeleteTuPersistenceCommand) cmd).setJobId(jobId);
                }
                cmd.persistObjects(conn);
            }

            if (s_COMMIT_DELAY != 0)
            {
                CATEGORY.info("Delaying commit for page "
                        + getSourcePage().getExternalPageId() + " by "
                        + s_COMMIT_DELAY + " seconds.");

                Thread.sleep(s_COMMIT_DELAY * 1000);

                CATEGORY.info("Commencing commit for page "
                        + getSourcePage().getExternalPageId() + ".");
            }

            conn.commit();

            if (s_AFTER_COMMIT_DELAY != 0)
            {
                CATEGORY.info("Delaying after commit for page "
                        + getSourcePage().getExternalPageId() + " by "
                        + s_AFTER_COMMIT_DELAY + " seconds.");

                Thread.sleep(s_AFTER_COMMIT_DELAY * 1000);

                CATEGORY.info("Commencing after commit for page "
                        + getSourcePage().getExternalPageId() + ".");
            }
        }
        catch (Throwable ex)
        {
            conn.rollback();
            throw ex;
        }
        finally
        {
            try
            {
                getPersistenceService().returnConnection(conn);
            }
            catch (Throwable ignore)
            {
            }
        }
    }

    //
    // Helpers
    //

    /**
     * Loads the TU/TUV data received from the client and populates UpdateState
     * with a breakdown into old and new data.
     */
    private void loadNewTuTuvData() throws Exception
    {
        ArrayList newTus = createTus(m_state.getGxmlRoot(), getSourcePage()
                .getGlobalSightLocale(), getLeverageGroupId());

        m_state.setNewTus(newTus);

        if (CATEGORY.isDebugEnabled())
        {
            ArrayList originalTus = getOriginalTusTuvs();

            CATEGORY.debug("Original TUs: " + originalTus.size()
                    + " incoming TUs: " + newTus.size());
        }

        // Separate modified from unmodified TUs. If structurally
        // added (new, merged, split), the editor has nulled out the
        // "blockId" attribute. If modified in-place, blockId is
        // there but the content is different, need to check that.
        for (int i = 0, max = newTus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) newTus.get(i);

            if (tu.getPid() > 0)
            {
                // Modified in-place after all?
                if (isGxmlModified(tu))
                {
                    m_state.addModifiedTu(tu);
                }
                else
                {
                    m_state.addUnmodifiedTu(tu);
                }
            }
            else
            {
                m_state.addModifiedTu(tu);
            }
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Unmodified TUs: "
                    + m_state.getUnmodifiedTus().size() + " modified TUs: "
                    + m_state.getModifiedTus().size());
        }
    }

    /**
     * Creates TUs for localizable and translatable segments of the GXML tree.
     * Fills in the correct ordernum, segmentId and leverageGroupId.
     */
    private ArrayList createTus(GxmlRootElement p_gxmlRoot,
            GlobalSightLocale p_sourceLocale, long p_lgid) throws Exception
    {
        ArrayList result = new ArrayList();

        // TUs with a blockId are unmodified. For all others we need to
        // assign a new (negative) blockId to reconstruct the paragraphs.
        // All paragraph ids are recomputed before saving the data.
        IntHolder blockId = new IntHolder(-1);

        createTus_1(p_gxmlRoot, p_sourceLocale, blockId, result);

        // Set the ordernum and fill in the leverage group on all TUs.
        for (int i = 0, max = result.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) result.get(i);

            tu.setLeverageGroupId(p_lgid);
            tu.setOrder(i);
        }

        return result;
    }

    private ArrayList createTus_1(GxmlElement p_gxmlElement,
            GlobalSightLocale p_sourceLocale, IntHolder p_blockId,
            ArrayList p_tuList) throws Exception
    {
        if (p_gxmlElement == null)
        {
            return p_tuList;
        }

        List elements = p_gxmlElement.getChildElements();

        for (int i = 0, maxi = elements.size(); i < maxi; i++)
        {
            GxmlElement elem = (GxmlElement) elements.get(i);
            Tu tu;

            switch (elem.getType())
            {
                case GxmlElement.LOCALIZABLE:
                    tu = createLocalizableSegment(elem, p_sourceLocale,
                            p_blockId);
                    p_tuList.add(tu);
                    break;

                case GxmlElement.TRANSLATABLE:
                    ArrayList tus = createTranslatableSegments(elem,
                            p_sourceLocale, p_blockId);
                    p_tuList.addAll(tus);
                    break;

                case GxmlElement.GS:
                    p_tuList = createTus_1(elem, p_sourceLocale, p_blockId,
                            p_tuList);
                    break;
                default:
                    break;
            }
        }

        return p_tuList;
    }

    private Tu createLocalizableSegment(GxmlElement p_elem,
            GlobalSightLocale p_sourceLocale, IntHolder p_blockId)
    {
        Long blockId = new Long(p_blockId.dec());
        Integer wordcount = s_INTONE;

        // If no blockId present, then localizable is new.
        // If present, it may be modified - checked in loadNewTuTuvData().

        String temp;
        temp = p_elem.getAttribute(GxmlNames.LOCALIZABLE_BLOCKID);
        if (temp != null)
        {
            blockId = Long.valueOf(temp);
        }

        temp = p_elem.getAttribute(GxmlNames.LOCALIZABLE_WORDCOUNT);
        if (temp != null)
        {
            wordcount = Integer.valueOf(temp);
        }

        String datatype = p_elem.getAttribute(GxmlNames.LOCALIZABLE_DATATYPE);

        // dataType is optional on LOCALIZABLE
        if (datatype == null)
        {
            GxmlElement diplomat = GxmlElement.getGxmlRootElement(p_elem);

            datatype = diplomat.getAttribute(GxmlNames.GXMLROOT_DATATYPE);
            if (datatype == null)
            {
                throw new RuntimeException(
                        "<localizable> or <diplomat> must carry 'datatype' attribute.");
            }
        }

        String tuType = p_elem.getAttribute(GxmlNames.LOCALIZABLE_TYPE);

        if (tuType == null)
        {
            throw new RuntimeException(
                    "<localizable> must carry 'type' attribute.");
        }

        TuImplVo tu = new TuImplVo();
        tu.setDataType(datatype);
        tu.setTuTypeName(tuType);
        tu.setLocalizableType('L');
        tu.setPid(blockId.longValue());

        TuvImplVo tuv = new TuvImplVo();
        tuv.setGlobalSightLocale(p_sourceLocale);
        tuv.setWordCount(wordcount != null ? wordcount.intValue() : 1);
        tuv.setGxml(p_elem.toGxml());
        tuv.setOrder(1);
        tu.addTuv(tuv);

        if (CATEGORY.isDebugEnabled())
        {
            System.err.println("Incoming localizable: " + p_elem.toGxml());
        }

        return tu;
    }

    private ArrayList createTranslatableSegments(GxmlElement p_elem,
            GlobalSightLocale p_sourceLocale, IntHolder p_blockId)
            throws Exception
    {
        ArrayList tuList = new ArrayList();

        Long blockId = new Long(p_blockId.dec());

        // If no blockId present, then segment is new.

        String temp;

        temp = p_elem.getAttribute(GxmlNames.TRANSLATABLE_BLOCKID);
        if (temp != null)
        {
            blockId = Long.valueOf(temp);
        }

        temp = p_elem.getAttribute(GxmlNames.TRANSLATABLE_WORDCOUNT);

        String tuType = p_elem.getAttribute(GxmlNames.TRANSLATABLE_TYPE);

        // set optional Gxml attribute "type" if not set
        if (tuType == null)
        {
            tuType = "text";
        }

        String datatype = p_elem.getAttribute(GxmlNames.TRANSLATABLE_DATATYPE);

        // dataType is optional on TRANSLATABLE/SEGMENT
        if (datatype == null)
        {
            GxmlElement diplomat = GxmlElement.getGxmlRootElement(p_elem);

            datatype = diplomat.getAttribute(GxmlNames.GXMLROOT_DATATYPE);
            if (datatype == null)
            {
                throw new RuntimeException(
                        "<translatable> or <diplomat> must carry 'datatype' attribute.");
            }
        }

        List segments = p_elem.getChildElements();

        for (int i = 0, max = segments.size(); i < max; i++)
        {
            GxmlElement segment = (GxmlElement) segments.get(i);

            TuImplVo tu = new TuImplVo();
            tu.setDataType(datatype);
            tu.setTuTypeName(tuType);
            tu.setLocalizableType('T');
            tu.setPid(blockId.longValue());

            Integer segWordCount = segment
                    .getAttributeAsInteger(GxmlNames.SEGMENT_WORDCOUNT);

            // Note: Structural edits come in with blockId <= 0 but
            // in-place edits still have their original blockid, so
            // our caller loadNewTuTuvData() will have the final word
            // on which TUVs were modified and which not.

            // find out if extracting each segment individually
            // works OK regarding split/merge in the editor, or if we
            // need to recombine the paragraph with a fake paragraph
            // separator, extract everything, and fish out the
            // original segments.

            String text = EditUtil.decodeXmlEntities(segment
                    .toGxmlExcludeTopTags());

            if (CATEGORY.isDebugEnabled())
            {
                System.err.println("Incoming segment (" + datatype + "): "
                        + text);
            }

            /*
             * if (CATEGORY.isDebugEnabled()) { System.err.println("text (" +
             * datatype + "): " + text + "\n--->" + EditUtil.toJavascript(text)
             * + "<----"); }
             */

            // Paragraph-extract the segment.
            String gxml;

            // Some HP segments contain only an nbsp which the 6.6+
            // extractor will not extract as text (causing an NPE
            // further on). It's not easy to add a special case in
            // the extractor so we'll fake the desired result here.
            if (text.equals("\u00a0") || text.equals("&nbsp;"))
            {
                gxml = fakeGxmlFromText(
                        "<ph type=\"x-nbspace\" x=\"1\" erasable=\"yes\">&amp;nbsp;</ph>",
                        0, i + 1);
            }
            // Also catch if somebody marks up whitespace as
            // translatable as this will cause the same NPE to occur
            // (extractor extracts no SegmentNode). Since spaces are
            // pretty meaningless anyway, map them to a single nbsp.
            else if (Text.isBlankOrNbsp(text))
            {
                gxml = fakeGxmlFromText(
                        "<ph type=\"x-nbspace\" x=\"1\" erasable=\"yes\">&amp;nbsp;</ph>",
                        0, i + 1);
            }
            // Ask the extractor to paragraph-extract the segment. If
            // that returns no usable string, the user made an error
            // and most likely typed a string that is incorrect in the
            // document's context (e.g. "<P>" instead of "&lt;P&gt;").
            // Fail the update and log a useful error message.
            else
            {
                SegmentNode tmp = extractSegment(text, datatype, p_sourceLocale);

                if (tmp != null)
                {
                    gxml = segmentNodeToXml(tmp, i + 1);
                    segWordCount = new Integer(tmp.getWordCount());
                }
                else
                {
                    throw new Exception("Cannot extract text from input `"
                            + text + "' (JS encoded: `"
                            + EditUtil.toJavascript(text) + "') at index "
                            + (i + 1) + ". Check the edited source page.");
                }
            }

            if (CATEGORY.isDebugEnabled())
            {
                System.err.println("extracted as: " + gxml);
            }

            TuvImplVo tuv = new TuvImplVo();
            tuv.setGlobalSightLocale(p_sourceLocale);
            tuv.setWordCount(segWordCount != null ? segWordCount.intValue() : 0);
            tuv.setOrder(i + 1);
            tuv.setGxml(gxml);
            tu.addTuv(tuv);

            tuList.add(tu);
        }

        return tuList;
    }

    private SegmentNode extractSegment(String p_segment, String p_datatype,
            GlobalSightLocale p_sourceLocale) throws Exception
    {
        DiplomatAPI api = getDiplomatApi();

        api.setEncoding("Unicode");
        api.setLocale(p_sourceLocale.getLocale());
        api.setInputFormat(p_datatype);
        api.setSentenceSegmentation(false);
        api.setExtractorSimplify(false);
        api.setCanCallOtherExtractor(false);
        api.setSegmenterPreserveWhitespace(true);

        if (EditUtil.isHtmlDerivedFormat(p_datatype))
        {
            api.setSourceString(p_segment);
        }
        else if (p_datatype.equals(IFormatNames.FORMAT_JAVASCRIPT))
        {
            // Extractor needs a complete input file... use a statement.
            api.setSourceString("var tmp = \"" + p_segment + "\"");
        }
        else
        {
            throw new RuntimeException("Source pages containing dataformat "
                    + p_datatype + " cannot be updated.");
        }

        api.extract();

        Output output = api.getOutput();

        for (Iterator it = output.documentElementIterator(); it.hasNext();)
        {
            Object o = it.next();

            if (o instanceof TranslatableElement)
            {
                TranslatableElement trans = (TranslatableElement) o;

                return (SegmentNode) (trans.getSegments().get(0));
            }
        }

        return null;
    }

    private String segmentNodeToXml(SegmentNode p_node, int p_segmentId)
    {
        StringBuffer result = new StringBuffer();

        result.append("<segment wordcount=\"");
        result.append(p_node.getWordCount());
        result.append("\" segmentId=\"");
        result.append(p_segmentId);
        result.append("\">");
        result.append(p_node.getSegment());
        result.append("</segment>");

        return result.toString();
    }

    /** Fakes a GXML segment without needing to extract the text first. */
    private String fakeGxmlFromText(String p_gxmlFragment, int p_wordcount,
            int p_segmentId)
    {
        StringBuffer result = new StringBuffer();

        result.append("<segment wordcount=\"");
        result.append(p_wordcount);
        result.append("\" segmentId=\"");
        result.append(p_segmentId);
        result.append("\">");
        result.append(p_gxmlFragment);
        result.append("</segment>");

        return result.toString();
    }

    /**
     * Copies the given TU's source TUV into all target locales.
     */
    private void copySourceTuvToTargets(TuImplVo p_tu) throws Exception
    {
        TuvImplVo sourceTuv = (TuvImplVo) p_tu.getTuv(getSourcePage()
                .getGlobalSightLocale().getIdAsLong());

        ArrayList targetLocales = getTargetLocales();

        for (int i = 0, max = targetLocales.size(); i < max; i++)
        {
            GlobalSightLocale locale = (GlobalSightLocale) targetLocales.get(i);

            TuvImplVo targetTuv = new TuvImplVo(sourceTuv);
            targetTuv.setGlobalSightLocale(locale);

            p_tu.addTuv(targetTuv);
        }
    }

    /**
     * Copies the old TUs from the original page into the working list of
     * unmodified TUs.
     */
    private ArrayList buildUnmodifiedTuList()
    {
        Long localeId = getSourceLocaleId();

        ArrayList unmodifiedTus = m_state.getUnmodifiedTus();

        for (int i = 0, max = unmodifiedTus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) unmodifiedTus.get(i);
            TuvImplVo tuv = (TuvImplVo) tu.getTuv(localeId);

            TuImplVo origTu = getOriginalTuByPidOrder(tu.getPid(),
                    tuv.getOrder());

            unmodifiedTus.set(i, origTu);
        }

        return unmodifiedTus;
    }

    /**
     * Builds a representation of all TUs in the to-be-updated page by either
     * using a modified TU from the new GXML, or copying the existing TU from
     * the old page into the "to save" list.
     * 
     * Make sure not to modify the "new TU" list.
     */
    private ArrayList buildToSaveList()
    {
        ArrayList result = new ArrayList();

        Long localeId = getSourceLocaleId();

        ArrayList newTus = m_state.getNewTus();
        ArrayList modifiedTus = m_state.getModifiedTus();

        for (int i = 0, max = newTus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) newTus.get(i);

            if (tu.getPid() > 0 && !modifiedTus.contains(tu))
            {
                TuvImplVo tuv = (TuvImplVo) tu.getTuv(localeId);
                result.add(getOriginalTuByPidOrder(tu.getPid(), tuv.getOrder()));
            }
            else
            {
                result.add(tu);
            }
        }

        return result;
    }

    /**
     * Returns all localizable TUs from the list of TUs passed in.
     */
    private ArrayList getLocalizableTus(ArrayList p_tus)
    {
        ArrayList result = new ArrayList();

        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) p_tus.get(i);

            if (tu.isLocalizable())
            {
                result.add(tu);
            }
        }

        return result;
    }

    /**
     * All GxmlElements in localizable TUVs contain TU attributes in their gxml
     * string. Since the TU/TUV attributes have changed, they must be patched
     * back into the string.
     * 
     * Also patches in the wordcount because newly created localizables don't
     * have it set in their GXML representation.
     */
    private void patchLocalizableBlockId(ArrayList p_tus)
    {
        Long localeId = getSourceLocaleId();

        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) p_tus.get(i);
            TuvImplVo srcTuv = (TuvImplVo) tu.getTuv(localeId);

            if (!tu.isLocalizable())
            {
                continue;
            }

            String blockId = String.valueOf(tu.getPid());
            String wordcount = String.valueOf(srcTuv.getWordCount());

            for (Iterator it = tu.getTuvs().iterator(); it.hasNext();)
            {
                TuvImplVo tuv = (TuvImplVo) it.next();

                GxmlElement elem = tuv.getGxmlElement();
                elem.setAttribute(GxmlNames.LOCALIZABLE_BLOCKID, blockId);
                elem.setAttribute(GxmlNames.LOCALIZABLE_WORDCOUNT, wordcount);
                tuv.setGxmlElement(elem);
            }
        }
    }

    /**
     * The GXML document received from the client contains <translatable>s with
     * no block ID. The argument p_allNewTus contains TUs with correct
     * attributes that need to be patched back into the GXML document before
     * producing templates.
     */
    private void patchGxmlElements(GxmlRootElement p_root, ArrayList p_allNewTus)
    {
        IntHolder index = new IntHolder(0);

        patchGxmlElements_1(p_root, p_allNewTus, index);
    }

    private void patchGxmlElements_1(GxmlElement p_element,
            ArrayList p_allNewTus, IntHolder p_index)
    {
        if (p_element == null)
        {
            return;
        }

        int segmentId = 1;
        List elements = p_element.getChildElements();

        for (int i = 0, maxi = elements.size(); i < maxi; i++)
        {
            GxmlElement elem = (GxmlElement) elements.get(i);

            switch (elem.getType())
            {
                case GxmlElement.LOCALIZABLE:
                {
                    int index = p_index.inc();
                    TuImplVo tu = (TuImplVo) p_allNewTus.get(index);

                    elem.setAttribute(GxmlNames.LOCALIZABLE_BLOCKID,
                            String.valueOf(tu.getPid()));

                    // honor Diplomat.properties (wordcount_localizables)
                    // and count localizables as 0 if so requested.
                    elem.setAttribute(GxmlNames.LOCALIZABLE_WORDCOUNT, "1");

                    if (CATEGORY.isDebugEnabled())
                    {
                        System.err.println("GXML loc " + i + ": "
                                + elem.toLines());
                    }
                }
                    break;

                case GxmlElement.SEGMENT:
                {
                    int index = p_index.inc();
                    TuImplVo tu = (TuImplVo) p_allNewTus.get(index);

                    Long localeId = getSourceLocaleId();
                    int wordcount = tu.getTuv(localeId).getWordCount();

                    elem.setAttribute(GxmlNames.SEGMENT_SEGMENTID,
                            String.valueOf(segmentId++));

                    elem.setAttribute(GxmlNames.SEGMENT_WORDCOUNT,
                            String.valueOf(wordcount));

                    if (CATEGORY.isDebugEnabled())
                    {
                        System.err.println("GXML seg " + i + ": "
                                + elem.toLines());
                    }
                }
                    break;

                case GxmlElement.TRANSLATABLE:
                {
                    int index = p_index.getValue();
                    TuImplVo tu = (TuImplVo) p_allNewTus.get(index);

                    long pid = tu.getPid();
                    int wordcount = getParagraphWordCount(p_allNewTus, p_index);

                    elem.setAttribute(GxmlNames.TRANSLATABLE_BLOCKID,
                            String.valueOf(pid));

                    elem.setAttribute(GxmlNames.TRANSLATABLE_WORDCOUNT,
                            String.valueOf(wordcount));

                    if (CATEGORY.isDebugEnabled())
                    {
                        System.err.println("GXML trans " + i + ": "
                                + elem.toLines());
                    }

                    // Recurse into the segments.
                    patchGxmlElements_1(elem, p_allNewTus, p_index);
                }
                    break;

                case GxmlElement.GS:
                {
                    if (CATEGORY.isDebugEnabled())
                    {
                        System.err.println("GXML GS " + i + ": "
                                + elem.toLines());
                    }

                    // Recurse into the deletable region.
                    patchGxmlElements_1(elem, p_allNewTus, p_index);
                }
                    break;

                case GxmlElement.SKELETON:
                    if (CATEGORY.isDebugEnabled())
                    {
                        System.err.println("GXML skel " + i + ": "
                                + elem.toLines());
                    }
                    break;

                default:
                    if (CATEGORY.isDebugEnabled())
                    {
                        System.err.println("GXML element " + i + ": "
                                + elem.toLines());
                    }
                    break;
            }
        }
    }

    /**
     * Given a translatable node, iterates over all child nodes (segments) and
     * sums up their wordcounts. Called from patchGxmlElements().
     */
    private int getParagraphWordCount(ArrayList p_tus, IntHolder p_index)
    {
        int result = 0;

        Long localeId = getSourceLocaleId();
        int start = p_index.getValue();

        TuImplVo tu = (TuImplVo) p_tus.get(start);

        long pid = tu.getPid();
        while (tu.getPid() == pid)
        {
            result += tu.getTuv(localeId).getWordCount();

            ++start;
            if (start < p_tus.size())
            {
                tu = (TuImplVo) p_tus.get(start);
            }
            else
            {
                break;
            }
        }

        return result;
    }

    /**
     * Given the list of TUs in the new page (including modified and unmodified
     * TUs in the correct order, assign the ordernum and set the PID (paragraph
     * id, blockId) to increasing positive numbers, using the same number for
     * TUs that belong to the same paragraph.
     */
    private void fixTuPidAndOrder(ArrayList p_tus)
    {
        long order = 1;
        long pid = 0;

        long oldpid = Long.MIN_VALUE;

        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) p_tus.get(i);

            // New PIDs start at 1.
            if (oldpid != tu.getPid())
            {
                oldpid = tu.getPid();
                pid++;
            }

            tu.setPid(pid);
            tu.setOrder(order++);
        }
    }

    /**
     * Allocates an ID range for the new TUs and TUVs (persists it in the DB)
     * and updates the TU/TUVs so that dependent objects (like TemplatePart) can
     * pick up the correct values.
     */
    private void assignTuTuvIds(ArrayList p_modifiedTus) throws Exception
    {
        long tuNeeded = p_modifiedTus.size();
        long tuvNeeded = tuNeeded * (1 + getTargetLocales().size());
        long maxTuId = getPersistenceService().getSequenceNumber(tuNeeded,
                ExtractedFileImportPersistenceHandler.TU_SEQUENCENAME);
        long maxTuvId = getPersistenceService().getSequenceNumber(tuvNeeded,
                ExtractedFileImportPersistenceHandler.TUV_SEQUENCENAME);

        long tuId = maxTuId - tuNeeded + 1;
        long tuvId = maxTuvId - tuvNeeded + 1;

        for (int i = 0, max = p_modifiedTus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) p_modifiedTus.get(i);

            tu.setId(tuId++);

            for (Iterator it = tu.getTuvs().iterator(); it.hasNext();)
            {
                TuvImplVo tuv = (TuvImplVo) it.next();

                tuv.setId(tuvId++);
            }
        }
    }

    /**
     * Allocates an ID range for the new TemplateParts (persists it in the DB)
     * and updates all TPs.
     */
    private void assignTemplatePartIds(ArrayList p_templates) throws Exception
    {
        long tpNeeded = 0;

        for (int i = 0, max = p_templates.size(); i < max; i++)
        {
            PageTemplate template = (PageTemplate) p_templates.get(i);

            tpNeeded += template.getTemplateParts().size();
        }

        long maxTp = getPersistenceService()
                .getSequenceNumber(
                        tpNeeded,
                        ExtractedFileImportPersistenceHandler.TEMPLATEPART_SEQUENCENAME);

        long tpId = maxTp - tpNeeded + 1;

        for (int i = 0, maxi = p_templates.size(); i < maxi; i++)
        {
            PageTemplate template = (PageTemplate) p_templates.get(i);

            List parts = template.getTemplateParts();

            for (int j = 0, maxj = parts.size(); j < maxj; j++)
            {
                TemplatePart part = (TemplatePart) parts.get(j);

                part.setId(tpId++);
            }
        }
    }

    /**
     * Finds the old TUs that must be deleted from the database because they're
     * no longer part of the new page. Ignoring the TU or TUV's ordernum is OK
     * since all TUVs in a modified TU (which does not carry a PID) must be
     * deleted.
     */
    private ArrayList getObsoleteTus() throws Exception
    {
        ArrayList result = new ArrayList();
        ArrayList originalTus = getOriginalTusTuvs();
        Long localeId = getSourceLocaleId();

        for (int i = 0, max = originalTus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) originalTus.get(i);
            TuvImplVo tuv = (TuvImplVo) tu.getTuv(localeId);

            if (getNewUnmodifiedTuByPidOrder(tu.getPid(), tuv.getOrder()) == null)
            {
                result.add(tu);

                if (CATEGORY.isDebugEnabled())
                {
                    TuvImplVo sourceTuv = (TuvImplVo) tu
                            .getTuv(getSourceLocaleId());

                    System.err.println("Deleting TU " + tu.getId() + " pid="
                            + tu.getPid() + ": " + sourceTuv.getGxml());

                }
            }
        }

        return result;
    }

    /**
     * Sets the TUV order to start at 1 within each translatable section.
     */
    private void renumberTuvsBySegmentOrder(ArrayList p_tus)
    {
        long oldpid = Long.MIN_VALUE;
        long order = 1;

        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) p_tus.get(i);

            if (oldpid != tu.getPid())
            {
                oldpid = tu.getPid();
                order = 1;
            }

            for (Iterator it = tu.getTuvs().iterator(); it.hasNext();)
            {
                TuvImplVo tuv = (TuvImplVo) it.next();

                tuv.setOrder(order);
            }

            ++order;
        }
    }

    /**
     * Sets the TUV order to increase incrementally (same as TU order), starting
     * with 1.
     */
    private void renumberTuvsByTuOrder(ArrayList p_tus)
    {
        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) p_tus.get(i);

            for (Iterator it = tu.getTuvs().iterator(); it.hasNext();)
            {
                TuvImplVo tuv = (TuvImplVo) it.next();

                tuv.setOrder(i + 1);
            }
        }
    }

    /**
     * Returns a TU by pid and TUV order if it is in the new page and hasn't
     * changed.
     */
    private TuImplVo getNewUnmodifiedTuByPidOrder(long p_pid, long p_tuvOrder)
    {
        ArrayList unmodifiedTus = m_state.getUnmodifiedTus();
        Long localeId = getSourceLocaleId();

        for (int i = 0, max = unmodifiedTus.size(); i < max; i++)
        {
            TuImplVo newTu = (TuImplVo) unmodifiedTus.get(i);
            TuvImplVo newTuv = (TuvImplVo) newTu.getTuv(localeId);

            if (newTu.getPid() == p_pid && newTuv.getOrder() == p_tuvOrder)
            {
                return newTu;
            }
        }

        return null;
    }

    private TuImplVo getOriginalTuByPidOrder(long p_pid, long p_tuvOrder)
    {
        Long localeId = getSourceLocaleId();

        ArrayList originalTus = m_state.getOriginalTus();

        for (int i = 0, max = originalTus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) originalTus.get(i);
            TuvImplVo tuv = (TuvImplVo) tu.getTuv(localeId);

            if (tu.getPid() == p_pid && tuv.getOrder() == p_tuvOrder)
            {
                return tu;
            }
        }

        throw new RuntimeException(
                "TU with PID:ORDER "
                        + p_pid
                        + ":"
                        + p_tuvOrder
                        + " not found. UI didn't mark all segments in paragraph as modified!");

        // return null;
    }

    /**
     * Checks whether a new TU with blockId (meaning un-modified) is really
     * unmodified by comparing the TUV's GXML content against the original TUV
     * content.
     */
    private boolean isGxmlModified(TuImplVo p_tu)
    {
        Long localeId = getSourceLocaleId();

        TuvImplVo newTuv = (TuvImplVo) p_tu.getTuv(localeId);
        TuImplVo oldTu = getOriginalTuByPidOrder(p_tu.getPid(),
                newTuv.getOrder());
        TuvImplVo oldTuv = (TuvImplVo) oldTu.getTuv(localeId);

        /*
         * String gxmlOld = oldTuv.getGxmlExcludeTopTags(); String gxmlNew =
         * newTuv.getGxmlExcludeTopTags();
         * 
         * return !gxmlOld.equals(gxmlNew);
         */

        GxmlElement elemOld = oldTuv.getGxmlElement();
        GxmlElement elemNew = newTuv.getGxmlElement();

        boolean result = elemOld.equals(elemNew);

        if (CATEGORY.isDebugEnabled())
        {
            System.err.println();
            System.err.println("TU PID " + p_tu.getPid() + " TUV order "
                    + newTuv.getOrder());
            System.err.println("Old = " + elemOld.toLines());
            System.err.println("New = " + elemNew.toLines());
            System.err.println((result ? "equal" : "NOT equal"));
        }

        return !result;
    }

    /**
     * Computes the wordcount of all source TUVs of the TUs passed in by summing
     * up the wordcounts of all source TUVs of each TU.
     */
    private int computeWordCount(ArrayList p_tus)
    {
        int result = 0;

        Long localeId = getSourceLocaleId();
        Vector excludedTypes = getExcludedItems();

        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) p_tus.get(i);
            TuvImplVo tuv = (TuvImplVo) tu.getTuv(localeId);

            int wordCount = tuv.getWordCount();

            // read subs, exclude and count
            List subs = tuv.getSubflowsAsGxmlElements();
            for (int j = 0, jmax = subs.size(); j < jmax; j++)
            {
                GxmlElement sub = (GxmlElement) subs.get(j);

                String type = sub.getAttribute(GxmlNames.SUB_TYPE);
                if (type == null)
                {
                    type = "text";
                }

                if (!excludedTypes.contains(type))
                {
                    String wc = sub.getAttribute(GxmlNames.SUB_WORDCOUNT);
                    if (wc != null)
                    {
                        int subCount = Integer.parseInt(wc);
                        wordCount += subCount;
                    }
                }
            }

            result += wordCount;
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("New GXML page contains " + result + " words.");
        }

        return result;
    }

    /**
     * Returns all TUVs of the TUs passed in in no particular order.
     */
    private ArrayList getAllTuvs(ArrayList p_tus)
    {
        ArrayList result = new ArrayList();

        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) p_tus.get(i);

            result.addAll(tu.getTuvs());
        }

        return result;
    }

    private Long getSourceLocaleId()
    {
        return getSourcePage().getGlobalSightLocale().getIdAsLong();
    }

    private long getLeverageGroupId()
    {
        SourcePage page = getSourcePage();
        List ids = ((ExtractedSourceFile) page.getPrimaryFile())
                .getLeverageGroupIds();
        return ((Long) ids.get(0)).longValue();
    }

    private Map getPageTemplates()
    {
        SourcePage page = getSourcePage();
        return ((ExtractedSourceFile) page.getPrimaryFile()).getTemplateMap();
    }

    private long getTemplateId(Map p_templates, int p_type)
    {
        return ((PageTemplate) p_templates.get(new Long(p_type))).getId();
    }

    /**
     * Creates the templates for the source page from the updated gxml. Patches
     * in the IDs of the old templates into the newly created ones so the
     * database update is easy.
     */
    private ArrayList generateTemplates(SourcePage p_page,
            GxmlRootElement p_gxml, ArrayList p_tus, Map p_oldTemplates)
            throws Exception
    {
        ArrayList result = new ArrayList();

        TemplateGenerator tg = new TemplateGenerator();
        PageTemplate template;

        template = tg.generateDetail(p_gxml, p_tus);
        template.setId(getTemplateId(p_oldTemplates, PageTemplate.TYPE_DETAIL));
        result.add(template);

        template = tg.generateStandard(p_gxml, p_tus);
        template.setId(getTemplateId(p_oldTemplates, PageTemplate.TYPE_STANDARD));
        result.add(template);

        template = tg.generateExport(p_gxml, p_tus);
        template.setId(getTemplateId(p_oldTemplates, PageTemplate.TYPE_EXPORT));
        result.add(template);

        if (EditUtil.hasPreviewMode(m_state.getDataFormat()))
        {
            template = tg.generatePreview(p_gxml, p_tus);
            template.setId(getTemplateId(p_oldTemplates,
                    PageTemplate.TYPE_PREVIEW));
            result.add(template);
        }

        return result;
    }

    private void separateClobTemplateParts(ArrayList p_templates,
            ArrayList p_nonClob, ArrayList p_clob)
    {
        for (int i = 0, maxi = p_templates.size(); i < maxi; i++)
        {
            PageTemplate template = (PageTemplate) p_templates.get(i);

            List parts = template.getTemplateParts();
            for (int j = 0, maxj = parts.size(); j < maxj; j++)
            {
                TemplatePart part = (TemplatePart) parts.get(j);

                if (isClob(part.getSkeleton()))
                {
                    p_clob.add(part);
                }
                else
                {
                    p_nonClob.add(part);
                }
            }
        }
    }

    private void separateClobTuvs(ArrayList p_tus, ArrayList p_nonClob,
            ArrayList p_clob)
    {
        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) p_tus.get(i);

            for (Iterator it = tu.getTuvs().iterator(); it.hasNext();)
            {
                TuvImplVo tuv = (TuvImplVo) it.next();

                if (isClob(tuv.getGxml()))
                {
                    p_clob.add(tuv);
                }
                else
                {
                    p_nonClob.add(tuv);
                }
            }
        }
    }

    private void setExactMatchKeysForSrcTUVs(ArrayList p_tus)
    {
        Long localeId = getSourceLocaleId();

        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) p_tus.get(i);

            TuvImplVo tuv = (TuvImplVo) tu.getTuv(localeId);

            tuv.setExactMatchKey(GlobalSightCrc.calculate(tuv
                    .getExactMatchFormat()));
        }
    }

    //
    // Email Helpers
    //

    // See PageEventObserverLocal.notifyProjectManager().
    private void sendSuccessEmail()
    {
        try
        {
            Job job = getJob();
            SourcePage page = getSourcePage();

            String[] args = new String[5];
            args[0] = String.valueOf(job.getId());
            args[1] = job.getJobName();
            args[2] = String.valueOf(page.getId());
            args[3] = page.getExternalPageId();
            args[4] = getCapLoginUrl();

            sendEmail(MailerConstants.GXML_EDIT_SUCCESS, EDIT_SUCCESS_MESSAGE,
                    args);
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Success email could not be sent to PM.");
        }
    }

    private void sendFailureEmail(Throwable p_exception)
    {
        try
        {
            Job job = getJob();
            SourcePage page = getSourcePage();

            String[] args = new String[6];
            args[0] = String.valueOf(job.getId());
            args[1] = job.getJobName();
            args[2] = String.valueOf(page.getId());
            args[3] = page.getExternalPageId();
            args[4] = p_exception.getMessage();
            args[5] = getCapLoginUrl();

            sendEmail(MailerConstants.GXML_EDIT_FAILURE, EDIT_FAILURE_MESSAGE,
                    args);
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Failure email could not be sent to PM.");
        }
    }

    private void sendEmail(String p_subject, String p_message, String[] p_args)
            throws Exception
    {
        L10nProfile l10nProfile = getL10nProfile();
        Project project = ServerProxy.getProjectHandler().getProjectById(
                l10nProfile.getProjectId());
        String companyIdStr = String.valueOf(project.getCompanyId());
        String pmName = project.getProjectManagerId();
        User pm = ServerProxy.getUserManager().getUser(pmName);

        ServerProxy.getMailer().sendMailFromAdmin(pm, p_args, p_subject,
                p_message, companyIdStr);
    }

    private String getCapLoginUrl() throws Exception
    {
        SystemConfiguration config = SystemConfiguration.getInstance();
        return config.getStringParameter(SystemConfiguration.CAP_LOGIN_URL);
    }

    private String truncate(String p_string, int p_max)
    {
        return p_string.substring(0, Math.min(p_string.length(), p_max));
    }

    private void synchronizeWithEditor(ArrayList p_targetPages, boolean p_start)
    {
        try
        {
            for (int i = 0, max = p_targetPages.size(); i < max; i++)
            {
                TargetPage page = (TargetPage) p_targetPages.get(i);

                if (p_start)
                {
                    m_syncManager.gxmlUpdateStarted(page.getIdAsLong());
                }
                else
                {
                    m_syncManager.gxmlUpdateFinished(page.getIdAsLong());
                }
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error("unexpected exception", ex);
        }
    }

    //
    // Dynamic Migration for pre-5.2 PIDs
    //

    /**
     * Determines if the given list of TUs has been migrated from pre-5.2 data.
     * The 5.2 migration script did set all paragraph to 0 instead of the real
     * paragraph number. Post-5.2 imports have PIDs set to positive integers.
     */
    private boolean containsZeroPid(ArrayList p_tus)
    {
        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo) p_tus.get(i);

            if (tu.getPid() == 0)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Loads the current export template of the source page and recomputes the
     * paragraph ids of the page's TUs. Segments belonging to the same paragraph
     * are separated in the template by an empty template part.
     */
    private void computePidFromTemplate(ArrayList p_tus) throws Exception
    {
        ArrayList parts = getExportTemplateParts();

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Number of template parts = " + parts.size()
                    + ", number of tus = " + p_tus.size()
                    + " (#parts should be #tus + 1)");
        }

        if (parts.size() < p_tus.size())
        {
            throw new Exception(
                    "Page "
                            + m_state.getSourcePage().getId()
                            + " contains more template parts than TUs! Cannot recompute PIDs.");
        }

        long pid = 1;
        TuImplVo prevTu = null;

        for (int i = 0, max = Math.min(parts.size(), p_tus.size()); i < max; i++)
        {
            TemplatePart part = (TemplatePart) parts.get(i);
            TuImplVo tu = (TuImplVo) p_tus.get(i);

            if (part.getTuId() != tu.getId())
            {
                throw new Exception("Template part " + part.getId()
                        + " points to TU " + part.getTuId()
                        + " but the current TU is " + tu.getId() + ".");
            }

            // Remember previous TU, for first tu use the same one.
            if (prevTu == null)
            {
                prevTu = tu;
            }

            // Empty skeleton separates two segments in the same paragraph.
            // Because of a linebreak between the GXML elements, the
            // skeleton was normalized by the XML parser to a single
            // line break (\n). I hope that is true all the time since
            // my check is very specific.
            String skeleton = part.getSkeleton();
            if ((skeleton.length() == 0 || (skeleton.length() == 1 && skeleton
                    .equals("\n")))
                    && !tu.isLocalizable()
                    && !prevTu.isLocalizable())
            {
                // subsequent seg in same para, set to pid of first seg
                tu.setPid(pid - 1);
            }
            else
            {
                tu.setPid(pid++);
            }

            prevTu = tu;
        }
    }

    //
    // Persistence Helpers
    //

    private PersistenceService getPersistenceService() throws Exception
    {
        if (m_persistenceService == null)
        {
            m_persistenceService = PersistenceService.getInstance();
        }

        return m_persistenceService;
    }

    private DiplomatAPI getDiplomatApi()
    {
        if (m_diplomat == null)
        {
            m_diplomat = new DiplomatAPI();
        }

        m_diplomat.reset();

        return m_diplomat;
    }

    private boolean isClob(String p_arg)
    {
        if (EditUtil.getUTF8Len(p_arg) > 4000)
        {
            return true;
        }

        return false;
    }

    private SourcePage getSourcePage()
    {
        return m_state.getSourcePage();
    }

    private SourcePage getNonClonedSourcePage()
    {
        try
        {
            // retrieve the source page not as a clone "false"
            // Vector queryArgs = new Vector(1);
            // queryArgs.add(m_state.getSourcePage().getIdAsLong());
            // Collection pages = getPersistenceService().executeNamedQuery(
            // PageQueryNames.SOURCE_PAGE_BY_ID, queryArgs, false);
            // return (SourcePage) pages.iterator().next();
            return (SourcePage) HibernateUtil.get(SourcePage.class, m_state
                    .getSourcePage().getIdAsLong());
        }
        catch (Exception e)
        {
            return getSourcePage();
        }
    }

    private ArrayList getTargetPages() throws Exception
    {
        ArrayList result = m_state.getTargetPages();

        if (result == null)
        {
            // get the target pages - but not as clones "false"
            // Vector queryArgs = new Vector(1);
            // queryArgs.add(m_state.getSourcePage().getIdAsLong());
            // result = (ArrayList) getPersistenceService().executeNamedQuery(
            // PageQueryNames.TARGET_PAGES_BY_SOURCE_PAGE_ID, queryArgs,
            // false);
            String hql = "from TargetPage t where t.sourcePage.id=:source_page_id";
            HashMap map = new HashMap();
            map.put("source_page_id", m_state.getSourcePage().getIdAsLong());
            result = (ArrayList) HibernateUtil.search(hql, map);
        }
        return result;
    }

    private ArrayList getTargetLocales() throws Exception
    {
        ArrayList result = m_state.getTargetLocales();

        if (result == null)
        {
            result = new ArrayList();

            ArrayList targetPages = getTargetPages();

            for (int i = 0, max = targetPages.size(); i < max; i++)
            {
                TargetPage page = (TargetPage) targetPages.get(i);

                result.add(page.getGlobalSightLocale());
            }

            m_state.setTargetLocales(result);
        }

        return result;
    }

    private Request getRequest()
    {
        return getSourcePage().getRequest();
    }

    private L10nProfile getL10nProfile()
    {
        return getRequest().getL10nProfile();
    }

    private Vector getExcludedItems()
    {
        return getL10nProfile().getTranslationMemoryProfile()
                .getJobExcludeTuTypes();
    }

    private Job getJob()
    {
        return getRequest().getJob();
    }

    private ArrayList getOriginalTusTuvs() throws Exception
    {
        ArrayList result = m_state.getOriginalTus();

        if (result == null)
        {
            Connection conn = getPersistenceService().getConnectionForImport();
            conn.setAutoCommit(false);

            try
            {
                TuvJdbcQuery query = new TuvJdbcQuery(conn);

                m_state.setOriginalTus(new ArrayList(query
                        .getTusBySourcePageIdAndLocales(getSourcePage(),
                                getTargetLocales())));

                conn.commit();
            }
            catch (Throwable ex)
            {
                conn.rollback();
                throw new Exception("Cannot load TU/TUV data", ex);
            }
            finally
            {
                try
                {
                    getPersistenceService().returnConnection(conn);
                }
                catch (Throwable ignore)
                {
                    CATEGORY.error(ignore.getMessage(), ignore);
                }
            }

            result = m_state.getOriginalTus();
        }

        return result;
    }

    private ArrayList getExportTemplateParts() throws Exception
    {
        return new ArrayList(m_pageManager.getTemplatePartsForSourcePage(
                m_state.getSourcePage().getIdAsLong(),
                PageTemplate.getTypeAsString(PageTemplate.TYPE_EXPORT)));
    }

    //
    // Persistence Command Helpers
    //

    /**
     * Creates persistence command to delete TUs that are no longer part of the
     * page, and their associated data. (Currently orphans them by setting the
     * leverage_group to 0.)
     */
    private void deleteObsoleteTus(ArrayList p_tus)
    {
        PersistenceCommand cmd = new DeleteTuPersistenceCommand(p_tus);
        m_state.addPersistenceCommand(cmd);
    }

    private void deleteObsoleteTemplateParts(Map p_templates)
    {
        ArrayList templates = new ArrayList(p_templates.values());

        PersistenceCommand cmd = new DeleteTemplatePartPersistenceCommand(
                templates);
        m_state.addPersistenceCommand(cmd);
    }

    /**
     * Create persistence commands to persist all changes in one go.
     */
    private void createUpdateCommands(ArrayList p_templates,
            ArrayList p_unmodifiedTus, ArrayList p_modifiedTus,
            ArrayList p_localizableTus, ArrayList p_allTus)
    {
        // Template parts get inserted.
        ArrayList nonClobTemplateParts = new ArrayList();
        ArrayList clobTemplateParts = new ArrayList();
        separateClobTemplateParts(p_templates, nonClobTemplateParts,
                clobTemplateParts);

        // Modified (new) TUs get inserted, nonClobTUVs get inserted.
        ArrayList nonClobTuvs = new ArrayList();
        ArrayList clobTuvs = new ArrayList();
        separateClobTuvs(p_modifiedTus, nonClobTuvs, clobTuvs);

        // nonClobLocTuvs have their segment_string updated (modified blockid)
        ArrayList nonClobLocTuvs = new ArrayList();
        ArrayList clobLocTuvs = new ArrayList();
        separateClobTuvs(p_localizableTus, nonClobLocTuvs, clobLocTuvs);

        // ALL TUs have their order, pid updated.
        // ALL TUVs have their order updated.
        ArrayList allTuvs = getAllTuvs(p_allTus);

        PersistenceCommand cmd = new UpdateGxmlPersistenceCommand(
                nonClobTemplateParts, clobTemplateParts, p_modifiedTus,
                nonClobTuvs, clobTuvs, nonClobLocTuvs, clobLocTuvs, p_allTus,
                allTuvs);

        long jobId = m_state.getSourcePage().getJobId();
        ((UpdateGxmlPersistenceCommand) cmd).setJobId(jobId);
        m_state.addPersistenceCommand(cmd);
    }
}
