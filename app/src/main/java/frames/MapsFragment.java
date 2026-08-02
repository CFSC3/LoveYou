package frames;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.a0411.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import permissoes.Permissoes;

public class MapsFragment extends Fragment {

    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference lugares;
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private LocationManager locationManager;
    private LocationListener locationListener;
    private int opcao=3;
    private android.content.BroadcastReceiver networkCallback;
    private boolean esperandoInternet = false;
    private GoogleMap mapaGlobal;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap maps) {

                mapaGlobal = maps; // Salva o mapa para podermos mexer nele pelo botão de decisão.

                //Objeto responsável por gerenciar a localização do usuário
                //locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

                //verifica se o celular está conectado a internet e faz o restante das chamadas se positivo.
                statusConexao(maps);
                //Liga o monitor passando o que ele deve fazer quando a internet voltar
                statusInternet(maps);

            // Add a marker in Sydney and move the camera
            LatLng myLocation = new LatLng(-3.074376, -60.040186);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                maps.setMyLocationEnabled(true);
                maps.getUiSettings().setMyLocationButtonEnabled(true);
                maps.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12));}
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    private void statusInternet(GoogleMap maps){
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
                                if (opcao == 1) {
                                    passeio(maps);
                                } else if (opcao == 2) {
                                    restaurantes(maps);
                                } else if (opcao == 3) {
                                    passeio(maps);
                                    restaurantes(maps);
                                }
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

    private void statusConexao(GoogleMap maps){
        //verifica se o celular está conectado a internet e faz o restante das chamadas se positivo.
        if (conexao.Internet.temInternet(requireContext())) {
            if (opcao == 1) {
                passeio(maps);
            } else if (opcao == 2) {
                restaurantes(maps);
            } else if (opcao == 3) {
                passeio(maps);
                restaurantes(maps);
            }
        }else{
            // Se está sem internet, mostra o aviso
            Toast.makeText(getContext(),
                    "Conecte a internet!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        Permissoes.validarPermissoes(permissoes, getActivity(), 1);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            opcao = bundle.getInt("dados");
        }
    }

    public void passeio(GoogleMap googleMa){

        GoogleMap googleMap = googleMa;
        lugares = referencia.child("lugares").child("passeio");

        lugares.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot s : dataSnapshot.getChildren()) {

                        LatLng lugares = new LatLng((Double) s.child("lat").getValue(), (Double) s.child("lon").getValue());
                        googleMap.addMarker(new MarkerOptions().position(lugares).title(s.child("nome").getValue().toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(lugares));


                    }
                } else {
                    Log.i("MeuLOG", "erro na captura");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void restaurantes(GoogleMap googleMa){

        GoogleMap googleMap = googleMa;
        lugares = referencia.child("lugares").child("restaurantes");

        lugares.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot s : dataSnapshot.getChildren()) {

                        LatLng lugares = new LatLng((Double) s.child("lat").getValue(), (Double) s.child("lon").getValue());
                        googleMap.addMarker(new MarkerOptions().position(lugares).title(s.child("nome").getValue().toString())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(lugares));


                    }
                } else {
                    Log.i("MeuLOG", "erro na captura");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {

            //permission denied (negada)
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                //Alerta
                alertaValidacaoPermissao();
            } else if (permissaoResultado == PackageManager.PERMISSION_GRANTED) {
                //Recuperar localizacao do usuario

                /*
                 * 1) Provedor da localização
                 * 2) Tempo mínimo entre atualizacões de localização (milesegundos)
                 * 3) Distancia mínima entre atualizacões de localização (metros)
                 * 4) Location listener (para recebermos as atualizações)
                 * */
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0,
                            locationListener
                    );
                }

            }
        }

    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void realizarSorteio(int filtroAtual) {
        if (mapaGlobal != null) {
            mapaGlobal.clear(); // Limpa todos os marcadores atuais da tela

            if (filtroAtual == 1) {
                buscarESortear("passeio", BitmapDescriptorFactory.HUE_BLUE);
                buscarESortear("restaurantes", BitmapDescriptorFactory.HUE_CYAN);
            } else if (filtroAtual == 2) {
                buscarESortear("passeio", BitmapDescriptorFactory.HUE_BLUE);
            } else if (filtroAtual == 3) {
                buscarESortear("restaurantes", BitmapDescriptorFactory.HUE_CYAN);
            }
        }
    }

    private void buscarESortear(String categoria, float corMarker) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("lugares").child(categoria);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Chama a classe Decisao para fazer o sorteio.
                DataSnapshot sorteado = utils.Decisao.sortearLugarAleatorio(dataSnapshot);

                if (sorteado != null) {
                    LatLng posicao = new LatLng((Double) sorteado.child("lat").getValue(), (Double) sorteado.child("lon").getValue());
                    String nome = sorteado.child("nome").getValue().toString();

                    // Adiciona o marcador sorteado no mapa
                    mapaGlobal.addMarker(new MarkerOptions()
                            .position(posicao)
                            .title("Sorteado: " + nome)
                            .icon(BitmapDescriptorFactory.defaultMarker(corMarker)));

                    // Move a câmera para o lugar sorteado com um zoom
                    mapaGlobal.animateCamera(CameraUpdateFactory.newLatLngZoom(posicao, 13));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("MeuLOG", "Erro ao realizar o sorteio");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Desliga o monitoramento usando a nossa classe utilitária centralizada
        conexao.Internet.pararMonitoramento(requireContext(), networkCallback);
    }
}