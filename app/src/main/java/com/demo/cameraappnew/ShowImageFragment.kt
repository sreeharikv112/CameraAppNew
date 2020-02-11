package com.demo.cameraappnew


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide

/**
 * A simple [Fragment] subclass.
 */
class ShowImageFragment : Fragment() {

    private val args:ShowImageFragmentArgs by navArgs()

    lateinit var imageView  : ImageView

    var imagePath : String = ""

    val mTag = ShowImageFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_show_image, container, false)

        imageView = view.findViewById(R.id.image_holder)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            imagePath = args.imagePath

            Log.d(mTag,"imagePath received = $imagePath")

            Glide.with(context!!).load(imagePath)
                .error(context!!.resources.getDrawable(R.drawable.ic_launcher_background))
                .placeholder(context!!.resources.getDrawable(R.drawable.ic_launcher_background))
                .timeout(4500)
                .into(imageView)

        } catch (e: Exception) {
            Log.e(mTag,"onViewCreated ex = $e")
        }
    }
}
