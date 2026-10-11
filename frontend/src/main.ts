import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.min.css';
// Posle Bootstrap-a, da bi naši stilovi pregazili njegove
import './app.css';
import {mount} from 'svelte';
import App from './App.svelte';

const app = mount(App, {
    target: document.getElementById('app')!,
});

export default app;