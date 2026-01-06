'use client';

import { useEffect, useState } from 'react';
import { collection, query, where, onSnapshot } from 'firebase/firestore';
import { db } from '@/lib/firebase';
import { DashboardStats, PoliStats, HourlyStats } from '@/types';

export default function DashboardPage() {
    const [stats, setStats] = useState<DashboardStats>({
        totalToday: 0,
        inQueue: 0,
        beingServed: 0,
        completed: 0,
    });
    const [poliStats, setPoliStats] = useState<PoliStats[]>([]);
    const [hourlyStats, setHourlyStats] = useState<HourlyStats[]>([]);
    const [recentAppointments, setRecentAppointments] = useState<any[]>([]);

    const [debugInfo, setDebugInfo] = useState({
        systemTime: '',
        targetDate: '',
        firebaseStatus: 'Checking...',
        totalDocsInDB: 0
    });

    useEffect(() => {
        // Realtime listener untuk appointments
        const today = new Date();
        const dateString = today.toISOString().split('T')[0]; // Format: 2026-01-04

        console.log('=== DASHBOARD DEBUG ===');
        console.log('Dashboard querying for date:', dateString);
        console.log('Firebase config:', db ? 'Connected' : 'Not Connected');

        const appointmentsRef = collection(db, 'appointments');

        // First, get ALL appointments to debug
        const allQuery = query(appointmentsRef);

        onSnapshot(allQuery, (allSnapshot) => {
            console.log('=== ALL APPOINTMENTS ===');
            console.log('Total documents in appointments collection:', allSnapshot.docs.length);
            allSnapshot.docs.forEach(doc => {
                console.log('Document:', doc.id, doc.data());
            });
        }, (error) => {
            console.error('Error fetching all appointments:', error);
        });

        // Query appointments for today
        const q = query(
            appointmentsRef,
            where('date', '==', dateString)
        );

        // REALTIME UPDATES dengan onSnapshot
        const unsubscribe = onSnapshot(q, (snapshot) => {
            console.log('=== TODAY APPOINTMENTS ===');
            console.log('Firestore snapshot received, docs count:', snapshot.docs.length);

            let appointments = snapshot.docs.map((doc) => ({
                id: doc.id,
                ...doc.data(),
            }));

            console.log('Appointments loaded:', appointments);

            if (appointments.length === 0) {
                console.log('No appointments found for today. Dashboard will show empty state.');
            }

            // Calculate stats
            const totalToday = appointments.length;
            const inQueue = appointments.filter((app: any) => app.status === 'upcoming').length;
            const beingServed = appointments.filter((app: any) => app.status === 'in-progress').length;
            const completed = appointments.filter((app: any) => app.status === 'completed').length;

            console.log('Stats calculated:', { totalToday, inQueue, beingServed, completed });

            setStats({ totalToday, inQueue, beingServed, completed });

            // Poli stats
            const poliCount: { [key: string]: number } = {};
            appointments.forEach((app: any) => {
                const poli = app.specialty || app.location || 'Poli Umum';
                poliCount[poli] = (poliCount[poli] || 0) + 1;
            });

            const poliArray = Object.entries(poliCount)
                .map(([name, count]) => ({ name, count }))
                .sort((a, b) => b.count - a.count);

            if (poliArray.length > 0) {
                console.log('Poli stats:', poliArray);
                setPoliStats(poliArray);
            }

            // Hourly stats
            const hourCount: { [key: string]: number } = {};
            appointments.forEach((app: any) => {
                if (app.time) {
                    const hour = app.time.split(':')[0] + ':00';
                    hourCount[hour] = (hourCount[hour] || 0) + 1;
                }
            });

            const hourlyArray = Object.entries(hourCount)
                .map(([hour, count]) => ({ hour, count }))
                .sort((a, b) => a.hour.localeCompare(b.hour));

            if (hourlyArray.length > 0) {
                console.log('Hourly stats:', hourlyArray);
                setHourlyStats(hourlyArray);
            }

            // Recent appointments
            if (appointments.length > 0) {
                console.log('Setting recent appointments...');
                setRecentAppointments(appointments.slice(0, 5));
            }
        }, (error) => {
            console.error('Error in Firestore listener:', error);
            console.error('Error code:', error.code);
            console.error('Error message:', error.message);
        });

        return () => unsubscribe();
    }, []);

    const maxPoliCount = Math.max(...poliStats.map((p) => p.count), 1);

    return (
        <div className="min-h-screen bg-gray-100">
            <div className="max-w-7xl mx-auto p-8">
                {/* Header */}
                <div className="mb-6">
                    <h1 className="text-3xl font-bold text-gray-900 mb-1">Dashboard Utama</h1>
                    <p className="text-sm text-gray-600">Ringkasan Operasional RS Medica</p>
                </div>

                {/* Stats Cards */}
                <div className="grid grid-cols-4 gap-4 mb-6">
                    <div className="bg-white rounded-lg p-5 shadow">
                        <p className="text-xs text-gray-600 mb-2">Total Antrian Hari Ini</p>
                        <p className="text-5xl font-bold text-gray-900 mb-1">{stats.totalToday}</p>
                        <p className="text-xs text-cyan-500 font-medium">pasien</p>
                    </div>
                    <div className="bg-white rounded-lg p-5 shadow">
                        <p className="text-xs text-gray-600 mb-2">Dalam Antrian</p>
                        <p className="text-5xl font-bold text-gray-900 mb-1">{stats.inQueue}</p>
                        <p className="text-xs text-cyan-500 font-medium">pasien</p>
                    </div>
                    <div className="bg-white rounded-lg p-5 shadow">
                        <p className="text-xs text-gray-600 mb-2">Sedang Dilayani</p>
                        <p className="text-5xl font-bold text-gray-900 mb-1">{stats.beingServed}</p>
                        <p className="text-xs text-cyan-500 font-medium">pasien</p>
                    </div>
                    <div className="bg-white rounded-lg p-5 shadow">
                        <p className="text-xs text-gray-600 mb-2">Selesai Dilayani</p>
                        <p className="text-5xl font-bold text-gray-900 mb-1">{stats.completed}</p>
                        <p className="text-xs text-cyan-500 font-medium">pasien</p>
                    </div>
                </div>

                {/* Charts */}
                <div className="grid grid-cols-2 gap-6 mb-6">
                    {/* Bar Chart */}
                    <div className="bg-white rounded-lg p-6 shadow h-full">
                        <h2 className="text-base font-semibold text-gray-700 mb-6">Antrian Per Poli Hari Ini</h2>
                        <div className="space-y-2">
                            {poliStats.map((poli) => (
                                <div key={poli.name} className="flex items-center gap-4">
                                    <div className="w-32 text-right flex-shrink-0">
                                        <span className="text-sm text-gray-700 font-medium">{poli.name}</span>
                                    </div>
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-center h-7 bg-gray-100 rounded">
                                            <div
                                                className="h-full bg-[#4A90E2] rounded flex items-center justify-end px-2"
                                                style={{ width: `${(poli.count / maxPoliCount) * 100}%` }}
                                            >
                                                <span className="text-white text-xs font-semibold">{poli.count}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Area Chart */}
                    <div className="bg-white rounded-lg p-6 shadow h-full">
                        <h2 className="text-base font-semibold text-gray-700 mb-6">Grafik Jam Sibuk Antrian (Hari Ini)</h2>
                        <div className="relative h-48">
                            <svg className="w-full h-full" viewBox="0 0 600 200" preserveAspectRatio="xMidYMid meet">
                                <line x1="0" y1="0" x2="600" y2="0" stroke="#e5e7eb" strokeWidth="1" />
                                <line x1="0" y1="50" x2="600" y2="50" stroke="#e5e7eb" strokeWidth="1" />
                                <line x1="0" y1="100" x2="600" y2="100" stroke="#e5e7eb" strokeWidth="1" />
                                <line x1="0" y1="150" x2="600" y2="150" stroke="#e5e7eb" strokeWidth="1" />
                                <line x1="0" y1="200" x2="600" y2="200" stroke="#e5e7eb" strokeWidth="1" />

                                <defs>
                                    <linearGradient id="areaGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                                        <stop offset="0%" stopColor="#B3D9FF" stopOpacity="0.8" />
                                        <stop offset="100%" stopColor="#B3D9FF" stopOpacity="0.1" />
                                    </linearGradient>
                                </defs>

                                {hourlyStats.length > 0 && (
                                    <>
                                        <path
                                            d={`M 0 200 ${hourlyStats.map((stat, i) => {
                                                const x = (i / (hourlyStats.length - 1)) * 600;
                                                const y = 200 - (stat.count / 300) * 200;
                                                return `L ${x} ${y}`;
                                            }).join(' ')} L 600 200 Z`}
                                            fill="url(#areaGradient)"
                                        />

                                        <polyline
                                            points={hourlyStats.map((stat, i) => {
                                                const x = (i / (hourlyStats.length - 1)) * 600;
                                                const y = 200 - (stat.count / 300) * 200;
                                                return `${x},${y}`;
                                            }).join(' ')}
                                            fill="none"
                                            stroke="#4A90E2"
                                            strokeWidth="3"
                                        />

                                        {hourlyStats.map((stat, i) => {
                                            const x = (i / (hourlyStats.length - 1)) * 600;
                                            const y = 200 - (stat.count / 300) * 200;
                                            return <circle key={i} cx={x} cy={y} r="4" fill="#4A90E2" />;
                                        })}
                                    </>
                                )}
                            </svg>

                            <div className="absolute -bottom-6 left-0 right-0 flex justify-between text-xs text-gray-500 px-1">
                                {hourlyStats.map((stat, i) => (
                                    i % 2 === 0 ? <span key={i}>{stat.hour}</span> : null
                                ))}
                                <span className="text-gray-400">Jumlah Pasien</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Table */}
                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <div className="p-4 border-b">
                        <h2 className="text-base font-semibold text-gray-900">Update Terkini Antrian</h2>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead className="bg-gray-50 border-b">
                                <tr>
                                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-600">No Antrian</th>
                                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-600">Nama Pasien</th>
                                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-600">Poli</th>
                                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-600">Dokter</th>
                                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-600">Status</th>
                                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-600">Pukul</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentAppointments.map((apt, i) => (
                                    <tr key={i} className="border-b hover:bg-gray-50">
                                        <td className="px-4 py-3 text-gray-900">{apt.no || (198 + i)}</td>
                                        <td className="px-4 py-3 text-gray-900">{apt.nama || apt.pasien}</td>
                                        <td className="px-4 py-3 text-gray-900">{apt.poli || apt.specialty}</td>
                                        <td className="px-4 py-3 text-gray-900">{apt.dokter || apt.doctorName}</td>
                                        <td className="px-4 py-3 text-gray-900">{apt.status || 'Sedang Dilayani'}</td>
                                        <td className="px-4 py-3 text-gray-900">{apt.pukul || apt.time}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            {/* VISIBLE DEBUG PANEL - TEMPORARY FOR TROUBLESHOOTING */}
            {/* <div className="mt-8 p-4 bg-gray-800 text-green-400 rounded-lg font-mono text-xs overflow-x-auto">
                <h3 className="text-white font-bold mb-2 text-sm uppercase border-b border-gray-600 pb-1">🔧 Dashboard Diagnostic Panel</h3>
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <p className="text-yellow-400">System Time:</p>
                        <p suppressHydrationWarning>{typeof window !== 'undefined' ? new Date().toString() : 'Loading...'}</p>

                        <p className="text-yellow-400 mt-2">Target Query Date:</p>
                        <p className="font-bold text-white">"{new Date().toISOString().split('T')[0]}"</p>

                        <p className="text-yellow-400 mt-2">Firebase Status:</p>
                        <p>{db ? '✅ Initialized' : '❌ Not Initialized'}</p>
                    </div>
                    <div>
                        <p className="text-yellow-400">Stats Calculated:</p>
                        <pre>{JSON.stringify(stats, null, 2)}</pre>
                    </div>
                </div>

                <div className="mt-4 border-t border-gray-600 pt-2">
                    <p className="text-yellow-400">Recent Raw Data (Last 3):</p>
                    {recentAppointments.length > 0 ? (
                        <pre>{JSON.stringify(recentAppointments.slice(0, 3), null, 2)}</pre>
                    ) : (
                        <p className="text-red-400">No data found in 'recentAppointments' state.</p>
                    )}
                </div>
            </div> */}
        </div>
    );
}
