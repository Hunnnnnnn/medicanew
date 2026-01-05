'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Home, FileText, Stethoscope } from 'lucide-react';

export default function Sidebar() {
    const pathname = usePathname();

    const menuItems = [
        { name: 'Dashboard Utama', path: '/dashboard', icon: Home },
        { name: 'Manajemen Dokter', path: '/doctors', icon: Stethoscope }, // Using Stethoscope for Doctors
        { name: 'Pendaftaran Poli', path: '/poli', icon: FileText }, // Changed icon for differentiation
        { name: 'Manajemen Artikel', path: '/articles', icon: FileText },
    ];

    return (
        <div className="w-64 min-h-screen bg-gradient-to-b from-cyan-500 to-cyan-600 text-white flex flex-col">
            {/* Logo */}
            <div className="p-6 flex items-center justify-center">
                <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center">
                    <svg
                        className="w-10 h-10 text-cyan-500"
                        fill="currentColor"
                        viewBox="0 0 24 24"
                        width="40"
                        height="40"
                    >
                        <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
                    </svg>
                </div>
            </div>

            {/* Navigation Menu */}
            <nav className="flex-1 px-4">
                {menuItems.map((item) => {
                    const Icon = item.icon;
                    const isActive = pathname === item.path;

                    return (
                        <Link
                            key={item.path}
                            href={item.path}
                            className={`
                flex items-center gap-3 px-4 py-3 mb-2 rounded-lg transition-all
                ${isActive
                                    ? 'bg-cyan-700 bg-opacity-50 font-semibold'
                                    : 'hover:bg-cyan-700 hover:bg-opacity-30'
                                }
              `}
                        >
                            <Icon className="w-5 h-5" />
                            <span>{item.name}</span>
                        </Link>
                    );
                })}
            </nav>

            {/* Footer */}
            <div className="p-4 text-center text-cyan-100 text-xs border-t border-cyan-400 border-opacity-30">
                <p>RS Medica Admin</p>
                <p className="mt-1 opacity-75">v2.0.0</p>
            </div>
        </div>
    );
}
