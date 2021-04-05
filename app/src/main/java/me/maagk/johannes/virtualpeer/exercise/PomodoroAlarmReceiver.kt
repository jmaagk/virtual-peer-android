package me.maagk.johannes.virtualpeer.exercise

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PomodoroAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // extracting the wrapped extra bundle
        val extraBundle = intent.getBundleExtra("extraBundle") ?: return

        if(!extraBundle.containsKey("learningContents") || !intent.hasExtra("position"))
            return

        val learningContentsExtra = extraBundle.getParcelableArrayList<LearningContent>("learningContents")

        val learningContents: ArrayList<LearningContent>
        if(learningContentsExtra is ArrayList<LearningContent>)
            learningContents = learningContentsExtra
        else
            return

        val position = intent.getIntExtra("position", -1)
        if(position == -1)
            return

        if(position == learningContents.size - 1) {
            // this is the point at which the exercise is completed; sending a notification for the user to rate this exercise
            PomodoroExercise.finish(context)
        } else {
            // going to the next part of the exercise
            PomodoroExercise.startLearningContent(context, learningContents, position + 1)
        }
    }
}