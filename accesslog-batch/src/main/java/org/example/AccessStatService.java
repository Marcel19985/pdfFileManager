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

    @Transactional //DB Opterationen innerhalb eine Transaktion, bei Fehler -> Rollback
    public void applyAccessLog(AccessLogFile logFile) {
        //date + source für xml:
        LocalDate date = LocalDate.parse(logFile.getDate());
        String source = logFile.getSourceSystem();

        //ID + count für jeden Eintrag:
        for (AccessEntry e : logFile.getEntries()) {
            UUID docId = e.getDocumentId();
            int count = e.getCount();

            DocumentAccessStat stat = repo
                    .findByDocumentIdAndAccessDateAndSourceSystem(docId, date, source) //zuerst nach existierenden Datensatz sucehn
                    .orElseGet(() -> { //neu erstellen, wenn noch nicht existiert
                        DocumentAccessStat s = new DocumentAccessStat();
                        s.setDocumentId(docId);
                        s.setAccessDate(date);
                        s.setSourceSystem(source);
                        s.setAccessCount(0);
                        return s;
                    });

            // Zahl aus dem XML als "Tagesgesamt" speichern:
            stat.setAccessCount(count);

            repo.save(stat);
        }

        log.info("Processed access log for {} entries (date={}, source={})",
                logFile.getEntries().size(), date, source);
    }
}
