package com.jippy.foodandmart.validation;

import com.jippy.foodandmart.dto.FmMerchantRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FmFileParser {

    private FmFileParser() {
    }

    // ============================================================
    // DOB FORMATS
    // ============================================================

    private static final List<DateTimeFormatter> DOB_FORMATS = List.of(DateTimeFormatter.ofPattern("yyyy-MM-dd"),

            DateTimeFormatter.ofPattern("M-d-yyyy"), DateTimeFormatter.ofPattern("MM-dd-yyyy"), DateTimeFormatter.ofPattern("d-M-yyyy"), DateTimeFormatter.ofPattern("dd-MM-yyyy"),

            DateTimeFormatter.ofPattern("M/d/yyyy"), DateTimeFormatter.ofPattern("MM/dd/yyyy"), DateTimeFormatter.ofPattern("d/M/yyyy"), DateTimeFormatter.ofPattern("dd/MM/yyyy"),

            DateTimeFormatter.ofPattern("yyyy/M/d"), DateTimeFormatter.ofPattern("yyyy/MM/dd"),

            DateTimeFormatter.ofPattern("d-M-yy"), DateTimeFormatter.ofPattern("dd-MM-yy"), DateTimeFormatter.ofPattern("M-d-yy"), DateTimeFormatter.ofPattern("MM-dd-yy"),

            DateTimeFormatter.ofPattern("d/M/yy"), DateTimeFormatter.ofPattern("dd/MM/yy"), DateTimeFormatter.ofPattern("M/d/yy"), DateTimeFormatter.ofPattern("MM/dd/yy"));

    private static final DateTimeFormatter DOB_OUT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ============================================================
    // SCIENTIFIC NOTATION
    // ============================================================

    static String expandScientificNotation(String value) {

        if (value == null || value.isBlank()) {
            return value;
        }

        String trimmed = value.trim();

        if (trimmed.matches(".*[Ee][+-]?\\d+.*") || trimmed.endsWith(".0")) {

            try {
                BigDecimal bd = new BigDecimal(trimmed);
                return bd.toBigIntegerExact().toString();

            } catch (Exception e) {

                try {
                    double d = Double.parseDouble(trimmed);
                    return String.valueOf(Math.round(d));

                } catch (Exception ignored) {
                    // Keep original value.
                }
            }
        }

        return trimmed;
    }

    // ============================================================
    // DOB NORMALIZATION
    // ============================================================

    static String normaliseDob(String raw) {

        if (raw == null || raw.isBlank()) {
            return raw;
        }

        String s = raw.trim().replace("\uFEFF", "").replace("\"", "");

        /*
         * Handles values such as:
         *
         * 8/15/1995
         * 08/15/1995
         * 8-15-1995
         * 1995-08-15
         */
        for (DateTimeFormatter formatter : DOB_FORMATS) {

            try {
                LocalDate date = LocalDate.parse(s, formatter);

                String result = date.format(DOB_OUT);

                log.debug("DOB normalized: '{}' -> '{}'", raw, result);

                return result;

            } catch (DateTimeParseException ignored) {
                // Try next format.
            }
        }

        log.warn("DOB value '{}' did not match any known format — passing through as-is", raw);

        return s;
    }

    // ============================================================
    // EXCEL
    // ============================================================

    public static List<FmMerchantRequestDTO> parseExcel(MultipartFile file) throws IOException {

        log.info("Parsing Excel file: name={}, size={} bytes", file.getOriginalFilename(), file.getSize());

        List<FmMerchantRequestDTO> dtos = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            int lastRow = sheet.getLastRowNum();

            if (lastRow < 1) {
                log.warn("Excel file has no data rows");
                return dtos;
            }

            Map<String, Integer> columnMap = buildExcelColMap(sheet.getRow(0));

            log.info("Excel column map: {}", columnMap);

            int dataStart = 1;

            Row indicatorRow = sheet.getRow(1);

            if (indicatorRow != null && isIndicatorExcelRow(indicatorRow)) {

                log.info("Excel row 2 detected as Yes/No indicator row — skipping");

                dataStart = 2;
            }

            for (int rowIndex = dataStart; rowIndex <= lastRow; rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null || isExcelRowEmpty(row)) {
                    continue;
                }

                FmMerchantRequestDTO dto = mapExcelRow(row, columnMap);

                dtos.add(dto);
            }
        }

        log.info("Excel parsing complete: {} merchant rows extracted", dtos.size());

        return dtos;
    }

    // ============================================================
    // EXCEL COLUMN MAP
    // ============================================================

    private static Map<String, Integer> buildExcelColMap(Row header) {

        Map<String, Integer> map = new LinkedHashMap<>();

        if (header == null) {
            return map;
        }

        for (int column = header.getFirstCellNum(); column < header.getLastCellNum(); column++) {

            Cell cell = header.getCell(column);

            if (cell == null) {
                continue;
            }

            String key = normaliseColumnName(excelCellStr(cell));

            if (!key.isBlank()) {
                map.put(key, column);
            }
        }

        return map;
    }

    // ============================================================
    // EXCEL INDICATOR ROW
    // ============================================================

    private static boolean isIndicatorExcelRow(Row row) {

        int checked = 0;
        int matched = 0;

        for (int column = row.getFirstCellNum(); column < row.getLastCellNum(); column++) {

            Cell cell = row.getCell(column);

            if (cell == null) {
                continue;
            }

            String value = excelCellStr(cell).trim().toLowerCase();

            if (!value.isBlank()) {

                checked++;

                if (value.equals("yes") || value.equals("no") || value.equals("y") || value.equals("n")) {

                    matched++;
                }
            }
        }

        return checked > 0 && matched == checked;
    }

    // ============================================================
    // EXCEL ROW MAPPING
    // ============================================================

    private static FmMerchantRequestDTO mapExcelRow(Row row, Map<String, Integer> map) {

        FmMerchantRequestDTO dto = new FmMerchantRequestDTO();

        dto.setFirstName(ec(row, map, "firstname"));

        dto.setLastName(ec(row, map, "lastname"));

        dto.setDob(normaliseDob(ec(row, map, "dob")));

        // ========================================================
        // EMAIL
        // ========================================================

        dto.setEmail(ec(row, map, "email"));

        // ========================================================
        // PHONE
        // ========================================================

        dto.setPhone(ec(row, map, "phone"));

        // ========================================================
        // USERNAME / PASSWORD
        //
        // Optional for bulk.
        //
        // If supplied -> service uses supplied value.
        // If blank -> service generates default.
        // ========================================================

        dto.setUsername(firstNonEmpty(ec(row, map, "username"), ec(row, map, "user")));

        dto.setPassword(ec(row, map, "password"));

        // ========================================================
        // BUSINESS
        // ========================================================

        dto.setOutletType(ec(row, map, "outlettype"));

        dto.setUploadedBy(ec(row, map, "uploadedby"));

        // ========================================================
        // KYC
        // ========================================================

        dto.setPan(ec(row, map, "pan"));

        dto.setAdhar(expandScientificNotation(firstNonEmpty(ec(row, map, "adhar"), ec(row, map, "aadhaar"))));

        dto.setFssai(expandScientificNotation(ec(row, map, "fssai")));

        dto.setGstNumber(ec(row, map, "gstnumber"));

        // ========================================================
        // BANK
        // ========================================================

        dto.setAccountNumber(expandScientificNotation(ec(row, map, "accountnumber")));

        dto.setIfscCode(ec(row, map, "ifsccode"));

        dto.setBankLocation(ec(row, map, "banklocation"));

        dto.setNameInBankAccount(ec(row, map, "nameinbankaccount"));

        // ========================================================
        // ADDRESS
        // ========================================================

        dto.setBuildingNumber(firstNonEmpty(ec(row, map, "buildingnumber"), ec(row, map, "building")));

        dto.setRoad(ec(row, map, "road"));

        dto.setLandmark(ec(row, map, "landmark"));

        dto.setStateName(firstNonEmpty(ec(row, map, "state"), ec(row, map, "statename")));

        dto.setCityName(firstNonEmpty(ec(row, map, "city"), ec(row, map, "cityname")));

        dto.setAreaName(firstNonEmpty(ec(row, map, "area"), ec(row, map, "areaname")));

        // ========================================================
        // LOCATION
        // ========================================================

        dto.setLatitude(ec(row, map, "latitude"));

        dto.setLongitude(ec(row, map, "longitude"));

        return dto;
    }

    // ============================================================
    // EXCEL CELL VALUE
    // ============================================================

    private static String ec(Row row, Map<String, Integer> map, String key) {

        Integer index = map.get(key);

        if (index == null) {
            return "";
        }

        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (cell == null) {
            return "";
        }

        return excelCellStr(cell);
    }

    // ============================================================
    // EXCEL CELL TO STRING
    // ============================================================

    private static String excelCellStr(Cell cell) {

        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {

            case STRING -> cell.getStringCellValue().trim();

            case NUMERIC -> {

                if (DateUtil.isCellDateFormatted(cell)) {

                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }

                double number = cell.getNumericCellValue();

                /*
                 * Do not use double for long identifiers
                 * where Excel has already lost precision.
                 *
                 * For normal phone/account-like values this
                 * removes the unwanted .0.
                 */
                if (number == Math.floor(number) && number <= Long.MAX_VALUE) {

                    yield String.valueOf((long) number);
                }

                yield String.valueOf(number);
            }

            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());

            case FORMULA -> {

                try {

                    yield cell.getStringCellValue().trim();

                } catch (Exception e) {

                    yield cell.getCellFormula().trim();
                }
            }

            default -> "";
        };
    }

    // ============================================================
    // EMPTY EXCEL ROW
    // ============================================================

    private static boolean isExcelRowEmpty(Row row) {

        for (int column = row.getFirstCellNum(); column < row.getLastCellNum(); column++) {

            Cell cell = row.getCell(column);

            if (cell != null && cell.getCellType() != CellType.BLANK && !excelCellStr(cell).isBlank()) {

                return false;
            }
        }

        return true;
    }

    // ============================================================
    // CSV
    // ============================================================

    public static List<FmMerchantRequestDTO> parseCsv(MultipartFile file) throws IOException {

        log.info("Parsing CSV file: name={}, size={} bytes", file.getOriginalFilename(), file.getSize());

        List<FmMerchantRequestDTO> dtos = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String line;

            int lineNumber = 0;

            Map<String, Integer> columnMap = new LinkedHashMap<>();

            while ((line = reader.readLine()) != null) {

                lineNumber++;

                // =================================================
                // HEADER
                // =================================================

                if (lineNumber == 1) {

                    String[] headers = splitCsv(line);

                    for (int i = 0; i < headers.length; i++) {

                        String key = normaliseColumnName(headers[i]);

                        if (!key.isBlank()) {
                            columnMap.put(key, i);
                        }
                    }

                    log.info("CSV column map: {}", columnMap);

                    continue;
                }

                // =================================================
                // EMPTY LINE
                // =================================================

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = splitCsv(line);

                // =================================================
                // YES / NO INDICATOR ROW
                // =================================================

                if (lineNumber == 2 && isIndicatorCsvRow(columns)) {

                    log.info("CSV line 2 detected as Yes/No indicator row — skipping");

                    continue;
                }

                // =================================================
                // MAP DATA ROW
                // =================================================

                FmMerchantRequestDTO dto = mapCsvRow(columns, columnMap);

                dtos.add(dto);
            }
        }

        log.info("CSV parsing complete: {} merchant rows extracted", dtos.size());

        return dtos;
    }

    // ============================================================
    // CSV HEADER NORMALIZATION
    // ============================================================

    private static String normaliseColumnName(String header) {

        if (header == null) {
            return "";
        }

        return header.replace("\uFEFF", "").replace("\"", "").trim().toLowerCase().replaceAll("[\\s_\\-]+", "");
    }

    // ============================================================
    // CSV INDICATOR ROW
    // ============================================================

    private static boolean isIndicatorCsvRow(String[] columns) {

        int checked = 0;
        int matched = 0;

        for (String column : columns) {

            if (column == null) {
                continue;
            }

            String value = column.trim().toLowerCase();

            if (!value.isBlank()) {

                checked++;

                if (value.equals("yes") || value.equals("no") || value.equals("y") || value.equals("n")) {

                    matched++;
                }
            }
        }

        return checked > 0 && matched == checked;
    }

    // ============================================================
    // CSV ROW MAPPING
    // ============================================================

    private static FmMerchantRequestDTO mapCsvRow(String[] columns, Map<String, Integer> map) {

        FmMerchantRequestDTO dto = new FmMerchantRequestDTO();

        // ========================================================
        // BASIC DETAILS
        // ========================================================

        dto.setFirstName(cc(columns, map, "firstname"));

        dto.setLastName(cc(columns, map, "lastname"));

        /*
         * IMPORTANT:
         *
         * This converts:
         *
         * 8/15/1995
         *
         * into:
         *
         * 1995-08-15
         *
         * BEFORE Bean Validation runs.
         */
        String rawDob = cc(columns, map, "dob");

        dto.setDob(normaliseDob(rawDob));

        // ========================================================
        // EMAIL
        // ========================================================

        dto.setEmail(cc(columns, map, "email"));

        // ========================================================
        // PHONE
        // ========================================================

        dto.setPhone(cc(columns, map, "phone"));

        // ========================================================
        // USERNAME
        //
        // OPTIONAL FOR BULK
        // ========================================================

        dto.setUsername(firstNonEmpty(cc(columns, map, "username"), cc(columns, map, "user")));

        // ========================================================
        // PASSWORD
        //
        // OPTIONAL FOR BULK
        // ========================================================

        dto.setPassword(cc(columns, map, "password"));

        // ========================================================
        // BUSINESS
        // ========================================================

        dto.setOutletType(cc(columns, map, "outlettype"));

        dto.setUploadedBy(cc(columns, map, "uploadedby"));

        // ========================================================
        // KYC
        // ========================================================

        dto.setPan(cc(columns, map, "pan"));

        dto.setAdhar(expandScientificNotation(firstNonEmpty(cc(columns, map, "adhar"), cc(columns, map, "aadhaar"))));

        dto.setFssai(expandScientificNotation(cc(columns, map, "fssai")));

        dto.setGstNumber(cc(columns, map, "gstnumber"));

        // ========================================================
        // BANK
        // ========================================================

        dto.setAccountNumber(expandScientificNotation(cc(columns, map, "accountnumber")));

        dto.setIfscCode(cc(columns, map, "ifsccode"));

        dto.setBankLocation(cc(columns, map, "banklocation"));

        dto.setNameInBankAccount(cc(columns, map, "nameinbankaccount"));

        // ========================================================
        // ADDRESS
        //
        // CSV provides names.
        //
        // Example:
        //
        // State = Telangana
        // City  = Hyderabad
        // Area  = Kukatpally
        //
        // Service resolves them to IDs.
        // ========================================================

        dto.setBuildingNumber(firstNonEmpty(cc(columns, map, "buildingnumber"), cc(columns, map, "building")));

        dto.setRoad(cc(columns, map, "road"));

        dto.setLandmark(cc(columns, map, "landmark"));

        dto.setStateName(firstNonEmpty(cc(columns, map, "state"), cc(columns, map, "statename")));

        dto.setCityName(firstNonEmpty(cc(columns, map, "city"), cc(columns, map, "cityname")));

        dto.setAreaName(firstNonEmpty(cc(columns, map, "area"), cc(columns, map, "areaname")));

        // ========================================================
        // LOCATION
        // ========================================================

        dto.setLatitude(cc(columns, map, "latitude"));

        dto.setLongitude(cc(columns, map, "longitude"));

        return dto;
    }

    // ============================================================
    // CSV CELL
    // ============================================================

    private static String cc(String[] columns, Map<String, Integer> map, String key) {

        Integer index = map.get(key);

        if (index == null || index < 0 || index >= columns.length) {

            return "";
        }

        String value = columns[index];

        if (value == null) {
            return "";
        }

        value = value.trim().replace("\uFEFF", "");

        // Remove surrounding quotes only.
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {

            value = value.substring(1, value.length() - 1);
        }

        return value.trim();
    }

    // ============================================================
    // CSV SPLITTER
    // ============================================================

    private static String[] splitCsv(String line) {

        List<String> result = new ArrayList<>();

        StringBuilder value = new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char character = line.charAt(i);

            if (character == '"') {

                /*
                 * Support escaped quotes:
                 *
                 * ""
                 */
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {

                    value.append('"');
                    i++;

                } else {

                    insideQuotes = !insideQuotes;
                }

            } else if (character == ',' && !insideQuotes) {

                result.add(value.toString());

                value.setLength(0);

            } else {

                value.append(character);
            }
        }

        result.add(value.toString());

        return result.toArray(new String[0]);
    }

    // ============================================================
    // FIRST NON EMPTY
    // ============================================================

    private static String firstNonEmpty(String first, String second) {

        if (first != null && !first.isBlank()) {

            return first.trim();
        }

        if (second != null && !second.isBlank()) {

            return second.trim();
        }

        return "";
    }
}