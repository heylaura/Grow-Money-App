package com.example.growmoneyapp.model;

import android.util.Log;
import com.example.growmoneyapp.config.ConfiguracaoFirebase;
import com.example.growmoneyapp.helper.Base64Custom;
import com.example.growmoneyapp.helper.DateCustom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Movimentacao {

    private String data;
    private String categoria;
    private String descricao;
    private String tipo;
    private double valor;
    private String key;

    public Movimentacao() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getKey() { return key; }

    public void setKey(String key) { this.key = key; }

    //salvando a despesa
    public void salvar( String dataEscolhida) {
        //recuperando a referência do Firebase
        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();

        //recuperando o id do usuario em Base64
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String idUsuario = Base64Custom.codificarBase64(autenticacao.getCurrentUser().getEmail());

        //recuperando o mes e ano da transação inserida
        String mesAno = DateCustom.mesAnoDataEscolhida(dataEscolhida);

        //criando o nó de movimentação
        firebase.child("movimentacao")
                .child(idUsuario)
                .child(mesAno)
                .push() //gera o id único do Firebase
                .setValue(this);
    }

    public static void editar(String idUsuario, String mesAno, String key, Movimentacao movimentacao) {

        DatabaseReference movimentacaoRef = ConfiguracaoFirebase.getFirebaseDatabase();

        // Criar a referência correta para a movimentação existente
        DatabaseReference movimentacaoNodeRef = movimentacaoRef.child("movimentacao")
                .child(idUsuario)
                .child(mesAno)
                .child(key);

        // Atualizando os dados da movimentação existente
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("categoria", movimentacao.getCategoria());
        updates.put("data", movimentacao.getData());
        updates.put("descricao", movimentacao.getDescricao());
        updates.put("valor", movimentacao.getValor());

        movimentacaoNodeRef.updateChildren(updates);
    }
}
