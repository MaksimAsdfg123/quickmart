package com.quickmart.dto.address

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Представляет ресурс адреса.")
data class AddressResponse(
    @field:Schema(description = "Идентифицирует адрес. Формат: UUID.", example = "20000000-0000-0000-0000-000000000001")
    val id: UUID,
    @field:Schema(description = "Содержит пользовательскую метку адреса.", example = "Home")
    val label: String,
    @field:Schema(description = "Содержит наименование города.", example = "Yekaterinburg")
    val city: String,
    @field:Schema(description = "Содержит наименование улицы.", example = "Lenina")
    val street: String,
    @field:Schema(description = "Содержит идентификатор дома.", example = "10")
    val house: String,
    @field:Schema(description = "Содержит номер квартиры, если он указан.", example = "25")
    val apartment: String?,
    @field:Schema(description = "Содержит номер подъезда, если он указан.", example = "2")
    val entrance: String?,
    @field:Schema(description = "Содержит этаж, если он указан.", example = "7")
    val floor: String?,
    @field:Schema(description = "Содержит комментарий для курьера, если он указан.", example = "Call 10 minutes before arrival")
    val comment: String?,
    @field:Schema(description = "Указывает, помечен ли адрес как адрес по умолчанию.", example = "true")
    val isDefault: Boolean,
)
