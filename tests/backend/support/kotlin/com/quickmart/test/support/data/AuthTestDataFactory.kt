package com.quickmart.test.support.data

import com.quickmart.test.support.model.LoginRequestModel
import com.quickmart.test.support.model.RegisterRequestModel
import com.quickmart.test.support.util.RandomDataUtils

class RegisterRequestBuilder {
    private var email: String = RandomDataUtils.uniqueEmail()
    private var password: String = "password123"
    private var fullName: String = RandomDataUtils.uniqueName()

    fun withEmail(value: String) = apply { email = value }

    fun withPassword(value: String) = apply { password = value }

    fun withFullName(value: String) = apply { fullName = value }

    fun build(): RegisterRequestModel = RegisterRequestModel(email = email, password = password, fullName = fullName)
}

class LoginRequestBuilder {
    private var email: String = "anna@example.com"
    private var password: String = "password"

    fun withEmail(value: String) = apply { email = value }

    fun withPassword(value: String) = apply { password = value }

    fun build(): LoginRequestModel = LoginRequestModel(email = email, password = password)
}

object AuthTestDataFactory {
    fun newCustomerRegistration(): RegisterRequestModel =
        RegisterRequestBuilder()
            .withPassword("password123")
            .build()

    fun registrationWithWhitespaceAndUppercaseEmail(): RegisterRequestModel =
        RegisterRequestBuilder()
            .withEmail(RandomDataUtils.uniqueEmail("qa.auth").uppercase())
            .withFullName("  ${RandomDataUtils.uniqueName("Новый пользователь")}  ")
            .withPassword("password123")
            .build()

    fun duplicatedRegistration(email: String): RegisterRequestModel =
        RegisterRequestBuilder()
            .withEmail(email)
            .withPassword("password123")
            .withFullName("Duplicate Email User")
            .build()

    fun invalidShortPasswordRegistration(): RegisterRequestModel =
        RegisterRequestBuilder()
            .withPassword("123")
            .build()

    fun seededCustomerLogin(email: String = "anna@example.com", password: String = "password"): LoginRequestModel =
        LoginRequestBuilder()
            .withEmail(email)
            .withPassword(password)
            .build()

    fun adminLogin(): LoginRequestModel =
        LoginRequestBuilder()
            .withEmail("admin@quickmart.local")
            .withPassword("password")
            .build()
}
