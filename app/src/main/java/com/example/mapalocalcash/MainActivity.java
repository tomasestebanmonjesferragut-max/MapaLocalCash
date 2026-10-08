package com.example.mapalocalcash;

import static com.example.mapalocalcash.R.*;
import static com.example.mapalocalcash.R.mipmap.*;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MapView map= null;
    private MapEventsOverlay eventos;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DBHelper(this);

        Configuration.getInstance().setUserAgentValue("Mapa/ tomas@gmail.com");

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.WIKIMEDIA);
        map.setMultiTouchControls(true);

        GeoPoint starPoin = new GeoPoint(-33.498895, -70.616617);

        map.getController().setZoom(18.0);
        map.getController().setCenter(starPoin);
        Toast.makeText(this, "Manten presionado el mapa para registrar un movimiento ahi", Toast.LENGTH_LONG).show();

        eventos = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                nuevoMovimiento(p);
                return true;
            }
        });

        findViewById(R.id.btnMovimiento).setOnClickListener(v -> nuevoMovimiento(null));
        findViewById(R.id.btnCuentas).setOnClickListener(v -> startActivity(new Intent(this, CuentasActivity.class)));
        findViewById(R.id.btnContactos).setOnClickListener(v -> startActivity(new Intent(this, ContactosActivity.class)));
        findViewById(R.id.btnCompras).setOnClickListener(v -> startActivity(new Intent(this, ComprasActivity.class)));
        findViewById(R.id.btnDeudas).setOnClickListener(v -> startActivity(new Intent(this, DeudasActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        cargarMovimientos();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    // Pone en el mapa un marcador por cada movimiento que tenga ubicacion
    private void cargarMovimientos() {
        map.getOverlays().clear();
        map.getOverlays().add(eventos);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT m.tipo, m.monto, m.descripcion, m.lat, m.lon, c.nombre FROM movimientos m JOIN cuentas c ON c.id = m.cuenta_id WHERE m.lat IS NOT NULL", null);
        while (c.moveToNext()) {
            Marker maker = new Marker(map);
            maker.setPosition(new GeoPoint(c.getDouble(3), c.getDouble(4)));
            maker.setTitle(c.getString(2));
            maker.setSnippet(c.getString(0) + " $" + Math.round(c.getDouble(1)) + " - " + c.getString(5));
            map.getOverlays().add(maker);
        }
        c.close();
        map.invalidate();
    }

    // punto es null cuando el movimiento se registra sin ubicacion
    private void nuevoMovimiento(GeoPoint punto) {
        ArrayList<Integer> idsCuentas = new ArrayList<>();
        ArrayList<String> nombresCuentas = new ArrayList<>();
        Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT id, nombre FROM cuentas", null);
        while (c.moveToNext()) {
            idsCuentas.add(c.getInt(0));
            nombresCuentas.add(c.getString(1));
        }
        c.close();

        if (idsCuentas.isEmpty()) {
            Toast.makeText(this, "Primero crea una cuenta", Toast.LENGTH_SHORT).show();
            return;
        }

        View vista = getLayoutInflater().inflate(R.layout.dialog_movimiento, null);
        Spinner spCuenta = vista.findViewById(R.id.spCuenta);
        Spinner spTipo = vista.findViewById(R.id.spTipo);
        EditText txtMonto = vista.findViewById(R.id.txtMonto);
        EditText txtDescripcion = vista.findViewById(R.id.txtDescripcion);

        spCuenta.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nombresCuentas));
        spTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Egreso", "Ingreso"}));

        new AlertDialog.Builder(this)
                .setTitle(punto == null ? "Nuevo movimiento" : "Nuevo movimiento en el mapa")
                .setView(vista)
                .setPositiveButton("Guardar", (d, w) -> {
                    String textoMonto = txtMonto.getText().toString();
                    if (textoMonto.isEmpty()) {
                        Toast.makeText(this, "Falta el monto", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double monto = Double.parseDouble(textoMonto);
                    String tipo = spTipo.getSelectedItem().toString();
                    int cuentaId = idsCuentas.get(spCuenta.getSelectedItemPosition());

                    ContentValues valores = new ContentValues();
                    valores.put("cuenta_id", cuentaId);
                    valores.put("tipo", tipo);
                    valores.put("monto", monto);
                    valores.put("descripcion", txtDescripcion.getText().toString());
                    if (punto != null) {
                        valores.put("lat", punto.getLatitude());
                        valores.put("lon", punto.getLongitude());
                    }

                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.insert("movimientos", null, valores);
                    double cambio = tipo.equals("Ingreso") ? monto : -monto;
                    db.execSQL("UPDATE cuentas SET saldo = saldo + ? WHERE id = ?", new Object[]{cambio, cuentaId});

                    Toast.makeText(this, "Movimiento guardado", Toast.LENGTH_SHORT).show();
                    cargarMovimientos();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
