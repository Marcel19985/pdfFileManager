package at.ac.fhtw.swen3.swen3teamm.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration //wird beim Starten der Spring Boot App automatisch geladen
public class MessagingConfig {
    public static final String OCR_QUEUE = "ocr.jobs";
    public static final String OCR_RESULTS_QUEUE = "ocr.results";

    // Durable Queue (persistente Nachrichten)
    @Bean //Singleton-Bean im Spring Context: einfach via @Autowired in anderen Klassen verwenden -> es existiert nur eine Instanz in der ganzen Appliaktion
    Queue ocrQueue() {
        return QueueBuilder.durable(OCR_QUEUE).build();
    } //durable: true -> Nachrichten bleiben auch bei Broker-Neustart erhalten

    @Bean
    Queue ocrResultsQueue() {
        return QueueBuilder.durable(OCR_RESULTS_QUEUE).build();
    }

    // RabbitTemplate mit JSON-Konverter + Confirms/Returns für robustes Fehlerhandling
    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter converter) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(converter); //alles was mit convertAndSend() gesendet wird, wird via Jackson zu JSON serialisiert

        // Returns aktivieren (unroutable messages)
        tpl.setMandatory(true);
        tpl.setReturnsCallback(ret ->
                org.slf4j.LoggerFactory.getLogger(MessagingConfig.class).warn(
                        "AMQP return: code={} text={} exchange={} routingKey={}",
                        ret.getReplyCode(), ret.getReplyText(), ret.getExchange(), ret.getRoutingKey()
                )
        );

        // Publisher Confirms (Broker-NACKs sichtbar machen)
        tpl.setConfirmCallback((corr, ack, cause) -> {
            if (!ack) {
                org.slf4j.LoggerFactory.getLogger(MessagingConfig.class).error(
                        "AMQP NACK corrId={} cause={}",
                        corr != null ? corr.getId() : null, cause
                );
            }
        });

        return tpl;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory cf, Jackson2JsonMessageConverter converter) {
        var f = new org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(converter);
        f.setPrefetchCount(1); // analog zum Worker
        return f;
    }
}