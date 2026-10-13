import {writable} from 'svelte/store';

// Korisničko ime igrača koji trenutno čeka protivnika, null kad niko ne čeka. Upisuje ga samo
// App.svelte, iz poruka sa teme /topic/lobby i iz odgovora sa REST-a posle povezivanja.
export const waitingUsernameStore = writable<string | null>(null);

// Igrač je kliknuo „Igraj“ u obaveštenju: početna strana ga uvodi u red čim tamo bude spremna
// pretplata na lični kanal, pa vraća ovo na false
export const pendingJoinStore = writable(false);
