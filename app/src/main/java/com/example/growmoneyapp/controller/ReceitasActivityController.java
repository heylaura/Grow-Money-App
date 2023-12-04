package com.example.growmoneyapp.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.example.growmoneyapp.R;
import com.example.growmoneyapp.adapter.AdapterMovimentacao;
import com.example.growmoneyapp.config.ConfiguracaoFirebase;
import com.example.growmoneyapp.helper.Base64Custom;
import com.example.growmoneyapp.helper.DateCustom;
import com.example.growmoneyapp.model.Movimentacao;
import com.example.growmoneyapp.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReceitasActivityController extends AppCompatActivity {

    private TextInputEditText campoData, campoCategoria, campoDescricao;
    private EditText campoValor;
    private Movimentacao movimentacao;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double receitaTotal;
    private AdapterMovimentacao adapterMovimentacao;
    private String mesAnoSelecionado;
    private String key;
    private List<Movimentacao> movimentacoes = new ArrayList<>();
    private DatabaseReference usuarioRef;
    private Double despesaTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);

        campoValor = findViewById(R.id.editValorR);
        campoData = findViewById(R.id.editDataR);
        campoDescricao = findViewById(R.id.editDescricaoR);
        campoCategoria = findViewById(R.id.editCategoriaR);

        //configurando o campo data com a data atual
        campoData.setText( DateCustom.dataAtual() );
        recuperarReceitaTotal();

        if(getIntent().getBooleanExtra("editar", false)){
            adapterMovimentacao = new AdapterMovimentacao(movimentacoes, this);
            recuperarDadosReceita();
        }
    }

    //validação dos campos para salvar a receita
    public Boolean validarCamposReceita() {

        String textoValor = campoValor.getText().toString();
        String textoData = campoData.getText().toString();
        String textoCategoria = campoCategoria.getText().toString();
        String textoDescricao = campoDescricao.getText().toString();

        if (!textoValor.isEmpty()) {
            if (!textoData.isEmpty()) {
                if (!textoCategoria.isEmpty()) {
                    if (!textoDescricao.isEmpty()) {
                        return true;
                    } else {
                        Toast.makeText(ReceitasActivityController.this,
                                "Informe a descrição da Receita!",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {
                    Toast.makeText(ReceitasActivityController.this,
                            "Informe a categoria da Receita!",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                Toast.makeText(ReceitasActivityController.this,
                        "Informe a data da Receita!",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(ReceitasActivityController.this,
                    "Informe o valor da Receita!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //método para recuperar a receita
    public void recuperarReceitaTotal(){
        //acessando o nó de usuários para incrementar as receitas
        //recuperando o email do usuário
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        //convertendo o email em base64
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        DatabaseReference usuarioRef = firebaseRef.child("usuarios")
                .child( idUsuario );

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //obtendo o valor e convertendo-o em um objeto do tipo Usuario
                Usuario usuario = dataSnapshot.getValue( Usuario.class );

                //recuperando o atributo receitaTotal
                receitaTotal = usuario.getReceitaTotal();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //salvando a receita
    public void salvarReceita(View view){

        if(getIntent().getBooleanExtra("editar", false)){

            if(validarCamposReceita()){
                confirmarEdicao();
            }
        }

        else{
            if( validarCamposReceita() ){

                //instanciando a movimentação
                movimentacao = new Movimentacao();

                Double valorRecuperado =  Double.parseDouble( campoValor.getText().toString() ) ;
                String data = campoData.getText().toString();

                movimentacao.setValor( valorRecuperado );
                movimentacao.setCategoria( campoCategoria.getText().toString() );
                movimentacao.setDescricao( campoDescricao.getText().toString() );
                movimentacao.setData( campoData.getText().toString() );
                movimentacao.setTipo( "r" ); //identificando que é uma receita

                Double receitaAtualizada = receitaTotal + valorRecuperado;

                //atualizando a receita
                atualizarReceita( receitaAtualizada );

                //salvando a receita
                movimentacao.salvar( data );

                finish();
            }
        }
    }

    //atualizando a receita
    public void atualizarReceita( Double receita){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        DatabaseReference usuarioRef = firebaseRef.child("usuarios")
                .child( idUsuario );

        //atualizando o atributo receitaTotal do nó "usuarios"
        usuarioRef.child("receitaTotal").setValue( receita );
    }

    public void recuperarDadosReceita(){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        Intent intent = getIntent();
        key = intent.getStringExtra("key");
        String data = intent.getStringExtra("data");
        String categoria = intent.getStringExtra("categoria");
        String descricao = intent.getStringExtra("descricao");
        double valor = intent.getDoubleExtra("valor", 0.0);

        campoData.setText(data);
        campoCategoria.setText(categoria);
        campoDescricao.setText(descricao);
        campoValor.setText(String.valueOf(valor));

        validarCamposReceita();
    }

    public void confirmarEdicao(  ){

        mesAnoSelecionado = getIntent().getStringExtra("mesAnoSelecionado");
        key = getIntent().getStringExtra("key");

        //novos valores
        String novaData = campoData.getText().toString();
        String novaCategoria = campoCategoria.getText().toString();
        String novaDescricao = campoDescricao.getText().toString();
        double novoValor = Double.parseDouble(campoValor.getText().toString());

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );

        // Criar uma nova instância de Movimentacao
        Movimentacao movimentacaoEditada = new Movimentacao();

        // Atribuir os novos valores
        movimentacaoEditada.setKey( key );
        movimentacaoEditada.setData(novaData);
        movimentacaoEditada.setCategoria(novaCategoria);
        movimentacaoEditada.setDescricao(novaDescricao);
        movimentacaoEditada.setValor(novoValor);

        movimentacaoEditada.editar(idUsuario, mesAnoSelecionado, key, movimentacaoEditada);

        adapterMovimentacao.notifyDataSetChanged();

        atualizarSaldo();
    }

    //atualizando o saldo geral
    public void atualizarSaldo(){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        usuarioRef = firebaseRef.child("usuarios")
                .child( idUsuario );

        //atualizando receitas
        if( movimentacao.getTipo().equals("r")){
            receitaTotal = receitaTotal - movimentacao.getValor();
            usuarioRef.child("receitaTotal").setValue(  receitaTotal );
        }

        //atualizando despesas
        if( movimentacao.getTipo().equals("d")){
            despesaTotal = despesaTotal - movimentacao.getValor();
            usuarioRef.child("despesaTotal").setValue(  despesaTotal );
        }
    }
}
