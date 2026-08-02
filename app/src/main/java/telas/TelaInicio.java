package telas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a0411.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import frames.Emergencia;
import frames.MapsFragment;
import frames.Video;

public class TelaInicio extends AppCompatActivity {

    private Video video;
    private Emergencia emergencia;
    private BottomNavigationView navegacao;
    private TextView tituloToolbar;
    private ImageView botaoFiltro, botaoDecisao;
    private int estadoFiltro = 1; // 1 = Geral, 2 = Passeio, 3 = Restaurante
    private Bundle dados = new Bundle();
    private MapsFragment mapsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_inicio);

        navegacao = findViewById(R.id.bottomNavigationView);

        tituloToolbar = findViewById(R.id.tituloToolbar);
        botaoFiltro = findViewById(R.id.botaoFiltro);
        botaoDecisao = findViewById(R.id.imageViewDecisao);

        // Estado inicial da tela (Música)
        tituloToolbar.setText("Sons e Sentimentos");
        botaoFiltro.setVisibility(View.INVISIBLE);
        botaoDecisao.setVisibility(View.INVISIBLE);

        video = new Video();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.passeDeFrames, video );
        fragmentTransaction.commit();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(getColor(R.color.notificacao));
        }

        if(!conexao.Internet.temInternet(this))
            Toast.makeText(this,
                    "Você está sem internet!",
                    Toast.LENGTH_SHORT).show();

        fragmen();
        botaoMapa();
        configurarBotaoDecisao();
    }

    public void fragmen(){

        navegacao.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if(id == R.id.musica){

                        video = new Video();
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.passeDeFrames, video );
                        fragmentTransaction.commit();

                        tituloToolbar.setText("Sons e Sentimentos");
                        botaoFiltro.setVisibility(View.INVISIBLE);
                        botaoDecisao.setVisibility(View.INVISIBLE);

                }else if(id == R.id.emergencia) {

                    emergencia = new Emergencia();
                    FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction2.replace(R.id.passeDeFrames, emergencia);
                    fragmentTransaction2.commit();

                    tituloToolbar.setText("Tickets");
                    botaoFiltro.setVisibility(View.INVISIBLE);
                    botaoDecisao.setVisibility(View.INVISIBLE);

                }else if(id == R.id.mapa) {

                    MapsFragment mapa = new MapsFragment();
                    FragmentTransaction fragmentTransaction3 = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction3.replace(R.id.passeDeFrames, mapa);
                    fragmentTransaction3.commit();

                    tituloToolbar.setText("Lembranças e Momentos");

                    botaoFiltro.setVisibility(View.VISIBLE);
                    botaoDecisao.setVisibility(View.VISIBLE);
                    botaoFiltro.setBackgroundResource(R.drawable.ic_maps);
                    estadoFiltro = 1;

                }else{
                        Toast.makeText(TelaInicio.this, "Algo deu errado!", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });
    }

    public void botaoMapa() {
        botaoFiltro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapsFragment = new MapsFragment();
                dados = new Bundle(); // Limpa os dados anteriores

                // Verifica qual é o filtro atual e avança para o próximo
                if (estadoFiltro == 1) {
                    // Estava no Geral, muda para Passeio
                    botaoFiltro.setBackgroundResource(R.drawable.ic_pass);
                    dados.putInt("dados", 1);
                    estadoFiltro = 2; // Atualiza o estado

                } else if (estadoFiltro == 2) {
                    // Estava no Passeio, muda para Restaurante
                    botaoFiltro.setBackgroundResource(R.drawable.ic_res);
                    dados.putInt("dados", 2);
                    estadoFiltro = 3; // Atualiza o estado

                } else {
                    // Estava no Restaurante, volta para Geral
                    botaoFiltro.setBackgroundResource(R.drawable.ic_maps);
                    dados.putInt("dados", 3);
                    estadoFiltro = 1; // Volta para o início
                }

                // Envia os dados e recarrega o fragmento do mapa
                mapsFragment.setArguments(dados);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.passeDeFrames, mapsFragment);
                fragmentTransaction.commit();
            }
        });
    }

    public void configurarBotaoDecisao() {
        botaoDecisao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Procura o fragmento do mapa que está atualmente aberto na tela
                androidx.fragment.app.Fragment fragmentAtual = getSupportFragmentManager().findFragmentById(R.id.passeDeFrames);

                // Se for realmente o mapa, nós chamamos o método de sorteio passando o filtro atual
                if (fragmentAtual instanceof MapsFragment) {
                    ((MapsFragment) fragmentAtual).realizarSorteio(estadoFiltro);
                }
            }
        });
    }
}