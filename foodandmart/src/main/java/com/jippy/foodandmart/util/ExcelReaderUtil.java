package com.jippy.foodandmart.util;

import com.jippy.foodandmart.dto.ExcelProductRowDto;
import com.jippy.foodandmart.exception.ProductContentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ExcelReaderUtil {

    /**
     * Reads Product Content Excel and converts it into DTOs.
     *
     * Expected Columns:
     * 0 - Product Name
     * 1 - Description
     * 2 - Image URL
     *
     * @param inputStream Product Content Excel InputStream
     * @return List of ExcelProductRowDto
     */
    public List<ExcelProductRowDto> readProductContentExcel(
            InputStream inputStream) {

        if (inputStream == null) {

            log.error("Unable to read Product Content Excel. InputStream is null.");

            throw new ProductContentException(
                    "Product Content Excel InputStream cannot be null.");
        }

        log.info("Started reading Product Content Excel.");

        List<ExcelProductRowDto> excelRows = new ArrayList<>();

        int skippedRows = 0;

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {

                log.error("Product Content Excel does not contain any worksheets.");

                throw new ProductContentException(
                        "No worksheet found in Product Content Excel.");
            }

            Sheet sheet = workbook.getSheetAt(0);

            log.info("Processing worksheet '{}'. Total Rows={}",
                    sheet.getSheetName(),
                    sheet.getLastRowNum());

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null) {

                    skippedRows++;

                    log.debug("Skipping empty row at index {}.", rowIndex);

                    continue;
                }

                String productName = getCellValue(row.getCell(0));
                String description = getCellValue(row.getCell(1));
                String imageUrl = getCellValue(row.getCell(2));

                if (productName.isBlank()) {

                    skippedRows++;

                    log.debug("Skipping row {} because product name is blank.", rowIndex);

                    continue;
                }

                excelRows.add(new ExcelProductRowDto(
                        productName,
                        description,
                        imageUrl));
            }

            log.info(
                    "Successfully parsed Product Content Excel. Valid Rows={}, Skipped Rows={}",
                    excelRows.size(),
                    skippedRows);

            return excelRows;

        } catch (ProductContentException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("Failed to parse Product Content Excel.", ex);

            throw new ProductContentException(
                    "Failed to read Product Content Excel.",
                    ex);
        }
    }

    /**
     * Returns the string value of an Excel cell.
     *
     * @param cell Excel cell
     * @return Cell value as String
     */
    private String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {

            case STRING -> cell.getStringCellValue().trim();

            case NUMERIC -> String.valueOf(cell.getNumericCellValue());

            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());

            case FORMULA -> cell.getCellFormula();

            default -> "";
        };
    }
}