package ro.familycare.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MessagesTest {

    @Test
    fun familyAlertMentionsMinutesAndNumber() {
        val text = Messages.familyAlert(CompletedCall("0722123456", false, 754, 0))
        assertTrue("12 min" in text)
        assertTrue("+40722123456" in text)
    }

    @Test
    fun warningTitleReflectsReason() {
        val hidden = CallAssessment(RiskLevel.HIGH, setOf(RiskReason.HIDDEN_NUMBER))
        assertEquals("Atenție: număr ascuns", Messages.warningTitle(hidden))
        val foreign = CallAssessment(RiskLevel.HIGH, setOf(RiskReason.UNKNOWN_NUMBER, RiskReason.INTERNATIONAL))
        assertTrue("străinătate" in Messages.warningBody(foreign))
    }
}
