
    // Menu burger pour mobile
    const burger = document.getElementById('burgerBtn');
    const sidebar = document.getElementById('sidebar');
    if (burger && sidebar) {
    burger.addEventListener('click', () => {
        sidebar.classList.toggle('open');
    });
    document.addEventListener('click', (e) => {
    if (window.innerWidth < 768 && !sidebar.contains(e.target) && !burger.contains(e.target)) {
    sidebar.classList.remove('open');
}
});
}

    const dateTimeElement = document.getElementById('currentDateTime');
    if (dateTimeElement) {
    dateTimeElement.textContent = new Date().toLocaleString('fr-FR', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
    });
}
