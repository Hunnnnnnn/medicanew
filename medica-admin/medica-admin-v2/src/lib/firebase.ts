import { initializeApp, getApps } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';

const firebaseConfig = {
    apiKey: "AIzaSyCvP8W5gwMuJrBvcWEqe9c5mLpAWY8QZf0",
    authDomain: "rs-gamebling.firebaseapp.com",
    projectId: "rs-gamebling",
    storageBucket: "rs-gamebling.firebasestorage.app",
    messagingSenderId: "291840618836",
    appId: "1:291840618836:android:4effd4df9020b015bce461"
};

// Initialize Firebase
const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApps()[0];
export const db = getFirestore(app);
export const storage = getStorage(app);

export default app;
