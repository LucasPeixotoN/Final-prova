package com.example.afinal;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class ApiPokemonActivity extends AppCompatActivity {

    private ListView listViewPokemons;
    private Button buttonLoadMore;
    private ProgressBar progressBar;
    private ArrayList<String> pokemonList;
    private ArrayAdapter<String> adapter;
    private Random random;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.api_nomes_pokemons);

        listViewPokemons = findViewById(R.id.listViewPokemons);
        buttonLoadMore = findViewById(R.id.buttonLoadMore);
        progressBar = findViewById(R.id.progressBar);

        pokemonList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pokemonList);
        listViewPokemons.setAdapter(adapter);

        random = new Random();

        buttonLoadMore.setOnClickListener(v -> buscarPokemon());
    }

    private void buscarPokemon() {
        progressBar.setVisibility(View.VISIBLE);
        buttonLoadMore.setEnabled(false);

        // Gera ID aleatório entre 1 e 898
        int pokemonId = random.nextInt(898) + 1;

        // Busca em thread separada
        new Thread(() -> {
            try {
                String urlString = "https://pokeapi.co/api/v2/pokemon/" + pokemonId;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse JSON
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String nomePokemon = jsonObject.getString("name");

                    // Capitalizar primeira letra
                    String nomeFormatado = nomePokemon.substring(0, 1).toUpperCase()
                            + nomePokemon.substring(1);

                    // Atualizar UI na thread principal
                    runOnUiThread(() -> {
                        pokemonList.add("🔴 " + nomeFormatado);
                        adapter.notifyDataSetChanged();
                        listViewPokemons.smoothScrollToPosition(pokemonList.size() - 1);

                        progressBar.setVisibility(View.GONE);
                        buttonLoadMore.setEnabled(true);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(ApiPokemonActivity.this,
                                "Erro ao buscar Pokémon",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        buttonLoadMore.setEnabled(true);
                    });
                }

                connection.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ApiPokemonActivity.this,
                            "Erro: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    buttonLoadMore.setEnabled(true);
                });
            }
        }).start();
    }
}