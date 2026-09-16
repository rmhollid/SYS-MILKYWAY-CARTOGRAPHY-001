/* STSF-CODE-PROVENANCE: SYS-MILKYWAY-CARTOGRAPHY-001 / ANDROID_POC / v0.1.1 */
package org.stsf.milkyway;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NavigationView extends View implements SensorEventListener, LocationListener {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final NavModel model = new NavModel();
    private final SensorManager sensorManager;
    private final LocationManager locationManager;
    private final Sensor rotationVector;
    private final ScaleGestureDetector pinch;
    private final List<PickPoint> picks = new ArrayList<>();
    private float yaw = -24f, pitch = 18f, zoom = 1.0f;
    private float lastX, lastY;
    private boolean dragging;
    private boolean sensorsStarted;

    private static final int BG = Color.rgb(5,7,8);
    private static final int PANEL = Color.rgb(15,18,20);
    private static final int GRID = Color.rgb(68,74,78);
    private static final int SILVER = Color.rgb(196,202,205);
    private static final int DIM = Color.rgb(122,130,134);
    private static final int WHITE = Color.rgb(238,241,242);
    private static final int AMBER = Color.rgb(235,165,54);
    private static final int RED = Color.rgb(216,62,48);

    public NavigationView(Context context) {
        super(context);
        setBackgroundColor(BG);
        text.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL));
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        rotationVector = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        pinch = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScale(ScaleGestureDetector d) {
                zoom *= d.getScaleFactor();
                if (zoom < 0.25f) zoom = 0.25f;
                if (zoom > 12f) zoom = 12f;
                invalidate();
                return true;
            }
        });
    }

    public void startNavigationSensors() {
        if (sensorsStarted) return;
        sensorsStarted = true;
        if (sensorManager != null && rotationVector != null)
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI);
        try {
            if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.5f, this);
                Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (last != null) model.updateFix(last);
            }
        } catch (Exception ignored) {}
        invalidate();
    }

    public void stopNavigationSensors() {
        sensorsStarted = false;
        if (sensorManager != null) sensorManager.unregisterListener(this);
        try { locationManager.removeUpdates(this); } catch (Exception ignored) {}
    }

    @Override public void onSensorChanged(SensorEvent e) {
        if (e.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] r = new float[9];
            float[] o = new float[3];
            SensorManager.getRotationMatrixFromVector(r, e.values);
            SensorManager.getOrientation(r, o);
            model.azimuthDeg = (float)Math.toDegrees(o[0]);
            if (model.azimuthDeg < 0) model.azimuthDeg += 360f;
            model.pitchDeg = (float)Math.toDegrees(o[1]);
            model.rollDeg = (float)Math.toDegrees(o[2]);
            invalidate();
        }
    }
    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    @Override public void onLocationChanged(Location location) { model.updateFix(location); invalidate(); }
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w = getWidth(), h = getHeight();
        float top = dp(42), bottom = dp(54);
        float dataH = Math.min(dp(176), h*0.28f);
        float viewBottom = h - bottom;
        float dataTop = viewBottom - dataH;

        drawStatus(c, w, top);
        drawViewport(c, 0, top, w, dataTop);
        drawDataRecess(c, 0, dataTop, w, viewBottom);
        drawTabs(c, 0, viewBottom, w, h);
    }

    private void drawStatus(Canvas c, float w, float h) {
        p.setColor(PANEL); c.drawRect(0,0,w,h,p);
        p.setColor(GRID); p.setStrokeWidth(dp(1)); c.drawLine(0,h-dp(1),w,h-dp(1),p);
        text.setTextSize(dp(11)); text.setColor(WHITE);
        c.drawText("SYS-MILKYWAY-CARTOGRAPHY-001", dp(10), dp(17), text);
        text.setTextSize(dp(9)); text.setColor(DIM);
        c.drawText(model.screen.name()+"  /  "+model.frameName(), dp(10), dp(33), text);
        String fix = model.fix == null ? "NO FIX" : String.format(Locale.US,"±%.1fm", model.fix.hasAccuracy()?model.fix.getAccuracy():0f);
        text.setColor(model.fix==null?RED:AMBER);
        float tw=text.measureText(fix); c.drawText(fix,w-dp(10)-tw,dp(24),text);
    }

    private void drawViewport(Canvas c, float l, float t, float r, float b) {
        c.save(); c.clipRect(l,t,r,b); c.drawColor(BG);
        picks.clear();
        float cx=(l+r)/2f, cy=(t+b)/2f;
        drawGrid(c,cx,cy,r-l,b-t);
        if (model.screen == NavModel.Screen.HISTORY || model.screen == NavModel.Screen.ROUTE) drawHistory(c,cx,cy,r-l,b-t);
        drawScene(c,cx,cy,r-l,b-t);
        drawViewportLabels(c,l,t,r,b);
        c.restore();
    }

    private void drawGrid(Canvas c,float cx,float cy,float w,float h) {
        p.setStrokeWidth(dp(1)); p.setColor(Color.rgb(40,45,48));
        for (int i=-4;i<=4;i++) {
            ScreenPt a=project(new Vec3(i,-4,0),cx,cy,w,h); ScreenPt b=project(new Vec3(i,4,0),cx,cy,w,h);
            c.drawLine(a.x,a.y,b.x,b.y,p);
            a=project(new Vec3(-4,i,0),cx,cy,w,h); b=project(new Vec3(4,i,0),cx,cy,w,h);
            c.drawLine(a.x,a.y,b.x,b.y,p);
        }
        drawAxis(c,new Vec3(0,0,0),new Vec3(2,0,0),cx,cy,w,h,WHITE,"X");
        drawAxis(c,new Vec3(0,0,0),new Vec3(0,2,0),cx,cy,w,h,AMBER,"Y");
        drawAxis(c,new Vec3(0,0,0),new Vec3(0,0,2),cx,cy,w,h,DIM,"Z");
    }

    private void drawAxis(Canvas c,Vec3 a,Vec3 b,float cx,float cy,float w,float h,int color,String label) {
        ScreenPt p1=project(a,cx,cy,w,h), p2=project(b,cx,cy,w,h);
        p.setColor(color); p.setStrokeWidth(dp(1)); c.drawLine(p1.x,p1.y,p2.x,p2.y,p);
        text.setColor(color); text.setTextSize(dp(9)); c.drawText(label,p2.x+dp(3),p2.y,text);
    }

    private void drawScene(Canvas c,float cx,float cy,float w,float h) {
        SceneObject[] objects = ReferenceScenes.forScale(model.scale);
        for (SceneObject o: objects) {
            ScreenPt s=project(o.p,cx,cy,w,h);
            int color = (model.target!=null && model.target.id.equals(o.id)) ? AMBER : WHITE;
            p.setColor(color); c.drawCircle(s.x,s.y, model.target!=null&&model.target.id.equals(o.id)?dp(5):dp(3), p);
            text.setTextSize(dp(9)); text.setColor(color); c.drawText(o.label,s.x+dp(7),s.y-dp(5),text);
            picks.add(new PickPoint(o,s.x,s.y));
        }
        if (model.fix != null) {
            Vec3 here = currentScenePoint();
            ScreenPt me = project(here,cx,cy,w,h);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); p.setColor(AMBER);
            c.drawCircle(me.x,me.y,dp(7),p);
            p.setStyle(Paint.Style.FILL);
            text.setTextSize(dp(9)); text.setColor(AMBER);
            c.drawText(model.scale == NavModel.Scale.LOCAL ? "YOU [MEASURED]" : "YOU [DEMO FRAME PROJECTION]", me.x+dp(9), me.y+dp(4), text);
        }
        if (model.target != null && model.screen == NavModel.Screen.ROUTE && !model.target.id.startsWith("HIST:")) {
            ScreenPt a=project(currentScenePoint(),cx,cy,w,h), z=project(model.target.p,cx,cy,w,h);
            p.setColor(AMBER); p.setStrokeWidth(dp(2)); c.drawLine(a.x,a.y,z.x,z.y,p);
        }
    }

    private void drawHistory(Canvas c,float cx,float cy,float w,float h) {
        if (model.history.size() < 2) return;
        Location origin=model.history.get(0);
        Path path=new Path(); boolean first=true;
        int n=model.history.size();
        for (int i=Math.max(0,n-800);i<n;i++) {
            Location q=model.history.get(i);
            Vec3 scaled=historyScenePoint(origin,q);
            ScreenPt s=project(scaled,cx,cy,w,h);
            if(first){path.moveTo(s.x,s.y);first=false;} else path.lineTo(s.x,s.y);
            if(model.screen==NavModel.Screen.HISTORY && i%Math.max(1,n/30)==0)
                picks.add(new PickPoint(new SceneObject("HIST:"+i,"HISTORY "+i,scaled.x,scaled.y,0,true),s.x,s.y));
        }
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); p.setColor(SILVER); c.drawPath(path,p); p.setStyle(Paint.Style.FILL);
        if (model.screen == NavModel.Screen.ROUTE && model.target != null && model.target.id.startsWith("HIST:")) {
            try {
                int idx = Integer.parseInt(model.target.id.substring(5));
                if (idx >= 0 && idx < model.history.size()) {
                    Vec3 targetPoint = historyScenePoint(origin, model.history.get(idx));
                    Vec3 currentPoint = historyScenePoint(origin, model.history.get(model.history.size()-1));
                    ScreenPt a = project(currentPoint,cx,cy,w,h);
                    ScreenPt z = project(targetPoint,cx,cy,w,h);
                    p.setColor(AMBER); p.setStrokeWidth(dp(2)); c.drawLine(a.x,a.y,z.x,z.y,p);
                    p.setStyle(Paint.Style.STROKE); c.drawCircle(z.x,z.y,dp(7),p); p.setStyle(Paint.Style.FILL);
                }
            } catch (Exception ignored) {}
        }
    }

    private Vec3 historyScenePoint(Location origin, Location q) {
        Vec3 enu = localMeters(origin,q);
        double norm = Math.max(25.0, maxHistoryRadius(origin));
        return new Vec3(enu.x/norm*3.2, enu.y/norm*3.2, 0);
    }

    private Vec3 currentScenePoint() {
        switch(model.scale) {
            case LOCAL_SYSTEM: return new Vec3(1.0,0.08,0.0);
            case LOCAL_GROUP: return new Vec3(0.0,0.0,0.0);
            case TRANSAXIAL: return new Vec3(0.0,0.0,0.0);
            case MILKY_WAY: return new Vec3(3.1,-1.1,0.06);
            default: return new Vec3(0.0,0.0,0.0);
        }
    }

    private double maxHistoryRadius(Location origin) {
        double max=25;
        for(Location q:model.history) max=Math.max(max, origin.distanceTo(q));
        return max;
    }

    private Vec3 localMeters(Location origin, Location q) {
        double lat0=Math.toRadians(origin.getLatitude());
        double dLat=Math.toRadians(q.getLatitude()-origin.getLatitude());
        double dLon=Math.toRadians(q.getLongitude()-origin.getLongitude());
        double R=6378137.0;
        return new Vec3(dLon*Math.cos(lat0)*R,dLat*R,q.getAltitude()-origin.getAltitude());
    }

    private void drawViewportLabels(Canvas c,float l,float t,float r,float b) {
        text.setTextSize(dp(10)); text.setColor(DIM);
        c.drawText("3D CARTOGRAPHIC VIEW", l+dp(10), t+dp(18), text);
        text.setColor(WHITE); String scale=model.scale.name().replace('_',' ');
        float tw=text.measureText(scale); c.drawText(scale,r-dp(10)-tw,t+dp(18),text);
        text.setColor(DIM); text.setTextSize(dp(8));
        c.drawText("DRAG ROTATE  •  PINCH SCALE  •  TAP SCALE LABEL TO CHANGE FRAME SCALE",l+dp(10),b-dp(10),text);
    }

    private void drawDataRecess(Canvas c,float l,float t,float r,float b) {
        p.setColor(PANEL); c.drawRect(l,t,r,b,p); p.setColor(GRID); c.drawRect(l,t,r,t+dp(1),p);
        float x=dp(10), y=t+dp(17); text.setTextSize(dp(10)); text.setColor(WHITE);
        c.drawText(screenTitle(),x,y,text); y+=dp(17); text.setTextSize(dp(9));
        switch(model.screen) {
            case NOW: y=drawNowData(c,x,y); break;
            case HISTORY: y=drawHistoryData(c,x,y); break;
            case TARGET: y=drawTargetData(c,x,y); break;
            case ROUTE: y=drawRouteData(c,x,y); break;
        }
        text.setColor(DIM); text.setTextSize(dp(8));
        c.drawText("MODE: "+model.localizationMode+"   FRAME: "+model.frameName(),x,b-dp(8),text);
    }

    private float drawNowData(Canvas c,float x,float y) {
        if(model.fix==null){ text.setColor(RED); c.drawText("NO MEASURED LOCATION FIX",x,y,text); return y; }
        text.setColor(SILVER);
        c.drawText(String.format(Locale.US,"LAT %.7f   LON %.7f",model.fix.getLatitude(),model.fix.getLongitude()),x,y,text); y+=dp(14);
        c.drawText(String.format(Locale.US,"ALT %.1f m   SPEED %.2f m/s",model.fix.getAltitude(),model.fix.hasSpeed()?model.fix.getSpeed():0f),x,y,text); y+=dp(14);
        c.drawText(String.format(Locale.US,"HEADING %.1f°   PITCH %.1f°   ROLL %.1f°",model.azimuthDeg,model.pitchDeg,model.rollDeg),x,y,text); return y;
    }
    private float drawHistoryData(Canvas c,float x,float y) {
        text.setColor(SILVER); c.drawText("RECORDED STATES: "+model.history.size(),x,y,text); y+=dp(14);
        if(model.history.size()>1){ float d=0; for(int i=1;i<model.history.size();i++)d+=model.history.get(i-1).distanceTo(model.history.get(i)); c.drawText(String.format(Locale.US,"TRACK DISTANCE %.1f m",d),x,y,text); y+=dp(14); }
        c.drawText("TAP A VISIBLE HISTORY SAMPLE TO SET IT AS TARGET",x,y,text); return y;
    }
    private float drawTargetData(Canvas c,float x,float y) {
        text.setColor(model.target==null?AMBER:WHITE); c.drawText(model.target==null?"NO TARGET SELECTED":"TARGET: "+model.target.label,x,y,text); y+=dp(14);
        text.setColor(SILVER); c.drawText("TAP A VISIBLE OBJECT TO SELECT DESTINATION",x,y,text); return y;
    }
    private float drawRouteData(Canvas c,float x,float y) {
        if(model.target==null){ text.setColor(AMBER); c.drawText("NO TARGET — SELECT ONE IN TARGET",x,y,text); return y; }
        text.setColor(WHITE); c.drawText("ROUTE TO: "+model.target.label,x,y,text); y+=dp(14);
        double d=model.targetDistanceMeters(); float br=model.targetBearingDeg(); text.setColor(SILVER);
        if(!Double.isNaN(d)) c.drawText(String.format(Locale.US,"LOCAL RANGE %.1f m   BEARING %.1f°",d,br),x,y,text);
        else c.drawText("ROUTE GEOMETRY: REFERENCE/DEMO FRAME — NOT FLIGHT AUTHORITY",x,y,text);
        return y;
    }

    private String screenTitle(){
        switch(model.screen){case NOW:return "WHERE YOU ARE";case HISTORY:return "WHERE YOU WERE";case TARGET:return "WHERE YOU WANT TO GO";default:return "HOW TO GET THERE";}
    }

    private void drawTabs(Canvas c,float l,float t,float r,float b) {
        String[] labels={"NOW","HISTORY","TARGET","ROUTE"};
        float bw=(r-l)/4f;
        for(int i=0;i<4;i++){
            boolean sel=model.screen.ordinal()==i;
            p.setColor(sel?Color.rgb(28,31,33):Color.rgb(12,14,15)); c.drawRect(l+i*bw,t,l+(i+1)*bw,b,p);
            p.setColor(sel?AMBER:GRID); c.drawRect(l+i*bw,t,l+(i+1)*bw,t+dp(sel?2:1),p);
            text.setTextSize(dp(9)); text.setColor(sel?WHITE:DIM); float tw=text.measureText(labels[i]); c.drawText(labels[i],l+i*bw+(bw-tw)/2f,t+(b-t)/2f+dp(3),text);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        pinch.onTouchEvent(e);
        float h=getHeight();
        if(e.getAction()==MotionEvent.ACTION_DOWN){lastX=e.getX();lastY=e.getY();dragging=false;return true;}
        if(e.getAction()==MotionEvent.ACTION_MOVE && !pinch.isInProgress()){
            float dx=e.getX()-lastX,dy=e.getY()-lastY;
            if(Math.abs(dx)+Math.abs(dy)>dp(2)){dragging=true;yaw+=dx*0.35f;pitch+=dy*0.25f;if(pitch>80)pitch=80;if(pitch<-80)pitch=-80;lastX=e.getX();lastY=e.getY();invalidate();}
            return true;
        }
        if(e.getAction()==MotionEvent.ACTION_UP){
            float bottom=dp(54);
            if(e.getY()>=h-bottom){ int idx=(int)(e.getX()/(getWidth()/4f)); if(idx<0)idx=0;if(idx>3)idx=3; model.screen=NavModel.Screen.values()[idx]; invalidate(); return true; }
            float top=dp(42); if(e.getY()>=top && e.getY()<=top+dp(34) && e.getX()>getWidth()*0.55f){ cycleScale(); return true; }
            if(!dragging && (model.screen==NavModel.Screen.TARGET || model.screen==NavModel.Screen.HISTORY)) pickTarget(e.getX(),e.getY());
            return true;
        }
        return true;
    }

    private void cycleScale(){int n=(model.scale.ordinal()+1)%NavModel.Scale.values().length;model.scale=NavModel.Scale.values()[n];model.target=null;invalidate();}
    private void pickTarget(float x,float y){
        PickPoint best=null; float bd=dp(28);
        for(PickPoint q:picks){float dx=x-q.x,dy=y-q.y,d=(float)Math.sqrt(dx*dx+dy*dy);if(d<bd&&q.o.targetable){best=q;bd=d;}}
        if(best!=null){model.target=best.o;model.screen=NavModel.Screen.ROUTE;invalidate();}
    }

    private ScreenPt project(Vec3 v,float cx,float cy,float w,float h){
        double yr=Math.toRadians(yaw),pr=Math.toRadians(pitch);
        double x1=v.x*Math.cos(yr)-v.y*Math.sin(yr), y1=v.x*Math.sin(yr)+v.y*Math.cos(yr), z1=v.z;
        double y2=y1*Math.cos(pr)-z1*Math.sin(pr), z2=y1*Math.sin(pr)+z1*Math.cos(pr);
        double dist=7.5, den=Math.max(1.5,dist-z2), s=Math.min(w,h)*0.12*zoom*dist/den;
        return new ScreenPt((float)(cx+x1*s),(float)(cy-y2*s),(float)z2);
    }

    private float dp(float v){return v*getResources().getDisplayMetrics().density;}
    private static final class ScreenPt{final float x,y,z;ScreenPt(float x,float y,float z){this.x=x;this.y=y;this.z=z;}}
    private static final class PickPoint{final SceneObject o;final float x,y;PickPoint(SceneObject o,float x,float y){this.o=o;this.x=x;this.y=y;}}
}
