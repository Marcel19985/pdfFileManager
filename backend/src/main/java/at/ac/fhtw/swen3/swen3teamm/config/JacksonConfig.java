package at.ac.fhtw.swen3.swen3teamm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//diese Klasse wurde aus dem sample code übernommen: rabbitmqechoservice von Moodle SWEN 3 Kurs
@Configuration
public class JacksonConfig {
    @Bean ObjectMapper objectMapper() {
        var om = new ObjectMapper();
        om.findAndRegisterModules();
        return om;
    }
    @Bean Jackson2JsonMessageConverter jacksonMsgConverter(ObjectMapper om) {
        return new Jackson2JsonMessageConverter(om);
    }
}
