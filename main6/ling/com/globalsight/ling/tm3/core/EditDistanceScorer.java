package com.globalsight.ling.tm3.core;

import java.util.List;

/**
 * Reference implementation of a modified Damerau-Levenshtein edit distance 
 * calculation for use as a fuzzy match scorer. 
 * <p>
 * In order to make use of this scorer, {@link TM3Data} implementations must implement
 * the {@link TM3Scorable} subinterface to provide token data for scoring.  (The 
 * token stream returned by {@link TM3Data#tokenize()} is not necessarily
 * the same.)
 * <p>
 * <b>About the algorithm</b>
 * <p>
 * The scorer calculates a 
 * <a href="http://en.wikipedia.org/wiki/Damerau-Levenshtein_distance">Damerau-Levenshtein Edit Distance</a>
 * across source and target lists of scorable objects.  "Scorable objects" may be words or tokens, but may
 * also be other objects such as placeholder or structural information.
 * <p>   
 * However, instead of using a fixed cost for substitution, as in a canonical edit distance, 
 * the scorer will apply a variable cost from 0..100 based on the similarity of the two
 * objects in question.  This is done to recapture some of the granularity that is lost
 * when implementing across words/objects, rather than across characters.
 */
public class EditDistanceScorer<T extends TM3Scorable<V>, 
           V extends TM3FuzzyComparable<V>> implements TM3FuzzyMatchScorer<T> {

    // Cost of a single change between source and target
    protected static final float COST = 1f;
    
    /**
     * Compute a fuzzy match score based on edit distance.  This implementation
     * ignores the <tt>locale</tt> parameter.
     * 
     * @param matchKey original segment data
     * @param candidate segment data to compare
     * @param locale locale of both pieces of segment data.
     */
    @Override
    public float score(T matchKey, T candidate, TM3Locale locale) {
        List<V> sources = matchKey.getScoringObjects();
        List<V> targets = candidate.getScoringObjects();
              
        // Compute substitution penalties
        float[][] penalties = new float[sources.size()][targets.size()];
        for (int x = 0; x < sources.size(); x++) {
            for (int y = 0; y < targets.size(); y++) {
                //penalties[x][y] = sources.get(x).getPenalty(targets.get(y));
                float f = sources.get(x).fuzzyCompare(targets.get(y));
                if (f < 0 || f > 1) {
                    throw new IllegalArgumentException(
                        "fuzzyCompare() returned invalid value " + 
                        f + " for source " + x + " target " + y);
                }
                penalties[x][y] = 1.0f - f;
            }
        }
        
        // Now compute the Damerau-Levenshtein distance on the object 
        // lists, but applying the pre-computed penalty on substitution
        // instead of a constant cost (100).

        float[] prev = null, prevprev = null, current;
        // Initialize the first column
        current = new float[targets.size() + 1];
        for (int i = 0; i < current.length; i++) {
            current[i] = i * COST;
        }
        for (int x = 0; x < sources.size(); x++) {
            // Set up columns
            prevprev = prev;
            prev = current;
            current = new float[targets.size() + 1];
            current[0] = (x + 1) * COST;
            // NOTE: The indexing here is gnarly, because the 'x' value
            // is zero-indexed, while the 'y' value is essentially 1-indexed
            // because the y=0 is seeded with fixed values.  As a result,
            // sources.get(x) and targets.get(y - 1) are comparing objects
            // in equivalent list positions.  
            //
            // This makes the transposition logic, in particular, tough
            // to follow
            for (int y = 1; y < targets.size() + 1; y++) {
                float delcost = prev[y] + COST;
                float addcost = current[y - 1] + COST;
                float substcost = prev[y - 1] + penalties[x][y - 1];
                current[y] = (delcost < addcost) ?
                                (delcost < substcost) ? delcost : substcost
                              : (addcost < substcost) ? addcost : substcost;
                // Handle transposition
                if (x > 0 && y > 1 &&
                        penalties[x][y - 2] == 0 &&
                        penalties[x - 1][y - 1] == 0 &&
                        penalties[x][y - 1] != 0) {
                    current[y] = Math.min(current[y], prevprev[y - 2] + COST);
                }
            }
        }
        
        // |result| is the total penalty, out of a possible maximum of 
        // 100 * whichever sentence has more words.
        float result = current[current.length - 1];
        float max = Math.max(sources.size(), targets.size());
        return (max - result) / max;
    }

}
