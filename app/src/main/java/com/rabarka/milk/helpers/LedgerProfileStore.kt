package com.rabarka.milk.helpers

import android.content.Context

enum class AccountType {
    INDIVIDUAL,
    BUSINESS
}

enum class UserMode {
    BUYER,
    SELLER,
    BOTH
}

data class AppSetup(
    val accountType: AccountType,
    val userMode: UserMode
)

data class LedgerProfile(
    val ownerName: String,
    val contactName: String,
    val contactPhone: String
)

class LedgerProfileStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getProfile(): LedgerProfile? {
        val ownerName = preferences.getString(KEY_OWNER_NAME, "").orEmpty().trim()
        val contactName = preferences.getString(KEY_CONTACT_NAME, "").orEmpty().trim()
        val contactPhone = preferences.getString(KEY_CONTACT_PHONE, "").orEmpty().trim()

        if (ownerName.isBlank() || contactName.isBlank()) {
            return null
        }

        return LedgerProfile(
            ownerName = ownerName,
            contactName = contactName,
            contactPhone = contactPhone
        )
    }

    fun saveProfile(profile: LedgerProfile) {
        preferences.edit()
            .putString(KEY_OWNER_NAME, profile.ownerName.trim())
            .putString(KEY_CONTACT_NAME, profile.contactName.trim())
            .putString(KEY_CONTACT_PHONE, profile.contactPhone.trim())
            .apply()
    }

    fun getAppSetup(): AppSetup? {
        val accountTypeRaw = preferences.getString(KEY_ACCOUNT_TYPE, "").orEmpty()
        val userModeRaw = preferences.getString(KEY_USER_MODE, "").orEmpty()

        if (accountTypeRaw.isBlank() || userModeRaw.isBlank()) {
            return null
        }

        val accountType = runCatching { AccountType.valueOf(accountTypeRaw) }.getOrNull()
        val userMode = runCatching { UserMode.valueOf(userModeRaw) }.getOrNull()
        if (accountType == null || userMode == null) {
            return null
        }

        return AppSetup(
            accountType = accountType,
            userMode = userMode
        )
    }

    fun saveAppSetup(setup: AppSetup) {
        preferences.edit()
            .putString(KEY_ACCOUNT_TYPE, setup.accountType.name)
            .putString(KEY_USER_MODE, setup.userMode.name)
            .apply()
    }

    companion object {
        private const val PREF_NAME = "milk_ledger_profile"
        private const val KEY_OWNER_NAME = "seller_name"
        private const val KEY_CONTACT_NAME = "contact_name"
        private const val KEY_CONTACT_PHONE = "contact_phone"
        private const val KEY_ACCOUNT_TYPE = "account_type"
        private const val KEY_USER_MODE = "user_mode"
    }
}
