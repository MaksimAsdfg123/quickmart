package com.quickmart.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "products")
class Product : BaseEntity() {
    @Column(name = "name", nullable = false)
    lateinit var name: String

    @Column(name = "description")
    var description: String? = null

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    lateinit var category: Category

    @Column(name = "image_url")
    var imageUrl: String? = null

    @Column(name = "active", nullable = false)
    var active: Boolean = true

    @OneToOne(mappedBy = "product")
    var inventoryStock: InventoryStock? = null
}
