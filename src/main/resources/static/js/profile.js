(function () {
    'use strict';

    console.log('[Profile] Initializing profile page...');

    function initAccordions() {
        const accordions = document.querySelectorAll('.accordion');

        accordions.forEach(button => {
            button.addEventListener('click', function () {
                const panel = this.nextElementSibling;

                if (panel && panel.classList.contains('panel')) {
                    this.classList.toggle('active');
                    panel.classList.toggle('active');

                    if (panel.style.maxHeight) {
                        panel.style.maxHeight = null;
                    } else {
                        panel.style.maxHeight = panel.scrollHeight + 'px';
                    }
                }
            });
        });

        console.log('[Profile] Accordions initialized:', accordions.length);
    }

    function init() {
        initAccordions();
        console.log('[Profile] Profile page ready');
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();