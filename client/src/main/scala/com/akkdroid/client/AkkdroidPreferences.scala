package com.akkdroid.client

import android.preference.PreferenceActivity
import android.os.Bundle

class AkkdroidPreferences extends PreferenceActivity {
  override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)
      addPreferencesFromResource(R.xml.preferences)
  }
  override def onStop() {
    super.onStop()
    Akktivity.reload()
  }
}
