package com.example.mapalocalcash;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ContactosActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private ListView lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);

        dbHelper = new DBHelper(this);
        EditText txtNombre = findViewById(R.id.txtNombre);
        lista = findViewById(R.id.lista);

        findViewById(R.id.btnAgregar).setOnClickListener(v -> {
            String nombre = txtNombre.getText().toString();
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Escribe un nombre", Toast.LENGTH_SHORT).show();
                return;
            }
            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            dbHelper.getWritableDatabase().insert("contactos", null, valores);

            txtNombre.setText("");
            cargar();
        });

        cargar();
    }

    private void cargar() {
        ArrayList<String> nombres = new ArrayList<>();
        Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT nombre FROM contactos ORDER BY nombre", null);
        while (c.moveToNext()) {
            nombres.add(c.getString(0));
        }
        c.close();
        lista.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombres));
    }
}
