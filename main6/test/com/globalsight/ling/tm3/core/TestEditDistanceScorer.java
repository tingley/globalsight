package com.globalsight.ling.tm3.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestEditDistanceScorer {
    
    /// TEST CODE
    
    public static void main(String[] args) {
     
        EditDistanceScorer<Data, DataChunk> scorer = new EditDistanceScorer<Data, DataChunk>();
        scorer.score(new Data("We are the shadow council of the localization industry"), new Data("We are the shadow council of the translation industry"), null);
        scorer.score(new Data("I am Henry the Eighth I am"), new Data("Henry the Eighth I am I am"), null);
        scorer.score(new Data("c a a"), new Data("b a a"), null);
        scorer.score(new Data("a b c"), new Data("a c b"), null);
    }
    
    EditDistanceScorer<Data, DataChunk> scorer = new EditDistanceScorer<Data, DataChunk>();
    
    @Test
    public void testEquality() {
        assertEquals(1.0, scorer.score(new Data("a b c d"), new Data("a b c d"), null), 0.0001);
    }
    
    @Test
    public void testSingleChange() {
        assertEquals(.75, scorer.score(new Data("a b c"), new Data("a b c e"), null), 0.0001);
        assertEquals(.75, scorer.score(new Data("e a b c"), new Data("a b c"), null), 0.0001);
        assertEquals(.75, scorer.score(new Data("a b c d"), new Data("a b c e"), null), 0.0001);
        assertEquals(.75, scorer.score(new Data("a b d c"), new Data("a b c d"), null), 0.0001);
    }
    
    @Test
    public void testTwoChanges() {
        assertEquals(.60, scorer.score(new Data("a b c"), new Data("a b c e f"), null), 0.0001);
        assertEquals(.60, scorer.score(new Data("e f a b c"), new Data("a b c"), null), 0.0001);
        assertEquals(.60, scorer.score(new Data("a b c d e"), new Data("b c d e f"), null), 0.0001);
        assertEquals(.60, scorer.score(new Data("a b c d e"), new Data("a b e"), null), 0.0001);
        assertEquals(.60, scorer.score(new Data("a b c d"), new Data("b a c d e"), null), 0.0001);
        assertEquals(.60, scorer.score(new Data("a b c d"), new Data("a c b d e"), null), 0.0001);
        assertEquals(.60, scorer.score(new Data("a b c d e"), new Data("b a c e"), null), 0.0001);
    }
    
    static class DataChunk implements TM3FuzzyComparable<DataChunk> {
        private String s;
        DataChunk(String s) {
            this.s = s;
        }
        @Override
        public float fuzzyCompare(DataChunk target) {
            if (s.equals(target.s)) {
                return 1.0f;
            }
            if (s.equalsIgnoreCase(target.s)) {
                return 0.75f;
            }
            return 0;
        }
        @Override
        public String toString() {
            return s;
        }
    }
    
    // TM3Data
    static class Data implements TM3Scorable<DataChunk> {
        private String s;
        public Data(String s) {
            this.s = s;
        }
        
        @Override
        public long getFingerprint() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DataChunk> getScoringObjects() {
            List<DataChunk> l = new ArrayList<DataChunk>();
            for (String ss : s.split(" ")) {
                l.add(new DataChunk(ss));
            }
            return l;
        }

        @Override
        public String getSerializedForm() {
            return s;
        }

        @Override
        public Iterable<Long> tokenize() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean equals(Object o) {
            return (o instanceof Data && ((Data)o).s.equals(s));
        }
        
        @Override
        public String toString() {
            return s;
        }
    }
}
