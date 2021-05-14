package exercise.find.roots

import androidx.appcompat.app.AppCompatActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.text.Editable
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import java.math.BigInteger

class MainActivity : AppCompatActivity() {
    private var broadcastReceiverForSuccess: BroadcastReceiver? = null
    private var broadcastReceiverForFailure: BroadcastReceiver? = null
    // TODO: add any other fields to the activity as you want
    var savedText: String = ""
    private var isCalculating: Boolean = false
    private var progressBarBool = false // = findViewById<ProgressBar>(R.id.progressBar)
    private val editTextUserInputBool = false//findViewById<EditText>(R.id.editTextInputNumber)
    private val buttonCalculateRootsBool = false //findViewById<Button>(R.id.buttonCalculateRoots)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val editTextUserInput = findViewById<EditText>(R.id.editTextInputNumber)
        val buttonCalculateRoots = findViewById<Button>(R.id.buttonCalculateRoots)

        // set initial UI:
        progressBar.visibility = View.GONE // hide progress
        editTextUserInput.setText("") // cleanup text in edit-text
        editTextUserInput.isEnabled = true // set edit-text as enabled (user can input text)
        buttonCalculateRoots.isEnabled = false // set button as disabled (user can't click)

        // set listener on the input written by the keyboard to the edit-text
        editTextUserInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            @RequiresApi(Build.VERSION_CODES.N)
            override fun afterTextChanged(s: Editable) {
                // text did change
                val newText = editTextUserInput.text.toString()
                // todo: check conditions to decide if button should be enabled/disabled (see spec below)
//                buttonCalculateRoots.isEnabled = !(newText == "" || newText.substring(0, 1) == "-")
                if (newText != "" && (newText.chars().allMatch(Character::isDigit) ||
                        (newText.substring(0, 1) == "+" &&
                                newText.substring(1).chars().allMatch(Character::isDigit))))
                {
                    buttonCalculateRoots.isEnabled = true
                }
                else
                {
                    buttonCalculateRoots.isEnabled = false
                    println("Invalid input $newText");
                }
            }
        })

        // set click-listener to the button
        buttonCalculateRoots.setOnClickListener {
            val intentToOpenService = Intent(this@MainActivity, CalculateRootsService::class.java)
            val userInputString: BigInteger = editTextUserInput.text.toString().toBigInteger()
            // todo: check that `userInputString` is a number. handle bad input. convert `userInputString` to long
            intentToOpenService.putExtra("number_for_service", userInputString.toLong())
            startService(intentToOpenService)
            isCalculating = true
            buttonCalculateRoots.isEnabled = false
            editTextUserInput.isEnabled = false
            progressBar.visibility = View.VISIBLE

//            val userInputLong: Long = 0 // todo this should be the converted string from the user
//            intentToOpenService.putExtra("number_for_service", userInputLong)
//            startService(intentToOpenService)
        }


        // register a broadcast-receiver to handle action "found_roots"
        broadcastReceiverForSuccess = object : BroadcastReceiver() {
            override fun onReceive(context: Context, incomingIntent: Intent) {
                if (incomingIntent == null || incomingIntent.action != "found_roots") return
                // success finding roots!
                /*
         TODO: handle "roots-found" as defined in the spec (below).
          also:
           - the service found roots and passed them to you in the `incomingIntent`. extract them.
           - when creating an intent to open the new-activity, pass the roots as extras to the new-activity intent
             (see for example how did we pass an extra when starting the calculation-service)
         */
                isCalculating = false;
                val originalNum = incomingIntent.getLongExtra("original_number", 0)
                val root1 = incomingIntent.getLongExtra("root1", 0)
                val root2 = incomingIntent.getLongExtra("root2", 0)
                val calTime = incomingIntent.getFloatExtra("calculation_time", 0f)
//                val outputStr = String.format("%d=%d*%d\n" +
//                        " calculation time is %f seconds", originalNum, root1, root2, calTime)

                val intentToOpenSuccessfulActivity =
                        Intent(this@MainActivity, SuccessfulActivity::class.java)
                intentToOpenSuccessfulActivity.putExtra("original_number", originalNum)
                intentToOpenSuccessfulActivity.putExtra("root1", root1)
                intentToOpenSuccessfulActivity.putExtra("root2", root2)
                intentToOpenSuccessfulActivity.putExtra("calculation_time", calTime)
                startActivity(intentToOpenSuccessfulActivity)
//                Toast.makeText(currActivity, outputStr, Toast.LENGTH_LONG).show()
                buttonCalculateRoots.isEnabled = true
                editTextUserInput.isEnabled = true
                progressBar.visibility = View.GONE
            }
        }
        val currActivity = this;
        broadcastReceiverForFailure = object : BroadcastReceiver() {
            override fun onReceive(context: Context, incomingIntent: Intent) {
                if (incomingIntent == null || incomingIntent.action != "stopped_calculations") return
                isCalculating = false;
//                val originalNum = incomingIntent.getLongExtra("original_number", 0)
                val timeGiveUp = incomingIntent.getFloatExtra("time_until_give_up_seconds", 0f)
                val outputStr = String.format("calculation aborted after %f seconds",/*originalNum,*/ timeGiveUp)
                Toast.makeText(currActivity, outputStr, Toast.LENGTH_LONG).show()
                buttonCalculateRoots.isEnabled = true
                editTextUserInput.isEnabled = true
                progressBar.visibility = View.GONE
            }
        }
        registerReceiver(broadcastReceiverForSuccess, IntentFilter("found_roots"))
        registerReceiver(broadcastReceiverForFailure, IntentFilter("stopped_calculations"))

        /*
    todo:
     add a broadcast-receiver to listen for abort-calculating as defined in the spec (below)
     to show a Toast, use this code:
     `Toast.makeText(this, "text goes here", Toast.LENGTH_SHORT).show()`
     */
    }

    override fun onDestroy() {
        super.onDestroy()
        // todo: remove ALL broadcast receivers we registered earlier in onCreate().
        //  to remove a registered receiver, call method `this.unregisterReceiver(<receiver-to-remove>)`
        this.unregisterReceiver(broadcastReceiverForFailure);
        this.unregisterReceiver(broadcastReceiverForSuccess);
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // TODO: put relevant data into bundle as you see fit

        //todo safe if user is editing and what he wrote until now
        val editTextUserInput = findViewById<EditText>(R.id.editTextInputNumber)
//        if(editTextUserInput.isEnabled)
//        {
//            savedText = editTextUserInput.text.toString()
//        }
            outState.putString("currentInput", editTextUserInput.text.toString());
            outState.putBoolean("isCalculating", isCalculating);
            outState.putBoolean("isLegalInput", findViewById<Button>(R.id.buttonCalculateRoots).isEnabled);
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // TODO: load data from bundle and set screen state (see spec below)
        setContentView(R.layout.activity_main)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val editTextUserInput = findViewById<EditText>(R.id.editTextInputNumber)
        val buttonCalculateRoots = findViewById<Button>(R.id.buttonCalculateRoots)

        isCalculating = savedInstanceState.getBoolean("isCalculating", false)
        isCalculating = savedInstanceState.getBoolean("isCalculating", false)
        savedText = savedInstanceState.getString("currentInput", "")
        editTextUserInput.setText(savedText)

        if (isCalculating)
        {
            buttonCalculateRoots.isEnabled = false
            editTextUserInput.isEnabled = false
            progressBar.visibility = View.VISIBLE
            return
        }
        if(!savedInstanceState.getBoolean("isLegalInput", false))
        {
            buttonCalculateRoots.isEnabled = false
            editTextUserInput.isEnabled = true
            progressBar.visibility = View.GONE
            return
        }
        buttonCalculateRoots.isEnabled = true
        editTextUserInput.isEnabled = true
        progressBar.visibility = View.GONE
    }



//    fun enableOrDisable(buttonCalculateRoots: View, editTextUserInput: View,
//                        progressBar: View, active: Boolean){
//        if(active){
//            buttonCalculateRoots.isEnabled = true
//            editTextUserInput.isEnabled = true
//            progressBar.visibility = View.GONE
//        }
//        else
//        {
//            buttonCalculateRoots.isEnabled = false
//            editTextUserInput.isEnabled = false
//            progressBar.visibility = View.VISIBLE
//        }
//    }
}



