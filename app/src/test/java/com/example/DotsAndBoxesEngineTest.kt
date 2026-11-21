package com.example

import com.example.data.LeaderboardScope
import com.example.data.MockGameService
import com.example.engine.BoxCoord
import com.example.engine.DotsAndBoxesEngine
import com.example.engine.GridPos
import com.example.engine.LevelState
import com.example.engine.LineCoord
import com.example.engine.LineKind
import com.example.engine.PlayerSlot
import com.example.model.ExpConfig
import com.example.model.GenderOption
import com.example.model.IdCodeGenerator
import com.example.model.PlayerColorOption
import com.example.model.UserProfile
import com.example.model.deriveInitialsFromName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DotsAndBoxesEngineTest {

    @Test
    fun `no capture passes turn and rejects duplicate line`() {
        val match = DotsAndBoxesEngine.createMatch()
        assertEquals(PlayerSlot.PLAYER_1, match.currentLevel.activePlayer)

        val move = LineCoord(LineKind.HORIZONTAL, 0, 0)
        val outcome = DotsAndBoxesEngine.drawLine(match, move)
        assertNotNull(outcome)
        assertTrue(outcome!!.drawResult.claimedBoxes.isEmpty())
        assertFalse(outcome.drawResult.extraTurnGranted)
        assertEquals(PlayerSlot.PLAYER_2, outcome.nextMatch.currentLevel.activePlayer)

        // Drawing the same line again is illegal
        assertNull(DotsAndBoxesEngine.drawLine(outcome.nextMatch, move))
    }

    @Test
    fun `single capture claims one box and grants extra turn`() {
        val level = LevelState(
            levelNumber = 1,
            dotCount = 4,
            horizontalLines = setOf(GridPos(0, 0), GridPos(1, 0)),
            verticalLines = setOf(GridPos(0, 0)),
            startingPlayer = PlayerSlot.PLAYER_1,
            activePlayer = PlayerSlot.PLAYER_1,
        )

        val outcome = DotsAndBoxesEngine.drawLine(level, LineCoord(LineKind.VERTICAL, 0, 1))
        assertNotNull(outcome)
        assertEquals(listOf(BoxCoord(0, 0)), outcome!!.claimedBoxes)
        assertTrue(outcome.extraTurnGranted)
        assertEquals(PlayerSlot.PLAYER_1, outcome.nextLevel.activePlayer)
        assertEquals(1, outcome.nextLevel.p1Score)
        assertEquals(0, outcome.nextLevel.p2Score)
    }

    @Test
    fun `double capture claims both adjacent boxes with one line`() {
        val level = LevelState(
            levelNumber = 1,
            dotCount = 4,
            horizontalLines = setOf(
                GridPos(0, 0), GridPos(1, 0),
                GridPos(0, 1), GridPos(1, 1),
            ),
            verticalLines = setOf(
                GridPos(0, 0),
                GridPos(0, 2),
            ),
            startingPlayer = PlayerSlot.PLAYER_2,
            activePlayer = PlayerSlot.PLAYER_2,
        )

        val outcome = DotsAndBoxesEngine.drawLine(level, LineCoord(LineKind.VERTICAL, 0, 1))
        assertNotNull(outcome)
        assertEquals(setOf(BoxCoord(0, 0), BoxCoord(0, 1)), outcome!!.claimedBoxes.toSet())
        assertTrue(outcome.extraTurnGranted)
        assertEquals(PlayerSlot.PLAYER_2, outcome.nextLevel.activePlayer)
        assertEquals(2, outcome.nextLevel.p2Score)
    }

    @Test
    fun `full three-level match completes all 83 boxes with no tie possible`() {
        var match = DotsAndBoxesEngine.createMatch()
        val expectedBoxesPerLevel = listOf(9, 25, 49)

        for (levelIdx in 0..2) {
            assertEquals(levelIdx + 1, match.currentLevel.levelNumber)
            assertEquals(expectedBoxesPerLevel[levelIdx], match.currentLevel.totalBoxes)

            while (!match.currentLevel.isComplete) {
                val move = DotsAndBoxesEngine.chooseSmartBotMove(match.currentLevel)
                assertNotNull(move)
                val outcome = DotsAndBoxesEngine.drawLine(match, move!!)
                assertNotNull(outcome)
                match = outcome!!.nextMatch
            }

            if (levelIdx < 2) {
                assertTrue(match.isAwaitingNextLevel)
                assertFalse(match.isMatchComplete)
                match = DotsAndBoxesEngine.advanceToNextLevel(match)
            } else {
                assertTrue(match.isMatchComplete)
                assertFalse(match.isAwaitingNextLevel)
            }
        }

        assertEquals(83, match.totalP1Score + match.totalP2Score)
        assertNotNull(match.matchWinner)
        val totalDiamondsAcrossMatch =
            match.completedLevels.sumOf { it.p1Diamonds + it.p2Diamonds }
        assertEquals(totalDiamondsAcrossMatch, match.totalP1Diamonds + match.totalP2Diamonds)
        assertTrue(totalDiamondsAcrossMatch in 3..9)
    }

    @Test
    fun `hidden diamonds place 1 or 3 unique boxes and reveal only on claim`() {
        val rng = Random(42)
        repeat(20) {
            val placed = DotsAndBoxesEngine.generateDiamondBoxes(dotCount = 4, random = rng)
            assertTrue(placed.size == 1 || placed.size == 3)
            assertTrue(placed.all { it.row in 0 until 3 && it.col in 0 until 3 })
        }

        val diamondBox = BoxCoord(0, 0)
        val level = LevelState(
            levelNumber = 1,
            dotCount = 4,
            horizontalLines = setOf(GridPos(0, 0), GridPos(1, 0)),
            verticalLines = setOf(GridPos(0, 0)),
            startingPlayer = PlayerSlot.PLAYER_1,
            activePlayer = PlayerSlot.PLAYER_1,
            diamondBoxes = setOf(diamondBox, BoxCoord(2, 2)),
            revealedDiamonds = emptySet(),
        )

        // Before claiming, nothing is revealed
        assertTrue(level.revealedDiamonds.isEmpty())

        // Claiming (0,0) reveals only that box and awards 1 diamond to Player 1
        val outcome = DotsAndBoxesEngine.drawLine(level, LineCoord(LineKind.VERTICAL, 0, 1))
        assertNotNull(outcome)
        assertEquals(listOf(diamondBox), outcome!!.newlyRevealedDiamonds)
        assertEquals(setOf(diamondBox), outcome.nextLevel.revealedDiamonds)
        assertFalse(BoxCoord(2, 2) in outcome.nextLevel.revealedDiamonds)
        assertEquals(1, outcome.nextLevel.p1Diamonds)
        assertEquals(0, outcome.nextLevel.p2Diamonds)
    }

    @Test
    fun `player id and room code generator exclude ambiguous characters`() {
        val rng = Random(99)
        repeat(50) {
            val playerId = IdCodeGenerator.generatePlayerId(rng)
            val roomCode = IdCodeGenerator.generateRoomCode(rng)
            assertTrue(playerId.startsWith("DB-"))
            assertTrue(IdCodeGenerator.isValidPlayerId(playerId))
            assertTrue(IdCodeGenerator.isValidCodeChars(roomCode))
            val body = playerId.removePrefix("DB-")
            assertFalse(body.contains('O') || body.contains('0') || body.contains('I') || body.contains('1'))
            assertFalse(roomCode.contains('O') || roomCode.contains('0') || roomCode.contains('I') || roomCode.contains('1'))
        }

        assertFalse(IdCodeGenerator.isValidPlayerId("DB-A0B2C3"))
        assertFalse(IdCodeGenerator.isValidPlayerId("DB-AOB2C3"))
        assertFalse(IdCodeGenerator.isValidPlayerId("DB-A1B2C3"))
        assertFalse(IdCodeGenerator.isValidPlayerId("DB-AIB2C3"))
        assertFalse(IdCodeGenerator.isValidPlayerId("XX-ABCDEF"))
        assertTrue(IdCodeGenerator.isValidPlayerId("DB-K9M4P2"))
    }

    @Test
    fun `exp formula and leaderboard tiebreak rules`() {
        assertEquals(1, ExpConfig.levelForExp(0))
        assertEquals(1, ExpConfig.levelForExp(99))
        assertEquals(2, ExpConfig.levelForExp(100))
        assertEquals(5, ExpConfig.levelForExp(405))

        val service = MockGameService()
        val user = UserProfile(
            displayName = "Test User",
            initials = "TU",
            gender = GenderOption.NON_BINARY,
            colorOption = PlayerColorOption.TOMATO,
            totalExp = 150,
            onlineMatchesPlayed = 30,
            onlineMatchesWon = 18,
        )
        assertEquals(PlayerColorOption.TOMATO, PlayerColorOption.fromName("TERRACOTTA"))
        val board = service.getLeaderboard(LeaderboardScope.ALL_TIME, user)
        assertEquals(41, board.size)
        assertTrue(board.any { it.isCurrentUser })
        for (i in 0 until board.lastIndex) {
            assertTrue(board[i].totalExp >= board[i + 1].totalExp)
        }
    }

    @Test
    fun `deriveInitialsFromName returns only single uppercase first letter`() {
        assertEquals("A", deriveInitialsFromName("Aarav Mehta"))
        assertEquals("P", deriveInitialsFromName("player 2"))
        assertEquals("Z", deriveInitialsFromName("  zeta"))
        assertEquals("", deriveInitialsFromName("   "))
    }
}
