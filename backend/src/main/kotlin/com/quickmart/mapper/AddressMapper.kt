package com.quickmart.mapper

import com.quickmart.domain.entity.Address
import com.quickmart.dto.address.AddressResponse
import org.springframework.stereotype.Component

@Component
class AddressMapper {
    fun toResponse(address: Address): AddressResponse =
        AddressResponse(
            id = address.id!!,
            label = address.label,
            city = address.city,
            street = address.street,
            house = address.house,
            apartment = address.apartment,
            entrance = address.entrance,
            floor = address.floor,
            comment = address.comment,
            isDefault = address.isDefault,
        )
}
