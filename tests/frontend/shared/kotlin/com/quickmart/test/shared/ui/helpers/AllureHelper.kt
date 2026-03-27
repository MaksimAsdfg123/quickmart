package com.quickmart.test.shared.ui.helpers

import io.qameta.allure.Allure
import java.nio.file.Files
import java.nio.file.Path

object AllureHelper {
    fun step(name: String, action: () -> Unit) {
        Allure.step(name, Allure.ThrowableRunnableVoid { action() })
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> stepWithResult(name: String, action: () -> T): T {
        var result: Any? = null
        Allure.step(name, Allure.ThrowableRunnableVoid { result = action() })
        return result as T
    }

    fun attachText(name: String, text: String) {
        Allure.addAttachment(name, "text/plain", text)
    }

    fun attachJson(name: String, json: String) {
        Allure.addAttachment(name, "application/json", json)
    }

    fun attachFile(name: String, mimeType: String, path: Path) {
        if (!Files.exists(path)) return
        Files.newInputStream(path).use { input ->
            Allure.addAttachment(name, mimeType, input, path.fileName.toString().substringAfterLast('.', "txt"))
        }
    }
}

