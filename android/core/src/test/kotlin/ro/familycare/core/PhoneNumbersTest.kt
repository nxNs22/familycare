package ro.familycare.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PhoneNumbersTest {

    @Test
    fun normalizesRomanianFormats() {
        assertEquals("+40722123456", PhoneNumbers.normalize("0722 123 456"))
        assertEquals("+40722123456", PhoneNumbers.normalize("+40 722-123-456"))
        assertEquals("+40722123456", PhoneNumbers.normalize("0040722123456"))
        assertEquals("+40213001234", PhoneNumbers.normalize("021 300 1234"))
    }

    @Test
    fun keepsShortCodesAndForeignNumbers() {
        assertEquals("112", PhoneNumbers.normalize("112"))
        assertEquals("+447700900123", PhoneNumbers.normalize("+44 7700 900123"))
    }

    @Test
    fun hiddenNumberIsNull() {
        assertNull(PhoneNumbers.normalize(null))
        assertNull(PhoneNumbers.normalize(""))
        assertNull(PhoneNumbers.normalize("Private"))
    }

    @Test
    fun classifiesNumbers() {
        assertTrue(PhoneNumbers.isInternational("+447700900123"))
        assertFalse(PhoneNumbers.isInternational("+40722123456"))
        assertFalse(PhoneNumbers.isInternational("112"))
        assertTrue(PhoneNumbers.isPremiumRate("+40900123456"))
        assertFalse(PhoneNumbers.isPremiumRate("+40722123456"))
    }
}
