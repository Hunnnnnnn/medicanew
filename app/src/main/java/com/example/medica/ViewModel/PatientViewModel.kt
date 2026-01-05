package com.example.medica.ViewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PatientViewModel : ViewModel() {

    // --- 1. DATA PASIEN (Inputan User) ---
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _nik = MutableStateFlow("")
    val nik = _nik.asStateFlow()

    private val _dob = MutableStateFlow("")
    val dob = _dob.asStateFlow()

    private val _gender = MutableStateFlow("")
    val gender = _gender.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone = _phone.asStateFlow()
    private val _bookingDate = MutableStateFlow("")
    val bookingDate = _bookingDate.asStateFlow()

    private val _bookingTime = MutableStateFlow("")
    val bookingTime = _bookingTime.asStateFlow()

    // --- 2. DATA DOKTER (Selected Doctor) ---
    private val _doctorId = MutableStateFlow("")
    val doctorId = _doctorId.asStateFlow()

    private val _doctorName = MutableStateFlow("")
    val doctorName = _doctorName.asStateFlow()

    private val _specialty = MutableStateFlow("")
    val specialty = _specialty.asStateFlow()

    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()

    // --- 3. FUNGSI PENGUBAH DATA PASIEN ---
    fun onNameChange(newValue: String) { _name.value = newValue }
    fun onNikChange(newValue: String) { _nik.value = newValue }
    fun onDobChange(newValue: String) { _dob.value = newValue }
    fun onGenderChange(newValue: String) { _gender.value = newValue }
    fun onPhoneChange(newValue: String) { _phone.value = newValue }

    // --- 4. FUNGSI SIMPAN JADWAL ---
    // Panggil fungsi ini di AppNavigation saat pindah dari Book -> PatientDetails
    fun setBookingDetails(date: String, time: String) {
        _bookingDate.value = date
        _bookingTime.value = time
    }

    // --- 5. FUNGSI SIMPAN DOKTER ---
    fun setDoctorDetails(doctorId: String, doctorName: String, specialty: String, location: String) {
        _doctorId.value = doctorId
        _doctorName.value = doctorName
        _specialty.value = specialty
        _location.value = location
    }
}