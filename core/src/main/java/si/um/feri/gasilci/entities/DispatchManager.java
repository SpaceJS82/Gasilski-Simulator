package si.um.feri.gasilci.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import si.um.feri.gasilci.data.FirePoint;
import si.um.feri.gasilci.data.FireStation;
import si.um.feri.gasilci.util.SoundManager;

public class DispatchManager {

    // Available vehicle sprites
    private static final String[] VEHICLE_SPRITES = {
        "images/truck-up-1",
        "images/ambulance-1",
        "images/ambulance-2",
        "images/car-red"
    };
    private static final String FIRETRUCK_SPRITE = "images/truck-up-1";

    public interface ExtinguishCompleteListener {
        void onExtinguishComplete(FirePoint fire);
    }
    
    public interface AllTrucksArrivedListener {
        void onAllTrucksArrived(FirePoint fire);
    }

    private final List<TruckMission> activeMissions = new ArrayList<>();
    private final TextureAtlas atlas;
    private final float[] stationPos;
    private ArrivalListener arrivalListener;
    private ExtinguishCompleteListener extinguishCompleteListener;
    private AllTrucksArrivedListener allTrucksArrivedListener;
    private final FireStation station;
    private Sound truckDrivingSound;
    private Sound truckSirenSound;
    private Sound waterExtinguishingSound;

    private static class TruckMission {
        List<Truck> trucks;
        FirePoint targetFire;
        List<float[]> originalRoute;
        boolean arrived = false;
        boolean returning = false;
        boolean returnedToStation = false;
        float missionStartTime;
        long waterSoundId = -1;

        TruckMission(List<Truck> trucks, FirePoint fire, List<float[]> route) {
            this.trucks = trucks;
            this.targetFire = fire;
            this.originalRoute = route;
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
    
    public void setAllTrucksArrivedListener(AllTrucksArrivedListener listener) {
        this.allTrucksArrivedListener = listener;
    }

    public void setTruckDrivingSound(Sound sound) {
        this.truckDrivingSound = sound;
    }

    public void setTruckSirenSound(Sound sound) {
        this.truckSirenSound = sound;
    }

    public void setWaterExtinguishingSound(Sound sound) {
        this.waterExtinguishingSound = sound;
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

            // First vehicle is always a firetruck, others are random
            String spriteName;
            if (i == 0) {
                spriteName = FIRETRUCK_SPRITE;
            } else {
                // Randomly select from all available vehicles
                spriteName = VEHICLE_SPRITES[(int)(Math.random() * VEHICLE_SPRITES.length)];
            }

            Truck truck = new Truck(atlas, stationPos[0] + offsetX, stationPos[1] + offsetY, spriteName);
            truck.setRoute(route);
            truck.setStartDelay(i * 0.5f); // First truck (i=0) has 0 delay
            truck.setDrivingSound(truckDrivingSound);
            truck.setSirenSound(truckSirenSound);
            trucks.add(truck);
        }

        TruckMission mission = new TruckMission(trucks, fire, route);
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
                // Start playing water extinguishing sound and fire sound
                if (waterExtinguishingSound != null && mission.waterSoundId == -1) {
                    float waterVolume = SoundManager.calculateWaterExtinguishingVolume();
                    mission.waterSoundId = waterExtinguishingSound.loop(waterVolume);
                }
                mission.targetFire.startFireSound();
                if (arrivalListener != null) {
                    arrivalListener.onTruckArrived(mission.trucks.get(0), mission.targetFire, mission.trucks.size());
                }
                // Notify that all trucks from this mission have arrived
                if (allTrucksArrivedListener != null) {
                    allTrucksArrivedListener.onAllTrucksArrived(mission.targetFire);
                }
            }
            
            // Update water sound volume dynamically
            if (mission.waterSoundId != -1 && waterExtinguishingSound != null) {
                float waterVolume = SoundManager.calculateWaterExtinguishingVolume();
                waterExtinguishingSound.setVolume(mission.waterSoundId, waterVolume);
            }
            
            // Update fire sound volume dynamically
            if (mission.arrived) {
                mission.targetFire.updateFireSoundVolume();
            }

            // Update extinguishing progress
            if (mission.arrived && !mission.returning && !justExtinguished.contains(mission.targetFire)) {
                mission.missionStartTime += delta;
                boolean extinguished = mission.targetFire.updateExtinguishing(delta);

                if (extinguished) {
                    mission.targetFire.putOut(); // This also stops fire sound
                    mission.targetFire.resetAssignedTrucks();
                    justExtinguished.add(mission.targetFire);

                    // Stop water extinguishing sound
                    if (waterExtinguishingSound != null && mission.waterSoundId != -1) {
                        waterExtinguishingSound.stop(mission.waterSoundId);
                        mission.waterSoundId = -1;
                    }

                    if (extinguishCompleteListener != null) {
                        extinguishCompleteListener.onExtinguishComplete(mission.targetFire);
                    }

                    // Start return journey to station
                    mission.returning = true;
                    // Create reversed route (go backwards along the original path)
                    List<float[]> returnRoute = new ArrayList<>();
                    for (int i = mission.originalRoute.size() - 1; i >= 0; i--) {
                        returnRoute.add(mission.originalRoute.get(i).clone());
                    }

                    // Set return routes with staggered delays
                    for (int i = 0; i < mission.trucks.size(); i++) {
                        Truck truck = mission.trucks.get(i);
                        truck.setReturnRoute(returnRoute);
                        truck.setStartDelay(i * 0.5f); // Stagger return by 0.5 seconds each
                    }
                }
            }

            // Check if trucks have returned to station
            if (mission.returning && !mission.returnedToStation) {
                boolean allReturned = true;
                for (Truck truck : mission.trucks) {
                    if (!truck.hasArrived()) {
                        allReturned = false;
                        break;
                    }
                }

                if (allReturned) {
                    mission.returnedToStation = true;
                    // Return trucks to station availability
                    station.returnTrucks(mission.trucks.size());
                    station.addResponseTime(mission.missionStartTime / 60f);

                    // Mark mission for cleanup
                    completed.add(mission);
                }
            }
        }

        // Clean up completed missions (trucks that have returned to station)
        for (TruckMission mission : completed) {
            // Hide and dispose all trucks
            for (Truck truck : mission.trucks) {
                truck.hide();
                truck.dispose();
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

    public void stopAllSounds() {
        // Stop all active mission sounds
        for (TruckMission mission : activeMissions) {
            // Stop water extinguishing sound
            if (waterExtinguishingSound != null && mission.waterSoundId != -1) {
                waterExtinguishingSound.stop(mission.waterSoundId);
                mission.waterSoundId = -1;
            }
            // Stop fire sound
            mission.targetFire.stopFireSound();
            // Dispose all trucks (stops siren sounds)
            for (Truck truck : mission.trucks) {
                truck.dispose();
            }
        }
    }

}
