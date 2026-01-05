package com.example.medica.Repository

import com.example.medica.Model.PoliData
import com.google.firebase.firestore.FirebaseFirestore

class PoliRepository {
    private val db = FirebaseFirestore.getInstance()
    private val polisCollection = db.collection("polis")

    /**
     * Get all poli/specialties
     */
    fun getAllPolis(
        onSuccess: (List<PoliData>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        polisCollection
            .get()
            .addOnSuccessListener { documents ->
                val polis = documents.mapNotNull { document ->
                    document.toObject(PoliData::class.java)
                }
                
                // If Firestore is empty, return default hardcoded list
                if (polis.isEmpty()) {
                    onSuccess(getDefaultPolis())
                } else {
                    onSuccess(polis)
                }
            }
            .addOnFailureListener { e ->
                // On error, return default list as fallback
                onSuccess(getDefaultPolis())
            }
    }

    /**
     * Default poli list (fallback)
     */
    private fun getDefaultPolis(): List<PoliData> {
        return listOf(
            PoliData(name = "Gigi / Oral", description = "Perawatan gigi, gusi, dan mulut."),
            PoliData(name = "Mata", description = "Kesehatan mata dan penglihatan."),
            PoliData(name = "Otak (Neurologi)", description = "Gangguan sistem saraf dan otak."),
            PoliData(name = "Tulang (Ortopedi)", description = "Cedera dan penyakit tulang serta sendi."),
            PoliData(name = "Radiologi", description = "Diagnosis menggunakan pencitraan medis."),
            PoliData(name = "Nutrisi", description = "Konsultasi diet dan gizi."),
            PoliData(name = "THT", description = "Telinga, Hidung, dan Tenggorokan."),
            PoliData(name = "Penyakit Dalam", description = "Penyakit pada organ dalam dewasa."),
            PoliData(name = "Anak (Pediatri)", description = "Kesehatan bayi, anak, dan remaja."),
            PoliData(name = "Kulit & Kelamin", description = "Masalah kulit, rambut, kuku, dan seksual."),
            PoliData(name = "Jantung & Pembuluh Darah", description = "Kesehatan jantung dan sistem peredaran darah."),
            PoliData(name = "Kandungan & Kebidanan", description = "Kesehatan reproduksi wanita dan kehamilan.")
        )
    }
}
