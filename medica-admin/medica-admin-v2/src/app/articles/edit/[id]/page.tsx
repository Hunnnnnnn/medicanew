'use client';

import { useState, useEffect } from 'react';
import { doc, getDoc, updateDoc } from 'firebase/firestore';
import { db } from '@/lib/firebase';
import { useRouter, useParams } from 'next/navigation';
import { ArrowLeft, Save } from 'lucide-react';
import Link from 'next/link';

export default function EditArticlePage() {
    const router = useRouter();
    const params = useParams();
    const id = params.id as string;

    const [loading, setLoading] = useState(false);
    const [fetching, setFetching] = useState(true);
    const [formData, setFormData] = useState({
        title: '',
        content: '',
        category: 'Health',
        imageUrl: '',
        date: '',
        readTime: '5 min read',
        isTrending: false,
    });

    useEffect(() => {
        const fetchArticle = async () => {
            try {
                const docRef = doc(db, 'articles', id);
                const docSnap = await getDoc(docRef);

                if (docSnap.exists()) {
                    const data = docSnap.data();
                    setFormData({
                        title: data.title || '',
                        content: data.content || '',
                        category: data.category || 'Health',
                        imageUrl: data.imageUrl || '',
                        date: data.date || '',
                        readTime: data.readTime || '5 min read',
                        isTrending: data.isTrending || false,
                    });
                } else {
                    alert('Artikel tidak ditemukan!');
                    router.push('/articles');
                }
            } catch (error: any) {
                console.error('Error fetching article:', error);
                alert('Gagal memuat data: ' + error.message);
            } finally {
                setFetching(false);
            }
        };

        if (id) {
            fetchArticle();
        }
    }, [id, router]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
        const { name, value, type } = e.target;

        if (type === 'checkbox') {
            setFormData(prev => ({
                ...prev,
                [name]: (e.target as HTMLInputElement).checked
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
            if (!formData.title || !formData.content) {
                alert('Judul dan konten wajib diisi!');
                setLoading(false);
                return;
            }

            const docRef = doc(db, 'articles', id);
            await updateDoc(docRef, formData);

            alert('Artikel berhasil diperbarui!');
            router.push('/articles');
        } catch (error: any) {
            console.error('Error updating article:', error);
            alert('Gagal memperbarui: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    if (fetching) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-cyan-500 mx-auto mb-4"></div>
                    <p className="text-gray-500">Memuat artikel...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 p-8">
            <div className="max-w-3xl mx-auto">
                <div className="mb-8">
                    <Link
                        href="/articles"
                        className="inline-flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4 transition-colors"
                    >
                        <ArrowLeft size={20} />
                        <span>Kembali ke Daftar Artikel</span>
                    </Link>
                    <h1 className="text-3xl font-bold text-gray-900">Edit Artikel</h1>
                    <p className="text-gray-500 mt-2">Perbarui konten artikel</p>
                </div>

                <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow-sm p-8 border border-gray-100">
                    <div className="space-y-6">
                        {/* Title */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Judul Artikel <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="text"
                                name="title"
                                value={formData.title}
                                onChange={handleChange}
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
                                placeholder="Masukkan judul artikel"
                                required
                            />
                        </div>

                        {/* Category & Date */}
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-semibold text-gray-700 mb-2">
                                    Kategori
                                </label>
                                <select
                                    name="category"
                                    value={formData.category}
                                    onChange={handleChange}
                                    className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
                                >
                                    <option value="Newest">Newest</option>
                                    <option value="Health">Health</option>
                                    <option value="Lifestyle">Lifestyle</option>
                                    <option value="Cancer">Cancer</option>
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-semibold text-gray-700 mb-2">
                                    Tanggal Publikasi
                                </label>
                                <input
                                    type="date"
                                    name="date"
                                    value={formData.date}
                                    onChange={handleChange}
                                    className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
                                />
                            </div>
                        </div>

                        {/* Read Time */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Estimasi Waktu Baca
                            </label>
                            <input
                                type="text"
                                name="readTime"
                                value={formData.readTime}
                                onChange={handleChange}
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
                                placeholder="5 min read"
                            />
                        </div>

                        {/* Content */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Konten Artikel <span className="text-red-500">*</span>
                            </label>
                            <textarea
                                name="content"
                                value={formData.content}
                                onChange={handleChange}
                                rows={10}
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
                                placeholder="Tulis konten artikel di sini..."
                                required
                            />
                        </div>

                        {/* Image URL */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                URL Gambar
                            </label>
                            <input
                                type="url"
                                name="imageUrl"
                                value={formData.imageUrl}
                                onChange={handleChange}
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
                                placeholder="https://example.com/image.jpg"
                            />
                            <p className="text-xs text-gray-500 mt-1">Kosongkan jika tidak ada gambar</p>
                        </div>

                        {/* Trending */}
                        <div className="flex items-center gap-3 p-4 bg-orange-50 rounded-lg border border-orange-100">
                            <input
                                type="checkbox"
                                name="isTrending"
                                checked={formData.isTrending}
                                onChange={handleChange}
                                className="w-5 h-5 text-orange-500 border-gray-300 rounded focus:ring-orange-500"
                                id="isTrending"
                            />
                            <label htmlFor="isTrending" className="text-sm font-medium text-gray-700 cursor-pointer">
                                Tandai sebagai artikel trending
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
                            href="/articles"
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
