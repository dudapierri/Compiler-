package classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Tipo {
    INTEGER(1),
    FLOAT(2),
    LITERAL(3),
    BOOLEAN(4),
    ADDRESS(5),
    FLOAT_CONSTANT(6),
    LITERAL_CONSTANT(7),
    NONE(0);

    public final Integer id;

    private Tipo(Integer value) {
        this.id = value;
    }

    public static Tipo get(int id) {
        for (Tipo d : values()) {
            if (d.id.equals(id)) {
                return d;
            }
        }
        return null;
    }

    public static List<Tipo> getTipos() {
        return new ArrayList<>(Arrays.asList(Tipo.FLOAT, Tipo.INTEGER));
    }
}