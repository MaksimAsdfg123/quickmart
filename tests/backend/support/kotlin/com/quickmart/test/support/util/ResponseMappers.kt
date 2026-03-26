package com.quickmart.test.support.util

import io.restassured.response.Response

inline fun <reified T> Response.toModel(): T = this.then().extract().`as`(T::class.java)

