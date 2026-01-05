package com.example.medica.ViewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class BookAppointmentState(
    val selectedDate: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH), // Default = today
    val selectedTime: String = "10.00",
    val currentMonth: String = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time),
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val currentMonthInt: Int = Calendar.getInstance().get(Calendar.MONTH), // 0-based (0 = January)
    val daysInMonth: Int = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH),
    val firstDayOfWeek: Int = 0 // Day of week for first day of month (0 = Monday, 6 = Sunday)
)

class BookAppointmentViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(BookAppointmentState())
    val state: StateFlow<BookAppointmentState> = _state.asStateFlow()
    
    init {
        // Initialize with current month data
        updateMonthData()
    }
    
    fun onDateSelected(date: Int) {
        _state.update { it.copy(selectedDate = date) }
    }
    
    fun onTimeSelected(time: String) {
        _state.update { it.copy(selectedTime = time) }
    }
    
    fun nextMonth() {
        val newMonth = (_state.value.currentMonthInt + 1) % 12
        val newYear = if (newMonth == 0) _state.value.currentYear + 1 else _state.value.currentYear
        
        _state.update { it.copy(currentMonthInt = newMonth, currentYear = newYear) }
        updateMonthData()
    }
    
    fun previousMonth() {
        val newMonth = if (_state.value.currentMonthInt == 0) 11 else _state.value.currentMonthInt - 1
        val newYear = if (_state.value.currentMonthInt == 0) _state.value.currentYear - 1 else _state.value.currentYear
        
        _state.update { it.copy(currentMonthInt = newMonth, currentYear = newYear) }
        updateMonthData()
    }
    
    private fun updateMonthData() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, _state.value.currentYear)
        calendar.set(Calendar.MONTH, _state.value.currentMonthInt)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Convert to Monday = 0
        
        _state.update {
            it.copy(
                currentMonth = monthName,
                daysInMonth = daysInMonth,
                firstDayOfWeek = firstDayOfWeek,
                selectedDate = if (it.selectedDate > daysInMonth) daysInMonth else it.selectedDate
            )
        }
    }
    
    fun getFormattedDate(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, _state.value.currentYear)
        calendar.set(Calendar.MONTH, _state.value.currentMonthInt)
        calendar.set(Calendar.DAY_OF_MONTH, _state.value.selectedDate)
        
        // Use ISO 8601 format (yyyy-MM-dd) for Firestore compatibility
        return SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.time)
    }
}