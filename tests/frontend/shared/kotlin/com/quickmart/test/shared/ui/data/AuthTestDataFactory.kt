package com.quickmart.test.shared.ui.data

import com.quickmart.test.shared.ui.helpers.AllureHelper
import java.time.Instant

object AuthTestDataFactory {
    fun uniqueUser(prefix: String = "ui-auth"): NewUserRegistration =
        AllureHelper.stepWithResult("Формирование уникальных данных пользователя") {
            val suffix = Instant.now().toEpochMilli().toString()
            NewUserRegistration(
                fullName = "Тестовый Пользователь $suffix",
                email = "$prefix-$suffix@example.com",
                password = "password123",
            )
        }
}

