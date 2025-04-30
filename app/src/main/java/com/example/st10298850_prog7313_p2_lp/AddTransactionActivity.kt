package com.example.st10298850_prog7313_p2_lp

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.st10298850_prog7313_p2_lp.data.AppDatabase
import com.example.st10298850_prog7313_p2_lp.data.Transaction
import com.example.st10298850_prog7313_p2_lp.databinding.ActivityAddTransactionBinding
import com.example.st10298850_prog7313_p2_lp.repositories.AccountRepository
import com.example.st10298850_prog7313_p2_lp.repositories.CategoryRepository
import com.example.st10298850_prog7313_p2_lp.repositories.TransactionRepository
import com.example.st10298850_prog7313_p2_lp.viewmodels.AddTransactionViewModel
import com.google.android.material.tabs.TabLayout
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var viewModel: AddTransactionViewModel
    private var receiptImageUri: Uri? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>

    private lateinit var photoUploadImage: ImageView
    private var currentPhotoPath: String = ""
    private var photoUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { entry ->
            Log.d("Permissions", "${entry.key} is ${if (entry.value) "granted" else "denied"}")
        }
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Log.d("Permissions", "All permissions granted, showing image source dialog")
            showImageSourceDialog()
        } else {
            Log.d("Permissions", "Some permissions were denied")
            Toast.makeText(this, "Permissions required to upload receipt", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUploadImage.setImageURI(photoUri)
            receiptImageUri = photoUri
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                photoUri = selectedImageUri
                photoUploadImage.setImageURI(photoUri)
                receiptImageUri = photoUri
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        photoUploadImage = binding.receiptImage

        Log.d("AddTransactionActivity", "etCategory null? ${binding.etCategory == null}")
        Log.d("AddTransactionActivity", "etAccount null? ${binding.etAccount == null}")

        photoUploadImage.setOnClickListener {
            checkPermissionsAndShowOptions()
        }

        setupViewModel()
        setupDropdowns()
        setupDatePickers()
        setupRepeatToggle()
        setupTabLayoutListener()

        binding.btnAddTransaction.setOnClickListener {
            addTransaction()
        }
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val transactionRepository = TransactionRepository(database.transactionDao())
        val categoryRepository = CategoryRepository(database.categoryDao())
        val accountRepository = AccountRepository(database.accountDao())
        val factory = AddTransactionViewModel.Factory(transactionRepository, categoryRepository, accountRepository)
        viewModel = ViewModelProvider(this, factory)[AddTransactionViewModel::class.java]

        val currentUserId = getCurrentUserId()
        viewModel.loadCategoriesForUser(currentUserId)
        viewModel.loadAccountsForUser(currentUserId)
    }

    private fun setupDropdowns() {
        viewModel.categories.observe(this) { categories ->
            Log.d("AddTransactionActivity", "Observed ${categories.size} categories")
            Log.d("AddTransactionActivity", "Category names: ${categories.map { it.name }}")
            if (categories.isEmpty()) {
                lifecycleScope.launch {
                    viewModel.insertDefaultCategories(getCurrentUserId())
                    viewModel.loadCategoriesForUser(getCurrentUserId())
                }
            } else {
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories.map { it.name })
                binding.etCategory.setAdapter(adapter)
                binding.etCategory.threshold = 1
                binding.etCategory.setOnClickListener {
                    binding.etCategory.showDropDown()
                }
            }
        }

        viewModel.accounts.observe(this) { accounts ->
            Log.d("AddTransactionActivity", "Observed ${accounts.size} accounts")
            Log.d("AddTransactionActivity", "Account names: ${accounts.map { it.name }}")
            if (accounts.isEmpty()) {
                lifecycleScope.launch {
                    viewModel.loadAccountsForUser(getCurrentUserId())
                }
            } else {
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, accounts.map { it.name })
                binding.etAccount.setAdapter(adapter)
                binding.etAccount.threshold = 1
                binding.etAccount.setOnClickListener {
                    binding.etAccount.showDropDown()
                }
            }
        }
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.etDate.setText(dateFormat.format(calendar.time))
        }

        binding.etDate.setOnClickListener {
            DatePickerDialog(
                this@AddTransactionActivity,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupRepeatToggle() {
        // Implementation as before
    }

    private fun setupTabLayoutListener() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // Expense selected
                        // Update UI or viewModel as needed
                    }
                    1 -> {
                        // Income selected
                        // Update UI or viewModel as needed
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun addTransaction() {
        val amountText = binding.tvAmount.text.toString()
        val amount = amountText.toDoubleOrNull()

        val category = binding.etCategory.text.toString()
        val account = binding.etAccount.text.toString()
        val description = binding.etDescription.text.toString()
        val dateStr = binding.etDate.text.toString()

        if (amount == null || amount <= 0 || category.isEmpty() || account.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields with valid values.", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)?.time ?: return

        // Find the selected account ID
        val selectedAccount = viewModel.accounts.value?.find { it.name == account }
        val accountId = selectedAccount?.accountId ?: return

        val transaction = Transaction(
            userId = getCurrentUserId(),
            type = if (binding.tabLayout.selectedTabPosition == 0) "Expense" else "Income",
            amount = amount,
            accountId = accountId,
            date = date,
            description = description,
            receiptPath = receiptImageUri?.toString(),
            repeat = binding.switchRepeat.isChecked
        )

        viewModel.addTransaction(transaction)
        Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun getCurrentUserId(): Long {
        // Implement this method to return the current user's ID
        // This could be stored in SharedPreferences, a database, or a singleton object
        return 1 // Placeholder, replace with actual implementation
    }

    private fun checkPermissionsAndShowOptions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        when {
            permissionsToRequest.isEmpty() -> {
                showImageSourceDialog()
            }
            permissionsToRequest.any { shouldShowRequestPermissionRationale(it) } -> {
                showPermissionRationaleDialog(permissionsToRequest.toTypedArray())
            }
            else -> {
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            }
        }
    }

    private fun showPermissionRationaleDialog(permissions: Array<String>) {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs access to your camera and storage to take and save photos. Please grant these permissions to continue.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissionLauncher.launch(permissions)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Permissions are required to upload a receipt", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("Upload Receipt")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> chooseFromGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            takePictureLauncher.launch(takePictureIntent)
        }
    }

    private fun chooseFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun getPhotoUri(): Uri? = photoUri
}