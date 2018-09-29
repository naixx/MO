package com.github.naixx.mo

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Subscription
import rx.schedulers.Schedulers
import java.util.*

fun d(s: Any?) = Log.d("---", s.toString())
fun e(s: Any?, t: Throwable?) = Log.e("---", s.toString(), t)

typealias Image = Bitmap

class ViewModel(application: Application) : AndroidViewModel(application) {
    private val data = MutableLiveData<Image>()
    val image: LiveData<Image>
        get() = data

    private val retrofit = Retrofit.Builder()
            .baseUrl("https://onde.app/mobile_optimized/")
            .addConverterFactory(GsonConverterFactory.create()).build()
    private val service = retrofit.create(Service::class.java)
    private var call: Call<Response>? = null
    private var subscription: Subscription? = null

    init {
        refresh()
    }

    fun refresh() {
        call = service.images().apply {
            enqueue(object : Callback<Response?> {
                override fun onFailure(call: Call<Response?>, t: Throwable) {
                }

                override fun onResponse(call: Call<Response?>, response: retrofit2.Response<Response?>) {
                    if (response.isSuccessful) {
                        response.body()?.let { decode(it.pngImagesInBase64) }
                    }
                }
            })
        }
    }

    private fun decode(images: List<String>) {
        val bytes = Base64.getDecoder().decode(images.first())
        val opts = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)

        val resulting = Bitmap.createBitmap(opts.outWidth, opts.outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resulting)
        val start = System.currentTimeMillis()

        subscription?.unsubscribe()
        subscription = Observable.from(images).concatMap {
            Observable.fromCallable {
                val bytes = Base64.getDecoder().decode(it)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                d("${Thread.currentThread().name} ${opts.outWidth} x ${opts.outHeight}")


                opts.inJustDecodeBounds = false
                val b = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                canvas.drawBitmap(b, 0f, 0f, null)
                resulting
            }
        }.last()
                .doOnNext {
                    d(System.currentTimeMillis() - start)
                }
                .subscribeOn(Schedulers.computation())
                .subscribe(data::postValue)
                { e("Error", it) }
    }

    override fun onCleared() {
        super.onCleared()
        call?.cancel()
        subscription?.unsubscribe()
    }
}
