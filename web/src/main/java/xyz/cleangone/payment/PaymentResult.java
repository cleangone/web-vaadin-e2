package xyz.cleangone.payment;

public class PaymentResult
{
    public enum PaymentStatus { Success, Failure }

    private PaymentStatus paymentStatus;
    private String message;

    public PaymentResult(PaymentStatus paymentStatus)
    {
        this.paymentStatus = paymentStatus;
    }

    public PaymentResult(PaymentStatus paymentStatus, String message)
    {
        this.paymentStatus = paymentStatus;
        this.message = message;
    }

    public boolean isSuccess()
    {
        return paymentStatus == PaymentStatus.Success;
    }
    public void setPaymentStatusFail()
    {
        this.paymentStatus = PaymentStatus.Failure;
    }

    public String getMessage()
    {
        return message;
    }
    public void setMessage(String message)
    {
        this.message = message;
    }
}
