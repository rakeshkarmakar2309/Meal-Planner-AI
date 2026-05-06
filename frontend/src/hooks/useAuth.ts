'use client';

import { useMutation } from '@apollo/client/react';
import { useRouter } from 'next/navigation';
import { LOGIN, REGISTER } from '@/graphql/mutations';
import { useAppDispatch } from '@/store/hooks';
import { setAuth } from '@/store/slices/authSlice';
import type { LoginInput, RegisterInput } from '@/types';

interface AuthResult {
  accessToken: string;
  refreshToken: string;
  user: {
    id: string;
    email: string;
    name: string;
  };
}

interface LoginData {
  login: AuthResult;
}

interface RegisterData {
  register: AuthResult;
}

export function useAuth() {
  const dispatch = useAppDispatch();
  const router = useRouter();

  const [loginMutation, loginState] = useMutation<LoginData, { input: LoginInput }>(LOGIN);
  const [registerMutation, registerState] = useMutation<RegisterData, { input: RegisterInput }>(REGISTER);

  const saveAuth = (payload: AuthResult) => {
    localStorage.setItem('accessToken', payload.accessToken);
    localStorage.setItem('refreshToken', payload.refreshToken);
    localStorage.setItem('user', JSON.stringify(payload.user));
  };

  const applyAuth = (payload: AuthResult) => {
    saveAuth(payload);
    dispatch(
      setAuth({
        accessToken: payload.accessToken,
        refreshToken: payload.refreshToken,
        user: payload.user,
      }),
    );
    router.push('/dashboard');
  };

  const login = async (input: LoginInput) => {
    const result = await loginMutation({ variables: { input } });
    if (result.data?.login) {
      applyAuth(result.data.login);
    }
  };

  const register = async (input: RegisterInput) => {
    const result = await registerMutation({ variables: { input } });
    if (result.data?.register) {
      applyAuth(result.data.register);
    }
  };

  return {
    login,
    register,
    loading: loginState.loading || registerState.loading,
    error: loginState.error || registerState.error,
  };
}
