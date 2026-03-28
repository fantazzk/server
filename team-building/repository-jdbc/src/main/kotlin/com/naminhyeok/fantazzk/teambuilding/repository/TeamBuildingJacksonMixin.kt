package com.naminhyeok.fantazzk.teambuilding.repository

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.naminhyeok.fantazzk.teambuilding.room.AuctionResult
import com.naminhyeok.fantazzk.teambuilding.room.Progression

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(Progression.Auction::class, name = "AUCTION"),
    JsonSubTypes.Type(Progression.Draft::class, name = "DRAFT"),
)
internal interface ProgressionMixin

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(AuctionResult.Outcome.Sold::class, name = "SOLD"),
    JsonSubTypes.Type(AuctionResult.Outcome.Passed::class, name = "PASSED"),
)
internal interface AuctionResultOutcomeMixin
