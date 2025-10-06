package at.ac.fhtw.swen3.swen3teamm.service.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractMapper<S, T> { //generische Klasse mit source und target

    public abstract T toDto(S source); //einzelnes Objekt S nach T mappen

    public final List<T> toDto(Collection<S> sources) {
        List<T> dtos = new ArrayList<>();
        if (sources != null) {
            for (S s : sources) {
                if (s != null) dtos.add(toDto(s));
            }
        }
        return ordering(dtos);
    }

    //default: wenn nicht überschrieben wird, gibt es keine spezielle Sortierung
    List<T> ordering(List<T> dtos) {
        return dtos;
    }
}
