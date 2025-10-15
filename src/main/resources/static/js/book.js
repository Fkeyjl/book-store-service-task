(function () {
    'use strict';

    console.log('[Book] Initializing book page...');

    function initFormValidation() {
        const forms = document.querySelectorAll('.book-form');

        forms.forEach(form => {
            form.addEventListener('submit', function (event) {
                const isValid = validateForm(form);

                if (!isValid) {
                    event.preventDefault();
                    console.warn('[Book] Form validation failed');
                }
            });
        });

        console.log('[Book] Form validation initialized');
    }

    function validateForm(form) {
        return form.checkValidity();
    }

    function initCheckboxGroups() {
        const checkboxGroups = document.querySelectorAll('.checkbox-group');

        checkboxGroups.forEach(group => {
            const checkboxes = group.querySelectorAll('input[type="checkbox"]');

            console.log('[Book] Checkbox group found with', checkboxes.length, 'items');
        });
    }

    function init() {
        initFormValidation();
        initCheckboxGroups();
        console.log('[Book] Book page ready');
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();