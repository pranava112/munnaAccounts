package com.vpm.Accounts.Entity.GST;

public class EInvoiceResponse {
    private String irn;
    private String ackNo;
    private String status;
    private String message;

    public String getIrn() { return irn; }
    public void setIrn(String irn) { this.irn = irn; }
    public String getAckNo() { return ackNo; }
    public void setAckNo(String ackNo) { this.ackNo = ackNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
