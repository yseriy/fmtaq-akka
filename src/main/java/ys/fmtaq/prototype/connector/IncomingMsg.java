package ys.fmtaq.prototype.connector;

public class IncomingMsg {

    private final String body;

    IncomingMsg(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}
