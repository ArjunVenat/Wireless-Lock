package com.example.fingerprintbluetoothauth

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {

    private var cancellationSignal: CancellationSignal? = null
    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
    get() =
        @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
                notifyUser("Authentication Error $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                notifyUser("Authentication Success")
                startActivity(Intent(this@MainActivity, SecretActivity::class.java))
            }
            }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkBiometricSupport()

        val button = findViewById<Button>(R.id.btn_authenticate)
        button.setOnClickListener{
            val biometricPrompt = BiometricPrompt.Builder(this)
                .setTitle("Title of Prompt")
                .setSubtitle("Authentication is Required")
                .setDescription("This app Android Biometrics to unlock your door")
                .setNegativeButton("Cancel", this.mainExecutor, DialogInterface.OnClickListener{dialog, which ->
                    notifyUser("Authentication Cancelled")
                }).build()
            biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallback)
        }
    }
    private fun notifyUser(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getCancellationSignal(): CancellationSignal{
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Authentication was cancelled by the user")
        }
        return cancellationSignal as CancellationSignal
    }

    private fun checkBiometricSupport(): Boolean{
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if(!keyguardManager.isKeyguardSecure){
            notifyUser("Fingerprint Authentication has not been enabled in settings")
            return false
        }
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.USE_BIOMETRIC) !=
        PackageManager.PERMISSION_GRANTED){
            notifyUser("Fingerprint Authentication permission is not enabled")
            return false
        }
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
            true
        }
        else true
    }
}
