package com.quickmart.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Version

@Entity
@Table(name = "inventory_stocks")
class InventoryStock : BaseEntity() {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    lateinit var product: Product

    @Column(name = "available_quantity", nullable = false)
    var availableQuantity: Int = 0

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0
}
