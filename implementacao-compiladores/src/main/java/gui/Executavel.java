package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe que define a aplicação JavaFX executável.
 */
public class Executavel extends Application {
    // Método principal que inicia a aplicação JavaFX.
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cria um objeto FXMLLoader para carregar a interface gráfica definida no arquivo "main.fxml".
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
        // Carrega a interface gráfica como um objeto Parent.
        Parent root = fxmlLoader.load();
        // Define o título da janela principal.
        primaryStage.setTitle("Compilador");
        // Cria uma cena (Scene) com o conteúdo da interface gráfica.
        primaryStage.setScene(new Scene(root));
        // Obtém uma referência para o controlador da interface gráfica.
        Controller controller = fxmlLoader.getController();
        // Configura o estágio (janela) da aplicação no controlador.
        controller.setar_estagio(primaryStage);
        // Exibe a janela principal.
        primaryStage.show();
    }
}