package com.rabarka.milk.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import com.rabarka.milk.data.MilkRecord
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class ContactInfo(
    val name: String,
    val phone: String
)

fun extractContactInfo(context: Context, contactUri: Uri): ContactInfo? {
    val contentResolver = context.contentResolver
    var contactId: String? = null
    var contactName: String? = null

    contentResolver.query(
        contactUri,
        arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            contactId = cursor.getString(idIndex)
            contactName = cursor.getString(nameIndex)
        }
    }

    val id = contactId ?: return null

    contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
        arrayOf(id),
        null
    )?.use { phoneCursor ->
        if (phoneCursor.moveToFirst()) {
            val phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val phone = phoneCursor.getString(phoneIndex).orEmpty()
            if (phone.isNotBlank()) {
                return ContactInfo(name = contactName.orEmpty(), phone = phone)
            }
        }
    }

    return null
}

fun formatPhoneForWhatsApp(rawPhone: String): String {
    val digits = rawPhone.filter { it.isDigit() }
    return when {
        digits.length == 10 -> "91$digits"
        digits.startsWith("0") && digits.length == 11 -> "91${digits.drop(1)}"
        else -> digits
    }
}

fun buildRecordShareMessage(record: MilkRecord): String {
    val cowText = if (record.cowFat > 0) {
        "${formatDecimal(record.cowLiters)} L, Fat ${formatDecimal(record.cowFat)}%"
    } else {
        "${formatDecimal(record.cowLiters)} L"
    }

    val buffaloText = if (record.buffaloFat > 0) {
        "${formatDecimal(record.buffaloLiters)} L, Fat ${formatDecimal(record.buffaloFat)}%"
    } else {
        "${formatDecimal(record.buffaloLiters)} L"
    }

    return buildString {
        appendLine("Milk Record")
        appendLine("Date & Time: ${formatDateTime(record.timestamp)}")
        if (record.partyName.isNotBlank()) {
            appendLine("Contact: ${record.partyName}")
        }
        appendLine("Cow: $cowText")
        appendLine("Buffalo: $buffaloText")
        if (record.note.isNotBlank()) {
            appendLine("Note: ${record.note}")
        }
    }.trim()
}

fun openWhatsApp(context: Context, phone: String, message: String): Boolean {
    val formattedPhone = formatPhoneForWhatsApp(phone)
    if (formattedPhone.isBlank()) return false

    val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
    val uri = Uri.parse("https://wa.me/$formattedPhone?text=$encodedMessage")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.whatsapp")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    return if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
        true
    } else {
        false
    }
}

fun openShareSheet(context: Context, message: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
