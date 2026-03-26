package com.quickmart.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    private val orderedTagDefinitions =
        listOf(
            TagDefinition(TAG_AUTH, "Операции аутентификации и выдачи токенов"),
            TagDefinition(TAG_CATALOG, "Операции публичного каталога товаров"),
            TagDefinition(TAG_CATEGORIES, "Операции публичного списка категорий"),
            TagDefinition(TAG_CART, "Операции с корзиной"),
            TagDefinition(TAG_ADDRESSES, "Операции с адресами пользователя"),
            TagDefinition(TAG_DELIVERY_SLOTS, "Операции публичного списка слотов доставки"),
            TagDefinition(TAG_ORDERS, "Операции оформления и просмотра заказов пользователя"),
            TagDefinition(TAG_ADMIN_PRODUCTS, "Операции административного управления товарами"),
            TagDefinition(TAG_ADMIN_CATEGORIES, "Операции административного управления категориями"),
            TagDefinition(TAG_ADMIN_INVENTORY, "Операции административного управления остатками"),
            TagDefinition(TAG_ADMIN_ORDERS, "Операции административного управления заказами"),
            TagDefinition(TAG_ADMIN_PROMOS, "Операции административного управления промокодами"),
            TagDefinition(TAG_ADMIN_DELIVERY_SLOTS, "Операции административного управления слотами доставки"),
        )

    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Quickmart Backend API")
                    .version("v1")
                    .summary("Спецификация REST API сервиса Quickmart.")
                    .description(
                        """
                        Тестовые учетные записи:
                        - Администратор: `admin@quickmart.local` / `password`
                        - Пользователь: `anna@example.com` / `password`
                        - Пользователь: `ivan@example.com` / `password`
                        """.trimIndent(),
                    )
                    .contact(
                        Contact()
                            .name("Сопровождение API Quickmart")
                            .email("admin@quickmart.local"),
                    ),
            ).servers(
                listOf(
                    Server().url("/").description("Текущее окружение"),
                    Server().url("http://localhost:8080").description("Локальное окружение разработки"),
                ),
            ).tags(
                orderedTagDefinitions.map { Tag().name(it.name).description(it.description) },
            ).components(
                Components()
                    .addSecuritySchemes(
                        BEARER_AUTH_SCHEME,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Bearer JWT-токен доступа. Указывается значение токена без префикса `Bearer`."),
                    ).addSchemas("ApiErrorResponse", errorSchema())
                    .addResponses(
                        "BadRequest",
                        errorResponse(
                            description = "Ошибка валидации запроса, некорректный формат полезной нагрузки или нарушение бизнес-ограничения, сопоставленного с HTTP 400.",
                            example = mapOf(
                                "timestamp" to "2026-03-26T10:15:30",
                                "status" to 400,
                                "error" to "Bad Request",
                                "message" to "Сумма заказа меньше минимальной для промокода",
                                "path" to "/api/orders/checkout",
                                "fieldErrors" to emptyMap<String, String>(),
                            ),
                        ),
                    ).addResponses(
                        "UnauthorizedAccess",
                        errorResponse(
                            description = "Требуется аутентификация либо переданный JWT недействителен, истек или не поддерживается.",
                            example = mapOf(
                                "timestamp" to "2026-03-26T10:15:30",
                                "status" to 401,
                                "error" to "Unauthorized",
                                "message" to "Требуется авторизация",
                                "path" to "/api/cart",
                                "fieldErrors" to null,
                            ),
                        ),
                    ).addResponses(
                        "UnauthorizedLogin",
                        errorResponse(
                            description = "Аутентификация не выполнена, поскольку переданы недействительные учетные данные.",
                            example = mapOf(
                                "timestamp" to "2026-03-26T10:15:30",
                                "status" to 401,
                                "error" to "Unauthorized",
                                "message" to "Неверный email или пароль",
                                "path" to "/api/auth/login",
                                "fieldErrors" to null,
                            ),
                        ),
                    ).addResponses(
                        "Forbidden",
                        errorResponse(
                            description = "У аутентифицированного субъекта отсутствуют полномочия для выполнения операции.",
                            example = mapOf(
                                "timestamp" to "2026-03-26T10:15:30",
                                "status" to 403,
                                "error" to "Forbidden",
                                "message" to "Доступ запрещен",
                                "path" to "/api/admin/products",
                                "fieldErrors" to null,
                            ),
                        ),
                    ).addResponses(
                        "NotFound",
                        errorResponse(
                            description = "Запрошенный ресурс не существует либо недоступен в текущем контексте безопасности.",
                            example = mapOf(
                                "timestamp" to "2026-03-26T10:15:30",
                                "status" to 404,
                                "error" to "Not Found",
                                "message" to "Заказ не найден",
                                "path" to "/api/orders/00000000-0000-0000-0000-000000000999",
                                "fieldErrors" to null,
                            ),
                        ),
                    ).addResponses(
                        "Conflict",
                        errorResponse(
                            description = "Запрос конфликтует с текущим состоянием данных, как правило из-за ограничения уникальности.",
                            example = mapOf(
                                "timestamp" to "2026-03-26T10:15:30",
                                "status" to 409,
                                "error" to "Conflict",
                                "message" to "Конфликт данных: значение уже существует или нарушены ограничения",
                                "path" to "/api/admin/categories",
                                "fieldErrors" to null,
                            ),
                        ),
                    ).addResponses(
                        "InternalServerError",
                        errorResponse(
                            description = "При обработке запроса возникла необработанная внутренняя ошибка сервера.",
                            example = mapOf(
                                "timestamp" to "2026-03-26T10:15:30",
                                "status" to 500,
                                "error" to "Internal Server Error",
                                "message" to "Внутренняя ошибка сервера",
                                "path" to "/api/orders/checkout",
                                "fieldErrors" to null,
                            ),
                        ),
                    ),
            )

    @Bean
    fun openApiTagOrderCustomizer(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            val mergedByName = linkedMapOf<String, Tag>()
            openApi.tags.orEmpty().forEach { tag ->
                val name = tag.name?.trim().orEmpty()
                if (name.isBlank()) return@forEach
                val existing = mergedByName[name]
                if (existing == null) {
                    mergedByName[name] = Tag().name(name).description(tag.description)
                } else if (existing.description.isNullOrBlank() && !tag.description.isNullOrBlank()) {
                    existing.description = tag.description
                }
            }

            val ordered = mutableListOf<Tag>()
            orderedTagDefinitions.forEach { definition ->
                val fromSpec = mergedByName.remove(definition.name)
                if (fromSpec != null) {
                    if (fromSpec.description.isNullOrBlank()) {
                        fromSpec.description = definition.description
                    }
                    ordered.add(fromSpec)
                } else {
                    ordered.add(Tag().name(definition.name).description(definition.description))
                }
            }

            ordered.addAll(mergedByName.values.sortedBy { it.name })
            openApi.tags = ordered
        }

    private data class TagDefinition(
        val name: String,
        val description: String,
    )

    private fun errorSchema(): Schema<*> =
        ObjectSchema()
            .addProperty(
                "timestamp",
                StringSchema()
                    .format("date-time")
                    .description("Временная отметка формирования ошибки."),
            ).addProperty(
                "status",
                IntegerSchema().description("HTTP-код состояния."),
            ).addProperty(
                "error",
                StringSchema().description("Стандартная текстовая причина HTTP-статуса."),
            ).addProperty(
                "message",
                StringSchema().description("Прикладное описание ошибки."),
            ).addProperty(
                "path",
                StringSchema().description("Путь запроса.").nullable(true),
            ).addProperty(
                "fieldErrors",
                MapSchema()
                    .additionalProperties(StringSchema())
                    .description("Сообщения об ошибках валидации по полям.")
                    .nullable(true),
            ).required(listOf("timestamp", "status", "error", "message"))

    private fun errorResponse(
        description: String,
        example: Map<String, Any?>,
    ): ApiResponse =
        ApiResponse()
            .description(description)
            .content(
                Content().addMediaType(
                    "application/json",
                    MediaType()
                        .schema(errorSchema())
                        .example(example),
                ),
            )
}
