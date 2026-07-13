document.addEventListener('DOMContentLoaded', function() {

    // --- SCROLL NAVBAR ---
    const navbar = document.getElementById('navbar');
    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });

    // --- BURGER MENU LOGIC ---
    const burger = document.getElementById('burgerMenu');
    const mobileMenu = document.getElementById('mobileMenu');
    const overlay = document.querySelector('.mobile-menu-overlay');

    function toggleMenu() {
        mobileMenu.classList.toggle('open');
        overlay.classList.toggle('open');
        // Bloquer le scroll derrière le menu
        document.body.style.overflow = mobileMenu.classList.contains('open') ? 'hidden' : '';
    }

    if (burger) {
        burger.addEventListener('click', (e) => {
            e.stopPropagation();
            toggleMenu();
        });
    }

    if (overlay) {
        overlay.addEventListener('click', toggleMenu);
    }

    // --- LIENS MOBILE : FERMETURE & REDIRECTION ---
    const mobileLinks = document.querySelectorAll('.mobile-nav-link');
    mobileLinks.forEach(link => {
        link.addEventListener('click', () => {
            // On ferme le menu visuellement
            mobileMenu.classList.remove('open');
            overlay.classList.remove('open');
            document.body.style.overflow = '';
            // Le lien HTML fera la redirection normalement
        });
    });

    // --- ANIMATIONS ---
    const cards = document.querySelectorAll('.feature-card, .step-card');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, { threshold: 0.1 });

    cards.forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = '0.6s ease-out';
        observer.observe(card);
    });
});