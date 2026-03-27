package com.quickmart.test.shared.common.util

import java.time.Instant
import java.util.concurrent.ThreadLocalRandom

object RandomDataUtils {
    fun uniqueEmail(prefix: String = "qa.auth"): String {
        val millis = Instant.now().toEpochMilli()
        val randomSuffix = ThreadLocalRandom.current().nextInt(1000, 9999)
        return "$prefix.$millis.$randomSuffix@example.com"
    }

    fun uniqueName(prefix: String = "QA User"): String {
        val randomSuffix = ThreadLocalRandom.current().nextInt(1000, 9999)
        return "$prefix $randomSuffix"
    }
}

