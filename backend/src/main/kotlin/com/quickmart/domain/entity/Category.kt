package com.quickmart.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "categories")
class Category : BaseEntity() {
    @Column(name = "name", nullable = false, unique = true)
    lateinit var name: String

    @Column(name = "description")
    var description: String? = null

    @Column(name = "active", nullable = false)
    var active: Boolean = true

    @OneToMany(mappedBy = "category")
    var products: MutableList<Product> = mutableListOf()
}
