import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class AnalyticsReport {
    private final List<Map<String,String>> rows;
    private final Map<String, Long> sourceVisits = new TreeMap<>();
    private final Map<String, Long> pageViews = new HashMap<>();
    private final Map<String, Long> pageVisits = new HashMap<>();
    private final Map<String, Long> dailyVisits = new TreeMap<>();
    private final Map<String, Long> queryClicks = new HashMap<>();
    private final Map<String, Long> queryImpressions = new HashMap<>();
    private long totalVisits, totalViews, totalClicks, totalImpressions;
    private long uniqueVisitors;
    private boolean hasVisitorId, hasSearchColumns, hasPosition;
    private double weightedPositionSum;
    private long positionWeight;

    public AnalyticsReport(List<Map<String,String>> rows) {
        this.rows = rows;
        Set<String> headers = rows.get(0).keySet();
        String[] required = {"date","page","source","visits","page_views"};
        for (String h : required) if (!headers.contains(h))
            throw new IllegalArgumentException("Missing required column: " + h);
        hasVisitorId = headers.contains("visitor_id");
        hasSearchColumns = headers.contains("query") && headers.contains("impressions") && headers.contains("clicks");
        hasPosition = headers.contains("average_position");
        Set<String> visitors = new HashSet<>();
        for (int i=0; i<rows.size(); i++) {
            Map<String,String> r = rows.get(i);
            String date = value(r,"date");
            try { LocalDate.parse(date); }
            catch (DateTimeParseException e) { throw new IllegalArgumentException("Invalid date on data row " + (i+2) + ": " + date + " (use YYYY-MM-DD)"); }
            long visits = number(r,"visits",i);
            long views = number(r,"page_views",i);
            totalVisits += visits; totalViews += views;
            String source = normal(value(r,"source"),"unknown");
            String page = normal(value(r,"page"),"unknown");
            sourceVisits.merge(source, visits, Long::sum);
            pageVisits.merge(page, visits, Long::sum);
            pageViews.merge(page, views, Long::sum);
            dailyVisits.merge(date, visits, Long::sum);
            if (hasVisitorId && !value(r,"visitor_id").isBlank()) visitors.add(value(r,"visitor_id"));
            if (hasSearchColumns) {
                long imp = numberField(r,"impressions",i);
                long clicks = numberField(r,"clicks",i);
                totalImpressions += imp; totalClicks += clicks;
                String query = normal(value(r,"query"),"(not provided)");
                queryClicks.merge(query, clicks, Long::sum);
                queryImpressions.merge(query, imp, Long::sum);
            }
            if (hasPosition && hasSearchColumns) {
                String p = value(r,"average_position");
                if (!p.isBlank()) {
                    double pos;
                    try { pos = Double.parseDouble(p); }
                    catch (NumberFormatException e) { throw new IllegalArgumentException("Invalid average_position on data row " + (i+2)); }
                    if (pos < 0) throw new IllegalArgumentException("average_position cannot be negative.");
                    long weight = numberField(r,"impressions",i);
                    weightedPositionSum += pos * weight;
                    positionWeight += weight;
                }
            }
        }
        uniqueVisitors = visitors.size();
    }

    private static String value(Map<String,String> row, String key) { return row.getOrDefault(key,"").trim(); }
    private static String normal(String s, String fallback) { return s.isBlank() ? fallback : s.trim(); }
    private static long number(Map<String,String> r, String key, int row) { return numberField(r,key,row); }
    private static long numberField(Map<String,String> r, String key, int row) {
        String s = value(r,key);
        if (s.isBlank()) return 0;
        try {
            long n = Long.parseLong(s);
            if (n < 0) throw new IllegalArgumentException(key + " cannot be negative on data row " + (row+2));
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer in column '" + key + "' on data row " + (row+2) + ": " + s);
        }
    }

    public void print() {
        System.out.println("\n=== ANALYTICS SUMMARY ===");
        System.out.println("Records analyzed: " + rows.size());
        System.out.println("Total visits: " + totalVisits);
        System.out.println("Total page views: " + totalViews);
        System.out.println("Unique visitors: " + (hasVisitorId ? uniqueVisitors : "Not available (visitor_id column missing)"));
        System.out.println("\n--- Visits by traffic source ---");
        sourceVisits.forEach((k,v) -> System.out.println(k + ": " + v));
        System.out.println("\n--- Top pages by page views ---");
        top(pageViews,5).forEach((k,v) -> System.out.println(k + ": " + v));
        System.out.println("\n--- Daily visits ---");
        dailyVisits.forEach((k,v) -> System.out.println(k + ": " + v));
        if (hasSearchColumns) {
            double ctr = totalImpressions == 0 ? 0.0 : (totalClicks * 100.0 / totalImpressions);
            System.out.printf(Locale.US, "%nTotal search impressions: %d%nTotal search clicks: %d%nAggregate CTR: %.2f%%%n", totalImpressions,totalClicks,ctr);
            System.out.println("\n--- Top queries by clicks ---");
            top(queryClicks,5).forEach((k,v) -> System.out.println(k + ": " + v + " clicks"));
            if (hasPosition) {
                if (positionWeight == 0) System.out.println("Weighted average position: Not available (no positive impression weight)");
                else System.out.printf(Locale.US,"Weighted average position: %.2f%n",weightedPositionSum/positionWeight);
            } else System.out.println("Average position: Not available (average_position column missing)");
        } else {
            System.out.println("\nSearch metrics: Not available (requires query, impressions, and clicks columns).");
        }
        System.out.println("\n--- Basic recommendations ---");
        recommend();
        System.out.println("\nNote: output is based only on the selected CSV; sample data is illustrative.");
    }

    private static Map<String,Long> top(Map<String,Long> map, int limit) {
        List<Map.Entry<String,Long>> list = new ArrayList<>(map.entrySet());
        list.sort((a,b) -> { int c = Long.compare(b.getValue(),a.getValue()); return c != 0 ? c : a.getKey().compareTo(b.getKey()); });
        Map<String,Long> result = new LinkedHashMap<>();
        for (int i=0; i<Math.min(limit,list.size()); i++) result.put(list.get(i).getKey(),list.get(i).getValue());
        return result;
    }

    private void recommend() {
        if (!sourceVisits.isEmpty()) {
            Map.Entry<String,Long> best = top(sourceVisits,1).entrySet().iterator().next();
            System.out.println("- Largest visit source is '" + best.getKey() + "' (" + best.getValue() + " visits); compare it with other channels before allocating effort.");
        }
        if (!pageViews.isEmpty()) {
            Map.Entry<String,Long> best = top(pageViews,1).entrySet().iterator().next();
            System.out.println("- Most viewed page is '" + best.getKey() + "'; review its content and make it easy to navigate.");
        }
        if (hasSearchColumns) {
            if (totalImpressions == 0) System.out.println("- Search CTR cannot be meaningfully interpreted because total impressions are zero.");
            else if (totalClicks * 100.0 / totalImpressions < 3.0) System.out.println("- Aggregate CTR is below 3% in this dataset; review query relevance and page titles. This is a simple heuristic, not a universal benchmark.");
            else System.out.println("- Search CTR is at least 3% in this dataset; inspect high-impression queries for further opportunities.");
        }
    }
}
