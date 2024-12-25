package com.example.daysinlove

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import ru.rustore.sdk.appupdate.manager.RuStoreAppUpdateManager
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.AppUpdateInfo
import ru.rustore.sdk.appupdate.model.AppUpdateOptions
import ru.rustore.sdk.appupdate.model.UpdateAvailability
import ru.rustore.sdk.core.tasks.Task

class RuStoreUpdates {
    companion object {
        private lateinit var updateManager: RuStoreAppUpdateManager

        fun checkForUpdates(context: Context): Int {
            updateManager = RuStoreAppUpdateManagerFactory.create(context)
            val appUpdateInfoTask: Task<AppUpdateInfo> = updateManager.getAppUpdateInfo()
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                // Здесь вы можете проверить статус обновления
                val updateAvailable: Boolean = appUpdateInfo.updateAvailability == UpdateAvailability.UPDATE_AVAILABLE
                if (updateAvailable) {
                    updateManager
                        .startUpdateFlow(appUpdateInfo, AppUpdateOptions.Builder().build())
                        .addOnSuccessListener { resultCode ->
                            if (resultCode == Activity.RESULT_CANCELED) {
                                Toast.makeText(context, "Обновление отложено", Toast.LENGTH_LONG)
                            }
                        }
                        .addOnFailureListener { throwable ->
                            Log.e(TAG, "startUpdateFlow error", throwable)
                        }
                }
            }.addOnFailureListener { exception ->
                // Обработка ошибки
            }
            return 0
        }
    }
}