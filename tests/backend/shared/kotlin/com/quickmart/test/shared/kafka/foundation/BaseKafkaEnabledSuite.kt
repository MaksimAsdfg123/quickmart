package com.quickmart.test.shared.kafka.foundation

import com.quickmart.QuickmartApplication
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [QuickmartApplication::class])
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = ["quickmart.order-events"],
    brokerProperties = ["listeners=PLAINTEXT://localhost:0", "port=0"],
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
abstract class BaseKafkaEnabledSuite : BaseKafkaComponentTest()
