import type {Component} from 'svelte';
import {wrap} from 'svelte-spa-router/wrap';
import Login from './pages/Login.svelte';
import Register from './pages/Register.svelte';
import Home from './pages/Home.svelte';
import NotFound from './pages/NotFound.svelte';
import {requireAuth, requireGuest} from './lib/auth/auth.guard';

const guarded = (component: Component) => wrap({component, conditions: [requireAuth]});
const guestOnly = (component: Component) => wrap({component, conditions: [requireGuest]});

export const routes = {
    '/login': guestOnly(Login),
    '/register': guestOnly(Register),
    '/': guarded(Home),

    // Mora biti poslednja: sve ostale putanje
    '*': guarded(NotFound),
};
