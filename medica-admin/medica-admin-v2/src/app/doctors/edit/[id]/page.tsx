'use client';

import { useState, useEffect } from 'react';
import { doc, getDoc, updateDoc } from 'firebase/firestore';
import { db } from '@/lib/firebase';
import { useRouter, useParams } from 'next/navigation';
import { ArrowLeft, Save } from 'lucide-react';
import Link from 'next/link';

export default function EditDoctorPage() {
    const router = useRouter();
    const params = useParams();
    const id = params.id as string;

    const [loading, setLoading] = useState(false);
    const [fetching, setFetching] = useState(true);
    const [formData, setFormData] = useState({
        name: '',
        specialty: '',
        hospital: '',
        rating: 0,
        patientsCount: 0,
        yearsExperience: 0,
        imageUrl: '',
        isAvailable: true,
        workingTime: '',
    });

    useEffect(() => {
        const fetchDoctor = async () => {
            try {
                const docRef = doc(db, 'doctors', id);
                const docSnap = await getDoc(docRef);

                if (docSnap.exists()) {
                    const data = docSnap.data();
                    setFormData({
                        name: data.name || '',
                        specialty: data.specialty || '',
                        hospital: data.hospital || '',
                        rating: data.rating || 0,
                        patientsCount: data.patientsCount || 0,
                        yearsExperience: data.yearsExperience || 0,
                        imageUrl: data.imageUrl || '',
                        isAvailable: data.isAvailable !== undefined ? data.isAvailable : true,
                        workingTime: data.workingTime || '',
                    });
                } else {
                    alert('Dokter tidak ditemukan!');
                    router.push('/doctors');
                }
            } catch (error: any) {
                console.error('Error fetching doctor:', error);
                alert('Gagal memuat data dokter: ' + error.message);
            } finally {
                setFetching(false);
            }
        };

        if (id) {
            fetchDoctor();
        }
    }, [id, router]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
        const { name, value, type } = e.target;

        if (type === 'checkbox') {
            setFormData(prev => ({
                ...prev,
                [name]: (e.target as HTMLInputElement).checked
            }));
        } else if (type === 'number') {
            setFormData(prev => ({
                ...prev,
                [name]: parseFloat(value) || 0
            }));
        } else {
            setFormData(prev => ({
                ...prev,
                [name]: value
            }));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);

        try {
            // Validate required fields
            if (!formData.name || !formData.specialty || !formData.hospital) {
                alert('Nama, Spesialis, dan Rumah Sakit wajib diisi!');
                setLoading(false);
                return;
            }

            const docRef = doc(db, 'doctors', id);
            await updateDoc(docRef, formData);

            alert('Dokter berhasil diperbarui!');
            router.push('/doctors');
        } catch (error: any) {
            console.error('Error updating doctor:', error);
            alert('Gagal memperbarui dokter: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    if (fetching) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-cyan-500 mx-auto mb-4"></div>
                    <p className="text-gray-500">Memuat data dokter...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 p-8">
            <div className="max-w-3xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <Link
                        href="/doctors"
                        className="inline-flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4 transition-colors"
                    >
                        <ArrowLeft size={20} />
                        <span>Kembali ke Daftar Dokter</span>
                    </Link>
                    <h1 className="text-3xl font-bold text-gray-900">Edit Dokter</h1>
                    <p className="text-gray-500 mt-2">Perbarui informasi dokter</p>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow-sm p-8 border border-gray-100">
                    <div className="space-y-6">
                        {/* Name */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Nama Lengkap <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="text"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500 transition-all"
                                placeholder="dr. Ahmad Subardjo, Sp.KG"
                                required
                            />
                        </div>

                        {/* Specialty */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Spesialis <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="text"
                                name="specialty"
                                value={formData.specialty}
                                onChange={handleChange}
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500 transition-all"
                                placeholder="Dokter Gigi"
                                required
                            />
                        </div>

                        {/* Hospital */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Rumah Sakit / Lokasi <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="text"
                                name="hospital"
                                value={formData.hospital}
                                onChange={handleChange}
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500 transition-all"
                                placeholder="RS Medica"
                                required
                            />
                        </div>

                        {/* Working Time */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Jam Kerja
                            </label>
                            <input
                                type="text"
                                name="workingTime"
                                value={formData.workingTime}
                                onChange={handleChange}
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500 transition-all"
                                placeholder="08:00 - 17:00"
                            />
                            <p className="text-xs text-gray-500 mt-1">Format: 08:00 - 17:00</p>
                        </div>

                        {/* Stats Grid */}
                        <div className="grid grid-cols-3 gap-4">
                            {/* Rating */}
                            <div>
                                <label className="block text-sm font-semibold text-gray-700 mb-2">
                                    Rating
                                </label>
                                <input
                                    type="number"
                                    name="rating"
                                    value={formData.rating}
                                    onChange={handleChange}
                                    step="0.1"
                                    min="0"
                                    max="5"
                                    className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500 transition-all"
                                    placeholder="4.8"
                                />
                            </div>

                            {/* Years Experience */}
                            <div>
                                <label className="block text-sm font-semibold text-gray-700 mb-2">
                                    Pengalaman (Tahun)
                                </label>
                                <input
                                    type="number"
                                    name="yearsExperience"
                                    value={formData.yearsExperience}
                                    onChange={handleChange}
                                    min="0"
                                    className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500 transition-all"
                                    placeholder="5"
                                />
                            </div>

                            {/* Patients Count */}
                            <div>
                                <label className="block text-sm font-semibold text-gray-700 mb-2">
                                    Jumlah Pasien
                                </label>
                                <input
                                    type="number"
                                    name="patientsCount"
                                    value={formData.patientsCount}
                                    onChange={handleChange}
                                    min="0"
                                    className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500 transition-all"
                                    placeholder="120"
                                />
                            </div>
                        </div>

                        {/* Image URL */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                URL Foto
                            </label>
                            <input
                                type="url"
                                name="imageUrl"
                                value={formData.imageUrl}
                                onChange={handleChange}
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500 transition-all"
                                placeholder="https://example.com/doctor.jpg"
                            />
                            <p className="text-xs text-gray-500 mt-1">Kosongkan jika tidak ada foto</p>
                        </div>

                        {/* Availability */}
                        <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-lg">
                            <input
                                type="checkbox"
                                name="isAvailable"
                                checked={formData.isAvailable}
                                onChange={handleChange}
                                className="w-5 h-5 text-cyan-500 border-gray-300 rounded focus:ring-cyan-500"
                                id="isAvailable"
                            />
                            <label htmlFor="isAvailable" className="text-sm font-medium text-gray-700 cursor-pointer">
                                Dokter sedang tersedia untuk menerima pasien
                            </label>
                        </div>
                    </div>

                    {/* Submit Button */}
                    <div className="mt-8 flex gap-4">
                        <button
                            type="submit"
                            disabled={loading}
                            className="flex-1 bg-cyan-500 hover:bg-cyan-600 text-white px-6 py-3 rounded-lg font-semibold flex items-center justify-center gap-2 transition-all shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {loading ? (
                                <>
                                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                                    <span>Menyimpan...</span>
                                </>
                            ) : (
                                <>
                                    <Save size={20} />
                                    <span>Simpan Perubahan</span>
                                </>
                            )}
                        </button>
                        <Link
                            href="/doctors"
                            className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-50 transition-all"
                        >
                            Batal
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}
