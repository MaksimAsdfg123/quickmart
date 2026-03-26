package com.quickmart.repository

import com.quickmart.domain.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CategoryRepository : JpaRepository<Category, UUID> {
    fun findAllByActiveTrueOrderByNameAsc(): List<Category>

    fun existsByNameIgnoreCase(name: String): Boolean
}
