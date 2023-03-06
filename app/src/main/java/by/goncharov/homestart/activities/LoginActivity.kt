package by.goncharov.homestart.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import by.goncharov.homestart.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.nav_graph)
    }
}
