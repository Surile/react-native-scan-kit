package com.reactnativescankit

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.facebook.react.bridge.Promise
import com.huawei.hms.hmsscankit.OnLightVisibleCallBack
import com.huawei.hms.hmsscankit.OnResultCallback
import com.huawei.hms.hmsscankit.RemoteView
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import java.io.IOException

class DefinedActivity:Activity() {
  var frameLayout:FrameLayout? = null
  var remoteView:RemoteView? = null
  var bacBtn:ImageView? = null
  var imgBtn:ImageView? = null
  var flusBtn:ImageView? = null
  val promise: Promise? = null

  var mScreenWidth = 0
  var mScreenHeight = 0

  var SCAN_FRAME_SIZE = 240
  var img = intArrayOf(R.drawable.scankit_flashlight_on,R.drawable.scankit_flashlight_off)

  companion object{
    const val SCAN_RESULT = "scanResult"
    const val REQUEST_CODE_PHOTO = 0X1113
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    setContentView(R.layout.activity_defined)

    frameLayout = findViewById(R.id.rim)
    // 获取屏幕密度来计算屏幕多大。
    val dm = resources.displayMetrics
    val density = dm.density
    // 获取屏幕大小
    mScreenWidth = resources.displayMetrics.widthPixels
    mScreenHeight = resources.displayMetrics.heightPixels

    val scanFrameSize = (SCAN_FRAME_SIZE * density).toInt()

    // 布局于页面中间部分
    var rect = Rect()
    rect.left = mScreenWidth / 2 - scanFrameSize / 2
    rect.right = mScreenWidth / 2 - scanFrameSize / 2
    rect.top = mScreenHeight / 2 - scanFrameSize / 2
    rect.bottom = mScreenHeight / 2 - scanFrameSize / 2

    // 初始化远程视图实例，并为扫描结果设置回调函数
    remoteView = RemoteView.Builder().setContext(this).setBoundingBox(rect).setFormat(HmsScan.ALL_SCAN_TYPE).build()

    // 当光线暗淡时，显示手电筒开关。
    flusBtn = findViewById(R.id.flush_btn)
    remoteView?.setOnLightVisibleCallback(OnLightVisibleCallBack { visible ->
      if (visible){
        flusBtn?.setVisibility(View.VISIBLE)
      }
    })

    // 订阅扫描结果回调事件。
    remoteView?.setOnResultCallback(OnResultCallback { result ->
      if (result != null && result.size > 0 && result[0] != null && !TextUtils.isEmpty(result[0].getOriginalValue())){
        val intent = Intent()
        intent.putExtra(SCAN_RESULT,result[0])
        promise?.resolve(intent)
        setResult(RESULT_OK, intent)
        finish()
      }
    })

    // 将视图渲染到页面上
    remoteView?.onCreate(savedInstanceState)
    val params = FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
    frameLayout?.addView(remoteView,params)

    // 设置返回按钮，照片扫描，和手电筒操作。
    setBackOperation()
    setPictureScanOperation()
    setFlashOperation()
  }

  /**
   * 返回按钮设置
   */
  private fun setBackOperation(){
    bacBtn = findViewById(R.id.back_img)
    bacBtn?.setOnClickListener(View.OnClickListener { finish() })
  }

  /**
   * 照片扫描设置
   * */
  private fun setPictureScanOperation(){
    imgBtn = findViewById(R.id.img_btn)
    imgBtn?.setOnClickListener(View.OnClickListener {
      val pickIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
      pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*")
      this@DefinedActivity.startActivityForResult(pickIntent, REQUEST_CODE_PHOTO)
    })
  }

  /**
   * 手电筒操作
   */
  private fun setFlashOperation(){
    flusBtn?.setOnClickListener{
      if (remoteView?.lightStatus ?: false){
        remoteView?.switchLight()
        flusBtn?.setImageResource(img[1])
      }else{
        remoteView?.switchLight()
        flusBtn?.setImageResource(img[0])
      }
    }
  }

  override fun onStart() {
    super.onStart()
    remoteView?.onStart()
  }

  override fun onResume() {
    super.onResume()
    remoteView?.onResume()
  }

  override fun onPause() {
    super.onPause()
    remoteView?.onPause()
  }

  override fun onStop() {
    super.onStop()
    remoteView?.onStop()
  }

  override fun onDestroy() {
    super.onDestroy()
    remoteView?.onDestroy()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO){
      var bitmap:Bitmap? = null
      val selectedPhotoUri = data.data
      try {
         selectedPhotoUri?.let {
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
             val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
             bitmap = ImageDecoder.decodeBitmap(source)
           }else{
             bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,selectedPhotoUri)
           }
         }
        val hmsScans = ScanUtil.decodeWithBitmap(this@DefinedActivity, bitmap, HmsScanAnalyzerOptions.Creator().setPhotoMode(true).create())
        if (hmsScans != null && hmsScans.size > 0 && hmsScans[0] != null && !TextUtils.isEmpty((hmsScans[0]!!.getOriginalValue()))){
          val intent = Intent()
          intent.putExtra(SCAN_RESULT,hmsScans[0])
          promise?.resolve(intent)
          setResult(RESULT_OK,intent)
          finish()
        }
      } catch (e:IOException){
        e.printStackTrace()
      }
    }
  }
}
