package com.demo.cameraappnew


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.demo.cameraappnew.camhelper.FileUtility
import com.demo.cameraappnew.camhelper.GenericListAdapter
import com.demo.cameraappnew.camhelper.decodeExifOrientation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import kotlin.math.max


class ImagePreviewFragment : Fragment() {

    lateinit var mImageHolder : ViewPager2
    lateinit var mRetakeBtn : ImageView
    lateinit var mSelectBtn : ImageView
    lateinit var mFinalBitmap : Bitmap
    val mTag = ImagePreviewFragment::class.java.simpleName

    /** AndroidX navigation arguments */
    private val args: ImagePreviewFragmentArgs by navArgs()

    /** Default Bitmap decoding options */
    private val bitmapOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = false
        // Keep Bitmaps at less than 1 MP
        if (max(outHeight, outWidth) > DOWNSAMPLE_SIZE) {
            val scaleFactorX = outWidth / DOWNSAMPLE_SIZE + 1
            val scaleFactorY = outHeight / DOWNSAMPLE_SIZE + 1
            inSampleSize = max(scaleFactorX, scaleFactorY)
        }
    }

    /** Bitmap transformation derived from passed arguments */
    private val bitmapTransformation: Matrix by lazy { decodeExifOrientation(args.orientation) }

    /** Flag indicating that there is depth data available for this image */
    private val isDepth: Boolean by lazy { args.depth }

    /** Data backing our Bitmap viewpager */
    private val bitmapList: MutableList<Bitmap> = mutableListOf()

    private fun imageViewFactory() = ImageView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_image_preview, container, false)

        mImageHolder = view.findViewById(R.id.camera_preview_view)

        mRetakeBtn = view.findViewById(R.id.retake_btn)
        mRetakeBtn.setOnClickListener {

            navigateBack()
        }

        mSelectBtn = view.findViewById(R.id.next_action)
        //Dont set action till data is ready


        // Populate the ViewPager and implement a cache of two media items
        mImageHolder.offscreenPageLimit = 2
        mImageHolder.adapter = GenericListAdapter(
            bitmapList,
            itemViewFactory = { imageViewFactory() }) { view, item, _ ->
            view as ImageView
            Glide.with(view).load(item).into(view)
        }


        return  view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {

            // Load input image file
            val inputBuffer = loadInputBuffer()

            // Load the main JPEG image
            mFinalBitmap = decodeBitmap(inputBuffer, 0, inputBuffer.size)

            addItemToViewPager(mImageHolder, mFinalBitmap)

            // If we have depth data attached, attempt to load it
            if (isDepth) {
                try {
                    val depthStart = findNextJpegEndMarker(inputBuffer, 2)

                    mFinalBitmap = decodeBitmap(
                        inputBuffer, depthStart, inputBuffer.size - depthStart)

                    addItemToViewPager(mImageHolder, mFinalBitmap)

                    val confidenceStart = findNextJpegEndMarker(inputBuffer, depthStart)

                    mFinalBitmap = decodeBitmap(
                        inputBuffer, confidenceStart, inputBuffer.size - confidenceStart)

                    addItemToViewPager(mImageHolder, mFinalBitmap)

                } catch (exc: RuntimeException) {
                    Log.e(TAG, "Invalid start marker for depth or confidence data")
                }
            }

            mSelectBtn.setOnClickListener {
                selectedImageAction()
            }
        }
    }

    /** Utility function used to read input file into a byte array */
    private fun loadInputBuffer(): ByteArray {
        val inputFile = File(args.filePath)
        return BufferedInputStream(inputFile.inputStream()).let { stream ->
            ByteArray(stream.available()).also {
                stream.read(it)
                stream.close()
            }
        }
    }

    /** Utility function used to add an item to the viewpager and notify it, in the main thread */
    private fun addItemToViewPager(view: ViewPager2, item: Bitmap) = view.post {
        bitmapList.add(item)
        view.adapter!!.notifyDataSetChanged()

        Log.d(mTag,"bitmapList.size ===== ${bitmapList.size}")

    }

    /** Utility function used to decode a [Bitmap] from a byte array */
    private fun decodeBitmap(buffer: ByteArray, start: Int, length: Int): Bitmap {

        // Load bitmap from given buffer
        val bitmap = BitmapFactory.decodeByteArray(buffer, start, length, bitmapOptions)

        // Transform bitmap orientation using provided metadata
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, bitmapTransformation, true)
    }

    companion object {
        private val TAG = ImagePreviewFragment::class.java.simpleName

        /** Maximum size of [Bitmap] decoded */
        private const val DOWNSAMPLE_SIZE: Int = 1024  // 1MP

        /** These are the magic numbers used to separate the different JPG data chunks */
        private val JPEG_DELIMITER_BYTES = arrayOf(-1, -39)

        /**
         * Utility function used to find the markers indicating separation between JPEG data chunks
         */
        private fun findNextJpegEndMarker(jpegBuffer: ByteArray, start: Int): Int {

            // Sanitize input arguments
            assert(start >= 0) { "Invalid start marker: $start" }
            assert(jpegBuffer.size > start) {
                "Buffer size (${jpegBuffer.size}) smaller than start marker ($start)" }

            // Perform a linear search until the delimiter is found
            for (i in start until jpegBuffer.size - 1) {
                if (jpegBuffer[i].toInt() == JPEG_DELIMITER_BYTES[0] &&
                    jpegBuffer[i + 1].toInt() == JPEG_DELIMITER_BYTES[1]) {
                    return i + 2
                }
            }

            // If we reach this, it means that no marker was found
            throw RuntimeException("Separator marker not found in buffer (${jpegBuffer.size})")
        }
    }
    //=================
    private fun selectedImageAction() {

        Toast.makeText(context,"Image Selected",Toast.LENGTH_SHORT).show()

        val fileWithBitmap = FileUtility().createImageFileWithBitmap(context!!,bitmapList[0])

        Log.d(mTag,"filePath = $fileWithBitmap")

        val baseActivity  = context!! as CameraMainActivity

        baseActivity.passDataBackToCaller(fileWithBitmap.absolutePath)
    }

    private fun navigateBack() {
        findNavController().popBackStack()
    }
}
