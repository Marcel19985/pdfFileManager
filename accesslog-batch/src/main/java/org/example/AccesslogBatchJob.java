package org.example;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccesslogBatchJob {

    private final AccessLogXmlParser parser;
    private final AccessStatService statService;

    @Value("${accesslog.input-dir}")
    private Path inputDir;

    @Value("${accesslog.archive-dir}")
    private Path archiveDir;

    @Value("${accesslog.error-dir}")
    private Path errorDir;

    @Value("${accesslog.file-pattern}")
    private String filePattern;

    @Scheduled(cron = "${accesslog.cron}")
    public void runDailyJob() {
        log.info("Starting accesslog batch job...");

        try {
            Files.createDirectories(inputDir);
            Files.createDirectories(archiveDir);
            Files.createDirectories(errorDir);
        } catch (IOException e) {
            log.error("Failed to ensure batch directories exist", e);
            return;
        }

        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(inputDir, filePattern)) {

            for (Path file : stream) {
                processFile(file);
            }
        } catch (IOException e) {
            log.error("Error while scanning input directory {}", inputDir, e);
        }

        log.info("Accesslog batch job finished.");
    }

    private void processFile(Path file) {
        log.info("Processing accesslog file {}", file.getFileName());
        try {
            AccessLogFile logFile = parser.parse(file);
            statService.applyAccessLog(logFile);

            // Erfolgreich → nach archive verschieben
            Path target = archiveDir.resolve(file.getFileName());
            Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archived file to {}", target);

        } catch (Exception e) {
            log.error("Error processing file {}: {}", file, e.getMessage(), e);
            try {
                Path target = errorDir.resolve(file.getFileName());
                Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
                log.warn("Moved file to error folder: {}", target);
            } catch (IOException ioEx) {
                log.error("Failed to move file {} to error folder", file, ioEx);
            }
        }
    }
}

