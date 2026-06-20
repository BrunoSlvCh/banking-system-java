import java.math.BigDecimal;

public class Transacao{

    private String tipo;
    private BigDecimal valor;
    private String data;
    Integer idTransferencia;

    public Transacao(String tipo, BigDecimal valor, String data){
        this.tipo = tipo;
        this.valor = valor;
        this.data = data;
        this.idTransferencia = null;
    }

    public Transacao(String tipo, BigDecimal valor, String data, Integer idTransferencia){
        this.tipo = tipo;
        this.valor = valor;
        this.data = data;
        this.idTransferencia = idTransferencia;
    }

    @Override
    public String toString(){
        if (idTransferencia != null){
            return tipo + " | R$" + valor + " | Em:" + data + " | ID:" + idTransferencia;
        }
        return  tipo + " | R$" + valor + " | Em: " + data;
    }

    public String getTipo() {
        return tipo;
    }

    public BigDecimal getValor() {
        return valor;}

    public String getData() {
        return data;
    }

    public Integer getIdTransferencia() {
        return idTransferencia;
    }
}