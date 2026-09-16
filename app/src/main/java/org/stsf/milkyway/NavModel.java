/* STSF-CODE-PROVENANCE: SYS-MILKYWAY-CARTOGRAPHY-001 / ANDROID_POC / v0.1.1 */
package org.stsf.milkyway;

import android.location.Location;
import java.util.ArrayList;
import java.util.List;

final class NavModel {
    enum Screen { NOW, HISTORY, TARGET, ROUTE }
    enum Scale { LOCAL, LOCAL_SYSTEM, LOCAL_GROUP, TRANSAXIAL, MILKY_WAY }

    Screen screen = Screen.NOW;
    Scale scale = Scale.LOCAL;
    Location fix;
    long fixTimeMillis;
    float azimuthDeg;
    float pitchDeg;
    float rollDeg;
    String localizationMode = "NO_FIX";
    SceneObject target;
    final List<Location> history = new ArrayList<>();
    long lastHistoryTime;

    void updateFix(Location location) {
        fix = new Location(location);
        fixTimeMillis = System.currentTimeMillis();
        localizationMode = "FUSED_MEASURED";
        boolean record = history.isEmpty();
        if (!record) {
            Location last = history.get(history.size()-1);
            record = location.distanceTo(last) >= 2.0f || (fixTimeMillis-lastHistoryTime) >= 2000;
        }
        if (record) {
            history.add(new Location(location));
            lastHistoryTime = fixTimeMillis;
            if (history.size() > 4096) history.remove(0);
        }
    }

    String frameName() {
        switch (scale) {
            case LOCAL: return "LOCAL / EARTH";
            case LOCAL_SYSTEM: return "LOCAL SYSTEM / SOL";
            case LOCAL_GROUP: return "INTER-SYSTEM LOCAL GROUP";
            case TRANSAXIAL: return "TRANSAXIAL";
            default: return "MILKY WAY / MWCRF";
        }
    }

    double targetDistanceMeters() {
        if (fix == null || target == null || !target.id.startsWith("HIST:")) return Double.NaN;
        try {
            int idx = Integer.parseInt(target.id.substring(5));
            if (idx < 0 || idx >= history.size()) return Double.NaN;
            return fix.distanceTo(history.get(idx));
        } catch (Exception e) { return Double.NaN; }
    }

    float targetBearingDeg() {
        if (fix == null || target == null || !target.id.startsWith("HIST:")) return Float.NaN;
        try {
            int idx = Integer.parseInt(target.id.substring(5));
            if (idx < 0 || idx >= history.size()) return Float.NaN;
            return fix.bearingTo(history.get(idx));
        } catch (Exception e) { return Float.NaN; }
    }
}
