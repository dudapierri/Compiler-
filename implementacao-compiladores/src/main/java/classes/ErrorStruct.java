package classes;

public class ErrorStruct {
    // Variáveis de instância para armazenar a mensagem de erro e a exceção ParseException associada a ela
    private ParseException error = null;
    private String msg = null;
    // Construtor da classe que recebe uma mensagem de erro e uma exceção ParseException
    public ErrorStruct(String msg, ParseException error){
        this.error = error;
        this.msg = msg;
    }
    // Método para obter a mensagem de erro
    public String getMsg() {
        return msg;
    }
    // Método para obter a exceção ParseException associada ao erro
    public ParseException getError() {
        return error;
    }
    // Método para obter a representação de texto das palavras-chave esperadas na exceção
    public String expected(){
        String expectedMsg = "";
        for (int i=0; i < this.error.expectedTokenSequences.length; i++){
            expectedMsg += "  ";
            for (int j=0; j < this.error.expectedTokenSequences[i].length; j++){
                expectedMsg += LinguagemConstants.tokenImage[this.error.expectedTokenSequences[i][j]] + ", ";
            }
            expectedMsg += " ";
        }
        return expectedMsg;
    }

    // Método para obter uma representação de texto da mensagem de erro e das palavras-chave esperadas
    @Override
    public String toString() {
        String expectedMsg = "";

        for (int i=0; i < this.error.expectedTokenSequences.length; i++){
            expectedMsg += "  ";
            for (int j=0; j < this.error.expectedTokenSequences[i].length; j++){
                expectedMsg += LinguagemConstants.tokenImage[this.error.expectedTokenSequences[i][j]] + ", ";
            }
            expectedMsg += " \n";
        }
        return msg + "Esperado: " + expectedMsg ;
    }

}