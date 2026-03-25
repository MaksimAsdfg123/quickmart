package com.quickmart.integration

import com.quickmart.dto.address.AddressRequest
import com.quickmart.repository.AddressRepository
import com.quickmart.service.AddressService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class AddressIntegrationTest : IntegrationTestBase() {
    @Autowired
    private lateinit var addressService: AddressService

    @Autowired
    private lateinit var addressRepository: AddressRepository

    private val customerId = UUID.fromString("00000000-0000-0000-0000-000000000002")

    @Test
    fun `create should normalize optional fields and keep only one default address`() {
        val created =
            addressService.create(
                customerId,
                AddressRequest(
                    label = "  Тестовый адрес  ",
                    city = "  Екатеринбург  ",
                    street = "  Мира  ",
                    house = "  42  ",
                    apartment = "   ",
                    entrance = "\t",
                    floor = "",
                    comment = "   ",
                    isDefault = true,
                ),
            )

        val saved = addressRepository.findById(created.id).orElseThrow()
        assertEquals("Тестовый адрес", saved.label)
        assertEquals("Екатеринбург", saved.city)
        assertEquals("Мира", saved.street)
        assertEquals("42", saved.house)

        assertNull(saved.apartment)
        assertNull(saved.entrance)
        assertNull(saved.floor)
        assertNull(saved.comment)

        val allAddresses = addressRepository.findAllByUserId(customerId)
        val defaults = allAddresses.filter { it.isDefault }

        assertEquals(1, defaults.size)
        assertTrue(defaults.any { it.id == created.id })
        assertNotNull(created.id)

        addressRepository.deleteById(created.id)
    }
}
