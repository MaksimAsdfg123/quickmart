package com.quickmart.test.support.assertion

import com.quickmart.test.support.model.CartResponseModel
import com.quickmart.test.support.util.allureStep
import com.quickmart.test.support.util.toModel
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat

object CartAssertions {
    fun assertEmptyCartReturned(response: Response): CartResponseModel =
        allureStep("Проверка состояния корзины нового пользователя") {
            assertThat(response.statusCode).isEqualTo(200)
            val cart = response.toModel<CartResponseModel>()
            assertThat(cart.id).isNotNull
            assertThat(cart.totalItems).isZero()
            assertThat(cart.items).isEmpty()
            assertThat(cart.subtotal).isNotNull
            cart
        }
}
