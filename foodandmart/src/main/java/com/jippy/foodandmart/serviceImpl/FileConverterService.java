package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.exception.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Spring service that converts supported upload file types (CSV, XLS, XLSX)
 * into a CSV {@link InputStream} for downstream processing by
 * {@link FmMasterProductService}.
 *
 * <p>Why convert everything to CSV: the master product parsing logic only
 * knows how to read CSV. By converting Excel files to CSV here, we keep
 * the parser simple and avoid duplicating Excel-reading logic in every
 * service that accepts file uploads.</p>
 *
 * <p>Why NOT support PDF/image (OCR): those would require heavyweight
 * dependencies (Tika, PDFBox, Tess4j) that are not in the pom.xml and
 * would significantly increase startup time and JAR size.</p>
 */
@Slf4j
@Service
public class FileConverterService {

    /**
     * Converts a {@link MultipartFile} to a CSV {@link InputStream}.
     *
     * <p>Why fall through to Excel conversion on unknown extensions:
     * some upload clients strip file extensions or send "application/octet-stream".
     * Trying Excel conversion first on unknown types is more lenient than
     * refusing the file outright.</p>
     *
     * @param file the uploaded file (CSV, XLS, or XLSX)
     * @return a CSV {@link InputStream} usable by the calling service
     * @throws FileProcessingException if conversion fails
     */
    public InputStream convertToCsv(MultipartFile file) {
        String name = (file.getOriginalFilename() != null)
                ? file.getOriginalFilename().toLowerCase() : "";
        try {
            if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
                return convertExcelToCsv(file);
            } else if (name.endsWith(".csv")) {
                // CSV needs no conversion — return the stream directly
                return file.getInputStream();
            } else {
                // Unknown extension: try Excel first, fall back to treating as CSV
                try {
                    return convertExcelToCsv(file);
                } catch (Exception ex) {
                    log.warn("[MASTER] Excel parse failed, treating as CSV: {}", ex.getMessage());
                    return file.getInputStream();
                }
            }
        } catch (Exception e) {
            throw new FileProcessingException("Failed to convert file: " + e.getMessage(), e);
        }
    }

    /**
     * Converts pre-read file bytes to a CSV {@link InputStream}.
     *
     * <p>Why accept bytes instead of a {@link MultipartFile}: reading
     * {@link MultipartFile#getInputStream()} consumes the stream. When the
     * calling service needs to use the same file bytes for both type detection
     * and content parsing, it should read the bytes once and pass them here.
     * This avoids the "stream already consumed" error.</p>
     *
     * @param fileBytes        the raw file bytes (already read from the MultipartFile)
     * @param originalFilename the original file name used for extension detection
     * @return a CSV {@link InputStream} derived from the bytes
     * @throws FileProcessingException if conversion fails
     */
    public InputStream convertToCsvFromBytes(byte[] fileBytes, String originalFilename) {
        String name = originalFilename != null ? originalFilename.toLowerCase() : "";
        try {
            if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
                return convertExcelBytesToCsv(fileBytes);
            } else {
                // CSV or unknown type — return bytes directly as an InputStream
                return new ByteArrayInputStream(fileBytes);
            }
        } catch (Exception e) {
            throw new FileProcessingException("Failed to convert file bytes: " + e.getMessage(), e);
        }
    }

    /**
     * Reads the Excel file from a {@link MultipartFile} and converts it to CSV bytes.
     *
     * <p>Why read all bytes first: {@code file.getInputStream()} can only be
     * called once. Reading to bytes lets us pass the same data to
     * {@link #convertExcelBytesToCsv(byte[])} without re-reading the stream.</p>
     *
     * @param file the Excel MultipartFile
     * @return a CSV InputStream
     * @throws Exception if the file bytes cannot be read or parsed as Excel
     */
    private InputStream convertExcelToCsv(MultipartFile file) throws Exception {
        return convertExcelBytesToCsv(file.getBytes());
    }

    /**
     * Parses Excel bytes using Apache POI and serialises the first sheet to CSV.
     *
     * <p>Why use {@link DataFormatter}: it formats numeric cells (dates, currency,
     * percentages) using the cell's Excel format string, which matches what the
     * user sees in the spreadsheet. Raw {@code cell.getNumericCellValue()} would
     * return unformatted doubles that differ from the displayed value.</p>
     *
     * <p>Why quote every cell value: CSV quoting is the safest way to handle
     * values that contain commas, quotes, or newlines. Any existing double-quote
     * characters are escaped by doubling them (RFC 4180 standard).</p>
     *
     * <p>Why only process the first sheet: upload templates are single-sheet
     * workbooks. Processing additional sheets would include data the user
     * did not intend to upload (e.g. instruction sheets).</p>
     *
     * @param bytes the raw bytes of the Excel file
     * @return a CSV InputStream encoded as UTF-8
     * @throws Exception if the bytes cannot be parsed as a valid Excel workbook
     */
    private InputStream convertExcelBytesToCsv(byte[] bytes) throws Exception {
        StringBuilder csv = new StringBuilder();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    // For NUMERIC cells use raw value to avoid currency/comma formatting issues
                    String value;
                    if (cell.getCellType() == CellType.NUMERIC && !DateUtil.isCellDateFormatted(cell)) {
                        double d = cell.getNumericCellValue();
                        value = (d == Math.floor(d) && !Double.isInfinite(d))
                                ? String.valueOf((long) d)
                                : String.valueOf(d);
                    } else {
                        value = formatter.formatCellValue(cell).trim();
                    }
                    value = value.replace("\"", "\"\"");
                    if (i > 0) line.append(",");
                    line.append("\"").append(value).append("\"");
                }
                csv.append(line).append("\n");
            }
        }
        log.info("[MASTER] Excel → CSV conversion complete");
        return new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.UTF_8));
    }
}
