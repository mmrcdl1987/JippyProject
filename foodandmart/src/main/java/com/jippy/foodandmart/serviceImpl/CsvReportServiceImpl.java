package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.MissingProductReportDto;
import com.jippy.foodandmart.exception.ProductContentException;
import com.jippy.foodandmart.service.CsvReportService;
import com.jippy.foodandmart.util.CsvGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class CsvReportServiceImpl implements CsvReportService {

    @Override
    public File generateMissingProductsReport(
            List<MissingProductReportDto> missingProducts) {

        if (missingProducts == null || missingProducts.isEmpty()) {

            log.info("No missing products found. CSV generation skipped.");

            return null;
        }

        try {

            log.info("Generating Missing Products CSV. Record Count={}",
                    missingProducts.size());

            File csvFile =
                    CsvGeneratorUtil.generateMissingProductsCsv(missingProducts);

            log.info("Missing Products CSV generated successfully. File={}",
                    csvFile.getAbsolutePath());

            return csvFile;

        } catch (Exception ex) {

            log.error("Failed to generate Missing Products CSV.", ex);

            throw new ProductContentException(
                    "Failed to generate Missing Products CSV.",
                    ex);
        }
    }
}