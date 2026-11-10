package com.nexdev.crochet.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PartWithRounds(
    @Embedded val part: PartEntity,
    @Relation(
        parentColumn = "idPieza",
        entityColumn = "partId"
    )
    val rounds: List<RoundEntity>
) {
    val sortedRounds: List<RoundEntity>
        get() = rounds.sortedBy { it.numero }
}

data class PatternWithPartsAndRounds(
    @Embedded val pattern: PatternEntity,
    @Relation(
        entity = PartEntity::class,
        parentColumn = "id",
        entityColumn = "patternId"
    )
    val parts: List<PartWithRounds>
) {
    val sortedParts: List<PartWithRounds>
        get() = parts.sortedBy { it.part.orden }
}

data class CompletedProjectCount(
    val patternId: String,
    val count: Int
)
