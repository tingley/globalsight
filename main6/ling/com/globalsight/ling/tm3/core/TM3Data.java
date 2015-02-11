package com.globalsight.ling.tm3.core;

/**
 * Generic "segment data".  Different implementations may store things 
 * differently in the database.  What is ultimately important is that 
 * the representation be convertible to a number of different forms
 * used by TM3. 
 * <p>
 * The underlying structure may be as simple as a String, or an arbitrarily 
 * complex structure, as long as it is capable of reducing its data to three 
 * forms:
 * <ul>
 * <li>A <b>serialized form</b> for database storage.  This much be a String.
 * The client implementation should also provide a {@link TM3DataFactory} 
 * implementation capable of deserializing this form into an object instance. 
 * <li>A <b>fingerprint</b>, which is an 8-byte hash (or other value) that 
 * is reasonably unique.  Fingerprints are used for exact match queries.
 * Fingerprint matches will be further verified by comparing the serialized
 * form of the exact match candidate, so the fingerprint need not guarantee
 * complete uniqueness.
 * <li>A <b>token stream</b> for fuzzy match indexing and queries.  Ideally,
 * this should be a stream of word tokens, although any well-defined sequence
 * of tokens should work.  Normalization may be applied to the token in order
 * to improve fuzzy candidate production.
 * </ul>
 * <p>
 * <b>Warning:</b> Implementations <b>must</b> override <tt>equals()</tt>
 * or else run the risk of duplicate target TUVs being persisted in the 
 * database.
 * </p>
 */
public interface TM3Data {
    
    /**
     * Return the data as a stream of tokens.  The fuzzy matching system
     * will bracket this stream with boundary tokens indicating the start
     * and end of the segment.
     * 
     * @return {@link Iterable} capable of producing the token stream
     */
    public Iterable<Long> tokenize();

    /**
     * Compute and return the data's fingerprint. 
     * @return 
     */
    public long getFingerprint();
    
    /**
     * Serialize this TUV data for storage.  This form should be deserializable
     * by a corresponding {@link TM3DataFactory} implementation.
     * @return
     */
    public String getSerializedForm();

    /**
     * Test object equality.
     * <p>"Equality" in this case may be as simple as a string compare,
     * or there may be more complicated logical comparisons going on.
     * <b>Warning:</b> Implementations <b>must</b> override <tt>equals()</tt>
     * or else run the risk of duplicate target TUVs being persisted in the 
     * database.  
     * @param o object to test for equality
     * @return true if the objects are equal; false otherwise
     */
    @Override
    public boolean equals(Object o);
}
