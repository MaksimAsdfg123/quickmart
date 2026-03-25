package com.quickmart.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "addresses")
class Address : BaseEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: User

    @Column(name = "label", nullable = false)
    lateinit var label: String

    @Column(name = "city", nullable = false)
    lateinit var city: String

    @Column(name = "street", nullable = false)
    lateinit var street: String

    @Column(name = "house", nullable = false)
    lateinit var house: String

    @Column(name = "apartment")
    var apartment: String? = null

    @Column(name = "entrance")
    var entrance: String? = null

    @Column(name = "floor")
    var floor: String? = null

    @Column(name = "comment")
    var comment: String? = null

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false

    fun toSingleLine(): String {
        val apartmentPart = apartment?.let { ", apt $it" } ?: ""
        return "$city, $street $house$apartmentPart"
    }
}
