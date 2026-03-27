package com.quickmart.test.shared.ui.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.quickmart.test.shared.ui.constants.UiRoutes
import com.quickmart.test.shared.ui.helpers.AllureHelper
import org.assertj.core.api.Assertions.assertThat as assertThatValue

class OrdersPage(
    private val page: Page,
) {
    fun openProtected() {
        AllureHelper.step("Открытие защищенной страницы заказов") {
            page.navigate(UiRoutes.ORDERS)
        }
    }

    fun expectOpened() {
        AllureHelper.step("Подтверждение открытия страницы заказов") {
            assertThatValue(page.url()).contains("/orders")
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING).first()).isVisible()
        }
    }
}

