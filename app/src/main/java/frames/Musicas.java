package frames;

import android.os.Bundle;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.a0411.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class Musicas extends Fragment implements Runnable {

    private ImageView botaoPlayPause;
    private TextView tempoMusica;
    private Button anterior;
    private Button proximo;
    private SeekBar progressoMusica;
    private ExoPlayer audioPlayer;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private TextView tituloMusica;
    private TextView frases;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference frase;
    private int atual = 0;
    private Random aleatorioMusic = new Random();
    private int back = -1;
    private ArrayList<Integer> antes = new ArrayList<>();
    private TextView tituloTolbar;
    private Toolbar principalToolbar;
    private BottomNavigationView navegação;
    private android.content.BroadcastReceiver networkCallback;
    private boolean esperandoInternet = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_musicas, container, false);

        botaoPlayPause = view.findViewById(R.id.botaoPlayPause);
        tempoMusica = view.findViewById(R.id.tempoMusica);
        anterior = view.findViewById(R.id.anterior);
        proximo = view.findViewById(R.id.proximo);
        tituloMusica = view.findViewById(R.id.tituloMusica);
        playerView = view.findViewById(R.id.videoViewGif);

        tituloTolbar = requireActivity().findViewById(R.id.tituloToolbar);
        principalToolbar = requireActivity().findViewById(R.id.toolbarPrincipal);
        navegação = requireActivity().findViewById(R.id.bottomNavigationView);

        frases = view.findViewById(R.id.frases);
        frase = databaseReference.child("frases");
        progressoMusica = view.findViewById(R.id.progressoMusica);

        //Configura a música e os gifs
        confExoPlayer();

        //verifica se o celular está conectado a internet.
        statusConexao();
        //Liga o monitor passando o que ele deve fazer quando a internet voltar ou cair
        statusInternet();

        progressoMusica.setEnabled(false);
        new Thread(this).start();
        seek();
        controleMusica();
        ocultarBarras();
        return view;
    }

    private void statusInternet(){
        //Liga o monitor passando o que ele deve fazer quando a internet voltar
        networkCallback = conexao.Internet.monitorarConexao(requireContext(), new conexao.Internet.RedeListener() {
            @Override
            public void onInternetRestaurada() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Só recarrega se o app estava travado esperando internet
                            if (esperandoInternet) {
                                esperandoInternet = false;

                                android.widget.Toast.makeText(getContext(),
                                        "Conexão restaurada!",
                                        android.widget.Toast.LENGTH_SHORT).show();

                                tocarMusicaAleatorio();
                                gifBaixar();
                                frases();

                                // Garante que a barra de progresso comece a andar
                                new Thread(Musicas.this).start();
                            }
                        }
                    });
                }
            }

            @Override
            public void onInternetPerdida() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Se a internet cair com o app aberto, ele ativa a trava de segurança
                            esperandoInternet = true;
                            android.widget.Toast.makeText(getContext(),
                                    "Conexão perdida!",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    private void statusConexao(){
        //verifica se o celular está conectado a internet.
        if (conexao.Internet.temInternet(requireContext())) {
            tocarMusicaAleatorio();
            gifBaixar();
            frases();
        } else {
            // Sem internet! Mostra o aviso.
            android.widget.Toast.makeText(getContext(),
                    "Falha na conexâo!",
                    android.widget.Toast.LENGTH_LONG).show();

            botaoPlayPause.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);

            tituloMusica.setText("Sem conexão com a internet");
        }
    }

    private void confExoPlayer(){
        if (getActivity() != null) {
            // Cria o motor do ExoPlayer
            exoPlayer = new ExoPlayer.Builder(getActivity()).build();
            playerView.setPlayer(exoPlayer);

            // Configura o vídeo para repetir continuamente (looping)
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

            // Tira completamente o som do ExoPlayer
            exoPlayer.setVolume(0f);

            audioPlayer = new ExoPlayer.Builder(getActivity()).build();

            // Ouve o momento exato em que a música termina para pular para a próxima
            audioPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_ENDED) {
                        gifBaixar();
                        frases();
                        tocarMusicaAleatorio();
                    }

                    // Quando a música CARREGA da internet e está pronta para tocar
                    else if (playbackState == Player.STATE_READY) {
                        long tempoTotal = audioPlayer.getDuration();

                        // Trava de segurança para garantir que a leitura foi feita
                        if (tempoTotal > 0) {
                            progressoMusica.setMax((int) tempoTotal);
                        }
                    }
                }
            });
        }
    }

    private void ocultarBarras() {
        navegação.setVisibility(View.INVISIBLE);
        principalToolbar.setVisibility(View.INVISIBLE);
        tituloTolbar.setVisibility(View.INVISIBLE);
        // Oculta a barra de notificações do Android
        requireActivity().getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void seek() {
        progressoMusica.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    if (audioPlayer.isPlaying() || audioPlayer != null) {
                        if (fromUser)
                            audioPlayer.seekTo(progress);
                    } else if (audioPlayer == null) {
                        Toast.makeText(getActivity(), "Media is not running",
                                Toast.LENGTH_SHORT).show();
                        seekBar.setProgress(0);
                    }
                } catch (Exception e) {
                    Log.e("seek bar", "" + e);
                    seekBar.setEnabled(false);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);

                if (getActivity() != null) {
                    // Pula para a Thread Principal ANTES de falar com o ExoPlayer
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                // Agora sim, em terreno seguro, fazemos as perguntas:
                                if (audioPlayer != null && audioPlayer.isPlaying()) {
                                    long totalLong = audioPlayer.getDuration();

                                    if (totalLong > 0) {
                                        int atual = (int) audioPlayer.getCurrentPosition();
                                        int total = (int) totalLong;

                                        progressoMusica.setMax(total);
                                        progressoMusica.setProgress(atual);
                                        tempoMusica.setText(formatarTempo(atual) + " / " + formatarTempo(total));
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("ThreadMusica", "Erro ao atualizar UI: " + e.getMessage());
                            }

                        }
                    });
                }
            } catch (InterruptedException e) {
                // Se a thread for interrompida ao fechar o app, sai do loop com segurança
                return;
            }
        }
    }

    private String formatarTempo(int milissegundos) {
        int segundos = (milissegundos / 1000) % 60;
        int minutos = (milissegundos / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    public void controleMusica(){

        botaoPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (audioPlayer != null) {
                    if (audioPlayer.isPlaying()) {
                        audioPlayer.pause();
                        botaoPlayPause.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    } else {
                        audioPlayer.play();
                        botaoPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                    }
                }
            }
        });

        anterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (conexao.Internet.temInternet(requireContext())) {
                    // Se tiver mais de uma música no histórico
                    if (antes.size() > 1) {
                        antes.remove(antes.size() - 1); // Remove a atual
                        atual = antes.get(antes.size() - 1); // Descobre a anterior

                        botaoPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);

                        carregarMusica(atual); // Toca a música certa do passado
                        gifBaixar();
                        frases();
                    } else {
                        Toast.makeText(getContext(), "Não há músicas anteriores", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(),
                            "Conecte a internet!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        proximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conexao.Internet.temInternet(requireContext())) {
                    tocarMusicaAleatorio(); // Gera uma nova
                    gifBaixar();
                    frases();
                    botaoPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                } else{
                Toast.makeText(getContext(),
                        "Conecte a internet!",
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void tocarMusicaAleatorio() {

        DatabaseReference musicasRef = databaseReference.child("links_musicas");

        musicasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalMusicas = snapshot.getChildrenCount();

                if (totalMusicas > 0) {
                    int novaMusica;
                    do {
                        // Sorteia de 1 até o total de músicas dinamicamente
                        novaMusica = aleatorioMusic.nextInt((int) totalMusicas) + 1;
                    } while (novaMusica == atual && totalMusicas > 1); // Evita repetir se houver mais de 1 música

                    atual = novaMusica;
                    antes.add(atual);

                    carregarMusica(atual);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Erro ao contar músicas: " + error.getMessage());
            }
        });
    }

    private void carregarMusica(int id) {
        // Para a música anterior antes de carregar a nova
        if (audioPlayer != null && audioPlayer.isPlaying()) {
            audioPlayer.stop();
        }

        // Aponta para o ID da música sorteada no banco de dados
        DatabaseReference musicaRef = databaseReference.child("links_musicas").child(String.valueOf(id));

        musicaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && getActivity() != null) {
                    // Puxa as informações da nuvem
                    String link = snapshot.child("link").getValue(String.class);
                    String nome = snapshot.child("nome").getValue(String.class);

                    if (link != null && nome != null) {
                        tituloMusica.setText(nome);

                        // Entrega o link do GitHub para o ExoPlayer tocar
                        MediaItem mediaItem = MediaItem.fromUri(link);
                        audioPlayer.setMediaItem(mediaItem);
                        audioPlayer.prepare();
                        audioPlayer.play();

                        botaoPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Erro ao ler música: " + error.getMessage());
            }
        });
    }

    public void gifBaixar(){

        DatabaseReference linksGifsRef = databaseReference.child("links_gifs");

        linksGifsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalGifs = snapshot.getChildrenCount();

                if (totalGifs > 0 && getActivity() != null) {
                    int gifA = aleatorioMusic.nextInt((int) totalGifs);

                    DataSnapshot gifSnapshot = snapshot.child(String.valueOf(gifA));
                    if (gifSnapshot.exists()) {
                        String urlVideo = gifSnapshot.getValue(String.class);

                        if (urlVideo != null && exoPlayer != null) {
                            MediaItem mediaItem = MediaItem.fromUri(urlVideo);
                            exoPlayer.setMediaItem(mediaItem);
                            exoPlayer.prepare();
                            exoPlayer.play();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Erro ao ler banco de dados: " + error.getMessage());
            }
        });

    }

    public void frases() {

        if (atual > 0) {
            frase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long totalFrases = snapshot.getChildrenCount();

                    if (totalFrases > 0) {
                        int i = aleatorioMusic.nextInt((int) totalFrases);

                        if (snapshot.hasChild("frase" + i)) {
                            frases.setText(snapshot.child("frase" + i).getValue().toString());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Erro ao carregar frase: " + error.getMessage());
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Apenas pausa a música, mantendo o motor vivo para quando o usuário voltar
        if (audioPlayer != null && audioPlayer.isPlaying()) {
            audioPlayer.pause();

        }

        // Pausa o vídeo também para economizar bateria em segundo plano
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // 1. Retoma a música exatamente de onde parou
        if (audioPlayer != null && !audioPlayer.isPlaying()) {
            try {
                audioPlayer.play();
                botaoPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
            } catch (IllegalStateException e) {
                Log.e("MusicasApp", "Erro ao retomar música no onResume: " + e.getMessage());
            }
        }

        // 2. Acorda o motor de vídeo e manda a animação voltar a rodar
        if (exoPlayer != null && !exoPlayer.isPlaying()) {
            exoPlayer.play();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Faz as barras voltarem a aparecer para os outros Fragments usarem
        if (navegação != null) {
            navegação.setVisibility(View.VISIBLE);
        }
        if (principalToolbar != null) {
            principalToolbar.setVisibility(View.VISIBLE);
        }
        if (tituloTolbar != null) {
            tituloTolbar.setVisibility(View.VISIBLE);
        }

        // Restaura a barra de notificações do Android
        requireActivity().getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Para e libera o vídeo
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }

        if (audioPlayer != null) {
            audioPlayer.stop();
            audioPlayer.release();
            audioPlayer = null;
        }

        // Desliga o monitoramento usando a nossa classe utilitária centralizada
        conexao.Internet.pararMonitoramento(requireContext(), networkCallback);
    }
}