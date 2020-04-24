package se.ju.edgeslam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    private val fragmentManager = supportFragmentManager

    val frame1Fragment = Frame1Fragment()
    val frame2Fragment = Frame2Fragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragmentFrame_1, frame1Fragment)
        fragmentTransaction.add(R.id.fragmentFrame_2, frame2Fragment)
        fragmentTransaction.commit()

    }

}
