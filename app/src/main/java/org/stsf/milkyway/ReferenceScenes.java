/* STSF-CODE-PROVENANCE: SYS-MILKYWAY-CARTOGRAPHY-001 / ANDROID_POC / v0.1.1 */
package org.stsf.milkyway;

final class ReferenceScenes {
    private ReferenceScenes() {}

    /*
     * POC visualization catalog only. These points are intentionally marked DEMO and are
     * not navigation-authority ephemerides or astrometric solutions. Their purpose is to
     * exercise scaling, selection, routing geometry, and frame transitions safely.
     */
    static SceneObject[] forScale(NavModel.Scale scale) {
        switch (scale) {
            case LOCAL_SYSTEM:
                return new SceneObject[]{
                    new SceneObject("SOL","SUN [DEMO]",0,0,0,true),
                    new SceneObject("EARTH","EARTH [DEMO]",1.0,0.08,0,true),
                    new SceneObject("MARS","MARS [DEMO]",1.48,-0.30,0.04,true),
                    new SceneObject("JUPITER","JUPITER [DEMO]",4.6,2.2,-0.1,true)
                };
            case LOCAL_GROUP:
                return new SceneObject[]{
                    new SceneObject("SOL","SOL [DEMO]",0,0,0,true),
                    new SceneObject("SYS-A","NEAR SYSTEM A [DEMO]",1.2,0.4,0.2,true),
                    new SceneObject("SYS-B","NEAR SYSTEM B [DEMO]",-1.8,1.1,-0.3,true),
                    new SceneObject("SYS-C","NEAR SYSTEM C [DEMO]",0.8,-2.2,0.6,true)
                };
            case TRANSAXIAL:
                return new SceneObject[]{
                    new SceneObject("TA-ORIGIN","TA-ORIGIN",0,0,0,true),
                    new SceneObject("TA-01","TA-01",-2.5,1.8,0.4,true),
                    new SceneObject("TA-02","TA-02",3.0,2.1,-0.6,true),
                    new SceneObject("TA-03","TA-03",2.0,-3.0,0.8,true),
                    new SceneObject("TA-04","TA-04",-3.2,-2.4,-0.2,true)
                };
            case MILKY_WAY:
                return new SceneObject[]{
                    new SceneObject("MW-CENTER","GALACTIC CENTER [DEMO]",0,0,0,true),
                    new SceneObject("MW-SOL","SOL REGION [DEMO]",3.1,-1.1,0.06,true),
                    new SceneObject("MW-R1","REGION 01 [DEMO]",-2.8,2.0,0.2,true),
                    new SceneObject("MW-R2","REGION 02 [DEMO]",1.6,3.4,-0.4,true),
                    new SceneObject("MW-R3","REGION 03 [DEMO]",-3.6,-2.2,0.5,true)
                };
            default:
                return new SceneObject[]{ new SceneObject("HERE","CURRENT FIX",0,0,0,false) };
        }
    }
}
