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
import com.example.growmoneyapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;


public class LoginActivityController extends AppCompatActivity {

    private EditText campoEmail, campoSenha;
    private Button botaoEntrar;
    private Usuario usuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);
        botaoEntrar = findViewById(R.id.buttonLogin);

        botaoEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validações
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                if (!textoEmail.isEmpty()) {
                    if (!textoSenha.isEmpty()) {
                        //autenticação de usuário
                        usuario = new Usuario();
                        usuario.setEmail( textoEmail );
                        usuario.setSenha( textoSenha );
                        validarLogin();

                    } else {
                        Toast.makeText(LoginActivityController.this,
                                "Preencha a Senha",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivityController.this,
                            "Preencha o Email",
                            Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    //método de validação
    public void validarLogin(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if( task.isSuccessful() ){
                    //redirecionar para a tela principal
                    abrirTelaPrincipal();
                }
                else{

                    String excecao = "";
                    try{
                        //retorna a exceção
                        throw task.getException();
                    }
                    catch( FirebaseAuthInvalidUserException e ){
                        //usuário inválido ou desabilitado
                        excecao = "Usuário não cadastrado.";
                    }
                    catch ( FirebaseAuthInvalidCredentialsException e ){
                        //usuáio e senha não conferem
                        excecao = "E-mail e senha não correspondem a um usuário cadastrado.";
                    }
                    catch( Exception e){
                        excecao = "Erro ao realizar o login: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText( LoginActivityController.this,
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
        finish();
    }
}
