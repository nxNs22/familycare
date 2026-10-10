package ro.familycare.core

/** Romanian texts shown to the senior and sent to the family. */
object Messages {

    fun warningTitle(assessment: CallAssessment): String = when {
        RiskReason.REPORTED_SCAM in assessment.reasons -> "Atenție: număr raportat ca înșelăciune"
        RiskReason.HIDDEN_NUMBER in assessment.reasons -> "Atenție: număr ascuns"
        assessment.level == RiskLevel.HIGH -> "Atenție: apel suspect"
        else -> "Vă sună un număr necunoscut"
    }

    fun warningBody(assessment: CallAssessment): String {
        val lines = mutableListOf<String>()
        if (RiskReason.INTERNATIONAL in assessment.reasons) lines += "Apelul vine din străinătate."
        if (RiskReason.PREMIUM_RATE in assessment.reasons) lines += "Este un număr cu tarif special."
        lines += "Nu dați coduri, parole sau date de card."
        lines += "Dacă cineva spune că o rudă a avut un accident, închideți și sunați chiar ruda."
        lines += "Banca și poliția nu vă cer niciodată bani la telefon."
        return lines.joinToString("\n")
    }

    fun callTrustedPerson(name: String): String = "Sună acum: $name"

    fun familyAlert(call: CompletedCall): String {
        val minutes = call.durationSeconds / 60
        val number = PhoneNumbers.normalize(call.number) ?: "număr ascuns"
        return "FamilyCare: o convorbire de $minutes min cu un număr necunoscut ($number). " +
            "Sunați să verificați că totul e în regulă."
    }
}
