package com.example.growmoneyapp;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import com.example.growmoneyapp.config.ConfiguracaoFirebase;
import com.example.growmoneyapp.controller.CadastroActivityController;
import com.example.growmoneyapp.controller.LoginActivityController;
import com.example.growmoneyapp.controller.PrincipalActivityController;
import com.google.firebase.auth.FirebaseAuth;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;

public class MainActivity extends IntroActivity {

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        setButtonBackVisible(false);
        setButtonNextVisible(false);

        addSlide( new FragmentSlide.Builder()
                        .background(R.color.bg_green)
                        .fragment(R.layout.intro_1)
                        .build());

        addSlide( new FragmentSlide.Builder()
                        .background(R.color.bg_green)
                        .fragment(R.layout.intro_2)
                        .build());

        addSlide( new FragmentSlide.Builder()
                        .background(R.color.bg_green)
                        .fragment(R.layout.intro_3)
                        .build());

        addSlide( new FragmentSlide.Builder()
                        .background(R.color.bg_green)
                        .fragment(R.layout.intro_4)
                        .build());

        addSlide( new FragmentSlide.Builder()
                .background(R.color.bg_light_gray)
                .fragment(R.layout.cadastro)
                .canGoForward(false)
                .build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificarUsuarioLogado();
    }

    //Login
    public void btEntrar(View view){
        startActivity(new Intent(this, LoginActivityController.class));
    }

    public void btCadastrar (View view){
        startActivity(new Intent(this, CadastroActivityController.class));
    }

    public void verificarUsuarioLogado(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        //verifica se existe usuário logado
        if( autenticacao.getCurrentUser() != null){
            abrirTelaPrincipal();
        }
    }

    public void abrirTelaPrincipal(){
        startActivity( new Intent(
                this,
                PrincipalActivityController.class
        ));
    }
}
