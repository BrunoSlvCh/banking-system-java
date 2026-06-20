import java.math.BigDecimal;
import java.util.ArrayList;

public class ContaBancaria {
    
    private String nome;
    private String senha;
    private BigDecimal saldo;
    private int numeroConta;
    private ArrayList<Transacao> historico;

    public ContaBancaria(int numeroContas, String nome, String senha) {
        this.numeroConta = numeroContas;
        this.nome = nome;
        this.senha = senha;
        this.saldo = BigDecimal.ZERO;
        this.historico = new ArrayList<>();
    }

    public String getNome(){
        return nome;
    }

    public int getNumeroConta(){
        return numeroConta;
    }

    public String getSenha() {
        return senha;
    }

    public boolean validarSenha(String senha){
        return this.senha.equals(senha);
    }

    public BigDecimal getSaldo(){
        return saldo;
    }

    public void setSaldo(BigDecimal saldo){
        this.saldo = saldo;
    }

    private String gerarDataHora(){
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatador = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return agora.format(formatador);
    }

    public void mostrarHistorico(){
        if (historico.isEmpty()){
            System.out.print("Nenhuma movimentação registrada.");
            return;
        }
        historico.forEach(System.out::println);
    }

    public void depositar(BigDecimal valor) {
        this.saldo = this.saldo.add(valor);
        historico.add(new Transacao("Depósito", valor, gerarDataHora()));
    }

    public void sacarValor(BigDecimal valor) {
        this.saldo = this.saldo.subtract(valor);
        historico.add(new Transacao("Saque", valor, gerarDataHora()));
    }

    public void transferir(ContaBancaria destino, BigDecimal valor, int idTransferencia) {
        this.saldo = this.saldo.subtract(valor);
        destino.saldo = destino.saldo.add(valor);

        this.historico.add(new Transacao("Transferência enviada", valor, gerarDataHora(), idTransferencia));

        destino.historico.add(new Transacao("Transferência recebida", valor, gerarDataHora(), idTransferencia));
    }

    public void consultaSaldo(){
        System.out.printf("O seu saldo atual é de R$%.2f%n", this.saldo);
    }

    public void setNome(String nome) {
        this.nome = nome;
        historico.add(new Transacao("Alteração de nome para "+ nome, BigDecimal.ZERO, gerarDataHora()));
    }

    public void setSenha(String senha){
        this.senha = senha;
        historico.add(new Transacao("Alteração de senha para " + senha, BigDecimal.ZERO, gerarDataHora()));
    }

    public int getQuantidadeTransacoes() {
        return historico.size();
    }

    public Transacao getUltimaTransacao() {

        if (historico.isEmpty()) {
            return null;
        }

        return historico.get(historico.size() - 1);
    }

    public void mostrarExtrato() {
        System.out.println("\n==============================");
        System.out.println("         EXTRATO");
        System.out.println("==============================");

        System.out.println("Titular: " + nome);
        System.out.println("Conta: " + numeroConta);

        System.out.printf("Saldo Atual: R$%.2f%n", saldo);

        System.out.println("\nMovimentações:");

        if (historico.isEmpty()) {
            System.out.println("Nenhuma movimentação encontrada.");
        }

        else {
            for (Transacao t : historico) {
                System.out.println(t);
            }
        }

        System.out.println("==============================");
    }
}