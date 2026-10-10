package ro.familycare.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CallRiskEvaluatorTest {

    private val evaluator = CallRiskEvaluator(reportedScamNumbers = listOf("0312 000 111"))

    @Test
    fun contactsAreNotFlagged() {
        val result = evaluator.assess("0722123456", isContact = true)
        assertEquals(RiskLevel.NONE, result.level)
        assertFalse(result.shouldWarn)
    }

    @Test
    fun unknownRomanianNumberIsCaution() {
        val result = evaluator.assess("0722123456", isContact = false)
        assertEquals(RiskLevel.CAUTION, result.level)
        assertEquals(setOf(RiskReason.UNKNOWN_NUMBER), result.reasons)
    }

    @Test
    fun hiddenNumberIsHigh() {
        val result = evaluator.assess(null, isContact = false)
        assertEquals(RiskLevel.HIGH, result.level)
        assertEquals(setOf(RiskReason.HIDDEN_NUMBER), result.reasons)
    }

    @Test
    fun unknownInternationalNumberIsHigh() {
        val result = evaluator.assess("+44 7700 900123", isContact = false)
        assertEquals(RiskLevel.HIGH, result.level)
        assertEquals(setOf(RiskReason.UNKNOWN_NUMBER, RiskReason.INTERNATIONAL), result.reasons)
    }

    @Test
    fun premiumRateIsHigh() {
        val result = evaluator.assess("0900 123 456", isContact = false)
        assertEquals(RiskLevel.HIGH, result.level)
    }

    @Test
    fun reportedNumberIsHighEvenWhenSavedAsContact() {
        val result = evaluator.assess("+40312000111", isContact = true)
        assertEquals(RiskLevel.HIGH, result.level)
        assertEquals(setOf(RiskReason.REPORTED_SCAM), result.reasons)
    }
}
