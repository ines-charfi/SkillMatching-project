

    function filterOffres() {
    const query = document.getElementById('searchInput').value.toLowerCase();
    const cards = document.querySelectorAll('.offre-card');

    cards.forEach(card => {
    const content = card.getAttribute('data-search').toLowerCase();
    // Animation de transition fluide
    if (content.includes(query)) {
    card.style.display = 'block';
    card.classList.add('fade-in');
} else {
    card.style.display = 'none';
}
});
}
