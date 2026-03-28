# TeamBuilding 도메인 설계

## 개요

게임 대회(자낳대, 치지직컵 등)에서 팀 빌딩 시 사용하는 모의경매/모의드래프트 플랫폼.
팬들이 재미로 모의경매, 모의드래프트를 진행할 수 있다.

## 유비쿼터스 언어

| 용어 | 의미 |
|------|------|
| 방 (Room) | 팀 빌딩이 진행되는 세션 단위 |
| 호스트 (Host) | 방을 만들고 진행하는 사람. 팀장 역할 겸임 |
| 팀장 (TeamLeader) | 선수를 픽/입찰할 권한이 있는 선수 |
| 선수 (Player) | 풀에 있는 모든 사람. 팀장도 선수의 일종 |
| 선수 풀 (PlayerPool) | 방에서 사용되는 선수 목록 |
| 템플릿 (Template) | 방을 만들기 위한 블루프린트 (규칙 + 선수 풀) |
| 경매 (Auction) | 팀장들이 입찰로 선수를 확보하는 방식 |
| 입찰 (Bid) | 경매에서 가격을 부르는 행위 |
| 낙찰 (Sold) | 최고 입찰자에게 선수가 확정됨 |
| 유찰 (Passed) | 아무도 입찰하지 않아 선수가 풀 맨 뒤로 이동 |
| 예산 (Budget) | 팀장이 경매에서 사용할 수 있는 총 포인트 |
| 드래프트 (Draft) | 팀장들이 순서대로 선수를 픽하는 방식 |
| 픽 (Pick) | 드래프트에서 선수를 선택하는 행위 |

## 바운디드 컨텍스트

단일 바운디드 컨텍스트 **TeamBuilding**으로 시작한다.

에릭 에반스의 원칙에 따라 언어적 경계가 발견되기 전까지는 분리하지 않는다.
선수(Player)가 독자적 라이프사이클(프로필, 전적, 통계 등)을 갖게 되면 별도 바운디드 컨텍스트로 분리를 검토한다.

```
team-building/          <- 바운디드 컨텍스트
├── model/
├── exception/
├── schema/
├── infrastructure/
├── service/
├── repository-jdbc/
├── api/
└── application-api/
```

## Aggregate 설계

### Aggregate 1: 템플릿 (Template)

방을 만들기 위한 블루프린트. 여러 방에서 재사용 가능하다.

```
Template (Aggregate Root, Entity)
├── templateId: TemplateId
├── name: String
├── mode: TeamBuildingMode (AUCTION / DRAFT)
├── rules: Rules (Value Object)
│   ├── teamCount: Int
│   ├── teamSize: Int
│   ├── budget: Int? (경매 시에만)
│   └── draftOrder: DraftOrderStrategy? (SNAKE / FIXED, 드래프트 시에만)
└── players: List<PlayerEntry> (Value Object)
    ├── name: String
    └── metadata: Map
```

### Aggregate 2: 방 (Room) - Core Aggregate Root

팀 빌딩의 전체 라이프사이클을 소유한다.

```
Room (Aggregate Root, Entity)
├── roomId: RoomId
├── hostId: ParticipantId
├── status: RoomStatus (WAITING / IN_PROGRESS / COMPLETED)
├── settings: RoomSettings (Value Object, 템플릿에서 복사)
│   ├── mode: TeamBuildingMode
│   ├── teamCount: Int
│   ├── teamSize: Int
│   └── budget: Int?
│
├── playerPool: PlayerPool (Value Object)
│   └── players: List<Player> (순서 있음)
│       ├── name: String
│       ├── status: AVAILABLE / ASSIGNED / UNASSIGNED
│       └── metadata: Map
│
├── teamLeaders: List<TeamLeader> (Entity)
│   ├── teamLeaderId: TeamLeaderId
│   ├── nickname: String
│   ├── remainingBudget: Int? (경매 시)
│   └── team: List<Player>
│
├── progression: Progression (Entity, sealed)
│   │
│   ├── AuctionProgression
│   │   ├── currentPlayerIndex: Int
│   │   ├── currentBids: List<Bid> (Value Object)
│   │   │   ├── teamLeaderId: TeamLeaderId
│   │   │   └── amount: Int
│   │   └── history: List<AuctionResult> (Value Object)
│   │       ├── player: Player
│   │       └── result: SOLD(teamLeaderId, amount) / PASSED
│   │
│   └── DraftProgression
│       ├── pickOrder: List<TeamLeaderId>
│       ├── currentTurnIndex: Int
│       └── history: List<Pick> (Value Object)
│           ├── teamLeaderId: TeamLeaderId
│           └── player: Player
│
└── result: RoomResult? (완료 시)
    └── teams: List<Team> (Value Object)
        ├── teamLeader: TeamLeader
        └── members: List<Player>
```

## 방 상태 머신

```
WAITING (대기) --> IN_PROGRESS (진행 중) --> COMPLETED (완료)
```

### 전이 조건

- WAITING -> IN_PROGRESS: 호스트가 팀장 배정 완료, 선수 풀 확정
- IN_PROGRESS -> COMPLETED: 모든 팀이 정원을 채우면 종료

## 팀 빌딩 진행 흐름

### 경매

1. 선수 풀에서 순서대로 경매 대상 등장
2. 팀장들이 실시간으로 입찰 경쟁 (가격을 올려가며)
3. 추가 입찰 없으면 판정
   - 입찰자 있음: 낙찰 (해당 팀장 팀에 배정, 예산 차감)
   - 입찰자 없음: 유찰 (선수 풀 맨 뒤로 이동)
4. 모든 팀 정원 충족 시 완료 (남은 선수는 미배정)

### 드래프트

1. 픽 순서 결정 (스네이크 / 고정순서, 확장 가능)
2. 현재 턴의 팀장이 선수 풀에서 반드시 픽 (스킵 불가)
3. 다음 턴으로 이동
4. 모든 팀 정원 충족 시 완료 (남은 선수는 미배정)

## 핵심 설계 결정

### 템플릿 -> 방 복사 (스냅샷)

방 생성 시 템플릿의 설정과 선수 풀을 복사한다.
참조가 아닌 스냅샷이므로, 템플릿 수정이 진행 중인 방에 영향을 주지 않는다.

### Player는 Value Object

현재 선수는 이름 + 메타데이터일 뿐 시스템 전체 식별자가 불필요하다.
선수가 프로필, 전적, 통계 등 독자적 라이프사이클을 갖게 되면 Entity로 승격하고 별도 바운디드 컨텍스트 분리를 검토한다.

### TeamLeader는 Entity

방 안에서 식별성이 필요하고, 예산과 팀 상태가 변경된다.
팀장도 선수의 일종이지만, 방 내에서 픽/입찰 권한이라는 역할을 가진다.

### Progression은 sealed class 다형성

경매와 드래프트는 진행 상태의 구조 자체가 다르다.
하나로 통합하면 nullable 필드가 난무하므로, sealed class로 분리하여 각 모드가 자신만의 상태를 명확히 소유한다.

## 인증

초기에는 비로그인(게스트) 방식으로 시작한다.
방 참가 시 닉네임만 입력하면 된다.

## 향후 확장 포인트

- 선수(Player)에 식별성이 필요해지면 별도 바운디드 컨텍스트로 분리
- 드래프트 순서 전략 추가 (스네이크/고정순서 외 커스텀)
- 팀 빌딩 방식 추가 (경매/드래프트 외 새로운 방식)
- 회원가입/로그인 도입
