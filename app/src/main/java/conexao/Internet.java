package conexao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Internet {
    //A Interface: O "contrato" que avisa as outras telas
    // Adiciona o aviso de quando a internet cair
    public interface RedeListener {
        void onInternetRestaurada();
        void onInternetPerdida();
    }

    //Métodoo checa a internet na hora
    public static boolean temInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }
        return false;
    }

    // Criamos o Interceptador de Sistema (BroadcastReceiver)
    public static BroadcastReceiver monitorarConexao(Context context, RedeListener listener) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Toda vez que o celular liga ou desliga qualquer rede, isso aqui roda.
                // Aí nós checamos: tem internet agora?
                if (temInternet(context)) {
                    if (listener != null) {
                        listener.onInternetRestaurada();
                    }
                } else {
                    if (listener != null) {
                        listener.onInternetPerdida();
                    }
                }
            }
        };

        //Registra o interceptador no contexto da tela
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, filter);

        return receiver;
    }

    //Método para desligar o interceptador ao sair da tela
    public static void pararMonitoramento(Context context, BroadcastReceiver receiver) {
        if (receiver != null && context != null) {
            try {
                context.unregisterReceiver(receiver);
            } catch (Exception e) {
                // Ignora se o Android já tiver matado o processo
            }
        }
    }
}