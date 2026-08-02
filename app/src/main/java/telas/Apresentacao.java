package telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.WindowManager;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a0411.R;

public class Apresentacao extends AppCompatActivity {

    private ConstraintLayout layoutPrincipal;
    private ImageView imageMusica, imageVale, imageMapa;

    private Button next;
    private int pass = 0;
    private TextView apre1, apre2;

    private int corAzul = Color.parseColor("#3399FF");
    private int corBranca = Color.parseColor("#FFFFFF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apresentacao);

        layoutPrincipal = findViewById(R.id.layoutPrincipal);
        next = findViewById(R.id.buttonNext);
        apre1 = findViewById(R.id.textViewA1);
        apre2 = findViewById(R.id.textViewA2);

        imageMusica = findViewById(R.id.imageMusica);
        imageVale = findViewById(R.id.imageVale);
        imageMapa = findViewById(R.id.imageMapa);

        // Oculta a barra de notificação
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        boasVindas();

    }

    private void boasVindas(){

        layoutPrincipal.setBackgroundColor(corAzul);
        apre1.setTextColor(corBranca);
        apre2.setTextColor(corBranca);

        apre1.setText("Bem-vindo ao\nLoveYou");
        apre2.setText("Criado para resgatar boas lembranças e construir novos momentos inesquecíveis. " +
                "Explore, divirta-se e deixe o app sortear o lugar ideal para o próximo passeio.");

        mudarCorIcone(imageMusica, corBranca);
        mudarCorIcone(imageVale, corBranca);
        mudarCorIcone(imageMapa, corBranca);
        mudarCorBotao(corBranca);

    }

    public void passarApresentacao(View view){

        pass++;

        if(pass == 1){
            // Tela 2: Sons e Sentimentos (Fundo Branco, Letras e Ícones Azuis)

            layoutPrincipal.setBackgroundColor(corBranca);

            apre1.setTextColor(corAzul);
            apre2.setTextColor(corAzul);
            apre1.setText("Sons e Sentimentos");
            apre2.setText("Dê play nas suas emoções. Um espaço dedicado às suas músicas favoritas, " +
                    "feito para te trazer paz, alegria e mensagens muito especiais.");

            // Lógica de Visibilidade
            imageMusica.setVisibility(View.VISIBLE);
            imageVale.setVisibility(View.INVISIBLE);
            imageMapa.setVisibility(View.INVISIBLE);

            mudarCorIcone(imageMusica, corAzul);
            mudarCorBotao(corAzul);

        } else if(pass == 2){
            // Tela 3: Tickets Especiais (Fundo Azul, Letras e Ícones Brancos)

            layoutPrincipal.setBackgroundColor(corAzul);

            apre1.setTextColor(corBranca);
            apre2.setTextColor(corBranca);
            apre1.setText("Tickets Especiais");
            apre2.setText("Um cantinho cheio de mimos pensado só para você. Crie o seu " +
                    "próprio ou sorteie um vale surpresa para usar quando quiser!");

            // Lógica de Visibilidade
            imageMusica.setVisibility(View.INVISIBLE);
            imageVale.setVisibility(View.VISIBLE);
            imageMapa.setVisibility(View.INVISIBLE);

            mudarCorIcone(imageVale, corBranca);
            mudarCorBotao(corBranca);

        } else if(pass == 3){
            // Tela 4: Lembranças e Momentos (Fundo Branco, Letras e Ícones Azuis)

            layoutPrincipal.setBackgroundColor(corBranca);

            apre1.setTextColor(corAzul);
            apre2.setTextColor(corAzul);
            apre1.setText("Lembranças e Momentos");
            apre2.setText("Onde nossa próxima lembrança vai acontecer? Navegue pelo mapa para revisitar lugares marcantes ou descobrir " +
                    "novas aventuras, e se bater a indecisão, use o botão aleatório e deixe o aplicativo escolher o nosso destino.");

            // Lógica de Visibilidade
            imageMusica.setVisibility(View.INVISIBLE);
            imageVale.setVisibility(View.INVISIBLE);
            imageMapa.setVisibility(View.VISIBLE);

            mudarCorIcone(imageMapa, corAzul);
            mudarCorBotao(corAzul);

        } else {
            // Finaliza apresentação e vai para TelaInicio
            Intent intent = new Intent(Apresentacao.this, TelaInicio.class);
            startActivity(intent);
            finish();
        }
    }

    // Função auxiliar para mudar a cor dos ícones de forma limpa
    private void mudarCorIcone(ImageView imageView, int cor) {
        imageView.setColorFilter(cor, PorterDuff.Mode.SRC_IN);
    }

    // Função auxiliar para mudar a cor do botão Next
    private void mudarCorBotao(int cor) {
        next.setBackgroundTintList(ColorStateList.valueOf(cor));
    }
}