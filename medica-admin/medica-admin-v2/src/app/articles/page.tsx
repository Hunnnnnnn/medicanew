'use client';

import { useState, useEffect } from 'react';
import { collection, onSnapshot, deleteDoc, doc } from 'firebase/firestore';
import { db } from '@/lib/firebase';
import { Article } from '@/types';
import Link from 'next/link';
import { Plus, Edit, Trash2, Search, TrendingUp, Clock } from 'lucide-react';

export default function ArticlesPage() {
    const [articles, setArticles] = useState<Article[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [categoryFilter, setCategoryFilter] = useState<string>('all');

    useEffect(() => {
        const articlesRef = collection(db, 'articles');

        const unsubscribe = onSnapshot(articlesRef, (snapshot) => {
            const docs = snapshot.docs.map((doc) => ({
                id: doc.id,
                ...doc.data(),
            })) as Article[];

            // Sort by date (newest first)
            docs.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

            setArticles(docs);
            setLoading(false);
        }, (error) => {
            console.error('Error fetching articles:', error);
            setLoading(false);
        });

        return () => unsubscribe();
    }, []);

    const handleDelete = async (id: string) => {
        if (confirm('Hapus artikel ini?')) {
            try {
                await deleteDoc(doc(db, 'articles', id));
                alert('Artikel berhasil dihapus!');
            } catch (error: any) {
                console.error('Error deleting article:', error);
                alert('Gagal menghapus: ' + error.message);
            }
        }
    };

    const filteredArticles = articles.filter(article => {
        const matchesSearch =
            article.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
            article.content.toLowerCase().includes(searchTerm.toLowerCase());

        const matchesCategory = categoryFilter === 'all' || article.category === categoryFilter;

        return matchesSearch && matchesCategory;
    });

    const stats = {
        total: articles.length,
        trending: articles.filter(a => a.isTrending).length,
        health: articles.filter(a => a.category === 'Health').length,
        lifestyle: articles.filter(a => a.category === 'Lifestyle').length,
    };

    return (
        <div className="min-h-screen bg-gray-50 p-8">
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Manajemen Artikel</h1>
                    <p className="text-gray-500 mt-1">Kelola artikel kesehatan</p>
                </div>
                <Link
                    href="/articles/add"
                    className="bg-cyan-500 hover:bg-cyan-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 transition-colors shadow-sm"
                >
                    <Plus size={20} />
                    <span>Tambah Artikel</span>
                </Link>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-4 gap-4 mb-6">
                <div className="bg-white p-5 rounded-lg shadow-sm border border-gray-100">
                    <p className="text-sm text-gray-600">Total Artikel</p>
                    <p className="text-3xl font-bold text-gray-900 mt-1">{stats.total}</p>
                </div>
                <div className="bg-white p-5 rounded-lg shadow-sm border border-gray-100">
                    <p className="text-sm text-gray-600">Trending</p>
                    <p className="text-3xl font-bold text-orange-600 mt-1">{stats.trending}</p>
                </div>
                <div className="bg-white p-5 rounded-lg shadow-sm border border-gray-100">
                    <p className="text-sm text-gray-600">Health</p>
                    <p className="text-3xl font-bold text-green-600 mt-1">{stats.health}</p>
                </div>
                <div className="bg-white p-5 rounded-lg shadow-sm border border-gray-100">
                    <p className="text-sm text-gray-600">Lifestyle</p>
                    <p className="text-3xl font-bold text-purple-600 mt-1">{stats.lifestyle}</p>
                </div>
            </div>

            {/* Filters */}
            <div className="bg-white p-4 rounded-lg shadow-sm mb-6 flex items-center gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                    <input
                        type="text"
                        placeholder="Cari judul atau konten artikel..."
                        className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <select
                    className="px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
                    value={categoryFilter}
                    onChange={(e) => setCategoryFilter(e.target.value)}
                >
                    <option value="all">Semua Kategori</option>
                    <option value="Newest">Newest</option>
                    <option value="Health">Health</option>
                    <option value="Lifestyle">Lifestyle</option>
                    <option value="Cancer">Cancer</option>
                </select>
            </div>

            {/* Articles Grid */}
            {loading ? (
                <div className="flex justify-center py-20">
                    <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-cyan-500"></div>
                </div>
            ) : filteredArticles.length === 0 ? (
                <div className="bg-white rounded-lg p-12 text-center shadow-sm">
                    <Search className="mx-auto text-gray-400 mb-4" size={48} />
                    <h3 className="text-lg font-medium text-gray-900">Tidak ada artikel</h3>
                    <p className="text-gray-500 mt-2">Tambahkan artikel baru atau ubah filter pencarian</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {filteredArticles.map((article) => (
                        <div key={article.id} className="bg-white rounded-xl shadow-sm overflow-hidden border border-gray-100 hover:shadow-md transition-shadow">
                            {/* Image */}
                            <div className="relative h-48 bg-gray-200">
                                {article.imageUrl ? (
                                    <img
                                        src={article.imageUrl}
                                        alt={article.title}
                                        className="w-full h-full object-cover"
                                        onError={(e) => { (e.target as HTMLImageElement).src = 'https://via.placeholder.com/400x300?text=No+Image' }}
                                    />
                                ) : (
                                    <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-cyan-100 to-cyan-200">
                                        <span className="text-cyan-600 font-bold text-xl">No Image</span>
                                    </div>
                                )}
                                {article.isTrending && (
                                    <div className="absolute top-3 right-3 bg-orange-500 text-white px-3 py-1 rounded-full text-xs font-semibold flex items-center gap-1">
                                        <TrendingUp size={14} />
                                        Trending
                                    </div>
                                )}
                            </div>

                            {/* Content */}
                            <div className="p-5">
                                <div className="flex items-center gap-2 mb-3">
                                    <span className="px-2.5 py-0.5 bg-cyan-100 text-cyan-700 rounded-full text-xs font-medium">
                                        {article.category}
                                    </span>
                                    <div className="flex items-center gap-1 text-gray-500 text-xs">
                                        <Clock size={14} />
                                        <span>{article.readTime}</span>
                                    </div>
                                </div>

                                <h3 className="font-bold text-gray-900 text-lg mb-2 line-clamp-2">
                                    {article.title}
                                </h3>
                                <p className="text-gray-600 text-sm line-clamp-3 mb-4">
                                    {article.content}
                                </p>

                                <div className="flex items-center justify-between pt-4 border-t border-gray-100">
                                    <span className="text-xs text-gray-500">{article.date}</span>
                                    <div className="flex gap-2">
                                        <Link
                                            href={`/articles/edit/${article.id}`}
                                            className="p-2 text-gray-400 hover:text-cyan-600 hover:bg-cyan-50 rounded-lg transition-all"
                                            title="Edit"
                                        >
                                            <Edit size={16} />
                                        </Link>
                                        <button
                                            onClick={() => handleDelete(article.id)}
                                            className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all"
                                            title="Hapus"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
