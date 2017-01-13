package uk.co.nevarneyok.interfaces;


import uk.co.nevarneyok.entities.delivery.Payment;

public interface PaymentDialogInterface {
    void onPaymentSelected(Payment payment);
}
