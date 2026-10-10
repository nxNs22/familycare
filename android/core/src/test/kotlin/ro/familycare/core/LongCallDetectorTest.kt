package ro.familycare.core

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LongCallDetectorTest {

    private val detector = LongCallDetector(thresholdSeconds = 600)

    private fun call(isContact: Boolean, seconds: Long) =
        CompletedCall("0722123456", isContact, seconds, endedAtMillis = 0)

    @Test
    fun longCallWithUnknownNumberAlerts() {
        assertTrue(detector.shouldAlertFamily(call(isContact = false, seconds = 600)))
    }

    @Test
    fun shortCallWithUnknownNumberDoesNotAlert() {
        assertFalse(detector.shouldAlertFamily(call(isContact = false, seconds = 599)))
    }

    @Test
    fun longCallWithContactDoesNotAlert() {
        assertFalse(detector.shouldAlertFamily(call(isContact = true, seconds = 3600)))
    }
}
