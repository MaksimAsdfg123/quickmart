package com.quickmart.test.shared.ui.components

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.quickmart.test.shared.ui.helpers.AllureHelper

class TopBarComponent(
    private val page: Page,
) {
    fun clickLogin() {
        AllureHelper.step("Переход к странице авторизации через топбар") {
            page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Вход")).click()
        }
    }

    fun clickRegister() {
        AllureHelper.step("Переход к странице регистрации через топбар") {
            page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Регистрация")).click()
        }
    }

    fun clickLogout() {
        AllureHelper.step("Выход пользователя из аккаунта") {
            page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Выйти")).click()
        }
    }

    fun expectAuthenticatedCustomer() {
        AllureHelper.step("Подтверждение отображения customer-сессии в топбаре") {
            assertThat(page.locator(".session-user")).isVisible()
            assertThat(page.locator("a[href='/orders']")).isVisible()
        }
    }

    fun expectAuthenticatedAdmin() {
        AllureHelper.step("Подтверждение отображения admin-сессии в топбаре") {
            assertThat(page.locator(".session-user")).isVisible()
            assertThat(page.locator("a[href='/admin/products']")).isVisible()
        }
    }

    fun expectAnonymous() {
        AllureHelper.step("Подтверждение анонимного состояния топбара") {
            assertThat(page.locator("a[href='/login']")).isVisible()
            assertThat(page.locator("a[href='/register']")).isVisible()
        }
    }
}

