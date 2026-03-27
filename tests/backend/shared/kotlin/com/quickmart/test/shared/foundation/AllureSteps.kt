package com.quickmart.test.shared.foundation

import io.qameta.allure.Allure

inline fun <T> allureStep(
    name: String,
    crossinline block: () -> T,
): T = Allure.step(name, Allure.ThrowableRunnable<T> { block() })

