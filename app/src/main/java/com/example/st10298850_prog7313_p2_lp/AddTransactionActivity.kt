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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
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
import android.widget.EditText
import com.example.st10298850_prog7313_p2_lp.utils.UserSessionManager
import kotlinx.coroutines.launch
// Add this import at the top of the file
import com.example.st10298850_prog7313_p2_lp.utils.ImagePreviewUtil

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var viewModel: AddTransactionViewModel
    private var receiptImageUri: Uri? = null
    private lateinit var photoUploadImage: ImageView
    private var currentPhotoPath: String = ""
    private var photoUri: Uri? = null
    private var currentReceiptPath: String? = null

    /**
     * Permission request launcher for camera and storage access.
     * Handles the result of permission requests.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            showImageSourceDialog()
        } else {
            Toast.makeText(this, "Permissions required to upload receipt", Toast.LENGTH_SHORT).show()
        }
    }
    /**
     * Activity result launcher for taking a picture.
     * Handles the result of camera capture intent.
     */
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUploadImage.setImageURI(photoUri)
            receiptImageUri = photoUri
        }
    }
    /**
     * Activity result launcher for picking an image from gallery.
     * Handles the result of gallery selection intent.
     */
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

        setupUI()
        setupViewModel()
        setupDropdowns()
        setupDatePickers()
        setupTabLayoutListener()
        setupReceiptPreview()
    }
    /**
     * Sets up basic UI components and click listeners.
     */
    private fun setupUI() {
        binding.btnClose.setOnClickListener { finish() }
        photoUploadImage = binding.receiptImage
        photoUploadImage.setOnClickListener { checkPermissionsAndShowOptions() }
        binding.btnAddTransaction.setOnClickListener { addTransaction() }
    }
    /**
     * Initializes the ViewModel and loads necessary data.
     */
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
    /**
     * Sets up dropdown menus for categories and accounts.
     * Populates dropdowns with user-specific data.
     */
    private fun setupDropdowns() {
        viewModel.categories.observe(this) { categories ->
            if (categories.isEmpty()) {
                lifecycleScope.launch {
                    viewModel.insertDefaultCategories(getCurrentUserId())
                    viewModel.loadCategoriesForUser(getCurrentUserId())
                }
            } else {
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories.map { it.name })
                binding.etCategory.setAdapter(adapter)
                binding.etCategory.threshold = 1
                binding.etCategory.setOnClickListener { binding.etCategory.showDropDown() }
            }
        }

        viewModel.accounts.observe(this) { accounts ->
            if (accounts.isEmpty()) {
                lifecycleScope.launch {
                    viewModel.loadAccountsForUser(getCurrentUserId())
                }
            } else {
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, accounts.map { it.name })
                binding.etAccount.setAdapter(adapter)
                binding.etAccount.threshold = 1
                binding.etAccount.setOnClickListener { binding.etAccount.showDropDown() }
            }
        }
    }
    /**
     * Sets up date pickers for start and end dates.
     */
    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        val dateSetListener = { field: EditText ->
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                field.setText(dateFormat.format(calendar.time))
            }
        }

        binding.etStartDate.setOnClickListener {
            DatePickerDialog(
                this@AddTransactionActivity,
                dateSetListener(binding.etStartDate),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.etEndDate.setOnClickListener {
            DatePickerDialog(
                this@AddTransactionActivity,
                dateSetListener(binding.etEndDate),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
    /**
     * Sets up tab layout listener for expense/income selection.
     */
    private fun setupTabLayoutListener() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Update UI or viewModel as needed based on selected tab
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    /**
     * Sets up receipt preview functionality.
     */
private fun setupReceiptPreview() {
    photoUploadImage.setOnClickListener {
        if (currentReceiptPath != null) {
            ImagePreviewUtil.showImagePreviewDialog(this, currentReceiptPath!!)
        } else {
            showImageOptions()
        }
    }
}

    /**
     * Shows options for attaching a receipt (take photo or choose from gallery).
     */
    private fun showImageOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Attach Receipt")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> chooseFromGallery()
                }
            }
            .show()
    }
    /**
     * Adds a new transaction based on user input.
     * Validates input and saves the transaction.
     */
    private fun addTransaction() {
        val amountText = binding.tvAmount.text.toString()
        val amount = amountText.toDoubleOrNull()
        val category = binding.etCategory.text.toString()
        val account = binding.etAccount.text.toString()
        val description = binding.etDescription.text.toString()
        val startDateStr = binding.etStartDate.text.toString()
        val endDateStr = binding.etEndDate.text.toString()
        val receiptPath = currentReceiptPath ?: receiptImageUri?.toString()
        Log.d(TAG, "Saving transaction with receipt path: $receiptPath")


        if (amount == null || amount <= 0 || category.isEmpty() || account.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields with valid values.", Toast.LENGTH_SHORT).show()
            return
        }

        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(startDateStr)?.time ?: return
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(endDateStr)?.time ?: return

        val selectedAccount = viewModel.accounts.value?.find { it.name == account }
        val accountId = selectedAccount?.accountId ?: return

        val transaction = Transaction(
            userId = getCurrentUserId(),
            type = if (binding.tabLayout.selectedTabPosition == 0) "Expense" else "Income",
            amount = amount,
            accountId = accountId,
            startDate = startDate,
            endDate = endDate,
            description = description,
            receiptPath = currentReceiptPath,
            category = category
        )

viewModel.addTransaction(transaction)
Log.d("AddTransaction", "Saved transaction with receipt path: ${transaction.receiptPath}")
Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()
finish()
        finish()
    }
    /**
     * Retrieves the current user's ID.
     * Redirects to login if user is not authenticated.
     */
    private fun getCurrentUserId(): Long {
        val userId = UserSessionManager.getUserId(this)
        if (userId == -1L) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        return userId
    }
    /**
     * Shows a dialog explaining why permissions are needed.
     */
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
            permissionsToRequest.isEmpty() -> showImageSourceDialog()
            permissionsToRequest.any { shouldShowRequestPermissionRationale(it) } -> showPermissionRationaleDialog(permissionsToRequest.toTypedArray())
            else -> requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    /**
     * Shows a dialog explaining why permissions are needed.
     */
    private fun showPermissionRationaleDialog(permissions: Array<String>) {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs access to your camera and storage to take and save photos. Please grant these permissions to continue.")
            .setPositiveButton("OK") { _, _ -> requestPermissionLauncher.launch(permissions) }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Permissions are required to upload a receipt", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
    /**
     * Shows a dialog for selecting image source (camera or gallery).
     */
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
    /**
     * Launches the camera to take a photo for the receipt.
     */
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

        // After launching the camera intent, add these lines:
        Log.d("ImageSave", "Image should be saved to: ${it.absolutePath}")
        // Check if file exists immediately after saving
        if (it.exists()) {
            Log.d("ImageSave", "File exists after saving")
            currentReceiptPath = it.absolutePath
        } else {
            Log.e("ImageSave", "File does not exist after saving")
        }
    }

    }
    /**
     * Launches the gallery to choose an existing photo for the receipt.
     */
    private fun chooseFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
    /**
     * Creates a file to store the captured image.
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    companion object {
        private const val TAG = "AddTransactionActivity"
    }
}

//    private fun setupRepeatFunctionality() {
//        // Setup repeat switch
//        binding.switchRepeat.setOnCheckedChangeListener { _, isChecked ->
//            binding.repeatSettingsContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
//        }
//
//        // Setup repeat interval spinner
//        repeatIntervalAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, repeatIntervals)
//        repeatIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.spinnerRepeatInterval.adapter = repeatIntervalAdapter
//
//        // Setup repeat until date picker
//        binding.etRepeatUntil.setOnClickListener {
//            showDatePickerDialog(binding.etRepeatUntil)
//        }
//
//        // Setup repeat count input
//        binding.etRepeatCount.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                if (!s.isNullOrEmpty()) {
//                    binding.etRepeatUntil.isEnabled = false
//                    binding.etRepeatUntil.text?.clear()
//                } else {
//                    binding.etRepeatUntil.isEnabled = true
//                }
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })
//    }


//private fun showDatePickerDialog(editText: EditText) {
//    val calendar = Calendar.getInstance()
//    val year = calendar.get(Calendar.YEAR)
//    val month = calendar.get(Calendar.MONTH)
//    val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//    val datePickerDialog = DatePickerDialog(
//        this,
//        { _, selectedYear, selectedMonth, selectedDay ->
//            val selectedDate = Calendar.getInstance()
//            selectedDate.set(selectedYear, selectedMonth, selectedDay)
//            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            editText.setText(dateFormat.format(selectedDate.time))
//        },
//        year, month, day
//    )
//
//    datePickerDialog.show()
//}
