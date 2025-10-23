package at.ac.fhtw.swen3.swen3teamm.service;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThatCode;

class OcrResultListenerTest {

    @Test
    void handle_acceptsValidDto_withoutException() {
        OcrResultListener listener = new OcrResultListener();
        OcrResultListener.OcrResultDto dto =
                new OcrResultListener.OcrResultDto("abc-123", "DONE", "hello…", null, Instant.now());

        assertThatCode(() -> listener.handle(dto)).doesNotThrowAnyException();
    }
}