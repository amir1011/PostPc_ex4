package exercise.find.roots

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent;
import android.widget.TextView

class SuccessfulActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.success)
        val originalNumberText = findViewById<TextView>(R.id.original_number)
        val twoRootText = findViewById<TextView>(R.id.roots)
        val calTimeText = findViewById<TextView>(R.id.calculate_time)
        val succRootCalculation: Intent = intent

        originalNumberText.text = String.format( "Original number: %d",
                succRootCalculation.getLongExtra("original_number", 0))
        twoRootText.text = String.format( "Found roots: %d * %d",
                succRootCalculation.getLongExtra("root1", 0),
                succRootCalculation.getLongExtra("root2", 0))
        calTimeText.text = String.format( "Calculation time: %f seconds",
                succRootCalculation.getFloatExtra("calculation_time", 0f))

    }
}
