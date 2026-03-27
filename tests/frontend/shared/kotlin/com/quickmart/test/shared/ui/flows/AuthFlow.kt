package com.quickmart.test.shared.ui.flows

import com.quickmart.test.shared.ui.data.NewUserRegistration
import com.quickmart.test.shared.ui.data.UserCredentials
import com.quickmart.test.shared.ui.helpers.AllureHelper
import com.quickmart.test.shared.ui.pages.LoginPage
import com.quickmart.test.shared.ui.pages.RegisterPage

class AuthFlow(
    private val loginPage: LoginPage,
    private val registerPage: RegisterPage,
) {
    fun loginAs(credentials: UserCredentials) {
        AllureHelper.step("Выполнение сценария входа пользователя") {
            loginPage.open()
            loginPage.login(credentials.email, credentials.password)
        }
    }

    fun loginWithPassword(email: String, password: String) {
        AllureHelper.step("Выполнение сценария входа с заданным паролем") {
            loginPage.open()
            loginPage.login(email, password)
        }
    }

    fun register(user: NewUserRegistration) {
        AllureHelper.step("Выполнение сценария регистрации пользователя") {
            registerPage.open()
            registerPage.register(user)
        }
    }

    fun openRegisterFromLogin() {
        AllureHelper.step("Переход из авторизации в регистрацию") {
            loginPage.open()
            loginPage.goToRegister()
        }
    }

    fun openLoginFromRegister() {
        AllureHelper.step("Переход из регистрации в авторизацию") {
            registerPage.open()
            registerPage.goToLogin()
        }
    }
}

