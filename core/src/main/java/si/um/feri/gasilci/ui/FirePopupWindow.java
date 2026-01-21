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

import si.um.feri.gasilci.data.FirePoint;
import si.um.feri.gasilci.util.SoundManager;

public class FirePopupWindow extends Window {
    private final FirePoint firePoint;
    private Runnable onPutOut;
    private final int availableTrucks;
    private int selectedTrucks = 1;
    private Label truckSelectionLabel;
    private TextButton decreaseButton;
    private TextButton increaseButton;
    private TextButton putOutButton;

    public FirePopupWindow(FirePoint firePoint, Skin skin, int availableTrucks) {
        super("Fire Information", skin);
        this.firePoint = firePoint;
        this.availableTrucks = availableTrucks;
        int remainingNeeded = firePoint.getRemainingTrucksNeeded();
        int maxCanSend = Math.min(availableTrucks, remainingNeeded);
        this.selectedTrucks = Math.min(1, maxCanSend);
        
        setupUI(skin);
    }

    private void setupUI(Skin skin) {
        // Set window style - NOT modal so map can be moved
        setModal(false);
        setMovable(false);
        
        // Add close button to title bar
        getTitleTable().add(createCloseButton(skin)).padRight(0);
        
        // Create content table with brown background
        Table contentTable = new Table(skin);
        contentTable.setBackground(skin.newDrawable("white", new Color(0.6f, 0.4f, 0.2f, 0.95f)));
        
        // Add fire information
        Label nameLabel = new Label("Name: " + firePoint.name, skin);
        nameLabel.setAlignment(Align.left);
        
        Label locationLabel = new Label("Location: " + firePoint.getLocation(), skin);
        locationLabel.setAlignment(Align.left);
        
        Label severityLabel = new Label("Severity: Level " + firePoint.severity, skin);
        severityLabel.setAlignment(Align.left);
        severityLabel.setColor(getSeverityColor(firePoint.severity));
        
        Label accessLabel = new Label("Accessibility: " + capitalizeFirst(firePoint.accessibility), skin);
        accessLabel.setAlignment(Align.left);
        
        Label durationLabel = new Label(String.format("Duration: %.1f minutes", firePoint.duration), skin);
        durationLabel.setAlignment(Align.left);

        Label requiredTrucksLabel = new Label("Required Trucks: " + firePoint.getRequiredTrucks(), skin);
        requiredTrucksLabel.setAlignment(Align.left);
        requiredTrucksLabel.setColor(Color.CYAN);

        Label assignedTrucksLabel = new Label("Already Dispatched: " + firePoint.getAssignedTrucks(), skin);
        assignedTrucksLabel.setAlignment(Align.left);
        assignedTrucksLabel.setColor(Color.ORANGE);

        Label availableLabel = new Label("Available in Station: " + availableTrucks, skin);
        availableLabel.setAlignment(Align.left);
        availableLabel.setColor(availableTrucks > 0 ? Color.GREEN : Color.RED);
        
        // Add to content table
        contentTable.pad(20);
        contentTable.add(nameLabel).left().padBottom(10).row();
        contentTable.add(locationLabel).left().padBottom(10).row();
        contentTable.add(severityLabel).left().padBottom(10).row();
        contentTable.add(accessLabel).left().padBottom(10).row();
        contentTable.add(durationLabel).left().padBottom(10).row();
        contentTable.add(requiredTrucksLabel).left().padBottom(10).row();
        contentTable.add(assignedTrucksLabel).left().padBottom(10).row();
        contentTable.add(availableLabel).left().padBottom(15).row();

        // Truck selection section
        int remainingNeeded = firePoint.getRemainingTrucksNeeded();
        int maxCanSend = Math.min(availableTrucks, remainingNeeded);
        
        if (availableTrucks > 0 && remainingNeeded > 0) {
            Label selectionTitle = new Label("--- Dispatch Trucks ---", skin);
            selectionTitle.setColor(Color.YELLOW);
            contentTable.add(selectionTitle).center().padBottom(10).row();

            // Truck selector
            Table selectorTable = new Table();
            decreaseButton = new TextButton("-", skin);
            increaseButton = new TextButton("+", skin);
            truckSelectionLabel = new Label(selectedTrucks + " Truck(s)", skin);
            truckSelectionLabel.setColor(Color.WHITE);

            decreaseButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SoundManager.playButtonClick();
                    if (selectedTrucks > 1) {
                        selectedTrucks--;
                        updateTruckSelection();
                    }
                }
            });

            increaseButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SoundManager.playButtonClick();
                    if (selectedTrucks < maxCanSend) {
                        selectedTrucks++;
                        updateTruckSelection();
                    }
                }
            });

            selectorTable.add(decreaseButton).width(40).height(40).padRight(10);
            selectorTable.add(truckSelectionLabel).width(120).center();
            selectorTable.add(increaseButton).width(40).height(40).padLeft(10);

            contentTable.add(selectorTable).center().padBottom(15).row();

            // Create button
            putOutButton = new TextButton("PUT OUT THE FIRE!", skin);
            putOutButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SoundManager.playButtonClick();
                    handlePutOut();
                }
            });

            contentTable.add(putOutButton).width(250).height(50).padTop(10);
        } else if (remainingNeeded <= 0) {
            Label maxTrucksLabel = new Label("MAX TRUCKS ALREADY DISPATCHED!", skin);
            maxTrucksLabel.setColor(Color.ORANGE);
            contentTable.add(maxTrucksLabel).center().padTop(10);
        } else {
            Label noTrucksLabel = new Label("NO TRUCKS AVAILABLE!", skin);
            noTrucksLabel.setColor(Color.RED);
            contentTable.add(noTrucksLabel).center().padTop(10);
        }
        
        add(contentTable);
        pack();
    }

    private void updateTruckSelection() {
        truckSelectionLabel.setText(selectedTrucks + " Truck(s)");
        int remainingNeeded = firePoint.getRemainingTrucksNeeded();
        int maxCanSend = Math.min(availableTrucks, remainingNeeded);
        decreaseButton.setDisabled(selectedTrucks <= 1);
        increaseButton.setDisabled(selectedTrucks >= maxCanSend);
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

    private Color getSeverityColor(int severity) {
        switch (severity) {
            case 1: return Color.YELLOW;
            case 2: return Color.ORANGE;
            case 3: return Color.RED;
            default: return Color.WHITE;
        }
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void handlePutOut() {
        if (onPutOut != null) {
            onPutOut.run();
        }
        remove();
    }

    public void setOnPutOut(Runnable callback) {
        this.onPutOut = callback;
    }

    public int getSelectedTrucks() {
        return selectedTrucks;
    }

    public void show(float screenX, float screenY, float stageWidth, float stageHeight) {
        // Position near the click but keep it on screen
        float x = Math.min(screenX + 20, stageWidth - getWidth() - 10);
        float y = Math.min(screenY - 20, stageHeight - getHeight() - 10);
        x = Math.max(10, x);
        y = Math.max(10, y);
        
        setPosition(x, y);
    }
}
