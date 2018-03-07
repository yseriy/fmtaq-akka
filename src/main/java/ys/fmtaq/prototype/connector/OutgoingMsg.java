package ys.fmtaq.prototype.connector;

public final class OutgoingMsg {

    private final String Address;
    private final String body;

    public OutgoingMsg(String address, String body) {
        Address = address;
        this.body = body;
    }

    public String getAddress() {
        return Address;
    }

    public String getBody() {
        return body;
    }
}
