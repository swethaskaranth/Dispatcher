package com.goflash.dispatch.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.goflash.dispatch.R
import com.goflash.dispatch.databinding.ActivityFinishBinding
import com.goflash.dispatch.features.lastmile.tripCreation.ui.activity.LastMileActivity
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


class FinishActivity : BaseActivity() {

    private lateinit var binding: ActivityFinishBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tripId =  intent.getStringExtra("tripId")
        val message = intent.getStringExtra("message")
        binding.toolbar.ivProfile.visibility = View.GONE
        binding.tvTripId.text = message

        Observable.just("launch").delay(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val intent = Intent(this, LastMileActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
    }
}
