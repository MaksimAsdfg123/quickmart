package com.quickmart.test.shared.ui.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.quickmart.test.shared.ui.constants.UiRoutes
import com.quickmart.test.shared.ui.data.NewUserRegistration
import com.quickmart.test.shared.ui.helpers.AllureHelper
import org.assertj.core.api.Assertions.assertThat as assertThatValue

class RegisterPage(
    private val page: Page,
) {
    fun open() {
        AllureHelper.step("Открытие страницы регистрации") {
            page.navigate(UiRoutes.REGISTER)
            assertThat(page.locator("form")).isVisible()
            assertThatValue(page.url()).contains("/register")
        }
    }

    fun fillForm(user: NewUserRegistration) {
        AllureHelper.step("Заполнение формы регистрации") {
            page.locator("#fullName").fill(user.fullName)
            page.locator("#email").fill(user.email)
            page.locator("#password").fill(user.password)
        }
    }

    fun submit() {
        AllureHelper.step("Отправка формы регистрации") {
            page.locator("form button[type='submit']").click()
        }
    }

    fun register(user: NewUserRegistration) {
        fillForm(user)
        submit()
    }

    fun goToLogin() {
        AllureHelper.step("Переход на страницу авторизации из формы регистрации") {
            page.locator("a[href='/login']").click()
        }
    }

    fun expectRegistrationErrorContains(expectedText: String) {
        AllureHelper.step("Проверка отображения ошибки регистрации") {
            assertThat(page.locator(".error")).containsText(expectedText)
        }
    }

    fun expectValidationErrorVisible() {
        AllureHelper.step("Проверка отображения ошибок валидации формы регистрации") {
            assertThat(page.locator(".error-text").first()).isVisible()
        }
    }
}

