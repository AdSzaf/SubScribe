package com.example.subscribe.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.application.Platform;

import com.example.subscribe.models.Subscription;
import com.example.subscribe.models.Category;
import com.example.subscribe.services.CurrencyService;
import com.example.subscribe.services.PaymentApiService;
import com.example.subscribe.services.SubscriptionService;
import com.example.subscribe.utils.ConfigManager;
import com.example.subscribe.utils.ICalendarExporter;
import com.example.subscribe.events.CurrencyChangedEvent;
import com.example.subscribe.events.EventBusManager;
import com.example.subscribe.events.SubscriptionAddedEvent;
import com.google.common.eventbus.Subscribe;
import com.example.subscribe.events.SubscriptionUpdatedEvent;
import com.example.subscribe.events.PaymentDueEvent;
import com.example.subscribe.events.CurrencyChangedEvent;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class MainController implements Initializable {

    // FXML Injected Components
    @FXML private Label totalMonthlyCostLabel;
    @FXML private Label activeSubscriptionsLabel;
    @FXML private Label dueThisWeekLabel;
    @FXML private Label statusLabel;
    @FXML private Label lastUpdateLabel;

    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TableView<Subscription> subscriptionsTable;
    @FXML private TableColumn<Subscription, String> nameColumn;
    @FXML private TableColumn<Subscription, String> costColumn;
    @FXML private TableColumn<Subscription, String> currencyColumn;
    @FXML private TableColumn<Subscription, String> categoryColumn;
    @FXML private TableColumn<Subscription, String> nextPaymentColumn;
    @FXML private TableColumn<Subscription, String> statusColumn;
    @FXML private TableColumn<Subscription, String> actionsColumn;

    @FXML private Button addSubscriptionBtn;

    // Data and Services
    private ObservableList<Subscription> subscriptionsList;
    private FilteredList<Subscription> filteredSubscriptions;
    private SubscriptionService subscriptionService;

    private BigDecimal currentExchangeRate = BigDecimal.ONE;
    private String targetCurrency = "PLN"; 
    private Map<String, BigDecimal> exchangeRates = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        subscriptionService = new SubscriptionService();

        // Register for events
        EventBusManager.getInstance().register(this);

        // Initialize data collections
        subscriptionsList = FXCollections.observableArrayList();
        filteredSubscriptions = new FilteredList<>(subscriptionsList);

        PaymentApiService paymentApiService = new PaymentApiService();
        paymentApiService.fetchTransactionsAsync().thenAccept(transactions -> {
            Platform.runLater(() -> {
                showAlert("Fetched Transactions", "Fetched " + transactions.size() + " transactions from PayPal/Stripe (mock).");
            });
        });

        // Fetch exchange rate asynchronously
        String baseCurrency = ConfigManager.get("currency.base", "USD");
        targetCurrency = ConfigManager.get("app.currency", "PLN");

        fetchAndUpdateExchangeRate();

        // Setup UI components
        setupTableColumns();
        setupFilters();

        // Bind filtered list to table
        subscriptionsTable.setItems(filteredSubscriptions);

        // Load initial data
        loadSubscriptions();

        // Update status
        updateStatus("Application loaded successfully");
        updateLastUpdateTime();
    }

    private void setupTableColumns() {
        // Name column
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Cost column with formatting
        costColumn.setCellValueFactory(cellData -> {
            BigDecimal cost = cellData.getValue().getCost();
            return new SimpleStringProperty(cost != null ? cost.toString() : "0.00");
        });

        // Currency column
        currencyColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));

        // Category column
        categoryColumn.setCellValueFactory(cellData -> {
            Category category = cellData.getValue().getCategory();
            return new SimpleStringProperty(category != null ? category.getDisplayName() : "Other");
        });

        // Next payment column with date formatting
        nextPaymentColumn.setCellValueFactory(cellData -> {
            LocalDate nextPayment = cellData.getValue().getNextPaymentDate();
            if (nextPayment != null) {
                return new SimpleStringProperty(nextPayment.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            return new SimpleStringProperty("N/A");
        });

        // Status column
        statusColumn.setCellValueFactory(cellData -> {
            boolean active = cellData.getValue().isActive();
            return new SimpleStringProperty(active ? "Active" : "Inactive");
        });

        // Actions column with buttons
        actionsColumn.setCellFactory(col -> new TableCell<Subscription, String>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setOnAction(e -> editSubscription(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> deleteSubscription(getTableRow().getItem()));
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new javafx.scene.layout.HBox(5, editBtn, deleteBtn));
                }
            }
        });
    }

    private void setupFilters() {
        // Setup category filter
        categoryFilter.getItems().addAll(Category.values());
        categoryFilter.getItems().add(0, null); // "All categories" option
        categoryFilter.setConverter(new javafx.util.StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category == null ? "All Categories" : category.getDisplayName();
            }

            @Override
            public Category fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });

        // Setup status filter
        statusFilter.getItems().addAll("All", "Active", "Inactive");
        statusFilter.setValue("All");

        // Setup filtering logic
        filteredSubscriptions.setPredicate(subscription -> {
            // Search filter
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!subscription.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return false;
                }
            }

            // Category filter
            Category selectedCategory = categoryFilter.getValue();
            if (selectedCategory != null && !selectedCategory.equals(subscription.getCategory())) {
                return false;
            }

            // Status filter
            String selectedStatus = statusFilter.getValue();
            if (!"All".equals(selectedStatus)) {
                boolean isActive = "Active".equals(selectedStatus);
                if (subscription.isActive() != isActive) {
                    return false;
                }
            }

            return true;
        });
    }

    // ---------------------------------------------------------------------------FXML------------------------------------------------------------------

    @FXML
    private void addSubscription() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/subscribe/fxml/add-subscription.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Add New Subscription");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(addSubscriptionBtn.getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Could not open Add Subscription dialog: " + e.getMessage());
        }
    }

    @FXML
    private void filterSubscriptions() {
        filteredSubscriptions.setPredicate(filteredSubscriptions.getPredicate());
        updateSummaryCards();
    }

    @FXML
    private void refreshSubscriptions() {
        loadSubscriptions();
        updateStatus("Subscriptions refreshed");
        updateLastUpdateTime();
    }

    @FXML
    private void importSubscriptions() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Subscriptions");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        Stage stage = (Stage) addSubscriptionBtn.getScene().getWindow();
        var file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            // TODO: Implement CSV import logic using Apache Commons CSV
            showAlert("Info", "Import functionality will be implemented soon!");
        }
    }

    @FXML
    private void exportSubscriptions() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Subscriptions");
        fileChooser.setInitialFileName("subscriptions.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        Stage stage = (Stage) addSubscriptionBtn.getScene().getWindow();
        var file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            // TODO: Implement CSV export logic using Apache Commons CSV
            showAlert("Info", "Export functionality will be implemented soon!");
        }
    }

    @FXML
    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/subscribe/fxml/settings.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(addSubscriptionBtn.getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Could not open Settings dialog: " + e.getMessage());
        }
    }

    @FXML
    private void openStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statistics.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Statistics");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(addSubscriptionBtn.getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Could not open Statistics dialog: " + e.getMessage());
        }
    }

    @FXML
    private void openCalendarView() {
        // TODO: Implement calendar view
        showAlert("Info", "Calendar view will be implemented soon!");
    }

    @FXML
    private void exitApplication() {
        Platform.exit();
    }

    @FXML
    private void showAbout() {
        showAlert("About SubScribe",
                "SubScribe - Subscription Management Application\n" +
                        "Version 1.0\n" +
                        "Built with JavaFX");
    }

    @FXML
    private void exportPaymentsToCalendar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Payments to Calendar");
        fileChooser.setInitialFileName("subscriptions.ics");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("iCalendar Files", "*.ics")
        );
        Stage stage = (Stage) addSubscriptionBtn.getScene().getWindow();
        var file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            Task<Void> exportTask = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        ICalendarExporter.exportSubscriptionsToICS(subscriptionsList, file.getAbsolutePath());
                        EventBusManager.getInstance().post(new com.example.subscribe.events.CalendarExportedEvent(true, "Calendar exported successfully!"));
                    } catch (IOException e) {
                        EventBusManager.getInstance().post(new com.example.subscribe.events.CalendarExportedEvent(false, "Failed to export calendar: " + e.getMessage()));
                    }
                    return null;
                }
            };
            Thread thread = new Thread(exportTask);
            thread.setDaemon(true);
            thread.start();
        }
    }


    // -----------------------------------------------------------------------------Business Logic Methods--------------------------------------------------------------------

    private void loadSubscriptions() {
        updateStatus("Loading subscriptions...");

            subscriptionService.getAllSubscriptionsAsync().thenAccept(subscriptions -> {
            Platform.runLater(() -> {
                subscriptionsList.clear();
                subscriptionsList.addAll(subscriptions);
                updateAllExchangeRatesAndSummary(); // <-- instead of updateSummaryCards()
                updateStatus("Loaded " + subscriptions.size() + " subscriptions");
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                updateStatus("Failed to load subscriptions");
                showAlert("Error", "Failed to load subscriptions: " + ex.getMessage());
            });
            return null;
        });

        // Create background task to load subscriptions
        Task<List<Subscription>> loadTask = new Task<List<Subscription>>() {
            @Override
            protected List<Subscription> call() throws Exception {
                // Simulate loading time
                Thread.sleep(500);

                // TODO: Replace with actual service call
                // return subscriptionService.getAllSubscriptions();

                // For now, return sample data
               // return createSampleData();
               return subscriptionService.getAllSubscriptions();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    subscriptionsList.clear();
                    subscriptionsList.addAll(getValue());
                    updateAllExchangeRatesAndSummary();
                    updateStatus("Loaded " + getValue().size() + " subscriptions");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    updateStatus("Failed to load subscriptions");
                    showAlert("Error", "Failed to load subscriptions: " + getException().getMessage());
                });
            }
        };

        // Run task in background thread
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void updateSummaryCards() {
        BigDecimal totalMonthlyCost = BigDecimal.ZERO;
        int activeCount = 0;
        int dueThisWeek = 0;
        LocalDate oneWeekFromNow = LocalDate.now().plusWeeks(1);

        for (Subscription sub : filteredSubscriptions) {
            if (sub.isActive()) {
                activeCount++;
                if (sub.getCost() != null) {
                    BigDecimal rate = sub.getCurrency().equals(targetCurrency)
                        ? BigDecimal.ONE
                        : exchangeRates.getOrDefault(sub.getCurrency(), BigDecimal.ONE);
                    totalMonthlyCost = totalMonthlyCost.add(sub.getCost().multiply(rate));
                }
                if (sub.getNextPaymentDate() != null &&
                        sub.getNextPaymentDate().isBefore(oneWeekFromNow)) {
                    dueThisWeek++;
                }
            }
        }

        totalMonthlyCostLabel.setText(targetCurrency + " " + totalMonthlyCost.toString());
        activeSubscriptionsLabel.setText(String.valueOf(activeCount));
        dueThisWeekLabel.setText(String.valueOf(dueThisWeek));
    }

    private void editSubscription(Subscription subscription) {
    if (subscription == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/subscribe/fxml/edit-subscription.fxml"));
            Scene scene = new Scene(loader.load());

            EditSubscriptionController controller = loader.getController();
            controller.setSubscription(subscription);

            Stage stage = new Stage();
            stage.setTitle("Edit Subscription");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(addSubscriptionBtn.getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Could not open Edit Subscription dialog: " + e.getMessage());
        }
    }

    private void deleteSubscription(Subscription subscription) {
        if (subscription == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Subscription");
        confirmAlert.setContentText("Are you sure you want to delete '" + subscription.getName() + "'?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                subscriptionService.deleteSubscription(subscription.getId());
                loadSubscriptions();
                updateStatus("Deleted subscription: " + subscription.getName());
            }
        });
    }

    //Currency Exchange Rate Fetching
    private void fetchAndUpdateExchangeRate() {
        String baseCurrency = "USD"; // or ConfigManager.get("currency.base", "USD");
        targetCurrency = ConfigManager.get("app.currency", "PLN");

        if (baseCurrency.equals(targetCurrency)) {
            currentExchangeRate = BigDecimal.ONE;
            updateStatus("Using same currency for base and target: " + baseCurrency);
            updateSummaryCards();
        } else {
            updateStatus("Fetching exchange rate from " + baseCurrency + " to " + targetCurrency);
            CurrencyService currencyService = new CurrencyService();
            currencyService.getExchangeRateAsync(baseCurrency, targetCurrency).thenAccept(rate -> {
                currentExchangeRate = rate;
                Platform.runLater(() -> {
                    showAlert("Exchange Rate", baseCurrency + "/" + targetCurrency + " = " + rate);
                    updateSummaryCards();
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> showAlert("Error", "Failed to fetch exchange rate: " + ex.getMessage()));
                return null;
            });
        }
    }
    private void updateAllExchangeRatesAndSummary() {
         targetCurrency = ConfigManager.get("app.currency", "PLN");

        Set<String> currencies = subscriptionsList.stream()
            .map(Subscription::getCurrency)
            .filter(c -> c != null && !c.equals(targetCurrency))
            .collect(Collectors.toSet());

        CurrencyService currencyService = new CurrencyService();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String currency : currencies) {
            futures.add(currencyService.getExchangeRateAsync(currency, targetCurrency)
                .thenAccept(rate -> exchangeRates.put(currency, rate))
                .exceptionally(ex -> {
                    exchangeRates.put(currency, BigDecimal.ONE); // fallback
                    return null;
                }));
        }

        // When all rates are fetched, update summary
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() ->
            Platform.runLater(this::updateSummaryCards)
        );
    }
    // Utility Methods

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void updateLastUpdateTime() {
        lastUpdateLabel.setText("Last updated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ---------------------------------------------------------------------Event Bus Subscribers-------------------------------------------------------------------------------

    @Subscribe
    public void onSubscriptionAdded(SubscriptionAddedEvent event) {
        Platform.runLater(() -> {
            subscriptionService.addSubscription(event.getSubscription());
            loadSubscriptions();
            updateStatus("Added new subscription: " + event.getSubscription().getName());
        });
    }

    
    @Subscribe
    public void onSubscriptionUpdated(SubscriptionUpdatedEvent event) {
        Platform.runLater(() -> {
            subscriptionService.updateSubscription(event.getSubscription());
            loadSubscriptions();
            updateStatus("Updated subscription: " + event.getSubscription().getName());
        });
    }
    @Subscribe
    public void onPaymentDue(PaymentDueEvent event) {
        Platform.runLater(() -> {
            Subscription sub = event.getSubscription();
            showAlert("Payment Due Soon",
                "Subscription \"" + sub.getName() + "\" is due on " +
                (sub.getNextPaymentDate() != null ? sub.getNextPaymentDate().toString() : "unknown") + "!");
        });
    }
    @Subscribe
    public void onCurrencyChanged(CurrencyChangedEvent event) {
        Platform.runLater(this::updateAllExchangeRatesAndSummary);
    }
    @Subscribe
    public void onCalendarExported(com.example.subscribe.events.CalendarExportedEvent event) {
        Platform.runLater(() -> showAlert(event.isSuccess() ? "Success" : "Error", event.getMessage()));
    }
}