package classes;

import java.util.*;

public class MaquinaVirtual {

    private final List<Instrucao> instrucoes;
    private final Stack<Parametro> pilha = new Stack<>();
    private int ponteiro = 0;
    private Status status = Status.NAO_INICIADO;
    private Tipo chamadaDadosTipo = null;
    private Object chamadaDados = null;

    public MaquinaVirtual(List<Instrucao> instrucoes) {
        this.instrucoes = instrucoes;
    }

    public Stack<Parametro> getPilha() {
        return pilha;
    }

    public Status getStatus() {
        return status;
    }

    public List<Instrucao> getInstrucoes() {
        return instrucoes;
    }

    public int getInstrucaoPonteiro() {
        return ponteiro;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String printarPilha() { //string  pilha de operandos.
        int pilhaPos = 0;
        StringBuilder sb = new StringBuilder("-- BOTTOM --\n");
        for (Parametro se : pilha) {
            sb.append(pilhaPos).append(" - ").append(se.toDebugString()).append("\n");
            pilhaPos++;
        }
        sb.append("-- STACK TOP --");
        return sb.toString();
    }
    /**
     * Retoma a execução da máquina virtual, processando dados de entrada após uma operação de leitura.
     * Este método é chamado quando o estado da máquina virtual é Status.LER.
     *
     * Se o tipo de dados esperado para a leitura for INTEGER ou FLOAT, a entrada é convertida
     * e empilhada na pilha de operandos.
     *
     * @throws RuntimeException Se a entrada fornecida não puder ser interpretada como o tipo esperado.
     */
    public void retomaExecucao() {
        if (status == Status.LER) {
            chamadaDados = chamadaDados.toString().trim(); // Remove espaços em branco da entrada
            try {
                switch (chamadaDadosTipo) {
                    case INTEGER -> chamadaDados = Integer.parseInt((String) chamadaDados);
                    case FLOAT -> chamadaDados = Float.parseFloat((String) chamadaDados);
                }
                pilha.push(new Parametro(chamadaDadosTipo, chamadaDados)); // Empilha o dado convertido
                System.out.println(printarPilha()); // Empilha o dado convertido
            } catch (NumberFormatException e) {
                // Lança uma exceção se a entrada não puder ser convertida para o tipo esperado
                throw new RuntimeException(String.format("Leitura de entrada invalida, nao é possivel interpretar %s as %s\n", chamadaDados.toString(), this.chamadaDadosTipo.toString()));
            }
        }
        // Limpa os dados de leitura após o processamento
        this.chamadaDados = null;
    }

    public Tipo getTipoSyscallData() { //tipo de dados associado à última chamada de sistema
        return chamadaDadosTipo;
    }

    public void setTipoSyscallData(Tipo chamadaDadosTipo) {
        this.chamadaDadosTipo = chamadaDadosTipo;
    }

    public Object getSyscallData() {
        return chamadaDados;
    }

    public void setSyscallData(Object chamadaDados) {
        this.chamadaDados = chamadaDados;
    }

    public void execucao() { //Executa a máquina virtual até que a execução seja concluída ou pausada
        while (this.status != Status.ENCERRADO) {

            if (status == Status.LER || status == Status.ESCREVER) {
                retomaExecucao();
            }
            executarSteps();

            if (status == Status.LER || status == Status.ESCREVER) {
                break;
            }
        }
    }

    public void executarSteps() {
        status = Status.RODANDO;
        Instrucao ins = instrucoes.get(ponteiro);
        System.out.printf("Instruction Pointer: %d\n", ponteiro+1);
        System.out.println(ins);
        switch (ins.codigo) {
            case ADD -> add(ins);
            case DIV -> divide(ins);
            case MUL -> multiply(ins);
            case SUB -> subtract(ins);
            case DVW -> divideWhole(ins);
            case MOD -> modulo(ins);
            case PWR -> potentiation(ins);
            case ALB -> allocateBoolean(ins);
            case ALI -> allocateInteger(ins);
            case ALR -> allocateFloat(ins);
            case ALS -> allocateLiteralValue(ins);
            case LDB -> loadBoolean(ins);
            case LDI -> loadInteger(ins);
            case LDR -> loadFloat(ins);
            case LDS -> loadLiteral(ins);
            case LDV -> loadValueAt(ins);
            case STR -> storeValueAt(ins);
            case AND -> logicalAnd(ins);
            case NOT -> logicalNOT(ins);
            case OR  -> logicalOr(ins);
            case BGE -> relationalGreaterOrEquals(ins);
            case BGR -> relationalGreater(ins);
            case DIF -> relationalDifferent(ins);
            case EQL -> relationalEquals(ins);
            case SME -> relationalLessOrEquals(ins);
            case SMR -> relationalLess(ins);
            case JMF -> jumpFalseToAddress(ins);
            case JMP -> jumpToAddress(ins);
            case JMT -> jumpTrueToAddress(ins);
            case STP -> {
                this.status = Status.ENCERRADO;
                return;
            }
            case REA -> read(ins);
            case WRT -> write(ins);
            case STC -> copiaPosicaoPilha(ins);
        }
        if (this.status != Status.LER) {
            System.out.println(this.printarPilha());
        }
        ponteiro++;

    }


    private void add(Instrucao ins) {
        // Retira os dois parâmetros do topo da pilha
        Parametro x = pilha.pop();
        Parametro y = pilha.pop();
        var tipo = avaliaTipo(Tipo.getTipos(), ins, x, y); // Verifica os tipos dos parâmetros
        if (tipo == Tipo.INTEGER) { // Se ambos são inteiros, realiza a adição de inteiros
            var x_val = (Integer) x.conteudo;
            var y_val = (Integer) y.conteudo;
            x_val = x_val + y_val;
            pilha.push(new Parametro(tipo, x_val));
        } else { // Se pelo menos um deles é ponto flutuante, realiza a adição de ponto flutuante
            var x_val = ((Number) x.conteudo).floatValue();
            var y_val = ((Number) y.conteudo).floatValue();
            float result = x_val + y_val;
            pilha.push(new Parametro(tipo, result));
        }
    }

    private void divide(Instrucao ins) {
        Parametro x = pilha.pop();
        Parametro y = pilha.pop();
        // exceção para divisão por zero
        var divideByZeroEx = new RuntimeException(String.format("Divisao por zero na instrucao %s\n ->> top: %s\n --> subTop: %s", ins, x, y));
        var tipo = avaliaTipo(Tipo.getTipos(), ins, x, y);
        if (tipo == Tipo.INTEGER) {
            var x_val = (Integer) x.conteudo;
            var y_val = (Integer) y.conteudo;
            if (x_val.equals(0)) {
                throw divideByZeroEx;
            }
            x_val = y_val / x_val;
            pilha.push(new Parametro(tipo, x_val));
        } else {
            var x_val = (Float)((Number) x.conteudo).floatValue();
            var y_val = (Float)((Number) y.conteudo).floatValue();
            if (x_val.equals(0f)) { // Verifica divisão por zero
                throw divideByZeroEx;
            }
            float result = y_val / x_val;
            pilha.push(new Parametro(tipo, result));
        }
    }

    private void multiply(Instrucao ins) {
        Parametro x = pilha.pop();
        Parametro y = pilha.pop();
        var tipo = avaliaTipo(Tipo.getTipos(), ins, x, y);
        if (tipo == Tipo.INTEGER) {
            var x_val = (Integer) x.conteudo;
            var y_val = (Integer) y.conteudo;
            x_val = x_val * y_val;
            pilha.push(new Parametro(tipo, x_val));
        } else {
            var x_val = ((Number) x.conteudo).floatValue();
            var y_val = ((Number) y.conteudo).floatValue();
            float result = x_val * y_val;
            pilha.push(new Parametro(tipo, result));
        }
    }

    private void subtract(Instrucao ins) {
        Parametro x = pilha.pop();
        Parametro y = pilha.pop();
        var tipo = avaliaTipo(Tipo.getTipos(), ins, x, y);
        if (tipo == Tipo.INTEGER) {
            var x_val = (Integer) x.conteudo;
            var y_val = (Integer) y.conteudo;
            x_val = y_val - x_val;
            pilha.push(new Parametro(tipo, x_val));
        } else {
            var x_val = ((Number) x.conteudo).floatValue();
            var y_val = ((Number) y.conteudo).floatValue();
            float result = y_val - x_val;
            pilha.push(new Parametro(tipo, result));
        }
    }

    private void divideWhole(Instrucao ins) {
        var x = pilha.pop();
        var y = pilha.pop();
        var tipo = avaliaTipo(Tipo.getTipos(), ins, x, y);
        if (tipo == Tipo.INTEGER) {
            var x_val = (Integer) x.conteudo;
            var y_val = (Integer) y.conteudo;
            x_val = y_val / x_val;
            pilha.push(new Parametro(tipo, x_val));
        } else {
            var x_val = ((Number) x.conteudo).floatValue();
            var y_val = ((Number) y.conteudo).floatValue();
            float result = y_val / x_val;
            pilha.push(new Parametro(Tipo.INTEGER, (int) result));
        }
    }

    private void modulo(Instrucao ins) {
        var x = pilha.pop();
        var y = pilha.pop();
        var tipo = avaliaTipo(Tipo.getTipos(), ins, x, y);
        if (tipo == Tipo.INTEGER) {
            var x_val = (Integer) x.conteudo;
            var y_val = (Integer) y.conteudo;
            x_val = y_val % x_val;
            pilha.push(new Parametro(tipo, x_val));
        } else {
            var x_val = ((Number) x.conteudo).floatValue();
            var y_val = ((Number) y.conteudo).floatValue();
            float result = y_val % x_val;
            pilha.push(new Parametro(tipo, result));
        }
    }

    private void potentiation(Instrucao ins) {
        var x = pilha.pop();
        var y = pilha.pop();
        var tipo = avaliaTipo(Tipo.getTipos(), ins, x, y);
        if (tipo == Tipo.INTEGER) {
            var x_val = (Integer) x.conteudo;
            var y_val = (Integer) y.conteudo;
            x_val = (int) Math.pow(y_val, x_val);
            pilha.push(new Parametro(tipo, x_val));
        } else {
            var x_val = ((Number) x.conteudo).floatValue();
            var y_val = ((Number) y.conteudo).floatValue();
            float result = (float) Math.pow(y_val,x_val);
            pilha.push(new Parametro(tipo, result));
        }
    }

    private void allocateBoolean(Instrucao ins) {
        if (ins.parametro.tipo != Tipo.INTEGER) {
            invalidInstructionParameter(Collections.singletonList(Tipo.INTEGER), ins.parametro.tipo);
        }
        for (int i = 0; i < (Integer) ins.parametro.conteudo; i++) {
            pilha.push(new Parametro(Tipo.BOOLEAN, false));
        }
    }

    private void allocateInteger(Instrucao ins) {
        if (ins.parametro.tipo != Tipo.INTEGER) {
            invalidInstructionParameter(Collections.singletonList(Tipo.INTEGER), ins.parametro.tipo);
        }
        for (int i = 0; i < (Integer) ins.parametro.conteudo; i++) {
            pilha.push(new Parametro(Tipo.INTEGER, 0));
        }
    }

    private void allocateFloat(Instrucao ins) {
        if (ins.parametro.tipo != Tipo.INTEGER) {
            invalidInstructionParameter(Collections.singletonList(Tipo.INTEGER), ins.parametro.tipo);
        }
        for (int i = 0; i < (Integer) ins.parametro.conteudo; i++) {
            pilha.push(new Parametro(Tipo.FLOAT, 0f));
        }
    }

    private void allocateLiteralValue(Instrucao ins) {
        if (ins.parametro.tipo != Tipo.INTEGER) {
            invalidInstructionParameter(Collections.singletonList(Tipo.INTEGER), ins.parametro.tipo);
        }
        for (int i = 0; i < (Integer) ins.parametro.conteudo; i++) {
            pilha.push(new Parametro(Tipo.LITERAL, ""));
        }
    }

    private void loadBoolean(Instrucao ins) {
        if (ins.parametro.tipo != Tipo.BOOLEAN) {
            invalidInstructionParameter(Collections.singletonList(Tipo.BOOLEAN), ins.parametro.tipo);
        }
        var conteudo = (Boolean) ins.parametro.conteudo;
        pilha.push(new Parametro(Tipo.BOOLEAN, conteudo));
    }

    private void loadInteger(Instrucao ins) {
        if (ins.parametro.tipo != Tipo.INTEGER) {
            invalidInstructionParameter(Collections.singletonList(Tipo.INTEGER), ins.parametro.tipo);
        }
        var conteudo = (Integer) ins.parametro.conteudo;
        pilha.push(new Parametro(Tipo.INTEGER, conteudo));
    }

    private void loadFloat(Instrucao ins) {
        if (ins.parametro.tipo != Tipo.FLOAT) {
            invalidInstructionParameter(Collections.singletonList(Tipo.FLOAT), ins.parametro.tipo);
        }
        var conteudo = (Float) ins.parametro.conteudo;
        pilha.push(new Parametro(Tipo.FLOAT, conteudo));
    }

    private void loadLiteral(Instrucao ins) {

        var conteudo = (String) ins.parametro.conteudo;
        pilha.push(new Parametro(Tipo.LITERAL, conteudo));
    }


    private void storeValueAt(Instrucao ins) {
        if (ins.parametro.tipo != Tipo.ADDRESS) {
            invalidInstructionParameter(Collections.singletonList(Tipo.ADDRESS), ins.parametro.tipo);
        }
        Parametro p = pilha.lastElement();
        Integer indice = (Integer) ins.parametro.conteudo - 1;
        pilha.set(indice, p);

    }

    private void loadValueAt(Instrucao ins) {
        if (ins.parametro.tipo != Tipo.ADDRESS) {
            invalidInstructionParameter(Collections.singletonList(Tipo.ADDRESS), ins.parametro.tipo);
        }
        var stackElement = pilha.get((Integer) ins.parametro.conteudo - 1);
        pilha.push(new Parametro(stackElement.tipo, stackElement.conteudo));
    }

    private void logicalAnd(Instrucao ins) {
        Parametro x = pilha.pop();
        Parametro y = pilha.pop();
        var tipo = avaliaTipo(Collections.singletonList(Tipo.BOOLEAN), ins, x, y);
        var x_val = (Boolean) x.conteudo;
        var y_val = (Boolean) y.conteudo;
        x_val = x_val & y_val;
        pilha.push(new Parametro(tipo, x_val));
    }

    private void logicalNOT(Instrucao ins) {
        Parametro x = pilha.pop();
        var tipo = avaliaTipo(Collections.singletonList(Tipo.BOOLEAN), ins, x, x);
        var x_val = !(Boolean) x.conteudo;
        pilha.push(new Parametro(tipo, x_val));
    }

    private void logicalOr(Instrucao ins) {
        Parametro x = pilha.pop();
        Parametro y = pilha.pop();
        var tipo = avaliaTipo(Collections.singletonList(Tipo.BOOLEAN), ins, x, y);
        var x_val = (Boolean) x.conteudo;
        var y_val = (Boolean) y.conteudo;
        x_val = x_val || y_val;
        pilha.push(new Parametro(tipo, x_val));
    }

    private void relationalGreaterOrEquals(Instrucao ins) {
        Parametro x = pilha.pop();
        Parametro y = pilha.pop();
        var tipo = avaliaTipo(Tipo.getTipos(), ins, x, y);
        if (tipo == Tipo.INTEGER) {
            var x_val = (Integer) x.conteudo;
            var y_val = (Integer) y.conteudo;
            var flag = y_val >= x_val;
            pilha.push(new Parametro(Tipo.BOOLEAN, flag));
        } else {
            var x_val = (Float) x.conteudo;
            var y_val = (Float) y.conteudo;
            var flag = y_val >= x_val;
            pilha.push(new Parametro(Tipo.BOOLEAN, flag));
        }
    }

    private void relationalGreater(Instrucao ins) {
        Parametro x = pilha.pop();
        Parametro y = pilha.pop();
        var tipo = avaliaTipo(Tipo.getTipos(), ins, x, y);
        if (tipo == Tipo.INTEGER) {
            var x_val = (Integer) x.conteudo;
            var y_val = (Integer) y.conteudo;
            var flag = y_val > x_val;
            pilha.push(new Parametro(Tipo.BOOLEAN, flag));
        } else {
            var x_val = ((Number) x.conteudo).floatValue();
            var y_val = ((Number) y.conteudo).floatValue();
            Boolean result = y_val > x_val;
            pilha.push(new Parametro(Tipo.BOOLEAN, result));
        }
    }

    private void relationalDifferent(Instrucao ins) {
        Parametro x = pilha.pop();
        Parametro y = pilha.pop();
        var tipo = avaliaTipo(Tipo.getTipos(), ins, x, y);
        var result = false;
        if (tipo == Tipo.INTEGER) {
            var x_val = (Integer) x.conteudo;
            var y_val = (Integer) y.conteudo;
            result = !y_val.equals(x_val);
        } else {
            var x_val = ((Number) x.conteudo).floatValue();
            var y_val = ((Number) y.conteudo).floatValue();
            result = y_val != x_val;
        }
        pilha.push(new Parametro(Tipo.BOOLEAN, result));
    }

    private void relationalEquals(Instrucao ins) {
        Parametro x = pilha.pop();
        Parametro y = pilha.pop();
        var tipo = avaliaTipo(Tipo.getTipos(), ins, x, y);
        var result = false;
        if (tipo == Tipo.INTEGER) {
            var x_val = (Integer) x.conteudo;
            var y_val = (Integer) y.conteudo;
            result = y_val.equals(x_val);
        } else {
            var x_val = ((Number) x.conteudo).floatValue();
            var y_val = ((Number) y.conteudo).floatValue();
            result = y_val == x_val;
        }
        pilha.push(new Parametro(Tipo.BOOLEAN, result));
    }

    private void relationalLessOrEquals(Instrucao ins) {
        Parametro top = pilha.pop();
        Parametro sub = pilha.pop();
        var tipo = avaliaTipo(Tipo.getTipos(), ins, top, sub);
        var result = false;
        if (tipo == Tipo.INTEGER) {
            var top_val = (Integer) top.conteudo;
            var sub_val = (Integer) sub.conteudo;
            result = sub_val <= top_val;
        } else {
            var top_val = (Float) top.conteudo;
            var sub_val = (Float) sub.conteudo;
            result = sub_val <= top_val;
        }
        pilha.push(new Parametro(Tipo.BOOLEAN, result));
    }

    private void relationalLess(Instrucao ins) {
        Parametro top = pilha.pop();
        Parametro sub = pilha.pop();
        var tipo = avaliaTipo(Tipo.getTipos(), ins, top, sub);
        var result = false;
        if (tipo == Tipo.INTEGER) {
            var top_val = (Integer) top.conteudo;
            var sub_val = (Integer) sub.conteudo;
            result = sub_val < top_val;
        } else {
            Float top_val, sub_val;
            if (top.conteudo instanceof Integer) {
                top_val = ((Integer) top.conteudo).floatValue();
            } else if (top.conteudo instanceof Float) {
                top_val = (Float) top.conteudo;
            } else {
                throw new IllegalArgumentException("Unsupported type: " + top.conteudo.getClass());
            }
            if (sub.conteudo instanceof Integer) {
                sub_val = ((Integer) sub.conteudo).floatValue();
            } else if (top.conteudo instanceof Float) {
                sub_val = (Float) sub.conteudo;
            } else {
                throw new IllegalArgumentException("Unsupported type: " + top.conteudo.getClass());
            }
            result = sub_val < top_val;
        }
        pilha.push(new Parametro(Tipo.BOOLEAN, result));
    }


    private void jumpFalseToAddress(Instrucao ins) {
        avaliaTipo(Collections.singletonList(Tipo.ADDRESS), ins, ins.parametro, ins.parametro);
        var top = pilha.pop();
        avaliaTipo(Collections.singletonList(Tipo.BOOLEAN), ins, top, top);
        if (!(Boolean) top.conteudo) {
            ponteiro = (Integer) ins.parametro.conteudo;
            ponteiro -= 2; // We always add +1 after each instruction, this will revert that
        }
    }

    private void jumpToAddress(Instrucao ins) {
        avaliaTipo(Collections.singletonList(Tipo.ADDRESS), ins, ins.parametro, ins.parametro);
        ponteiro = (Integer) ins.parametro.conteudo;
        ponteiro -= 2; // We always add +1 after each instruction, this will revert that
    }

    private void jumpTrueToAddress(Instrucao ins) {
        avaliaTipo(Collections.singletonList(Tipo.ADDRESS), ins, ins.parametro, ins.parametro);
        var top = pilha.pop();
        avaliaTipo(Collections.singletonList(Tipo.BOOLEAN), ins, top, top);
        if ((Boolean) top.conteudo) {
            ponteiro = (Integer) ins.parametro.conteudo;
            ponteiro--; // We always add +1 after each instruction, this will revert that
        }
    }

    private void read(Instrucao ins) {
        var acceptedTypes = Tipo.getTipos();
        acceptedTypes.add(Tipo.LITERAL);
        if (!acceptedTypes.contains(ins.parametro.tipo)) {
            invalidInstructionParameter(acceptedTypes, ins.parametro.tipo);
        }
        this.status = Status.LER;
        this.chamadaDadosTipo = ins.parametro.tipo;
    }

    private void write(Instrucao ins) {
        var stackElement = pilha.pop();
        avaliaTipo(Arrays.asList(Tipo.INTEGER, Tipo.FLOAT, Tipo.LITERAL),
                ins, stackElement, stackElement);
        this.status = Status.ESCREVER;
        this.chamadaDadosTipo = stackElement.tipo;
        this.chamadaDados = stackElement.conteudo;
    }

    private void copiaPosicaoPilha(Instrucao ins) {
        if (ins.parametro.tipo != Tipo.INTEGER) {
            invalidInstructionParameter(Collections.singletonList(Tipo.INTEGER), ins.parametro.tipo);
        }
        var numberPositions = (Integer) ins.parametro.conteudo;
        var stackElement = pilha.pop();
        for (int i=pilha.size()-numberPositions; i<=pilha.size()-1; i++) {
            pilha.set(i, stackElement);
        }
    }

    /**
     * Avalia o tipo resultante da operação com base nos tipos dos parâmetros e na instrução especificada.
     *
     * @param compatibleTypes Lista de tipos compatíveis.
     * @param ins Instrução que contém informações sobre a operação.
     * @param x Parâmetro do topo da pilha.
     * @param y Parâmetro sub-topo da pilha.
     * @return Tipo resultante da operação.
     * @throws RuntimeException Se os tipos dos parâmetros forem incompatíveis.
     */
    private static Tipo avaliaTipo(List<Tipo> compatibleTypes, Instrucao ins, Parametro x, Parametro y) {
        // Cria uma exceção de runtime para tipos incompatíveis
        var runtimeException = new RuntimeException(String.format("Tipos de pilha de dados incompativeis para instrucao %s!\n --> top: %s\n --> subTop: %s", ins, x.toDebugString(), y.toDebugString()));
        // Tipo efetivo resultante da operação
        Tipo effectiveOutputDataTypeK = null;
        // Flag indicando se os tipos são incompatíveis
        boolean compatibleTypesFlag = !(compatibleTypes.contains(x.tipo)) && !(compatibleTypes.contains(y.tipo));
        if (!compatibleTypesFlag) { // Verifica se os tipos são incompatíveis
            switch (x.tipo) {
                // ADD, MUL, SUB, DIV
                case FLOAT -> {
                    switch (y.tipo) {
                        case FLOAT, INTEGER -> effectiveOutputDataTypeK = Tipo.FLOAT;
                    }
                }
                // ADD, MUL, SUB, DIV
                case INTEGER -> {
                    switch (y.tipo) {
                        case FLOAT -> effectiveOutputDataTypeK = Tipo.FLOAT;
                        case INTEGER -> effectiveOutputDataTypeK = Tipo.INTEGER;
                    }
                }
                // LDB
                case BOOLEAN -> {
                    // Verifica se o tipo de y é BOOLEAN
                    if (y.tipo == Tipo.BOOLEAN) {
                        effectiveOutputDataTypeK = Tipo.BOOLEAN;
                    }
                }
                // JMP
                case ADDRESS -> {
                    // Verifica se o tipo de y é ADDRESS
                    if (y.tipo == Tipo.ADDRESS) {
                        effectiveOutputDataTypeK = Tipo.ADDRESS;
                    }
                }
                case LITERAL -> {
                    // Verifica se o tipo de y é LITERAL
                    if (y.tipo == Tipo.LITERAL) {
                        effectiveOutputDataTypeK = Tipo.LITERAL;
                    }
                }
            }
        } else { // Lança exceção se os tipos são incompatíveis
            throw runtimeException;
        }
        if (effectiveOutputDataTypeK == null) { // Se não houve correspondência de tipos, lança exceção
            throw runtimeException;
        }
        return effectiveOutputDataTypeK; // Retorna o tipo efetivo resultante da operação
    }

    private static void invalidInstructionParameter(List<Tipo> expected, Tipo got) {
        throw new RuntimeException(String.format("Instrucao invalida, parametro %s esperado, recebido: %s", expected, got));
    }



}
