package com.jippy.foodandmart.util;

import com.jippy.foodandmart.dto.FmVariantBulkUploadRowDto;
import com.jippy.foodandmart.exception.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
public final class FmVariantExcelReader {

    private static final String PRODUCT_NAME = "Product Name";
    private static final String VARIANT_GROUP_NAME = "Variant Group Name";
    private static final String VARIANT_GROUP_VALUE = "Variant Group Value";
    private static final String PRICE_TYPE = "Price Type";
    private static final String VARIANT_PRICE = "Variant Price";

    private static final List<String> REQUIRED_HEADERS = List.of(PRODUCT_NAME, VARIANT_GROUP_NAME, VARIANT_GROUP_VALUE, PRICE_TYPE, VARIANT_PRICE);

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    private FmVariantExcelReader() {
    }

    /**
     * Reads the first sheet of the uploaded Excel file.
     *
     * @param file uploaded Excel file
     * @return parsed Excel rows
     */
    public static List<FmVariantBulkUploadRowDto> read(MultipartFile file) {

        validateFile(file);

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {

                throw new FileProcessingException("Excel file does not contain any sheet.");
            }

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() <= 1) {

                throw new FileProcessingException("Excel file does not contain any data rows.");
            }

            /*
             * ----------------------------------------------------
             * Read and validate headers.
             * Header order does not matter.
             * ----------------------------------------------------
             */
            Map<String, Integer> headerIndexes = getHeaderIndexes(sheet);

            List<FmVariantBulkUploadRowDto> rows = new ArrayList<>();

            /*
             * ----------------------------------------------------
             * Read data rows.
             * ----------------------------------------------------
             */
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (isEmptyRow(row)) {
                    continue;
                }

                rows.add(parseRow(row, headerIndexes));
            }

            if (rows.isEmpty()) {

                throw new FileProcessingException("Excel file does not contain any valid data rows.");
            }

            log.info("[VARIANT-BULK] EXCEL_PARSED | file={} | rows={}", file.getOriginalFilename(), rows.size());

            return rows;

        } catch (FileProcessingException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error("[VARIANT-BULK] EXCEL_READ_FAILED | file={}", file.getOriginalFilename(), exception);

            throw new FileProcessingException("Failed to process Excel file: " + exception.getMessage());
        }
    }

    /**
     * Validates uploaded file.
     */
    private static void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new FileProcessingException("Excel file is required.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {

            throw new FileProcessingException("Excel file name is missing.");
        }

        String lowerCaseFileName = fileName.toLowerCase(Locale.ROOT);

        if (!lowerCaseFileName.endsWith(".xlsx") && !lowerCaseFileName.endsWith(".xls")) {

            throw new FileProcessingException("Only Excel files (.xlsx or .xls) are supported.");
        }
    }

    /**
     * Reads the header row and creates:
     * <p>
     * normalized header -> Excel column index
     * <p>
     * Example:
     * <p>
     * product name -> 0
     * variant group name -> 1
     * variant group value -> 2
     * price type -> 3
     * variant price -> 4
     */
    private static Map<String, Integer> getHeaderIndexes(Sheet sheet) {

        Row headerRow = sheet.getRow(0);

        if (headerRow == null) {

            throw new FileProcessingException("Excel header row is missing.");
        }

        Map<String, Integer> headerIndexes = new HashMap<>();

        for (Cell cell : headerRow) {

            String originalHeader = getCellStringValue(cell);

            if (originalHeader.isBlank()) {
                continue;
            }

            String normalizedHeader = normalizeHeader(originalHeader);

            if (headerIndexes.containsKey(normalizedHeader)) {

                throw new FileProcessingException("Duplicate Excel column: " + originalHeader);
            }

            headerIndexes.put(normalizedHeader, cell.getColumnIndex());
        }

        /*
         * Validate all required headers.
         */
        for (String requiredHeader : REQUIRED_HEADERS) {

            String normalizedRequiredHeader = normalizeHeader(requiredHeader);

            if (!headerIndexes.containsKey(normalizedRequiredHeader)) {

                throw new FileProcessingException("Required Excel column is missing: " + requiredHeader);
            }
        }

        return headerIndexes;
    }

    /**
     * Converts one Excel row into DTO.
     */
    private static FmVariantBulkUploadRowDto parseRow(Row row, Map<String, Integer> headerIndexes) {

        int excelRowNumber = row.getRowNum() + 1;

        String productName = getCellStringValue(row.getCell(getColumnIndex(headerIndexes, PRODUCT_NAME)));

        String variantGroupName = getCellStringValue(row.getCell(getColumnIndex(headerIndexes, VARIANT_GROUP_NAME)));

        String variantGroupValue = getCellStringValue(row.getCell(getColumnIndex(headerIndexes, VARIANT_GROUP_VALUE)));

        String priceType = getCellStringValue(row.getCell(getColumnIndex(headerIndexes, PRICE_TYPE)));

        BigDecimal variantPrice = getBigDecimalValue(row.getCell(getColumnIndex(headerIndexes, VARIANT_PRICE)));

        return new FmVariantBulkUploadRowDto(excelRowNumber, productName, variantGroupName, variantGroupValue, priceType, variantPrice);
    }

    /**
     * Gets column index for a required header.
     */
    private static int getColumnIndex(Map<String, Integer> headerIndexes, String headerName) {

        Integer columnIndex = headerIndexes.get(normalizeHeader(headerName));

        if (columnIndex == null) {

            throw new FileProcessingException("Required Excel column is missing: " + headerName);
        }

        return columnIndex;
    }

    /**
     * Converts Excel cell to BigDecimal.
     */
    private static BigDecimal getBigDecimalValue(Cell cell) {

        if (cell == null || cell.getCellType() == CellType.BLANK) {

            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {

            return BigDecimal.valueOf(cell.getNumericCellValue());
        }

        if (cell.getCellType() == CellType.STRING) {

            String value = cell.getStringCellValue().trim();

            if (value.isBlank()) {
                return null;
            }

            try {

                return new BigDecimal(value);

            } catch (NumberFormatException exception) {

                throw new FileProcessingException("Invalid variant price at Excel row " + (cell.getRowIndex() + 1) + ": " + value);
            }
        }

        throw new FileProcessingException("Invalid variant price at Excel row " + (cell.getRowIndex() + 1));
    }

    /**
     * Converts an Excel cell to String.
     */
    private static String getCellStringValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    /**
     * Checks whether the entire Excel row is empty.
     */
    private static boolean isEmptyRow(Row row) {

        if (row == null) {
            return true;
        }

        for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {

            Cell cell = row.getCell(cellIndex);

            if (cell != null && !getCellStringValue(cell).isBlank()) {

                return false;
            }
        }

        return true;
    }

    /**
     * Header normalization.
     * <p>
     * Only:
     * 1. trim
     * 2. multiple spaces -> single space
     * 3. lowercase
     */
    private static String normalizeHeader(String value) {

        if (value == null) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}