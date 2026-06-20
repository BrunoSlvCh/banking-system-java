import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.sql.*;
import java.util.concurrent.Executors;

/**
 * NexusBank - API REST Server
 * Usa APENAS java.net.httpserver (nativo do JDK - zero dependências externas!)
 * Nenhuma classe original foi modificada.
 *
 * Compilar:
 *   javac -cp "lib\*;src" src\ApiServer.java src\Banco.java src\ContaBancaria.java src\Database.java src\Transacao.java -d out
 *
 * Executar:
 *   java -cp "lib\*;out" ApiServer
 *
 * Depois abra index.html no navegador.
 */
public class ApiServer {

    private static Banco banco;
    private static final Map<String, ContaBancaria> sessoes = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Database.criarTabelas();
        banco = new Banco();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Registra todos os endpoints
        server.createContext("/api/login",        ApiServer::handleLogin);
        server.createContext("/api/cadastro",      ApiServer::handleCadastro);
        server.createContext("/api/logout",        ApiServer::handleLogout);
        server.createContext("/api/conta",         ApiServer::handleConta);
        server.createContext("/api/extrato",       ApiServer::handleExtrato);
        server.createContext("/api/deposito",      ApiServer::handleDeposito);
        server.createContext("/api/saque",         ApiServer::handleSaque);
        server.createContext("/api/transferencia", ApiServer::handleTransferencia);
        server.createContext("/api/perfil/nome",   ApiServer::handlePerfNome);
        server.createContext("/api/perfil/senha",  ApiServer::handlePerfSenha);

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("✅ NexusBank API rodando em http://localhost:8080");
        System.out.println("   Abra o arquivo index.html no navegador.");
    }

    // ── HANDLERS ──────────────────────────────────────────────────────────

    static void handleLogin(HttpExchange ex) throws IOException {
        if (cors(ex)) return;
        if (!method(ex, "POST")) return;

        Map<String, String> body = parseBody(ex);
        int numeroConta = intVal(body, "numeroConta", 0);
        String senha = body.getOrDefault("senha", "");

        ContaBancaria conta = banco.realizarLogin(numeroConta, senha);
        if (conta == null) { respond(ex, 401, erro("Número ou senha inválidos.")); return; }

        String token = "tk_" + numeroConta + "_" + System.currentTimeMillis();
        sessoes.put(token, conta);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("numeroConta", conta.getNumeroConta());
        data.put("nome", conta.getNome());
        respond(ex, 200, sucesso(data));
    }

    static void handleCadastro(HttpExchange ex) throws IOException {
        if (cors(ex)) return;
        if (!method(ex, "POST")) return;

        Map<String, String> body = parseBody(ex);
        String nome = body.getOrDefault("nome", "").trim();
        String senha = body.getOrDefault("senha", "");

        if (nome.isEmpty() || senha.isEmpty()) { respond(ex, 400, erro("Nome e senha são obrigatórios.")); return; }

        ContaBancaria nova = banco.criarConta(nome, senha);
        Database.salvaConta(nova);

        String token = "tk_" + nova.getNumeroConta() + "_" + System.currentTimeMillis();
        sessoes.put(token, nova);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("numeroConta", nova.getNumeroConta());
        data.put("nome", nova.getNome());
        respond(ex, 200, sucesso(data));
    }

    static void handleLogout(HttpExchange ex) throws IOException {
        if (cors(ex)) return;
        String token = getToken(ex);
        if (token != null) sessoes.remove(token);
        respond(ex, 200, sucesso("Logout realizado."));
    }

    static void handleConta(HttpExchange ex) throws IOException {
        if (cors(ex)) return;
        String m = ex.getRequestMethod().toUpperCase();

        if (m.equals("GET")) {
            ContaBancaria conta = auth(ex); if (conta == null) return;
            ContaBancaria att = banco.buscarConta(conta.getNumeroConta());
            if (att == null) { respond(ex, 404, erro("Conta não encontrada.")); return; }
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("numeroConta", att.getNumeroConta());
            d.put("nome", att.getNome());
            d.put("saldo", att.getSaldo());
            d.put("quantidadeTransacoes", att.getQuantidadeTransacoes());
            respond(ex, 200, sucesso(d));

        } else if (m.equals("DELETE")) {
            ContaBancaria conta = auth(ex); if (conta == null) return;
            Map<String, String> body = parseBody(ex);
            String senha = body.getOrDefault("senha", "");
            boolean ok = banco.excluirConta(conta, conta.getNumeroConta(), senha);
            if (!ok) { respond(ex, 400, erro("Senha incorreta.")); return; }
            String token = getToken(ex);
            if (token != null) sessoes.remove(token);
            respond(ex, 200, sucesso("Conta excluída com sucesso."));
        } else {
            respond(ex, 405, erro("Método não permitido."));
        }
    }

    static void handleExtrato(HttpExchange ex) throws IOException {
        if (cors(ex)) return;
        if (!method(ex, "GET")) return;
        ContaBancaria conta = auth(ex); if (conta == null) return;
        List<Map<String, Object>> lista = carregarTransacoes(conta.getNumeroConta());
        respond(ex, 200, sucesso(lista));
    }

    static void handleDeposito(HttpExchange ex) throws IOException {
        if (cors(ex)) return;
        if (!method(ex, "POST")) return;
        ContaBancaria conta = auth(ex); if (conta == null) return;
        Map<String, String> body = parseBody(ex);
        BigDecimal valor = bdVal(body, "valor");
        String senha = body.getOrDefault("senha", "");
        boolean ok = banco.realizarDeposito(conta, senha, valor);
        if (!ok) { respond(ex, 400, erro("Senha incorreta ou valor inválido.")); return; }
        respond(ex, 200, sucesso("Depósito de R$" + valor + " realizado com sucesso."));
    }

    static void handleSaque(HttpExchange ex) throws IOException {
        if (cors(ex)) return;
        if (!method(ex, "POST")) return;
        ContaBancaria conta = auth(ex); if (conta == null) return;
        Map<String, String> body = parseBody(ex);
        BigDecimal valor = bdVal(body, "valor");
        String senha = body.getOrDefault("senha", "");
        boolean ok = banco.realizarSaque(conta, senha, valor);
        if (!ok) { respond(ex, 400, erro("Saldo insuficiente, senha incorreta ou valor inválido.")); return; }
        respond(ex, 200, sucesso("Saque de R$" + valor + " realizado com sucesso."));
    }

    static void handleTransferencia(HttpExchange ex) throws IOException {
        if (cors(ex)) return;
        if (!method(ex, "POST")) return;
        ContaBancaria conta = auth(ex); if (conta == null) return;
        Map<String, String> body = parseBody(ex);
        int destino = intVal(body, "numeroDestino", 0);
        BigDecimal valor = bdVal(body, "valor");
        String senha = body.getOrDefault("senha", "");
        boolean ok = banco.realizarTransferencia(conta, senha, valor, destino);
        if (!ok) { respond(ex, 400, erro("Transferência não realizada. Verifique saldo, senha e conta de destino.")); return; }
        respond(ex, 200, sucesso("Transferência de R$" + valor + " realizada com sucesso."));
    }

    static void handlePerfNome(HttpExchange ex) throws IOException {
        if (cors(ex)) return;
        if (!method(ex, "PUT")) return;
        ContaBancaria conta = auth(ex); if (conta == null) return;
        Map<String, String> body = parseBody(ex);
        String senha = body.getOrDefault("senha", "");
        String novoNome = body.getOrDefault("novoNome", "").trim();
        boolean ok = banco.modificarNome(senha, conta, novoNome);
        if (!ok) { respond(ex, 400, erro("Senha incorreta ou nome inválido.")); return; }
        respond(ex, 200, sucesso("Nome alterado para " + novoNome + "."));
    }

    static void handlePerfSenha(HttpExchange ex) throws IOException {
        if (cors(ex)) return;
        if (!method(ex, "PUT")) return;
        ContaBancaria conta = auth(ex); if (conta == null) return;
        Map<String, String> body = parseBody(ex);
        String senhaAtual = body.getOrDefault("senhaAtual", "");
        String novaSenha = body.getOrDefault("novaSenha", "");
        boolean ok = banco.modificarSenha(conta, senhaAtual, novaSenha);
        if (!ok) { respond(ex, 400, erro("Senha atual incorreta ou nova senha igual à anterior.")); return; }
        respond(ex, 200, sucesso("Senha alterada com sucesso."));
    }

    // ── UTILITÁRIOS ───────────────────────────────────────────────────────

    /** Adiciona cabeçalhos CORS e responde OPTIONS. Retorna true se era preflight. */
    static boolean cors(HttpExchange ex) throws IOException {
        Headers h = ex.getResponseHeaders();
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        h.add("Content-Type", "application/json; charset=utf-8");
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    /** Verifica método HTTP. Responde 405 se errado. */
    static boolean method(HttpExchange ex, String expected) throws IOException {
        if (!ex.getRequestMethod().equalsIgnoreCase(expected)) {
            respond(ex, 405, erro("Método não permitido."));
            return false;
        }
        return true;
    }

    /** Autentica pelo token Bearer. Responde 401 se inválido. */
    static ContaBancaria auth(HttpExchange ex) throws IOException {
        String token = getToken(ex);
        if (token == null || !sessoes.containsKey(token)) {
            respond(ex, 401, erro("Não autenticado."));
            return null;
        }
        return sessoes.get(token);
    }

    static String getToken(HttpExchange ex) {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        return null;
    }

    /** Lê o corpo da requisição e faz parse manual de JSON simples. */
    static Map<String, String> parseBody(HttpExchange ex) throws IOException {
        String raw = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        // Remove { } e quebra por vírgula
        raw = raw.trim();
        if (raw.startsWith("{")) raw = raw.substring(1);
        if (raw.endsWith("}")) raw = raw.substring(0, raw.length() - 1);
        for (String pair : raw.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("\"", "");
                String val = kv[1].trim().replaceAll("\"", "");
                map.put(key, val);
            }
        }
        return map;
    }

    static int intVal(Map<String, String> m, String k, int def) {
        try { return Integer.parseInt(m.getOrDefault(k, String.valueOf(def)).trim()); }
        catch (Exception e) { return def; }
    }

    static BigDecimal bdVal(Map<String, String> m, String k) {
        try { return new BigDecimal(m.getOrDefault(k, "0").trim()); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }

    /** Serializa Map/List/String/Number para JSON manualmente. */
    static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String s) return "\"" + s.replace("\\","\\\\").replace("\"","\\\"") + "\"";
        if (obj instanceof Boolean || obj instanceof Number) return obj.toString();
        if (obj instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            return sb.append("]").toString();
        }
        if (obj instanceof Map<?,?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?,?> e : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(e.getKey()).append("\":").append(toJson(e.getValue()));
                first = false;
            }
            return sb.append("}").toString();
        }
        return "\"" + obj.toString() + "\"";
    }

    static void respond(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    static String sucesso(Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("ok", true);
        r.put("data", data);
        return toJson(r);
    }

    static String erro(String msg) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("ok", false);
        r.put("erro", msg);
        return toJson(r);
    }

    // Busca transações do banco de dados
    static List<Map<String, Object>> carregarTransacoes(int numeroConta) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT * FROM transacoes WHERE numeroConta = ? ORDER BY id DESC LIMIT 50";
        try (Connection conn = Database.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, numeroConta);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> t = new LinkedHashMap<>();
                t.put("id", rs.getInt("id"));
                t.put("tipo", rs.getString("tipo"));
                t.put("valor", rs.getDouble("valor"));
                t.put("data", rs.getString("data"));
                Object idT = rs.getObject("idTransferencia");
                t.put("idTransferencia", idT);
                lista.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}