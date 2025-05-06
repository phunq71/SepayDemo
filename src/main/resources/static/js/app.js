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
            const qrCodeUrl = ref('');
            const orderId = ref('');
            const isLoading = ref(false);
            const error = ref(null);
            
            const createPayment = async () => {
                isLoading.value = true;
                error.value = null;
                qrCodeUrl.value = '';
                
                try {
                    const response = await fetch('/api/payment/create-with-qr', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            amount: parseFloat(amount.value),
                            description: description.value,
                            returnUrl: window.location.origin + '/payment/success',
                            cancelUrl: window.location.origin + '/payment/cancel'
                        })
                    });
                    
                    const data = await response.json();
                    
                    if (response.ok) {
                        paymentUrl.value = data.paymentUrl;
                        qrCodeUrl.value = data.qrCodeUrl;
                        orderId.value = data.orderId;
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
                qrCodeUrl,
                orderId,
                isLoading,
                error,
                createPayment
            };
        }
    }).mount('#payment-app');
}
