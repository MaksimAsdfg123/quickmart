package com.quickmart.test.support.util

import io.qameta.allure.Allure

inline fun <T> allureStep(
    name: String,
    crossinline block: () -> T,
): T = Allure.step(name, Allure.ThrowableRunnable<T> { block() })

