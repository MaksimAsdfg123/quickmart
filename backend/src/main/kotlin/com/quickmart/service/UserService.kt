package com.quickmart.service

import com.quickmart.domain.entity.User
import com.quickmart.exception.NotFoundException
import com.quickmart.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getByIdOrThrow(id: UUID): User =
        userRepository
            .findById(id)
            .orElseThrow { NotFoundException("Пользователь не найден") }
}
