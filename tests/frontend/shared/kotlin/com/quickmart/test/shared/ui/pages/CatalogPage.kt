package com.quickmart.test.shared.ui.pages

import com.microsoft.playwright.Page
import com.quickmart.test.shared.ui.constants.UiRoutes
import com.quickmart.test.shared.ui.helpers.AllureHelper
import org.assertj.core.api.Assertions.assertThat

class CatalogPage(
    private val page: Page,
) {
    fun expectOpened() {
        AllureHelper.step("Подтверждение открытия страницы каталога") {
            assertThat(page.url()).endsWith("/")
        }
    }

    fun openOrders() {
        AllureHelper.step("Открытие страницы заказов") {
            page.navigate(UiRoutes.ORDERS)
        }
    }
}

