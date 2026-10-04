package com.unieats.shared

/** Last-write-wins on updatedAt: the server wins only if strictly newer; a tie goes to the client. */
object SyncConflictResolver {
    fun resolve(local: Spot, server: Spot): Spot =
        if (server.updatedAt > local.updatedAt) server else local
}
