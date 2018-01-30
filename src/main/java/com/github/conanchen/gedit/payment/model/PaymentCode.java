package com.github.conanchen.gedit.payment.model;

public class PaymentCode {

    private volatile String payeeCode;
    private long expiresIn_;
    private volatile String payeeUuid;
    private volatile String payeeLogo;
    private volatile String payeeName;
    private volatile String payeeStoreUuid;
    private volatile String payeeStoreLogo;
    private volatile String payeeStoreNamee;
    private volatile String payeeWorkerUuid;
    private volatile String payeeWorkerLogo;
    private volatile String payeeWorkerName;

    public String getPayeeCode() {
        return payeeCode;
    }

    public void setPayeeCode(String payeeCode) {
        this.payeeCode = payeeCode;
    }

    public long getExpiresIn_() {
        return expiresIn_;
    }

    public void setExpiresIn_(long expiresIn_) {
        this.expiresIn_ = expiresIn_;
    }

    public String getPayeeUuid() {
        return payeeUuid;
    }

    public void setPayeeUuid(String payeeUuid) {
        this.payeeUuid = payeeUuid;
    }

    public String getPayeeLogo() {
        return payeeLogo;
    }

    public void setPayeeLogo(String payeeLogo) {
        this.payeeLogo = payeeLogo;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getPayeeStoreUuid() {
        return payeeStoreUuid;
    }

    public void setPayeeStoreUuid(String payeeStoreUuid) {
        this.payeeStoreUuid = payeeStoreUuid;
    }

    public String getPayeeStoreLogo() {
        return payeeStoreLogo;
    }

    public void setPayeeStoreLogo(String payeeStoreLogo) {
        this.payeeStoreLogo = payeeStoreLogo;
    }

    public String getPayeeStoreNamee() {
        return payeeStoreNamee;
    }

    public void setPayeeStoreNamee(String payeeStoreNamee) {
        this.payeeStoreNamee = payeeStoreNamee;
    }

    public String getPayeeWorkerUuid() {
        return payeeWorkerUuid;
    }

    public void setPayeeWorkerUuid(String payeeWorkerUuid) {
        this.payeeWorkerUuid = payeeWorkerUuid;
    }

    public String getPayeeWorkerLogo() {
        return payeeWorkerLogo;
    }

    public void setPayeeWorkerLogo(String payeeWorkerLogo) {
        this.payeeWorkerLogo = payeeWorkerLogo;
    }

    public String getPayeeWorkerName() {
        return payeeWorkerName;
    }

    public void setPayeeWorkerName(String payeeWorkerName) {
        this.payeeWorkerName = payeeWorkerName;
    }
}
