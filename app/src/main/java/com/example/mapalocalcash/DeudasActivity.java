package com.example.mapalocalcash;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class DeudasActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private ListView listaDeudas;
    private ArrayList<Integer> idsDeudas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deudas);

        dbHelper = new DBHelper(this);
        EditText txtProductos = findViewById(R.id.txtProductos);
        EditText txtTotal = findViewById(R.id.txtTotal);
        ListView listaContactos = findViewById(R.id.listaContactos);
        listaDeudas = findViewById(R.id.listaDeudas);

        ArrayList<Integer> idsContactos = new ArrayList<>();
        ArrayList<String> nombres = new ArrayList<>();
        Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT id, nombre FROM contactos ORDER BY nombre", null);
        while (c.moveToNext()) {
            idsContactos.add(c.getInt(0));
            nombres.add(c.getString(1));
        }
        c.close();
        listaContactos.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listaContactos.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, nombres));

        findViewById(R.id.btnDividir).setOnClickListener(v -> {
            String productos = txtProductos.getText().toString();
            String total = txtTotal.getText().toString();
            if (productos.isEmpty() || total.isEmpty()) {
                Toast.makeText(this, "Faltan los productos o el total", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<Integer> elegidos = new ArrayList<>();
            SparseBooleanArray marcados = listaContactos.getCheckedItemPositions();
            for (int i = 0; i < idsContactos.size(); i++) {
                if (marcados.get(i)) {
                    elegidos.add(idsContactos.get(i));
                }
            }
            if (elegidos.isEmpty()) {
                Toast.makeText(this, "Elige al menos un contacto", Toast.LENGTH_SHORT).show();
                return;
            }

            // Se divide en partes iguales entre los contactos elegidos y yo
            double montoTotal = Double.parseDouble(total);
            double parte = montoTotal / (elegidos.size() + 1);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues compra = new ContentValues();
            compra.put("descripcion", productos);
            compra.put("monto", montoTotal);
            compra.put("fecha", "");
            compra.put("agendada", 0);
            long compraId = db.insert("compras", null, compra);

            for (int contactoId : elegidos) {
                ContentValues deuda = new ContentValues();
                deuda.put("compra_id", compraId);
                deuda.put("contacto_id", contactoId);
                deuda.put("monto", parte);
                deuda.put("pagada", 0);
                db.insert("deudas", null, deuda);
            }

            Toast.makeText(this, "Cada uno debe $" + Math.round(parte), Toast.LENGTH_LONG).show();
            txtProductos.setText("");
            txtTotal.setText("");
            listaContactos.clearChoices();
            listaContactos.invalidateViews();
            cargarDeudas();
        });

        listaDeudas.setOnItemClickListener((parent, view, position, id) ->
                new AlertDialog.Builder(this)
                        .setMessage("¿Esta deuda ya fue pagada?")
                        .setPositiveButton("Si", (d, w) -> {
                            dbHelper.getWritableDatabase().execSQL("UPDATE deudas SET pagada = 1 WHERE id = ?", new Object[]{idsDeudas.get(position)});
                            cargarDeudas();
                        })
                        .setNegativeButton("No", null)
                        .show());

        cargarDeudas();
    }

    private void cargarDeudas() {
        idsDeudas.clear();
        ArrayList<String> textos = new ArrayList<>();
        Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT d.id, ct.nombre, d.monto, co.descripcion FROM deudas d JOIN contactos ct ON ct.id = d.contacto_id JOIN compras co ON co.id = d.compra_id WHERE d.pagada = 0 ORDER BY d.id DESC", null);
        while (c.moveToNext()) {
            idsDeudas.add(c.getInt(0));
            textos.add(c.getString(1) + " debe $" + Math.round(c.getDouble(2)) + "  (" + c.getString(3) + ")");
        }
        c.close();
        listaDeudas.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, textos));
    }
}
