package me.maagk.johannes.virtualpeer.tracking

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.maagk.johannes.virtualpeer.UserProfile
import me.maagk.johannes.virtualpeer.Utils
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class RegistrationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val TAG = "registrationWorker"
    }

    override fun doWork(): Result {
        val context = applicationContext

        // registering the user on the app's server to get a UUID
        val client = OkHttpClient()
        val request = Request.Builder().url(Utils.newServerUrlBuilder().addPathSegment("register").build()).get().build()
        val call = client.newCall(request)
        val response = call.execute()

        if(response.isSuccessful) {
            response.use {
                it.body?.let { body ->
                    val jsonObject = JSONObject(body.string())
                    val status = jsonObject.getInt("status")
                    val uuid = jsonObject.getString("uuid")

                    if(status == 1) {
                        val userProfile = UserProfile(context)
                        userProfile.uuid = uuid
                        return Result.success()
                    }
                }
            }
        }

        // only retrying 3 times
        return if(runAttemptCount > 3) Result.failure() else Result.retry()
    }

}