package io.github.jefersonsantos06.stdnum.br;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The 27 Brazilian federative units (26 states plus the Federal District),
 * with their IBGE numeric codes as used in the NF-e access key and other
 * fiscal documents.
 */
public enum Uf {

    AC(12, "Acre"),
    AL(27, "Alagoas"),
    AP(16, "Amapá"),
    AM(13, "Amazonas"),
    BA(29, "Bahia"),
    CE(23, "Ceará"),
    DF(53, "Distrito Federal"),
    ES(32, "Espírito Santo"),
    GO(52, "Goiás"),
    MA(21, "Maranhão"),
    MT(51, "Mato Grosso"),
    MS(50, "Mato Grosso do Sul"),
    MG(31, "Minas Gerais"),
    PA(15, "Pará"),
    PB(25, "Paraíba"),
    PR(41, "Paraná"),
    PE(26, "Pernambuco"),
    PI(22, "Piauí"),
    RJ(33, "Rio de Janeiro"),
    RN(24, "Rio Grande do Norte"),
    RS(43, "Rio Grande do Sul"),
    RO(11, "Rondônia"),
    RR(14, "Roraima"),
    SC(42, "Santa Catarina"),
    SP(35, "São Paulo"),
    SE(28, "Sergipe"),
    TO(17, "Tocantins");

    private static final Map<Integer, Uf> BY_IBGE = new HashMap<>();

    static {
        for (Uf uf : values()) {
            BY_IBGE.put(uf.ibgeCode, uf);
        }
    }

    private final int ibgeCode;
    private final String displayName;

    Uf(int ibgeCode, String displayName) {
        this.ibgeCode = ibgeCode;
        this.displayName = displayName;
    }

    /** The two-digit IBGE code of this federative unit (SP = 35). */
    public int ibgeCode() {
        return ibgeCode;
    }

    /** The Portuguese name of this federative unit. */
    public String displayName() {
        return displayName;
    }

    /** Resolves an IBGE code to its federative unit, if it exists. */
    public static Optional<Uf> fromIbge(int ibgeCode) {
        return Optional.ofNullable(BY_IBGE.get(ibgeCode));
    }
}
