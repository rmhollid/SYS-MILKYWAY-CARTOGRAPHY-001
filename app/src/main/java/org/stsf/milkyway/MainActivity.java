/* STSF-CODE-PROVENANCE: SYS-MILKYWAY-CARTOGRAPHY-001 / ANDROID_POC / v0.1.1 */
package org.stsf.milkyway;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public final class MainActivity extends Activity {
    private NavigationView navigationView;
    private static final int REQ_LOCATION = 4101;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        navigationView = new NavigationView(this);
        setContentView(navigationView);
        requestLocationIfNeeded();
    }

    private void requestLocationIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOCATION);
        } else {
            navigationView.startNavigationSensors();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQ_LOCATION) navigationView.startNavigationSensors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (navigationView != null) navigationView.startNavigationSensors();
    }

    @Override
    protected void onPause() {
        if (navigationView != null) navigationView.stopNavigationSensors();
        super.onPause();
    }
}
