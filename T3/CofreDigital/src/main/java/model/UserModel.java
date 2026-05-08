package model;
import java.time.LocalDateTime;

/**
 * Reflects de Usuarios table in the database.
 * This class is used to represent user data in the application.
 */
public class UserModel {
    private Integer uid;
    private String login;
    private String nome;
    private String senhaBcrypt;
    private byte[] totpSecretEncrypted;
    private int grupoId;
    private Integer kid;
    private int erroSenha;
    private int erroToken;
    private LocalDateTime bloqueadoAte;
    private int totalAcessos;
    private int totalConsultas;

    //Getters and Setters

    public Integer getUid() {
        return this.uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenhaBcrypt() {
        return this.senhaBcrypt;
    }

    public void setSenhaBcrypt(String senhaBcrypt) {
        this.senhaBcrypt = senhaBcrypt;
    }

    public byte[] getTotpSecretEncrypted() {
        return this.totpSecretEncrypted;
    }

    public void setTotpSecretEncrypted(byte[] totpSecretEncrypted) {
        this.totpSecretEncrypted = totpSecretEncrypted;
    }

    public int getGrupoId() {
        return this.grupoId;
    }

    public void setGrupoId(int grupoId) {
        this.grupoId = grupoId;
    }

    public Integer getKid() {
        return this.kid;
    }

    public void setKid(Integer kid) {
        this.kid = kid;
    }

    public int getErroSenha() {
        return this.erroSenha;
    }

    public void setErroSenha(int erroSenha) {
        this.erroSenha = erroSenha;
    }

    public int getErroToken() {
        return this.erroToken;
    }

    public void setErroToken(int erroToken) {
        this.erroToken = erroToken;
    }

    public LocalDateTime getBloqueadoAte() {
        return this.bloqueadoAte;
    }

    public void setBloqueadoAte(LocalDateTime bloqueadoAte) {
        this.bloqueadoAte = bloqueadoAte;
    }

    public int getTotalAcessos() {
        return this.totalAcessos;
    }

    public void setTotalAcessos(int totalAcessos) {
        this.totalAcessos = totalAcessos;
    }

    public int getTotalConsultas() {
        return this.totalConsultas;
    }

    public void setTotalConsultas(int totalConsultas) {
        this.totalConsultas = totalConsultas;
    }
}
