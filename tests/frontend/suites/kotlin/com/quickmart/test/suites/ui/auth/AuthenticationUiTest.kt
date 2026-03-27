package com.quickmart.test.suites.ui.auth

import com.quickmart.test.shared.ui.api.AuthSetupApiClient
import com.quickmart.test.shared.ui.assertions.AuthAssertions
import com.quickmart.test.shared.ui.base.BaseUiSuite
import com.quickmart.test.shared.ui.components.TopBarComponent
import com.quickmart.test.shared.ui.config.UiTestEnvironment
import com.quickmart.test.shared.ui.data.AuthTestDataFactory
import com.quickmart.test.shared.ui.data.NewUserRegistration
import com.quickmart.test.shared.ui.data.UserCredentials
import com.quickmart.test.shared.ui.fixtures.UiSessionFixture
import com.quickmart.test.shared.ui.flows.AuthFlow
import com.quickmart.test.shared.ui.pages.LoginPage
import com.quickmart.test.shared.ui.pages.OrdersPage
import com.quickmart.test.shared.ui.pages.RegisterPage
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("ui")
@Epic("UI Automation")
@Feature("Аутентификация")
@Owner("qa-automation")
class AuthenticationUiTest : BaseUiSuite() {
    private lateinit var loginPage: LoginPage
    private lateinit var registerPage: RegisterPage
    private lateinit var ordersPage: OrdersPage
    private lateinit var topBar: TopBarComponent
    private lateinit var authFlow: AuthFlow
    private lateinit var authAssertions: AuthAssertions
    private lateinit var sessionFixture: UiSessionFixture

    private val customerCredentials =
        UserCredentials(
            email = UiTestEnvironment.customerEmail,
            password = UiTestEnvironment.customerPassword,
        )

    private val adminCredentials =
        UserCredentials(
            email = UiTestEnvironment.adminEmail,
            password = UiTestEnvironment.adminPassword,
        )

    @BeforeEach
    fun initTestLayers() {
        val authSetupApiClient = AuthSetupApiClient()

        loginPage = LoginPage(page)
        registerPage = RegisterPage(page)
        ordersPage = OrdersPage(page)
        topBar = TopBarComponent(page)

        authFlow = AuthFlow(loginPage = loginPage, registerPage = registerPage)
        authAssertions = AuthAssertions(page = page, topBarComponent = topBar, loginPage = loginPage, registerPage = registerPage)
        sessionFixture = UiSessionFixture(page = page, authSetupApiClient = authSetupApiClient)
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("Успешный вход пользователя")
    @DisplayName("Успешная авторизация покупателя")
    fun shouldLoginCustomerSuccessfully() {
        sessionFixture.openAnonymousSession()

        authFlow.loginAs(customerCredentials)

        authAssertions.assertLoggedInAsCustomer()
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Успешный вход администратора")
    @DisplayName("Успешная авторизация администратора")
    fun shouldLoginAdminSuccessfully() {
        sessionFixture.openAnonymousSession()

        authFlow.loginAs(adminCredentials)

        authAssertions.assertLoggedInAsAdmin()
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Доступ к защищенному маршруту")
    @DisplayName("Редирект на login для анонимного пользователя и возврат на orders после входа")
    fun shouldRedirectAnonymousUserAndReturnAfterLogin() {
        sessionFixture.openAnonymousSession()

        ordersPage.openProtected()
        authAssertions.assertRedirectedToLogin()

        loginPage.login(customerCredentials.email, customerCredentials.password)
        authAssertions.assertOrdersPageOpened()
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Регистрация пользователя")
    @DisplayName("Успешная регистрация нового пользователя")
    fun shouldRegisterNewUserSuccessfully() {
        sessionFixture.openAnonymousSession()
        val user = AuthTestDataFactory.uniqueUser()

        authFlow.register(user)

        authAssertions.assertLoggedInAsCustomer()
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Выход пользователя")
    @DisplayName("Выход завершает сессию и закрывает доступ к защищенным маршрутам")
    fun shouldLogoutAndDropProtectedAccess() {
        sessionFixture.openAuthenticatedSession(customerCredentials)

        topBar.clickLogout()
        authAssertions.assertAnonymousTopbar()

        ordersPage.openProtected()
        authAssertions.assertRedirectedToLogin()
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Неверные учетные данные")
    @DisplayName("Ошибка авторизации при неверном пароле")
    fun shouldShowErrorForInvalidCredentials() {
        sessionFixture.openAnonymousSession()

        authFlow.loginWithPassword(customerCredentials.email, "wrong-password")

        authAssertions.assertOnLoginPage()
        authAssertions.assertLoginError("email")
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Валидация формы входа")
    @DisplayName("Валидация формы авторизации при невалидном email")
    fun shouldValidateLoginForm() {
        sessionFixture.openAnonymousSession()

        loginPage.open()
        loginPage.typeEmail(customerCredentials.email)
        loginPage.typePassword("")
        loginPage.submit()

        authAssertions.assertOnLoginPage()
        authAssertions.assertLoginValidationErrorShown()
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Валидация формы регистрации")
    @DisplayName("Валидация формы регистрации при коротком пароле")
    fun shouldValidateRegisterForm() {
        sessionFixture.openAnonymousSession()

        registerPage.open()
        registerPage.register(
            NewUserRegistration(
                fullName = "Тестовый Пользователь",
                email = "short-pass-${System.currentTimeMillis()}@example.com",
                password = "123",
            ),
        )

        authAssertions.assertOnRegisterPage()
        authAssertions.assertRegisterValidationErrorShown()
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Защита от дублирования учетных записей")
    @DisplayName("Ошибка регистрации при повторном email")
    fun shouldRejectDuplicateRegistration() {
        sessionFixture.openAnonymousSession()
        val duplicateUser = AuthTestDataFactory.uniqueUser(prefix = "duplicate")
        sessionFixture.ensureRegistered(duplicateUser)

        authFlow.register(duplicateUser)

        authAssertions.assertOnRegisterPage()
        authAssertions.assertRegisterError("Email")
    }
}

