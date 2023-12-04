package com.example.growmoneyapp.controller;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.growmoneyapp.R;
import com.example.growmoneyapp.config.ConfiguracaoFirebase;
import com.example.growmoneyapp.helper.Base64Custom;
import com.example.growmoneyapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;


public class CadastroActivityController extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha; //campos do formulário de cadastro
    private Button botaoCadastrar;
    private FirebaseAuth autenticacao;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);


        campoNome = findViewById(R.id.editNome);
        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
        botaoCadastrar = findViewById(R.id.buttonCadastrar);

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                if (!textoNome.isEmpty()) {
                    if (!textoEmail.isEmpty()) {
                        if (!textoSenha.isEmpty()) {
                            usuario = new Usuario();
                            usuario.setNome( textoNome );
                            usuario.setEmail( textoEmail);
                            usuario.setSenha( textoSenha);
                            cadastrarUsuario();
                        } else {
                            Toast.makeText(CadastroActivityController.this,
                                    "Informe uma Senha",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CadastroActivityController.this,
                                "Informe um Email",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CadastroActivityController.this,
                            "Preencha o campo Nome",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void cadastrarUsuario(){
        //recuperando a instância do Firebase que permite cadastrar o usuário
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                     usuario.getEmail(),
                     usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if( task.isSuccessful() ){
                    //salvando dados usuario
                    //email Base64
                    String idUsuario = Base64Custom.codificarBase64( usuario.getEmail() );
                    usuario.setIdUsuario( idUsuario );
                    usuario.salvar();
                    finish();
                }
                else{
                    String excecao = "";
                    try{
                        //retorna a exceção
                        throw task.getException();
                    }
                    catch( FirebaseAuthWeakPasswordException e ){
                        //senha fraca
                        excecao = "Digite uma senha mais forte!";
                    }
                    catch( FirebaseAuthInvalidCredentialsException e ){
                        //email em formato inválido
                        excecao = "Por favor, digite um e-mail válido!";
                    }
                    catch( FirebaseAuthUserCollisionException e){
                        //usuario tenta cadastrar um email que já existe no Firebase
                        excecao = "Esta conta já foi cadastrada! Por favor, verifique.";
                    }
                    catch( Exception e){
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivityController.this,
                            excecao,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void abrirTelaPrincipal(){
        startActivity( new Intent(
                this,
                PrincipalActivityController.class
        ));
    }
}
