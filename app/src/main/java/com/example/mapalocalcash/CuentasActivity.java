package com.example.mapalocalcash;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class CuentasActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private ListView lista;
    private ArrayList<Integer> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuentas);

        dbHelper = new DBHelper(this);
        EditText txtNombre = findViewById(R.id.txtNombre);
        EditText txtSaldo = findViewById(R.id.txtSaldo);
        lista = findViewById(R.id.lista);

        findViewById(R.id.btnAgregar).setOnClickListener(v -> {
            String nombre = txtNombre.getText().toString();
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Escribe un nombre (ej: Efectivo)", Toast.LENGTH_SHORT).show();
                return;
            }
            String saldo = txtSaldo.getText().toString();

            ContentValues valores = new ContentValues();
            valores.put("nombre", nombre);
            valores.put("saldo", saldo.isEmpty() ? 0 : Double.parseDouble(saldo));
            dbHelper.getWritableDatabase().insert("cuentas", null, valores);

            txtNombre.setText("");
            txtSaldo.setText("");
            cargar();
        });

        lista.setOnItemClickListener((parent, view, position, id) -> verMovimientos(ids.get(position)));

        cargar();
    }

    private void cargar() {
        ids.clear();
        ArrayList<String> textos = new ArrayList<>();
        Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT id, nombre, saldo FROM cuentas", null);
        while (c.moveToNext()) {
            ids.add(c.getInt(0));
            textos.add(c.getString(1) + "   $" + Math.round(c.getDouble(2)));
        }
        c.close();
        lista.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, textos));
    }

    private void verMovimientos(int cuentaId) {
        ArrayList<String> textos = new ArrayList<>();
        Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT tipo, monto, descripcion, lat FROM movimientos WHERE cuenta_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(cuentaId)});
        while (c.moveToNext()) {
            String ubicacion = c.isNull(3) ? "" : " (en mapa)";
            textos.add(c.getString(0) + " $" + Math.round(c.getDouble(1)) + " - " + c.getString(2) + ubicacion);
        }
        c.close();

        if (textos.isEmpty()) {
            textos.add("Sin movimientos");
        }

        new AlertDialog.Builder(this)
                .setTitle("Movimientos")
                .setItems(textos.toArray(new String[0]), null)
                .setPositiveButton("Cerrar", null)
                .show();
    }
}
