import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Small CSV reader supporting quoted fields and escaped double quotes. */
public class CsvReader {
    public static List<Map<String, String>> read(Path path) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = br.readLine();
            if (headerLine == null) throw new IOException("CSV file is empty.");
            List<String> headers = parseLine(headerLine);
            if (headers.isEmpty()) throw new IOException("CSV header is missing.");
            String line;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) continue;
                List<String> values = parseLine(line);
                if (values.size() != headers.size())
                    throw new IOException("Line " + lineNo + " has " + values.size()
                        + " fields; expected " + headers.size() + ".");
                Map<String, String> row = new LinkedHashMap<>();
                for (int i=0; i<headers.size(); i++) row.put(headers.get(i).trim(), values.get(i).trim());
                rows.add(row);
            }
        }
        return rows;
    }

    private static List<String> parseLine(String line) throws IOException {
        List<String> out = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i=0; i<line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i+1 < line.length() && line.charAt(i+1) == '"') {
                    field.append('"'); i++;
                } else quoted = !quoted;
            } else if (c == ',' && !quoted) {
                out.add(field.toString()); field.setLength(0);
            } else field.append(c);
        }
        if (quoted) throw new IOException("Unclosed quote in CSV line: " + line);
        out.add(field.toString());
        return out;
    }
}
