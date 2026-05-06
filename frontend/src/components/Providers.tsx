'use client';

import { useEffect } from 'react';
import { ApolloProvider } from '@apollo/client/react';
import { Provider, useDispatch } from 'react-redux';
import { client } from '../graphql/client';
import { store } from '../store/store';
import { setAuth } from '../store/slices/authSlice';

function AuthHydrator({ children }: { children: React.ReactNode }) {
  const dispatch = useDispatch();

  useEffect(() => {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');
    const userJson = localStorage.getItem('user');

    if (accessToken && refreshToken && userJson) {
      try {
        const user = JSON.parse(userJson);
        dispatch(setAuth({ accessToken, refreshToken, user }));
      } catch {
        localStorage.removeItem('user');
      }
    }
  }, [dispatch]);

  return <>{children}</>;
}

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <Provider store={store}>
      <ApolloProvider client={client}>
        <AuthHydrator>{children}</AuthHydrator>
      </ApolloProvider>
    </Provider>
  );
}
