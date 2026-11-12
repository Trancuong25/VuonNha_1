package com.example.vuonnha.ui.common

import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareDatePicker(date: LocalDate, onDismiss: () -> Unit, onPicked: (LocalDate) -> Unit) {
    // Material date picker represents calendar dates at UTC midnight.
    val state = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(enabled = state.selectedDateMillis != null, onClick = {
            state.selectedDateMillis?.let { onPicked(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
        }) { Text("Chọn") } },
        dismissButton = { TextButton(onDismiss) { Text("Huỷ") } }
    ) { DatePicker(state) }
}
