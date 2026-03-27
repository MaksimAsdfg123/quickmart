package com.quickmart.test.shared.ui.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.quickmart.test.shared.ui.constants.UiRoutes
import com.quickmart.test.shared.ui.helpers.AllureHelper
import org.assertj.core.api.Assertions.assertThat as assertThatValue

class LoginPage(
    private val page: Page,
) {
    fun open() {
        AllureHelper.step("Открытие страницы авторизации") {
            page.navigate(UiRoutes.LOGIN)
            assertThat(page.locator("form")).isVisible()
            assertThatValue(page.url()).contains("/login")
        }
    }

    fun typeEmail(email: String) {
        AllureHelper.step("Ввод email на странице авторизации") {
            page.locator("#email").fill(email)
        }
    }

    fun typePassword(password: String) {
        AllureHelper.step("Ввод пароля на странице авторизации") {
            page.locator("#password").fill(password)
        }
    }

    fun submit() {
        AllureHelper.step("Отправка формы авторизации") {
            page.locator("form button[type='submit']").click()
        }
    }

    fun login(email: String, password: String) {
        typeEmail(email)
        typePassword(password)
        submit()
    }

    fun goToRegister() {
        AllureHelper.step("Переход на страницу регистрации из формы авторизации") {
            page.locator("a[href='/register']").click()
        }
    }

    fun expectAuthErrorContains(expectedText: String) {
        AllureHelper.step("Проверка отображения ошибки авторизации") {
            assertThat(page.locator(".error")).containsText(expectedText)
        }
    }

    fun expectValidationErrorVisible() {
        AllureHelper.step("Проверка отображения ошибок валидации формы авторизации") {
            assertThat(page.locator(".error-text").first()).isVisible()
        }
    }
}

