import {Client} from '@stomp/stompjs';
import {get} from 'svelte/store';
import {push} from 'svelte-spa-router';
import {authStore} from '../auth/auth.store';
import {logout} from '../auth/auth.service';
import {connectionState} from './websocket.store';

// Adresa se sklapa iz adrese strane: na HTTPS-u pregledač odbija ws://, pa tamo mora wss://.
// Lokalno veza ide na Vite, koji je preko proxy-ja prosleđuje backendu.
const client = new Client({
    brokerURL: `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws`,

    // Kad veza pukne, biblioteka sama pokušava ponovo na svakih pet sekundi
    reconnectDelay: 5000,

    // Pregledač ne može da pošalje Authorization zaglavlje pri otvaranju WebSocket veze, pa token ide
    // u STOMP CONNECT poruci. Čita se pred svaki pokušaj, da ponovno povezivanje ne ide sa starim tokenom.
    beforeConnect: () => {
        client.connectHeaders = {Authorization: `Bearer ${get(authStore).accessToken}`};
    },

    onConnect: () => connectionState.set('connected'),

    onWebSocketClose: () => connectionState.set(client.active ? 'connecting' : 'disconnected'),

    // Server je odbio CONNECT jer token nedostaje ili nije ispravan. Isto kao apiFetch na 401:
    // korisnik se odjavljuje i vraća na prijavu, inače bi se veza beskonačno otvarala sa lošim tokenom.
    onStompError: () => {
        disconnect();
        logout();
        push('/login');
    }
});

export function connect() {
    if (client.active) return;

    connectionState.set('connecting');
    client.activate();
}

export function disconnect() {
    client.deactivate();
    connectionState.set('disconnected');
}

// Pretplata ne preživljava prekid veze, pa se poziva iz $effect-a koji zavisi od connectionState:
// posle ponovnog povezivanja efekat se izvrši opet i pretplata se obnovi.
// Vraća funkciju za odjavu sa destinacije; kad je veza već pukla, nema šta da se odjavi.
export function subscribe<T>(destination: string, callback: (body: T) => void): () => void {
    const subscription = client.subscribe(destination, (message) => callback(JSON.parse(message.body)));

    return () => {
        if (client.connected) subscription.unsubscribe();
    };
}

// Poruke bez sadržaja, kao ulazak u red čekanja, šalju se bez tela
export function send(destination: string, body?: unknown) {
    client.publish({destination, body: body === undefined ? undefined : JSON.stringify(body)});
}
