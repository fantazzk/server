# TeamBuilding 도메인 구현 플랜

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 게임 대회 모의경매/모의드래프트 팀 빌딩 플랫폼의 TeamBuilding 바운디드 컨텍스트 구현

**Architecture:** 단일 바운디드 컨텍스트(TeamBuilding) 내에 Template과 Room 두 개의 Aggregate. Room이 경매/드래프트의 전체 라이프사이클을 소유. 모듈러 모놀리스 구조(model, exception, infrastructure, service, repository-jdbc, api, schema)를 따르며, `@AutoConfiguration` 기반 명시적 빈 등록, 컴포넌트 스캔 금지.

**Tech Stack:** Kotlin 2.3, Spring Boot 4.0.3, Spring Data JDBC, MySQL, Liquibase, JUnit 5

**Design Spec:** `docs/superpowers/specs/2026-03-28-team-building-domain-design.md`

---

## File Structure

```
team-building/
├── build.gradle.kts                          (container, no src)
├── model/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/
│       ├── TeamBuildingMode.kt
│       ├── DraftOrderStrategy.kt
│       ├── template/
│       │   ├── TemplateId.kt
│       │   ├── Rules.kt
│       │   ├── PlayerEntry.kt
│       │   └── Template.kt
│       └── room/
│           ├── RoomId.kt
│           ├── RoomStatus.kt
│           ├── RoomSettings.kt
│           ├── TeamLeaderId.kt
│           ├── TeamLeader.kt
│           ├── Player.kt
│           ├── PlayerPool.kt
│           ├── Bid.kt
│           ├── AuctionResult.kt
│           ├── Pick.kt
│           ├── Progression.kt
│           ├── Team.kt
│           ├── RoomResult.kt
│           └── Room.kt
├── exception/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/exception/
│       └── TeamBuildingException.kt
├── infrastructure/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/infrastructure/
│       ├── TemplateRepository.kt
│       └── RoomRepository.kt
├── service/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/service/
│       ├── TemplateService.kt
│       ├── RoomService.kt
│       └── TeamBuildingServiceAutoConfiguration.kt
├── repository-jdbc/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/repository/
│       ├── TemplateJdbcRepository.kt
│       ├── RoomJdbcRepository.kt
│       └── TeamBuildingRepositoryAutoConfiguration.kt
├── api/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/
│       ├── TemplateController.kt
│       ├── RoomController.kt
│       ├── dto/
│       │   ├── TemplateRequest.kt
│       │   ├── TemplateResponse.kt
│       │   ├── RoomRequest.kt
│       │   └── RoomResponse.kt
│       └── TeamBuildingApiAutoConfiguration.kt
└── schema/
    ├── build.gradle.kts
    └── src/main/resources/db/changelog/
        ├── db.changelog-master.yaml
        └── 001-create-team-building-tables.sql
```

---

## Task 1: Gradle Module Scaffolding

**Files:**
- Modify: `settings.gradle.kts`
- Create: `team-building/build.gradle.kts`
- Create: `team-building/model/build.gradle.kts`
- Create: `team-building/exception/build.gradle.kts`
- Create: `team-building/infrastructure/build.gradle.kts`
- Create: `team-building/service/build.gradle.kts`
- Create: `team-building/repository-jdbc/build.gradle.kts`
- Create: `team-building/api/build.gradle.kts`
- Create: `team-building/schema/build.gradle.kts`
- Modify: `application-api/build.gradle.kts`

- [ ] **Step 1: Create directory structure**

```bash
mkdir -p team-building/{model,exception,infrastructure,service,repository-jdbc,api,schema}/src/{main,test}/kotlin
mkdir -p team-building/schema/src/main/resources
```

- [ ] **Step 2: Create container build.gradle.kts**

Create `team-building/build.gradle.kts`:
```kotlin
// Container module — no source code, only groups submodules
```

- [ ] **Step 3: Create sub-module build files**

Create `team-building/model/build.gradle.kts`:
```kotlin
// Model module — no external dependencies
```

Create `team-building/exception/build.gradle.kts`:
```kotlin
// Exception module — no external dependencies
```

Create `team-building/infrastructure/build.gradle.kts`:
```kotlin
dependencies {
    api(project(":team-building:model"))
}
```

Create `team-building/service/build.gradle.kts`:
```kotlin
dependencies {
    api(project(":team-building:infrastructure"))
    implementation(project(":team-building:exception"))
}
```

Create `team-building/repository-jdbc/build.gradle.kts`:
```kotlin
dependencies {
    api(project(":team-building:infrastructure"))
}
```

Create `team-building/api/build.gradle.kts`:
```kotlin
dependencies {
    implementation(project(":team-building:service"))
    implementation(project(":team-building:exception"))
}
```

Create `team-building/schema/build.gradle.kts`:
```kotlin
// Schema module — Liquibase migrations only, no Kotlin sources
```

- [ ] **Step 4: Update settings.gradle.kts**

Add after `include(":application-api")`:
```kotlin
include(":team-building")
include(":team-building:model")
include(":team-building:exception")
include(":team-building:infrastructure")
include(":team-building:service")
include(":team-building:repository-jdbc")
include(":team-building:api")
include(":team-building:schema")
```

- [ ] **Step 5: Update application-api dependencies**

In `application-api/build.gradle.kts`, add:
```kotlin
implementation(project(":team-building:api"))
implementation(project(":team-building:repository-jdbc"))
implementation(project(":team-building:schema"))
```

- [ ] **Step 6: Verify Gradle sync**

```bash
./gradlew projects
```
Expected: all team-building sub-modules listed.

- [ ] **Step 7: Commit**

```bash
git add -A && git commit -m "chore: team-building 모듈 스캐폴딩"
```

---

## Task 2: Shared Enums & Template Aggregate Model

**Files:**
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/TeamBuildingMode.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/DraftOrderStrategy.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/template/TemplateId.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/template/PlayerEntry.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/template/Rules.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/template/Template.kt`
- Test: `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/template/RulesTest.kt`
- Test: `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/template/TemplateTest.kt`

- [ ] **Step 1: Write failing tests for Rules validation and Template creation**

Create `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/template/RulesTest.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.template

import com.naminhyeok.fantazzk.teambuilding.model.DraftOrderStrategy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class RulesTest {
    @Test
    fun `teamCount must be positive`() {
        assertThrows<IllegalArgumentException> {
            Rules(teamCount = 0, teamSize = 5)
        }
    }

    @Test
    fun `teamSize must be positive`() {
        assertThrows<IllegalArgumentException> {
            Rules(teamCount = 5, teamSize = 0)
        }
    }

    @Test
    fun `budget must be positive when provided`() {
        assertThrows<IllegalArgumentException> {
            Rules(teamCount = 5, teamSize = 5, budget = 0)
        }
    }

    @Test
    fun `valid auction rules`() {
        assertDoesNotThrow {
            Rules(teamCount = 5, teamSize = 5, budget = 300)
        }
    }

    @Test
    fun `valid draft rules`() {
        assertDoesNotThrow {
            Rules(teamCount = 5, teamSize = 5, draftOrderStrategy = DraftOrderStrategy.SNAKE)
        }
    }
}
```

Create `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/template/TemplateTest.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.template

import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TemplateTest {
    @Test
    fun `create auction template`() {
        val template = Template(
            id = TemplateId(1L),
            name = "자낳대 시즌7 경매",
            mode = TeamBuildingMode.AUCTION,
            rules = Rules(teamCount = 5, teamSize = 5, budget = 300),
            players = listOf(
                PlayerEntry("선수1"),
                PlayerEntry("선수2", mapOf("tier" to "S")),
            ),
        )

        assertEquals(TeamBuildingMode.AUCTION, template.mode)
        assertEquals(2, template.players.size)
        assertEquals(300, template.rules.budget)
    }
}
```

- [ ] **Step 2: Run tests — verify they fail (classes don't exist yet)**

```bash
./gradlew :team-building:model:test
```
Expected: compilation failure.

- [ ] **Step 3: Implement shared enums and Template aggregate**

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/TeamBuildingMode.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model

enum class TeamBuildingMode {
    AUCTION,
    DRAFT,
}
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/DraftOrderStrategy.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model

enum class DraftOrderStrategy {
    SNAKE,
    FIXED,
}
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/template/TemplateId.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.template

@JvmInline
value class TemplateId(val value: Long)
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/template/PlayerEntry.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.template

data class PlayerEntry(
    val name: String,
    val metadata: Map<String, String> = emptyMap(),
)
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/template/Rules.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.template

import com.naminhyeok.fantazzk.teambuilding.model.DraftOrderStrategy

data class Rules(
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int? = null,
    val draftOrderStrategy: DraftOrderStrategy? = null,
) {
    init {
        require(teamCount > 0) { "teamCount must be positive" }
        require(teamSize > 0) { "teamSize must be positive" }
        budget?.let { require(it > 0) { "budget must be positive" } }
    }
}
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/template/Template.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.template

import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode

data class Template(
    val id: TemplateId,
    val name: String,
    val mode: TeamBuildingMode,
    val rules: Rules,
    val players: List<PlayerEntry>,
)
```

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew :team-building:model:test
```
Expected: all tests pass.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: Template aggregate 모델 구현"
```

---

## Task 3: Room Value Objects & PlayerPool

**Files:**
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomId.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomStatus.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomSettings.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/TeamLeaderId.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/TeamLeader.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Player.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/PlayerPool.kt`
- Test: `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/PlayerPoolTest.kt`

- [ ] **Step 1: Write failing tests for PlayerPool**

Create `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/PlayerPoolTest.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PlayerPoolTest {
    private val pool = PlayerPool(
        players = listOf(
            Player("선수1"),
            Player("선수2"),
            Player("선수3"),
        ),
    )

    @Test
    fun `currentTarget returns first available player`() {
        assertEquals("선수1", pool.currentTarget()?.name)
    }

    @Test
    fun `assignPlayer marks player as ASSIGNED and returns updated pool`() {
        val updated = pool.assignPlayer("선수1")
        assertEquals(PlayerStatus.ASSIGNED, updated.players.first { it.name == "선수1" }.status)
        assertEquals("선수2", updated.currentTarget()?.name)
    }

    @Test
    fun `moveCurrentToBack moves first available player to end`() {
        val updated = pool.moveCurrentToBack()
        val available = updated.players.filter { it.status == PlayerStatus.AVAILABLE }
        assertEquals("선수2", available.first().name)
        assertEquals("선수1", available.last().name)
    }

    @Test
    fun `currentTarget returns null when no available players`() {
        val empty = pool
            .assignPlayer("선수1")
            .assignPlayer("선수2")
            .assignPlayer("선수3")
        assertNull(empty.currentTarget())
    }

    @Test
    fun `allTeamsFull checks against team count and size`() {
        assertEquals(false, pool.allTeamsFull(teamCount = 1, picksPerTeam = 3))
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew :team-building:model:test
```
Expected: compilation failure.

- [ ] **Step 3: Implement Room value objects and PlayerPool**

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomId.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

@JvmInline
value class RoomId(val value: Long)
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomStatus.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

enum class RoomStatus {
    WAITING,
    IN_PROGRESS,
    COMPLETED,
}
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomSettings.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

import com.naminhyeok.fantazzk.teambuilding.model.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode

data class RoomSettings(
    val mode: TeamBuildingMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int? = null,
    val draftOrderStrategy: DraftOrderStrategy? = null,
) {
    /** teamSize는 팀장 포함 인원. 팀장이 픽할 선수 수. */
    val picksPerTeam: Int get() = teamSize - 1
}
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/TeamLeaderId.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

@JvmInline
value class TeamLeaderId(val value: String)
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/TeamLeader.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

data class TeamLeader(
    val id: TeamLeaderId,
    val nickname: String,
    val remainingBudget: Int? = null,
    val team: List<Player> = emptyList(),
) {
    fun hasPickedEnough(picksPerTeam: Int): Boolean = team.size >= picksPerTeam

    fun addPlayer(player: Player): TeamLeader = copy(team = team + player)

    fun deductBudget(amount: Int): TeamLeader {
        val current = requireNotNull(remainingBudget) { "No budget in this mode" }
        require(amount <= current) { "Insufficient budget: $current < $amount" }
        return copy(remainingBudget = current - amount)
    }
}
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Player.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

enum class PlayerStatus {
    AVAILABLE,
    ASSIGNED,
    UNASSIGNED,
}

data class Player(
    val name: String,
    val status: PlayerStatus = PlayerStatus.AVAILABLE,
    val metadata: Map<String, String> = emptyMap(),
)
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/PlayerPool.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

data class PlayerPool(val players: List<Player>) {

    fun currentTarget(): Player? = players.firstOrNull { it.status == PlayerStatus.AVAILABLE }

    fun assignPlayer(name: String): PlayerPool {
        val updated = players.map { p ->
            if (p.name == name && p.status == PlayerStatus.AVAILABLE) {
                p.copy(status = PlayerStatus.ASSIGNED)
            } else {
                p
            }
        }
        return PlayerPool(updated)
    }

    fun moveCurrentToBack(): PlayerPool {
        val target = currentTarget() ?: return this
        val remaining = players.filter { it != target }
        return PlayerPool(remaining + target)
    }

    fun markRemainingAsUnassigned(): PlayerPool {
        val updated = players.map { p ->
            if (p.status == PlayerStatus.AVAILABLE) p.copy(status = PlayerStatus.UNASSIGNED) else p
        }
        return PlayerPool(updated)
    }

    fun allTeamsFull(teamCount: Int, picksPerTeam: Int): Boolean {
        val assignedCount = players.count { it.status == PlayerStatus.ASSIGNED }
        return assignedCount >= teamCount * picksPerTeam
    }
}
```

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew :team-building:model:test
```
Expected: all tests pass.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: Room 값 객체 및 PlayerPool 구현"
```

---

## Task 4: Progression Sealed Class

**Files:**
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Bid.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/AuctionResult.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Pick.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Progression.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Team.kt`
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomResult.kt`
- Test: `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/ProgressionTest.kt`

- [ ] **Step 1: Write failing tests for Progression**

Create `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/ProgressionTest.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

import com.naminhyeok.fantazzk.teambuilding.model.DraftOrderStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProgressionTest {
    @Test
    fun `AuctionProgression tracks highest bid`() {
        val progression = Progression.Auction()
            .addBid(Bid(TeamLeaderId("A"), 100))
            .addBid(Bid(TeamLeaderId("B"), 150))

        assertEquals(150, progression.highestBid()?.amount)
        assertEquals(TeamLeaderId("B"), progression.highestBid()?.teamLeaderId)
    }

    @Test
    fun `AuctionProgression highestBid returns null when no bids`() {
        val progression = Progression.Auction()
        assertEquals(null, progression.highestBid())
    }

    @Test
    fun `Draft snake order generates correct pick sequence for 3 teams`() {
        val teamLeaders = listOf(TeamLeaderId("A"), TeamLeaderId("B"), TeamLeaderId("C"))
        val order = Progression.Draft.generatePickOrder(teamLeaders, DraftOrderStrategy.SNAKE, picksPerTeam = 2)

        // Round 1: A, B, C  |  Round 2: C, B, A
        assertEquals(
            listOf("A", "B", "C", "C", "B", "A"),
            order.map { it.value },
        )
    }

    @Test
    fun `Draft fixed order generates correct pick sequence`() {
        val teamLeaders = listOf(TeamLeaderId("A"), TeamLeaderId("B"), TeamLeaderId("C"))
        val order = Progression.Draft.generatePickOrder(teamLeaders, DraftOrderStrategy.FIXED, picksPerTeam = 2)

        // Round 1: A, B, C  |  Round 2: A, B, C
        assertEquals(
            listOf("A", "B", "C", "A", "B", "C"),
            order.map { it.value },
        )
    }

    @Test
    fun `Draft currentTurn returns correct team leader`() {
        val draft = Progression.Draft(
            pickOrder = listOf(TeamLeaderId("A"), TeamLeaderId("B"), TeamLeaderId("C")),
        )
        assertEquals(TeamLeaderId("A"), draft.currentTurn())
    }

    @Test
    fun `Draft advanceTurn moves to next turn`() {
        val draft = Progression.Draft(
            pickOrder = listOf(TeamLeaderId("A"), TeamLeaderId("B"), TeamLeaderId("C")),
        )
        val next = draft.advanceTurn()
        assertEquals(TeamLeaderId("B"), next.currentTurn())
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew :team-building:model:test
```
Expected: compilation failure.

- [ ] **Step 3: Implement Progression and related types**

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Bid.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

data class Bid(
    val teamLeaderId: TeamLeaderId,
    val amount: Int,
)
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/AuctionResult.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

data class AuctionResult(
    val player: Player,
    val outcome: Outcome,
) {
    sealed class Outcome {
        data class Sold(val teamLeaderId: TeamLeaderId, val amount: Int) : Outcome()
        data object Passed : Outcome()
    }
}
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Pick.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

data class Pick(
    val teamLeaderId: TeamLeaderId,
    val player: Player,
)
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Progression.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

import com.naminhyeok.fantazzk.teambuilding.model.DraftOrderStrategy

sealed class Progression {

    data class Auction(
        val currentBids: List<Bid> = emptyList(),
        val history: List<AuctionResult> = emptyList(),
    ) : Progression() {

        fun addBid(bid: Bid): Auction = copy(currentBids = currentBids + bid)

        fun highestBid(): Bid? = currentBids.maxByOrNull { it.amount }

        fun clearBids(): Auction = copy(currentBids = emptyList())

        fun addResult(result: AuctionResult): Auction =
            copy(currentBids = emptyList(), history = history + result)
    }

    data class Draft(
        val pickOrder: List<TeamLeaderId>,
        val currentTurnIndex: Int = 0,
        val history: List<Pick> = emptyList(),
    ) : Progression() {

        fun currentTurn(): TeamLeaderId = pickOrder[currentTurnIndex]

        fun advanceTurn(): Draft = copy(currentTurnIndex = currentTurnIndex + 1)

        fun addPick(pick: Pick): Draft = copy(history = history + pick)

        fun isFinished(): Boolean = currentTurnIndex >= pickOrder.size

        companion object {
            fun generatePickOrder(
                teamLeaders: List<TeamLeaderId>,
                strategy: DraftOrderStrategy,
                picksPerTeam: Int,
            ): List<TeamLeaderId> =
                (0 until picksPerTeam).flatMap { round ->
                    when (strategy) {
                        DraftOrderStrategy.SNAKE ->
                            if (round % 2 == 0) teamLeaders else teamLeaders.reversed()
                        DraftOrderStrategy.FIXED -> teamLeaders
                    }
                }
        }
    }
}
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Team.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

data class Team(
    val teamLeader: TeamLeader,
    val members: List<Player>,
)
```

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomResult.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

data class RoomResult(val teams: List<Team>)
```

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew :team-building:model:test
```
Expected: all tests pass.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: Progression sealed class 및 관련 값 객체 구현"
```

---

## Task 5: Room Aggregate — Lifecycle (생성, 팀장 참가, 시작)

**Files:**
- Create: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Room.kt`
- Test: `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomLifecycleTest.kt`

- [ ] **Step 1: Write failing tests for Room lifecycle**

Create `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomLifecycleTest.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

import com.naminhyeok.fantazzk.teambuilding.model.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RoomLifecycleTest {
    private val auctionSettings = RoomSettings(
        mode = TeamBuildingMode.AUCTION,
        teamCount = 2,
        teamSize = 3,
        budget = 300,
    )
    private val players = listOf(Player("선수1"), Player("선수2"), Player("선수3"), Player("선수4"))

    private fun createWaitingRoom(): Room = Room.create(
        id = RoomId(1L),
        code = "ABC123",
        hostId = TeamLeaderId("host-1"),
        hostNickname = "호스트",
        settings = auctionSettings,
        playerPool = PlayerPool(players),
    )

    @Test
    fun `create room initializes in WAITING status with host as first team leader`() {
        val room = createWaitingRoom()

        assertEquals(RoomStatus.WAITING, room.status)
        assertEquals(1, room.teamLeaders.size)
        assertEquals("호스트", room.teamLeaders.first().nickname)
        assertEquals(300, room.teamLeaders.first().remainingBudget)
    }

    @Test
    fun `addTeamLeader adds a new team leader`() {
        val room = createWaitingRoom()
            .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")

        assertEquals(2, room.teamLeaders.size)
    }

    @Test
    fun `addTeamLeader fails when room is full`() {
        val room = createWaitingRoom()
            .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")

        assertThrows<IllegalStateException> {
            room.addTeamLeader(TeamLeaderId("leader-3"), "팀장3")
        }
    }

    @Test
    fun `addTeamLeader fails when not in WAITING status`() {
        val room = createWaitingRoom()
            .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")
            .start()

        assertThrows<IllegalStateException> {
            room.addTeamLeader(TeamLeaderId("leader-3"), "팀장3")
        }
    }

    @Test
    fun `start transitions to IN_PROGRESS and initializes progression`() {
        val room = createWaitingRoom()
            .addTeamLeader(TeamLeaderId("leader-2"), "팀장2")
            .start()

        assertEquals(RoomStatus.IN_PROGRESS, room.status)
        assert(room.progression is Progression.Auction)
    }

    @Test
    fun `start fails when not all team leader slots are filled`() {
        val room = createWaitingRoom()

        assertThrows<IllegalStateException> {
            room.start()
        }
    }

    @Test
    fun `start draft room initializes draft progression with pick order`() {
        val draftSettings = RoomSettings(
            mode = TeamBuildingMode.DRAFT,
            teamCount = 2,
            teamSize = 3,
            draftOrderStrategy = DraftOrderStrategy.SNAKE,
        )
        val room = Room.create(
            id = RoomId(2L),
            code = "DEF456",
            hostId = TeamLeaderId("host-1"),
            hostNickname = "호스트",
            settings = draftSettings,
            playerPool = PlayerPool(players),
        ).addTeamLeader(TeamLeaderId("leader-2"), "팀장2")
            .start()

        val draft = room.progression as Progression.Draft
        // Snake: picksPerTeam=2 → Round1: host,leader2  Round2: leader2,host
        assertEquals(4, draft.pickOrder.size)
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew :team-building:model:test --tests "*RoomLifecycleTest*"
```
Expected: compilation failure.

- [ ] **Step 3: Implement Room aggregate root**

Create `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Room.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode

data class Room(
    val id: RoomId,
    val code: String,
    val hostId: TeamLeaderId,
    val status: RoomStatus,
    val settings: RoomSettings,
    val playerPool: PlayerPool,
    val teamLeaders: List<TeamLeader>,
    val progression: Progression?,
    val result: RoomResult?,
) {
    fun addTeamLeader(id: TeamLeaderId, nickname: String): Room {
        check(status == RoomStatus.WAITING) { "Can only add team leaders while WAITING" }
        check(teamLeaders.size < settings.teamCount) { "Room is full" }
        check(teamLeaders.none { it.id == id }) { "Team leader already joined" }

        val leader = TeamLeader(
            id = id,
            nickname = nickname,
            remainingBudget = settings.budget,
        )
        return copy(teamLeaders = teamLeaders + leader)
    }

    fun start(): Room {
        check(status == RoomStatus.WAITING) { "Can only start from WAITING" }
        check(teamLeaders.size == settings.teamCount) { "All team leader slots must be filled" }

        val progression = when (settings.mode) {
            TeamBuildingMode.AUCTION -> Progression.Auction()
            TeamBuildingMode.DRAFT -> {
                val strategy = requireNotNull(settings.draftOrderStrategy) { "Draft requires order strategy" }
                val pickOrder = Progression.Draft.generatePickOrder(
                    teamLeaders.map { it.id },
                    strategy,
                    settings.picksPerTeam,
                )
                Progression.Draft(pickOrder = pickOrder)
            }
        }

        return copy(status = RoomStatus.IN_PROGRESS, progression = progression)
    }

    companion object {
        fun create(
            id: RoomId,
            code: String,
            hostId: TeamLeaderId,
            hostNickname: String,
            settings: RoomSettings,
            playerPool: PlayerPool,
        ): Room {
            val host = TeamLeader(
                id = hostId,
                nickname = hostNickname,
                remainingBudget = settings.budget,
            )
            return Room(
                id = id,
                code = code,
                hostId = hostId,
                status = RoomStatus.WAITING,
                settings = settings,
                playerPool = playerPool,
                teamLeaders = listOf(host),
                progression = null,
                result = null,
            )
        }
    }
}
```

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew :team-building:model:test --tests "*RoomLifecycleTest*"
```
Expected: all tests pass.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: Room aggregate root - 라이프사이클 (생성, 참가, 시작)"
```

---

## Task 6: Room Aggregate — Auction Logic (입찰, 낙찰, 유찰)

**Files:**
- Modify: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Room.kt`
- Test: `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomAuctionTest.kt`

- [ ] **Step 1: Write failing tests for auction logic**

Create `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomAuctionTest.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RoomAuctionTest {
    private fun createStartedAuctionRoom(): Room {
        val settings = RoomSettings(
            mode = TeamBuildingMode.AUCTION,
            teamCount = 2,
            teamSize = 2,
            budget = 300,
        )
        val players = listOf(Player("선수1"), Player("선수2"))
        return Room.create(
            id = RoomId(1L),
            code = "ABC123",
            hostId = TeamLeaderId("host"),
            hostNickname = "호스트",
            settings = settings,
            playerPool = PlayerPool(players),
        ).addTeamLeader(TeamLeaderId("leader2"), "팀장2")
            .start()
    }

    @Test
    fun `placeBid adds bid to current auction`() {
        val room = createStartedAuctionRoom()
            .placeBid(TeamLeaderId("host"), 100)

        val auction = room.progression as Progression.Auction
        assertEquals(1, auction.currentBids.size)
        assertEquals(100, auction.currentBids.first().amount)
    }

    @Test
    fun `placeBid fails when bid exceeds remaining budget`() {
        val room = createStartedAuctionRoom()

        assertThrows<IllegalArgumentException> {
            room.placeBid(TeamLeaderId("host"), 301)
        }
    }

    @Test
    fun `placeBid fails when not IN_PROGRESS`() {
        val room = Room.create(
            id = RoomId(1L),
            code = "ABC123",
            hostId = TeamLeaderId("host"),
            hostNickname = "호스트",
            settings = RoomSettings(mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300),
            playerPool = PlayerPool(listOf(Player("선수1"), Player("선수2"))),
        )

        assertThrows<IllegalStateException> {
            room.placeBid(TeamLeaderId("host"), 100)
        }
    }

    @Test
    fun `settleAuction with bids results in sold — player assigned, budget deducted`() {
        val room = createStartedAuctionRoom()
            .placeBid(TeamLeaderId("host"), 100)
            .placeBid(TeamLeaderId("leader2"), 150)
            .settleCurrentAuction()

        val winner = room.teamLeaders.first { it.id == TeamLeaderId("leader2") }
        assertEquals(1, winner.team.size)
        assertEquals("선수1", winner.team.first().name)
        assertEquals(150, winner.remainingBudget) // 300 - 150

        val auction = room.progression as Progression.Auction
        assertEquals(1, auction.history.size)
        val result = auction.history.first()
        assert(result.outcome is AuctionResult.Outcome.Sold)
    }

    @Test
    fun `settleAuction with no bids results in passed — player moves to back`() {
        val room = createStartedAuctionRoom()
            .settleCurrentAuction()

        val pool = room.playerPool
        val available = pool.players.filter { it.status == PlayerStatus.AVAILABLE }
        assertEquals("선수2", available.first().name)
        assertEquals("선수1", available.last().name)

        val auction = room.progression as Progression.Auction
        assertEquals(1, auction.history.size)
        assert(auction.history.first().outcome is AuctionResult.Outcome.Passed)
    }

    @Test
    fun `room completes when all teams are full`() {
        val room = createStartedAuctionRoom()
            .placeBid(TeamLeaderId("host"), 100)
            .settleCurrentAuction()    // 선수1 → host
            .placeBid(TeamLeaderId("leader2"), 100)
            .settleCurrentAuction()    // 선수2 → leader2

        assertEquals(RoomStatus.COMPLETED, room.status)
        assertNull(room.playerPool.currentTarget())

        val result = requireNotNull(room.result)
        assertEquals(2, result.teams.size)
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew :team-building:model:test --tests "*RoomAuctionTest*"
```
Expected: compilation failure (placeBid, settleCurrentAuction not defined).

- [ ] **Step 3: Add auction methods to Room**

Add to `Room.kt`:
```kotlin
fun placeBid(teamLeaderId: TeamLeaderId, amount: Int): Room {
    check(status == RoomStatus.IN_PROGRESS) { "Room is not in progress" }
    val auction = progression as? Progression.Auction
        ?: error("Not in auction mode")

    val leader = teamLeaders.first { it.id == teamLeaderId }
    require(amount <= (leader.remainingBudget ?: 0)) { "Insufficient budget" }

    val currentHighest = auction.highestBid()?.amount ?: 0
    require(amount > currentHighest) { "Bid must be higher than current highest: $currentHighest" }

    return copy(progression = auction.addBid(Bid(teamLeaderId, amount)))
}

fun settleCurrentAuction(): Room {
    check(status == RoomStatus.IN_PROGRESS) { "Room is not in progress" }
    val auction = progression as? Progression.Auction
        ?: error("Not in auction mode")

    val target = requireNotNull(playerPool.currentTarget()) { "No player to auction" }
    val highestBid = auction.highestBid()

    return if (highestBid != null) {
        settleAsSold(auction, target, highestBid)
    } else {
        settleAsPassed(auction, target)
    }
}

private fun settleAsSold(auction: Progression.Auction, target: Player, bid: Bid): Room {
    val assignedPlayer = target.copy(status = PlayerStatus.ASSIGNED)
    val updatedLeaders = teamLeaders.map { leader ->
        if (leader.id == bid.teamLeaderId) {
            leader.deductBudget(bid.amount).addPlayer(assignedPlayer)
        } else {
            leader
        }
    }
    val updatedPool = playerPool.assignPlayer(target.name)
    val result = AuctionResult(target, AuctionResult.Outcome.Sold(bid.teamLeaderId, bid.amount))
    val updatedAuction = auction.addResult(result)

    val room = copy(
        teamLeaders = updatedLeaders,
        playerPool = updatedPool,
        progression = updatedAuction,
    )
    return room.checkCompletion()
}

private fun settleAsPassed(auction: Progression.Auction, target: Player): Room {
    val updatedPool = playerPool.moveCurrentToBack()
    val result = AuctionResult(target, AuctionResult.Outcome.Passed)
    val updatedAuction = auction.addResult(result)

    return copy(
        playerPool = updatedPool,
        progression = updatedAuction,
    )
}

private fun checkCompletion(): Room {
    val allFull = teamLeaders.all { it.hasPickedEnough(settings.picksPerTeam) }
    if (!allFull) return this

    val updatedPool = playerPool.markRemainingAsUnassigned()
    val teams = teamLeaders.map { leader -> Team(teamLeader = leader, members = leader.team) }

    return copy(
        status = RoomStatus.COMPLETED,
        playerPool = updatedPool,
        result = RoomResult(teams),
    )
}
```

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew :team-building:model:test --tests "*RoomAuctionTest*"
```
Expected: all tests pass.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: Room aggregate - 경매 로직 (입찰, 낙찰, 유찰)"
```

---

## Task 7: Room Aggregate — Draft Logic (픽)

**Files:**
- Modify: `team-building/model/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/Room.kt`
- Test: `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomDraftTest.kt`

- [ ] **Step 1: Write failing tests for draft logic**

Create `team-building/model/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/model/room/RoomDraftTest.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.model.room

import com.naminhyeok.fantazzk.teambuilding.model.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RoomDraftTest {
    private fun createStartedDraftRoom(): Room {
        val settings = RoomSettings(
            mode = TeamBuildingMode.DRAFT,
            teamCount = 2,
            teamSize = 2,
            draftOrderStrategy = DraftOrderStrategy.SNAKE,
        )
        val players = listOf(Player("선수1"), Player("선수2"))
        return Room.create(
            id = RoomId(1L),
            code = "ABC123",
            hostId = TeamLeaderId("host"),
            hostNickname = "호스트",
            settings = settings,
            playerPool = PlayerPool(players),
        ).addTeamLeader(TeamLeaderId("leader2"), "팀장2")
            .start()
    }

    @Test
    fun `pick assigns player to current turn team leader`() {
        val room = createStartedDraftRoom()
        val draft = room.progression as Progression.Draft
        val currentTurn = draft.currentTurn()

        val updated = room.pick(currentTurn, "선수1")
        val leader = updated.teamLeaders.first { it.id == currentTurn }
        assertEquals(1, leader.team.size)
        assertEquals("선수1", leader.team.first().name)
    }

    @Test
    fun `pick fails when it is not the team leader's turn`() {
        val room = createStartedDraftRoom()
        val draft = room.progression as Progression.Draft
        val notCurrentTurn = draft.pickOrder[1]

        assertThrows<IllegalStateException> {
            room.pick(notCurrentTurn, "선수1")
        }
    }

    @Test
    fun `pick fails when player is not available`() {
        val room = createStartedDraftRoom()
        val draft = room.progression as Progression.Draft

        assertThrows<IllegalArgumentException> {
            room.pick(draft.currentTurn(), "존재하지않는선수")
        }
    }

    @Test
    fun `draft completes when all picks are done`() {
        val room = createStartedDraftRoom()
        val draft = room.progression as Progression.Draft

        // Snake with 2 teams, picksPerTeam=1: [host, leader2]
        val result = room
            .pick(draft.pickOrder[0], "선수1")
            .pick(draft.pickOrder[1], "선수2")

        assertEquals(RoomStatus.COMPLETED, result.status)
        assertEquals(2, result.result?.teams?.size)
    }

    @Test
    fun `snake draft pick order alternates direction`() {
        val settings = RoomSettings(
            mode = TeamBuildingMode.DRAFT,
            teamCount = 2,
            teamSize = 3,
            draftOrderStrategy = DraftOrderStrategy.SNAKE,
        )
        val players = (1..4).map { Player("선수$it") }
        val room = Room.create(
            id = RoomId(1L),
            code = "XYZ789",
            hostId = TeamLeaderId("A"),
            hostNickname = "팀장A",
            settings = settings,
            playerPool = PlayerPool(players),
        ).addTeamLeader(TeamLeaderId("B"), "팀장B")
            .start()

        val draft = room.progression as Progression.Draft
        // picksPerTeam=2, snake: A, B, B, A
        assertEquals(listOf("A", "B", "B", "A"), draft.pickOrder.map { it.value })
    }
}
```

- [ ] **Step 2: Run tests — verify they fail**

```bash
./gradlew :team-building:model:test --tests "*RoomDraftTest*"
```
Expected: compilation failure (pick not defined).

- [ ] **Step 3: Add draft pick method to Room**

Add to `Room.kt`:
```kotlin
fun pick(teamLeaderId: TeamLeaderId, playerName: String): Room {
    check(status == RoomStatus.IN_PROGRESS) { "Room is not in progress" }
    val draft = progression as? Progression.Draft
        ?: error("Not in draft mode")

    check(draft.currentTurn() == teamLeaderId) { "Not your turn" }

    val target = playerPool.players.firstOrNull { it.name == playerName && it.status == PlayerStatus.AVAILABLE }
    requireNotNull(target) { "Player '$playerName' is not available" }

    val assignedPlayer = target.copy(status = PlayerStatus.ASSIGNED)
    val updatedLeaders = teamLeaders.map { leader ->
        if (leader.id == teamLeaderId) leader.addPlayer(assignedPlayer) else leader
    }
    val updatedPool = playerPool.assignPlayer(playerName)
    val updatedDraft = draft.addPick(Pick(teamLeaderId, target)).advanceTurn()

    val room = copy(
        teamLeaders = updatedLeaders,
        playerPool = updatedPool,
        progression = updatedDraft,
    )
    return room.checkCompletion()
}
```

- [ ] **Step 4: Run tests — verify they pass**

```bash
./gradlew :team-building:model:test --tests "*RoomDraftTest*"
```
Expected: all tests pass.

- [ ] **Step 5: Run all model tests**

```bash
./gradlew :team-building:model:test
```
Expected: all tests pass.

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat: Room aggregate - 드래프트 로직 (픽, 턴 관리)"
```

---

## Task 8: Domain Exceptions

**Files:**
- Create: `team-building/exception/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/exception/TeamBuildingException.kt`

- [ ] **Step 1: Create domain exception hierarchy**

Create `team-building/exception/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/exception/TeamBuildingException.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.exception

sealed class TeamBuildingException(message: String) : RuntimeException(message)

class TemplateNotFoundException(templateId: Long) :
    TeamBuildingException("Template not found: $templateId")

class RoomNotFoundException(code: String) :
    TeamBuildingException("Room not found: $code")

class RoomFullException(code: String) :
    TeamBuildingException("Room is full: $code")
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :team-building:exception:build
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat: TeamBuilding 도메인 예외 정의"
```

---

## Task 9: Infrastructure Ports

**Files:**
- Create: `team-building/infrastructure/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/infrastructure/TemplateRepository.kt`
- Create: `team-building/infrastructure/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/infrastructure/RoomRepository.kt`

- [ ] **Step 1: Define port interfaces**

Create `team-building/infrastructure/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/infrastructure/TemplateRepository.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.infrastructure

import com.naminhyeok.fantazzk.teambuilding.model.template.Template
import com.naminhyeok.fantazzk.teambuilding.model.template.TemplateId

interface TemplateRepository {
    fun save(template: Template): Template
    fun findById(id: TemplateId): Template?
    fun findAll(): List<Template>
}
```

Create `team-building/infrastructure/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/infrastructure/RoomRepository.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.infrastructure

import com.naminhyeok.fantazzk.teambuilding.model.room.Room
import com.naminhyeok.fantazzk.teambuilding.model.room.RoomId

interface RoomRepository {
    fun save(room: Room): Room
    fun findByCode(code: String): Room?
    fun findById(id: RoomId): Room?
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :team-building:infrastructure:build
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat: TeamBuilding 인프라스트럭처 포트 인터페이스 정의"
```

---

## Task 10: Service Layer

**Files:**
- Create: `team-building/service/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/service/TemplateService.kt`
- Create: `team-building/service/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/service/RoomService.kt`
- Create: `team-building/service/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/service/TeamBuildingServiceAutoConfiguration.kt`
- Create: `team-building/service/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Test: `team-building/service/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/service/TemplateServiceTest.kt`
- Test: `team-building/service/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/service/RoomServiceTest.kt`

- [ ] **Step 1: Write failing tests for TemplateService**

Create `team-building/service/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/service/TemplateServiceTest.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.service

import com.naminhyeok.fantazzk.teambuilding.exception.TemplateNotFoundException
import com.naminhyeok.fantazzk.teambuilding.infrastructure.TemplateRepository
import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.model.template.PlayerEntry
import com.naminhyeok.fantazzk.teambuilding.model.template.Rules
import com.naminhyeok.fantazzk.teambuilding.model.template.Template
import com.naminhyeok.fantazzk.teambuilding.model.template.TemplateId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TemplateServiceTest {
    private val repository = InMemoryTemplateRepository()
    private val service = TemplateService(repository)

    @Test
    fun `createTemplate saves and returns template`() {
        val template = service.createTemplate(
            name = "테스트 템플릿",
            mode = TeamBuildingMode.AUCTION,
            rules = Rules(teamCount = 5, teamSize = 5, budget = 300),
            players = listOf(PlayerEntry("선수1")),
        )

        assertEquals("테스트 템플릿", template.name)
        assertEquals(template, repository.findById(template.id))
    }

    @Test
    fun `getTemplate throws when not found`() {
        assertThrows<TemplateNotFoundException> {
            service.getTemplate(TemplateId(999L))
        }
    }

    private class InMemoryTemplateRepository : TemplateRepository {
        private val store = mutableMapOf<Long, Template>()
        private var seq = 1L

        override fun save(template: Template): Template {
            val saved = if (template.id.value == 0L) {
                template.copy(id = TemplateId(seq++))
            } else {
                template
            }
            store[saved.id.value] = saved
            return saved
        }

        override fun findById(id: TemplateId): Template? = store[id.value]
        override fun findAll(): List<Template> = store.values.toList()
    }
}
```

- [ ] **Step 2: Write failing tests for RoomService**

Create `team-building/service/src/test/kotlin/com/naminhyeok/fantazzk/teambuilding/service/RoomServiceTest.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.service

import com.naminhyeok.fantazzk.teambuilding.exception.RoomNotFoundException
import com.naminhyeok.fantazzk.teambuilding.infrastructure.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.model.room.Player
import com.naminhyeok.fantazzk.teambuilding.model.room.PlayerPool
import com.naminhyeok.fantazzk.teambuilding.model.room.Room
import com.naminhyeok.fantazzk.teambuilding.model.room.RoomId
import com.naminhyeok.fantazzk.teambuilding.model.room.RoomSettings
import com.naminhyeok.fantazzk.teambuilding.model.room.RoomStatus
import com.naminhyeok.fantazzk.teambuilding.model.template.PlayerEntry
import com.naminhyeok.fantazzk.teambuilding.model.template.Rules
import com.naminhyeok.fantazzk.teambuilding.model.template.Template
import com.naminhyeok.fantazzk.teambuilding.model.template.TemplateId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RoomServiceTest {
    private val roomRepository = InMemoryRoomRepository()
    private val service = RoomService(roomRepository)

    private val template = Template(
        id = TemplateId(1L),
        name = "테스트",
        mode = TeamBuildingMode.AUCTION,
        rules = Rules(teamCount = 2, teamSize = 2, budget = 300),
        players = listOf(PlayerEntry("선수1"), PlayerEntry("선수2")),
    )

    @Test
    fun `createRoom creates room from template`() {
        val room = service.createRoom(template, "호스트")

        assertEquals(RoomStatus.WAITING, room.status)
        assertEquals(1, room.teamLeaders.size)
        assertEquals("호스트", room.teamLeaders.first().nickname)
        assertEquals(6, room.code.length)
    }

    @Test
    fun `joinRoom adds team leader to room`() {
        val room = service.createRoom(template, "호스트")
        val updated = service.joinRoom(room.code, "팀장2")

        assertEquals(2, updated.teamLeaders.size)
    }

    @Test
    fun `joinRoom throws when room not found`() {
        assertThrows<RoomNotFoundException> {
            service.joinRoom("INVALID", "팀장")
        }
    }

    @Test
    fun `startRoom transitions room to IN_PROGRESS`() {
        val room = service.createRoom(template, "호스트")
        service.joinRoom(room.code, "팀장2")
        val started = service.startRoom(room.code)

        assertEquals(RoomStatus.IN_PROGRESS, started.status)
    }

    private class InMemoryRoomRepository : RoomRepository {
        private val store = mutableMapOf<String, Room>()
        private var seq = 1L

        override fun save(room: Room): Room {
            val saved = if (room.id.value == 0L) {
                room.copy(id = RoomId(seq++))
            } else {
                room
            }
            store[saved.code] = saved
            return saved
        }

        override fun findByCode(code: String): Room? = store[code]
        override fun findById(id: RoomId): Room? = store.values.firstOrNull { it.id == id }
    }
}
```

- [ ] **Step 3: Run tests — verify they fail**

```bash
./gradlew :team-building:service:test
```
Expected: compilation failure.

- [ ] **Step 4: Implement TemplateService**

Create `team-building/service/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/service/TemplateService.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.service

import com.naminhyeok.fantazzk.teambuilding.exception.TemplateNotFoundException
import com.naminhyeok.fantazzk.teambuilding.infrastructure.TemplateRepository
import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.model.template.PlayerEntry
import com.naminhyeok.fantazzk.teambuilding.model.template.Rules
import com.naminhyeok.fantazzk.teambuilding.model.template.Template
import com.naminhyeok.fantazzk.teambuilding.model.template.TemplateId

class TemplateService(private val templateRepository: TemplateRepository) {

    fun createTemplate(
        name: String,
        mode: TeamBuildingMode,
        rules: Rules,
        players: List<PlayerEntry>,
    ): Template {
        val template = Template(
            id = TemplateId(0L),
            name = name,
            mode = mode,
            rules = rules,
            players = players,
        )
        return templateRepository.save(template)
    }

    fun getTemplate(id: TemplateId): Template =
        templateRepository.findById(id) ?: throw TemplateNotFoundException(id.value)

    fun listTemplates(): List<Template> = templateRepository.findAll()
}
```

- [ ] **Step 5: Implement RoomService**

Create `team-building/service/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/service/RoomService.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.service

import com.naminhyeok.fantazzk.teambuilding.exception.RoomNotFoundException
import com.naminhyeok.fantazzk.teambuilding.infrastructure.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.model.room.Player
import com.naminhyeok.fantazzk.teambuilding.model.room.PlayerPool
import com.naminhyeok.fantazzk.teambuilding.model.room.Room
import com.naminhyeok.fantazzk.teambuilding.model.room.RoomId
import com.naminhyeok.fantazzk.teambuilding.model.room.RoomSettings
import com.naminhyeok.fantazzk.teambuilding.model.room.TeamLeaderId
import com.naminhyeok.fantazzk.teambuilding.model.template.Template
import java.util.UUID

class RoomService(private val roomRepository: RoomRepository) {

    fun createRoom(template: Template, hostNickname: String): Room {
        val settings = RoomSettings(
            mode = template.mode,
            teamCount = template.rules.teamCount,
            teamSize = template.rules.teamSize,
            budget = template.rules.budget,
            draftOrderStrategy = template.rules.draftOrderStrategy,
        )
        val playerPool = PlayerPool(
            players = template.players.map { Player(name = it.name, metadata = it.metadata) },
        )
        val room = Room.create(
            id = RoomId(0L),
            code = generateCode(),
            hostId = TeamLeaderId(UUID.randomUUID().toString()),
            hostNickname = hostNickname,
            settings = settings,
            playerPool = playerPool,
        )
        return roomRepository.save(room)
    }

    fun joinRoom(code: String, nickname: String): Room {
        val room = getRoom(code)
        val updated = room.addTeamLeader(TeamLeaderId(UUID.randomUUID().toString()), nickname)
        return roomRepository.save(updated)
    }

    fun startRoom(code: String): Room {
        val room = getRoom(code)
        val started = room.start()
        return roomRepository.save(started)
    }

    fun placeBid(code: String, teamLeaderId: TeamLeaderId, amount: Int): Room {
        val room = getRoom(code)
        val updated = room.placeBid(teamLeaderId, amount)
        return roomRepository.save(updated)
    }

    fun settleAuction(code: String): Room {
        val room = getRoom(code)
        val updated = room.settleCurrentAuction()
        return roomRepository.save(updated)
    }

    fun pick(code: String, teamLeaderId: TeamLeaderId, playerName: String): Room {
        val room = getRoom(code)
        val updated = room.pick(teamLeaderId, playerName)
        return roomRepository.save(updated)
    }

    fun getRoom(code: String): Room =
        roomRepository.findByCode(code) ?: throw RoomNotFoundException(code)

    private fun generateCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
```

- [ ] **Step 6: Create AutoConfiguration and register**

Create `team-building/service/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/service/TeamBuildingServiceAutoConfiguration.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.service

import com.naminhyeok.fantazzk.teambuilding.infrastructure.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.infrastructure.TemplateRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class TeamBuildingServiceAutoConfiguration {

    @Bean
    fun templateService(templateRepository: TemplateRepository): TemplateService =
        TemplateService(templateRepository)

    @Bean
    fun roomService(roomRepository: RoomRepository): RoomService =
        RoomService(roomRepository)
}
```

Create `team-building/service/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:
```
com.naminhyeok.fantazzk.teambuilding.service.TeamBuildingServiceAutoConfiguration
```

- [ ] **Step 7: Run tests — verify they pass**

```bash
./gradlew :team-building:service:test
```
Expected: all tests pass.

- [ ] **Step 8: Commit**

```bash
git add -A && git commit -m "feat: TeamBuilding 서비스 레이어 구현"
```

---

## Task 11: Liquibase Schema

**Files:**
- Create: `team-building/schema/src/main/resources/db/changelog/db.changelog-master.yaml`
- Create: `team-building/schema/src/main/resources/db/changelog/001-create-team-building-tables.sql`

- [ ] **Step 1: Create changelog master**

Create `team-building/schema/src/main/resources/db/changelog/db.changelog-master.yaml`:
```yaml
databaseChangeLog:
  - include:
      file: db/changelog/001-create-team-building-tables.sql
      relativeToChangelogFile: false
```

- [ ] **Step 2: Create migration script**

Create `team-building/schema/src/main/resources/db/changelog/001-create-team-building-tables.sql`:
```sql
--liquibase formatted sql

--changeset team-building:001-1
CREATE TABLE template (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255)    NOT NULL,
    mode       VARCHAR(20)     NOT NULL,
    rules_json JSON            NOT NULL,
    players_json JSON          NOT NULL,
    created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--changeset team-building:001-2
CREATE TABLE room (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(6)   NOT NULL UNIQUE,
    host_id          VARCHAR(36)  NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    settings_json    JSON         NOT NULL,
    player_pool_json JSON         NOT NULL,
    team_leaders_json JSON        NOT NULL,
    progression_json JSON         NULL,
    result_json      JSON         NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

--changeset team-building:001-3
CREATE INDEX idx_room_code ON room (code);
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat: TeamBuilding Liquibase 스키마 마이그레이션"
```

---

## Task 12: Repository JDBC

**Files:**
- Create: `team-building/repository-jdbc/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/repository/TemplateJdbcRepository.kt`
- Create: `team-building/repository-jdbc/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/repository/RoomJdbcRepository.kt`
- Create: `team-building/repository-jdbc/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/repository/TeamBuildingRepositoryAutoConfiguration.kt`
- Create: `team-building/repository-jdbc/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

- [ ] **Step 1: Implement TemplateJdbcRepository**

Create `team-building/repository-jdbc/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/repository/TemplateJdbcRepository.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.naminhyeok.fantazzk.teambuilding.infrastructure.TemplateRepository
import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.model.template.PlayerEntry
import com.naminhyeok.fantazzk.teambuilding.model.template.Rules
import com.naminhyeok.fantazzk.teambuilding.model.template.Template
import com.naminhyeok.fantazzk.teambuilding.model.template.TemplateId
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder

class TemplateJdbcRepository(
    private val jdbcClient: JdbcClient,
    private val objectMapper: ObjectMapper,
) : TemplateRepository {

    private val rowMapper = RowMapper { rs, _ ->
        Template(
            id = TemplateId(rs.getLong("id")),
            name = rs.getString("name"),
            mode = TeamBuildingMode.valueOf(rs.getString("mode")),
            rules = objectMapper.readValue<Rules>(rs.getString("rules_json")),
            players = objectMapper.readValue<List<PlayerEntry>>(rs.getString("players_json")),
        )
    }

    override fun save(template: Template): Template {
        if (template.id.value == 0L) {
            val keyHolder = GeneratedKeyHolder()
            jdbcClient.sql(
                """
                INSERT INTO template (name, mode, rules_json, players_json)
                VALUES (:name, :mode, :rulesJson, :playersJson)
                """,
            )
                .param("name", template.name)
                .param("mode", template.mode.name)
                .param("rulesJson", objectMapper.writeValueAsString(template.rules))
                .param("playersJson", objectMapper.writeValueAsString(template.players))
                .update(keyHolder)
            return template.copy(id = TemplateId(keyHolder.key!!.toLong()))
        }
        jdbcClient.sql(
            """
            UPDATE template SET name = :name, mode = :mode, rules_json = :rulesJson, players_json = :playersJson
            WHERE id = :id
            """,
        )
            .param("id", template.id.value)
            .param("name", template.name)
            .param("mode", template.mode.name)
            .param("rulesJson", objectMapper.writeValueAsString(template.rules))
            .param("playersJson", objectMapper.writeValueAsString(template.players))
            .update()
        return template
    }

    override fun findById(id: TemplateId): Template? =
        jdbcClient.sql("SELECT * FROM template WHERE id = :id")
            .param("id", id.value)
            .query(rowMapper)
            .optional()
            .orElse(null)

    override fun findAll(): List<Template> =
        jdbcClient.sql("SELECT * FROM template ORDER BY id")
            .query(rowMapper)
            .list()
}
```

- [ ] **Step 2: Implement RoomJdbcRepository**

Create `team-building/repository-jdbc/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/repository/RoomJdbcRepository.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.naminhyeok.fantazzk.teambuilding.infrastructure.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.model.room.PlayerPool
import com.naminhyeok.fantazzk.teambuilding.model.room.Progression
import com.naminhyeok.fantazzk.teambuilding.model.room.Room
import com.naminhyeok.fantazzk.teambuilding.model.room.RoomId
import com.naminhyeok.fantazzk.teambuilding.model.room.RoomResult
import com.naminhyeok.fantazzk.teambuilding.model.room.RoomSettings
import com.naminhyeok.fantazzk.teambuilding.model.room.RoomStatus
import com.naminhyeok.fantazzk.teambuilding.model.room.TeamLeader
import com.naminhyeok.fantazzk.teambuilding.model.room.TeamLeaderId
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder

class RoomJdbcRepository(
    private val jdbcClient: JdbcClient,
    private val objectMapper: ObjectMapper,
) : RoomRepository {

    private val rowMapper = RowMapper { rs, _ ->
        Room(
            id = RoomId(rs.getLong("id")),
            code = rs.getString("code"),
            hostId = TeamLeaderId(rs.getString("host_id")),
            status = RoomStatus.valueOf(rs.getString("status")),
            settings = objectMapper.readValue<RoomSettings>(rs.getString("settings_json")),
            playerPool = objectMapper.readValue<PlayerPool>(rs.getString("player_pool_json")),
            teamLeaders = objectMapper.readValue<List<TeamLeader>>(rs.getString("team_leaders_json")),
            progression = rs.getString("progression_json")?.let { objectMapper.readValue<Progression>(it) },
            result = rs.getString("result_json")?.let { objectMapper.readValue<RoomResult>(it) },
        )
    }

    override fun save(room: Room): Room {
        if (room.id.value == 0L) {
            val keyHolder = GeneratedKeyHolder()
            jdbcClient.sql(
                """
                INSERT INTO room (code, host_id, status, settings_json, player_pool_json, team_leaders_json, progression_json, result_json)
                VALUES (:code, :hostId, :status, :settingsJson, :playerPoolJson, :teamLeadersJson, :progressionJson, :resultJson)
                """,
            )
                .param("code", room.code)
                .param("hostId", room.hostId.value)
                .param("status", room.status.name)
                .param("settingsJson", objectMapper.writeValueAsString(room.settings))
                .param("playerPoolJson", objectMapper.writeValueAsString(room.playerPool))
                .param("teamLeadersJson", objectMapper.writeValueAsString(room.teamLeaders))
                .param("progressionJson", room.progression?.let { objectMapper.writeValueAsString(it) })
                .param("resultJson", room.result?.let { objectMapper.writeValueAsString(it) })
                .update(keyHolder)
            return room.copy(id = RoomId(keyHolder.key!!.toLong()))
        }
        jdbcClient.sql(
            """
            UPDATE room SET status = :status, settings_json = :settingsJson, player_pool_json = :playerPoolJson,
                team_leaders_json = :teamLeadersJson, progression_json = :progressionJson, result_json = :resultJson
            WHERE id = :id
            """,
        )
            .param("id", room.id.value)
            .param("status", room.status.name)
            .param("settingsJson", objectMapper.writeValueAsString(room.settings))
            .param("playerPoolJson", objectMapper.writeValueAsString(room.playerPool))
            .param("teamLeadersJson", objectMapper.writeValueAsString(room.teamLeaders))
            .param("progressionJson", room.progression?.let { objectMapper.writeValueAsString(it) })
            .param("resultJson", room.result?.let { objectMapper.writeValueAsString(it) })
            .update()
        return room
    }

    override fun findByCode(code: String): Room? =
        jdbcClient.sql("SELECT * FROM room WHERE code = :code")
            .param("code", code)
            .query(rowMapper)
            .optional()
            .orElse(null)

    override fun findById(id: RoomId): Room? =
        jdbcClient.sql("SELECT * FROM room WHERE id = :id")
            .param("id", id.value)
            .query(rowMapper)
            .optional()
            .orElse(null)
}
```

- [ ] **Step 3: Create AutoConfiguration and register**

Create `team-building/repository-jdbc/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/repository/TeamBuildingRepositoryAutoConfiguration.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.naminhyeok.fantazzk.teambuilding.infrastructure.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.infrastructure.TemplateRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.simple.JdbcClient

@AutoConfiguration
class TeamBuildingRepositoryAutoConfiguration {

    @Bean
    fun templateRepository(jdbcClient: JdbcClient, objectMapper: ObjectMapper): TemplateRepository =
        TemplateJdbcRepository(jdbcClient, objectMapper)

    @Bean
    fun roomRepository(jdbcClient: JdbcClient, objectMapper: ObjectMapper): RoomRepository =
        RoomJdbcRepository(jdbcClient, objectMapper)
}
```

Create `team-building/repository-jdbc/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:
```
com.naminhyeok.fantazzk.teambuilding.repository.TeamBuildingRepositoryAutoConfiguration
```

- [ ] **Step 4: Verify compilation**

```bash
./gradlew :team-building:repository-jdbc:build
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: TeamBuilding Repository JDBC 구현"
```

---

## Task 13: API Layer

**Files:**
- Create: `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/dto/TemplateRequest.kt`
- Create: `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/dto/TemplateResponse.kt`
- Create: `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/dto/RoomRequest.kt`
- Create: `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/dto/RoomResponse.kt`
- Create: `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/TemplateController.kt`
- Create: `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/RoomController.kt`
- Create: `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/TeamBuildingApiAutoConfiguration.kt`
- Create: `team-building/api/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Create: `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/TeamBuildingExceptionHandler.kt`

- [ ] **Step 1: Create DTOs**

Create `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/dto/TemplateRequest.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.api.dto

import com.naminhyeok.fantazzk.teambuilding.model.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode

data class CreateTemplateRequest(
    val name: String,
    val mode: TeamBuildingMode,
    val teamCount: Int,
    val teamSize: Int,
    val budget: Int? = null,
    val draftOrderStrategy: DraftOrderStrategy? = null,
    val players: List<PlayerEntryRequest>,
)

data class PlayerEntryRequest(
    val name: String,
    val metadata: Map<String, String> = emptyMap(),
)
```

Create `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/dto/TemplateResponse.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.api.dto

import com.naminhyeok.fantazzk.teambuilding.model.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.model.template.Template

data class TemplateResponse(
    val id: Long,
    val name: String,
    val mode: TeamBuildingMode,
    val teamCount: Int,
    val teamSize: Int,
    val playerCount: Int,
) {
    companion object {
        fun from(template: Template): TemplateResponse = TemplateResponse(
            id = template.id.value,
            name = template.name,
            mode = template.mode,
            teamCount = template.rules.teamCount,
            teamSize = template.rules.teamSize,
            playerCount = template.players.size,
        )
    }
}
```

Create `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/dto/RoomRequest.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.api.dto

data class CreateRoomRequest(
    val templateId: Long,
    val hostNickname: String,
)

data class JoinRoomRequest(
    val nickname: String,
)

data class PlaceBidRequest(
    val teamLeaderId: String,
    val amount: Int,
)

data class PickRequest(
    val teamLeaderId: String,
    val playerName: String,
)
```

Create `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/dto/RoomResponse.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.api.dto

import com.naminhyeok.fantazzk.teambuilding.model.room.Room
import com.naminhyeok.fantazzk.teambuilding.model.room.RoomStatus

data class RoomResponse(
    val code: String,
    val status: RoomStatus,
    val teamLeaders: List<TeamLeaderResponse>,
    val currentTarget: String?,
    val result: List<TeamResponse>?,
)

data class TeamLeaderResponse(
    val id: String,
    val nickname: String,
    val remainingBudget: Int?,
    val teamSize: Int,
)

data class TeamResponse(
    val leaderNickname: String,
    val members: List<String>,
)

fun Room.toResponse(): RoomResponse = RoomResponse(
    code = code,
    status = status,
    teamLeaders = teamLeaders.map {
        TeamLeaderResponse(
            id = it.id.value,
            nickname = it.nickname,
            remainingBudget = it.remainingBudget,
            teamSize = it.team.size,
        )
    },
    currentTarget = playerPool.currentTarget()?.name,
    result = result?.teams?.map { team ->
        TeamResponse(
            leaderNickname = team.teamLeader.nickname,
            members = team.members.map { it.name },
        )
    },
)
```

- [ ] **Step 2: Implement controllers**

Create `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/TemplateController.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.api

import com.naminhyeok.fantazzk.teambuilding.api.dto.CreateTemplateRequest
import com.naminhyeok.fantazzk.teambuilding.api.dto.TemplateResponse
import com.naminhyeok.fantazzk.teambuilding.model.template.PlayerEntry
import com.naminhyeok.fantazzk.teambuilding.model.template.Rules
import com.naminhyeok.fantazzk.teambuilding.model.template.TemplateId
import com.naminhyeok.fantazzk.teambuilding.service.TemplateService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/templates")
class TemplateController(private val templateService: TemplateService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateTemplateRequest): TemplateResponse {
        val rules = Rules(
            teamCount = request.teamCount,
            teamSize = request.teamSize,
            budget = request.budget,
            draftOrderStrategy = request.draftOrderStrategy,
        )
        val players = request.players.map { PlayerEntry(it.name, it.metadata) }
        val template = templateService.createTemplate(request.name, request.mode, rules, players)
        return TemplateResponse.from(template)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): TemplateResponse =
        TemplateResponse.from(templateService.getTemplate(TemplateId(id)))

    @GetMapping
    fun list(): List<TemplateResponse> =
        templateService.listTemplates().map(TemplateResponse::from)
}
```

Create `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/RoomController.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.api

import com.naminhyeok.fantazzk.teambuilding.api.dto.CreateRoomRequest
import com.naminhyeok.fantazzk.teambuilding.api.dto.JoinRoomRequest
import com.naminhyeok.fantazzk.teambuilding.api.dto.PickRequest
import com.naminhyeok.fantazzk.teambuilding.api.dto.PlaceBidRequest
import com.naminhyeok.fantazzk.teambuilding.api.dto.RoomResponse
import com.naminhyeok.fantazzk.teambuilding.api.dto.toResponse
import com.naminhyeok.fantazzk.teambuilding.model.room.TeamLeaderId
import com.naminhyeok.fantazzk.teambuilding.model.template.TemplateId
import com.naminhyeok.fantazzk.teambuilding.service.RoomService
import com.naminhyeok.fantazzk.teambuilding.service.TemplateService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/rooms")
class RoomController(
    private val roomService: RoomService,
    private val templateService: TemplateService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateRoomRequest): RoomResponse {
        val template = templateService.getTemplate(TemplateId(request.templateId))
        return roomService.createRoom(template, request.hostNickname).toResponse()
    }

    @GetMapping("/{code}")
    fun getByCode(@PathVariable code: String): RoomResponse =
        roomService.getRoom(code).toResponse()

    @PostMapping("/{code}/join")
    fun join(@PathVariable code: String, @RequestBody request: JoinRoomRequest): RoomResponse =
        roomService.joinRoom(code, request.nickname).toResponse()

    @PostMapping("/{code}/start")
    fun start(@PathVariable code: String): RoomResponse =
        roomService.startRoom(code).toResponse()

    @PostMapping("/{code}/bid")
    fun placeBid(@PathVariable code: String, @RequestBody request: PlaceBidRequest): RoomResponse =
        roomService.placeBid(code, TeamLeaderId(request.teamLeaderId), request.amount).toResponse()

    @PostMapping("/{code}/settle")
    fun settle(@PathVariable code: String): RoomResponse =
        roomService.settleAuction(code).toResponse()

    @PostMapping("/{code}/pick")
    fun pick(@PathVariable code: String, @RequestBody request: PickRequest): RoomResponse =
        roomService.pick(code, TeamLeaderId(request.teamLeaderId), request.playerName).toResponse()
}
```

- [ ] **Step 3: Create exception handler**

Create `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/TeamBuildingExceptionHandler.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.api

import com.naminhyeok.fantazzk.teambuilding.exception.RoomFullException
import com.naminhyeok.fantazzk.teambuilding.exception.RoomNotFoundException
import com.naminhyeok.fantazzk.teambuilding.exception.TeamBuildingException
import com.naminhyeok.fantazzk.teambuilding.exception.TemplateNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackageClasses = [TeamBuildingExceptionHandler::class])
class TeamBuildingExceptionHandler {

    @ExceptionHandler(TemplateNotFoundException::class, RoomNotFoundException::class)
    fun handleNotFound(ex: TeamBuildingException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Not found")

    @ExceptionHandler(RoomFullException::class)
    fun handleConflict(ex: TeamBuildingException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message ?: "Conflict")

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message ?: "Invalid state")
}
```

- [ ] **Step 4: Create AutoConfiguration and register**

Create `team-building/api/src/main/kotlin/com/naminhyeok/fantazzk/teambuilding/api/TeamBuildingApiAutoConfiguration.kt`:
```kotlin
package com.naminhyeok.fantazzk.teambuilding.api

import com.naminhyeok.fantazzk.teambuilding.service.RoomService
import com.naminhyeok.fantazzk.teambuilding.service.TemplateService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class TeamBuildingApiAutoConfiguration {

    @Bean
    fun templateController(templateService: TemplateService): TemplateController =
        TemplateController(templateService)

    @Bean
    fun roomController(roomService: RoomService, templateService: TemplateService): RoomController =
        RoomController(roomService, templateService)

    @Bean
    fun teamBuildingExceptionHandler(): TeamBuildingExceptionHandler =
        TeamBuildingExceptionHandler()
}
```

Create `team-building/api/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:
```
com.naminhyeok.fantazzk.teambuilding.api.TeamBuildingApiAutoConfiguration
```

- [ ] **Step 5: Verify compilation**

```bash
./gradlew :team-building:api:build
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat: TeamBuilding API 레이어 구현"
```

---

## Task 14: Application Wiring & Smoke Test

**Files:**
- Modify: `application-api/build.gradle.kts` (already done in Task 1)
- Test: `application-api/src/integrationTest/kotlin/com/naminhyeok/fantazzk/bootstrap/root/TeamBuildingSmokeTest.kt`

- [ ] **Step 1: Verify application-api dependencies include team-building modules**

Check `application-api/build.gradle.kts` includes:
```kotlin
implementation(project(":team-building:api"))
implementation(project(":team-building:repository-jdbc"))
implementation(project(":team-building:schema"))
```

- [ ] **Step 2: Write smoke test**

Create `application-api/src/integrationTest/kotlin/com/naminhyeok/fantazzk/bootstrap/root/TeamBuildingSmokeTest.kt`:
```kotlin
package com.naminhyeok.fantazzk.bootstrap.root

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class TeamBuildingSmokeTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `create template and room, then run full auction flow`() {
        // 1. 템플릿 생성
        val templateResult = mockMvc.post("/api/v1/templates") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "name": "테스트 경매",
                    "mode": "AUCTION",
                    "teamCount": 2,
                    "teamSize": 2,
                    "budget": 300,
                    "players": [
                        {"name": "선수1"},
                        {"name": "선수2"}
                    ]
                }
            """.trimIndent()
        }.andExpect { status { isCreated() } }
            .andReturn()

        val templateId = templateResult.response.contentAsString
            .let { Regex(""""id":(\d+)""").find(it)!!.groupValues[1] }

        // 2. 방 생성
        val roomResult = mockMvc.post("/api/v1/rooms") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"templateId": $templateId, "hostNickname": "호스트"}"""
        }.andExpect { status { isCreated() } }
            .andReturn()

        val code = roomResult.response.contentAsString
            .let { Regex(""""code":"(\w+)"""").find(it)!!.groupValues[1] }

        // 3. 방 조회
        mockMvc.get("/api/v1/rooms/$code")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("WAITING") }
            }
    }
}
```

- [ ] **Step 3: Run smoke test**

```bash
./gradlew :application-api:integrationTest --tests "*TeamBuildingSmokeTest*"
```
Expected: test passes.

- [ ] **Step 4: Run full build**

```bash
./gradlew build
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: TeamBuilding 통합 테스트 및 application 와이어링"
```
