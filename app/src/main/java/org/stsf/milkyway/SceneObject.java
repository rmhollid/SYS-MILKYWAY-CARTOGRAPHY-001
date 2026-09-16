/* STSF-CODE-PROVENANCE: SYS-MILKYWAY-CARTOGRAPHY-001 / ANDROID_POC / v0.1.1 */
package org.stsf.milkyway;

final class SceneObject {
    final String id;
    final String label;
    final Vec3 p;
    final boolean targetable;
    SceneObject(String id, String label, double x, double y, double z, boolean targetable) {
        this.id=id; this.label=label; this.p=new Vec3(x,y,z); this.targetable=targetable;
    }
}
