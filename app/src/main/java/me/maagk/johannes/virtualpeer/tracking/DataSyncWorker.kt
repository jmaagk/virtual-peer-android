package me.maagk.johannes.virtualpeer.tracking

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.UserProfile
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.useractivity.UserActivityManager
import me.maagk.johannes.virtualpeer.useractivity.UserActivityStorage
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class DataSyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val TAG = "dataSyncWorker"
    }

    override fun doWork(): Result {
        val context = applicationContext

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val lastSyncTime = pref.getLong(context.getString(R.string.pref_last_sync_time), -1)

        val userProfile = UserProfile(context)
        val userActivityStorage = UserActivityStorage(context)
        val userActivityManager = UserActivityManager(context, userActivityStorage)

        // getting all activities that happened since the last sync (or all activities on the first sync)
        val activities = userActivityManager.getActivitiesStartingAtTime(lastSyncTime,
            correctStartTime = false, cutEndTime = false, includeCurrent = false)

        if(activities.isEmpty())
            return Result.success()

        // getting the document (the content of the request); this looks like activities.xml but
        // with only the relevant activities being included
        val doc = userActivityStorage.getDocument(activities)

        val writer = StringWriter()

        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        transformer.transform(DOMSource(doc), StreamResult(writer))

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "activities.xml", writer.toString().toRequestBody("application/xml".toMediaType()))
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