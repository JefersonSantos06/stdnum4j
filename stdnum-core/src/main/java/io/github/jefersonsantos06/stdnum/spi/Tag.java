package io.github.jefersonsantos06.stdnum.spi;

/**
 * Broad categories a number type can belong to, used for discovery through
 * {@code StdNums.byTag(...)}. A number may carry several tags (a personal
 * tax number is both {@link #TAX} and {@link #PERSON}).
 */
public enum Tag {

    /** Tax or fiscal identifiers (CPF, NIF, TIN, ...). */
    TAX,

    /** Value added tax registration numbers (VAT, GST, ...). */
    VAT,

    /** Identifies a natural person. */
    PERSON,

    /** Identifies a company or other legal entity. */
    COMPANY,

    /** Bank accounts, bank codes and payment references. */
    BANK,

    /** Payment instruments: payment slips, transfer references, QR payloads. */
    PAYMENT,

    /** Financial instruments and securities (ISIN, CUSIP, ...). */
    FINANCIAL,

    /** Health-related identifiers (health cards, patient ids, ...). */
    HEALTH,

    /** Vehicles and transport (VIN, RENAVAM, container codes, ...). */
    VEHICLE,

    /** Postal codes and routing identifiers. */
    POSTAL,

    /** Telecom identifiers (IMEI, IMSI, MAC, ...). */
    TELECOM,

    /** Published media (ISBN, ISSN, ISRC, ...). */
    MEDIA,

    /** Product and article numbers (EAN, GTIN, ...). */
    PRODUCT,

    /** Education-related identifiers. */
    EDUCATION,

    /** Geographic or administrative location codes. */
    LOCATION,

    /** Anything that does not fit the categories above. */
    OTHER
}
