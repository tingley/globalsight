Exact match key migration tool

This tool calculates exact match keys and persists them for the
following segments:

1. Segment state is COMPLETE, LOCALIZED or ALIGNMENT_LOCALIZED.
2. exact_match_key is 0 (exact match key is not calculated before).


Instructions:
1. Unzip the archive.
2. Go to <weblogic home> and type "setenv.cmd".
3. Go to system4_exactmatchkey and type System4ExactMatchKey.bat plus
   parameters if applicable. The command-line syntax is as follows.

Usage: System4ExactMatchKey [-q]
  -q,      Suppress the confirmation message.

