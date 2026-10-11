import {apiFetch, errorMessage} from './client';
import type {AuthRequest, AuthResponse, RegisterRequest} from '../types/auth.types';

const BASE = '/api/v1/auth';

export async function login(data: AuthRequest): Promise<AuthResponse> {
    const res = await apiFetch(`${BASE}/login`, {
        method: 'POST',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Prijava nije uspela'));
    return res.json();
}

// Backend posle registracije vraća token i korisnika kao i prijava (201), pa je korisnik odmah prijavljen
export async function register(data: RegisterRequest): Promise<AuthResponse> {
    const res = await apiFetch(`${BASE}/register`, {
        method: 'POST',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Registracija nije uspela'));
    return res.json();
}
