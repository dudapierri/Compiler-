package gui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class EditorFile {
    // Definição da extensão padrão dos arquivos
    public static final String FILE_EXT = "txt";
    // Representação do arquivo
    private File file = null;
    // Construtor padrão da classe EditorFile
    public EditorFile(){};
    // Construtor da classe EditorFile com a opção de criar um novo arquivo
    public EditorFile(File file, boolean isNewFile) {
        // Se isNewFile for verdadeiro, tenta criar um novo arquivo no sistema
        if (isNewFile) {
            try {
                FileUtils.touch(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.file = file;
    }
    // Getter para obter o arquivo
    public File getFile() {
        return file;
    }
    // Setter para definir o arquivo
    public void setFile(File file) {
        this.file = file;
    }
    // Método para obter o conteúdo do arquivo
    public String getFileContents() throws IOException {
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    // Método para obter o caminho do arquivo (path absoluto)
    public Optional<String> getFilePath() {
        if (getFileStatus() == FileStatus.OK) {
            return Optional.of(file.getAbsolutePath());
        }
        return Optional.empty();
    }
    // Método para salvar o conteúdo no arquivo
    public FileStatus save(String contents) {
        return saveAs(contents, this.file);
    }
    // Método para salvar o conteúdo em um arquivo específico
    public FileStatus saveAs(String contents, File target) {
        try {
            FileUtils.write(target, contents, StandardCharsets.UTF_8);
            this.file = target;
        } catch (IOException e) {
            e.printStackTrace();
            return FileStatus.IO_ERROR;
        }
        return FileStatus.OK;
    }
    // Método para verificar se o arquivo possui a extensão válida
    public boolean hasValidExtension() {
        if (this.file == null) {
            return false;
        }
        boolean hasValidExt = FilenameUtils.isExtension(file.getName(), FILE_EXT);
        if (hasValidExt) {
            return true;
        }
        System.err.printf("File doesn't have a valid extension, want %s, have %s\n", FILE_EXT, FilenameUtils.getExtension(file.getName()));
        return false;
    }
    // Método para obter o status do arquivo
    public FileStatus getFileStatus() {
        if (file == null) {
            return FileStatus.NO_OPEN_FILE;
        }
        if (!hasValidExtension()) {
            return FileStatus.INVALID_EXTENSION;
        }
        return FileStatus.OK;
    }
    // Método para verificar se o status do arquivo é OK
    public boolean isFileStatusOK() {
        return this.getFileStatus() == FileStatus.OK;
    }
    // Enumeração que representa o status do arquivo
    public enum FileStatus {
        OK, // Arquivo OK
        IO_ERROR, // Erro de entrada/saída
        INVALID_EXTENSION, // Extensão inválida
        NO_OPEN_FILE, // Nenhum arquivo aberto
    }
}
