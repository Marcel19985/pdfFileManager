package at.ac.fhtw.swen3.swen3teamm.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class MessagingConfigTest {

    private MessagingConfig config;

    @BeforeEach
    void setUp() {
        config = new MessagingConfig();
    }

    @Test
    void ocrQueue_shouldBeDurableAndHaveCorrectName() {
        var queue = config.ocrQueue();

        assertThat(queue).isNotNull();
        assertThat(queue.getName()).isEqualTo(MessagingConfig.OCR_QUEUE);
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    void rabbitTemplate_shouldBeCreatedWithoutExceptions() {
        // Arrange
        ConnectionFactory mockFactory = mock(ConnectionFactory.class);
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        // Act & Assert: Bean wird erstellt ohne Exception
        assertDoesNotThrow(() -> {
            RabbitTemplate template = config.rabbitTemplate(mockFactory, converter);
            assertThat(template).isNotNull();
            assertThat(template.getMessageConverter()).isSameAs(converter);
        });
    }
}
