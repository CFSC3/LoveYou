package com.example.a0411;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import telas.Apresentacao;
import telas.TelaInicio;

public class MainActivity extends AppCompatActivity {

    private Button avanca;
    private EditText dataNiver, nome;
    private FirebaseAuth usuario = FirebaseAuth.getInstance();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference dadosUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        avanca = findViewById(R.id.buttonAvancarCad);
        dataNiver = (EditText) findViewById(R.id.dataNiver);
        nome = (EditText) findViewById(R.id.textNome);

        ajustarPadrãodeData();

        if (usuario.getCurrentUser()==null) {

        }else{
            telaPricipal();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(getColor(R.color.notificacao));
        }

    }

    public void telaPricipal(){

        Intent intent = new Intent(this, TelaInicio.class);
        startActivity(intent);
        finish();

    }

    public void apresentacao(View view) {

        //verifica se o celular está conectado a internet.
        if (conexao.Internet.temInternet(this)) {

            if (nome.getText().toString().equals("")) {
                Toast.makeText(this, "Adicione o nome.", Toast.LENGTH_SHORT).show();
            } else {

                if (dataNiver.getText().toString().equals("")) {
                    Toast.makeText(this, "Adicione a data do aniversário.", Toast.LENGTH_SHORT).show();
                } else {

                    salvarNomeNiver();

                    usuario.signInWithEmailAndPassword("henlima12@gmail.com", "1365812177").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.i("signIN", "Sucesso ao logar");
                            } else {
                                Log.i("signIN", "Erro ao logar");
                            }
                        }
                    });

                    Intent intent = new Intent(this, Apresentacao.class);
                    startActivity(intent);
                    finish();
                }
            }

        } else {
            // Se está sem internet, mostra o aviso
            Toast.makeText(this,
                    "Você está sem internet!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void ajustarPadrãodeData(){

        //MÁSCARA AUTOMÁTICA DE DATA

        dataNiver.addTextChangedListener(new android.text.TextWatcher() {
            boolean isUpdating = false;
            String old = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Remove tudo que não for número
                String str = s.toString().replaceAll("[^0-9]*", "");
                String mascara = "";
                String mascaraPattern = "##/##/####";

                if (isUpdating) {
                    old = str;
                    isUpdating = false;
                    return;
                }

                int i = 0;
                for (char m : mascaraPattern.toCharArray()) {
                    if (m != '#' && str.length() > old.length()) {
                        mascara += m;
                        continue;
                    }
                    try {
                        mascara += str.charAt(i);
                    } catch (Exception e) {
                        break;
                    }
                    i++;
                }

                isUpdating = true;
                dataNiver.setText(mascara);
                // Mantém o cursor sempre no final da digitação
                dataNiver.setSelection(mascara.length());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    public void salvarNomeNiver(){

        dadosUsuario = databaseReference.child("usuario");

        dadosUsuario.child("nome").setValue(nome.getText().toString());
        dadosUsuario.child("data aniversario").setValue(dataNiver.getText().toString());
    }
}