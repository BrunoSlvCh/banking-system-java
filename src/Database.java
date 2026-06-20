import java.util.ArrayList;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;

public class Database {

    private static final String URL =
            "jdbc:sqlite:banco.db";

    public static Connection conectar()
            throws SQLException {

        return DriverManager.getConnection(URL);
    }

    public static void criarTabelas() {
        String sqlContas = """
            CREATE TABLE IF NOT EXISTS contas (
                numeroConta INTEGER PRIMARY KEY,
                nome TEXT NOT NULL,
                senha TEXT NOT NULL,
                saldo REAL NOT NULL
            );
        """;

        String sqlTransacoes = """
            CREATE TABLE IF NOT EXISTS transacoes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numeroConta INTEGER NOT NULL,
                tipo TEXT NOT NULL,
                valor REAL NOT NULL,
                data TEXT NOT NULL,
                idTransferencia INTEGER,
                FOREIGN KEY(numeroConta)
                    REFERENCES contas(numeroConta)
            );
        """;

        try (
            Connection conn = conectar();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sqlContas);
            stmt.execute(sqlTransacoes);
            System.out.println("Tabelas criadas com sucesso!");
        }

        catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void salvaConta(ContaBancaria conta){

        String sql = """
                INSERT OR REPLACE INTO contas
                (numeroConta, nome, senha, saldo)
                VALUES (?, ?, ?, ?)
        """;

        try (
            Connection conn = conectar();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, conta.getNumeroConta());
            stmt.setString(2, conta.getNome());
            stmt.setString(3, conta.getSenha());
            stmt.setBigDecimal(4, conta.getSaldo());

            stmt.executeUpdate();
        }

        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deletarConta(int numeroContaUser) {

        String sql = """
            DELETE FROM contas WHERE numeroConta = ?;
    """;

        try (
                Connection conn = conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, numeroContaUser);

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("Conta número " + numeroContaUser + " deletada com sucesso!");
            }

            else {
                System.out.println("Nenhuma conta encontrada com o número " + numeroContaUser);
            }
        }

        catch (SQLException e) {
            System.err.println("Erro ao deletar conta: " + e.getMessage());

            e.printStackTrace();
        }
    }

    public static ArrayList<ContaBancaria> carregarContas() {

        ArrayList<ContaBancaria> contas = new ArrayList<>();

        String sql = "SELECT * FROM contas";

        try (
                Connection conn = conectar();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {

            while (rs.next()) {

                int numeroConta = rs.getInt("numeroConta");

                String nome = rs.getString("nome");

                String senha = rs.getString("senha");

                BigDecimal saldo = rs.getBigDecimal("saldo");

                ContaBancaria conta = new ContaBancaria(numeroConta, nome, senha);

                conta.setSaldo(saldo);

                contas.add(conta);
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
        }

        return contas;
    }

    public static void salvarTransacao(int numeroConta, Transacao transacao){

        String sql = """
            INSERT INTO transacoes
                (numeroConta, tipo, valor, data, idTransferencia)
                VALUES (?, ?, ?, ?, ?)
            """;

        try (
                Connection conn = conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numeroConta);
            stmt.setString(2, transacao.getTipo());
            stmt.setBigDecimal(3, transacao.getValor());
            stmt.setString(4, transacao.getData());

            if (transacao.getIdTransferencia() != null){
                stmt.setInt(5, transacao.getIdTransferencia());
            }

            else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.executeUpdate();
        }

        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}