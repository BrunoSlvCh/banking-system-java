import java.util.Scanner;
import java.math.BigDecimal;

public class Main {

    public static void menuInicial(){
        System.out.println("\nSelecione uma opção: ");
        System.out.println("1 - Entrar");
        System.out.println("2 - Criar conta");
        System.out.println("3 - Sair");
        System.out.print(">> ");
    }

    public static void menuDeAcoes(ContaBancaria conta) {
        System.out.println("\n====================\n");
        System.out.println(conta.getNome() + ", selecione uma opção: " );
        System.out.println("1 - Ver saldo");
        System.out.println("2 - Depositar");
        System.out.println("3 - Sacar");
        System.out.println("4 - Transferir");
        System.out.println("5 - Ver histórico");
        System.out.println("6 - Mudar Nome");
        System.out.println("7 - Mudar Senha");
        System.out.println("8 - Excluir conta");
        System.out.println("9 - Dados da minha conta");
        System.out.println("10 - Ver extrato");
        System.out.println("11 - Sair da conta");
    }

    public static void main(String[] args) {

        Scanner leitor = new Scanner(System.in);

        Database.criarTabelas();

        Banco banco = new Banco();

        int resp = 0;

        ContaBancaria contaLogada = null;

        while (true) {

            if (contaLogada == null){
                menuInicial();
                resp = leitor.nextInt();
            }

            if (resp > 2){
                System.out.print("Programa Finalizado...");
                break;
            }

            if (contaLogada == null && resp == 1) {
                System.out.println("\n===== LOGIN =====");

                System.out.print("\nDigite o número da conta: ");
                int numeroConta = leitor.nextInt();
                leitor.nextLine();

                System.out.print("Digite a senha: ");
                String senhaPass = leitor.nextLine();

                contaLogada = banco.realizarLogin(numeroConta, senhaPass);

                if (contaLogada != null){

                    System.out.println("Bem-vindo " + contaLogada.getNome() + "!");
                }

                else {
                    System.out.print("\nNúmero o senha inválidos!");
                    continue;
                }
            }

            else if (resp == 2){
                leitor.nextLine();
                System.out.print("Digite seu nome: ");
                String nome = leitor.nextLine();

                System.out.print("Crie uma senha: ");
                String senha = leitor.nextLine();

                ContaBancaria contaCriada = banco.criarConta(nome, senha);

                contaLogada = contaCriada;

                if (contaLogada != null){
                    System.out.println("Conta criada com sucesso!");

                    System.out.println("Número da conta: " + contaCriada.getNumeroConta());
                    System.out.print("\nPressione enter para continuar...");
                    leitor.nextLine();
                    Database.salvaConta(contaCriada);

                    resp = 0;
                }
            }

            if (contaLogada != null) {
                menuDeAcoes(contaLogada);

                System.out.print("\nDigite a sua opção: ");
                int respMenuAcoes = leitor.nextInt();

                if (respMenuAcoes > 11 || respMenuAcoes < 1) {
                    leitor.nextLine();
                    System.out.print("Opção inválida! Pressione enter para continuar...");
                    leitor.nextLine();
                }

                else if (respMenuAcoes == 11) {
                    leitor.nextLine();
                    System.out.print("\nDeseja realmente sair da conta? (s/n) ");
                    String respLogout = leitor.nextLine();

                    if (respLogout.equals("s") || respLogout.equals("S")) {

                        contaLogada = null;
                        resp = 0;
                    }
                }

                else if (respMenuAcoes == 1) {
                    leitor.nextLine();
                    System.out.print("Digite a sua senha: ");
                    String senhaPass = leitor.nextLine();

                    if (contaLogada.validarSenha(senhaPass)) {
                        contaLogada.consultaSaldo();

                        System.out.print("\nPressione enter para continuar...");
                        leitor.nextLine();
                    }

                    else {
                        System.out.print("Senha incorreta! Pressione enter e tente novamente...");
                        leitor.nextLine();
                    }
                }

                else if (respMenuAcoes == 2) {
                    leitor.nextLine();
                    System.out.print("Digite o valor a ser depositado: R$");
                    BigDecimal valor = new BigDecimal(leitor.nextLine());

                    System.out.print("Digite a sua senha: ");
                    String senhaPass = leitor.nextLine();

                    boolean sucesso = banco.realizarDeposito(contaLogada ,senhaPass, valor);

                    if(sucesso){
                        System.out.print("\nDepósito de R$" + valor + " realizado com sucesso.");
                    }

                    else {
                        System.out.print("Senha ou valor inválido.");
                        continue;
                    }

                    System.out.print("\nPressione enter para continuar...");
                    leitor.nextLine();
                }

                else if (respMenuAcoes == 3) {
                    leitor.nextLine();
                    System.out.print("Digite o valor a ser sacado: ");
                    BigDecimal valor = new BigDecimal(leitor.nextLine());

                    System.out.print("Digite a sua senha: ");
                    String senhaPass = leitor.nextLine();

                    boolean sucesso = banco.realizarSaque(contaLogada, senhaPass, valor);

                    if (sucesso){
                        System.out.println("\nSaque de R$" + valor + " realizado com sucesso!");
                    }

                    else {
                        System.out.print("Senha ou valor inválido.");
                        continue;
                    }
                    System.out.print("Pressione enter para continuar...");
                    leitor.nextLine();
                }

                else if (respMenuAcoes == 4) {
                    leitor.nextLine();
                    System.out.print("Digite o número da conta de destino: ");
                    int numeroDestino = leitor.nextInt();
                    leitor.nextLine();

                    System.out.print("Digite o valor da transferência: ");
                    BigDecimal valor = new BigDecimal(leitor.nextLine());

                    System.out.print("Digite sua senha: ");
                    String senhaPass = leitor.nextLine();

                    boolean sucesso = banco.realizarTransferencia(contaLogada, senhaPass, valor, numeroDestino);

                    if (sucesso){
                        System.out.println("\nTransferência de R$" + valor + " realizada com sucesso!");
                    }

                    else {
                        System.out.print("Valor ou senha inválidos.");
                        continue;
                    }
                    System.out.print("Pressione enter para continuar...");
                    leitor.nextLine();
                }

                else if (respMenuAcoes == 5) {
                    leitor.nextLine();
                    System.out.print("\n");

                    contaLogada.mostrarHistorico();

                    System.out.print("\nPressione enter para continuar...");
                    leitor.nextLine();
                }

                else if (respMenuAcoes == 6) {
                    leitor.nextLine();

                    System.out.print("Digite a sua senha: ");
                    String senhaPass = leitor.nextLine();

                    System.out.print("Digite o novo nome: ");
                    String novoNome = leitor.nextLine();

                    boolean sucesso = banco.modificarNome(senhaPass, contaLogada, novoNome);

                    if (sucesso){
                        System.out.println("\nNome alterado com sucesso!");
                    }

                    else {
                        System.out.print("\nNome ou senha inválidos");
                        continue;
                    }

                    System.out.print("Pressione enter para continuar...");
                    leitor.nextLine();
                }

                else if (respMenuAcoes == 7){
                    leitor.nextLine();

                    System.out.print("Digite a sua senha ATUAL: ");
                    String senhaPass = leitor.nextLine();

                    System.out.print("Digite a sua nova senha: ");
                    String novaSenha = leitor.nextLine();

                    boolean sucesso = banco.modificarSenha(contaLogada, senhaPass, novaSenha);

                    if (sucesso){
                        System.out.print("Senha modificada com sucesso!");
                    }

                    else {
                        System.out.print("Senha inválida.");
                        continue;
                    }

                    System.out.print("\nPressione enter para continuar...");
                    leitor.nextLine();
                }

                else if (respMenuAcoes == 8){
                    leitor.nextLine();

                    System.out.print("Deseja realmente excuir a sua conta? (s/n)");
                    String resposta = leitor.nextLine();

                    if (resposta.equals("S") || resposta.equals("s")){
                        System.out.print("Digite o número da sua conta: ");
                        int numeroConta = leitor.nextInt();

                        leitor.nextLine();
                        System.out.print("Digite a sua senha: ");
                        String senhaPass = leitor.nextLine();

                        boolean sucesso = banco.excluirConta(contaLogada, numeroConta, senhaPass);

                        if (sucesso){
                            System.out.print("\nConta excluída com sucesso!");
                        }

                        else {
                            System.out.print("Erro ao excluir conta.");
                            continue;
                        }

                        System.out.print("\nPressione enter para continuar...");
                        leitor.nextLine();

                        contaLogada = null;
                    }
                }

                else if (respMenuAcoes == 9){
                    leitor.nextLine();

                    System.out.print("\nDigite o número da conta: ");
                    int numeroConta = leitor.nextInt();

                    ContaBancaria conta = banco.buscarConta(numeroConta);

                    if (conta != null){

                        System.out.print("\nConta encontrada!\n");

                        System.out.println("\n==== MINHA CONTA ====\n");
                        System.out.println("Titular: " + conta.getNome());
                        System.out.println("Número: " + conta.getNumeroConta());
                        System.out.println("Saldo atual: " + conta.getSaldo());
                        System.out.println("Quantidade de transações: " + conta.getQuantidadeTransacoes());
                        leitor.nextLine();
                    }

                    else {
                        System.out.print("\nConta não encontrada.");
                        continue;
                    }

                    System.out.println("Pressione enter para continuar...");
                    leitor.nextLine();
                }

                else if (respMenuAcoes == 10){
                    leitor.nextLine();

                    contaLogada.mostrarExtrato();

                    System.out.print("\nPressione enter para continuar...");
                    leitor.nextLine();
                }
            }
        }
    }
}
