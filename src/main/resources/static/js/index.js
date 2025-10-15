function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

document.addEventListener('DOMContentLoaded', () => {
    initSlider();
});

function initSlider() {
    const sliderTrack = document.getElementById('book-slider-track');
    const sliderSection = document.querySelector('.book-slider-section');

    if (!sliderTrack || !sliderSection) {
        console.warn('Slider elements not found. Skipping slider init.');
        return;
    }

    const state = {offset: 0};

    const getGapPx = () => {
        const styles = getComputedStyle(sliderTrack);
        const gapStr = styles.gap || styles.columnGap || styles.rowGap || '0px';
        const n = parseFloat(gapStr);
        return Number.isFinite(n) ? n : 0;
    };

    const getCardWidth = () => {
        const firstSlide = sliderTrack.querySelector('.book-slide');
        if (!firstSlide) return 0;
        return firstSlide.getBoundingClientRect().width;
    };

    const getItemsPerView = () => {
        const container = sliderTrack.parentElement;
        if (!container) return 1;
        const available = container.getBoundingClientRect().width;
        const card = getCardWidth();
        const gap = getGapPx();
        if (card <= 0) return 1;
        return Math.max(1, Math.floor((available + gap) / (card + gap)));
    };

    const getMaxOffset = () => {
        const total = sliderTrack.children.length;
        const perView = getItemsPerView();
        return Math.max(0, total - perView);
    };

    const updateTransform = () => {
        const cardWidth = getCardWidth();
        const gap = getGapPx();
        const translateX = -state.offset * (cardWidth + gap);
        sliderTrack.style.transform = `translateX(${translateX}px)`;
    };

    const scroll = (direction) => {
        const maxOffset = getMaxOffset();
        if (maxOffset === 0) {
            state.offset = 0;
            updateTransform();
            return;
        }

        if (direction === 1) {
            state.offset = state.offset < maxOffset ? state.offset + 1 : 0;
        } else if (direction === -1) {
            state.offset = state.offset > 0 ? state.offset - 1 : maxOffset;
        }
        updateTransform();
    };

    sliderSection.addEventListener('click', (event) => {
        const button = event.target.closest('.slider-arrow');
        if (button) {
            const direction = parseInt(button.dataset.direction, 10);
            if (!isNaN(direction)) {
                scroll(direction);
            }
        }
    });

    sliderSection.addEventListener('keydown', (event) => {
        if (event.key === 'ArrowLeft') {
            event.preventDefault();
            scroll(-1);
        } else if (event.key === 'ArrowRight') {
            event.preventDefault();
            scroll(1);
        }
    });

    if (!sliderSection.hasAttribute('tabindex')) {
        sliderSection.setAttribute('tabindex', '0');
    }

    updateTransform();

    const handleResize = debounce(() => {
        const maxOffset = getMaxOffset();
        if (state.offset > maxOffset) {
            state.offset = maxOffset;
        }
        updateTransform();
    }, 200);

    window.addEventListener('resize', handleResize);

    window.scrollSlider = scroll;
}