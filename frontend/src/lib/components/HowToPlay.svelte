<!-- Samo tekst za igrača: front i dalje ne zna pravila, o svakom potezu odlučuje server -->
<div class="card">
    <div class="card-header">
        <i class="bi bi-question-circle me-2 text-primary"></i>Kako se igra
    </div>
    <div class="card-body">
        <!-- Na velikom ekranu su pravila u dve kolone, a na telefonu jedna ispod druge -->
        <div class="row g-3">
            <div class="col-12 col-lg-6">
                <h6><i class="bi bi-grid-3x3 me-2 text-primary"></i>Tabla</h6>
                <p class="mb-0">
                    Velika tabla se sastoji od devet mini-tabli, a svaka mini-tabla ima devet polja. Igraju dva
                    igrača, X i O, a ko je koji određuje se nasumično. X igra prvi i prvi potez sme da odigra
                    u bilo koje polje.
                </p>
            </div>
            <div class="col-12 col-lg-6">
                <h6><i class="bi bi-arrow-right-circle me-2 text-primary"></i>Gde se igra</h6>
                <p class="mb-0">
                    Polje u koje odigrate određuje mini-tablu u kojoj protivnik igra sledeći potez: ako odigrate
                    u gornje desno polje, protivnik igra u gornjoj desnoj mini-tabli. Mini-tabla u kojoj smete
                    da igrate je istaknuta žutim okvirom.
                </p>
            </div>
            <div class="col-12 col-lg-6">
                <h6><i class="bi bi-unlock me-2 text-primary"></i>Slobodan potez</h6>
                <p class="mb-0">
                    Mini-tablu osvaja ko u njoj sklopi tri svoja znaka u nizu. Osvojena ili popunjena mini-tabla
                    je zatvorena i u nju se više ne igra. Ako ste poslati u zatvorenu mini-tablu, imate slobodan
                    potez i birate bilo koju otvorenu mini-tablu.
                </p>
            </div>
            <div class="col-12 col-lg-6">
                <h6><i class="bi bi-trophy me-2 text-primary"></i>Pobeda</h6>
                <p class="mb-0">
                    Pobeđuje ko prvi osvoji tri mini-table u nizu: u redu, koloni ili dijagonali. Mini-tabla
                    popunjena bez pobednika ne pripada nikome. Ako se sve mini-table zatvore, a niko nema tri
                    u nizu, partija je nerešena.
                </p>
            </div>
            <div class="col-12">
                <h6><i class="bi bi-stopwatch me-2 text-primary"></i>Vreme</h6>
                <p class="mb-0">
                    Za svaki potez imate 20 sekundi. Kad vreme istekne, server umesto vas igra nasumičan potez
                    i partija se nastavlja.
                </p>
            </div>
        </div>
    </div>
</div>
