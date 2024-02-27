package classes;

import recovery.RecoverySet;

public class First {
    static public final RecoverySet lista_de_comandos = new RecoverySet();
    static public final RecoverySet comando_selecao = new RecoverySet();

    static {
        lista_de_comandos.add(LinguagemConstants.READ);
        lista_de_comandos.add(LinguagemConstants.WRITE);
        lista_de_comandos.add(LinguagemConstants.DESIGNATE);
        lista_de_comandos.add(LinguagemConstants.AVALIATE);
        lista_de_comandos.add(LinguagemConstants.REPEAT);

        comando_selecao.add(LinguagemConstants.TRUE);
        comando_selecao.add(LinguagemConstants.UNTRUE);
    }
}