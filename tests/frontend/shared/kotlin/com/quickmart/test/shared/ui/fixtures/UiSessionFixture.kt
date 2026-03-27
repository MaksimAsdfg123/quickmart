package com.quickmart.test.shared.ui.fixtures

import com.microsoft.playwright.Page
import com.quickmart.test.shared.ui.api.AuthSetupApiClient
import com.quickmart.test.shared.ui.api.RegisterApiPayload
import com.quickmart.test.shared.ui.constants.UiRoutes
import com.quickmart.test.shared.ui.data.NewUserRegistration
import com.quickmart.test.shared.ui.data.UserCredentials
import com.quickmart.test.shared.ui.helpers.AllureHelper
import com.quickmart.test.shared.ui.helpers.JsonHelper

class UiSessionFixture(
    private val page: Page,
    private val authSetupApiClient: AuthSetupApiClient,
) {
    fun openAnonymousSession() {
        AllureHelper.step("Подготовка анонимной сессии") {
            page.context().clearCookies()
            page.navigate(UiRoutes.LOGIN)
            page.evaluate("() => localStorage.clear()")
            page.reload()
        }
    }

    fun openAuthenticatedSession(credentials: UserCredentials, targetPath: String = UiRoutes.CATALOG) {
        AllureHelper.step("Подготовка авторизованной сессии через API") {
            val auth = authSetupApiClient.login(credentials.email, credentials.password)
            val persistedState =
                JsonHelper.mapper.writeValueAsString(
                    mapOf(
                        "state" to
                            mapOf(
                                "token" to auth.token,
                                "user" to auth.user,
                            ),
                        "version" to 0,
                    ),
                )

            page.navigate(UiRoutes.LOGIN)
            page.evaluate(
                "(payload) => localStorage.setItem(payload.key, payload.value)",
                mapOf("key" to "quickmart-auth", "value" to persistedState),
            )
            page.navigate(targetPath)
        }
    }

    fun ensureRegistered(user: NewUserRegistration) {
        authSetupApiClient.ensureUserExists(
            RegisterApiPayload(
                email = user.email,
                password = user.password,
                fullName = user.fullName,
            ),
        )
    }
}

