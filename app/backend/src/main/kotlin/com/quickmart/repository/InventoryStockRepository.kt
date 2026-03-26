package com.quickmart.repository

import com.quickmart.domain.entity.InventoryStock
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface InventoryStockRepository : JpaRepository<InventoryStock, UUID> {
    @EntityGraph(attributePaths = ["product", "product.category"])
    override fun findAll(pageable: Pageable): Page<InventoryStock>

    fun findByProductId(productId: UUID): InventoryStock?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from InventoryStock s where s.product.id = :productId")
    fun findByProductIdForUpdate(
        @Param("productId") productId: UUID,
    ): Optional<InventoryStock>
}
