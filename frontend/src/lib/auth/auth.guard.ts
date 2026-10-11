import {get} from 'svelte/store';
import {authStore} from './auth.store';

export function requireAuth(): boolean {
    if (!get(authStore).accessToken) {
        window.location.href = '#/login';
        return false;
    }
    return true;
}

// Strane za goste (prijava, registracija): prijavljen korisnik se vraća na početnu,
// da ne bi video formu ispod zaglavlja ili novom registracijom tiho zamenio svoj nalog
export function requireGuest(): boolean {
    if (get(authStore).accessToken) {
        window.location.href = '#/';
        return false;
    }
    return true;
}
