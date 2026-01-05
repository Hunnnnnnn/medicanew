export interface DashboardStats {
    totalToday: number;
    inQueue: number;
    beingServed: number;
    completed: number;
}

export interface PoliStats {
    name: string;
    count: number;
}

export interface HourlyStats {
    hour: string;
    count: number;
}

export interface Doctor {
    id: string;
    name: string;
    specialty: string;
    hospital: string;
    rating: number;
    patientsCount: number;
    yearsExperience: number;
    imageUrl: string;
    isAvailable: boolean;
    workingTime: string; // Using string for flexibility (e.g., "08:00 - 17:00")
}

export interface Article {
    id: string;
    title: string;
    content: string;
    category: string; // "Newest", "Health", "Lifestyle", "Cancer"
    imageUrl: string;
    date: string;
    readTime: string; // e.g., "5 min read"
    isTrending: boolean;
}

export interface Appointment {
    id: string;
    date: string;
    time: string;
    specialty: string;
    doctorName: string;
    status: string;
    userId?: string;
    location?: string;
}
