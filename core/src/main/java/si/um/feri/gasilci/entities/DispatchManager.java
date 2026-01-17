package si.um.feri.gasilci.entities;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import si.um.feri.gasilci.data.FirePoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DispatchManager {

    public interface ArrivalListener {
        void onTruckArrived(Truck truck, FirePoint fire);
    }

    private final List<TruckMission> activeMissions = new ArrayList<>();
    private final TextureAtlas atlas;
    private final float[] stationPos;
    private ArrivalListener arrivalListener;

    private static class TruckMission {
        Truck truck;
        FirePoint targetFire;

        TruckMission(Truck truck, FirePoint fire) {
            this.truck = truck;
            this.targetFire = fire;
        }
    }

    public DispatchManager(TextureAtlas atlas, float[] stationPos) {
        this.atlas = atlas;
        this.stationPos = stationPos;
    }

    public void setArrivalListener(ArrivalListener listener) {
        this.arrivalListener = listener;
    }

    public void dispatchTruck(FirePoint fire, List<float[]> route) {
        Truck truck = new Truck(atlas, stationPos[0], stationPos[1]);
        truck.setRoute(route);
        activeMissions.add(new TruckMission(truck, fire));
    }

    public void update(float delta) {
        List<TruckMission> completed = new ArrayList<>();

        for (TruckMission mission : activeMissions) {
            mission.truck.update(delta);

            if (mission.truck.hasArrived()) {
                if (arrivalListener != null) {
                    arrivalListener.onTruckArrived(mission.truck, mission.targetFire);
                }
                completed.add(mission);
            }
        }

        activeMissions.removeAll(completed);
    }

    public Map<Truck, FirePoint> getActiveTrucks() {
        Map<Truck, FirePoint> result = new java.util.HashMap<>();
        for (TruckMission mission : activeMissions) {
            result.put(mission.truck, mission.targetFire);
        }
        return result;
    }

}
