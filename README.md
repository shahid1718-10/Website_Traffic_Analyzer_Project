# Website Traffic and Search Performance Analyzer

A beginner-friendly Java console application for analyzing local CSV data. It does **not** connect to a live website, Google Analytics, Google Search Console, or social platforms.

## Requirements
- Java JDK 17 or later (Java 25 should also work)
- No third-party dependencies

## Run
From the project root:

```bash
javac -d out src/*.java
java -cp out AnalyticsApp
```

Press Enter to load `data/sample_analytics.csv`, or enter another CSV path. You can also pass the path as an argument:

```bash
java -cp out AnalyticsApp data/sample_traffic_only.csv
```

## CSV format

Required columns: `date,page,source,visits,page_views`

Optional columns:
- `visitor_id`: unique visitor estimate based on distinct non-empty IDs in the file.
- `query`, `impressions`, `clicks`: all three are required to show search metrics.
- `average_position`: used only when search columns are also present. The program calculates an impression-weighted average of the supplied row positions.

Dates must use `YYYY-MM-DD`. Visits, page views, impressions, and clicks must be non-negative whole numbers. Blank numeric fields are treated as zero. Invalid numbers, negative numbers, invalid dates, malformed CSV quoting, missing required headers, or mismatched row field counts produce an error.

The same search query may appear on multiple rows; impressions and clicks are summed by query. This dataset's search figures are illustrative, not real analytics. The sample repeats query-level values on some traffic rows, so the search totals are totals of the provided records—not a deduplicated Google Search Console export. For real data, prepare a dedicated search-performance CSV with one record per date/query/page combination to avoid accidental double-counting.

## Calculations
- Total visits = sum of `visits`.
- Total page views = sum of `page_views`.
- Unique visitors = count of distinct non-empty `visitor_id` values; only an estimate if the source IDs are genuinely user-level identifiers and the export period is appropriate.
- Traffic-source totals = sum of visits grouped by `source`.
- Top pages = rank by summed page views.
- Daily traffic = sum of visits grouped by date.
- Aggregate CTR = total clicks / total impressions × 100. If impressions are zero, CTR is displayed as 0.00% and the recommendation warns that it is not meaningful.
- Weighted average position = sum(position × impressions) / sum(impressions) for rows with supplied position and impression weights. This is only an approximation if rows are already aggregated or overlapping.

## Architecture
- `AnalyticsApp`: command-line entry point and error handling.
- `CsvReader`: reads and parses CSV rows, including quoted commas and escaped quotes.
- `AnalyticsReport`: validates fields, calculates metrics, ranks pages/queries, and prints recommendations.

## Tests
See `tests/test_cases.md` for manual test cases. Compilation and runtime execution status should be checked in your local Java environment before submission.
