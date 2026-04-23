# 🦅 Eagle Nations — Base Mod Plan

A server-focused political + territory system built on top of FTB Teams, turning teams into fully functional nations with land control, government, and diplomacy.

---

# 🧭 1. Core Vision

**Eagle Nations = “Countries inside Minecraft servers”**

Players don’t just join teams—they form:

* 🏛 Nations (FTB Teams-based)
* 🗺 Territories (claimed land)
* ⚖ Governments (ranks + laws)
* 🤝 Diplomacy (alliances, wars, treaties)

Everything is **server-authoritative**, meaning:

* No client-side trust
* Admin-configurable rules
* Works in any SMP or modpack

---

# 🧱 2. Core Systems Overview

## 🏛 Nation System (Foundation)

Every FTB Team becomes a Nation wrapper.

### Responsibilities:

* Store nation metadata
* Link to FTB Team UUID
* Manage members, ranks, treasury

### Core data:

* Name
* Leader UUID
* Members list
* Capital chunk
* Nation color / banner
* Treasury (optional currency hook)

---

## 🗺 Territory System (Land Control)

### Chunk-based claims:

* Nations claim chunks as territory
* Each chunk has permission rules

### Rules per chunk:

* Build allowed?
* PvP allowed?
* Interact allowed?
* Container access?

### Expansion rules:

* Claim cost increases over distance from capital
* Optional upkeep tax per chunk

---

## 👥 Government System

Each nation has roles + permissions:

### Default ranks:

* 👑 Leader (full control)
* 🏛 Officer (manage land, diplomacy)
* 🧑 Citizen (basic access)
* 🚫 Exiled (no permissions)

### Features:

* Rank permissions system
* Role editing UI
* Optional elections (config toggle)

---

## 🤝 Diplomacy System

Nation relationships:

* Neutral
* Allied
* Hostile
* War

### Features:

* Alliance system
* War declarations
* Peace treaties
* Optional trade agreements

### War mechanics (MVP):

* PvP enabled in enemy territory
* Capture chunks system
* War cooldowns

---

## ⚖ Law System (Server Roleplay Layer)

Each nation can define rules:

### Examples:

* PvP allowed in borders
* Entry permission for outsiders
* Tax rates
* Restricted items (optional modpack feature)

### Enforcement:

* Hook into server events:

  * block break
  * interaction
  * combat
* Cancel or allow actions based on law + permissions

---

## 💰 Economy Layer (Optional Integration)

Designed to work with external currency systems (like Lightman-style economy mods)

### Features:

* Nation treasury
* Taxes:

  * income tax
  * land tax
  * trade tax
* War funding system

---

## 🧠 Event Engine (Core Logic Layer)

This is the “brain” of Eagle Nations.

### Hooks into:

* PlayerJoinEvent
* BlockBreakEvent
* EntityDamageEvent
* ChunkClaimEvent (custom)
* Command events

### Logic flow:

```
Event happens
→ check chunk ownership
→ check nation permissions
→ apply law rules
→ allow / deny / modify outcome
```

---

## 🧩 FTB Teams Integration Layer

This is critical.

### Responsibilities:

* Detect team creation → create nation
* Team join/leave → update nation members
* Team disband → destroy nation or archive it (config)

---

## 🧭 Commands System

### Core commands:

* `/nation create <name>`
* `/nation info`
* `/nation claim`
* `/nation unclaim`
* `/nation rank set`
* `/nation diplomacy`
* `/nation war declare`

---

## 🖥 UI Systems (Client Side)

### Screens:

* Nation overview dashboard
* Territory map viewer
* Diplomacy panel
* Government management screen

### Optional item:

* “Nation Tablet” (opens UI)

---

# 🧱 3. Mod Structure (NeoForge 1.21.1)

```id="8v5m8q"
eaglenations/
 ├── core/
 │    ├── EagleNations.java (main mod class)
 │    ├── config/
 │    └── registry/
 │
 ├── ftb/
 │    └── FTBTeamBridge.java
 │
 ├── nation/
 │    ├── Nation.java
 │    ├── NationManager.java
 │    ├── NationData.java
 │
 ├── territory/
 │    ├── ChunkClaimManager.java
 │    ├── TerritoryData.java
 │
 ├── politics/
 │    ├── RankSystem.java
 │    ├── LawSystem.java
 │
 ├── diplomacy/
 │    ├── WarManager.java
 │    ├── RelationsManager.java
 │
 ├── events/
 │    ├── ServerEventHooks.java
 │
 ├── commands/
 │    ├── NationCommand.java
 │
 ├── network/
 │    ├── packets/
 │
 └── client/
      ├── screens/
      ├── map/
```

---

# 🔥 4. MVP Development Plan (Important)

## Phase 1 — Foundation

* FTB Teams integration
* Nation creation system
* Basic member tracking

## Phase 2 — Land Control

* Chunk claiming
* Permission system
* Territory storage

## Phase 3 — Politics

* Ranks
* Basic laws
* Command system

## Phase 4 — Diplomacy

* Alliances
* War declarations
* PvP rules

## Phase 5 — UI + Polish

* Nation screen
* Map overlay
* Better UX

---

# 🧨 5. Design Philosophy

Eagle Nations should:

* Never break vanilla feel
* Stay server-first (no client authority)
* Be configurable for:

  * hardcore geopolitics servers
  * casual SMP nations
  * modded economy servers

---

# 🦅 Final Idea

Think of it as:

> “FTB Teams becomes the *identity system*, Eagle Nations becomes the *political simulator layer*.”

---
