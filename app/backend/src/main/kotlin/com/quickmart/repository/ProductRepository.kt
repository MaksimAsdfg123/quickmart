package com.quickmart.repository

import com.quickmart.domain.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface ProductRepository : JpaRepository<Product, UUID> {
    @EntityGraph(attributePaths = ["category"])
    override fun findById(id: UUID): Optional<Product>

    @Query(
        """
        select p from Product p
        join p.category c
        where p.active = true
          and (:categoryId is null or c.id = :categoryId)
          and (:searchPattern is null or lower(p.name) like :searchPattern)
        """,
    )
    @EntityGraph(attributePaths = ["category"])
    fun findCatalogPage(
        @Param("categoryId") categoryId: UUID?,
        @Param("searchPattern") searchPattern: String?,
        pageable: Pageable,
    ): Page<Product>
}
