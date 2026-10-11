import type {Component} from 'svelte';
import {wrap} from 'svelte-spa-router/wrap';
import Login from './pages/Login.svelte';
import Register from './pages/Register.svelte';
import Home from './pages/Home.svelte';
import Game from './pages/Game.svelte';
import Leaderboard from './pages/Leaderboard.svelte';
import Profile from './pages/Profile.svelte';
import NotFound from './pages/NotFound.svelte';
import {requireAuth, requireGuest} from './lib/auth/auth.guard';

const guarded = (component: Component) => wrap({component, conditions: [requireAuth]});
const guestOnly = (component: Component) => wrap({component, conditions: [requireGuest]});

export const routes = {
    '/login': guestOnly(Login),
    '/register': guestOnly(Register),
    '/': guarded(Home),
    '/game': guarded(Game),
    '/leaderboard': guarded(Leaderboard),
    '/profile': guarded(Profile),

    // Mora biti poslednja: sve ostale putanje
    '*': guarded(NotFound),
};
