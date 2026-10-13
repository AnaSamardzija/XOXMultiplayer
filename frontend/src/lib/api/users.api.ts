import {apiFetch, errorMessage} from './client';
import type {ChangePasswordRequestDto} from '../types/user.types';

const BASE = '/api/v1/users';

// Backend vraća 204 bez tela; pogrešna trenutna lozinka je 422, pa korisnik ostaje prijavljen
export async function changePassword(data: ChangePasswordRequestDto): Promise<void> {
    const res = await apiFetch(`${BASE}/me/password`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Promena lozinke nije uspela'));
}
