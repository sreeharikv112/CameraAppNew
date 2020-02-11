package com.demo.cameraappnew


import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.demo.cameraappnew.camhelper.*

/**
 * A simple [Fragment] subclass.
 */
class CameraPermissionFragment : Fragment() {


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        checkCameraPermission()
    }
    /*override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_landing, container, false)

        mInvokeCamBtn = view.findViewById(R.id.checkPermission)
        mInvokeCamBtn.setOnClickListener {
            checkCameraPermission()
        }
        return view
    }*/

    private fun checkCameraPermission() {
        if (hasPermissions(requireContext())) {
            // If permissions have already been granted, proceed

            invokeCameraFragment()

        } else {
            // Request camera-related permissions
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Takes the user to the success fragment when permission is granted

                invokeCameraFragment()

            } else {
                Toast.makeText(context, "Photo Permission request denied. App will not be able to proceed! ", Toast.LENGTH_LONG).show()
                exitCameraFunctionality()
            }
        }
    }

    fun exitCameraFunctionality(){
        val baseActivity  = context!! as CameraMainActivity
        baseActivity.onBackPressed()
    }

    private fun invokeCameraFragment(){

        findNavController().navigate(CameraPermissionFragmentDirections.moveToCameraFragment("",-1))

    }

    companion object {
        // Convenience method used to check if all permissions required by this app are granted
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    }
}
