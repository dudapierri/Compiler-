// funções do layout
package gui;


import classes.Acoes;
import classes.Instrucao;
import classes.Linguagem;
import classes.MaquinaVirtual;
import classes.ParseException;
import classes.ParseEOFException;
import classes.Status;
import classes.Tipo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import util.AlertFactory;
import util.Operation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Controller {
    // Declaração de variáveis e elementos da interface gráfica
    private EditorFile editorFile = new EditorFile();
    private static boolean hasEditedFile = false;
    private static boolean hasOpenFile = false;
    private boolean isReadingConsole = false;
    private MaquinaVirtual vm;
    private List<Instrucao> listaDeInstrucoes;
    // tabela da máquina virtual
    @FXML
    private TableView<Instrucao> instructionTable;
    @FXML
    private TableColumn<Instrucao, Integer> instructionNumberCol;
    @FXML
    private TableColumn<Instrucao, String> instructionMnemonicCol;
    @FXML
    private TableColumn<Instrucao, String> instructionParameterCol;
    @FXML
    private Stage stage;
    public CodeArea inputTextArea;
    public TextArea messageTextArea;
    public TextArea consoleInput;
    public Label statusBar, lineColLabel;
    // Itens do menu
    public MenuItem saveMenuItem, saveAsMenuItem, exitProgramItem;
    public MenuItem cutMenuItem, copyMenuItem, pasteMenuItem;
    public MenuItem compileMenuItem, runMenuItem;
    // Botões na barra de ferramentas
    public Button newBtn, openBtn, saveBtn;
    public Button copyBtn, cutBtn, pasteBtn;
    public Button buildBtn, runBtn;

    // Método para lidar com a ação de abrir um arquivo
    @FXML
    public void abrir_arquivo_dialogo(ActionEvent actionEvent) {
        actionEvent.consume();
        // Verifica se há um arquivo não salvo antes de abrir outro
        if (arquivo_nao_salvo() != Operation.SUCCESS) {
            return;
        }
        // Configura o seletor de arquivos
        FileChooser filePicker = new FileChooser();
        filePicker.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*." + EditorFile.FILE_EXT));
        filePicker.setInitialDirectory(new File(System.getProperty("user.dir")));
        // Abre um novo arquivo
        editorFile = new EditorFile(filePicker.showOpenDialog(new Stage()), false);
        // Verifica se o arquivo foi aberto com sucesso
        if (!editorFile.isFileStatusOK()) {
            Alert alert = AlertFactory.create
                    (
                            Alert.AlertType.ERROR,
                            "Erro",
                            "Erro",
                            String.format("ERRO ABRIR ARQUIVO: %s", editorFile.getFileStatus())
                    );
            alert.showAndWait();
            return;
        }
        conteudo_txt_area_codigo();
    }
    // Método para criar um novo arquivo
    public void dialogo_novo_arquivo(ActionEvent event) {
        event.consume();
        // Verifica se há um arquivo não salvo antes de criar um novo
        if (arquivo_nao_salvo() != Operation.SUCCESS) {
            return;
        }
        // Limpa a área de texto
        inputTextArea.clear();
        limpar_mensagem();
        hasOpenFile = false;
        this.editorFile.setFile(null);
        atualizar_titulo();
        status_mensagem("");
    }

    // Método para carregar o conteúdo do arquivo na área de texto
    private void conteudo_txt_area_codigo() {
        hasEditedFile = false;
        hasOpenFile = true;
        inputTextArea.setWrapText(false);
        try {
            inputTextArea.replaceText(editorFile.getFileContents());
        } catch (IOException e) {
            e.printStackTrace();
        }
        status_mensagem(String.format("Arquivo lido %s", editorFile.getFilePath().get()));
        atualizar_titulo();
        limpar_mensagem();
    }
    // Método para atualizar o título da janela
    private void atualizar_titulo() {
        String title = "Compilador";
        if (hasOpenFile) {
            title += String.format(" - [%s]", editorFile.getFilePath().get());
        }
        this.stage.setTitle(title);
    }

    @FXML
    // Método para lidar com a ação de salvar um arquivo
    public Operation salvar_arquivo_mensagem(ActionEvent actionEvent) {
        Operation op = Operation.FAILURE;
        actionEvent.consume();
        EditorFile.FileStatus status = editorFile.save(inputTextArea.getText());
        if (status == EditorFile.FileStatus.OK) {
            sucesso_salvar();
            op = Operation.SUCCESS;
        } else {
            AlertFactory.create(Alert.AlertType.ERROR, "Erro", "Falha",
                    String.format("Falha ao salvar arquivo em '%s'", editorFile.getFilePath().get())).show();
        }
        return op;
    }
    // Método para lidar com a ação de salvar como
    @FXML
    private Operation salvar_como_mensagem(ActionEvent actionEvent) {
        actionEvent.consume();
        Operation op = Operation.CANCELED;
        FileChooser filePicker = new FileChooser();
        filePicker.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*." + EditorFile.FILE_EXT));
        filePicker.setInitialDirectory(new File(System.getProperty("user.dir")));
        // Exibe o diálogo de "Salvar como"
        File newFile = filePicker.showSaveDialog(new Stage());
        EditorFile newED = new EditorFile(newFile, false);
        switch (newED.getFileStatus()) {
            case INVALID_EXTENSION -> {
                // Caso a extensão seja inválida, exibe uma mensagem de erro
                AlertFactory.create
                        (
                                Alert.AlertType.ERROR,
                                "Erro",
                                "Extensao invalida",
                                "O nome do arquivo deve usar a extensao '.txt'"
                        ).show();
                op = Operation.FAILURE;
            }
            case IO_ERROR -> {
                // Em caso de erro de leitura/escrita do arquivo, exibe uma mensagem de erro
                AlertFactory.create(
                        Alert.AlertType.ERROR,
                        "Erro",
                        "Erro de leitura/escrita de arquivo",
                        "Erro ao ler/escrever arquivo"
                ).show();
                op = Operation.FAILURE;
            }
            case NO_OPEN_FILE -> {
                // Se não houver arquivo aberto, exibe uma mensagem informativa
                AlertFactory.create
                        (
                                Alert.AlertType.INFORMATION,
                                "Info",
                                "Operacao cancelada",
                                "Salvar em um novo arquivo cancelado"
                        ).show();
                op = Operation.CANCELED;
            }
            case OK -> {
                // Salva o conteúdo na área de texto no novo arquivo
                editorFile.saveAs(inputTextArea.getText(), newFile);
                System.out.println("Salvo com sucesso");
                sucesso_salvar();
                op = Operation.SUCCESS;
            }
        }
        return op;
    }
    // Método para realizar a ação de salvar, que pode ser salvar o arquivo aberto ou salvar como
    public Operation acao_salvar() {
        if (hasOpenFile && hasEditedFile) {
            return salvar_arquivo_mensagem(new ActionEvent());
        }
        if (!hasOpenFile && hasEditedFile) {
            return salvar_como_mensagem(new ActionEvent());
        }
        return Operation.SUCCESS;
    }
    // Método chamado após um salvamento bem-sucedido
    private void sucesso_salvar() {
        hasOpenFile = true;
        hasEditedFile = false;
        status_mensagem("Arquivo salvo");
        atualizar_titulo();
        desativar_salvamento(true);
    }
    // Método para desativar botões de salvamento na interface
    public void desativar_salvamento(boolean b) {
        saveBtn.setDisable(b);
        saveMenuItem.setDisable(b);
    }
    // Método para exibir mensagens de status na barra de status
    public void status_mensagem(String msg) {
        statusBar.setText(String.format("Status: %s", msg));
    }
    // Método chamado quando o conteúdo do arquivo é modificado
    public void conteudo_arquivo_mudanca() {
        desativar_salvamento(false);
        hasEditedFile = true;
    }

    private void registro_linha_coluna() {
        inputTextArea.caretPositionProperty().addListener((observableValue, integer, t1) -> {
            int line = inputTextArea.getCurrentParagraph();
            int col = inputTextArea.getCaretColumn();
            set_linha_coluna_label(line + 1, col + 1);
        });
    }

    private void set_linha_coluna_label(int line, int col) {
        lineColLabel.setText(String.format("Linha/Coluna: %d:%d", line, col));
    }
    // Método para registrar a ação de fechamento da janela
    public void registrar_fechamento_tela() {
        this.stage.setOnCloseRequest(new ExitButtonListener());
    }
    // Método para configurar o estágio (janela) da aplicação
    public void setar_estagio(Stage primaryStage) {
        this.stage = primaryStage;
        inputTextArea.setParagraphGraphicFactory(LineNumberFactory.get(inputTextArea));
        registrar_fechamento_tela();
        registro_linha_coluna();
    }
    // Método para verificar se um arquivo não foi salvo antes de realizar uma ação
    @FXML
    private Operation arquivo_nao_salvo() {
        Operation op = Operation.CANCELED;
        Alert alert;
        if (hasEditedFile) {
            // Exibe um diálogo de confirmação se houver alterações não salvas
            alert = AlertFactory.AlertYesNoCancel(Alert.AlertType.CONFIRMATION,
                    "Confirmacao",
                    "Arquivo nao salvo",
                    "Voce editou um arquivo aberto e nao salvou, deseja salva-lo?"
            );
        } else {
            return Operation.SUCCESS;
        }
        Optional<ButtonType> optional = alert.showAndWait();
        if (optional.isEmpty()) {
            return Operation.CANCELED;
        }
        var buttonData = optional.get().getButtonData();
        if (buttonData.equals(ButtonType.YES.getButtonData()) && !hasOpenFile) {
            return salvar_como_mensagem(new ActionEvent());
        }
        if (buttonData.equals(ButtonType.YES.getButtonData())) {
            return salvar_arquivo_mensagem(new ActionEvent());
        }
        if (buttonData.equals(ButtonType.NO.getButtonData())) {
            return Operation.SUCCESS;
        }
        return op;
    }
    // Método para limpar a área de mensagem
    private void limpar_mensagem() {
        this.messageTextArea.clear();
    }
    // Método para sair do programa
    public void sair_programa(ActionEvent actionEvent) {
        if (arquivo_nao_salvo() == Operation.SUCCESS) {
            Platform.exit();
        } else {
            actionEvent.consume();
        }
    }
    // Classe interna para lidar com a ação de fechamento da janela
    public class ExitButtonListener implements EventHandler<WindowEvent> {
        @Override
        public void handle(WindowEvent windowEvent) {
            if (arquivo_nao_salvo() == Operation.SUCCESS) {
                Platform.exit();
            } else {
                windowEvent.consume();
            }
        }
    }
    // Método para compilar o programa
    public boolean compilar_programa(ActionEvent actionEvent) throws ParseEOFException, ParseException {
        consoleInput.setDisable(true);
        runMenuItem.setDisable(true);
        runBtn.setDisable(true);
        this.listaDeInstrucoes = new ArrayList<>();
        boolean erro = false;
        actionEvent.consume();
        if (inputTextArea.getText().length() == 0) {
            Alert alert = AlertFactory.create
                    (
                            Alert.AlertType.ERROR,
                            "Erro",
                            "Arquivo vazio",
                            "Um arquivo vazio nao pode ser compilado"
                    );
            alert.show();
            return true;
        }
        // Cria um InputStream a partir do texto na área de texto
        String[] args = new String[0];
        java.io.InputStream targetStream = new java.io.ByteArrayInputStream(inputTextArea.getText().getBytes());
        Linguagem linguagem = new Linguagem(targetStream);
        linguagem.disable_tracing();

        String erros_lexicos = linguagem.getTokens(args, inputTextArea.getText());
        if (linguagem.contErroLexico == 0){
            try {
                System.out.println("###########################" + linguagem.acaoSemantica.getInstrucao().size());
                linguagem.acaoSemantica = new Acoes();
                // Tenta analisar a sintaxe do programa
                String erros_sintaticos = linguagem.analisarSintatica(args, inputTextArea.getText());
                if (erros_sintaticos.isEmpty()) {
                    //messageTextArea.setText("Sintático compilado com sucesso!");
                    List<String> errosSemanticosAcoes = linguagem.acaoSemantica.getListaErros();
                    if (errosSemanticosAcoes.size() > 0) {
                        StringBuilder errosSemanticosStr = new StringBuilder("");
                        errosSemanticosStr.append(linguagem.errorListToString("Erros semanticos encontrados", errosSemanticosAcoes));
                        System.out.println(errosSemanticosStr.toString());
                        messageTextArea.setText(errosSemanticosStr.toString());
                        displayInstructions(new ArrayList<Instrucao>());

                        erro = true;
                    } else {
                        this.listaDeInstrucoes = linguagem.acaoSemantica.getInstrucao();

                        System.out.println("tamanho da lista: " + listaDeInstrucoes.size());
                        System.out.println("ENTROU NA PARTE DE MOSTRAR AS INSTRUÇÕES");
                        displayInstructions(listaDeInstrucoes);
                        messageTextArea.setText("Compilado com sucesso!");
                    }
                } else {
                    displayInstructions(new ArrayList<Instrucao>());
                    erros_sintaticos = erros_sintaticos.replace('<', '\"');
                    erros_sintaticos = erros_sintaticos.replace('>', '\"');
                    messageTextArea.setText(erros_sintaticos);
                    erro = true;
                }
                //linguagem.begin_program();
            } catch (ParseEOFException e) {
                displayInstructions(new ArrayList<Instrucao>());
                System.err.println(e.getMessage());
            } finally {
                System.out.println("Erros sintaticos: " + linguagem.contParserError);
            }
        }else{
            erro = true;
            messageTextArea.setText(erros_lexicos);
        }

        consoleInput.setDisable(erro);
        runMenuItem.setDisable(erro);
        runBtn.setDisable(erro);

        return erro;
    }
    // Método para executar o programa, lidando com a máquina virtual e sua execução
    public void rodar(ActionEvent actionEvent) throws ParseException, ParseEOFException {
        if (aguardarMaquinaVirtual() == Operation.SUCCESS) { // Verifica se a máquina virtual pode ser iniciada
            if (!compilar_programa(actionEvent)) {// Compila o programa se não houver erros
                this.vm = new MaquinaVirtual(this.listaDeInstrucoes);// Inicializa uma nova instância da máquina virtual com as instruções compiladas
                this.isReadingConsole = false;
                rodarMaquinaVirtual();// Executa a máquina virtual
            }
        }
    }

    public Operation aguardarMaquinaVirtual() {// Método para aguardar a finalização da máquina virtual antes de continuar com a operação
        if (vm == null || vm.getStatus() == Status.ENCERRADO) { // Verifica se a máquina virtual não foi iniciada ou já está encerrada
            return Operation.SUCCESS;
        }
        var confirm = AlertFactory.create( // Cria um diálogo de confirmação para verificar se o usuário deseja interromper a operação
                Alert.AlertType.CONFIRMATION,
                "Confirmação", "",
                "Máquina virtual ainda está rodando, você deseja para a operação?"
        );
        Optional<ButtonType> optional = Optional.empty();
        var op = Operation.CANCELED;
        if (
                vm.getStatus() == Status.RODANDO // Verifica se a máquina virtual está em um estado em que a interrupção é possível
                        || vm.getStatus() == Status.LER
                        || vm.getStatus() == Status.ESCREVER)
        {
            optional = confirm.showAndWait(); // Exibe o diálogo de confirmação
        }
        if (optional.isEmpty()) { // Verifica a escolha do usuário no diálogo de confirmação
            return Operation.CANCELED;
        }
        var buttonData = optional.get().getButtonData();
        if (buttonData.equals(ButtonType.OK.getButtonData())) {  // Se o usuário escolheu "OK", encerra forçadamente a máquina virtual
            vm = null;
            this.messageTextArea.clear();
            status_mensagem("VM fechada de forma forçada");
            return Operation.SUCCESS; // Retorna sucesso após o encerramento forçado
        }
        if (buttonData.equals(ButtonType.CANCEL.getButtonData())) {
            return Operation.CANCELED; // Retorna cancelado se o usuário escolheu "Cancelar"
        }
        return op; // Retorna a operação padrão (CANCELADO)
    }

    public void rodarMaquinaVirtual() { // Método para executar a máquina virtual em um loop até que o programa seja encerrado

        try {
            while (vm.getStatus() != Status.ENCERRADO) {        // Continua a execução enquanto o status da máquina virtual não for "ENCERRADO"
                if (isReadingConsole) {            // Verifica se a leitura do console está em andamento
                    return;
                }
                statusBar.setText("Executando maquina virtual");             // Atualiza a barra de status indicando a execução da máquina virtual
                System.out.println("Executando maquina virtual");
                vm.execucao();             // Executa todas as instruções disponíveis na máquina virtual
                switch (vm.getStatus()) {            // Avalia o estado atual da máquina virtual para possíveis operações de E/S
                    case LER -> {
                        handleSyscallRead(vm.getTipoSyscallData());  // Trata a chamada de sistema de leitura de E/S
                    }
                    case ESCREVER -> handleSyscallWrite(vm.getSyscallData());
                }
            }
            statusBar.setText("Maquina virtual terminada, programa encerrado");        // Atualiza a barra de status indicando que a máquina virtual foi encerrada
        } catch (Exception e) {
            statusBar.setText("Erro em tempo de execução da VM, finalizando VM!");// Trata exceções durante a execução da máquina virtual
            this.messageTextArea.appendText(String.format("\n== ERROR - VM ==\n%s", e.getMessage()));
            e.printStackTrace();
            this.vm.setStatus(Status.ENCERRADO); // Define o status da máquina virtual como "ENCERRADO" e desabilita a leitura do console
            this.isReadingConsole = false;
            this.consoleInput.setDisable(true);
        }
    }

    private void handleSyscallWrite(Object o) { // Método para lidar com a chamada de sistema de escrita (syscall write)
        messageTextArea.appendText("\n" + o.toString());     // Adiciona o resultado da chamada de sistema à área de texto de mensagens
    }

    private void handleSyscallRead(Tipo o) {// Método para lidar com a chamada de sistema de leitura (syscall read)
        consoleInput.setDisable(false); // Habilita a entrada do console e sinaliza que a leitura do console está em andamento
        isReadingConsole = true;
        statusBar.setText("Aguardando entrada de " + o.toString()); // Atualiza a barra de status indicando que a máquina virtual está aguardando entrada do tipo especificado
        consoleInput.setOnKeyReleased(event -> { // Configura um evento de tecla liberada no console
            if (event.getCode().equals(KeyCode.ENTER)) { // Verifica se a tecla pressionada é ENTER
                vm.setSyscallData(consoleInput.getText().trim());// Define os dados da chamada de sistema com o conteúdo do console
                messageTextArea.appendText("\n--> " + consoleInput.getText().trim());// Adiciona o conteúdo do console à área de texto de mensagens
                consoleInput.setDisable(true);// Desabilita o console, limpa seu conteúdo e sinaliza o fim da leitura do console
                consoleInput.clear();
                isReadingConsole = false;
                rodarMaquinaVirtual();// Inicia novamente a execução da máquina virtual
            }
        });
    }

    private ObservableList<Instrucao> getObservableListOf(List<Instrucao> instructionList) {
        return FXCollections.observableArrayList(instructionList);
    }

    private void displayInstructions(List<Instrucao> instructions) {
        instructionNumberCol.setCellValueFactory(new PropertyValueFactory<>("numero"));
        instructionMnemonicCol.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        instructionParameterCol.setCellValueFactory(new PropertyValueFactory<>("parametro"));
        instructionTable.setItems(getObservableListOf(instructions));
    }

    // Método para copiar uma seleção de texto
    public String copiar_selecao() {
        String selection = inputTextArea.getSelectedText();
        inputTextArea.copy();
        return selection;
    }

    // Método para cortar uma seleção de texto
    public String cortar_selecao() {
        String selection = inputTextArea.getSelectedText();
        inputTextArea.cut();
        return selection;
    }
    // Método para colar texto da área de transferência
    public void colar() {
        inputTextArea.paste();
    }
}
