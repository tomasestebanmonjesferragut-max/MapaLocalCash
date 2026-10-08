package com.example.mapalocalcash;

import static com.example.mapalocalcash.R.*;
import static com.example.mapalocalcash.R.mipmap.*;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

public class MainActivity extends AppCompatActivity {

    private MapView map= null;
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


        Configuration.getInstance().setUserAgentValue("Mapa/ tomas@gmail.com");

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.WIKIMEDIA);
        map.setMultiTouchControls(true);

        GeoPoint starPoin = new GeoPoint(-33.498895, -70.616617);
        GeoPoint PuntoMoto = new GeoPoint(-33.498738, -70.616173);
        GeoPoint PuntoPoli = new GeoPoint(-33.498609, -70.615595);

        map.getController().setZoom(20.0);
        Toast.makeText(this, "Tengo que ver el Codigo de la Discodia", Toast.LENGTH_SHORT).show();

        map.getController().setCenter(starPoin);

        Marker maker = new Marker(map);
        maker.setPosition(starPoin);
        maker.setTitle("Hola ");
        maker.setSnippet("Repartidor cerca");

        Marker maker2 = new Marker(map);
        maker2.setPosition(PuntoMoto);
        maker2.setIcon(
                ContextCompat.getDrawable(this,R.mipmap.ic_moto_foreground)
        );
        maker2.setTitle("Hola ");
        maker2.setSnippet("Repartidor cerca");

        Marker maker3 = new Marker(map);
        maker3.setPosition(PuntoPoli);
        maker3.setIcon(
                ContextCompat.getDrawable(this,R.mipmap.ic_poli_foreground)
        );
        maker3.setTitle("Hola ");
        maker3.setSnippet("Repartidor cerca");


        map.getOverlays().add(maker);
        map.getOverlays().add(maker2);
        map.getOverlays().add(maker3);
        map.invalidate();







    }
}