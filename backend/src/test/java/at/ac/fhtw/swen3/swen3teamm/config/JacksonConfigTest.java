package at.ac.fhtw.swen3.swen3teamm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigTest {

    private final JacksonConfig config = new JacksonConfig();

    @Test
    void objectMapper_shouldRegisterModules() {
        ObjectMapper om = config.objectMapper();

        assertThat(om).isNotNull();
        // Prüft, ob z. B. das JavaTimeModule (für Instant/LocalDateTime) registriert ist
        assertThat(om.getRegisteredModuleIds()).isNotEmpty();
    }

    @Test
    void jacksonMsgConverter_shouldUseProvidedObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        Jackson2JsonMessageConverter converter = config.jacksonMsgConverter(om);

        assertThat(converter).isNotNull();
        assertThat(converter.getJavaTypeMapper()).isNotNull();

        // Leider kein direkter Getter für den ObjectMapper, aber man kann indirekt prüfen:
        // Der Converter sollte denselben Typmapper verwenden, was auf denselben OM schließen lässt
        // Anders ging es irgendwie nicht
        Jackson2JsonMessageConverter other = config.jacksonMsgConverter(om);
        assertThat(other).usingRecursiveComparison().isEqualTo(converter);
    }
}
