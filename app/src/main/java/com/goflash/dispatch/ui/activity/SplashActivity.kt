package com.goflash.dispatch.ui.activity

import android.content.Intent
import android.os.Bundle
import com.goflash.dispatch.R
import com.goflash.dispatch.util.Toaster
import com.goflash.dispatch.util.isRooted
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action == Intent.ACTION_MAIN
        ) {

            finish()
            return
        }

        delayedLogin()
    }

    private fun delayedLogin() {

        Observable.just("launch").delay(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (isRooted()) {
                    Toaster.show(this, "Cannot run on a rooted device")
                    //finish()
                } else
                    login()
            }
    }

    private fun login() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
