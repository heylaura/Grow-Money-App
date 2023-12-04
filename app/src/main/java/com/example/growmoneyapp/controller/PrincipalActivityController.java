package com.example.growmoneyapp.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.ReceiverCallNotAllowedException;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.example.growmoneyapp.MainActivity;
import com.example.growmoneyapp.R;
import com.example.growmoneyapp.adapter.AdapterMovimentacao;
import com.example.growmoneyapp.config.ConfiguracaoFirebase;
import com.example.growmoneyapp.helper.Base64Custom;
import com.example.growmoneyapp.model.Movimentacao;
import com.example.growmoneyapp.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;



public class PrincipalActivityController extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView textoSaudacao, textoSaldo;
    private Double despesaTotal = 0.0;
    private Double receitaTotal = 0.0;
    private Double resumoUsuario = 0.0;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerUsuario;
    private ValueEventListener valueEventListenerMovimentacoes;
    private RecyclerView recyclerView;
    private List<Movimentacao> movimentacoes = new ArrayList<>();
    private DatabaseReference movimentacaoRef;
    private String mesAnoSelecionado;
    private AdapterMovimentacao adapterMovimentacao;
    private Movimentacao movimentacao;
    private TextInputEditText campoData, campoCategoria, campoDescricao;
    private EditText campoValor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        textoSaldo = findViewById(R.id.textSaldo);
        textoSaudacao = findViewById(R.id.textSaudacao);
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerMovimentos);
        configuraCalendarView();
        swipe();

        //configurando adapter
        adapterMovimentacao = new AdapterMovimentacao(movimentacoes, this);

        //configurando recycler view
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager( layoutManager );
        recyclerView.setHasFixedSize( true );
        recyclerView.setAdapter( adapterMovimentacao );
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


    //recuperando as movimentações
    public void recuperarMovimentacoes(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        movimentacaoRef = firebaseRef.child("movimentacao")
                                       .child(idUsuario)
                                       .child( mesAnoSelecionado );

        valueEventListenerMovimentacoes = movimentacaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                movimentacoes.clear();

                //percorrendo as movimentações
                for( DataSnapshot dados : dataSnapshot.getChildren() ){
                    Movimentacao movimentacao = dados.getValue( Movimentacao.class );
                    movimentacao.setKey( dados.getKey() );
                    movimentacoes.add( movimentacao );
                }

                adapterMovimentacao.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //recuperando o resumo das movimentações do usuário logado
    public void recuperarResumo(){

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64( emailUsuario );
        usuarioRef = firebaseRef.child("usuarios")
                .child( idUsuario );

        valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                despesaTotal = usuario.getDespesaTotal();
                receitaTotal = usuario.getReceitaTotal();
                resumoUsuario = receitaTotal - despesaTotal;

                //formatando para exibir somete as duas últimas casas decimais
                DecimalFormat decimalFormat = new DecimalFormat("0.##");
                String resultadoFormatado = decimalFormat.format( resumoUsuario );

                //exibindo os resultados na tela
                textoSaudacao.setText("Olá, " + usuario.getNome());
                textoSaldo.setText( "R$ " + resultadoFormatado);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //criando o método para Delete
    public void swipe(){

        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.ACTION_STATE_IDLE; // drag and drop inativo
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                //excluindo a movimentação
                excluirMovimentacao( viewHolder );
            }
        };

        //adicionando o swipe ao RecyclerView
        new ItemTouchHelper(itemTouch).attachToRecyclerView(recyclerView);
    }

    public void excluirMovimentacao(final RecyclerView.ViewHolder viewHolder ){

        //recuperando a posição do item da lista para exclui-lo
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( this );
        alertDialog.setMessage("Você tem certeza que deseja excluir essa movimentação?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //realizando a exclusão
                int position = viewHolder.getAdapterPosition();
                movimentacao = movimentacoes.get( position );

                String emailUsuario = autenticacao.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64( emailUsuario );
                movimentacaoRef = firebaseRef.child("movimentacao")
                        .child(idUsuario)
                        .child( mesAnoSelecionado );
                movimentacaoRef.child( movimentacao.getKey() ).removeValue();
                adapterMovimentacao.notifyItemRemoved( position );
                atualizarSaldo();

            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                adapterMovimentacao.notifyDataSetChanged();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();

    }


    public void editarMovimentacao(final RecyclerView.ViewHolder viewHolder ){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( this );
        alertDialog.setMessage("Deseja editar essa movimentação?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = viewHolder.getAdapterPosition();
                movimentacao = movimentacoes.get(position);

                if (movimentacao.getTipo() == "d" || movimentacao.getTipo().equals("d")) {
                    Intent intent = new Intent(PrincipalActivityController.this, DespesasActivityController.class);
                    intent.putExtra("editar", true);  // Indicador de edição
                    intent.putExtra("key", movimentacao.getKey());
                    intent.putExtra("data", movimentacao.getData());
                    intent.putExtra("categoria", movimentacao.getCategoria());
                    intent.putExtra("descricao", movimentacao.getDescricao());
                    intent.putExtra("valor", movimentacao.getValor());
                    intent.putExtra("mesAnoSelecionado", mesAnoSelecionado);
                    startActivity(intent);
                }

                if (movimentacao.getTipo() == "r" || movimentacao.getTipo().equals("r")) {
                    Intent intent = new Intent(PrincipalActivityController.this, ReceitasActivityController.class);
                    intent.putExtra("editar", true);  // Indicador de edição
                    intent.putExtra("key", movimentacao.getKey());
                    intent.putExtra("data", movimentacao.getData());
                    intent.putExtra("categoria", movimentacao.getCategoria());
                    intent.putExtra("descricao", movimentacao.getDescricao());
                    intent.putExtra("valor", movimentacao.getValor());
                    intent.putExtra("mesAnoSelecionado", mesAnoSelecionado);
                    startActivity(intent);
                }
            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                adapterMovimentacao.notifyDataSetChanged();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //verificação de menus
        switch (item.getItemId()){
            case R.id.menuSair :
                autenticacao.signOut(); //deslogando o usuário
                startActivity(new Intent(this, MainActivity.class)); //redirecionando para o slider
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void adicionarDespesa(View view){
        startActivity( new Intent(this, DespesasActivityController.class));
    }

    public void adicionarReceita(View view){
        startActivity( new Intent(this, ReceitasActivityController.class));
    }

    public void configuraCalendarView(){

        //definindo um array de meses
        CharSequence meses[] = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        calendarView.setTitleMonths( meses );

        CalendarDay dataAtual = calendarView.getCurrentDate();
        String mesSelecionado = String.format("%02d", (dataAtual.getMonth() + 1));
        mesAnoSelecionado = String.valueOf((mesSelecionado + "" + dataAtual.getYear()));
        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                String mesSelecionado = String.format("%02d", (date.getMonth() + 1));
                mesAnoSelecionado = String.valueOf(mesSelecionado + "" + date.getYear());
                movimentacaoRef.removeEventListener( valueEventListenerMovimentacoes );
                recuperarMovimentacoes();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarResumo();
        recuperarMovimentacoes();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioRef.removeEventListener( valueEventListenerUsuario );
        movimentacaoRef.removeEventListener( valueEventListenerMovimentacoes );
    }
}
