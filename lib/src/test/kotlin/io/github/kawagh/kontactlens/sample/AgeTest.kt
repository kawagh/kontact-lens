package io.github.kawagh.kontactlens.sample

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AgeTest {
    // 境界値 18 のテスト。これが無いと CONDITIONALS_BOUNDARY の変異が生き残る。
    @Test fun eighteenIsAdult() {
        assertTrue(isAdult(18))
    }

    @Test fun twentyIsAdult() {
        assertTrue(isAdult(20))
    }

    @Test fun tenIsNotAdult() {
        assertFalse(isAdult(10))
    }
}
