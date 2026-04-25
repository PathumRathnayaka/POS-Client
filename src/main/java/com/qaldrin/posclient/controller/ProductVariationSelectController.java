package com.qaldrin.posclient.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import com.qaldrin.posclient.dto.CurrentStockBatchDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProductVariationSelectController {

    @FXML
    private Label productNameLabel;
    @FXML
    private ComboBox<String> variationComboBox;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnSubmit;

    private List<CurrentStockBatchDTO> allBatches;
    private Map<String, CurrentStockBatchDTO> variationToBestBatchMap = new HashMap<>();
    private Consumer<CurrentStockBatchDTO> onBatchSelected;
    private Runnable onCancel;

    @FXML
    public void initialize() {
        // Style the ComboBox cells to ensure visibility in dark theme
        variationComboBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #2c3e50;");
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setStyle("-fx-background-color: #2c3e50; -fx-padding: 5 10;");
                }
            }
        });

        variationComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                }
            }
        });
    }

    public void setData(String productName, List<CurrentStockBatchDTO> batches) {
        this.productNameLabel.setText(productName);
        this.allBatches = batches;

        // Group and prioritize batches (FIFO)
        processBatches();

        // Populate ComboBox
        List<String> displayStrings = new ArrayList<>(variationToBestBatchMap.keySet());
        Collections.sort(displayStrings);
        variationComboBox.setItems(FXCollections.observableArrayList(displayStrings));

        if (!displayStrings.isEmpty()) {
            variationComboBox.getSelectionModel().selectFirst();
        }
    }

    private void processBatches() {
        variationToBestBatchMap.clear();

        // Group batches by variation name
        Map<String, List<CurrentStockBatchDTO>> grouped = allBatches.stream()
                .filter(b -> b.getQuantity().doubleValue() > 0)
                .collect(Collectors.groupingBy(
                        b -> (b.getVariation() == null || b.getVariation().isEmpty()) ? "DEFAULT" : b.getVariation()));

        for (Map.Entry<String, List<CurrentStockBatchDTO>> entry : grouped.entrySet()) {
            String variationName = entry.getKey();
            List<CurrentStockBatchDTO> variationBatches = entry.getValue();

            // Prioritize by created date (FIFO)
            variationBatches.sort((b1, b2) -> {
                LocalDateTime c1 = b1.getCreatedDate();
                LocalDateTime c2 = b2.getCreatedDate();
                if (c1 == null && c2 == null)
                    return 0;
                if (c1 == null)
                    return 1;
                if (c2 == null)
                    return -1;
                return c1.compareTo(c2);
            });

            // The first one is our FIFO "best" batch
            CurrentStockBatchDTO bestBatch = variationBatches.get(0);

            // Format display label: "Variation → $ Price"
            String displayLabel = String.format("%s → $%.2f",
                    variationName,
                    bestBatch.getSalePrice() != null ? bestBatch.getSalePrice().doubleValue() : 0.0);

            variationToBestBatchMap.put(displayLabel, bestBatch);
        }
    }

    @FXML
    public void onSubmitClick(ActionEvent event) {
        String selectedVariation = variationComboBox.getSelectionModel().getSelectedItem();
        if (selectedVariation != null && onBatchSelected != null) {
            CurrentStockBatchDTO bestBatch = variationToBestBatchMap.get(selectedVariation);
            onBatchSelected.accept(bestBatch);
        }
    }

    @FXML
    public void onCancelClick(ActionEvent event) {
        if (onCancel != null) {
            onCancel.run();
        }
    }

    public void setOnBatchSelected(Consumer<CurrentStockBatchDTO> callback) {
        this.onBatchSelected = callback;
    }

    public void setOnCancel(Runnable callback) {
        this.onCancel = callback;
    }
}
