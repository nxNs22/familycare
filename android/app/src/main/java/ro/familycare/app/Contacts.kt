package ro.familycare.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract

object Contacts {

    /** True if [number] is saved in the address book. False without READ_CONTACTS. */
    fun isContact(context: Context, number: String?): Boolean {
        if (number.isNullOrBlank()) return false
        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        return context.contentResolver
            .query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
            ?.use { it.moveToFirst() }
            ?: false
    }
}
