<script lang="ts">
    import {authStore} from '../lib/auth/auth.store';
</script>

<div class="card">
    <div class="card-body p-4">
        <h4 class="mb-1">Zdravo, {$authStore.user?.username}</h4>
        <p class="text-muted mb-0">Prijavljeni ste.</p>
    </div>
</div>
