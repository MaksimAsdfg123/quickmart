package com.quickmart.test.shared.foundation

import io.restassured.response.Response

inline fun <reified T> Response.toModel(): T = this.then().extract().`as`(T::class.java)

