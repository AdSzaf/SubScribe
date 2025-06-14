package com.example.subscribe.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.application.Platform;

import com.example.subscribe.models.Subscription;
import com.example.subscribe.patterns.strategy.AlertNotificationStrategy;
import com.example.subscribe.patterns.strategy.EndOfSubscriptionNotificationStrategy;
import com.example.subscribe.patterns.strategy.NotificationStrategy;
import com.example.subscribe.models.Category;
import com.example.subscribe.services.CurrencyService;
import com.example.subscribe.services.PaymentApiService;
import com.example.subscribe.services.PublicHolidaysService;
import com.example.subscribe.services.SubscriptionService;
import com.example.subscribe.utils.ConfigManager;
import com.example.subscribe.utils.ICalendarExporter;
import com.example.subscribe.utils.ReflectionUtils;
import com.example.subscribe.events.CurrencyChangedEvent;
import com.example.subscribe.events.EventBusManager;
import com.example.subscribe.events.SubscriptionAddedEvent;
import com.example.subscribe.events.SubscriptionEndingEvent;
import com.google.common.eventbus.Subscribe;
import com.example.subscribe.events.SubscriptionUpdatedEvent;
import com.example.subscribe.events.PaymentDueEvent;
import com.example.subscribe.events.PublicHolidaysFetchedEvent;
import com.example.subscribe.events.LanguageChangedEvent;
import com.example.subscribe.services.TranslationService;
import com.example.subscribe.patterns.factory.NotificationStrategyFactory;

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
    @FXML private ImageView logoImageView;

    private TableColumn<Subscription, ?> lastSortedColumn = null;
    
    private ObservableList<Subscription> subscriptionsList;
    private FilteredList<Subscription> filteredSubscriptions;
    private SubscriptionService subscriptionService;

    private BigDecimal currentExchangeRate = BigDecimal.ONE;
    private String targetCurrency = "PLN"; 
    private Map<String, BigDecimal> exchangeRates = new HashMap<>();
    private NotificationStrategy notificationStrategy;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            Image image = new Image(getClass().getResourceAsStream("/com/example/subscribe/images/logo_origin.png"));
            logoImageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }

        subscriptionService = new SubscriptionService();

        EventBusManager.getInstance().register(this);

        subscriptionsList = FXCollections.observableArrayList();
        filteredSubscriptions = new FilteredList<>(subscriptionsList);

        PaymentApiService paymentApiService = new PaymentApiService();
        paymentApiService.fetchTransactionsAsync().thenAccept(transactions -> {
            Platform.runLater(() -> {
                showAlert("Fetched Transactions", "Fetched " + transactions.size() + " transactions from PayPal/Stripe (mock).");
            });
        });

        String baseCurrency = ConfigManager.get("currency.base", "USD");
        targetCurrency = ConfigManager.get("app.currency", "PLN");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filteredSubscriptions.setPredicate(this::filterPredicate));
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> filteredSubscriptions.setPredicate(this::filterPredicate));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filteredSubscriptions.setPredicate(this::filterPredicate));

        String notifType = ConfigManager.get("notification.type", "alert");
        notificationStrategy = NotificationStrategyFactory.create(notifType);

        filteredSubscriptions.setPredicate(this::filterPredicate);

        fetchAndUpdateExchangeRate();

        setupTableColumns();
        setupFilters();


        SortedList<Subscription> sortedSubscriptions = new SortedList<>(filteredSubscriptions);
        sortedSubscriptions.comparatorProperty().bind(subscriptionsTable.comparatorProperty());
        subscriptionsTable.setItems(sortedSubscriptions);

        loadSubscriptions();



        updateStatus("Application loaded successfully");
        updateLastUpdateTime();

    }

    private void setupTableColumns() {

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        costColumn.setCellValueFactory(cellData -> {
            BigDecimal cost = cellData.getValue().getCost();
            return new SimpleStringProperty(cost != null ? cost.toString() : "0.00");
        });

        costColumn.setComparator((s1, s2) -> {
            Subscription sub1 = subscriptionsList.stream()
                .filter(sub -> sub.getCost() != null && sub.getCost().toString().equals(s1))
                .findFirst().orElse(null);
            Subscription sub2 = subscriptionsList.stream()
                .filter(sub -> sub.getCost() != null && sub.getCost().toString().equals(s2))
                .findFirst().orElse(null);

            BigDecimal val1 = sub1 != null ? getConvertedCost(sub1) : BigDecimal.ZERO;
            BigDecimal val2 = sub2 != null ? getConvertedCost(sub2) : BigDecimal.ZERO;
            return val1.compareTo(val2);
        });

        currencyColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));

        categoryColumn.setCellValueFactory(cellData -> {
            Category category = cellData.getValue().getCategory();
            return new SimpleStringProperty(category != null ? category.getDisplayName() : "Other");
        });

        nextPaymentColumn.setCellValueFactory(cellData -> {
            LocalDate nextPayment = cellData.getValue().getNextPaymentDate();
            if (nextPayment != null) {
                return new SimpleStringProperty(nextPayment.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            return new SimpleStringProperty("N/A");
        });

        statusColumn.setCellValueFactory(cellData -> {
            boolean active = cellData.getValue().isActive();
            return new SimpleStringProperty(active ? "Active" : "Inactive");
        });

        nameColumn.setSortable(true);
        costColumn.setSortable(true);
        currencyColumn.setSortable(true);
        categoryColumn.setSortable(true);
        nextPaymentColumn.setSortable(true);
        statusColumn.setSortable(true);

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

        categoryFilter.getItems().setAll(ReflectionUtils.loadAllCategories());
        categoryFilter.getItems().add(0, null);
        categoryFilter.setConverter(new javafx.util.StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category == null ? "All Categories" : category.getDisplayName();
            }

            @Override
            public Category fromString(String string) {
                return null; 
            }
        });

        statusFilter.getItems().addAll("All", "Active", "Inactive");
        statusFilter.setValue("All");

        filteredSubscriptions.setPredicate(subscription -> {

            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!subscription.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return false;
                }
            }

            Category selectedCategory = categoryFilter.getValue();
            if (selectedCategory != null && !selectedCategory.equals(subscription.getCategory())) {
                return false;
            }

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

    private boolean filterPredicate(Subscription subscription) {

        String searchText = searchField.getText();
        if (searchText != null && !searchText.isEmpty()) {
            String lowerCaseFilter = searchText.toLowerCase();
            if (!subscription.getName().toLowerCase().contains(lowerCaseFilter)) {
                return false;
            }
        }

        Category selectedCategory = categoryFilter.getValue();
        if (selectedCategory != null) {
            Category subCategory = subscription.getCategory();
            if (subCategory == null || !subCategory.getClass().equals(selectedCategory.getClass())) {
                return false;
            }
        }

        String selectedStatus = statusFilter.getValue();
        if (!"All".equals(selectedStatus)) {
            boolean isActive = "Active".equals(selectedStatus);
            if (subscription.isActive() != isActive) {
                return false;
            }
        }

        return true;
    }


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
            Task<Void> exportTask = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        com.example.subscribe.utils.CSVExporter.exportToCSV(subscriptionsList, file.getAbsolutePath());
                        Platform.runLater(() -> showAlert("Success", "Exported subscriptions to CSV."));
                    } catch (IOException e) {
                        Platform.runLater(() -> showAlert("Error", "Failed to export CSV: " + e.getMessage()));
                    }
                    return null;
                }
            };
            Thread thread = new Thread(exportTask);
            thread.setDaemon(true);
            thread.start();
        }
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
            Task<Void> importTask = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        List<Subscription> imported = com.example.subscribe.utils.CSVExporter.importFromCSV(file.getAbsolutePath());
                        SubscriptionService subscriptionService = new SubscriptionService();
                        List<Subscription> current = subscriptionService.getAllSubscriptions();

                        for (Subscription importedSub : imported) {

                            Subscription existing = current.stream()
                                .filter(s -> s.getName().equalsIgnoreCase(importedSub.getName()))
                                .findFirst()
                                .orElse(null);

                            if (existing != null) {

                                importedSub.setId(existing.getId());
                                subscriptionService.updateSubscription(importedSub);
                            } else {

                                subscriptionService.addSubscription(importedSub);
                            }
                        }

                        Platform.runLater(() -> {
                            loadSubscriptions();
                            showAlert("Success", "Imported subscriptions from CSV.");
                        });
                    } catch (IOException e) {
                        Platform.runLater(() -> showAlert("Error", "Failed to import CSV: " + e.getMessage()));
                    }
                    return null;
                }
            };
            Thread thread = new Thread(importTask);
            thread.setDaemon(true);
            thread.start();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/subscribe/fxml/statistics.fxml"));
            Scene scene = new Scene(loader.load());

            StatisticsController controller = loader.getController();
            controller.setSubscriptions(subscriptionsList);

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/subscribe/fxml/calendar-view.fxml"));
            Scene scene = new Scene(loader.load());

            CalendarViewController controller = loader.getController();
            controller.setSubscriptions(subscriptionsList);

            String lang = ConfigManager.get("app.language", "en");
            EventBusManager.getInstance().post(new LanguageChangedEvent(lang));

            Stage stage = new Stage();
            stage.setTitle("Calendar View");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(addSubscriptionBtn.getScene().getWindow());

            stage.setOnHiding(e -> controller.onClose());

            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Could not open Calendar view: " + e.getMessage());
        }
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


    private void loadSubscriptions() {
        updateStatus("Loading subscriptions...");

        subscriptionService.getAllSubscriptionsAsync().thenAccept(subscriptions -> {
            Platform.runLater(() -> {
                subscriptionsList.clear();
                subscriptionsList.addAll(subscriptions);
                updateAllExchangeRatesAndSummary(); 
                updateStatus("Loaded " + subscriptions.size() + " subscriptions");
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                updateStatus("Failed to load subscriptions");
                showAlert("Error", "Failed to load subscriptions: " + ex.getMessage());
            });
            return null;
        });
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
        String formattedCost = String.format("%.2f", totalMonthlyCost);

        totalMonthlyCostLabel.setText(targetCurrency + " " + formattedCost.toString());
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

    private void fetchAndUpdateExchangeRate() {
        String baseCurrency = "USD";
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
                    exchangeRates.put(currency, BigDecimal.ONE);
                    return null;
                }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() ->
            Platform.runLater(this::updateSummaryCards)
        );
    }


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

    private BigDecimal getConvertedCost(Subscription sub) {
        if (sub.getCost() == null) return BigDecimal.ZERO;
        String currency = sub.getCurrency();
        BigDecimal rate = currency.equals(targetCurrency)
            ? BigDecimal.ONE
            : exchangeRates.getOrDefault(currency, BigDecimal.ONE);
        return sub.getCost().multiply(rate);
    }


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
        Subscription sub = event.getSubscription();
        String message = "Subscription \"" + sub.getName() + "\" is due on " +
            (sub.getNextPaymentDate() != null ? sub.getNextPaymentDate().toString() : "unknown") + "!";
        if (notificationStrategy != null) {
            notificationStrategy.notify(sub, message);
        }
    }
    @Subscribe
    public void onCurrencyChanged(CurrencyChangedEvent event) {
        Platform.runLater(this::updateAllExchangeRatesAndSummary);
    }
    @Subscribe
    public void onCalendarExported(com.example.subscribe.events.CalendarExportedEvent event) {
        Platform.runLater(() -> showAlert(event.isSuccess() ? "Success" : "Error", event.getMessage()));
    }
    @Subscribe
    public void onLanguageChanged(LanguageChangedEvent event) {
        String lang = event.getLanguage();
        String countryCode = mapLanguageToCountry(lang);
        int year = java.time.LocalDate.now().getYear();

        PublicHolidaysService holidaysService = new PublicHolidaysService();
        holidaysService.fetchHolidays(year, countryCode).thenAccept(holidays -> {
            EventBusManager.getInstance().post(new PublicHolidaysFetchedEvent(holidays));
        });
    }

    private String mapLanguageToCountry(String lang) {
    if (lang == null) return "US";
    return switch (lang.toLowerCase()) {
        case "en" -> "US";
        default -> lang.toUpperCase();
    };
}

    @Subscribe
    public void onPublicHolidaysFetched(PublicHolidaysFetchedEvent event) {
        List<PublicHolidaysService.Holiday> holidays = event.getHolidays();
        LocalDate today = LocalDate.now();
        List<PublicHolidaysService.Holiday> upcoming = holidays.stream()
            .filter(h -> LocalDate.parse(h.date).isAfter(today) || LocalDate.parse(h.date).isEqual(today))
            .sorted((h1, h2) -> h1.date.compareTo(h2.date))
            .collect(Collectors.toList());

        Platform.runLater(() -> {
            StringBuilder sb = new StringBuilder("Upcoming public holidays:\n");
            upcoming.stream().limit(5).forEach(h -> sb.append(h.date).append(" - ").append(h.name).append("\n"));
            showAlert("Public Holidays", sb.toString());
        });
    }

    @Subscribe
    public void onSubscriptionEnding(SubscriptionEndingEvent event) {
        Subscription sub = event.getSubscription();
        String message = "Would you like to prolong or cancel this subscription?";

        NotificationStrategy endStrategy = NotificationStrategyFactory.create(
            "end",
            this::prolongSubscription,
            this::cancelSubscription
        );
        endStrategy.notify(sub, message);
    }
    private void prolongSubscription(Subscription sub) {
        int cycle = sub.getBillingCycle(); 
        LocalDate newDate = sub.getNextPaymentDate().plusDays(cycle);
        sub.setNextPaymentDate(newDate);
        subscriptionService.updateSubscription(sub);
        Platform.runLater(() -> showAlert("Prolonged", "Subscription prolonged to " + newDate));
        loadSubscriptions();
    }
    private void cancelSubscription(Subscription sub) {
        sub.setActive(false);
        subscriptionService.updateSubscription(sub);
        Platform.runLater(() -> showAlert("Cancelled", "Subscription marked as inactive."));
        loadSubscriptions();
    }
}