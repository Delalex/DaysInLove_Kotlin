package com.example.daysinlove

import android.content.Context
import android.widget.Toast
import ru.rustore.sdk.appupdate.manager.RuStoreAppUpdateManager
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.AppUpdateInfo
import ru.rustore.sdk.appupdate.model.UpdateAvailability
import ru.rustore.sdk.core.tasks.Task

class RuStoreUpdates {
    companion object {
        private lateinit var updateManager: RuStoreAppUpdateManager

        fun checkForUpdates(context: Context): Boolean {
            updateManager = RuStoreAppUpdateManagerFactory.create(context)
            val appUpdateInfoTask: Task<AppUpdateInfo> = updateManager.getAppUpdateInfo()
            var result: Boolean = false
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                // Здесь вы можете проверить статус обновления
                val updateAvailable = appUpdateInfo.updateAvailability == UpdateAvailability.UPDATE_AVAILABLE
                result = true
            }.addOnFailureListener { exception ->
                // Обработка ошибки
                result = false
            }
            return result
        }
    }
}