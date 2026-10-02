package com.simpsonm09.template.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ItemJpaRepository : JpaRepository<ItemEntity, Long>
