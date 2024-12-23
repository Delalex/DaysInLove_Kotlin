package com.example.daysinlove

import android.content.Context
import android.widget.Toast
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory

class RuStoreUpdates {
    companion object {
        fun check_updates(context: Context): Boolean {
            val updateManager = RuStoreAppUpdateManagerFactory.create(context)
            val result = updateManager.getAppUpdateInfo()
            return false
        }
    }
}