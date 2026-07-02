/**
 * Logique de gestion des offres d'emploi
 */

let allOffres = [];
let mesCandidaturesIds = [];

document.addEventListener('DOMContentLoaded', async () => {
    // Vérification de sécurité
    if (!SESSION_DATA.token) {
        window.location.href = '/login';
        return;
    }
    await loadInitialData();
});

async function loadInitialData() {
    try {
        const [offresRes, candsRes] = await Promise.all([
            fetch(`${SESSION_DATA.apiBaseUrl}/api/offres`, {
                headers: { 'Authorization': `Bearer ${SESSION_DATA.token}` }
            }),
            SESSION_DATA.userRole === 'CANDIDAT' ?
                fetch(`${SESSION_DATA.apiBaseUrl}/api/candidatures/candidat/${SESSION_DATA.userId}`, {
                    headers: { 'Authorization': `Bearer ${SESSION_DATA.token}` }
                }) :
                Promise.resolve({ ok: true, json: () => [] })
        ]);

        allOffres = await offresRes.json();
        const cands = await candsRes.json();
        mesCandidaturesIds = cands.map(c => c.offreId);

        displayOffres(allOffres);
    } catch (error) {
        console.error("Erreur de chargement:", error);
        const container = document.getElementById('offresList');
        if (container) {
            container.innerHTML = '<p class="text-center text-danger">Erreur de connexion aux microservices.</p>';
        }
    }
}

function displayOffres(offres) {
    const container = document.getElementById('offresList');
    if (!container) return;

    if (offres.length === 0) {
        container.innerHTML = '<div class="text-center p-5 text-muted"><h3>Aucune offre trouvée</h3></div>';
        return;
    }

    container.innerHTML = offres.map(o => {
        const aPostule = mesCandidaturesIds.includes(o.id);
        return `
            <div class="offre-card">
                <div class="offre-header">
                    <div>
                        <h3 class="offre-title">${o.titre}</h3>
                        <p class="offre-company">🏢 ${o.entrepriseNom || 'Entreprise'} • ${o.ville || 'France'}</p>
                    </div>
                    <span class="offre-salaire">${o.salaire || 'A négocier'}</span>
                </div>
                <p class="offre-description">${o.description ? o.description.substring(0, 200) + '...' : ''}</p>
                <div class="offre-skills">
                    ${o.competencesRequises ? o.competencesRequises.split(',').map(s => `<span class="skill-tag">${s.trim()}</span>`).join('') : ''}
                </div>
                <div class="offre-footer">
                    <div class="text-muted small">🎓 Niveau : ${o.niveauRequis || 'N/C'}</div>
                    ${SESSION_DATA.userRole === 'CANDIDAT' ? `
                        <button class="btn-postuler" ${aPostule ? 'disabled' : `onclick="postuler(${o.id}, this)"`}>
                            ${aPostule ? '✅ Déjà postulé' : '📤 Postuler'}
                        </button>
                    ` : ''}
                </div>
            </div>
        `;
    }).join('');
}

function filterOffres() {
    const search = document.getElementById('searchInput').value.toLowerCase();
    const ville = document.getElementById('villeFilter').value.toLowerCase();

    const filtered = allOffres.filter(o =>
        (o.titre.toLowerCase().includes(search) || (o.entrepriseNom && o.entrepriseNom.toLowerCase().includes(search))) &&
        (ville === "" || (o.ville && o.ville.toLowerCase().includes(ville)))
    );
    displayOffres(filtered);
}

async function postuler(offreId, btn) {
    btn.disabled = true;
    const originalText = btn.textContent;
    btn.textContent = "⏳...";

    try {
        const res = await fetch(`${SESSION_DATA.apiBaseUrl}/api/candidatures?candidatId=${SESSION_DATA.userId}&offreId=${offreId}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${SESSION_DATA.token}` }
        });

        if (res.ok) {
            btn.textContent = "✅ Postulé";
            showNotif();
            mesCandidaturesIds.push(offreId);
        } else {
            throw new Error("Erreur lors de l'envoi");
        }
    } catch (e) {
        btn.disabled = false;
        btn.textContent = originalText;
        alert("Impossible d'envoyer la candidature.");
    }
}

function showNotif() {
    const n = document.getElementById('notif');
    if (n) {
        n.style.display = 'block';
        setTimeout(() => n.style.display = 'none', 3000);
    }
}