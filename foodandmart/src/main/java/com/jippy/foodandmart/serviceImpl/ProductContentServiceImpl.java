package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.ExcelProductRowDto;
import com.jippy.foodandmart.dto.MissingProductReportDto;
import com.jippy.foodandmart.dto.ProductContentDto;
import com.jippy.foodandmart.dto.SchedulerSummaryDto;
import com.jippy.foodandmart.entity.FmProduct;
import com.jippy.foodandmart.exception.ProductContentException;
import com.jippy.foodandmart.repository.FmProductRepository;
import com.jippy.foodandmart.service.CsvReportService;
import com.jippy.foodandmart.service.EmailService;
import com.jippy.foodandmart.service.ProductContentService;
import com.jippy.foodandmart.service.S3Service;
import com.jippy.foodandmart.util.ExcelReaderUtil;
import com.jippy.foodandmart.util.ProductNameMatcher;
import com.jippy.foodandmart.util.ProductNameNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductContentServiceImpl implements ProductContentService {

    private static final Random RANDOM = new Random();
    private static final int PAGE_SIZE = 1000;
    private final FmProductRepository productRepository;
    private final S3Service s3Service;
    private final EmailService emailService;
    private final CsvReportService csvReportService;
    private final ExcelReaderUtil excelReaderUtil;

    @Override
    public void processProductContent() {

        long startTime = System.currentTimeMillis();

        log.info("Product Content Scheduler Started");
        int pageNumber = 0;
        int totalProducts = 0;
        int updatedProducts = 0;
        int failedProducts = 0;

        List<MissingProductReportDto> missingProducts = new ArrayList<>();

        try (InputStream inputStream = downloadExcel()) {

            List<ExcelProductRowDto> excelRows = readExcel(inputStream);

            if (excelRows.isEmpty()) {

                log.warn("Product Content Excel is empty. Scheduler execution stopped.");

                return;
            }

            Map<String, List<ProductContentDto>> productMap = buildProductMap(excelRows);

            Page<FmProduct> page;

            do {

                page = productRepository.findByIsImageDescUpdatedFalse(PageRequest.of(pageNumber, PAGE_SIZE));

                if (page.isEmpty()) {

                    log.info("No more pending products found.");

                    break;
                }

                log.info("Processing Page {}/{} | Records={}", page.getNumber() + 1, page.getTotalPages(), page.getNumberOfElements());

                /*
                 * updateProduct s(...) will be changed in the next step.
                 * It should return SchedulerSummaryDto for this page.
                 */
                SchedulerSummaryDto pageSummary = updateProducts(page.getContent(), productMap, missingProducts);

                totalProducts += pageSummary.getTotalProducts();
                updatedProducts += pageSummary.getUpdatedProducts();
                failedProducts += pageSummary.getFailedProducts();
                log.info("Page {} completed. Processed={}, Updated={}, Missing={}, Failed={}", pageNumber + 1, pageSummary.getTotalProducts(), pageSummary.getUpdatedProducts(), pageSummary.getMissingProducts(), pageSummary.getFailedProducts());

                pageNumber++;

            } while (page.hasNext());

            SchedulerSummaryDto schedulerSummary = SchedulerSummaryDto.builder().totalProducts(totalProducts).updatedProducts(updatedProducts).missingProducts(missingProducts.size()).failedProducts(failedProducts).totalPages(pageNumber).executionTimeInMillis(System.currentTimeMillis() - startTime).build();

            File csvFile = csvReportService.generateMissingProductsReport(missingProducts);

            emailService.sendMissingProductsEmail(schedulerSummary, csvFile);

            log.info("Scheduler completed successfully. TotalProducts={}, Updated={}, Missing={}, Failed={}, Pages={}", schedulerSummary.getTotalProducts(), schedulerSummary.getUpdatedProducts(), schedulerSummary.getMissingProducts(), schedulerSummary.getFailedProducts(), schedulerSummary.getTotalPages());

        } catch (Exception ex) {

            log.error("Product Content Scheduler failed.", ex);

            throw new ProductContentException("Product Content Scheduler execution failed.", ex);

        } finally {

            log.info("Execution Time : {} ms", System.currentTimeMillis() - startTime);

            log.info("==========================================================");
            log.info("Product Content Scheduler Completed");
            log.info("==========================================================");
        }
    }


    private InputStream downloadExcel() {

        log.info("Downloading product content Excel from S3.");

        InputStream inputStream = s3Service.downloadProductContentExcel();

        if (inputStream == null) {

            log.error("Downloaded InputStream is null.");

            throw new ProductContentException("Product content Excel file not found in S3.");
        }

        log.info("Product content Excel downloaded successfully.");

        return inputStream;
    }

    private List<ExcelProductRowDto> readExcel(InputStream inputStream) {

        if (inputStream == null) {

            log.error("InputStream is null. Unable to read product content Excel.");

            throw new ProductContentException("Product content Excel InputStream cannot be null.");
        }

        log.info("Reading product content Excel.");

        List<ExcelProductRowDto> rows = excelReaderUtil.readProductContentExcel(inputStream);

        if (rows.isEmpty()) {

            log.warn("No product records found in Excel.");

        } else {

            log.info("Successfully read {} product records from Excel.", rows.size());
        }

        return rows;
    }

    private Map<String, List<ProductContentDto>> buildProductMap(List<ExcelProductRowDto> excelRows) {

        if (excelRows == null) {

            log.error("Excel rows list is null.");

            throw new ProductContentException("Excel rows cannot be null.");
        }

        log.info("Building product lookup map from {} Excel rows.", excelRows.size());

        Map<String, List<ProductContentDto>> productMap = new HashMap<>();

        int skippedRows = 0;

        for (ExcelProductRowDto row : excelRows) {

            if (row == null || row.getProductName() == null || row.getProductName().isBlank()) {

                skippedRows++;

                log.debug("Skipping Excel row because product name is empty.");

                continue;
            }

            String normalizedName = ProductNameNormalizer.normalize(row.getProductName());

            ProductContentDto content = new ProductContentDto(row.getDescription(), row.getImageUrl());

            productMap.computeIfAbsent(normalizedName, key -> new ArrayList<>()).add(content);
        }

        if (productMap.isEmpty()) {

            log.error("No valid product records found after parsing Excel.");

            throw new ProductContentException("No valid product data found in Excel.");
        }

        log.info("Product lookup map created successfully.");
        log.info("Unique Products : {}", productMap.size());
        log.info("Skipped Rows    : {}", skippedRows);

        return productMap;
    }

    @Transactional
    private SchedulerSummaryDto updateProducts(List<FmProduct> products, Map<String, List<ProductContentDto>> productMap, List<MissingProductReportDto> missingProducts) {

        if (products == null || productMap == null || missingProducts == null) {
            throw new ProductContentException("Invalid input received for product update.");
        }

        log.info(
                "Started processing {} products for image and description update.",
                products.size());

        List<FmProduct> updatedProducts = new ArrayList<>();

        int updatedCount = 0;
        int failedCount = 0;

        int serialNo = missingProducts.size() + 1;

        for (FmProduct product : products) {

            try {

                if (product == null || product.getProductName() == null || product.getProductName().isBlank()) {

                    failedCount++;

                    log.warn(
                            "Skipping invalid product. ProductId={}, ProductName={}",
                            product != null ? product.getProductId() : null,
                            product != null ? product.getProductName() : null);

                    continue;
                }

                String normalizedName = ProductNameNormalizer.normalize(product.getProductName());

                String matchedName = ProductNameMatcher.findBestMatch(normalizedName, productMap);

                if (matchedName == null) {
                    log.warn(
                            "No matching Excel record found. ProductId={}, ProductName={}",
                            product.getProductId(),
                            product.getProductName());

                    missingProducts.add(MissingProductReportDto.builder().serialNo(serialNo++).productId(product.getProductId()).productName(product.getProductName()).reason("Product not found in Excel").build());

                    continue;
                }

                List<ProductContentDto> contents = productMap.get(matchedName);

                if (contents == null || contents.isEmpty()) {
                    log.warn(
                            "Missing content in Excel. ProductId={}, ProductName={}",
                            product.getProductId(),
                            product.getProductName());

                    missingProducts.add(MissingProductReportDto.builder().serialNo(serialNo++).productId(product.getProductId()).productName(product.getProductName()).reason("Description/Image not available").build());

                    continue;
                }

                ProductContentDto dto = contents.get(RANDOM.nextInt(contents.size()));

                if (dto.getDescription() == null || dto.getDescription().isBlank()) {
                    log.warn(
                            "Description missing in Excel. ProductId={}, ProductName={}",
                            product.getProductId(),
                            product.getProductName());

                    missingProducts.add(MissingProductReportDto.builder().serialNo(serialNo++).productId(product.getProductId()).productName(product.getProductName()).reason("Description Missing").build());

                    continue;
                }

                if (dto.getImageUrl() == null || dto.getImageUrl().isBlank()) {
                    log.warn(
                            "Image URL missing in Excel. ProductId={}, ProductName={}",
                            product.getProductId(),
                            product.getProductName());

                    missingProducts.add(MissingProductReportDto.builder().serialNo(serialNo++).productId(product.getProductId()).productName(product.getProductName()).reason("Image URL Missing").build());

                    continue;
                }

                updateProductContent(product, contents);

                updatedProducts.add(product);

                updatedCount++;

            } catch (Exception ex) {

                failedCount++;

                log.error("Failed processing ProductId={}, ProductName={}", product.getProductId(), product.getProductName(), ex);
            }
        }

        if (!updatedProducts.isEmpty()) {

            productRepository.saveAllAndFlush(updatedProducts);

            log.info(
                    "Successfully persisted {} updated products.",
                    updatedProducts.size());        }
        log.info("Page Processing Summary");
        log.info("Processed : {}", products.size());
        log.info("Updated   : {}", updatedCount);
        log.info("Missing   : {}", missingProducts.size());
        log.info("Failed    : {}", failedCount);

        return SchedulerSummaryDto.builder().totalProducts(products.size()).updatedProducts(updatedCount).missingProducts(missingProducts.size()).failedProducts(failedCount).build();
    }

    private void updateProductContent(FmProduct product, List<ProductContentDto> contents) {

        Objects.requireNonNull(product, "Product cannot be null.");
        Objects.requireNonNull(contents, "Product contents cannot be null.");

        if (contents.isEmpty()) {

            log.error("No product content available for ProductId={}", product.getProductId());

            throw new ProductContentException("No product content available for product: " + product.getProductName());
        }

        ProductContentDto selectedContent = contents.get(RANDOM.nextInt(contents.size()));

        if (selectedContent == null) {

            log.error("Randomly selected product content is null. ProductId={}", product.getProductId());

            throw new ProductContentException("Invalid product content found for product: " + product.getProductName());
        }

        product.setDescription(selectedContent.getDescription());
        product.setImageLink(selectedContent.getImageUrl());
        product.setIsImageDescUpdated(Boolean.TRUE);

        log.debug("Updated product content successfully. ProductId={}, ProductName={}", product.getProductId(), product.getProductName());
    }
}
