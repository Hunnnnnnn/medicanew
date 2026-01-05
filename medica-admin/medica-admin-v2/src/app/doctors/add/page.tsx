'use client';

import { useState } from 'react';
import { collection, addDoc } from 'firebase/firestore';
import { getStorage, ref, uploadBytesResumable, getDownloadURL } from 'firebase/storage';
import { db } from '@/lib/firebase';
import { useRouter } from 'next/navigation';
import { ArrowLeft, Save, Upload, X } from 'lucide-react';
import Link from 'next/link';

export default function AddDoctorPage() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [imagePreview, setImagePreview] = useState<string>('');
    const [uploadProgress, setUploadProgress] = useState(0);
    const [isUploading, setIsUploading] = useState(false);
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

    // Upload image to Firebase Storage
    const uploadImage = async (file: File): Promise<string> => {
        const storage = getStorage();
        const fileName = `${Date.now()}_${file.name}`;
        const storageRef = ref(storage, `doctors/${fileName}`);

        setIsUploading(true);

        return new Promise((resolve, reject) => {
            const uploadTask = uploadBytesResumable(storageRef, file);

            uploadTask.on(
                'state_changed',
                (snapshot) => {
                    const progress = (snapshot.bytesTransferred / snapshot.totalBytes) * 100;
                    setUploadProgress(progress);
                },
                (error) => {
                    setIsUploading(false);
                    reject(error);
                },
                async () => {
                    const downloadURL = await getDownloadURL(uploadTask.snapshot.ref);
                    setIsUploading(false);
                    resolve(downloadURL);
                }
            );
        });
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

            // Upload image first if file selected
            let finalImageUrl = formData.imageUrl;

            if (imageFile) {
                try {
                    finalImageUrl = await uploadImage(imageFile);
                } catch (error) {
                    console.error('Error uploading image:', error);
                    alert('Gagal upload gambar, tetapi data dokter akan disimpan');
                }
            }

            // Add doctor to Firestore with uploaded image URL
            await addDoc(collection(db, 'doctors'), {
                ...formData,
                imageUrl: finalImageUrl
            });

            alert('✅ Dokter berhasil ditambahkan!');
            router.push('/doctors');
        } catch (error: any) {
            console.error('Error adding doctor:', error);
            alert('❌ Gagal menambahkan dokter: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

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
                    <h1 className="text-3xl font-bold text-gray-900">Tambah Dokter Baru</h1>
                    <p className="text-gray-500 mt-2">Lengkapi informasi dokter di bawah ini</p>
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

                        {/* Image Upload */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Foto Dokter
                            </label>

                            {/* File Input */}
                            <div className="space-y-4">
                                <div className="flex items-center justify-center w-full">
                                    <label className="flex flex-col items-center justify-center w-full h-40 border-2 border-gray-300 border-dashed rounded-lg cursor-pointer bg-gray-50 hover:bg-gray-100 transition-colors">
                                        <div className="flex flex-col items-center justify-center pt-5 pb-6">
                                            <Upload className="w-10 h-10 mb-3 text-gray-400" />
                                            <p className="mb-2 text-sm text-gray-500">
                                                <span className="font-semibold">Klik untuk upload</span> atau drag & drop
                                            </p>
                                            <p className="text-xs text-gray-500">PNG, JPG, JPEG (MAX. 5MB)</p>
                                        </div>
                                        <input
                                            type="file"
                                            className="hidden"
                                            accept="image/*"
                                            onChange={(e) => {
                                                const file = e.target.files?.[0];
                                                if (file) {
                                                    if (file.size > 5 * 1024 * 1024) {
                                                        alert('File terlalu besar! Maksimal 5MB');
                                                        return;
                                                    }
                                                    setImageFile(file);
                                                    // Create preview
                                                    const reader = new FileReader();
                                                    reader.onloadend = () => {
                                                        setImagePreview(reader.result as string);
                                                    };
                                                    reader.readAsDataURL(file);
                                                }
                                            }}
                                        />
                                    </label>
                                </div>

                                {/* Upload Progress */}
                                {isUploading && (
                                    <div className="w-full">
                                        <div className="flex items-center justify-between mb-1">
                                            <span className="text-sm text-gray-600">Uploading...</span>
                                            <span className="text-sm font-semibold text-cyan-600">{Math.round(uploadProgress)}%</span>
                                        </div>
                                        <div className="w-full bg-gray-200 rounded-full h-2">
                                            <div
                                                className="bg-cyan-500 h-2 rounded-full transition-all duration-300"
                                                style={{ width: `${uploadProgress}%` }}
                                            />
                                        </div>
                                    </div>
                                )}

                                {/* Image Preview */}
                                {(imagePreview || formData.imageUrl) && (
                                    <div className="relative w-32 h-32 rounded-lg overflow-hidden border-2 border-gray-200">
                                        <img
                                            src={imagePreview || formData.imageUrl}
                                            alt="Preview"
                                            className="w-full h-full object-cover"
                                        />
                                        <button
                                            type="button"
                                            onClick={() => {
                                                setImageFile(null);
                                                setImagePreview('');
                                                setFormData({ ...formData, imageUrl: '' });
                                            }}
                                            className="absolute top-1 right-1 bg-red-500 text-white p-1 rounded-full hover:bg-red-600 transition-colors"
                                        >
                                            <X size={16} />
                                        </button>
                                    </div>
                                )}

                                {/* Manual URL Input (Optional) */}
                                <div>
                                    <p className="text-xs text-gray-500 mb-2">Atau masukkan URL foto manual:</p>
                                    <input
                                        type="url"
                                        name="imageUrl"
                                        value={formData.imageUrl}
                                        onChange={(e) => {
                                            handleChange(e);
                                            if (e.target.value) {
                                                setImageFile(null);
                                                setImagePreview('');
                                            }
                                        }}
                                        className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500 transition-all text-sm"
                                        placeholder="https://example.com/doctor.jpg"
                                    />
                                </div>
                            </div>
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
                                    <span>Simpan Dokter</span>
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
