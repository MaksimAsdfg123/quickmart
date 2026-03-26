package com.quickmart.dto.address

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AddressRequest(
    @field:NotBlank(message = "Укажите название адреса")
    @field:Size(max = 100, message = "Название адреса: максимум 100 символов")
    val label: String,
    @field:NotBlank(message = "Укажите город")
    @field:Size(max = 100, message = "Город: максимум 100 символов")
    val city: String,
    @field:NotBlank(message = "Укажите улицу")
    @field:Size(max = 150, message = "Улица: максимум 150 символов")
    val street: String,
    @field:NotBlank(message = "Укажите номер дома")
    @field:Size(max = 30, message = "Дом: максимум 30 символов")
    val house: String,
    @field:Size(max = 30, message = "Квартира: максимум 30 символов")
    val apartment: String?,
    @field:Size(max = 30, message = "Подъезд: максимум 30 символов")
    val entrance: String?,
    @field:Size(max = 30, message = "Этаж: максимум 30 символов")
    val floor: String?,
    @field:Size(max = 300, message = "Комментарий: максимум 300 символов")
    val comment: String?,
    val isDefault: Boolean = false,
)
