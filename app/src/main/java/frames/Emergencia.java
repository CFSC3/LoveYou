package frames;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.a0411.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class Emergencia extends Fragment {

    private Button aleatorio, enviarLigar;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference vales;
    private EditText textVales;
    private Random random = new Random();
    private android.content.BroadcastReceiver networkCallback;
    private boolean esperandoInternet = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_emergencia, container, false);

        aleatorio = view.findViewById(R.id.buttonAleatorio);
        enviarLigar = view.findViewById(R.id.buttonAcao);
        textVales = view.findViewById(R.id.textVales);

        vales = databaseReference.child("vales");

        //verifica se o celular está conectado a internet.
        statusConexao();

        statusInternet();

            aleatorio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (conexao.Internet.temInternet(requireContext())) {
                        aleatorios();
                    } else {
                        // Se está sem internet, mostra o aviso
                        Toast.makeText(getContext(),
                                "Conecte a internet!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

        enviarLigar();

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
                                aleatorios();

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

            aleatorios();

        } else {
            // Se está sem internet, mostra o aviso
            Toast.makeText(getContext(),
                    "Você está sem internet!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void aleatorios(){

        //addListenerForSingleValueEvent para não deixar o app funcionando sem necessidade.
        vales.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalVales = snapshot.getChildrenCount(); // Conta dinamicamente

                if (totalVales > 0) {
                    int i = random.nextInt((int) totalVales);

                    // Previne crash caso o número sorteado tenha sido apagado do banco
                    if (snapshot.hasChild(String.valueOf(i))) {
                        textVales.setText(snapshot.child(String.valueOf(i)).getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log de erro
                Log.e("Firebase", "Erro ao carregar vales: " + error.getMessage());
            }
        });
        }

    public void enviarLigar(){

        enviarLigar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conexao.Internet.temInternet(requireContext())) {
                    telegram();
                } else {
                    // Se está sem internet, mostra o aviso
                    Toast.makeText(getContext(),
                            "Conecte a internet!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        enviarLigar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Toast.makeText(getActivity(), "Esse programador te ama muito!!!", Toast.LENGTH_LONG)
                        .show();
                return true;
            }
        });
    }

    private void whatssap(){
        try {
            // Mensagem com os emojis
            String mensagemComEmojis = "☀️ " + textVales.getText().toString() + " 🌙";

            // Uri.encode na mensagem para garantir que os emojis não quebrem o link
            Uri whatssap = Uri.parse("https://api.whatsapp.com/send?phone=55DDD9XXXXXXXX&text=" + Uri.encode(mensagemComEmojis));
            Intent whatssapIntent = new Intent(Intent.ACTION_VIEW, whatssap);
            startActivity(whatssapIntent);

        } catch (android.content.ActivityNotFoundException e) {
            // Proteção: Se o WhatsApp não estiver instalado
            android.widget.Toast.makeText(getContext(), "O WhatsApp não está instalado!", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void telegram(){
        try {
            // Mensagem com os emojis
            String mensagemComEmojis = "☀️ " + textVales.getText().toString() + " 🌙";

            // Tenta abrir o Telegram com o texto formatado e protegido
            Uri telegram = Uri.parse("https://t.me/Luz_Nullings?text=" + Uri.encode(mensagemComEmojis));
            Intent telegramIntent = new Intent(Intent.ACTION_VIEW, telegram);
            startActivity(telegramIntent);

        } catch (android.content.ActivityNotFoundException e) {
            // Proteção: Se o Telegram não estiver instalado
            android.widget.Toast.makeText(getContext(), "O Telegram não está instalado!", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Desliga o monitoramento usando a nossa classe utilitária centralizada
        conexao.Internet.pararMonitoramento(requireContext(), networkCallback);
    }
}