import type {UserDto} from './user.types';

export interface AuthRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    password: string;
}

// Odgovor i na prijavu i na registraciju: token i korisnik zajedno, pa nema posebnog poziva za korisnika
export interface AuthResponse {
    accessToken: string;
    user: UserDto;
}

export type AuthState = {
    accessToken: string | null;
    user: UserDto | null;
}
