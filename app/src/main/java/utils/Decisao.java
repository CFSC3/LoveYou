package utils;

import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Decisao {

    // Método que recebe os dados do Firebase e devolve um único lugar aleatório
    public static DataSnapshot sortearLugarAleatorio(DataSnapshot dataSnapshot) {
        // Verifica se existem dados
        if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
            return null;
        }

        // Transforma os filhos do DataSnapshot em uma Lista tradicional do Java
        List<DataSnapshot> listaLugares = new ArrayList<>();
        for (DataSnapshot s : dataSnapshot.getChildren()) {
            listaLugares.add(s);
        }

        // Sorteia um número entre 0 e o tamanho total da lista
        Random random = new Random();
        int indiceSorteado = random.nextInt(listaLugares.size());

        // Retorna o lugar sorteado
        return listaLugares.get(indiceSorteado);
    }
}