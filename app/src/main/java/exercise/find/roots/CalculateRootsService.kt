package exercise.find.roots

import android.app.IntentService
import android.content.Intent
import android.util.Log
import kotlin.math.sqrt

class CalculateRootsService : IntentService("CalculateRootsService") {
    public override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        val timeStartMs = System.currentTimeMillis()
        val numberToCalculateRootsFor = intent.getLongExtra("number_for_service", 0)
        if (numberToCalculateRootsFor <= 0) {
            Log.e("CalculateRootsService", "can't calculate roots for non-positive input$numberToCalculateRootsFor")
            return
        }

        /*
    TODO:
     calculate the roots.
     check the time (using `System.currentTimeMillis()`) and stop calculations if can't find an answer after 20 seconds
     upon success (found a root, or found that the input number is prime):
      send broadcast with action "found_roots" and with extras:
       - "original_number"(long)
       - "root1"(long)
       - "root2"(long)
     upon failure (giving up after 20 seconds without an answer):
      send broadcast with action "stopped_calculations" and with extras:
       - "original_number"(long)
       - "time_until_give_up_seconds"(long) the time we tried calculating

      examples:
       for input "33", roots are (3, 11)
       for input "30", roots can be (3, 10) or (2, 15) or other options
       for input "17", roots are (17, 1)
       for input "829851628752296034247307144300617649465159", after 20 seconds give up

     */
        val broadcastSuccess = Intent("found_roots")
        val broadcastFailure = Intent("stopped_calculations")
        var i: Long = 2L
        val sqrtRoot = sqrt(numberToCalculateRootsFor.toDouble())
        while (i <= sqrtRoot && System.currentTimeMillis() - timeStartMs <= 20000)
        {
            if (numberToCalculateRootsFor % i == 0L)
            {
                broadcastSuccess.putExtra("original_number", numberToCalculateRootsFor)
                broadcastSuccess.putExtra("root1", i)
                broadcastSuccess.putExtra("root2", numberToCalculateRootsFor/i)
                broadcastSuccess.putExtra("calculation_time",
                        (System.currentTimeMillis() - timeStartMs)/1000f)
                sendBroadcast(broadcastSuccess)
                return
            }
            i += 1
        }
        if(i > sqrtRoot)
        {
            //todo send the number is prime
            broadcastSuccess.putExtra("original_number", numberToCalculateRootsFor)
            broadcastSuccess.putExtra("root1", 1)  //todo send 0 and 0 as indecation of prime number for MainActivity
            broadcastSuccess.putExtra("root2", numberToCalculateRootsFor)
            broadcastSuccess.putExtra("calculation_time",
                    (System.currentTimeMillis() - timeStartMs)/1000f)
            sendBroadcast(broadcastSuccess)
            return
        }
        else
        {
            //todo fail > 20 sec
            val timeSendMs = (System.currentTimeMillis() - timeStartMs)/1000f;
            broadcastFailure.putExtra("original_number", numberToCalculateRootsFor)
            broadcastFailure.putExtra("time_until_give_up_seconds", timeSendMs)
            sendBroadcast(broadcastFailure)
            return
        }
    }
}



/*
TODO:
the spec is:
upon launch, Activity starts out "clean":
* progress-bar is hidden
* "input" edit-text has no input and it is enabled
* "calculate roots" button is disabled
the button behavior is:
* when there is no valid-number as an input in the edit-text, button is disabled
* when we triggered a calculation and still didn't get any result, button is disabled
* otherwise (valid number && not calculating anything in the BG), button is enabled
the edit-text behavior is:
* when there is a calculation in the BG, edit-text is disabled (user can't input anything)
* otherwise (not calculating anything in the BG), edit-text is enabled (user can tap to open the keyboard and add input)
the progress behavior is:
* when there is a calculation in the BG, progress is showing
* otherwise (not calculating anything in the BG), progress is hidden
when "calculate roots" button is clicked:
* change states for the progress, edit-text and button as needed, so user can't interact with the screen
when calculation is complete successfully:
* change states for the progress, edit-text and button as needed, so the screen can accept new input
* open a new "success" screen showing the following data:
  - the original input number
  - 2 roots combining this number (e.g. if the input was 99 then you can show "99=9*11" or "99=3*33"
  - calculation time in seconds
when calculation is aborted as it took too much time:
* change states for the progress, edit-text and button as needed, so the screen can accept new input
* show a toast "calculation aborted after X seconds"
upon screen rotation (saveState && loadState) the new screen should show exactly the same state as the old screen. this means:
* edit-text shows the same input
* edit-text is disabled/enabled based on current "is waiting for calculation?" state
* progress is showing/hidden based on current "is waiting for calculation?" state
* button is enabled/disabled based on current "is waiting for calculation?" state && there is a valid number in the edit-text input
 */