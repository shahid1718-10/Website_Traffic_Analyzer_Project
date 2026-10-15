# Test cases

These are proposed/manual test cases, not a claim that every case has been executed.

1. Default sample: run `java -cp out AnalyticsApp`; expect visits, page views, sources, pages, daily totals, CTR, queries, and weighted position.
2. Traffic-only sample: run `java -cp out AnalyticsApp data/sample_traffic_only.csv`; expect traffic metrics and search metrics marked unavailable.
3. Missing file: pass a nonexistent path; expect a readable I/O error.
4. Missing required column: remove `page_views`; expect a missing-column validation error.
5. Invalid date: change a date to `01/09/2026`; expect a date-format error.
6. Negative visits: set visits to `-3`; expect a validation error.
7. Zero impressions: use search columns with all impressions zero; CTR should print 0.00% with a warning.
8. Quoted comma: use a page field such as `"/blog/java, basics"`; parser should keep it as one field.
9. Mismatched row: remove a field from one row; expect a line-number error.
10. Blank visitor IDs: blanks should not count as unique visitors.
