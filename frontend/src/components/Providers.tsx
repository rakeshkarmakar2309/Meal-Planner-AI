'use client';

import { ApolloProvider } from '@apollo/client/react';
import { Provider } from 'react-redux';
import { client } from '../graphql/client';
import { store } from '../store/store';

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <Provider store={store}>
      <ApolloProvider client={client}>
        {children}
      </ApolloProvider>
    </Provider>
  );
}