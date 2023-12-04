package com.example.growmoneyapp.config;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfiguracaoFirebase {
    //atributo e método estático - serão os mesmos independentemente da quantidade de instâncias
    private static FirebaseAuth autenticacao;
    private static DatabaseReference firebase;

    //criando o método que retorna a instância da autenticação
    public static FirebaseAuth getFirebaseAutenticacao(){
        if(autenticacao == null){
            autenticacao = FirebaseAuth.getInstance();
        }
        return autenticacao;
    }

    //retorna a instância do Firebase Database
    public static DatabaseReference getFirebaseDatabase(){
        if( firebase == null) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            firebase = FirebaseDatabase.getInstance().getReference();
        }
        return firebase;
    }
}
