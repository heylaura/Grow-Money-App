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

public class DespesasActivityController extends AppCompatActivity {

    private TextInputEditText campoData, campoCategoria, campoDescricao;
    private EditText campoValor;
    private Movimentacao movimentacao;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double despesaTotal;
    private AdapterMovimentacao adapterMovimentacao;
    private String mesAnoSelecionado;
    private String key;
    private List<Movimentacao> movimentacoes = new ArrayList<>();
    private DatabaseReference usuarioRef;
    private Double receitaTotal = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despesas);

        campoValor = findViewById(R.id.editValor);
        campoData = findViewById(R.id.editData);
        campoDescricao = findViewById(R.id.editDescricao);
        campoCategoria = findViewById(R.id.editCategoria);

        //configurando o campo data com a data atual
        campoData.setText( DateCustom.dataAtual() );
        //quando o usuário recarregar essa tela já terá o valor da despesa total
        recuperarDespesaTotal();

        if(getIntent().getBooleanExtra("editar", false)){
            adapterMovimentacao = new AdapterMovimentacao(movimentacoes, this);
            recuperarDadosDespesa();
        }
    }

    //salvando a despesa
    public void salvarDespesa(View view){

        if(getIntent().getBooleanExtra("editar", false)){

            if(validarCamposDespesa()){

                confirmarEdicao();
            }
        }

        else{
            if( validarCamposDespesa() ){

                //instanciando a movimentação
                movimentacao = new Movimentacao();

                Double valorRecuperado =  Double.parseDouble( campoValor.getText().toString() ) ;
                String data = campoData.getText().toString();

                movimentacao.setValor( valorRecuperado );
                movimentacao.setCategoria( campoCategoria.getText().toString() );
                movimentacao.setDescricao( campoDescricao.getText().toString() );
                movimentacao.setData( campoData.getText().toString() );
                movimentacao.setTipo( "d" ); //identificando que é uma despesa

                Double despesaAtualizada = despesaTotal + valorRecuperado;

                //atualizando a despesa
                atualizarDespesa( despesaAtualizada );

                //salvando a despesa
                movimentacao.salvar( data );
                finish();
            }
        }
    }

    //validação dos campos para salvar a despesa
    public Boolean validarCamposDespesa(){

        String textoValor = campoValor.getText().toString();
        String textoData = campoData.getText().toString();
        String textoCategoria = campoCategoria.getText().toString();
        String textoDescricao = campoDescricao.getText().toString();

        if( !textoValor.isEmpty() ){
            if( !textoData.isEmpty() ){
                if( !textoCategoria.isEmpty() ){
                    if( !textoDescricao.isEmpty() ){
                        return true;
                    }
                    else{
                        Toast.makeText(  DespesasActivityController.this,
                                "Informe a descrição da Despesa!",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                else{
                    Toast.makeText(  DespesasActivityController.this,
                            "Informe a categoria da Despesa!",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            else{
                Toast.makeText(  DespesasActivityController.this,
                        "Informe a data da Despesa!",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        else{
            Toast.makeText(  DespesasActivityController.this,
                    "Informe o valor da Despesa!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //método para recuperar a despesa
    public void recuperarDespesaTotal(){
        //acessando o nó de usuários para incrementar as despesas
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

                //recuperando o atributo despesaTotal
                despesaTotal = usuario.getDespesaTotal();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void atualizarDespesa( Double despesa){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        DatabaseReference usuarioRef = firebaseRef.child("usuarios")
                                                  .child( idUsuario );

        //atualizando o atributo despesaTotal do nó "usuarios"
        usuarioRef.child("despesaTotal").setValue( despesa );
    }


    public void recuperarDadosDespesa(){

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

        validarCamposDespesa();
    }

    public void confirmarEdicao( ){

        mesAnoSelecionado = getIntent().getStringExtra("mesAnoSelecionado");
        key = getIntent().getStringExtra("key");
        
        //novos valores
        String novaData = campoData.getText().toString();
        String novaCategoria = campoCategoria.getText().toString();
        String novaDescricao = campoDescricao.getText().toString();
        double novoValor = Double.parseDouble(campoValor.getText().toString());

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario ); //usuario Ok


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
