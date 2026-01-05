'use client';

import { useState, useEffect } from 'react';
import { collection, onSnapshot, doc, updateDoc, deleteDoc, addDoc, serverTimestamp } from 'firebase/firestore';
import { db } from '@/lib/firebase';
import { Appointment } from '@/types';
import { Search, Calendar, Clock, User, Trash2, Edit, X, Check } from 'lucide-react';

export default function PoliPage() {
    const [appointments, setAppointments] = useState<Appointment[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState<string>('all');
    const [editingId, setEditingId] = useState<string | null>(null);
    const [rescheduleData, setRescheduleData] = useState({ date: '', time: '' });

    useEffect(() => {
        const appointmentsRef = collection(db, 'appointments');

        const unsubscribe = onSnapshot(appointmentsRef, (snapshot) => {
            console.log('📋 Firestore snapshot received, doc count:', snapshot.docs.length);

            const docs = snapshot.docs.map((doc) => {
                const data = doc.data();
                const appointmentWithId = {
                    id: doc.id,
                    ...data,
                };
                console.log('📄 Processing doc:', doc.id, '→', appointmentWithId);
                return appointmentWithId;
            }) as Appointment[];

            // Sort by date (newest first)
            docs.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

            console.log('✅ Final appointments array:', docs);
            console.log('🔍 First appointment ID check:', docs[0]?.id);

            setAppointments(docs);
            setLoading(false);
        }, (error) => {
            console.error('Error fetching appointments:', error);
            setLoading(false);
        });

        return () => unsubscribe();
    }, []);

    const handleReschedule = async (id: string) => {
        console.log('handleReschedule called with id:', id);

        if (!id || id.trim() === '') {
            alert('❌ Error: Appointment ID is missing!');
            console.error('Missing appointment ID');
            return;
        }

        if (!rescheduleData.date || !rescheduleData.time) {
            alert('Tanggal dan waktu harus diisi!');
            return;
        }

        try {
            const appointment = appointments.find(apt => apt.id === id);
            if (!appointment) {
                alert('❌ Error: Appointment not found!');
                console.error('Appointment not found for id:', id);
                return;
            }

            console.log('Found appointment:', appointment);

            const oldDateTime = `${appointment.date} ${appointment.time}`;
            const newDateTime = `${rescheduleData.date} ${rescheduleData.time}`;

            console.log('Updating appointment in Firestore...');

            // Update appointment
            await updateDoc(doc(db, 'appointments', id), {
                date: rescheduleData.date,
                time: rescheduleData.time,
                status: 'rescheduled'
            });

            console.log('Creating notification...');

            // Create notification for patient
            await addDoc(collection(db, 'notifications'), {
                userId: appointment.userId,
                appointmentId: id,
                type: 'rescheduled',
                title: 'Jadwal Berubah',
                message: `Anda telah berhasil mengubah janji temu dengan ${appointment.doctorName} pada tanggal ${oldDateTime}. Silakan datang sesuai jadwal baru Anda pada tanggal ${newDateTime}, Jangan lupa aktifkan pengingat alarm Anda.`,
                timestamp: serverTimestamp(),
                isRead: false,
                doctorName: appointment.doctorName,
                originalDate: oldDateTime,
                newDate: newDateTime
            });

            console.log('✅ Reschedule successful!');
            alert('✅ Jadwal berhasil diubah dan notifikasi dikirim!');
            setEditingId(null);
            setRescheduleData({ date: '', time: '' });
        } catch (error: any) {
            console.error('Error rescheduling:', error);
            alert('❌ Gagal mengubah jadwal: ' + error.message);
        }
    };

    const handleStatusChange = async (id: string, newStatus: string) => {
        try {
            await updateDoc(doc(db, 'appointments', id), { status: newStatus });
            alert('Status berhasil diperbarui!');
        } catch (error: any) {
            console.error('Error updating status:', error);
            alert('Gagal update status: ' + error.message);
        }
    };

    const handleCancel = async (id: string) => {
        if (confirm('Batalkan appointment ini? Pasien akan menerima notifikasi.')) {
            try {
                const appointment = appointments.find(apt => apt.id === id);
                if (!appointment) return;

                const dateTime = `${appointment.date} ${appointment.time}`;

                // Update appointment status to cancelled
                await updateDoc(doc(db, 'appointments', id), {
                    status: 'cancelled'
                });

                // Create notification for patient
                await addDoc(collection(db, 'notifications'), {
                    userId: appointment.userId,
                    appointmentId: id,
                    type: 'cancelled',
                    title: 'Janji Temu Dibatalkan!',
                    message: `Anda telah berhasil membatalkan janji temu dengan Dr. ${appointment.doctorName} pada tanggal ${dateTime}, pukul ${appointment.time}, 80% dari dana akan dikembalikan ke akun Anda.`,
                    timestamp: serverTimestamp(),
                    isRead: false,
                    doctorName: appointment.doctorName,
                    originalDate: dateTime,
                    newDate: ''
                });

                alert('✅ Appointment dibatalkan dan notifikasi dikirim!');
            } catch (error: any) {
                console.error('Error cancelling:', error);
                alert('❌ Gagal membatalkan: ' + error.message);
            }
        }
    };

    const handleDelete = async (id: string) => {
        if (confirm('HAPUS PERMANEN appointment ini dari database?')) {
            try {
                await deleteDoc(doc(db, 'appointments', id));
                alert('✅ Appointment berhasil dihapus!');
            } catch (error: any) {
                console.error('Error deleting:', error);
                alert('❌ Gagal menghapus: ' + error.message);
            }
        }
    };

    const filteredAppointments = appointments.filter(apt => {
        const matchesSearch =
            apt.doctorName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            apt.specialty?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            apt.location?.toLowerCase().includes(searchTerm.toLowerCase());

        const matchesStatus = statusFilter === 'all' || apt.status === statusFilter;

        return matchesSearch && matchesStatus;
    });

    const stats = {
        total: appointments.length,
        upcoming: appointments.filter(a => a.status === 'upcoming').length,
        completed: appointments.filter(a => a.status === 'completed').length,
        cancelled: appointments.filter(a => a.status === 'cancelled').length,
    };

    return (
        <div className="min-h-screen bg-gray-50 p-8">
            <div className="mb-8">
                <h1 className="text-3xl font-bold text-gray-900">Pendaftaran Poli</h1>
                <p className="text-gray-500 mt-1">Kelola janji temu pasien</p>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-4 gap-4 mb-6">
                <div className="bg-white p-5 rounded-lg shadow-sm border border-gray-100">
                    <p className="text-sm text-gray-600">Total Pendaftaran</p>
                    <p className="text-3xl font-bold text-gray-900 mt-1">{stats.total}</p>
                </div>
                <div className="bg-white p-5 rounded-lg shadow-sm border border-gray-100">
                    <p className="text-sm text-gray-600">Upcoming</p>
                    <p className="text-3xl font-bold text-blue-600 mt-1">{stats.upcoming}</p>
                </div>
                <div className="bg-white p-5 rounded-lg shadow-sm border border-gray-100">
                    <p className="text-sm text-gray-600">Completed</p>
                    <p className="text-3xl font-bold text-green-600 mt-1">{stats.completed}</p>
                </div>
                <div className="bg-white p-5 rounded-lg shadow-sm border border-gray-100">
                    <p className="text-sm text-gray-600">Cancelled</p>
                    <p className="text-3xl font-bold text-red-600 mt-1">{stats.cancelled}</p>
                </div>
            </div>

            {/* Filters */}
            <div className="bg-white p-4 rounded-lg shadow-sm mb-6 flex items-center gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                    <input
                        type="text"
                        placeholder="Cari dokter, spesialis, atau poli..."
                        className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <select
                    className="px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                >
                    <option value="all">Semua Status</option>
                    <option value="upcoming">Upcoming</option>
                    <option value="completed">Completed</option>
                    <option value="cancelled">Cancelled</option>
                </select>
            </div>

            {/* Appointments Table */}
            {loading ? (
                <div className="flex justify-center py-20">
                    <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-cyan-500"></div>
                </div>
            ) : filteredAppointments.length === 0 ? (
                <div className="bg-white rounded-lg p-12 text-center shadow-sm">
                    <Search className="mx-auto text-gray-400 mb-4" size={48} />
                    <h3 className="text-lg font-medium text-gray-900">Tidak ada appointment</h3>
                    <p className="text-gray-500 mt-2">Coba filter atau kata kunci lain</p>
                </div>
            ) : (
                <div className="bg-white rounded-xl shadow-sm overflow-hidden">
                    <table className="w-full">
                        <thead className="bg-gray-50 border-b">
                            <tr>
                                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-600">Pasien</th>
                                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-600">Dokter</th>
                                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-600">Poli</th>
                                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-600">Tanggal & Waktu</th>
                                <th className="px-6 py-4 text-left text-sm font-semibold text-gray-600">Status</th>
                                <th className="px-6 py-4 text-right text-sm font-semibold text-gray-600">Aksi</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y">
                            {filteredAppointments.map((apt) => (
                                <tr key={apt.id} className="hover:bg-gray-50">
                                    <td className="px-6 py-4">
                                        <div className="flex items-center gap-3">
                                            <div className="w-8 h-8 rounded-full bg-cyan-100 flex items-center justify-center">
                                                <User className="text-cyan-600" size={16} />
                                            </div>
                                            <div>
                                                <p className="font-medium text-gray-900">
                                                    {(apt as any).patientName || 'Unknown Patient'}
                                                </p>
                                                <p className="text-xs text-gray-400">{apt.userId?.substring(0, 12) || 'No ID'}...</p>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <p className="font-medium text-gray-900">{apt.doctorName}</p>
                                        <p className="text-xs text-gray-500">{apt.specialty}</p>
                                    </td>
                                    <td className="px-6 py-4 text-gray-700">{apt.location}</td>
                                    <td className="px-6 py-4">
                                        {editingId === apt.id ? (
                                            <div className="flex gap-2">
                                                <input
                                                    type="date"
                                                    className="px-2 py-1 border rounded text-sm"
                                                    value={rescheduleData.date}
                                                    onChange={(e) => setRescheduleData(prev => ({ ...prev, date: e.target.value }))}
                                                />
                                                <input
                                                    type="time"
                                                    className="px-2 py-1 border rounded text-sm"
                                                    value={rescheduleData.time}
                                                    onChange={(e) => setRescheduleData(prev => ({ ...prev, time: e.target.value }))}
                                                />
                                                <button
                                                    onClick={() => handleReschedule(apt.id)}
                                                    className="px-3 py-1 bg-cyan-500 text-white rounded text-sm hover:bg-cyan-600"
                                                >
                                                    Save
                                                </button>
                                                <button
                                                    onClick={() => setEditingId(null)}
                                                    className="px-3 py-1 bg-gray-200 text-gray-700 rounded text-sm hover:bg-gray-300"
                                                >
                                                    Cancel
                                                </button>
                                            </div>
                                        ) : (
                                            <div>
                                                <div className="flex items-center gap-2 text-gray-700">
                                                    <Calendar size={16} />
                                                    <span className="text-sm">{apt.date}</span>
                                                </div>
                                                <div className="flex items-center gap-2 text-gray-500 mt-1">
                                                    <Clock size={16} />
                                                    <span className="text-sm">{apt.time}</span>
                                                </div>
                                            </div>
                                        )}
                                    </td>
                                    <td className="px-6 py-4">
                                        <select
                                            value={apt.status}
                                            onChange={(e) => handleStatusChange(apt.id, e.target.value)}
                                            className={`px-3 py-1 rounded-full text-xs font-medium border-0 ${apt.status === 'upcoming' ? 'bg-blue-100 text-blue-800' :
                                                apt.status === 'completed' ? 'bg-green-100 text-green-800' :
                                                    'bg-red-100 text-red-800'
                                                }`}
                                        >
                                            <option value="upcoming">Upcoming</option>
                                            <option value="completed">Completed</option>
                                            <option value="cancelled">Cancelled</option>
                                        </select>
                                    </td>
                                    <td className="px-6 py-4">
                                        <div className="flex items-center justify-end gap-2">
                                            <button
                                                onClick={() => {
                                                    console.log('🔘 Edit button clicked! apt object:', apt);
                                                    console.log('🔘 apt.id value:', apt.id);
                                                    console.log('🔘 apt.id type:', typeof apt.id);
                                                    setEditingId(apt.id);
                                                    setRescheduleData({ date: apt.date, time: apt.time });
                                                }}
                                                disabled={apt.status === 'cancelled'}
                                                className="p-2 text-gray-400 hover:text-cyan-600 hover:bg-cyan-50 rounded-lg disabled:opacity-30 disabled:cursor-not-allowed"
                                                title="Reschedule"
                                            >
                                                <Edit size={18} />
                                            </button>
                                            <button
                                                onClick={() => handleCancel(apt.id)}
                                                disabled={apt.status === 'cancelled'}
                                                className="p-2 text-gray-400 hover:text-orange-600 hover:bg-orange-50 rounded-lg disabled:opacity-30 disabled:cursor-not-allowed"
                                                title="Cancel (Send Notification)"
                                            >
                                                <X size={18} />
                                            </button>
                                            <button
                                                onClick={() => handleDelete(apt.id)}
                                                className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg"
                                                title="Delete Permanently"
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
            )}
        </div>
    );
}
