package com.quickmart.domain.entity

import com.quickmart.domain.enums.Role
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User : BaseEntity() {
    @Column(name = "email", nullable = false, unique = true)
    lateinit var email: String

    @Column(name = "password_hash", nullable = false)
    lateinit var passwordHash: String

    @Column(name = "full_name", nullable = false)
    lateinit var fullName: String

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: Role = Role.CUSTOMER

    @Column(name = "active", nullable = false)
    var active: Boolean = true

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var addresses: MutableList<Address> = mutableListOf()

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var cart: Cart? = null

    @OneToMany(mappedBy = "user")
    var orders: MutableList<Order> = mutableListOf()
}
