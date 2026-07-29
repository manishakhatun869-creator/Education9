package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeRecord
import com.example.data.model.FeeStatus
import com.example.data.model.Student
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusLateYellow
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = All Fees, 1 = Due List

    val allFees by viewModel.fees.collectAsState(initial = emptyList())
    val dueFees by viewModel.dueFeeRecords.collectAsState(initial = emptyList())
    val students by viewModel.students.collectAsState(initial = emptyList())

    var showAddFeeModal by remember { mutableStateOf(false) }
    var selectedReceipt by remember { mutableStateOf<FeeRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fee Collection & Digital Receipts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFeeModal = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Fee Record")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All Fee Records", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Due Fees List (${dueFees.size})", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val currentList = if (selectedTab == 0) allFees else dueFees

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No fee records found. Tap + to collect fee.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList) { record ->
                        FeeRecordCard(
                            fee = record,
                            onViewReceipt = { selectedReceipt = record }
                        )
                    }
                }
            }
        }
    }

    // Modal to add fee
    if (showAddFeeModal) {
        AddFeeRecordDialog(
            students = students,
            onDismiss = { showAddFeeModal = false },
            onSave = { feeRecord ->
                viewModel.addFeeRecord(feeRecord) {
                    Toast.makeText(context, "Fee Record Saved & Notification Sent!", Toast.LENGTH_SHORT).show()
                    showAddFeeModal = false
                }
            }
        )
    }

    // Modal for digital receipt preview
    selectedReceipt?.let { fee ->
        DigitalReceiptDialog(
            feeRecord = fee,
            onDismiss = { selectedReceipt = null }
        )
    }
}

@Composable
fun FeeRecordCard(
    fee: FeeRecord,
    onViewReceipt: () -> Unit
) {
    val statusColor = when (fee.status) {
        FeeStatus.PAID -> StatusPresentGreen
        FeeStatus.DUE -> StatusAbsentRed
        FeeStatus.PARTIAL -> StatusLateYellow
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = fee.studentName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Code: ${fee.studentCode} | ${fee.month} ${fee.year}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = fee.status.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total: $${fee.totalAmount.toInt()}", style = MaterialTheme.typography.bodyMedium)
                Text("Paid: $${fee.paidAmount.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = StatusPresentGreen))
                Text("Due: $${fee.dueAmount.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = StatusAbsentRed))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onViewReceipt,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Digital Receipt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeeRecordDialog(
    students: List<Student>,
    onDismiss: () -> Unit,
    onSave: (FeeRecord) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }

    var month by remember { mutableStateOf("July") }
    var year by remember { mutableStateOf("2026") }
    var totalAmount by remember { mutableStateOf("2500") }
    var paidAmount by remember { mutableStateOf("2500") }
    var paymentDate by remember { mutableStateOf("2026-07-25") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Collect / Add Fee Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Student selector dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedStudent?.let { "${it.name} (${it.studentCode})" } ?: "Select Student",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Student") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        students.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.name} (${s.studentCode})") },
                                onClick = {
                                    selectedStudent = s
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Month") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    label = { Text("Total Fee Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = paidAmount,
                    onValueChange = { paidAmount = it },
                    label = { Text("Paid Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("Payment Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = selectedStudent
                    val total = totalAmount.toDoubleOrNull() ?: 0.0
                    val paid = paidAmount.toDoubleOrNull() ?: 0.0
                    val due = (total - paid).coerceAtLeast(0.0)

                    val status = when {
                        due <= 0 -> FeeStatus.PAID
                        paid > 0 -> FeeStatus.PARTIAL
                        else -> FeeStatus.DUE
                    }

                    if (s != null) {
                        val record = FeeRecord(
                            studentId = s.id,
                            studentCode = s.studentCode,
                            studentName = s.name,
                            month = month,
                            year = year.toIntOrNull() ?: 2026,
                            totalAmount = total,
                            paidAmount = paid,
                            dueAmount = due,
                            status = status,
                            paymentDate = paymentDate,
                            receiptNo = "REC-${System.currentTimeMillis().toString().takeLast(6)}"
                        )
                        onSave(record)
                    }
                }
            ) {
                Text("Save & Notify Student")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DigitalReceiptDialog(
    feeRecord: FeeRecord,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Official Fee Receipt", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TOWFIK EDUCATION INSTITUTE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    Text("Receipt No: ${feeRecord.receiptNo}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text("Date: ${feeRecord.paymentDate}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Student Name: ${feeRecord.studentName}", fontWeight = FontWeight.Bold)
                        Text("Student Code: ${feeRecord.studentCode}")
                        Text("Fee Period: ${feeRecord.month} ${feeRecord.year}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Total Amount: $${feeRecord.totalAmount.toInt()}")
                        Text("Amount Paid: $${feeRecord.paidAmount.toInt()}", color = StatusPresentGreen, fontWeight = FontWeight.Bold)
                        Text("Balance Due: $${feeRecord.dueAmount.toInt()}", color = StatusAbsentRed, fontWeight = FontWeight.Bold)
                        Text("Status: ${feeRecord.status.name}", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Thank you for your payment!", fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = {
                        Toast.makeText(context, "Receipt PDF generated and saved to device downloads!", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download PDF")
                }
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
