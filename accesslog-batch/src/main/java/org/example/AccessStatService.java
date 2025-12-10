package org.example;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessStatService {

    private final DocumentAccessStatRepository repo;
    private static final Logger log = LoggerFactory.getLogger(AccessStatService.class);

    @Transactional
    public void applyAccessLog(AccessLogFile logFile) {
        LocalDate date = LocalDate.parse(logFile.getDate());
        String source = logFile.getSourceSystem();

        for (AccessEntry e : logFile.getEntries()) {
            UUID docId = e.getDocumentId();
            int count = e.getCount();

            DocumentAccessStat stat = repo
                    .findByDocumentIdAndAccessDateAndSourceSystem(docId, date, source)
                    .orElseGet(() -> {
                        DocumentAccessStat s = new DocumentAccessStat();
                        s.setDocumentId(docId);
                        s.setAccessDate(date);
                        s.setSourceSystem(source);
                        s.setAccessCount(0);
                        return s;
                    });

            // Wenn du die Zahl aus dem XML als "Tagesgesamt" speichern willst:
            stat.setAccessCount(count);

            // Wenn du stattdessen addieren möchtest:
            // stat.setAccessCount(stat.getAccessCount() + count);

            repo.save(stat);
        }

        log.info("Processed access log for {} entries (date={}, source={})",
                logFile.getEntries().size(), date, source);
    }
}
