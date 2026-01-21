package si.um.feri.gasilci.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import si.um.feri.gasilci.data.FireStation;
import si.um.feri.gasilci.util.SoundManager;

public class StationPopupWindow extends Window {
    private final FireStation station;
    private Label availableTrucksLabel;
    private Label onMissionLabel;
    private Label occupancyLabel;
    private Label firesExtinguishedLabel;
    private Label avgResponseTimeLabel;

    public StationPopupWindow(FireStation station, Skin skin) {
        super("Fire Station", skin);
        this.station = station;
        
        setupUI(skin);
    }

    private void setupUI(Skin skin) {
        // Set window style - NOT modal so map can be moved
        setModal(false);
        setMovable(false);
        
        // Add close button to title bar
        getTitleTable().add(createCloseButton(skin)).padRight(0);
        
        // Create content table with dark blue/gray background
        Table contentTable = new Table(skin);
        contentTable.setBackground(skin.newDrawable("white", new Color(0.2f, 0.3f, 0.5f, 0.95f)));
        
        // Title
        Label titleLabel = new Label(station.name, skin, "default");
        titleLabel.setColor(Color.WHITE);
        titleLabel.setFontScale(1.2f);
        
        // Location
        Label locationLabel = new Label("Location: " + station.getLocation(), skin);
        locationLabel.setAlignment(Align.left);
        
        // Statistics
        Label statsTitle = new Label("--- Station Statistics ---", skin);
        statsTitle.setColor(Color.YELLOW);
        statsTitle.setAlignment(Align.center);
        
        availableTrucksLabel = new Label("Available Trucks: " + station.getAvailableTrucks() + "/" + station.getTotalTrucks(), skin);
        availableTrucksLabel.setAlignment(Align.left);
        
        onMissionLabel = new Label("Trucks on Mission: " + station.getOnMission(), skin);
        onMissionLabel.setAlignment(Align.left);
        
        occupancyLabel = new Label(String.format("Occupancy: %.1f%%", station.getOccupancyPercentage()), skin);
        occupancyLabel.setAlignment(Align.left);
        occupancyLabel.setColor(getOccupancyColor(station.getOccupancyPercentage()));
        
        firesExtinguishedLabel = new Label("Fires Extinguished: " + station.getFiresExtinguished(), skin);
        firesExtinguishedLabel.setAlignment(Align.left);
        
        avgResponseTimeLabel = new Label(String.format("Avg Response Time: %.1f min", station.getAverageResponseTime()), skin);
        avgResponseTimeLabel.setAlignment(Align.left);
        
        // Add to content table
        contentTable.pad(20);
        contentTable.add(titleLabel).center().padBottom(10).row();
        contentTable.add(locationLabel).left().padBottom(15).row();
        contentTable.add(statsTitle).center().padBottom(10).row();
        contentTable.add(availableTrucksLabel).left().padBottom(5).row();
        contentTable.add(onMissionLabel).left().padBottom(5).row();
        contentTable.add(occupancyLabel).left().padBottom(5).row();
        contentTable.add(firesExtinguishedLabel).left().padBottom(5).row();
        contentTable.add(avgResponseTimeLabel).left().padBottom(15).row();
        
        // Close button
        TextButton closeButton = new TextButton("CLOSE", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.playButtonClick();
                remove();
            }
        });
        
        contentTable.add(closeButton).width(200).height(40).padTop(10);
        
        add(contentTable);
        pack();
    }

    private Color getOccupancyColor(float occupancy) {
        if (occupancy >= 80) return Color.RED;
        if (occupancy >= 50) return Color.ORANGE;
        return Color.GREEN;
    }

    public void updateStats() {
        availableTrucksLabel.setText("Available Trucks: " + station.getAvailableTrucks() + "/" + station.getTotalTrucks());
        onMissionLabel.setText("Trucks on Mission: " + station.getOnMission());
        occupancyLabel.setText(String.format("Occupancy: %.1f%%", station.getOccupancyPercentage()));
        occupancyLabel.setColor(getOccupancyColor(station.getOccupancyPercentage()));
        firesExtinguishedLabel.setText("Fires Extinguished: " + station.getFiresExtinguished());
        avgResponseTimeLabel.setText(String.format("Avg Response Time: %.1f min", station.getAverageResponseTime()));
    }

    public void show(float screenX, float screenY, float stageWidth, float stageHeight) {
        // Position near the click but keep it on screen
        float x = Math.min(screenX + 20, stageWidth - getWidth() - 10);
        float y = Math.min(screenY - 20, stageHeight - getHeight() - 10);
        x = Math.max(10, x);
        y = Math.max(10, y);
        
        setPosition(x, y);
    }

    private TextButton createCloseButton(Skin skin) {
        TextButton closeButton = new TextButton("X", skin);
        closeButton.padTop(0).padBottom(0).padLeft(2).padRight(2);
        closeButton.getLabelCell().pad(0);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.playButtonClick();
                remove();
            }
        });
        return closeButton;
    }
}
