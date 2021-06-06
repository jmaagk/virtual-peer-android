package me.maagk.johannes.virtualpeer.tracking

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Storage.Companion.transformToString
import me.maagk.johannes.virtualpeer.UserProfile
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.useractivity.UserActivityManager
import me.maagk.johannes.virtualpeer.useractivity.UserActivityStorage
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.Instant
import javax.xml.parsers.DocumentBuilderFactory

class DataSyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val TAG = "dataSyncWorker"
    }

    private lateinit var userProfile: UserProfile

    override fun doWork(): Result {
        val context = applicationContext

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val lastSyncTime = pref.getLong(context.getString(R.string.pref_last_sync_time), Instant.EPOCH.toEpochMilli())

        // the document that will combine all data that will be sent into one thing
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        doc.xmlStandalone = true

        // the root of this document
        val root = doc.createElement("syncContent")

        userProfile = UserProfile(context)
        val userActivityStorage = UserActivityStorage(context)
        val userActivityManager = UserActivityManager(context, userActivityStorage)

        // getting all activities that happened since the last sync (or all activities on the first sync)
        val activities = userActivityManager.getActivitiesStartingAtTime(lastSyncTime,
            correctStartTime = false, cutEndTime = false, includeCurrent = false)

        // getting all app usage data that need to be sent
        val trackingManager = TrackingManager(context, false)
        trackingManager.update(lastSyncTime)
        val apps = trackingManager.getApps()

        if(activities.isEmpty() && apps.isEmpty())
            return Result.success()

        if(activities.isNotEmpty()) {
            // this looks like activities.xml but with only the relevant activities being included
            val activitiesRoot = userActivityStorage.getFinishedRootElement(doc, activities)
            root.appendChild(activitiesRoot)
        }

        if(apps.isNotEmpty()) {
            val appsRoot = doc.createElement("apps")
            for(app in apps) {
                val appTag = doc.createElement("app")
                appTag.setAttribute("package", app.packageName)
                appTag.setAttribute("timeUsed", app.timeUsed.toString())

                appsRoot.appendChild(appTag)
            }

            root.appendChild(appsRoot)
        }

        doc.appendChild(root)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "sync.xml", doc.transformToString().toRequestBody("application/xml".toMediaType()))
            .build()

        val client = OkHttpClient()

        val httpUrl = Utils.newServerUrlBuilder()
            .addPathSegment("report_upload")
            .addQueryParameter("id", userProfile.uuid)
            .build()

        val request = Request.Builder()
            .url(httpUrl)
            .post(requestBody)
            .build()

        val call = client.newCall(request)
        val response = call.execute()

        if(response.isSuccessful) {
            response.use {
                it.body?.let { body ->
                    val jsonObject = JSONObject(body.string())
                    val status = jsonObject.getInt("status")

                    if(status == 1) {
                        pref.edit(commit = true) {
                            putLong(context.getString(R.string.pref_last_sync_time), System.currentTimeMillis())
                        }
                        return Result.success()
                    }
                }
            }
        }

        // only retrying 3 times
        return if(runAttemptCount > 3) Result.failure() else Result.retry()
    }

}