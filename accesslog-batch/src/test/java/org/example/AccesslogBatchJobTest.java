package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccesslogBatchJobTest {

    @TempDir
    Path tempDir;

    @Test
    void runDailyJob_success_movesFileToArchive_andCallsService() throws Exception {
        Path input = tempDir.resolve("in");
        Path archive = tempDir.resolve("archive");
        Path error = tempDir.resolve("error");
        Files.createDirectories(input);

        Path file = input.resolve("accesslog-1.xml");
        Files.writeString(file, "<access-log date=\"2025-12-15\" sourceSystem=\"x\"/>");

        AccessLogXmlParser parser = mock(AccessLogXmlParser.class);
        AccessStatService statService = mock(AccessStatService.class);

        when(parser.parse(any(Path.class))).thenReturn(new AccessLogFile());

        AccesslogBatchJob job = new AccesslogBatchJob(parser, statService);
        ReflectionTestUtils.setField(job, "inputDir", input);
        ReflectionTestUtils.setField(job, "archiveDir", archive);
        ReflectionTestUtils.setField(job, "errorDir", error);
        ReflectionTestUtils.setField(job, "filePattern", "*.xml");

        job.runDailyJob();

        assertTrue(Files.exists(archive.resolve("accesslog-1.xml")));
        assertFalse(Files.exists(file));

        verify(parser).parse(any(Path.class));
        verify(statService).applyAccessLog(any(AccessLogFile.class));
    }

    @Test
    void runDailyJob_whenParserThrows_movesFileToError_andDoesNotCallService() throws Exception {
        Path input = tempDir.resolve("in");
        Path archive = tempDir.resolve("archive");
        Path error = tempDir.resolve("error");
        Files.createDirectories(input);

        Path file = input.resolve("accesslog-2.xml");
        Files.writeString(file, "broken");

        AccessLogXmlParser parser = mock(AccessLogXmlParser.class);
        AccessStatService statService = mock(AccessStatService.class);

        when(parser.parse(any(Path.class))).thenThrow(new RuntimeException("parse failed"));

        AccesslogBatchJob job = new AccesslogBatchJob(parser, statService);
        ReflectionTestUtils.setField(job, "inputDir", input);
        ReflectionTestUtils.setField(job, "archiveDir", archive);
        ReflectionTestUtils.setField(job, "errorDir", error);
        ReflectionTestUtils.setField(job, "filePattern", "*.xml");

        job.runDailyJob();

        assertTrue(Files.exists(error.resolve("accesslog-2.xml")));
        assertFalse(Files.exists(file));

        verify(statService, never()).applyAccessLog(any());
    }

    @Test
    void runDailyJob_noMatchingFiles_doesNothing() throws Exception {
        Path input = tempDir.resolve("in");
        Path archive = tempDir.resolve("archive");
        Path error = tempDir.resolve("error");
        Files.createDirectories(input);

        // kein xml-file
        Files.writeString(input.resolve("ignore.txt"), "x");

        AccessLogXmlParser parser = mock(AccessLogXmlParser.class);
        AccessStatService statService = mock(AccessStatService.class);

        AccesslogBatchJob job = new AccesslogBatchJob(parser, statService);
        ReflectionTestUtils.setField(job, "inputDir", input);
        ReflectionTestUtils.setField(job, "archiveDir", archive);
        ReflectionTestUtils.setField(job, "errorDir", error);
        ReflectionTestUtils.setField(job, "filePattern", "*.xml");

        job.runDailyJob();

        verifyNoInteractions(parser, statService);
    }
}
