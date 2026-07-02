/**
 * SkillMatch - Scripts principaux
 * Gestion de la navbar, du menu burger et des animations
 */

// ============================================
// NAVBAR - EFFET DE SCROLL
// ============================================
document.addEventListener('DOMContentLoaded', function() {
    const navbar = document.getElementById('navbar');

    if (navbar) {
        if (window.scrollY > 50) {
            navbar.classList.add('scrolled');
        }

        window.addEventListener('scroll', function() {
            if (window.scrollY > 50) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
        });
    }
});

// ============================================
// MENU BURGER - MOBILE (CORRIGÉ)
// ============================================
(function() {
    const burger = document.getElementById('burgerMenu');
    const mobileMenu = document.getElementById('mobileMenu');

    // Créer ou récupérer l'overlay
    let overlay = document.querySelector('.mobile-menu-overlay');
    if (!overlay && burger) {
        overlay = document.createElement('div');
        overlay.className = 'mobile-menu-overlay';
        document.body.appendChild(overlay);
    }

    // Fonction pour fermer le menu proprement
    function closeMenu() {
        if (!burger || !mobileMenu || !overlay) return;
        burger.classList.remove('open');
        mobileMenu.classList.remove('open');
        overlay.classList.remove('open');
        document.body.style.overflow = ''; // Libère le scroll
    }

    // Fonction pour ouvrir le menu
    function openMenu() {
        burger.classList.add('open');
        mobileMenu.classList.add('open');
        overlay.classList.add('open');
        document.body.style.overflow = 'hidden'; // Bloque le scroll
    }

    // Toggle au clic sur le burger
    if (burger) {
        burger.addEventListener('click', function(e) {
            e.preventDefault();
            if (mobileMenu.classList.contains('open')) {
                closeMenu();
            } else {
                openMenu();
            }
        });
    }

    // Fermer au clic sur l'overlay
    if (overlay) {
        overlay.addEventListener('click', closeMenu);
    }

    // RÉPARATION DES LIENS : Fermer le menu quand on clique sur un lien
    const mobileLinks = document.querySelectorAll('.mobile-nav-link');
    mobileLinks.forEach(function(link) {
        link.addEventListener('click', function() {
            // On ferme tout immédiatement pour libérer le scroll et le z-index
            closeMenu();
        });
    });

})();

// ============================================
// ANIMATIONS AU SCROLL
// ============================================
document.addEventListener('DOMContentLoaded', function() {
    const cards = document.querySelectorAll('.feature-card, .step-card');

    if ('IntersectionObserver' in window) {
        const observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }
            });
        }, {
            threshold: 0.1
        });

        cards.forEach(function(card) {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            observer.observe(card);
        });
    }
});

// ============================================
// CONFIRMATION DE SUPPRESSION
// ============================================
document.addEventListener('DOMContentLoaded', function() {
    const deleteForms = document.querySelectorAll('form[onsubmit*="confirm"]');
    deleteForms.forEach(function(form) {
        const message = form.getAttribute('onsubmit');
        if (message) {
            form.removeAttribute('onsubmit');
            form.addEventListener('submit', function(e) {
                if (!confirm('Supprimer définitivement cette offre ?')) {
                    e.preventDefault();
                }
            });
        }
    });
});