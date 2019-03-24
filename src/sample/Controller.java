package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

public class Controller implements Initializable {

    private FoodDiary foodDiary;
    private FoodDataAccessor foodDataAccessor;

    // Food Diary
    @FXML private TableView<Food> breakfastTableView;
    @FXML private TableView<Food> lunchTableView;
    @FXML private TableView<Food> dinnerTableView;
    @FXML private TableView<Food> snackTableView;
    @FXML private TableColumn<Food, String> breakfastNameColumn;
    @FXML private TableColumn<Food, Integer> breakfastCaloriesColumn;
    @FXML private TableColumn<Food, String> lunchNameColumn;
    @FXML private TableColumn<Food, Integer> lunchCaloriesColumn;
    @FXML private TableColumn<Food, String> dinnerNameColumn;
    @FXML private TableColumn<Food, Integer> dinnerCaloriesColumn;
    @FXML private TableColumn<Food, String> snackNameColumn;
    @FXML private TableColumn<Food, Integer> snackCaloriesColumn;

    // Add Foods
    @FXML private TableView<Food> addFoodTableView;
    @FXML private TableColumn<Food, String> addFoodNameColumn;
    @FXML private TableColumn<Food, Integer> addFoodCaloriesColumn;
    @FXML private ChoiceBox<String> mealtimeChoiceBox;
    @FXML private TextField addFoodNameTextField;
    @FXML private TextField addFoodCaloriesTextField;
    @FXML private Label errorLabel;

    // Progress
    @FXML private Label totalCaloriesLabel;
    @FXML private Label targetCaloriesLabel;
    @FXML private Label remainingCaloriesLabel;
    @FXML private ProgressBar progressBar;
    @FXML private LineChart<Number, Number> lineChart;
    @FXML private NumberAxis xAxis;


    // Settings
    @FXML private TextField targetCaloriesInput;


    public Controller() {
        try {
            foodDataAccessor = new FoodDataAccessor();
        } catch (ClassNotFoundException| SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // --- Food Diary Tab ---
        breakfastTableView.setPlaceholder(new Label("To add foods, go to Add Food"));
        lunchTableView.setPlaceholder(new Label("To add foods, go to Add Food"));
        dinnerTableView.setPlaceholder(new Label("To add foods, go to Add Food"));
        snackTableView.setPlaceholder(new Label("To add foods, go to Add Food"));

        breakfastNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        breakfastNameColumn.setSortable(false);
        breakfastCaloriesColumn.setCellValueFactory(new PropertyValueFactory<>("calories"));
        breakfastCaloriesColumn.setSortable(false);
        lunchNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        lunchNameColumn.setSortable(false);
        lunchCaloriesColumn.setCellValueFactory(new PropertyValueFactory<>("calories"));
        lunchCaloriesColumn.setSortable(false);
        dinnerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dinnerNameColumn.setSortable(false);
        dinnerCaloriesColumn.setCellValueFactory(new PropertyValueFactory<>("calories"));
        dinnerCaloriesColumn.setSortable(false);
        snackNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        snackNameColumn.setSortable(false);
        snackCaloriesColumn.setCellValueFactory(new PropertyValueFactory<>("calories"));
        snackCaloriesColumn.setSortable(false);

        // Reading in a saved food diary
        foodDiary = readFoodDiary();
        if (foodDiary != null) {
            LocalDate currentDate = LocalDate.now();
            if (foodDiary.getEstimatedDate().compareTo(currentDate) == 0) {
                if (foodDiary.getBreakfastFoodsList() != null) {
                    foodDiary.setBreakfastFoodsObservableList(FXCollections.observableList(foodDiary.getBreakfastFoodsList()));
                    breakfastTableView.setItems(foodDiary.getBreakfastFoodsObservableList());
                }
                if (foodDiary.getLunchFoodsList() != null) {
                    foodDiary.setLunchFoodsObservableList(FXCollections.observableList(foodDiary.getLunchFoodsList()));
                    lunchTableView.setItems(foodDiary.getLunchFoodsObservableList());
                }
                if (foodDiary.getDinnerFoodsList() != null) {
                    foodDiary.setDinnerFoodsObservableList(FXCollections.observableList(foodDiary.getDinnerFoodsList()));
                    dinnerTableView.setItems(foodDiary.getDinnerFoodsObservableList());
                }
                if (foodDiary.getSnackFoodsList() != null) {
                    foodDiary.setSnackFoodsObservableList(FXCollections.observableList(foodDiary.getSnackFoodsList()));
                    snackTableView.setItems(foodDiary.getSnackFoodsObservableList());
                }
            }
            else {
                FoodDiary savedFoodDiary = foodDiary;
                foodDiary = new FoodDiary();
                foodDiary.setEstimatedDate(currentDate);
                foodDiary.setTargetCalories(savedFoodDiary.getTargetCalories());
                if (savedFoodDiary.getSavedCalories() == null) {
                    foodDiary.setSavedCalories(new ArrayList<>());
                }
                else {
                    foodDiary.addToSavedCalories(savedFoodDiary.getTotalCalories());
                }

            }
        }
        // Create a completely new Food Diary
        else {
            foodDiary = new FoodDiary();
            foodDiary.setTargetCalories(SpashScreen.welcome());
            foodDiary.setEstimatedDate(LocalDate.now());
            foodDiary.setSavedCalories(new ArrayList<>());
        }

        foodDiary.setChangesMade(false);

        // --- Add Foods Tab ---
        // Populating the Add Food Table

        addFoodNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addFoodNameColumn.setSortable(false);
        addFoodCaloriesColumn.setCellValueFactory(new PropertyValueFactory<>("calories"));
        addFoodCaloriesColumn.setSortable(false);
        try {
            ObservableList<Food> foods = foodDataAccessor.getFoodObservableList(); // Getting the foods from the database as Food objects
            addFoodTableView.setItems(foods);
            addFoodTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Choosing a meal time
        ObservableList<String> mealtimeChoices = FXCollections.observableArrayList();
        mealtimeChoices.addAll("Breakfast", "Lunch", "Dinner", "Snacks");
        mealtimeChoiceBox.setItems(mealtimeChoices);
        mealtimeChoiceBox.setValue("Breakfast");

        // --- Progress Tab ---
        System.out.println(foodDiary.getSavedCalories());
        Font font = new Font(20);
        totalCaloriesLabel.setFont(font);
        targetCaloriesLabel.setFont(font);
        remainingCaloriesLabel.setFont(font);
        updateProgress();
        progressBar.progressProperty().bind(foodDiary.barUpdaterProperty());
        lineChart.setTitle("Last " + (foodDiary.getSavedCalories().size() - 1) + " days of tracking");
        XYChart.Series<Number, Number> targetSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        targetSeries.setName("Target Calories");
        series.setName("Your Progress");
        for (int i = 0; i <= foodDiary.getSavedCalories().size() - 1; i ++) {
            targetSeries.getData().add(new XYChart.Data<>(i, foodDiary.getTargetCalories()));
            series.getData().add(new XYChart.Data<>(i, foodDiary.getSavedCalories().get(i)));
        }
        lineChart.getData().add(targetSeries);
        lineChart.getData().add(series);
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(1);
        xAxis.setUpperBound(foodDiary.getSavedCalories().size());
        xAxis.setTickUnit(1);

    }


    // --- Methods relating to Food Diary ---

    private void save() {
        if (foodDiary.getBreakfastFoodsObservableList() != null) {
            foodDiary.setBreakfastFoodsList(new ArrayList<>(foodDiary.getBreakfastFoodsObservableList()));
        }
        if (foodDiary.getLunchFoodsObservableList() != null) {
            foodDiary.setLunchFoodsList(new ArrayList<>(foodDiary.getLunchFoodsObservableList()));
        }
        if (foodDiary.getDinnerFoodsObservableList() != null) {
            foodDiary.setDinnerFoodsList(new ArrayList<>(foodDiary.getDinnerFoodsObservableList()));
        }
        if (foodDiary.getSnackFoodsObservableList() != null) {
            foodDiary.setSnackFoodsList(new ArrayList<>(foodDiary.getSnackFoodsObservableList()));
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream("FoodDiarySaveFile.ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(foodDiary);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FoodDiary readFoodDiary() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream("FoodDiarySaveFile.ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            return (FoodDiary) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeEventHandler() {
        ObservableList<Food> selectedBreakfastItems = breakfastTableView.getSelectionModel().getSelectedItems();
        ObservableList<Food> selectedLunchItems = lunchTableView.getSelectionModel().getSelectedItems();
        ObservableList<Food> selectedDinnerItems = dinnerTableView.getSelectionModel().getSelectedItems();
        ObservableList<Food> selectedSnackItems = snackTableView.getSelectionModel().getSelectedItems();

        ArrayList<Food> breakfastList = new ArrayList<>(selectedBreakfastItems);
        ArrayList<Food> lunchList = new ArrayList<>(selectedLunchItems);
        ArrayList<Food> dinnerList = new ArrayList<>(selectedDinnerItems);
        ArrayList<Food> snackList = new ArrayList<>(selectedSnackItems);

        breakfastList.forEach(row -> {
            breakfastTableView.getItems().remove(row);
            foodDiary.getBreakfastFoodsObservableList().remove(row);
            foodDiary.getBreakfastFoodsList().remove(row);
            foodDiary.decreaseTotalCalories(row.getCalories());
        });

        lunchList.forEach(row -> {
            lunchTableView.getItems().remove(row);
            foodDiary.getLunchFoodsObservableList().remove(row);
            foodDiary.getLunchFoodsList().remove(row);
            foodDiary.decreaseTotalCalories(row.getCalories());
        });

        dinnerList.forEach(row -> {
            dinnerTableView.getItems().remove(row);
            foodDiary.getDinnerFoodsObservableList().remove(row);
            foodDiary.getDinnerFoodsList().remove(row);
            foodDiary.decreaseTotalCalories(row.getCalories());
        });

        snackList.forEach(row -> {
            snackTableView.getItems().remove(row);
            foodDiary.getSnackFoodsObservableList().remove(row);
            foodDiary.getSnackFoodsList().remove(row);
            foodDiary.decreaseTotalCalories(row.getCalories());
        });

        updateProgress();
        foodDiary.setChangesMade(true);
    }

    // --- Methods relating to Add Food ---
    public void addFoodsEventHandler() {
        ObservableList<Food> foodsSelected = FXCollections.observableArrayList();
        foodsSelected.addAll(addFoodTableView.getSelectionModel().getSelectedItems());
        String mealtime = mealtimeChoiceBox.getValue();
        addFoods(mealtime, foodsSelected);
    }

    private void addFoods(String mealtime, ObservableList<Food> newFoods) {
        ObservableList<Food> updatedFoodList = FXCollections.observableArrayList();
        ObservableList<Food> currentFoods;
        switch (mealtime) {
            case "Breakfast":
                currentFoods = foodDiary.getBreakfastFoodsObservableList();
                addFoodsHelper(newFoods, updatedFoodList, currentFoods);
                foodDiary.setBreakfastFoodsObservableList(updatedFoodList);
                foodDiary.setBreakfastFoodsList(new ArrayList<>(updatedFoodList));
                breakfastTableView.setItems(foodDiary.getBreakfastFoodsObservableList());
                break;
            case "Lunch":
                currentFoods = foodDiary.getLunchFoodsObservableList();
                addFoodsHelper(newFoods, updatedFoodList, currentFoods);
                foodDiary.setLunchFoodsObservableList(updatedFoodList);
                foodDiary.setLunchFoodsList(new ArrayList<>(updatedFoodList));
                lunchTableView.setItems(foodDiary.getLunchFoodsObservableList());
                break;
            case "Dinner":
                currentFoods = foodDiary.getDinnerFoodsObservableList();
                addFoodsHelper(newFoods, updatedFoodList, currentFoods);
                foodDiary.setDinnerFoodsObservableList(updatedFoodList);
                foodDiary.setDinnerFoodsList(new ArrayList<>(updatedFoodList));
                dinnerTableView.setItems(foodDiary.getDinnerFoodsObservableList());
                break;
            case "Snacks":
                currentFoods = foodDiary.getSnackFoodsObservableList();
                addFoodsHelper(newFoods, updatedFoodList, currentFoods);
                foodDiary.setSnackFoodsObservableList(updatedFoodList);
                foodDiary.setSnackFoodsList(new ArrayList<>(updatedFoodList));
                snackTableView.setItems(foodDiary.getSnackFoodsObservableList());
                break;
        }
        foodDiary.setChangesMade(true);
    }

    // Creates the updated food Observable list
    private void addFoodsHelper(ObservableList<Food> newFoods, ObservableList<Food> foodsToAdd, ObservableList<Food> currentFoods) {
        if (currentFoods == null) {
            foodsToAdd.addAll(newFoods);
            for (Food food: foodsToAdd) {
                foodDiary.addToTotalCalories(food.getCalories());
                updateProgress();
            }
        }
        else {
            foodsToAdd.addAll(currentFoods);
            foodsToAdd.addAll(newFoods);
            for (Food food: newFoods) {
                foodDiary.addToTotalCalories(food.getCalories());
                updateProgress();
            }
        }
    }

    public void addUserFoodEventHandler() {
        try {
            int id = foodDataAccessor.generateId();
            String name = addFoodNameTextField.getText();
            String caloriesString = addFoodCaloriesTextField.getText();
            if (isAlpha(name) && isNumber(caloriesString) && name.length() > 0) {
                int calories = Integer.parseInt(caloriesString);
                Food food = new Food(id, name, calories);
                foodDataAccessor.insertFood(food);
                ObservableList<Food> foods = foodDataAccessor.getFoodObservableList(); // Getting the foods from the database as Food objects
                addFoodTableView.setItems(foods);
                errorLabel.setText("");
            }
            else {
                errorLabel.setText("Please enter in a valid format");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isAlpha(String s) {
        char[] chars = s.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c) && c != ' ') {
                return false;
            }
        }

        return true;
    }

    private boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }


    // --- Methods relating to progress ---
    private void updateProgress() {
        totalCaloriesLabel.setText("Total Calories: " + foodDiary.getTotalCalories());
        targetCaloriesLabel.setText("Target Calories: " + foodDiary.getTargetCalories());
        remainingCaloriesLabel.setText("Remaining Calories: " + (foodDiary.getTargetCalories() - foodDiary.getTotalCalories()));
        foodDiary.setBarUpdater((foodDiary.getTotalCalories() / (double)foodDiary.getTargetCalories()));
    }

    // --- Methods relating to settings ---
    public void setTargetCaloriesEventHandler() {
        int newTargetCalories = Integer.parseInt(targetCaloriesInput.getText());
        foodDiary.setTargetCalories(newTargetCalories);
        targetCaloriesLabel.setText("Target Calories: " + foodDiary.getTargetCalories());
        remainingCaloriesLabel.setText("Remaining Calories: " + (foodDiary.getTargetCalories() - foodDiary.getTotalCalories()));
        foodDiary.setBarUpdater((foodDiary.getTotalCalories() / (double)foodDiary.getTargetCalories()));
        foodDiary.setChangesMade(true);
    }


    void handleClose() {
        if (foodDiary.isChangesMade()) {
            save();
        }
    }
}
