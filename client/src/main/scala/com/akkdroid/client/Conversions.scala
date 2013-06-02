package com.akkdroid.client

import android.view.View
import android.view.View.OnClickListener

object Conversions {

  implicit class AugmentedView(view: View) {
    def onClick(fun: View => Any) {
      view.setOnClickListener(new OnClickListener {
        def onClick(target: View) {
          fun(target)
        }
      })
    }


    def onClickAsync(fun: View => Any) {
      onClick({ target =>
       new Thread(new Runnable {
         def run() {
           fun(target)
         }
       }).start()
      })
    }
  }

}
