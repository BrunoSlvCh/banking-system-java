import java.util.ArrayList;
import java.io.PrintWriter;
import java.math.BigDecimal;
public class Banco {

    private ArrayList<ContaBancaria> contas;
    private int proximoNumeroConta;
    private int proximoIdTransferencia;

    public Banco() {

        contas = Database.carregarContas();

        proximoNumeroConta = 1001;
        proximoIdTransferencia = 100000;

        for (ContaBancaria contas : contas) {

            if (contas.getNumeroConta() >= proximoNumeroConta) {

                proximoNumeroConta = contas.getNumeroConta() + 1;
            }
        }
    }

    public ContaBancaria criarConta(String nome, String senha) {
        ContaBancaria novaConta = new ContaBancaria(proximoNumeroConta, nome, senha);
        contas.add(novaConta);
        proximoNumeroConta++;
        return novaConta;
    }

    public ContaBancaria realizarLogin(int numeroConta, String senhaPass) {
        ContaBancaria conta = autenticar(numeroConta, senhaPass);

        if (conta != null) {
            return conta;
        } else {
            return null;
        }
    }

    public boolean realizarDeposito(ContaBancaria conta, String senhaPass, BigDecimal valor) {
        if (!conta.validarSenha(senhaPass)) {
            System.out.print("Senha incorreta.");
            return false;
        }

        conta.depositar(valor);

        Database.salvaConta(conta);
        Database.salvarTransacao(conta.getNumeroConta(), conta.getUltimaTransacao());
        return true;
    }

    public boolean realizarSaque(ContaBancaria conta, String senhaPass, BigDecimal valor) {
        if (!conta.validarSenha(senhaPass)) {
            System.out.print("Senha incorreta.");
            return false;
        }

        if (valor.compareTo(BigDecimal.ONE) < 0) {
            System.out.println("O valor mínimo para saque é R$1,00.");
            return false;
        }

        if (valor.compareTo(conta.getSaldo()) > 0) {

            System.out.println("Saldo insuficiente.");
            return false;
        }

        conta.sacarValor(valor);

        Database.salvaConta(conta);
        Database.salvarTransacao(conta.getNumeroConta(), conta.getUltimaTransacao());
        return true;
    }

    public boolean realizarTransferencia(ContaBancaria conta, String senha, BigDecimal valor, int numeroDestino) {
        ContaBancaria contaDestino = buscarConta(numeroDestino);

        if (contaDestino == null) {
            System.out.println("Conta não encontrada.");
            return false;
        }

        if (conta.getNumeroConta() == contaDestino.getNumeroConta()) {
            System.out.println("Você não pode transferir para si mesmo.");
            return false;
        }

        if (!conta.validarSenha(senha)) {
            System.out.println("Senha incorreta.");
            return false;
        }

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Valor inválido.");
            return false;
        }

        if (valor.compareTo(conta.getSaldo()) > 0) {
            System.out.println("Saldo insuficiente.");
            return false;
        }

        int id = gerarIdTransferencia();
        conta.transferir(contaDestino, valor, id);

        Database.salvaConta(conta);
        Database.salvarTransacao(conta.getNumeroConta(), conta.getUltimaTransacao());
        return true;
    }

    public boolean modificarNome(String senhaPass, ContaBancaria contaLogada, String novoNome) {
        if (!contaLogada.validarSenha(senhaPass)) {
            System.out.print("Senha incorreta.");
            return false;
        }

        if (contaLogada.getNome().equals(novoNome)) {
            System.out.print("\nNão é possivel modificar o nome para o nome atual.");
            return false;
        }
        alterarNome(contaLogada, novoNome);
        return true;
    }

    public boolean modificarSenha(ContaBancaria contaLogada, String senhaPass, String novaSenha) {
        if (!contaLogada.validarSenha(senhaPass)) {
            System.out.print("Senha incorreta.");
            return false;
        }

        if (senhaPass.equals(novaSenha)) {
            System.out.print("\nSua senha não pode ser igual a senha anteririor!");
            return false;
        }
        alterarSenha(contaLogada, novaSenha);
        return true;
    }

    public int gerarIdTransferencia() {
        int id = proximoIdTransferencia;

        proximoIdTransferencia++;

        salvarProximoIdTransferencia();
        return id;
    }

    public void salvarProximoIdTransferencia() {
        try {

            PrintWriter escritor = new PrintWriter("id_transferencia.txt");
            escritor.println(proximoIdTransferencia);
            escritor.close();
        } catch (Exception e) {
            System.out.println("Erro ao salvar ID das transferências.");
        }
    }

    public ContaBancaria buscarConta(int numeroConta) {
        for (ContaBancaria conta : contas) {

            if (conta.getNumeroConta() == numeroConta) {
                return conta;
            }
        }
        return null;
    }

    public boolean excluirConta(ContaBancaria contaLogada, int numeroConta, String senhaPass) {
        if (contaLogada.getNumeroConta() != numeroConta) {

            System.out.print("\nNúmero da conta incorreto!");
            return false;
        }

        if (!contaLogada.validarSenha(senhaPass)) {

            System.out.print("\nSenha incorreta!");
            return false;
        } else {

            Database.deletarConta(numeroConta);

            contas.remove(contaLogada);
            return true;
        }
    }

    public ContaBancaria autenticar(int numeroConta, String senha) {
        ContaBancaria conta = buscarConta(numeroConta);

        if (conta != null && conta.validarSenha(senha)) {
            return conta;
        }

        return null;
    }

    public void alterarNome(ContaBancaria conta, String novoNome) {
        conta.setNome(novoNome);
        Database.salvaConta(conta);
    }

    public void alterarSenha(ContaBancaria conta, String novaSenha) {
        conta.setSenha(novaSenha);
        Database.salvaConta(conta);
    }
}