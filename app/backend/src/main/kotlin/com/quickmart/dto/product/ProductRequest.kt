package com.quickmart.dto.product

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.UUID

data class ProductRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val name: String,
    @field:Size(max = 2000)
    val description: String?,
    @field:NotNull
    @field:DecimalMin("0.01")
    val price: BigDecimal,
    @field:NotNull
    val categoryId: UUID,
    @field:Size(max = 500)
    val imageUrl: String?,
    val active: Boolean = true,
)
