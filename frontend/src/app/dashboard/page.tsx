'use client';

import { useEffect } from 'react';
import { useQuery } from '@apollo/client';
import { useRouter } from 'next/navigation';
import { GET_ME } from '@/graphql/queries';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { setAuth } from '@/store/slices/authSlice';

export default function Dashboard() {
  const router = useRouter();
  const dispatch = useAppDispatch();
  const auth = useAppSelector((state) => state.auth);
  const { data, loading, error } = useQuery(GET_ME);

  useEffect(() => {
    if (!loading) {
      if (error || !data?.me) {
        router.replace('/login');
        return;
      }

      if (!auth.isAuthenticated) {
        const accessToken = localStorage.getItem('accessToken');
        const refreshToken = localStorage.getItem('refreshToken');

        if (accessToken && refreshToken) {
          dispatch(
            setAuth({
              accessToken,
              refreshToken,
              user: data.me,
            }),
          );
        }
      }
    }
  }, [loading, error, data, auth.isAuthenticated, dispatch, router]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <p className="text-gray-700">Loading dashboard...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        </div>
      </header>
      <main>
        <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
          <div className="px-4 py-6 sm:px-0">
            <div className="border-4 border-dashed border-gray-200 rounded-lg h-96 flex items-center justify-center">
              <p className="text-gray-500">
                Welcome back, {data?.me?.name}! Your meal planning dashboard will go here.
              </p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
