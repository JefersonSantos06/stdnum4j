package io.github.jefersonsantos06.stdnum.br;

import io.github.jefersonsantos06.stdnum.br.ie.InscricaoEstadual;
import io.github.jefersonsantos06.stdnum.spi.StdNum;
import io.github.jefersonsantos06.stdnum.spi.StdNumProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Registers every Brazilian number type with the registry. */
public final class BrProvider implements StdNumProvider {

    @Override
    public Collection<StdNum> numbers() {
        List<StdNum> numbers = new ArrayList<>(List.of(
                Cpf.INSTANCE,
                Cnpj.INSTANCE,
                PisPasep.INSTANCE,
                Cns.INSTANCE,
                TituloEleitor.INSTANCE,
                Renavam.INSTANCE,
                ChaveNfe.INSTANCE));
        numbers.addAll(InscricaoEstadual.all());
        return numbers;
    }
}
