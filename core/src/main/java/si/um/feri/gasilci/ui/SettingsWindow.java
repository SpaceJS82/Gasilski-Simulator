package si.um.feri.gasilci.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import si.um.feri.gasilci.util.SoundManager;

public class SettingsWindow extends Window {
    private static final String PREFS_NAME = "GasilskiSimulatorSettings";
    private Preferences prefs;
    
    private CheckBox masterSoundCheckBox;
    private Slider masterVolumeSlider;
    private Slider buttonSoundSlider;
    private Slider truckDrivingSlider;
    private Slider truckSirenSlider;
    private Slider waterExtinguishingSlider;
    private Slider fireAmbientSlider;
    
    private Label masterVolumeLabel;
    private Label buttonSoundLabel;
    private Label truckDrivingLabel;
    private Label truckSirenLabel;
    private Label waterExtinguishingLabel;
    private Label fireAmbientLabel;

    public SettingsWindow(Skin skin) {
        super("Settings", skin);
        
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        
        setModal(true);
        setMovable(false);
        
        // Create UI elements
        Table contentTable = new Table(skin);
        contentTable.defaults().pad(5);
        
        // Master Sound On/Off
        masterSoundCheckBox = new CheckBox(" Master Sound", skin);
        masterSoundCheckBox.setChecked(SoundManager.isMasterSoundEnabled());
        masterSoundCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean enabled = masterSoundCheckBox.isChecked();
                SoundManager.setMasterSoundEnabled(enabled);
                updateSlidersEnabled(enabled);
                saveSettings();
            }
        });
        
        // Master Volume Slider
        masterVolumeSlider = new Slider(0, 1, 0.1f, false, skin);
        masterVolumeSlider.setValue(SoundManager.getMasterVolume());
        masterVolumeLabel = new Label(formatVolume(masterVolumeSlider.getValue()), skin);
        masterVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = masterVolumeSlider.getValue();
                SoundManager.setMasterVolume(volume);
                masterVolumeLabel.setText(formatVolume(volume));
                saveSettings();
            }
        });
        
        // Individual sound sliders
        buttonSoundSlider = createVolumeSlider(SoundManager.getButtonVolume());
        buttonSoundLabel = new Label(formatVolume(buttonSoundSlider.getValue()), skin);
        buttonSoundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = buttonSoundSlider.getValue();
                SoundManager.setButtonVolume(volume);
                buttonSoundLabel.setText(formatVolume(volume));
                saveSettings();
            }
        });
        
        truckDrivingSlider = createVolumeSlider(SoundManager.getTruckDrivingVolume());
        truckDrivingLabel = new Label(formatVolume(truckDrivingSlider.getValue()), skin);
        truckDrivingSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = truckDrivingSlider.getValue();
                SoundManager.setTruckDrivingVolume(volume);
                truckDrivingLabel.setText(formatVolume(volume));
                saveSettings();
            }
        });
        
        truckSirenSlider = createVolumeSlider(SoundManager.getTruckSirenVolume());
        truckSirenLabel = new Label(formatVolume(truckSirenSlider.getValue()), skin);
        truckSirenSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = truckSirenSlider.getValue();
                SoundManager.setTruckSirenVolume(volume);
                truckSirenLabel.setText(formatVolume(volume));
                saveSettings();
            }
        });
        
        waterExtinguishingSlider = createVolumeSlider(SoundManager.getWaterExtinguishingVolume());
        waterExtinguishingLabel = new Label(formatVolume(waterExtinguishingSlider.getValue()), skin);
        waterExtinguishingSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = waterExtinguishingSlider.getValue();
                SoundManager.setWaterExtinguishingVolume(volume);
                waterExtinguishingLabel.setText(formatVolume(volume));
                saveSettings();
            }
        });
        
        fireAmbientSlider = createVolumeSlider(SoundManager.getFireAmbientVolume());
        fireAmbientLabel = new Label(formatVolume(fireAmbientSlider.getValue()), skin);
        fireAmbientSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = fireAmbientSlider.getValue();
                SoundManager.setFireAmbientVolume(volume);
                fireAmbientLabel.setText(formatVolume(volume));
                saveSettings();
            }
        });
        
        // Layout
        contentTable.row();
        contentTable.add(masterSoundCheckBox).left().colspan(3);
        
        contentTable.row();
        contentTable.add(new Label("Master Volume:", skin)).left();
        contentTable.add(masterVolumeSlider).width(200).padLeft(10);
        contentTable.add(masterVolumeLabel).width(50).left().padLeft(10);
        
        contentTable.row();
        contentTable.add(new Label("Button Click:", skin)).left();
        contentTable.add(buttonSoundSlider).width(200).padLeft(10);
        contentTable.add(buttonSoundLabel).width(50).left().padLeft(10);
        
        contentTable.row();
        contentTable.add(new Label("Truck Driving:", skin)).left();
        contentTable.add(truckDrivingSlider).width(200).padLeft(10);
        contentTable.add(truckDrivingLabel).width(50).left().padLeft(10);
        
        contentTable.row();
        contentTable.add(new Label("Truck Siren:", skin)).left();
        contentTable.add(truckSirenSlider).width(200).padLeft(10);
        contentTable.add(truckSirenLabel).width(50).left().padLeft(10);
        
        contentTable.row();
        contentTable.add(new Label("Water Spray:", skin)).left();
        contentTable.add(waterExtinguishingSlider).width(200).padLeft(10);
        contentTable.add(waterExtinguishingLabel).width(50).left().padLeft(10);
        
        contentTable.row();
        contentTable.add(new Label("Fire Ambient:", skin)).left();
        contentTable.add(fireAmbientSlider).width(200).padLeft(10);
        contentTable.add(fireAmbientLabel).width(50).left().padLeft(10);
        
        // Close button
        TextButton closeButton = new TextButton("Close", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.playButtonClick();
                remove();
            }
        });
        
        contentTable.row();
        contentTable.add(closeButton).colspan(3).center().padTop(20).width(100).height(40);
        
        add(contentTable).pad(20);
        pack();
        
        updateSlidersEnabled(SoundManager.isMasterSoundEnabled());
    }
    
    private Slider createVolumeSlider(float initialValue) {
        Slider slider = new Slider(0, 1, 0.1f, false, getSkin());
        slider.setValue(initialValue);
        return slider;
    }
    
    private String formatVolume(float volume) {
        return String.format("%d%%", (int)(volume * 100));
    }
    
    private void updateSlidersEnabled(boolean enabled) {
        masterVolumeSlider.setDisabled(!enabled);
        buttonSoundSlider.setDisabled(!enabled);
        truckDrivingSlider.setDisabled(!enabled);
        truckSirenSlider.setDisabled(!enabled);
        waterExtinguishingSlider.setDisabled(!enabled);
        fireAmbientSlider.setDisabled(!enabled);
    }
    
    private void saveSettings() {
        prefs.putBoolean("masterSoundEnabled", SoundManager.isMasterSoundEnabled());
        prefs.putFloat("masterVolume", SoundManager.getMasterVolume());
        prefs.putFloat("buttonVolume", SoundManager.getButtonVolume());
        prefs.putFloat("truckDrivingVolume", SoundManager.getTruckDrivingVolume());
        prefs.putFloat("truckSirenVolume", SoundManager.getTruckSirenVolume());
        prefs.putFloat("waterExtinguishingVolume", SoundManager.getWaterExtinguishingVolume());
        prefs.putFloat("fireAmbientVolume", SoundManager.getFireAmbientVolume());
        prefs.flush();
    }
    
    public void show(float screenWidth, float screenHeight) {
        setPosition(
            (screenWidth - getWidth()) / 2,
            (screenHeight - getHeight()) / 2
        );
    }
    
    public static void loadSettings() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        SoundManager.setMasterSoundEnabled(prefs.getBoolean("masterSoundEnabled", true));
        SoundManager.setMasterVolume(prefs.getFloat("masterVolume", 1.0f));
        SoundManager.setButtonVolume(prefs.getFloat("buttonVolume", 1.0f));
        SoundManager.setTruckDrivingVolume(prefs.getFloat("truckDrivingVolume", 1.0f));
        SoundManager.setTruckSirenVolume(prefs.getFloat("truckSirenVolume", 1.0f));
        SoundManager.setWaterExtinguishingVolume(prefs.getFloat("waterExtinguishingVolume", 1.0f));
        SoundManager.setFireAmbientVolume(prefs.getFloat("fireAmbientVolume", 1.0f));
    }
}
