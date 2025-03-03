package classes;

public class Simbolo {
    private String identificador;
    private int categoria;
    private int atributo1;
    private int atributo2;

    public Simbolo(String identificador, int categoria){
        this.identificador = identificador;
        this.categoria = categoria;
    }

    public Simbolo(String identificador, int categoria, int atributo1){
        this.identificador = identificador;
        this.categoria = categoria;
        this.atributo1 = atributo1;
    }

    public Simbolo(String identificador, int categoria, int atributo1, int atributo2){
        this.identificador = identificador;
        this.categoria = categoria;
        this.atributo1 = atributo1;
        this.atributo2 = atributo2;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public int getCategoria() {
        return categoria;
    }

    public void setCategoria(int categoria) {
        this.categoria = categoria;
    }

    public int getAtributo1() {
        return atributo1;
    }

    public void setAtributo1(int atributo1) {
        this.atributo1 = atributo1;
    }

    public int getAtributo2() {
        return atributo2;
    }

    public void setAtributo2(int atributo2) {
        this.atributo2 = atributo2;
    }

    @Override
    public String toString() {
        return "Simbolo{" +
                "identificador='" + identificador + '\'' +
                ", categoria=" + categoria +
                ", atributo1=" + atributo1 +
                ", atributo2=" + atributo2 +
                '}';
    }



}
