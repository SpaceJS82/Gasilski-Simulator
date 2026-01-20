package si.um.feri.gasilci.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import si.um.feri.gasilci.data.FirePoint;
import si.um.feri.gasilci.data.FireStation;

public class DispatchManager {



    public interface ExtinguishCompleteListener {
        void onExtinguishComplete(FirePoint fire);
    }

    private final List<TruckMission> activeMissions = new ArrayList<>();
    private final TextureAtlas atlas;
    private final float[] stationPos;
    private ArrivalListener arrivalListener;
    private ExtinguishCompleteListener extinguishCompleteListener;
    private final FireStation station;

    private static class TruckMission {
        List<Truck> trucks;
        FirePoint targetFire;
        boolean arrived = false;
        float missionStartTime;

        TruckMission(List<Truck> trucks, FirePoint fire) {
            this.trucks = trucks;
            this.targetFire = fire;
            this.missionStartTime = 0;
        }
    }

    public DispatchManager(TextureAtlas atlas, float[] stationPos, FireStation station) {
        this.atlas = atlas;
        this.stationPos = stationPos;
        this.station = station;
    }

    public void setArrivalListener(ArrivalListener listener) {
        this.arrivalListener = listener;
    }

    public void setExtinguishCompleteListener(ExtinguishCompleteListener listener) {
        this.extinguishCompleteListener = listener;
    }

    public interface ArrivalListener {
        void onTruckArrived(Truck truck, FirePoint fire, int numTrucks);
    }

    public void dispatchTrucks(FirePoint fire, List<float[]> route, int numTrucks) {
        if (!station.canDispatch(numTrucks)) {
            return;
        }

        station.dispatchTrucks(numTrucks);
        fire.addAssignedTrucks(numTrucks); // Add to existing assigned trucks

        List<Truck> trucks = new ArrayList<>();
        for (int i = 0; i < numTrucks; i++) {
            float offsetX = (i % 2) * 0.15f - 0.075f;
            float offsetY = (i / 2) * 0.15f;
            Truck truck = new Truck(atlas, stationPos[0] + offsetX, stationPos[1] + offsetY);
            truck.setRoute(route);
            truck.setStartDelay(i * 0.5f); // First truck (i=0) has 0 delay
            trucks.add(truck);
        }

        TruckMission mission = new TruckMission(trucks, fire);
        activeMissions.add(mission);
    }


    public void update(float delta) {
        List<TruckMission> completed = new ArrayList<>();
        List<FirePoint> justExtinguished = new ArrayList<>();

        for (TruckMission mission : activeMissions) {
            // Update all trucks in the mission
            boolean allArrived = true;
            for (Truck truck : mission.trucks) {
                truck.update(delta);
                if (!truck.hasArrived()) {
                    allArrived = false;
                }
            }

            // When all trucks arrive, start extinguishing
            if (allArrived && !mission.arrived) {
                mission.arrived = true;
                mission.missionStartTime = 0;
                if (arrivalListener != null) {
                    arrivalListener.onTruckArrived(mission.trucks.get(0), mission.targetFire, mission.trucks.size());
                }
            }

            // Update extinguishing progress
            if (mission.arrived && !justExtinguished.contains(mission.targetFire)) {
                mission.missionStartTime += delta;
                boolean extinguished = mission.targetFire.updateExtinguishing(delta);

                if (extinguished) {
                    mission.targetFire.putOut();
                    mission.targetFire.resetAssignedTrucks();
                    justExtinguished.add(mission.targetFire);
                    
                    if (extinguishCompleteListener != null) {
                        extinguishCompleteListener.onExtinguishComplete(mission.targetFire);
                    }
                }
            }
        }

        // Remove all missions for extinguished fires
        for (FirePoint fire : justExtinguished) {
            for (TruckMission mission : activeMissions) {
                if (mission.targetFire == fire) {
                    // Return trucks to station
                    station.returnTrucks(mission.trucks.size());
                    station.addResponseTime(mission.missionStartTime / 60f);
                    
                    // Hide all trucks
                    for (Truck truck : mission.trucks) {
                        truck.hide();
                    }
                    
                    completed.add(mission);
                }
            }
        }

        activeMissions.removeAll(completed);
    }

    public Map<Truck, FirePoint> getActiveTrucks() {
        Map<Truck, FirePoint> result = new java.util.HashMap<>();
        for (TruckMission mission : activeMissions) {
            for (Truck truck : mission.trucks) {
                if (truck.isVisible()) {
                    result.put(truck, mission.targetFire);
                }
            }
        }
        return result;
    }

    public FireStation getStation() {
        return station;
    }

}
