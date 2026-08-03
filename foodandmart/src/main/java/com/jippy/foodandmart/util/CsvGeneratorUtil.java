package com.jippy.foodandmart.util;

import com.jippy.foodandmart.dto.MissingProductReportDto;
import com.jippy.foodandmart.exception.ProductContentException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@UtilityClass
public class CsvGeneratorUtil {

    private static final String HEADER =
            "S.No,Product Id,Product Name,Reason";

    public File generateMissingProductsCsv(
            List<MissingProductReportDto> missingProducts) {

        if (missingProducts == null || missingProducts.isEmpty()) {

            log.info("No missing products found. CSV generation skipped.");

            return null;
        }

        String fileName =
                "Missing_Products_" + LocalDate.now() + ".csv";

        File csvFile = new File(
                System.getProperty("java.io.tmpdir"),
                fileName);

        log.info("Generating Missing Products CSV : {}",
                csvFile.getAbsolutePath());

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(csvFile))) {

            writer.write(HEADER);
            writer.newLine();

            for (MissingProductReportDto dto : missingProducts) {

                writer.write(buildCsvRow(dto));
                writer.newLine();
            }

            writer.flush();

            log.info(
                    "CSV generated successfully. File={}, Total Records={}",
                    csvFile.getAbsolutePath(),
                    missingProducts.size());

            return csvFile;

        } catch (IOException ex) {

            log.error("Failed to generate Missing Products CSV.", ex);

            throw new ProductContentException(
                    "Unable to generate Missing Products CSV.",
                    ex);
        }
    }

    private String buildCsvRow(
            MissingProductReportDto dto) {

        return String.format(
                "%d,%d,\"%s\",\"%s\"",
                dto.getSerialNo(),
                dto.getProductId(),
                escape(dto.getProductName()),
                escape(dto.getReason())
        );
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\"", "\"\"");
    }

}