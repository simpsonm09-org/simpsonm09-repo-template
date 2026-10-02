package com.simpsonm09.template.config

import com.simpsonm09.template.persistence.ItemEntity
import com.simpsonm09.template.persistence.ItemJpaRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/** Seeds three items on startup under the `dev` profile so the API has something to return. */
@Configuration
@Profile("dev")
class DevSeedConfig {
    @Bean
    fun seedItems(repository: ItemJpaRepository): CommandLineRunner = CommandLineRunner {
        if (repository.count() == 0L) {
            repository.saveAll(
                listOf(
                    ItemEntity(name = "Widget", description = "A small widget"),
                    ItemEntity(name = "Gadget", description = "A handy gadget"),
                    ItemEntity(name = "Gizmo", description = "A clever gizmo"),
                ),
            )
        }
    }
}
