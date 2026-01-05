'use client';

import { useState, useEffect } from 'react';
import { collection, getDocs, deleteDoc, doc, onSnapshot } from 'firebase/firestore';
import { db } from '@/lib/firebase';
import { Doctor } from '@/types';
import Link from 'next/link';
import { Plus, Edit, Trash2, Search, Star } from 'lucide-react';

export default function DoctorsPage() {
    const [doctors, setDoctors] = useState<Doctor[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        const doctorsRef = collection(db, 'doctors'); // Assuming collection name is 'doctors'

        // Listen for realtime updates
        const unsubscribe = onSnapshot(doctorsRef, (snapshot) => {
            const docs = snapshot.docs.map((doc) => ({
                id: doc.id,
                ...doc.data(),
            })) as Doctor[];
            setDoctors(docs);
            setLoading(false);
        }, (error) => {
            console.error('Error fetching doctors:', error);
            setLoading(false);
        });

        return () => unsubscribe();
    }, []);

    const handleDelete = async (id: string) => {
        if (confirm('Apakah Anda yakin ingin menghapus dokter ini?')) {
            try {
                await deleteDoc(doc(db, 'doctors', id));
                alert('Dokter berhasil dihapus!');
            } catch (error: any) {
                console.error('Error deleting doctor:', error);
                alert('Gagal menghapus dokter: ' + error.message);
            }
        }
    };

    const filteredDoctors = doctors.filter(doc =>
        doc.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        doc.specialty.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="min-h-screen bg-gray-50 p-8 pb-32">
            {/* Header */}
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Manajemen Dokter</h1>
                    <p className="text-gray-500 mt-1">Kelola data dokter RS Medica</p>
                </div>
                <Link
                    href="/doctors/add"
                    className="bg-cyan-500 hover:bg-cyan-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 transition-colors shadow-sm"
                >
                    <Plus size={20} />
                    <span>Tambah Dokter</span>
                </Link>
            </div>

            {/* Search and Filters */}
            <div className="bg-white p-4 rounded-lg shadow-sm mb-6 flex items-center gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                    <input
                        type="text"
                        placeholder="Cari nama dokter atau spesialis..."
                        className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500 transition-all"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                {/* Add filters if needed here */}
            </div>

            {/* Stats Overview (Optional but nice) */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <p className="text-gray-500 text-sm font-medium">Total Dokter</p>
                    <p className="text-3xl font-bold text-gray-900 mt-2">{doctors.length}</p>
                </div>
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <p className="text-gray-500 text-sm font-medium">Dokter Tersedia (Available)</p>
                    <p className="text-3xl font-bold text-cyan-600 mt-2">
                        {doctors.filter(d => d.isAvailable).length}
                    </p>
                </div>
            </div>

            {/* Doctors List */}
            {loading ? (
                <div className="flex justify-center py-20">
                    <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-cyan-500"></div>
                </div>
            ) : filteredDoctors.length === 0 ? (
                <div className="bg-white rounded-lg p-12 text-center shadow-sm">
                    <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                        <Search className="text-gray-400" size={32} />
                    </div>
                    <h3 className="text-lg font-medium text-gray-900">Tidak ada dokter ditemukan</h3>
                    <p className="text-gray-500 mt-2">Coba kata kunci lain atau tambahkan dokter baru.</p>
                </div>
            ) : (
                <div className="bg-white rounded-xl shadow-sm overflow-hidden border border-gray-100">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="bg-gray-50 border-b border-gray-100">
                                    <th className="px-6 py-4 font-semibold text-gray-600 text-sm">Dokter</th>
                                    <th className="px-6 py-4 font-semibold text-gray-600 text-sm">Spesialis</th>
                                    <th className="px-6 py-4 font-semibold text-gray-600 text-sm">RS / Lokasi</th>
                                    <th className="px-6 py-4 font-semibold text-gray-600 text-sm">Rating</th>
                                    <th className="px-6 py-4 font-semibold text-gray-600 text-sm">Status</th>
                                    <th className="px-6 py-4 font-semibold text-gray-600 text-sm text-right">Aksi</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100">
                                {filteredDoctors.map((doctor) => (
                                    <tr key={doctor.id} className="hover:bg-gray-50 transition-colors">
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-4">
                                                <div className="w-12 h-12 rounded-full bg-gray-200 overflow-hidden flex-shrink-0 border-2 border-white shadow-sm">
                                                    {doctor.imageUrl ? (
                                                        <img src={doctor.imageUrl} alt={doctor.name} className="w-full h-full object-cover" onError={(e) => { (e.target as HTMLImageElement).src = 'https://ui-avatars.com/api/?name=' + doctor.name }} />
                                                    ) : (
                                                        <div className="w-full h-full flex items-center justify-center bg-cyan-100 text-cyan-600 font-bold text-lg">
                                                            {doctor.name.charAt(0)}
                                                        </div>
                                                    )}
                                                </div>
                                                <div>
                                                    <p className="font-semibold text-gray-900">{doctor.name}</p>
                                                    <p className="text-xs text-gray-500">{doctor.yearsExperience}th Pengalaman</p>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-gray-700">{doctor.specialty}</td>
                                        <td className="px-6 py-4 text-gray-600 text-sm">{doctor.hospital}</td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-1 text-yellow-500 font-medium">
                                                <Star className="fill-yellow-500" size={16} />
                                                <span>{doctor.rating}</span>
                                            </div>
                                            <p className="text-xs text-gray-400 mt-1">{doctor.patientsCount} Pasien</p>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${doctor.isAvailable
                                                    ? 'bg-green-100 text-green-800'
                                                    : 'bg-red-100 text-red-800'
                                                }`}>
                                                {doctor.isAvailable ? 'Available' : 'Unavailable'}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <div className="flex items-center justify-end gap-2">
                                                <Link
                                                    href={`/doctors/edit/${doctor.id}`}
                                                    className="p-2 text-gray-400 hover:text-cyan-600 hover:bg-cyan-50 rounded-lg transition-all"
                                                    title="Edit"
                                                >
                                                    <Edit size={18} />
                                                </Link>
                                                <button
                                                    onClick={() => handleDelete(doctor.id)}
                                                    className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all"
                                                    title="Hapus"
                                                >
                                                    <Trash2 size={18} />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
}
