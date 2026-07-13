// ============================================
// BURGER MENU TOGGLE
// Toggles sidebar open/close on burger button click
// ============================================
const burger = document.getElementById('burgerBtn');
const sidebar = document.getElementById('sidebar');
if (burger && sidebar) {
    burger.addEventListener('click', () => {
        sidebar.classList.toggle('open');
    });

    // Closes sidebar when clicking outside (only on mobile devices)
    document.addEventListener('click', (e) => {
        if (window.innerWidth < 768 && !sidebar.contains(e.target) && !burger.contains(e.target)) {
            sidebar.classList.remove('open');
        }
    });
}

// ============================================
// CURRENT DATE & TIME DISPLAY
// Displays formatted current date/time in French locale
// ============================================
const dateTimeElement = document.getElementById('currentDateTime');
if (dateTimeElement) {
    dateTimeElement.textContent = new Date().toLocaleString('fr-FR', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
    });
}