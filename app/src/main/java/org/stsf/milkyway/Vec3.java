/* STSF-CODE-PROVENANCE: SYS-MILKYWAY-CARTOGRAPHY-001 / ANDROID_POC / v0.1.1 */
package org.stsf.milkyway;

final class Vec3 {
    final double x, y, z;
    Vec3(double x, double y, double z) { this.x=x; this.y=y; this.z=z; }
    Vec3 add(Vec3 o) { return new Vec3(x+o.x,y+o.y,z+o.z); }
    Vec3 sub(Vec3 o) { return new Vec3(x-o.x,y-o.y,z-o.z); }
    Vec3 scale(double s) { return new Vec3(x*s,y*s,z*s); }
    double norm() { return Math.sqrt(x*x+y*y+z*z); }
}
