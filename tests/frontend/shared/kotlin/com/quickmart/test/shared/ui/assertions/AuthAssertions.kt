package com.quickmart.test.shared.ui.assertions

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.quickmart.test.shared.ui.components.TopBarComponent
import com.quickmart.test.shared.ui.helpers.AllureHelper
import com.quickmart.test.shared.ui.pages.LoginPage
import com.quickmart.test.shared.ui.pages.RegisterPage
import java.util.regex.Pattern

class AuthAssertions(
    private val page: Page,
    private val topBarComponent: TopBarComponent,
    private val loginPage: LoginPage,
    private val registerPage: RegisterPage,
) {
    fun assertLoggedInAsCustomer() {
        topBarComponent.expectAuthenticatedCustomer()
    }

    fun assertLoggedInAsAdmin() {
        topBarComponent.expectAuthenticatedAdmin()
    }

    fun assertAnonymousTopbar() {
        topBarComponent.expectAnonymous()
    }

    fun assertRedirectedToLogin() {
        AllureHelper.step("Проверка редиректа на страницу авторизации") {
            assertThat(page).hasURL(Pattern.compile(".*/login.*"))
            assertThat(page.locator("form")).isVisible()
        }
    }

    fun assertOrdersPageOpened() {
        AllureHelper.step("Проверка успешного доступа к странице заказов") {
            assertThat(page).hasURL(Pattern.compile(".*/orders$"))
        }
    }

    fun assertLoginError(message: String) {
        loginPage.expectAuthErrorContains(message)
    }

    fun assertLoginValidationErrorShown() {
        loginPage.expectValidationErrorVisible()
    }

    fun assertRegisterValidationErrorShown() {
        registerPage.expectValidationErrorVisible()
    }

    fun assertRegisterError(message: String) {
        registerPage.expectRegistrationErrorContains(message)
    }

    fun assertOnLoginPage() {
        AllureHelper.step("Проверка, что отображается форма авторизации") {
            assertThat(page).hasURL(Pattern.compile(".*/login.*"))
            assertThat(page.locator("form")).isVisible()
        }
    }

    fun assertOnRegisterPage() {
        AllureHelper.step("Проверка, что отображается форма регистрации") {
            assertThat(page).hasURL(Pattern.compile(".*/register.*"))
            assertThat(page.locator("form")).isVisible()
        }
    }
}

