package com.globalsight.ling.tm3.core;

/**
 * Mode flag to be passed to a TM3Tm.save() call.  The values specify
 * what action should be taken when storing a source/target TUV pair
 * that conflicts with a pair already in the TM.
 * <p>
 * Note that the save mode is only considered under a specific set of circumstances:
 * <ul>
 * <li>The source identity must match.  This means that the source TUV must be
 * an exact match of an existing TU, and all TU attributes must match as well.
 * <li>The target locale must match.  So for an existing source, new target TUVs
 * are always added, regardless of the save mode.
 * </ul>
 */
public enum TM3SaveMode {

    /**
     * In the case of collision, overwrite any existing TUV in the
     * target locale with the target that is being saved.
     */
    OVERWRITE,

    /**
     * In the case of collision, add a new target TUV to the TU, unless
     * that target already has a TUV with identical text.  (In other words,
     * MERGE operations are still idempotent.)
     */
    MERGE,
    
    /**
     * In the case of collision, discard the TUV data that is being saved, 
     * and retain the data that is already in the TM.
     */
    DISCARD;
}
