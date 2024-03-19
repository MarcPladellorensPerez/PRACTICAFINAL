package com.mpladellorens.m8_uf1_practica_final_marcpladellorensperez;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText Nom, Cognoms, Telefon;
    private TextView ultimaConexionTextView;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String LICENSE_ACCEPTED_KEY = "LicenseAccepted";
    private static final String DEFAULT_USER_NAME = "user";
    private static final String DEFAULT_USER_SURNAME = "default";
    private static final String DEFAULT_USER_PHONE = "5554";
    private static final String ULTIMA_CONEXION_KEY = "UltimaConexion";
    private boolean permissionsAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si la licencia ha sido aceptada antes
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean licenseAccepted = prefs.getBoolean(LICENSE_ACCEPTED_KEY, false);

        if (!licenseAccepted) {
            // Mostrar el diálogo de licencia si aún no se ha aceptado
            mostrarDialogoLicencia();
        } else {
            // Configurar la actividad principal si la licencia ya ha sido aceptada
            setContentView(R.layout.activity_main);
            iniciarComponentes();
            solicitarPermisos();
        }
    }

    private void mostrarDialogoLicencia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Licencia");

        // Crear un ScrollView y un TextView para mostrar el texto completo
        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(getString(R.string.terminos_y_condiciones));
        scrollView.addView(textView);

        builder.setView(scrollView);

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Guardar el estado de la licencia como aceptado
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putBoolean(LICENSE_ACCEPTED_KEY, true);
                editor.apply();

                // Configurar la actividad principal después de aceptar la licencia
                setContentView(R.layout.activity_main);
                iniciarComponentes();
                solicitarPermisos();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cerrar la aplicación si el usuario cancela la licencia
                finish();
            }
        });
        builder.show();
    }

    private void iniciarComponentes() {
        Nom = findViewById(R.id.Nom);
        Cognoms = findViewById(R.id.Cognoms);
        Telefon = findViewById(R.id.Telefon);
        ultimaConexionTextView = findViewById(R.id.ultimaConexionTextView);

        // Mostrar la fecha y hora de la última conexión
        mostrarUltimaConexion();
    }

    private void solicitarPermisos() {
        String[] permisos = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS
        };

        for (String permiso : permisos) {
            if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permisos, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        permissionsAccepted = true;
        enviarDatosUsuari();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                permissionsAccepted = true;
                enviarDatosUsuari();
            } else {
                mostrarMensajePermiso();
            }
        }
    }

    private void mostrarMensajePermiso() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permisos necesarios");
        builder.setMessage("La aplicación necesita permisos para funcionar correctamente.");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                solicitarPermisos();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    private void enviarDatosUsuari() {
        if (!permissionsAccepted) {
            return;
        }
        String nom = Nom.getText().toString();
        String cognoms = Cognoms.getText().toString();
        String telefon = Telefon.getText().toString();

        if (TextUtils.isEmpty(nom) || TextUtils.isEmpty(cognoms) || TextUtils.isEmpty(telefon)) {
            // Si algún campo está vacío, mostrar un mensaje al usuario
            return;
        }

        // Aquí puedes realizar acciones con los datos del usuario, como enviarlos a un servidor o guardarlos localmente
        enviarSMSUsuario();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Guardar la fecha y hora de la última conexión al pausar la aplicación
        guardarUltimaConexion();
        // Guardar los datos del usuario en las preferencias compartidas
        guardarDatosUsuario();
    }

    private void guardarUltimaConexion() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Obtener la fecha y hora actual
        String fechaHoraActual = obtenerFechaHoraActual();
        // Guardar la fecha y hora en SharedPreferences
        editor.putString(ULTIMA_CONEXION_KEY, fechaHoraActual);
        editor.apply();
    }

    private void guardarDatosUsuario() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Guardar los datos del usuario en SharedPreferences
        editor.putString("NomValue", Nom.getText().toString());
        editor.putString("CognomsValue", Cognoms.getText().toString());
        editor.putString("TelefonValue", Telefon.getText().toString());
        editor.apply();
    }

    private void mostrarUltimaConexion() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Obtener la fecha y hora de la última conexión guardada
        String ultimaConexion = prefs.getString(ULTIMA_CONEXION_KEY, "");
        // Mostrar la fecha y hora en el TextView
        ultimaConexionTextView.setText("Última conexión: " + ultimaConexion);

        // Obtener los valores guardados en las preferencias compartidas
        String nomValue = prefs.getString("NomValue", "");
        String cognomsValue = prefs.getString("CognomsValue", "");
        String telefonValue = prefs.getString("TelefonValue", "");

        // Verificar si los campos están vacíos y establecer los valores predeterminados si es necesario
        if (TextUtils.isEmpty(nomValue)) {
            Nom.setText(DEFAULT_USER_NAME);
        } else {
            Nom.setText(nomValue);
        }

        if (TextUtils.isEmpty(cognomsValue)) {
            Cognoms.setText(DEFAULT_USER_SURNAME);
        } else {
            Cognoms.setText(cognomsValue);
        }

        if (TextUtils.isEmpty(telefonValue)) {
            Telefon.setText(DEFAULT_USER_PHONE);
        } else {
            Telefon.setText(telefonValue);
        }
    }

    private void enviarSMSUsuario() {
        String nom = Nom.getText().toString();
        String cognoms = Cognoms.getText().toString();
        String medioAccesoRed = obtenerMedioAccesoRed();
        String telefono = Telefon.getText().toString();

        if (TextUtils.isEmpty(nom)) {
            nom = DEFAULT_USER_NAME;
        }
        if (TextUtils.isEmpty(cognoms)) {
            cognoms = DEFAULT_USER_SURNAME;
        }
        if (TextUtils.isEmpty(telefono)) {
            telefono = DEFAULT_USER_PHONE;
        }

        String mensaje = "usuari: " + nom + " " + cognoms + " via: " + medioAccesoRed;

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(telefono, null, mensaje, null, null); // Enviar el SMS al número proporcionado por el usuario
        Log.d("MainActivity", "SMS enviado: " + mensaje + " al número: " + telefono);
    }

    private String obtenerMedioAccesoRed() {
        // Aquí puedes implementar la lógica para obtener el medio de acceso a la red actual (Wi-Fi, datos móviles, etc.)
        return "Wi-Fi";
    }

    private String obtenerFechaHoraActual() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
