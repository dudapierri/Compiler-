package classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Acoes {
    private String contexto = "";
    private int vt = 0;
    private int vp = 0;
    private int vi = 0;
    private int tvi = 0;
    private int tipo;
    private int ponteiro = 1;
    boolean variavelIndexada = false;
    private String saida = "";
    private List<Integer> pilhaDeDesvios = new ArrayList<>();
    private List<Simbolo> tabelaDeSimbolos = new ArrayList<>();
    private static List<Instrucao> areaDeInstrucoes = new ArrayList<>();

    private String identificadorReconhecido;
    private int constanteInteira;
    private List<Object> listaAtributos = new ArrayList<>();
    private static List<String> listaErros = new ArrayList<>();

    private List<String> tiposEnumerados = new ArrayList<>();
    private List<String> constantesTiposEnumerados = new ArrayList<>();

    public List<Instrucao> getInstrucao() {
        var tmp = areaDeInstrucoes;
        areaDeInstrucoes = new ArrayList<>();
        return tmp;
    }

    public List<String> getListaErros() {
        var tmp = listaErros;
        listaErros = new ArrayList<>();
        return tmp;
    }

    public void a1(Token token){  // reconhecimento do identificador de programa
        Simbolo simbolo = new Simbolo(token.image, 0);
        tabelaDeSimbolos.add(simbolo);
    }

    public void a2() { // reconhecimento de fim de programa
        Instrucao instruction = new Instrucao(Instrucao.Mnemonic.STP, new Parametro(Tipo.INTEGER, 0));
        areaDeInstrucoes.add(instruction);
        Instrucao.enumerarInstrucoes(areaDeInstrucoes);
        System.out.println("Erros: " + listaErros.toString());
        System.out.println("Instruções:");
        for (Instrucao i : areaDeInstrucoes) {
            System.out.println(i.toString());
        }
    }

    public void a3(Token token){ //reconhecimento de identificador de tipo enumerado
        Simbolo simbolo = this.tabelaDeSimbolos.stream().filter(simb -> token.image.equals(simb.getIdentificador())).findAny().orElse(null);
        if (simbolo != null || tiposEnumerados.contains(token.image)) {
            this.listaErros.add("ERRO A3: Identificador já declarado" + token.image + "- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
        } else {
            tiposEnumerados.add(token.image);
        }
    }

    public void a4(Token token){ //reconhecimento de identificador de constante de tipo enumerado
        Simbolo simbolo = this.tabelaDeSimbolos.stream().filter(simb -> token.image.equals(simb.getIdentificador())).findAny().orElse(null);
        if (simbolo != null || tiposEnumerados.contains(token.image) || constantesTiposEnumerados.contains(token.image)) {
            this.listaErros.add("ERRO A4: Identificador já declarado" + token.image + "- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
        } else {
            constantesTiposEnumerados.add(token.image);
        }
    }

    public void a5(){ //reconhecimento de palavras reservadas constant
            this.contexto = "as constant";
            System.out.println("contexto: as constant");
            this.vi = 0;
            this.tvi = 0;


    }

    public void a6(){ // reconhecimento do término da declaração de constantes ou variáveis de um determinado tipo
        int last_index = tabelaDeSimbolos.size() - 1;
        for (int i = last_index; i > last_index - (vp + vi); --i)
            tabelaDeSimbolos.get(i).setCategoria(tipo);

        this.vp = this.vp + this.tvi;
        switch (this.tipo){
            case 1: case 5: {
                Instrucao instrucao = new Instrucao(Instrucao.Mnemonic.ALI, new Parametro(Tipo.INTEGER, this.vp));
                areaDeInstrucoes.add(instrucao);
                this.ponteiro++;
                break;
            }
            case 2:
            case 6:
            {
                Instrucao instrucao = new Instrucao(Instrucao.Mnemonic.ALR, new Parametro(Tipo.INTEGER, this.vp));
                areaDeInstrucoes.add(instrucao);
                this.ponteiro++;
                break;
            }
            case 3: case 7: {
                Instrucao instrucao = new Instrucao(Instrucao.Mnemonic.ALS, new Parametro(Tipo.INTEGER, this.vp));
                areaDeInstrucoes.add(instrucao);
                this.ponteiro++;
                break;
            }
            case 4:{
                Instrucao instrucao = new Instrucao(Instrucao.Mnemonic.ALB, new Parametro(Tipo.INTEGER, this.vp));
                areaDeInstrucoes.add(instrucao);
                this.ponteiro++;
                break;
            }
        }
        if(this.tipo == 1 || this.tipo == 2 || this.tipo == 3 || this.tipo == 4){
            this.vp = 0;
            this.vi = 0;
            this.tvi = 0;
        }
    }

    public void a7(String valor){ // Reconhecimento de valor na declaração de constante
        switch (this.tipo){
            case 5: {
                Instrucao instrucao = new Instrucao(Instrucao.Mnemonic.LDI, new Parametro(Tipo.INTEGER, Integer.parseInt(valor)));
                areaDeInstrucoes.add(instrucao);
                this.ponteiro++;
                break;
            }
            case 6: {
                Instrucao instrucao = new Instrucao(Instrucao.Mnemonic.LDR, new Parametro(Tipo.FLOAT, Float.parseFloat(valor)));
                areaDeInstrucoes.add(instrucao);
                this.ponteiro++;
                break;
            }
            case 7: {
                Instrucao instrucao = new Instrucao(Instrucao.Mnemonic.LDS, new Parametro(Tipo.LITERAL, valor));
                areaDeInstrucoes.add(instrucao);
                this.ponteiro++;
                break;
            }
        }
        Instrucao instrucao = new Instrucao(Instrucao.Mnemonic.STC, new Parametro(Tipo.INTEGER, this.vp));
        areaDeInstrucoes.add(instrucao);
        this.ponteiro = this.ponteiro + 1;
        this.vp = 0;
    }

    public void a8(){ //Reconhecimento das palavras reservadas as variable
        this.contexto = "as variable";
    }

    public void a9(Token token) {  // Reconhecimento de identificador de constante
        Simbolo simbolo = tabelaDeSimbolos.stream().filter(simb -> token.image.equals(simb.getIdentificador())).findAny().orElse(null);
        if (simbolo != null || tiposEnumerados.contains(token.image) || constantesTiposEnumerados.contains(token.image)) {
            this.listaErros.add("ERRO A9: Identificador já declarado: '" + token.image + "- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
        }else{
            this.vt = this.vt + 1;
            this.vp = this.vp + 1;
            simbolo = new Simbolo(token.image, this.tipo, this.vt);
            tabelaDeSimbolos.add(simbolo);
        }
    }

    public void a10(Token token) { // Reconhecimento de identificador de variável
        switch (this.contexto){
            case "as variable": {
                Simbolo exist = tabelaDeSimbolos.stream().filter(simb -> token.image.equals(simb.getIdentificador())).findAny().orElse(null);
                if (exist != null || tiposEnumerados.contains(token.image) || constantesTiposEnumerados.contains(token.image)) {
                    this.listaErros.add("ERRO A10: Identificador ja declarado: '" + token.image + "- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
                } else {
                    this.variavelIndexada = false;
                    this.identificadorReconhecido = token.image;
                }
                break;
            }
            case "atribuicao": case "entrada dados": {
                variavelIndexada = false;
                this.identificadorReconhecido = token.image;
                break;
            }
        }
    }

    public void a11(Token token) { // Reconhecimento de identificador de variável e tamanho da variável indexada
        System.out.println("contexto setado = " + this.contexto);
        switch (this.contexto) {
            case "as variable": {
                if (!this.variavelIndexada) {
                    this.vt = this.vt + 1;
                    this.vp = this.vp + 1;
                    Simbolo simbolo = new Simbolo(this.identificadorReconhecido, this.tipo, this.vt);
                    tabelaDeSimbolos.add(simbolo);
                } else {
                    this.vi = this.vi + 1;
                    this.tvi = this.tvi + this.constanteInteira;
                    Simbolo simbolo = new Simbolo(this.identificadorReconhecido, this.tipo, this.vt + 1, this.constanteInteira);
                    tabelaDeSimbolos.add(simbolo);
                    this.vt = this.vt + this.constanteInteira;
                }
                break;
            }
            case "atribuicao": {
                Simbolo simbolo = this.identificadorReconhecido == null ? null : tabelaDeSimbolos.stream().filter(simb -> this.identificadorReconhecido.equals(simb.getIdentificador())).findAny().orElse(null);
                if (simbolo != null) {
                    if (simbolo.getAtributo2() == 0) {
                        if (!this.variavelIndexada) {
                            this.listaAtributos.add(simbolo.getAtributo1());
                        } else {
                            this.listaErros.add("ERRO A11: Identificador de variavel nao indexada '" + this.identificadorReconhecido + "- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
                        }
                    } else {
                        if (this.variavelIndexada) {
                            this.listaAtributos.add(simbolo.getAtributo1() + this.constanteInteira - 1);
                        } else {
                            this.listaErros.add("ERRO A11: Variavel indexada exige indice " + "- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
                        }
                    }
                } else {
                    this.listaErros.add("ERRO A11: Identificador nao declarado ou identificador de programa, de constante ou de tipo enumerado" + "- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
                }
                break;
            }
            case "entrada dados": {
                Simbolo simbolo = this.identificadorReconhecido == null ? null : tabelaDeSimbolos.stream().filter(simb -> this.identificadorReconhecido.equals(simb.getIdentificador())).findAny().orElse(null);
                if (simbolo != null) {
                    int atr1 = simbolo.getAtributo1();
                    int atr2 = simbolo.getAtributo2();
                    //int categoria = simbolo.getCategoria();
                    int categ = simbolo.getCategoria();
                    if (simbolo.getAtributo2() == 0) {
                        if (!this.variavelIndexada) {
                            areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.REA, new Parametro(Tipo.get(categ), '?')));
                            //areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.REA, new Parametro(Tipo.get(atr1), '?')));
                            this.ponteiro = this.ponteiro + 1;
                            areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.STR, new Parametro(Tipo.ADDRESS, atr1)));
                            this.ponteiro = this.ponteiro + 1;
                        } else {
                            this.listaErros.add("ERRO A11: Identificador de variavel nao indexada" + "- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
                        }
                    } else {
                        if (this.variavelIndexada) {
                            areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.REA, new Parametro(Tipo.get(categ), '?')));
                            //areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.REA, new Parametro(Tipo.get(atr1), '?')));
                            this.ponteiro = this.ponteiro + 1;
                            areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.STR, new Parametro(Tipo.ADDRESS, atr1 + this.constanteInteira - 1)));
                            this.ponteiro = this.ponteiro + 1;
                        } else {
                        this.listaErros.add("ERRO A11: identificador de varivael indexada exige indice '" + this.identificadorReconhecido + "- Linha: " + token.beginLine + "; " + "Coluna: " + token.beginColumn);
                        }
                    }
                    break;
                }
                break;
            }
        }
    }
    public void a12(Token token){ // Reconhecimento de constante inteira como tamanho da variável indexada
        try {
            this.constanteInteira = Integer.parseInt(token.image);
        } catch (Exception e) {
            System.out.println("Acao 12 - falha na conversao para inteiro: " + token.image);
        } finally {
            this.variavelIndexada = true;
        }
    }

    public void a13(){ // Reconhecimento da palavra reservada integer
        if(this.contexto.equals("as variable")){
            this.tipo = 1;
        }else{
            this.tipo = 5;
        }
    }

    public void a14(){ // Reconhecimento da palavra reservada float
        if(this.contexto.equals("as variable")){
            this.tipo = 2;
        }else{
            this.tipo = 6;
        }
    }

    public void a15(){ //   Reconhecimento da palavra reservada string
        if(this.contexto.equals("as variable")){
            this.tipo = 3;
        }else{
            this.tipo = 7;
        }
    }

    public void a16(Token token){ // Reconhecimento da palavra reservada boolean
        if(this.contexto.equals("as variable")){
            this.tipo = 4;
        }else{
            this.listaErros.add("ERRO A16: Tipo invalido para constante "+"- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
        }
    }

    public void a17(Token token){ // Reconhecimento de identificador do tipo enumerado
        if(this.contexto.equals("as variable")){
            this.tipo = 1;
        }else{
            this.listaErros.add("ERRO A17 - Tipo invalido para constante "+ "- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
        }
    }

    public void a18(){ //reconhecimento do início do comando de atribuicao
        this.contexto = "atribuicao";
    }

    public void a19(){ //Reconhecimento do fim do comando de atribuicao
        /*for(int i=0; i< this.listaAtributos.size(); i++){
            if (areaDeInstrucoes.size() > 0) {
                Parametro parametro = new Parametro(Tipo.ADDRESS, this.listaAtributos.get(i));
                Instrucao instrucaoAnterior = areaDeInstrucoes.get(areaDeInstrucoes.size() -1);
                if (!instrucaoAnterior.getCodigo().equals(Instrucao.Mnemonic.STR) || !instrucaoAnterior.getParametro().conteudo.equals(parametro.conteudo))
                    areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.STR, parametro));
            } else {
                areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.STR, new Parametro(Tipo.ADDRESS, this.listaAtributos.get(i))));
            }
            this.ponteiro = this.ponteiro + 1;
        }*/
        for(int i=0; i< this.listaAtributos.size(); i++){
            areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.STR, new Parametro(Tipo.ADDRESS, this.listaAtributos.get(i))));
            this.ponteiro = this.ponteiro + 1;
        }
        listaAtributos.clear();
    }

    public void a20(){ // Reconhecimento do comando de entrada de dados
        this.contexto = "entrada dados";

    }

    public void a21() { // Reconhecimento das palavras reservadas write all this
        saida = "write all this";
    }

    public void a22() { // reconhecimento das palavras reservadas write this
        saida = "write this";
    }

    public void a23(){ // Reconhecimento de mensagem em comando de saída de dados
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.WRT, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a24(Token token){ //reconhecimento de identificador em comando de saída ou em expressão
        Simbolo simbolo = this.tabelaDeSimbolos.stream().filter(simb -> token.image.equals(simb.getIdentificador())).findAny().orElse(null);
        if (simbolo != null) {
            this.variavelIndexada = false;
            this.identificadorReconhecido = token.image;
        }else{
            this.listaErros.add("ERRO A24: Identificador nao declarado, identificador de programa ou de tipo enumerado "+token.image+"- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
        }

    }

    public void a25(Token token) { // reconhecimento de identificador de constante ou de variável e tamanho de variável indexada em comando de saída
        Simbolo exist = null;
        if (this.identificadorReconhecido != null)
            exist = this.tabelaDeSimbolos.stream().filter(simb -> this.identificadorReconhecido.equals(simb.getIdentificador())).findAny().orElse(null);
        if(exist != null) {
            if (!variavelIndexada) {
                if (exist.getAtributo2() == 0) {
                    if (saida.equals("write all this")) {
                        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.LDS, new Parametro(Tipo.LITERAL, exist.getIdentificador() + " = ")));
                        this.ponteiro = this.ponteiro + 1;
                        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.WRT, new Parametro(Tipo.ADDRESS, 0)));
                        this.ponteiro = this.ponteiro + 1;
                    }
                    areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.LDV, new Parametro(Tipo.ADDRESS, exist.getAtributo1())));
                    this.ponteiro = this.ponteiro + 1;
                } else {
                    this.listaErros.add("ERRO A25 - Identificador de variavel indexada exige indice'"+token.image+"- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
                }
            } else {
                if(exist.getAtributo2() != 0) {
                    if(saida.equals("write all this")) {
                        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.LDS, new Parametro(Tipo.LITERAL, exist.getIdentificador() + " = ")));
                        this.ponteiro = this.ponteiro + 1;
                        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.WRT, new Parametro(Tipo.ADDRESS, 0)));
                        this.ponteiro = this.ponteiro + 1;
                    }
                    areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.LDV, new Parametro(Tipo.ADDRESS, exist.getAtributo1() + this.constanteInteira -1)));
                    this.ponteiro = this.ponteiro + 1;
                }else{
                    this.listaErros.add("ERRO A25: Identificador de constante ou variavel nao indexada: '"+this.identificadorReconhecido+"- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
                }
            }
        }
    }

    public void a26(Integer constInt){ //reconhecimento de constante inteira em comando de saída ou em expressão
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.LDI, new Parametro(Tipo.INTEGER, constInt)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a27(Float constReal){ //reconhecimento de constante real em comando de saída ou em expressão
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.LDR, new Parametro(Tipo.FLOAT, constReal)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a28(String constLiteral){ // reconhecimento de constante literal em comando de saída ou em expressão
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.LDS, new Parametro(Tipo.LITERAL, constLiteral)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a29(){//reconhecimento de fim de comando de seleção
        Integer endereco = this.pilhaDeDesvios.remove(this.pilhaDeDesvios.size()-1);
        areaDeInstrucoes.get(endereco).setParametro(new Parametro(Tipo.ADDRESS, this.ponteiro));
        //this.pilhaDeDesvios.set(this.pilhaDeDesvios.size()-1, ponteiro);
    }

    public void a30(){ //reconhecimento da palavra reservada true
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.JMF, new Parametro(Tipo.NONE, '?')));
        this.pilhaDeDesvios.add(this.ponteiro -1);
        this.ponteiro = this.ponteiro + 1;
    }

    public void a31(){ //reconhecimento da palavra reservada untrue
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.JMT, new Parametro(Tipo.ADDRESS, '?')));
        this.pilhaDeDesvios.add(this.ponteiro -1);
        this.ponteiro = this.ponteiro + 1;
    }

    public void a32(){ //reconhecimento da palavra reservada untrue (ou true)
        Integer endereco = this.pilhaDeDesvios.remove(this.pilhaDeDesvios.size()-1);
        areaDeInstrucoes.get(endereco).setParametro(new Parametro(Tipo.ADDRESS, this.ponteiro + 1));
        //this.pilhaDeDesvios.set(this.pilhaDeDesvios.size()-1, ponteiro+1);
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.JMP, new Parametro(Tipo.NONE, '?')));
        this.pilhaDeDesvios.add(this.ponteiro - 1);
        this.ponteiro = this.ponteiro + 1;
    }

    public void a33(){ //reconhecimento do início de expressão em comando de repetição
        this.pilhaDeDesvios.add(this.ponteiro);
    }

    public void a34(){ //reconhecimento de expressão em comando de repetição
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.JMF, new Parametro(Tipo.INTEGER, '?')));
        this.pilhaDeDesvios.add(this.ponteiro-1);
        this.ponteiro = this.ponteiro + 1;
    }

    public void a35(){ //reconhecimento do fim do comando de repetição
        Integer desvioAcao34 = this.pilhaDeDesvios.remove(this.pilhaDeDesvios.size()-1);
        areaDeInstrucoes.get(desvioAcao34).setParametro(new Parametro(Tipo.ADDRESS, this.ponteiro + 1));
        Integer desvioAcao33 = this.pilhaDeDesvios.remove(this.pilhaDeDesvios.size()-1);
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.JMP, new Parametro(Tipo.ADDRESS, desvioAcao33)));
        this.ponteiro++;
    }

    public void a36(){ //reconhecimento de operação relacional igual
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.EQL, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a37(){ //reconhecimento de operação relacional diferente
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.DIF, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a38(){ //reconhecimento de operação relacional menor
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.SMR, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a39(){ //reconhecimento de operação relacional maior
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.BGR, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a40(){ //reconhecimento de operação relacional menor igual
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.SME, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a41(){ //reconhecimento de operação relacional maior igual
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.BGE, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a42(){ //reconhecimento de operação aritmética adição
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.ADD, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a43(){ //reconhecimento de operação aritmética subtração
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.SUB, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a44(){ //reconhecimento de operação lógica OU ( | )
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.OR, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a45(){//reconhecimento de operação aritmética multiplicação
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.MUL, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a46(){ //reconhecimento de operação aritmética divisão real
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.DIV, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a47(){//reconhecimento de operação aritmética divisão inteira
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.DVW, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a48(){ //reconhecimento de operação aritmética resto da divisão inteira
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.MOD, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a49(){ //reconhecimento de operação lógica E
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.AND, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a50(){ //reconhecimento de operação aritmética potenciação
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.PWR, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = ponteiro + 1;
    }

    public void a51(Token token){ //reconhecimento de identificador de constante ou de variável e tamanho de variável indexada em expressão
        Simbolo exist = tabelaDeSimbolos.stream().filter(simb -> this.identificadorReconhecido.equals(simb.getIdentificador())).findAny().orElse(null);
        if(!this.variavelIndexada){
            if(exist.getAtributo2() == 0){
                areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.LDV, new Parametro(Tipo.ADDRESS, exist.getAtributo1())));
                this.ponteiro = this.ponteiro + 1;
            }else{
                this.listaErros.add("ERRO A51: Identificador de variavel indexada exige indice'"+this.identificadorReconhecido+"- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
            }
        }else{
            if(exist.getAtributo2() != 0){
                System.out.println("Acao 51 - Atributo 2 é diferente de vazio/0");
                areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.LDV, new Parametro(Tipo.ADDRESS, exist.getAtributo1() + this.constanteInteira -1)));
                this.ponteiro = this.ponteiro + 1;
            }else{
                this.listaErros.add("ERRO A51: Idnetificador de constante ou de variavel nao indexada"+this.identificadorReconhecido+"- Linha: " + token.beginLine + "; "+ "Coluna: " +  token.beginColumn);
            }
        }
    }

    public void a52(){ //reconhecimento de constante lógica true
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.LDB, new Parametro(Tipo.BOOLEAN, true)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a53(){ //reconhecimento de constante lógica untrue
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.LDB, new Parametro(Tipo.BOOLEAN, false)));
        this.ponteiro = this.ponteiro + 1;
    }

    public void a54(){ //reconhecimento de operação lógica não
        areaDeInstrucoes.add(new Instrucao(Instrucao.Mnemonic.NOT, new Parametro(Tipo.INTEGER, 0)));
        this.ponteiro = this.ponteiro + 1;
    }






}
