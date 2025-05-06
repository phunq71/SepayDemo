// Load Vue.js from CDN
document.write('<script src="https://cdn.jsdelivr.net/npm/vue@3.2.47/dist/vue.global.min.js"><\/script>');

document.addEventListener('DOMContentLoaded', function() {
    // Wait for Vue to be loaded
    const checkVueLoaded = setInterval(function() {
        if (typeof Vue !== 'undefined') {
            clearInterval(checkVueLoaded);
            initVueApp();
        }
    }, 100);
});

function initVueApp() {
    const { createApp, ref } = Vue;
    
    createApp({
        setup() {
            const amount = ref(0);
            const description = ref('');
            const paymentUrl = ref('');
            const isLoading = ref(false);
            const error = ref(null);
            
            const createPayment = async () => {
                isLoading.value = true;
                error.value = null;
                
                try {
                    const response = await fetch('/api/payment/create', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            amount: amount.value,
                            description: description.value,
                            returnUrl: window.location.origin + '/payment/success',
                            cancelUrl: window.location.origin + '/payment/cancel'
                        })
                    });
                    
                    const data = await response.json();
                    
                    if (response.ok) {
                        paymentUrl.value = data.paymentUrl;
                        window.location.href = data.paymentUrl;
                    } else {
                        throw new Error(data.message || 'Failed to create payment');
                    }
                } catch (err) {
                    error.value = err.message;
                } finally {
                    isLoading.value = false;
                }
            };
            
            return {
                amount,
                description,
                paymentUrl,
                isLoading,
                error,
                createPayment
            };
        }
    }).mount('#payment-app');
}