package com.example.statussaver

import android.app.Dialog
import android.content.ContentValues
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import com.example.statussaver.databinding.FragmentWhatsappBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class WhatsappFragment : Fragment() , StatusAdapter.ButtonClicked{

    var list = ArrayList<Status>()
    lateinit var adapter: StatusAdapter
    lateinit var binding: FragmentWhatsappBinding
    var count=false
    val launcher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        if (it != null) {

            val sp= this.requireActivity().getSharedPreferences("DATA_PATH",MODE_PRIVATE)
            val editor=sp.edit()
            editor.putString("PATH",it.toString())
            editor.apply()

            this.requireActivity().contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val fileDoc= DocumentFile.fromTreeUri(requireContext(),it)
            lifecycleScope.launch(Dispatchers.IO) {
                for (file: DocumentFile in fileDoc!!.listFiles()) {
                    if (!file.name!!.endsWith(".nomedia")) {
                        val status:Status
                        if(file.name!!.endsWith(".mp4")) {
                            status = Status(file.name!!, file.uri.toString(), true)
                        }
                        else{
                            status = Status(file.name!!, file.uri.toString())
                        }
                        list.add(status)
                    }
                }

                lifecycleScope.launch(Dispatchers.Main){
                    setUpRecyclerView()
                    binding.progressBar.visibility=View.GONE
                }

            }

        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
    Log.d("Whatsapp","viewCreated")
        binding=DataBindingUtil.inflate(layoutInflater,R.layout.fragment_whatsapp,container,false)

        if(count==false) {
            count=true
            accessStorage()
        }else{
            binding.progressBar.visibility=View.GONE
           setUpRecyclerView()
        }
        binding.swipeRefresh.setOnRefreshListener {
            list.clear()
            accessStorage()
            binding.swipeRefresh.isRefreshing=false
        }
        return binding.root
    }

    private fun readDataFromPrefs(): Boolean {
        val sp=this.requireActivity().getSharedPreferences("DATA_PATH", MODE_PRIVATE)
        val uripath=sp.getString("PATH","")
        if(uripath!!.isEmpty()){
            return false
        }
        return true
    }

    private fun takePermission() {
        val targetDirectory =
            "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(targetDirectory))
            putExtra("android.content.extra.SHOW_ADVANCED", true)
        }
        val wLauncher = launcher
        wLauncher.launch(intent.data)

    }

    private fun setUpRecyclerView() {

        adapter=StatusAdapter(this,requireContext().applicationContext)
        binding.recyclerView.adapter=adapter
        adapter.updateList(list)

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun imageClicked(status: Status) {

        val dialog= Dialog(requireContext())
        dialog.setContentView(R.layout.custom_dialog)
        dialog.show()
        val btnDownload=dialog.findViewById<Button>(R.id.btn_download)
        btnDownload.setOnClickListener {
            dialog.dismiss()
            saveFiles(status)
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFiles(status: Status) {

        if(status.fileUri.endsWith(".mp4")){
            val inputStream=this.requireActivity().contentResolver.openInputStream(Uri.parse(status.fileUri))
            val fileName="${System.currentTimeMillis()}"
            try {
                val values = ContentValues()
                values.apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS
                    )
                }
                val uri = this.requireActivity().contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                val outputStream = uri?.let { this.requireActivity().contentResolver.openOutputStream(it) }!!
                if(inputStream!=null){
                    outputStream.write(inputStream.readBytes())
                    Toast.makeText(context?.applicationContext ,"video Saved", Toast.LENGTH_SHORT).show()
                    inputStream.close()
                }
                outputStream.close()


            }catch (e: Exception){
                Toast.makeText(context?.applicationContext,e.message, Toast.LENGTH_SHORT).show()
            }
        }else{
            try {
                val inputStream = this.requireActivity().contentResolver.openInputStream(Uri.parse(status.fileUri))
                val fileName = "${System.currentTimeMillis()}"
                val values = ContentValues()
                values.apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val uri = this.requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                val outputStream = uri?.let { this.requireActivity().contentResolver.openOutputStream(it) }!!
                if (inputStream != null) {
                    outputStream.write(inputStream.readBytes())
                    Toast.makeText(context?.applicationContext, "Image Saved", Toast.LENGTH_SHORT).show()
                    inputStream.close()
                }
                outputStream.close()
            }catch (e: Exception){
                Toast.makeText(context?.applicationContext,e.message, Toast.LENGTH_SHORT).show()
            }

        }

    }
    private fun accessStorage() {
        val result=readDataFromPrefs()
        if (result) {
            val sp=this.requireActivity().getSharedPreferences("DATA_PATH", MODE_PRIVATE)
            val uri= Uri.parse(sp.getString("PATH",""))
            this.requireActivity().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val fileDoc=DocumentFile.fromTreeUri(context?.applicationContext!!,uri)
            lifecycleScope.launch(Dispatchers.IO) {
                for (file: DocumentFile in fileDoc!!.listFiles()) {
                    if (!file.name!!.endsWith(".nomedia")) {
                        val status:Status
                        if(file.name!!.endsWith(".mp4")) {
                            status = Status(file.name!!, file.uri.toString(), true)
                        }
                        else{
                            status = Status(file.name!!, file.uri.toString())
                        }
                        list.add(status)
                    }
                }

                lifecycleScope.launch(Dispatchers.Main) {

                    setUpRecyclerView()
                    binding.progressBar.visibility=View.GONE
                }
            }


        } else {
            takePermission()

        }
    }


}