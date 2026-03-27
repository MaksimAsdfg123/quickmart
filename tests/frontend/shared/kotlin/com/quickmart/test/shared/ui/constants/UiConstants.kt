package com.quickmart.test.shared.ui.constants

object UiRoutes {
    const val CATALOG = "/"
    const val LOGIN = "/login"
    const val REGISTER = "/register"
    const val ORDERS = "/orders"
}

object UiTestIds {
    const val LOGIN_PAGE = "login-page"
    const val LOGIN_FORM = "login-form"
    const val LOGIN_EMAIL = "login-email"
    const val LOGIN_PASSWORD = "login-password"
    const val LOGIN_SUBMIT = "login-submit"
    const val LOGIN_ERROR = "login-error"
    const val LOGIN_GO_REGISTER = "login-go-register"

    const val REGISTER_PAGE = "register-page"
    const val REGISTER_FORM = "register-form"
    const val REGISTER_FULL_NAME = "register-full-name"
    const val REGISTER_EMAIL = "register-email"
    const val REGISTER_PASSWORD = "register-password"
    const val REGISTER_SUBMIT = "register-submit"
    const val REGISTER_ERROR = "register-error"
    const val REGISTER_GO_LOGIN = "register-go-login"

    const val TOPBAR = "topbar"
    const val TOPBAR_LOGIN_LINK = "topbar-login-link"
    const val TOPBAR_REGISTER_LINK = "topbar-register-link"
    const val TOPBAR_NAV_ORDERS = "topbar-nav-orders"
    const val TOPBAR_NAV_ADMIN = "topbar-nav-admin"
    const val SESSION_USER = "session-user"
    const val LOGOUT_BUTTON = "logout-button"
}

object UiTimeouts {
    const val ACTION_TIMEOUT_MS = 10_000.0
    const val ASSERT_TIMEOUT_MS = 12_000.0
    const val NAVIGATION_TIMEOUT_MS = 25_000.0
}

