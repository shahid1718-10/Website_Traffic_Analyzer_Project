import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Website Traffic and Search Performance Analyzer
 * Beginner-friendly console analytics tool. It reads local CSV files only.
 */
public class AnalyticsApp {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        Path csvPath;
        if (args.length > 0) {
            csvPath = Paths.get(args[0]);
        } else {
            System.out.println("Website Traffic and Search Performance Analyzer");
            System.out.println("Enter CSV path (or press Enter for data/sample_analytics.csv):");
            String input = SCANNER.nextLine().trim();
            csvPath = Paths.get(input.isEmpty() ? "data/sample_analytics.csv" : input);
        }
        try {
            List<Map<String,String>> rows = CsvReader.read(csvPath);
            if (rows.isEmpty()) {
                System.out.println("No data records found in " + csvPath);
                return;
            }
            AnalyticsReport report = new AnalyticsReport(rows);
            report.print();
        } catch (IOException e) {
            System.err.println("Could not read CSV: " + e.getMessage());
            System.err.println("Check the path, header row, quotes, and number of columns.");
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid dataset: " + e.getMessage());
        }
    }
}
