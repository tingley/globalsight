package com.globalsight.ling.tm3.core;

import java.util.List;

/**
 * An object that allows complex save requests to be sent to the TM
 * in a somewhat orderly way.
 * <p>
 * A sample invocation might look like this:
 * <pre>
 *   TM3Saver<T> saver = tm.createSaver();
 *   for (<i>some condition</i>) {
 *      saver.tu(srcContent, srcLocale, event)
 *           .attr(attr1, value1)
 *           .attr(attr2, value2)
 *           .tuv(frenchContent, frenchLocale, event)
 *           .tuv(germanContent, germanLocale, event);
 *   }
 *   saver.save(TM3SaveMode.MERGE);
 * </pre>
 */
class BaseSaver<T extends TM3Data> extends TM3Saver<T> {

    private BaseTm<T> tm;
    
    BaseSaver(BaseTm<T> tm) {
        super();
        this.tm = tm;
    }

    /**
     * Update the TM based on the contents of this saver.  This will
     * flush all TU and TUV to the database.
     * @param mode Save mode
     * @return
     * @throws TM3Exception
     */
    public List<TM3Tu<T>> save(TM3SaveMode mode, boolean indexTarget)
            throws TM3Exception
    {
        return tm.save(this, mode, indexTarget);
    }
}
