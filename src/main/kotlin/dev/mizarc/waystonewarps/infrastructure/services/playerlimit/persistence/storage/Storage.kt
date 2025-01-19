package dev.mizarc.waystonewarps.infrastructure.services.playerlimit.persistence.storage

interface Storage<T> {
    val connection: T
}