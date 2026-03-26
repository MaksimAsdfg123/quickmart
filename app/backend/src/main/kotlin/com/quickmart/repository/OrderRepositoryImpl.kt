package com.quickmart.repository

import com.quickmart.domain.entity.Order
import com.quickmart.domain.enums.OrderStatus
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class OrderRepositoryImpl : OrderRepositoryCustom {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun searchForAdmin(
        statuses: Collection<OrderStatus>?,
        query: String?,
        pageable: Pageable,
    ): Page<Order> {
        val criteriaBuilder = entityManager.criteriaBuilder

        val contentQuery = criteriaBuilder.createQuery(Order::class.java)
        val contentRoot = contentQuery.from(Order::class.java)
        contentQuery.select(contentRoot)
        contentQuery.where(*buildPredicates(criteriaBuilder, contentRoot, statuses, query).toTypedArray())
        contentQuery.orderBy(criteriaBuilder.desc(contentRoot.get<java.time.LocalDateTime>("createdAt")))

        val content =
            entityManager
                .createQuery(contentQuery)
                .setFirstResult(pageable.offset.toInt())
                .setMaxResults(pageable.pageSize)
                .resultList

        val countQuery = criteriaBuilder.createQuery(Long::class.java)
        val countRoot = countQuery.from(Order::class.java)
        countQuery.select(criteriaBuilder.count(countRoot))
        countQuery.where(*buildPredicates(criteriaBuilder, countRoot, statuses, query).toTypedArray())

        val total = entityManager.createQuery(countQuery).singleResult
        return PageImpl(content, pageable, total)
    }

    private fun buildPredicates(
        criteriaBuilder: jakarta.persistence.criteria.CriteriaBuilder,
        root: jakarta.persistence.criteria.Root<Order>,
        statuses: Collection<OrderStatus>?,
        query: String?,
    ): List<Predicate> {
        val predicates = mutableListOf<Predicate>()

        if (!statuses.isNullOrEmpty()) {
            predicates += root.get<OrderStatus>("status").`in`(statuses)
        }

        val normalizedQuery = query?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        if (normalizedQuery != null) {
            val pattern = "%$normalizedQuery%"
            val addressExpression = criteriaBuilder.lower(root.get<String>("addressSnapshot"))
            val idExpression = criteriaBuilder.lower(root.get<Any>("id").`as`(String::class.java))
            predicates +=
                criteriaBuilder.or(
                    criteriaBuilder.like(addressExpression, pattern),
                    criteriaBuilder.like(idExpression, pattern),
                )
        }

        return predicates
    }
}
