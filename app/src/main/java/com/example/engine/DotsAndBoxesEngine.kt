package com.example.engine

import kotlin.random.Random

enum class PlayerSlot {
    PLAYER_1,
    PLAYER_2;

    fun other(): PlayerSlot = if (this == PLAYER_1) PLAYER_2 else PLAYER_1
}

enum class LineKind {
    HORIZONTAL,
    VERTICAL,
}

data class LineCoord(
    val kind: LineKind,
    val row: Int,
    val col: Int,
)

data class BoxCoord(
    val row: Int,
    val col: Int,
)

data class GridPos(
    val row: Int,
    val col: Int,
)

data class LevelState(
    val levelNumber: Int,
    val dotCount: Int,
    val horizontalLines: Set<GridPos> = emptySet(),
    val verticalLines: Set<GridPos> = emptySet(),
    val boxOwners: Map<BoxCoord, PlayerSlot> = emptyMap(),
    val startingPlayer: PlayerSlot = PlayerSlot.PLAYER_1,
    val activePlayer: PlayerSlot = startingPlayer,
    val lastMove: LineCoord? = null,
    val lastClaimedBoxes: List<BoxCoord> = emptyList(),
    val diamondBoxes: Set<BoxCoord> = emptySet(),
    val revealedDiamonds: Set<BoxCoord> = emptySet(),
    val p1Diamonds: Int = 0,
    val p2Diamonds: Int = 0,
    val lastRevealedDiamonds: List<BoxCoord> = emptyList(),
) {
    val boxesPerSide: Int get() = dotCount - 1
    val totalBoxes: Int get() = boxesPerSide * boxesPerSide
    val p1Score: Int get() = boxOwners.values.count { it == PlayerSlot.PLAYER_1 }
    val p2Score: Int get() = boxOwners.values.count { it == PlayerSlot.PLAYER_2 }
    val claimedCount: Int get() = boxOwners.size
    val isComplete: Boolean get() = claimedCount == totalBoxes
    val winner: PlayerSlot?
        get() = when {
            !isComplete -> null
            p1Score > p2Score -> PlayerSlot.PLAYER_1
            p2Score > p1Score -> PlayerSlot.PLAYER_2
            else -> null
        }

    fun isLineDrawn(line: LineCoord): Boolean {
        val pos = GridPos(line.row, line.col)
        return when (line.kind) {
            LineKind.HORIZONTAL -> pos in horizontalLines
            LineKind.VERTICAL -> pos in verticalLines
        }
    }

    fun isValidLine(line: LineCoord): Boolean {
        return when (line.kind) {
            LineKind.HORIZONTAL -> line.row in 0 until dotCount && line.col in 0 until (dotCount - 1)
            LineKind.VERTICAL -> line.row in 0 until (dotCount - 1) && line.col in 0 until dotCount
        }
    }
}

data class LevelSummary(
    val levelNumber: Int,
    val dotCount: Int,
    val p1Boxes: Int,
    val p2Boxes: Int,
    val winner: PlayerSlot?,
    val p1Diamonds: Int = 0,
    val p2Diamonds: Int = 0,
)

data class DrawLineResult(
    val nextLevel: LevelState,
    val claimedBoxes: List<BoxCoord>,
    val extraTurnGranted: Boolean,
    val levelCompleted: Boolean,
    val newlyRevealedDiamonds: List<BoxCoord> = emptyList(),
)

data class MatchState(
    val currentLevelIndex: Int = 0,
    val currentLevel: LevelState,
    val completedLevels: List<LevelSummary> = emptyList(),
    val alternateStarter: Boolean = true,
    val isAwaitingNextLevel: Boolean = false,
    val isMatchComplete: Boolean = false,
) {
    val totalP1Score: Int
        get() = completedLevels.sumOf { it.p1Boxes } +
            if (completedLevels.any { it.levelNumber == currentLevel.levelNumber }) 0 else currentLevel.p1Score

    val totalP2Score: Int
        get() = completedLevels.sumOf { it.p2Boxes } +
            if (completedLevels.any { it.levelNumber == currentLevel.levelNumber }) 0 else currentLevel.p2Score

    val totalP1Diamonds: Int
        get() = completedLevels.sumOf { it.p1Diamonds } +
            if (completedLevels.any { it.levelNumber == currentLevel.levelNumber }) 0 else currentLevel.p1Diamonds

    val totalP2Diamonds: Int
        get() = completedLevels.sumOf { it.p2Diamonds } +
            if (completedLevels.any { it.levelNumber == currentLevel.levelNumber }) 0 else currentLevel.p2Diamonds

    val matchWinner: PlayerSlot?
        get() = when {
            !isMatchComplete -> null
            totalP1Score > totalP2Score -> PlayerSlot.PLAYER_1
            totalP2Score > totalP1Score -> PlayerSlot.PLAYER_2
            else -> null
        }
}

data class MatchMoveOutcome(
    val nextMatch: MatchState,
    val drawResult: DrawLineResult,
)

object DotsAndBoxesEngine {
    val LEVEL_DOT_COUNTS = listOf(4, 6, 8)
    const val TOTAL_MATCH_BOXES = 83 // 9 + 25 + 49

    fun generateDiamondBoxes(dotCount: Int, random: Random = Random.Default): Set<BoxCoord> {
        val boxesPerSide = (dotCount - 1).coerceAtLeast(1)
        val count = if (random.nextBoolean()) 1 else 3
        val allBoxes = ArrayList<BoxCoord>(boxesPerSide * boxesPerSide)
        for (r in 0 until boxesPerSide) {
            for (c in 0 until boxesPerSide) {
                allBoxes.add(BoxCoord(r, c))
            }
        }
        allBoxes.shuffle(random)
        return allBoxes.take(count.coerceAtMost(allBoxes.size)).toSet()
    }

    fun createMatch(
        startingPlayer: PlayerSlot = PlayerSlot.PLAYER_1,
        alternateStarter: Boolean = true,
        random: Random = Random.Default,
    ): MatchState {
        val firstDotCount = LEVEL_DOT_COUNTS[0]
        return MatchState(
            currentLevelIndex = 0,
            currentLevel = LevelState(
                levelNumber = 1,
                dotCount = firstDotCount,
                startingPlayer = startingPlayer,
                activePlayer = startingPlayer,
                diamondBoxes = generateDiamondBoxes(firstDotCount, random),
            ),
            completedLevels = emptyList(),
            alternateStarter = alternateStarter,
            isAwaitingNextLevel = false,
            isMatchComplete = false,
        )
    }

    fun drawLine(level: LevelState, line: LineCoord): DrawLineResult? {
        if (level.isComplete) return null
        if (!level.isValidLine(line)) return null
        if (level.isLineDrawn(line)) return null

        val pos = GridPos(line.row, line.col)
        val newHorizontal = if (line.kind == LineKind.HORIZONTAL) {
            level.horizontalLines + pos
        } else {
            level.horizontalLines
        }
        val newVertical = if (line.kind == LineKind.VERTICAL) {
            level.verticalLines + pos
        } else {
            level.verticalLines
        }

        val candidateBoxes = adjacentBoxesForLine(line, level.boxesPerSide)
        val newlyClaimed = candidateBoxes.filter { box ->
            box !in level.boxOwners && isBoxComplete(box, newHorizontal, newVertical)
        }

        val updatedOwners = if (newlyClaimed.isEmpty()) {
            level.boxOwners
        } else {
            level.boxOwners + newlyClaimed.associateWith { level.activePlayer }
        }

        val newlyRevealedDiamonds = newlyClaimed.filter { box ->
            box in level.diamondBoxes && box !in level.revealedDiamonds
        }
        val updatedRevealed = if (newlyRevealedDiamonds.isEmpty()) {
            level.revealedDiamonds
        } else {
            level.revealedDiamonds + newlyRevealedDiamonds
        }
        val p1DiaGain = if (level.activePlayer == PlayerSlot.PLAYER_1) newlyRevealedDiamonds.size else 0
        val p2DiaGain = if (level.activePlayer == PlayerSlot.PLAYER_2) newlyRevealedDiamonds.size else 0

        val extraTurn = newlyClaimed.isNotEmpty()
        val tempNext = level.copy(
            horizontalLines = newHorizontal,
            verticalLines = newVertical,
            boxOwners = updatedOwners,
            lastMove = line,
            lastClaimedBoxes = newlyClaimed,
            revealedDiamonds = updatedRevealed,
            p1Diamonds = level.p1Diamonds + p1DiaGain,
            p2Diamonds = level.p2Diamonds + p2DiaGain,
            lastRevealedDiamonds = newlyRevealedDiamonds,
        )
        val levelDone = tempNext.isComplete
        val nextActive = if (levelDone || extraTurn) level.activePlayer else level.activePlayer.other()

        return DrawLineResult(
            nextLevel = tempNext.copy(activePlayer = nextActive),
            claimedBoxes = newlyClaimed,
            extraTurnGranted = extraTurn && !levelDone,
            levelCompleted = levelDone,
            newlyRevealedDiamonds = newlyRevealedDiamonds,
        )
    }

    fun drawLine(match: MatchState, line: LineCoord): MatchMoveOutcome? {
        if (match.isMatchComplete || match.isAwaitingNextLevel) return null
        val result = drawLine(match.currentLevel, line) ?: return null

        val nextLevel = result.nextLevel
        return if (result.levelCompleted) {
            val summary = LevelSummary(
                levelNumber = nextLevel.levelNumber,
                dotCount = nextLevel.dotCount,
                p1Boxes = nextLevel.p1Score,
                p2Boxes = nextLevel.p2Score,
                winner = nextLevel.winner,
                p1Diamonds = nextLevel.p1Diamonds,
                p2Diamonds = nextLevel.p2Diamonds,
            )
            val isFinalLevel = match.currentLevelIndex >= LEVEL_DOT_COUNTS.lastIndex
            MatchMoveOutcome(
                nextMatch = match.copy(
                    currentLevel = nextLevel,
                    completedLevels = match.completedLevels + summary,
                    isAwaitingNextLevel = !isFinalLevel,
                    isMatchComplete = isFinalLevel,
                ),
                drawResult = result,
            )
        } else {
            MatchMoveOutcome(
                nextMatch = match.copy(currentLevel = nextLevel),
                drawResult = result,
            )
        }
    }

    fun advanceToNextLevel(match: MatchState, random: Random = Random.Default): MatchState {
        if (!match.isAwaitingNextLevel || match.isMatchComplete) return match
        val nextIndex = match.currentLevelIndex + 1
        if (nextIndex > LEVEL_DOT_COUNTS.lastIndex) return match

        val prevStarter = match.currentLevel.startingPlayer
        val nextStarter = if (match.alternateStarter) prevStarter.other() else prevStarter
        val nextDotCount = LEVEL_DOT_COUNTS[nextIndex]

        return match.copy(
            currentLevelIndex = nextIndex,
            currentLevel = LevelState(
                levelNumber = nextIndex + 1,
                dotCount = nextDotCount,
                startingPlayer = nextStarter,
                activePlayer = nextStarter,
                diamondBoxes = generateDiamondBoxes(nextDotCount, random),
            ),
            isAwaitingNextLevel = false,
        )
    }

    fun legalLines(level: LevelState): List<LineCoord> {
        if (level.isComplete) return emptyList()
        val lines = ArrayList<LineCoord>()
        for (r in 0 until level.dotCount) {
            for (c in 0 until level.boxesPerSide) {
                val candidate = LineCoord(LineKind.HORIZONTAL, r, c)
                if (!level.isLineDrawn(candidate)) lines.add(candidate)
            }
        }
        for (r in 0 until level.boxesPerSide) {
            for (c in 0 until level.dotCount) {
                val candidate = LineCoord(LineKind.VERTICAL, r, c)
                if (!level.isLineDrawn(candidate)) lines.add(candidate)
            }
        }
        return lines
    }

    fun chooseSmartBotMove(level: LevelState, random: Random = Random.Default): LineCoord? {
        val legal = legalLines(level)
        if (legal.isEmpty()) return null

        // 1. Prefer any line that immediately completes one or more boxes (double capture first)
        val scoringMoves = legal.mapNotNull { line ->
            val outcome = drawLine(level, line)
            if (outcome != null && outcome.claimedBoxes.isNotEmpty()) {
                line to outcome.claimedBoxes.size
            } else {
                null
            }
        }
        if (scoringMoves.isNotEmpty()) {
            val maxGain = scoringMoves.maxOf { it.second }
            val bestScoring = scoringMoves.filter { it.second == maxGain }.map { it.first }
            return bestScoring[random.nextInt(bestScoring.size)]
        }

        // 2. Avoid creating a 3-sided box that hands a free capture to the opponent
        val safeMoves = legal.filter { line ->
            val outcome = drawLine(level, line)?.nextLevel ?: return@filter false
            val adjacent = adjacentBoxesForLine(line, level.boxesPerSide)
            adjacent.none { box -> countDrawnSides(box, outcome.horizontalLines, outcome.verticalLines) == 3 }
        }
        if (safeMoves.isNotEmpty()) {
            return safeMoves[random.nextInt(safeMoves.size)]
        }

        // 3. Otherwise pick the move that minimizes immediate chain risk or random legal move
        return legal[random.nextInt(legal.size)]
    }

    private fun adjacentBoxesForLine(line: LineCoord, boxesPerSide: Int): List<BoxCoord> {
        val boxes = ArrayList<BoxCoord>(2)
        when (line.kind) {
            LineKind.HORIZONTAL -> {
                if (line.row > 0) boxes.add(BoxCoord(line.row - 1, line.col))
                if (line.row < boxesPerSide) boxes.add(BoxCoord(line.row, line.col))
            }
            LineKind.VERTICAL -> {
                if (line.col > 0) boxes.add(BoxCoord(line.row, line.col - 1))
                if (line.col < boxesPerSide) boxes.add(BoxCoord(line.row, line.col))
            }
        }
        return boxes
    }

    private fun isBoxComplete(
        box: BoxCoord,
        horizontal: Set<GridPos>,
        vertical: Set<GridPos>,
    ): Boolean {
        return countDrawnSides(box, horizontal, vertical) == 4
    }

    private fun countDrawnSides(
        box: BoxCoord,
        horizontal: Set<GridPos>,
        vertical: Set<GridPos>,
    ): Int {
        var count = 0
        if (GridPos(box.row, box.col) in horizontal) count++
        if (GridPos(box.row + 1, box.col) in horizontal) count++
        if (GridPos(box.row, box.col) in vertical) count++
        if (GridPos(box.row, box.col + 1) in vertical) count++
        return count
    }
}
