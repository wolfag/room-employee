package com.example.room_employee

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.room_employee.databinding.ActivityMainBinding
import com.example.room_employee.databinding.DialogUpdateBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val employeeDao = (application as EmployeeApp).db.employeeDao()
        binding?.btnAdd?.setOnClickListener {
            addRecord(employeeDao)
        }

        lifecycleScope.launch {
            employeeDao.fetchAll().collect {
                val list = ArrayList(it)
                setupList(list, employeeDao)
            }
        }
    }

    private fun visibleList(visible: Boolean = true) {
        val rvVisible = if (visible) View.VISIBLE else View.GONE
        val tvVisible = if (visible) View.GONE else View.VISIBLE

        binding?.rvItemsList?.visibility = rvVisible
        binding?.tvNoRecordsAvailable?.visibility = tvVisible
    }

    private fun setupList(employeeList: ArrayList<EmployeeEntity>, employeeDao: EmployeeDao) {
        if (employeeList.isNotEmpty()) {
            val itemAdaptor = ItemAdaptor(employeeList, { updateId ->
                updateRecordDialog(updateId, employeeDao)
            }, { deleteId ->
                lifecycleScope.launch {
                    employeeDao.fetchEmployee(deleteId).collect {
                        if (it != null) {
                            deleteRecordDialog(deleteId, employeeDao, it)
                        }
                    }
                }
            })

            binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemsList?.adapter = itemAdaptor

            visibleList()
        } else {
            visibleList(false)
        }
    }

    fun updateRecordDialog(id: Int, employeeDao: EmployeeDao) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            employeeDao.fetchEmployee(id).collect {
                if (it != null) {
                    binding.etUpdateName.setText(it.name)
                    binding.etUpdateEmailId.setText(it.email)
                }
            }
        }
        binding.tvUpdate.setOnClickListener {
            val name = binding.etUpdateName.text.toString()
            val email = binding.etUpdateEmailId.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty()) {
                lifecycleScope.launch {
                    employeeDao.update(EmployeeEntity(id, name, email))
                    Toast.makeText(applicationContext, "Record updated", Toast.LENGTH_LONG).show()
                    updateDialog.dismiss()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Name or Email cannot be blank",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    fun deleteRecordDialog(id: Int, employeeDao: EmployeeDao, employeeEntity: EmployeeEntity) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete record")
        builder.setMessage("Are you sure you want to delete ${employeeEntity.name}")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(applicationContext, "Record deleted", Toast.LENGTH_LONG).show()
                dialogInterface.dismiss()
            }
        }

        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun addRecord(employeeDao: EmployeeDao) {
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmailId?.text.toString()

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name or email can not blank", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            employeeDao.insert(EmployeeEntity(name = name, email = email))
            Toast.makeText(applicationContext, "Record saved", Toast.LENGTH_LONG).show()

            binding?.etName?.text?.clear()
            binding?.etEmailId?.text?.clear()
        }

    }
}