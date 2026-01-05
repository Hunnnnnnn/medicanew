import type { Metadata } from 'next';
import Sidebar from '@/components/Sidebar';
import './globals.css';

export const metadata: Metadata = {
    title: 'Medica Admin Dashboard',
    description: 'Admin Dashboard untuk RS Medica',
};

export default function RootLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <html lang="id">
            <body>
                <div className="flex">
                    <Sidebar />
                    <main className="flex-1">{children}</main>
                </div>
            </body>
        </html>
    );
}
