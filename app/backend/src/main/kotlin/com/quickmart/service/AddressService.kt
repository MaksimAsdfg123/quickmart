package com.quickmart.service

import com.quickmart.domain.entity.Address
import com.quickmart.dto.address.AddressRequest
import com.quickmart.dto.address.AddressResponse
import com.quickmart.exception.NotFoundException
import com.quickmart.mapper.AddressMapper
import com.quickmart.repository.AddressRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AddressService(
    private val userService: UserService,
    private val addressRepository: AddressRepository,
    private val addressMapper: AddressMapper,
) {
    private fun normalizeOptional(value: String?): String? = value?.trim()?.takeIf { it.isNotEmpty() }

    fun getMyAddresses(userId: UUID): List<AddressResponse> =
        addressRepository
            .findAllByUserId(userId)
            .sortedByDescending { it.isDefault }
            .map(addressMapper::toResponse)

    @Transactional
    fun create(
        userId: UUID,
        request: AddressRequest,
    ): AddressResponse {
        if (request.isDefault) {
            unsetDefaultForUser(userId)
        }

        val address =
            Address().apply {
                user = userService.getByIdOrThrow(userId)
                label = request.label.trim()
                city = request.city.trim()
                street = request.street.trim()
                house = request.house.trim()
                apartment = normalizeOptional(request.apartment)
                entrance = normalizeOptional(request.entrance)
                floor = normalizeOptional(request.floor)
                comment = normalizeOptional(request.comment)
                isDefault = request.isDefault
            }

        return addressMapper.toResponse(addressRepository.save(address))
    }

    @Transactional
    fun update(
        userId: UUID,
        id: UUID,
        request: AddressRequest,
    ): AddressResponse {
        val address =
            addressRepository.findByIdAndUserId(id, userId)
                ?: throw NotFoundException("Адрес не найден")

        if (request.isDefault) {
            unsetDefaultForUser(userId)
        }

        address.label = request.label.trim()
        address.city = request.city.trim()
        address.street = request.street.trim()
        address.house = request.house.trim()
        address.apartment = normalizeOptional(request.apartment)
        address.entrance = normalizeOptional(request.entrance)
        address.floor = normalizeOptional(request.floor)
        address.comment = normalizeOptional(request.comment)
        address.isDefault = request.isDefault

        return addressMapper.toResponse(addressRepository.save(address))
    }

    @Transactional
    fun delete(
        userId: UUID,
        id: UUID,
    ) {
        val address =
            addressRepository.findByIdAndUserId(id, userId)
                ?: throw NotFoundException("Адрес не найден")
        addressRepository.delete(address)
    }

    fun getByIdAndUserOrThrow(
        addressId: UUID,
        userId: UUID,
    ): Address = addressRepository.findByIdAndUserId(addressId, userId) ?: throw NotFoundException("Адрес не найден")

    @Transactional
    fun unsetDefaultForUser(userId: UUID) {
        val addresses = addressRepository.findAllByUserId(userId)
        addresses.forEach { it.isDefault = false }
        addressRepository.saveAll(addresses)
    }
}
