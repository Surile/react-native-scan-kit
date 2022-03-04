package com.reactnativescankit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.Promise
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.reactnativescankit.DefinedActivity.Companion.SCAN_RESULT

class MainActivity:Activity(),ActivityCompat.OnRequestPermissionsResultCallback {
  companion object {
    const val CAMERA_REQ_CODE = 111
    const val DEFINED_CODE = 222
    const val BITMAP_CODE = 333
    const val MULTIPROCESSOR_SYN_CODE = 444
    const val MULTIPROCESSOR_ASYN_CODE = 555
    const val GENERATE_CODE = 666
    const val DECODE = 1
    const val GENERATE = 2
    const val REQUEST_CODE_SCAN_ONE = 0X01
    const val REQUEST_CODE_DEFINE = 0X0111
    const val REQUEST_CODE_SCAN_MULTI = 0X011
    const val DECODE_MODE = "decode_mode"
    const val RESULT = "SCAN_RESULT"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    setContentView(R.layout.activity_mwcmain)
    this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    // Set noTitleBar.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      val window = window
      window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }
  }

  fun loadScanKitBtnClick() {
    requestPermission(CAMERA_REQ_CODE, DECODE)
  }

  /**
   * Call the customized view.
   */
  fun newViewBtnClick() {
    requestPermission(DEFINED_CODE, DECODE)
  }

  /**
   * Apply for permissions.
   */
  private fun requestPermission(requestCode: Int, mode: Int) {
    if (mode == DECODE) {
      decodePermission(requestCode)
    } else if (mode == GENERATE) {
      generatePermission(requestCode)
    }
  }

  /**
   * Apply for permissions.
   */
  private fun decodePermission(requestCode: Int) {
    ActivityCompat.requestPermissions(
      this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
      requestCode)
  }

  /**
   * Apply for permissions.
   */
  private fun generatePermission(requestCode: Int) {
    ActivityCompat.requestPermissions(
      this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
      requestCode)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (grantResults.size < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
      return
    }
    // Default View Mode
    if (requestCode == CAMERA_REQ_CODE) {
      ScanUtil.startScan(this, REQUEST_CODE_SCAN_ONE, HmsScanAnalyzerOptions.Creator().create())
    }
    // Customized View Mode
    if (requestCode == DEFINED_CODE) {
      val intent = Intent(this, DefinedActivity::class.java)
      this.startActivityForResult(intent, REQUEST_CODE_DEFINE)
    }
  }
}
