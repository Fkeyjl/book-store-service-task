(function () {
    'use strict';

    console.log('[Header] Initializing header with side menu...');

    function initSideMenu() {
        const menuToggle = document.querySelector('[data-menu-toggle]');
        const sideMenu = document.getElementById('sideMenu');
        const sideMenuOverlay = document.getElementById('sideMenuOverlay');
        const sideMenuClose = document.getElementById('sideMenuClose');

        if (!menuToggle || !sideMenu || !sideMenuOverlay || !sideMenuClose) {
            console.warn('[Header] Side menu elements not found');
            return;
        }

        function openSideMenu() {
            sideMenu.classList.add('active');
            sideMenuOverlay.classList.add('active');
            menuToggle.setAttribute('aria-expanded', 'true');
            document.body.style.overflow = 'hidden';
            console.log('[Header] Side menu opened');
        }

        function closeSideMenu() {
            sideMenu.classList.remove('active');
            sideMenuOverlay.classList.remove('active');
            menuToggle.setAttribute('aria-expanded', 'false');
            document.body.style.overflow = '';
            console.log('[Header] Side menu closed');
        }

        function toggleSideMenu() {
            if (sideMenu.classList.contains('active')) {
                closeSideMenu();
            } else {
                openSideMenu();
            }
        }

        menuToggle.addEventListener('click', toggleSideMenu);
        sideMenuClose.addEventListener('click', closeSideMenu);
        sideMenuOverlay.addEventListener('click', closeSideMenu);

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && sideMenu.classList.contains('active')) {
                closeSideMenu();
            }
        });

        const sideMenuLinks = sideMenu.querySelectorAll('.side-menu-link');
        sideMenuLinks.forEach(link => {
            link.addEventListener('click', function () {
                setTimeout(closeSideMenu, 100);
            });
        });

        console.log('[Header] ✅ Side menu initialized');
    }

    function initThemeSwitcher() {
        const themeToggle = document.getElementById('themeToggle');
        const themeIcon = document.getElementById('themeIcon');

        if (!themeToggle || !themeIcon) {
            console.warn('[Header] Theme toggle elements not found');
            return;
        }

        const savedTheme = localStorage.getItem('theme') || 'light';
        document.documentElement.setAttribute('data-theme', savedTheme);
        updateThemeIcon(savedTheme, themeIcon);

        themeToggle.addEventListener('click', function () {
            const currentTheme = document.documentElement.getAttribute('data-theme');
            const newTheme = currentTheme === 'light' ? 'dark' : 'light';

            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
            updateThemeIcon(newTheme, themeIcon);

            console.log('[Header] Theme changed to:', newTheme);
        });

        console.log('[Header] ✅ Theme switcher initialized');
    }

    function updateThemeIcon(theme, icon) {
        if (theme === 'dark') {
            icon.classList.remove('fa-moon');
            icon.classList.add('fa-sun');
        } else {
            icon.classList.remove('fa-sun');
            icon.classList.add('fa-moon');
        }
    }

    function init() {
        initSideMenu();
        initThemeSwitcher();
        console.log('[Header] ✅ Header module loaded');
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();

