package si.um.feri.gasilci.entities;

import java.util.List;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import si.um.feri.gasilci.util.SoundManager;

public class Truck {
    private final Sprite sprite;
    private List<float[]> route;
    private int currentWaypoint = 0;
    private boolean arrived = false;
    private static final float SPEED = 1.5f;
    private float startDelay = 0;
    private boolean started = false;
    private boolean visible = true;
    private Sound drivingSound;
    private long drivingSoundId = -1;
    private Sound sirenSound;
    private long sirenSoundId = -1;

    public Truck(TextureAtlas atlas, float startX, float startY) {
        this.sprite = atlas.createSprite("images/truck-up-1");

        if (this.sprite == null) {
            throw new RuntimeException("Truck sprite 'images/truck-up-1' not found in atlas");
        }

        float targetHeight = 0.4f;
        float aspectRatio = sprite.getRegionWidth() / (float) sprite.getRegionHeight();
        float targetWidth = targetHeight * aspectRatio;

        sprite.setSize(targetWidth, targetHeight);
        sprite.setOriginCenter();
        sprite.setPosition(startX - sprite.getWidth() / 2, startY - sprite.getHeight() / 2);
    }

    public void setRoute(List<float[]> route) {
        this.route = route;
        this.currentWaypoint = 0;
        this.arrived = false;
    }

    public void setStartDelay(float delay) {
        this.startDelay = delay;
        this.started = (delay <= 0);
    }

    public void setDrivingSound(Sound sound) {
        this.drivingSound = sound;
    }

    public void setSirenSound(Sound sound) {
        this.sirenSound = sound;
    }

    public void update(float delta) {
        if (arrived || route == null || route.isEmpty()) return;

        if (!started) {
            startDelay -= delta;
            if (startDelay <= 0) {
                started = true;
                // Start playing driving sound and siren when truck starts moving
                if (drivingSound != null && drivingSoundId == -1) {
                    float drivingVolume = SoundManager.calculateTruckDrivingVolume();
                    if (drivingVolume > 0) {
                        drivingSoundId = drivingSound.loop(drivingVolume);
                    }
                }
                if (sirenSound != null && sirenSoundId == -1) {
                    float sirenVolume = SoundManager.calculateTruckSirenVolume();
                    if (sirenVolume > 0) {
                        sirenSoundId = sirenSound.loop(sirenVolume);
                    }
                }
            } else {
                return;
            }
        }
        
        // Update sound volumes dynamically
        if (drivingSoundId != -1 && drivingSound != null) {
            float drivingVolume = SoundManager.calculateTruckDrivingVolume();
            drivingSound.setVolume(drivingSoundId, drivingVolume);
        }
        if (sirenSoundId != -1 && sirenSound != null) {
            float sirenVolume = SoundManager.calculateTruckSirenVolume();
            sirenSound.setVolume(sirenSoundId, sirenVolume);
        }

        if (currentWaypoint >= route.size()) {
            arrived = true;
            // Stop driving sound and siren when truck arrives
            if (drivingSound != null && drivingSoundId != -1) {
                drivingSound.stop(drivingSoundId);
                drivingSoundId = -1;
            }
            if (sirenSound != null && sirenSoundId != -1) {
                sirenSound.stop(sirenSoundId);
                sirenSoundId = -1;
            }
            return;
        }

        float[] target = route.get(currentWaypoint);
        float tx = target[0];
        float ty = target[1];
        float cx = sprite.getX() + sprite.getWidth() / 2;
        float cy = sprite.getY() + sprite.getHeight() / 2;

        float dx = tx - cx;
        float dy = ty - cy;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        updateSpriteDirection(dx, dy);

        if (dist < 0.05f) {
            currentWaypoint++;
        } else {
            float move = SPEED * delta;
            if (move > dist) move = dist;
            float nx = cx + (dx / dist) * move;
            float ny = cy + (dy / dist) * move;
            sprite.setPosition(nx - sprite.getWidth() / 2, ny - sprite.getHeight() / 2);
        }
    }

    private void updateSpriteDirection(float dx, float dy) {
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;
        sprite.setRotation(angle - 90);
    }

    public boolean hasArrived() {
        return arrived;
    }

    public boolean hasStarted() {
        return started;
    }

    public boolean isVisible() {
        return visible;
    }

    public void hide() {
        this.visible = false;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void dispose() {
        // Stop sounds if still playing
        if (drivingSound != null && drivingSoundId != -1) {
            drivingSound.stop(drivingSoundId);
            drivingSoundId = -1;
        }
        if (sirenSound != null && sirenSoundId != -1) {
            sirenSound.stop(sirenSoundId);
            sirenSoundId = -1;
        }
    }
}
