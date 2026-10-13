import type {Component} from 'svelte';
import {wrap} from 'svelte-spa-router/wrap';
import Login from './pages/Login.svelte';
import Register from './pages/Register.svelte';
import Home from './pages/Home.svelte';
import Game from './pages/Game.svelte';
import Leaderboard from './pages/Leaderboard.svelte';
import MyGames from './pages/MyGames.svelte';
import Settings from './pages/Settings.svelte';
import NotFound from './pages/NotFound.svelte';
import {requireAuth, requireGuest} from './lib/auth/auth.guard';
import {requireNoGameInProgress} from './lib/stores/game.guard';

// Redosled uslova je bitan: ruter staje na prvom koji ne prođe, pa neprijavljen korisnik ide na prijavu
const guarded = (component: Component) => wrap({component, conditions: [requireAuth, requireNoGameInProgress]});
const guestOnly = (component: Component) => wrap({component, conditions: [requireGuest]});

export const routes = {
    '/login': guestOnly(Login),
    '/register': guestOnly(Register),
    '/': guarded(Home),
    // Jedina ruta dostupna dok partija traje
    '/game': wrap({component: Game, conditions: [requireAuth]}),
    '/leaderboard': guarded(Leaderboard),
    '/my-games': guarded(MyGames),
    '/settings': guarded(Settings),

    // Mora biti poslednja: sve ostale putanje
    '*': guarded(NotFound),
};
