package com.betanalyzer.domain.enums;

public enum MatchStatus {
    TBD, NS, LIVE, FT, HT, ET, P, AET, SUSP, ABD, PST, CANC,
    ONE_H("1H"), TWO_H("2H");

    private String code;

    MatchStatus() {
    }

    MatchStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code != null ? code : name();
    }

    public static MatchStatus fromCode(String code) {
        if (code == null) return null;
        for (MatchStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code) || status.name().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return TBD; // Default or could throw exception
    }

    /** Pré-jogo ou ao vivo — alinhado ao front {@code SUGGESTABLE_MATCH_STATUSES}. */
    public boolean allowsOddsCapture() {
        return switch (this) {
            case NS, TBD, LIVE, HT, ET, ONE_H, TWO_H -> true;
            default -> false;
        };
    }
}
