package fr.northborders.di

import fr.northborders.repository.HeroRepository
import fr.northborders.repository.HeroRepositoryImpl
import org.koin.dsl.module

val koinModule = module {
    single<HeroRepository> {
        HeroRepositoryImpl()
    }
}