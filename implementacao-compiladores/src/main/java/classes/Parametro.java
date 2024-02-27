package classes;

public class Parametro {

    public final Object conteudo;
    public final Tipo tipo;

    public Parametro(Tipo tipo, Object conteudo) {
        if (tipo == Tipo.NONE)
            this.conteudo = '?';
        else
            this.conteudo = conteudo;
        this.tipo = tipo;
    }


    @Override
    public String toString() {
        String contStr = null;
        switch (tipo) {
            case FLOAT, ADDRESS -> {
                contStr = (conteudo).toString();
            }
            case INTEGER -> {
                try {
                    contStr = String.valueOf(Integer.parseInt(String.valueOf(conteudo)));
                } catch (Exception e) {
                    contStr = String.valueOf(conteudo);
                }
            }
            case BOOLEAN -> {
                contStr = ((Boolean) conteudo).toString();
            }
            case LITERAL, NONE -> {
                contStr = String.valueOf(conteudo);
            }
        }
        if (contStr == null) {
            return "0";
        }
        return String.format("%s", contStr);
    }

    public String toDebugString() {
        return String.format("%s T: %s", this.toString(), tipo);
    }

}
