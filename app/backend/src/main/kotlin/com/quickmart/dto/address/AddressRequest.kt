package com.quickmart.dto.address

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Представляет полезную нагрузку запроса на создание или обновление адреса.")
data class AddressRequest(
    @field:NotBlank(message = "Укажите название адреса")
    @field:Size(max = 100, message = "Название адреса: максимум 100 символов")
    @field:Schema(description = "Указывает пользовательскую метку адреса. Ограничения: максимальная длина — 100.", example = "Home")
    val label: String,
    @field:NotBlank(message = "Укажите город")
    @field:Size(max = 100, message = "Город: максимум 100 символов")
    @field:Schema(description = "Указывает наименование города. Ограничения: максимальная длина — 100.", example = "Yekaterinburg")
    val city: String,
    @field:NotBlank(message = "Укажите улицу")
    @field:Size(max = 150, message = "Улица: максимум 150 символов")
    @field:Schema(description = "Указывает наименование улицы. Ограничения: максимальная длина — 150.", example = "Lenina")
    val street: String,
    @field:NotBlank(message = "Укажите номер дома")
    @field:Size(max = 30, message = "Дом: максимум 30 символов")
    @field:Schema(description = "Указывает идентификатор дома. Ограничения: максимальная длина — 30.", example = "10")
    val house: String,
    @field:Size(max = 30, message = "Квартира: максимум 30 символов")
    @field:Schema(description = "Указывает номер квартиры, если применимо. Ограничения: максимальная длина — 30.", example = "25")
    val apartment: String?,
    @field:Size(max = 30, message = "Подъезд: максимум 30 символов")
    @field:Schema(description = "Указывает номер подъезда, если применимо. Ограничения: максимальная длина — 30.", example = "2")
    val entrance: String?,
    @field:Size(max = 30, message = "Этаж: максимум 30 символов")
    @field:Schema(description = "Указывает этаж, если применимо. Ограничения: максимальная длина — 30.", example = "7")
    val floor: String?,
    @field:Size(max = 300, message = "Комментарий: максимум 300 символов")
    @field:Schema(description = "Указывает необязательный комментарий для курьера. Ограничения: максимальная длина — 300.", example = "Call 10 minutes before arrival")
    val comment: String?,
    @field:Schema(description = "Указывает, должен ли адрес быть сохранен как адрес по умолчанию.", example = "true")
    val isDefault: Boolean = false,
)
