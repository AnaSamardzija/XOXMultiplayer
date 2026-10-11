import * as authApi from '../api/auth.api';
import {authStore} from './auth.store';
import {clearAuth, saveAuth} from '../stores/auth.storage';
import type {RegisterRequest} from '../types/auth.types';

export async function login(username: string, password: string) {
    const data = await authApi.login({username, password});

    authStore.set(data);
    saveAuth(data);
}

export async function register(request: RegisterRequest) {
    const data = await authApi.register(request);

    authStore.set(data);
    saveAuth(data);
}

export function logout() {
    authStore.set({accessToken: null, user: null});
    clearAuth();
}
