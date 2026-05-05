package com.jippy.foodandmart.validation;

import com.jippy.foodandmart.dto.FmMerchantRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Static utility class for parsing uploaded Excel (.xlsx) or CSV files
 * into a list of {@link FmMerchantRequestDTO}.
 *
 * <p>Why dynamic column detection: upload templates evolve over time and
 * different admins may customise column order. Building a column-name-to-index
 * map from the header row means the parser works regardless of column order.</p>
 *
 * <p>Supported header names (case-insensitive):
 * {@code firstName, lastName, dob, email, phone, outletType, accountNumber,
 * ifscCode, bankLocation, nameInBankAccount, uploadedBy, pan, adhar/aadhaar,
 * fssai, gstNumber}.</p>
 *
 * <p>File layout: Row 1 = headers; Row 2 (optional) = Yes/No indicator row
 * (auto-detected and skipped); data rows start from Row 2 or Row 3.</p>
 *
 * <p>NOTE: zone, approver, subscription, and plan columns are intentionally
 * ignored even if present — they are not part of the current data model.</p>
 */
@Slf4j
public class FmFileParser {

    /**
     * Private constructor — static utility class, must not be instantiated.
     */
    private FmFileParser() {}

    /**
     * Supported DOB input formats, tried in order when parsing a date string.
     * Using multiple formats accommodates different regional conventions in
     * the uploaded spreadsheets.
     */
    private static final List<DateTimeFormatter> DOB_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yy"),
            DateTimeFormatter.ofPattern("MM-dd-yy"),
            DateTimeFormatter.ofPattern("dd/MM/yy"),
            DateTimeFormatter.ofPattern("MM/dd/yy")
    );

    /** Output format for normalised DOB — always stored as YYYY-MM-DD. */
    private static final DateTimeFormatter DOB_OUT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Expands scientific-notation strings (e.g. "1.23457E+13") to plain integers.
     * Also strips trailing ".0" from numeric strings (e.g. "9876543210.0" → "9876543210").
     *
     * <p>Why needed: Excel serialises long numeric cell values (Aadhaar, account
     * numbers) in scientific notation when the number of digits exceeds 15.
     * Without this expansion, the value stored in the DB would be wrong.</p>
     *
     * @param value the raw cell string, possibly in scientific notation
     * @return a plain integer string, or the original value if not parseable as a number
     */
    static String expandScientificNotation(String value) {
        if (value == null || value.isBlank()) return value;
        String trimmed = value.trim();
        if (trimmed.matches(".*[Ee][+-]?\\d+.*") || trimmed.endsWith(".0")) {
            try {
                java.math.BigDecimal bd = new java.math.BigDecimal(trimmed);
                return bd.toBigIntegerExact().toString();
            } catch (Exception e) {
                try {
                    double d = Double.parseDouble(trimmed);
                    return String.valueOf(Math.round(d));
                } catch (Exception ignored) {}
            }
        }
        return trimmed;
    }

    /**
     * Normalises a DOB string from any supported input format to "YYYY-MM-DD".
     *
     * <p>Why try multiple formats: different admins use different date conventions.
     * Trying each format in order until one succeeds is more robust than requiring
     * a single fixed format.</p>
     *
     * <p>If no format matches, the raw value is passed through unchanged
     * so upstream validation annotations can produce a meaningful error message.</p>
     *
     * @param raw the raw date string from the file cell
     * @return normalised "YYYY-MM-DD" string, or the original value if unrecognised
     */
    static String normaliseDob(String raw) {
        if (raw == null || raw.isBlank()) return raw;
        String s = raw.trim();
        for (DateTimeFormatter fmt : DOB_FORMATS) {
            try {
                return LocalDate.parse(s, fmt).format(DOB_OUT);
            } catch (DateTimeParseException ignored) {}
        }
        log.warn("DOB value '{}' did not match any known format — passing through as-is", raw);
        return s;
    }

    // ── Excel ─────────────────────────────────────────────────────────────────

    /**
     * Parses an Excel (.xlsx) MultipartFile into a list of {@link FmMerchantRequestDTO}.
     *
     * <p>Why only process the first sheet: upload templates are single-sheet
     * workbooks. Additional sheets (e.g. instructions, lookup tables) would
     * produce invalid DTOs if processed.</p>
     *
     * <p>Why detect and skip the indicator row: the Jippy merchant upload
     * template includes a "Yes/No" row below the header showing which
     * columns are required. If all non-blank cells in row 2 are yes/no/y/n,
     * we treat it as an indicator row and skip it.</p>
     *
     * @param file the uploaded .xlsx file
     * @return list of parsed {@link FmMerchantRequestDTO}, one per data row
     * @throws IOException if the file stream cannot be read
     */
    public static List<FmMerchantRequestDTO> parseExcel(MultipartFile file) throws IOException {
        log.info("Parsing Excel file: name={}, size={} bytes", file.getOriginalFilename(), file.getSize());
        List<FmMerchantRequestDTO> dtos = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            log.debug("Excel sheet loaded: lastRowNum={}", lastRow);

            if (lastRow < 1) {
                log.warn("Excel file has no data rows (lastRowNum={})", lastRow);
                return dtos;
            }

            // Build a normalised column-name → column-index map from the header row
            Map<String, Integer> cm = buildExcelColMap(sheet.getRow(0));
            log.debug("Excel column map built: {} columns detected: {}", cm.size(), cm.keySet());

            int dataStart = 1;
            Row r1 = sheet.getRow(1);
            if (r1 != null && isIndicatorExcelRow(r1)) {
                log.info("Row 2 detected as Yes/No indicator row — skipping");
                dataStart = 2;
            }

            for (int i = dataStart; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isExcelRowEmpty(row)) {
                    log.debug("Skipping empty Excel row at index {}", i);
                    continue;
                }
                dtos.add(mapExcelRow(row, cm));
            }
        }

        log.info("Excel parsing complete: {} merchant rows extracted", dtos.size());
        return dtos;
    }

    /**
     * Builds a map of normalised column-name → column-index from an Excel header row.
     *
     * <p>Why normalise (lowercase): allows the header to use "FirstName",
     * "firstName", or "FIRSTNAME" interchangeably, making the parser robust
     * against capitalisation differences between template versions.</p>
     *
     * @param hdr the Excel header row (may be null)
     * @return a map of lowercase column name → zero-based column index
     */
    private static Map<String, Integer> buildExcelColMap(Row hdr) {
        Map<String, Integer> m = new LinkedHashMap<>();
        if (hdr == null) return m;
        for (int c = hdr.getFirstCellNum(); c < hdr.getLastCellNum(); c++) {
            Cell cell = hdr.getCell(c);
            if (cell != null) {
                String key = cell.getStringCellValue().trim().toLowerCase();
                m.put(key, c);
            }
        }
        return m;
    }

    /**
     * Detects whether an Excel row is the Yes/No indicator row by checking
     * if every non-blank cell contains only "yes", "no", "y", or "n".
     *
     * @param row the Excel row to inspect
     * @return true if all non-blank cells are yes/no indicators
     */
    private static boolean isIndicatorExcelRow(Row row) {
        int checked = 0, matched = 0;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell == null) continue;
            String v = excelCellStr(cell).toLowerCase();
            if (!v.isBlank()) {
                checked++;
                if (v.equals("yes") || v.equals("no") || v.equals("y") || v.equals("n")) matched++;
            }
        }
        return checked > 0 && matched == checked;
    }

    /**
     * Maps a single Excel row to a {@link FmMerchantRequestDTO} using setter methods.
     *
     * <p>Why use setters instead of the Lombok builder: consistent with the
     * project-wide convention of setter-based construction.</p>
     *
     * <p>Why zone/approver/subscription/plan are not mapped: those columns were
     * removed from the data model. Silently ignoring them means older template
     * files still work without errors.</p>
     *
     * @param row the Excel data row
     * @param m   the column name → index map
     * @return a {@link FmMerchantRequestDTO} populated from the row's cell values
     */
    private static FmMerchantRequestDTO mapExcelRow(Row row, Map<String, Integer> m) {
        FmMerchantRequestDTO dto = new FmMerchantRequestDTO();
        dto.setFirstName(ec(row, m, "firstname"));
        dto.setLastName(ec(row, m, "lastname"));
        dto.setDob(normaliseDob(ec(row, m, "dob")));
        dto.setEmail(ec(row, m, "email"));
        dto.setPhone(ec(row, m, "phone"));
        dto.setOutletType(ec(row, m, "outlettype"));
        // Account number may be in scientific notation for long values
        dto.setAccountNumber(expandScientificNotation(ec(row, m, "accountnumber")));
        dto.setIfscCode(ec(row, m, "ifsccode"));
        dto.setBankLocation(ec(row, m, "banklocation"));
        dto.setNameInBankAccount(ec(row, m, "nameinbankaccount"));
        dto.setUploadedBy(ec(row, m, "uploadedby"));
        dto.setPan(ec(row, m, "pan"));
        // Accept both "adhar" (Indian abbreviation) and "aadhaar" (full spelling)
        dto.setAdhar(expandScientificNotation(firstNonEmpty(ec(row, m, "adhar"), ec(row, m, "aadhaar"))));
        dto.setFssai(expandScientificNotation(ec(row, m, "fssai")));
        dto.setGstNumber(ec(row, m, "gstnumber"));
        return dto;
    }

    /**
     * Reads a cell value by column name from an Excel row, returning empty string if absent.
     *
     * @param row the Excel row
     * @param m   column name → index map
     * @param key the normalised column name to look up
     * @return the cell's string value, or empty string if the column is not present
     */
    private static String ec(Row row, Map<String, Integer> m, String key) {
        Integer idx = m.get(key);
        if (idx == null) return "";
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : excelCellStr(cell);
    }

    /**
     * Converts any Excel {@link Cell} to a plain string.
     *
     * <p>Why handle NUMERIC specially: integer cells (merchant ID, phone)
     * are stored as doubles in the POI model. Casting to long removes the
     * decimal before converting to string.</p>
     *
     * <p>Why handle date-formatted cells: DOB cells are often formatted as
     * Excel dates. Extracting as {@code LocalDate} and converting to ISO
     * string avoids locale-dependent date formatting.</p>
     *
     * @param cell the cell to convert
     * @return string representation of the cell value
     */
    private static String excelCellStr(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell))
                    // Date cells: extract as LocalDate → ISO string
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                // Numeric cells: cast to long to remove ".0" suffix
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    /**
     * Returns true if every cell in the Excel row is blank or empty.
     *
     * <p>Why skip empty rows: Excel files often have trailing empty rows
     * at the bottom due to formatting. Processing them would create DTOs
     * with all-null fields that would fail validation and clutter the error log.</p>
     *
     * @param row the Excel row to check
     * @return true if the row has no non-blank cell values
     */
    private static boolean isExcelRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !excelCellStr(cell).isBlank()) return false;
        }
        return true;
    }

    // ── CSV ───────────────────────────────────────────────────────────────────

    /**
     * Parses a CSV MultipartFile into a list of {@link FmMerchantRequestDTO}.
     *
     * <p>Why a custom CSV splitter instead of using a library like OpenCSV:
     * the file is already small and the splitting logic is simple. Avoiding
     * a dependency keeps the parser self-contained and removes a potential
     * point of version conflict.</p>
     *
     * <p>Why check for indicator row on line 2: the CSV template mirrors the
     * Excel template which has a Yes/No indicator row. If every non-blank
     * cell in line 2 is "yes"/"no"/"y"/"n", we skip it.</p>
     *
     * @param file the uploaded .csv file
     * @return list of parsed {@link FmMerchantRequestDTO}, one per data row
     * @throws IOException if the file stream cannot be read
     */
    public static List<FmMerchantRequestDTO> parseCsv(MultipartFile file) throws IOException {
        log.info("Parsing CSV file: name={}, size={} bytes", file.getOriginalFilename(), file.getSize());
        List<FmMerchantRequestDTO> dtos = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNum = 0;
            Map<String, Integer> cm = new LinkedHashMap<>();

            while ((line = reader.readLine()) != null) {
                lineNum++;

                if (lineNum == 1) {
                    // First line is the header — build the column map
                    String[] hdr = splitCsv(line);
                    for (int i = 0; i < hdr.length; i++) {
                        cm.put(hdr[i].trim().toLowerCase(), i);
                    }
                    log.debug("CSV header parsed: {} columns: {}", cm.size(), cm.keySet());
                    log.info("cm map:"+cm.toString() );
                    continue;

                }

                if (line.trim().isEmpty()) {
                    log.debug("Skipping empty CSV line at lineNum={}", lineNum);
                    continue;
                }

                String[] cols = splitCsv(line);
log.info("clos value:"+cols.toString());
                // Auto-detect and skip the Yes/No indicator row if present on line 2
                if (lineNum == 2 && isIndicatorCsvRow(cols)) {
                    log.info("CSV line 2 detected as Yes/No indicator row — skipping");
                    continue;
                }

                dtos.add(mapCsvRow(cols, cm));
            }
        }

        log.info("CSV parsing complete: {} merchant rows extracted", dtos.size());
        return dtos;
    }

    /**
     * Detects whether a CSV row is the Yes/No indicator row by checking
     * if every non-blank cell contains only "yes", "no", "y", or "n".
     *
     * @param cols the split cell values of the row
     * @return true if all non-blank cells are yes/no indicators
     */
    private static boolean isIndicatorCsvRow(String[] cols) {
        int checked = 0, matched = 0;
        for (String col : cols) {
            String v = col.trim().toLowerCase();
            if (!v.isBlank()) {
                checked++;
                if (v.equals("yes") || v.equals("no") || v.equals("y") || v.equals("n")) matched++;
            }
        }
        return checked > 0 && matched == checked;
    }

    /**
     * Maps a single CSV row (array of cell strings) to a {@link FmMerchantRequestDTO}.
     *
     * <p>Why use setters instead of the Lombok builder: consistent with the
     * project-wide convention of setter-based object construction.</p>
     *
     * <p>Why zone/approver/subscription/plan are not mapped: those columns were
     * removed from the data model; older CSV files that still include them work
     * without errors because unrecognised columns are silently ignored.</p>
     *
     * @param cols the CSV cell values for this data row
     * @param m    the column name → index map from the header row
     * @return a populated {@link FmMerchantRequestDTO}
     */
    private static FmMerchantRequestDTO mapCsvRow(String[] cols, Map<String, Integer> m) {
        FmMerchantRequestDTO dto = new FmMerchantRequestDTO();
        dto.setFirstName(cc(cols, m, "firstname"));
        dto.setLastName(cc(cols, m, "lastname"));
        dto.setDob(normaliseDob(cc(cols, m, "dob")));
        dto.setEmail(cc(cols, m, "email"));
        dto.setPhone(cc(cols, m, "phone"));
        dto.setOutletType(cc(cols, m, "outlettype"));
        dto.setAccountNumber(expandScientificNotation(cc(cols, m, "accountnumber")));
        dto.setIfscCode(cc(cols, m, "ifsccode"));
        dto.setBankLocation(cc(cols, m, "banklocation"));
        dto.setNameInBankAccount(cc(cols, m, "nameinbankaccount"));
        dto.setUploadedBy(cc(cols, m, "uploadedby"));
        dto.setPan(cc(cols, m, "pan"));
        dto.setAdhar(expandScientificNotation(firstNonEmpty(cc(cols, m, "adhar"), cc(cols, m, "aadhaar"))));
        dto.setFssai(expandScientificNotation(cc(cols, m, "fssai")));
        dto.setGstNumber(cc(cols, m, "gstnumber"));
        return dto;
    }

    /**
     * Reads a cell value by column name from a CSV row, returning empty string if absent.
     *
     * <p>Why bounds-check: a short row (fewer columns than the header) would
     * throw {@code ArrayIndexOutOfBoundsException} without this guard.</p>
     *
     * @param cols the cell array for this row
     * @param m    column name → index map
     * @param key  the normalised column name to look up
     * @return the trimmed cell value with surrounding quotes stripped, or empty string
     */
    private static String cc(String[] cols, Map<String, Integer> m, String key) {
        Integer idx = m.get(key);
        if (idx == null || idx >= cols.length) return "";
        // Strip surrounding double-quotes added by some CSV writers
        return cols[idx].trim().replaceAll("^\"|\"$", "");
    }

    /**
     * Splits a CSV line into an array of cell strings, respecting quoted fields.
     *
     * <p>Why a custom splitter instead of {@code line.split(",")}: standard
     * {@code split} does not handle quoted fields that contain commas, e.g.
     * {@code "Sharma, Ravi",ravi@example.com} would split incorrectly on the
     * comma inside the quoted name.</p>
     *
     * @param line the raw CSV line
     * @return array of unquoted cell values
     */
    private static String[] splitCsv(String line) {
        log.info("line:"+line);
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        log.info("result:"+result.toString());
        return result.toArray(new String[0]);
    }

    /**
     * Returns the first non-blank string from two candidates.
     *
     * <p>Why needed: the Aadhaar column may be labelled "adhar" or "aadhaar"
     * depending on the template version. This helper returns whichever one
     * has a value, falling back to the second if the first is blank.</p>
     *
     * @param a the preferred value
     * @param b the fallback value
     * @return a if non-blank, otherwise b (which may itself be blank)
     */
    private static String firstNonEmpty(String a, String b) {
        return (a != null && !a.isBlank()) ? a : (b != null ? b : "");
    }
}
