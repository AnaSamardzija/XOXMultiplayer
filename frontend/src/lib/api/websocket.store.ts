import {writable} from 'svelte/store';

export type ConnectionState = 'disconnected' | 'connecting' | 'connected';

export const connectionState = writable<ConnectionState>('disconnected');
