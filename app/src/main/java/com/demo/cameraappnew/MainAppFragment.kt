package com.demo.cameraappnew


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.demo.cameraappnew.camhelper.CAMERA_ACTION_REQ_CODE
import com.demo.cameraappnew.camhelper.FILE_PATH_KEY

/**
 * A simple [Fragment] subclass.
 */
class MainAppFragment : Fragment() {

    lateinit var mBtn: Button
    lateinit var mImageView : ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_landing_activity, container, false)

        mBtn = view.findViewById(R.id.btnMoveAction)
        mImageView = view.findViewById(R.id.image_holder)

        mBtn.setOnClickListener { moveToNextNavHost() }

        return view
    }

    private fun moveToNextNavHost() {

        startActivityForResult(Intent(context,CameraMainActivity::class.java),CAMERA_ACTION_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CAMERA_ACTION_REQ_CODE){
            Log.d("TAG","onActivityResult ===== ")

            //if(resultCode == Activity.RESULT_OK){
                try {
                    val dataReceived = data?.getStringExtra(FILE_PATH_KEY)

                    Log.d("TAG","dataReceived = $dataReceived")

                    reloadImage(dataReceived!!)

                } catch (e: Exception) {
                    Log.d("TAG","EXCEP = $e")
                }

            //}
        }
    }


    fun reloadImage(filePath:String){
        try {
            Glide.with(context!!).load(filePath)
                .error(context!!.resources.getDrawable(R.drawable.ic_launcher_background))
                .placeholder(context!!.resources.getDrawable(R.drawable.ic_launcher_background))
                .timeout(4500)
                .into(mImageView)

        } catch (e: Exception) {
        }

    }

}
