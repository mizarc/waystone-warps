package dev.mizarc.waystonewarps.infrastructure.persistence.storage

interface Storage<T> {
    val connection: T
}