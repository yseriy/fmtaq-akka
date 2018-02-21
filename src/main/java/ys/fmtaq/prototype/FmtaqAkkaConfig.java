package ys.fmtaq.prototype;

public enum FmtaqAkkaConfig {
    INSTANCE;

    public String getSystemName() {
        return "Fmtaq";
    }

    public String getInboundStreamAddress() {
        return "inbound_stream";
    }

    public String getOutboundStreamAddress() {
        return "outbound_stream";
    }
}
