import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class Main extends Application {

    private ObservableList<Job> jobs = FXCollections.observableArrayList();
    private Job currentJob;
    private ListView<Job> jobsList;
    private Slider durationSlider, decaySlider, gapSlider;
    private Label durationValueLabel, decayValueLabel, gapValueLabel;
    private Canvas canvas;
    private TextField nameField;
    private Spinner<Integer> startNoteSpinner, endNoteSpinner;
    private ToggleGroup intervalGroup;
    private TableView<Note> notesTable;

    @Override
    public void start(Stage primaryStage) {
        initializeComponents();
        jobs.addAll(new Job("Job 1", 1000, 500, 200), new Job("Job 2", 1200, 400, 300), new Job("Job 3", 1500, 600, 100));
        currentJob = jobs.get(0);

        jobsList = new ListView<>(jobs);
        jobsList.setPrefWidth(200);
        jobsList.getSelectionModel().selectedItemProperty().addListener((obs, oldJob, newJob) -> {
            if (newJob != null) {
                if (oldJob != null) {
                    // Save changes made to the previous job before navigating
                    saveJobDetails(oldJob);
                }
                currentJob = newJob;
                loadJobDetails(newJob);
            }
        });
        notesTable = createNotesTableView();

        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Name change doesn't affect notes table
        });

        startNoteSpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentJob != null && !oldVal.equals(newVal)) {
                currentJob.setFromNote(newVal);
                updateNotesTable(currentJob);
            }
        });

        endNoteSpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentJob != null && !oldVal.equals(newVal)) {
                currentJob.setToNote(newVal);
                updateNotesTable(currentJob);
            }
        });


        intervalGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null && currentJob != null) {
                Job.Interval selectedInterval = (Job.Interval) newToggle.getUserData();
                if (currentJob.getInterval() != selectedInterval) {
                    currentJob.setInterval(selectedInterval);
                    updateJobAndNotesTable();
                }
            }
        });

        durationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentJob != null && !oldVal.equals(newVal)) {
                currentJob.setNoteDuration(newVal.intValue());
                updateJobAndNotesTable();
            }
        });

        decaySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentJob != null && !oldVal.equals(newVal)) {
                currentJob.setNoteDecay(newVal.intValue());
                updateJobAndNotesTable();
            }
        });

        gapSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentJob != null && !oldVal.equals(newVal)) {
                currentJob.setNoteGap(newVal.intValue());
                updateJobAndNotesTable();
            }
        });

        initializeComponents();
        SplitPane mainSplitPane = new SplitPane(jobsList, createJobEditorPane());
        mainSplitPane.setDividerPositions(0.25);

        Scene scene = new Scene(mainSplitPane, 1024, 768);
        primaryStage.setTitle("Job Editor");
        primaryStage.setScene(scene);
        primaryStage.show();

        loadJobDetails(currentJob);
    }

    private void saveJobDetails(Job job) {
        if (job != null) {
            job.setName(nameField.getText());
            job.setFromNote(startNoteSpinner.getValue());
            job.setToNote(endNoteSpinner.getValue());
            job.setInterval((Job.Interval) intervalGroup.getSelectedToggle().getUserData());
            job.setNoteDuration((int) durationSlider.getValue());
            job.setNoteDecay((int) decaySlider.getValue());
            job.setNoteGap((int) gapSlider.getValue());
        }
    }

    private void initializeComponents() {
        canvas = new Canvas(500, 50);
        durationSlider = new Slider(0, 5000, 1000);
        decaySlider = new Slider(0, 4500, 500);
        gapSlider = new Slider(0, 500, 100);
        durationValueLabel = new Label();
        decayValueLabel = new Label();
        gapValueLabel = new Label();
        nameField = new TextField();
        startNoteSpinner = new Spinner<>(0, 127, 0);
        endNoteSpinner = new Spinner<>(0, 127, 127);
        intervalGroup = new ToggleGroup();
    }

    private void updateJobAndNotesTable() {
        if (currentJob != null && notesTable != null) {
            notesTable.getItems().clear();
            notesTable.getItems().addAll(createNotesList(currentJob));
        }
    }


    private void updateNotesTable(Job job) {
        if (job != null && notesTable != null) {
            notesTable.getItems().clear();
            notesTable.getItems().addAll(createNotesList(job));
        }
    }

    private void loadJobDetails(Job job) {
        if (job == null) {
            System.out.println("Job data is null.");
            return;
        }

        nameField.setText(job.getName());
        startNoteSpinner.getValueFactory().setValue(job.getFromNote());
        endNoteSpinner.getValueFactory().setValue(job.getToNote());

        durationSlider.setValue(job.getNoteDuration());
        decaySlider.setValue(job.getNoteDecay());
        gapSlider.setValue(job.getNoteGap());

        durationValueLabel.setText(job.getNoteDuration() + " ms");
        decayValueLabel.setText(job.getNoteDecay() + " ms");
        gapValueLabel.setText(job.getNoteGap() + " ms");

        updateRadioButtonGroup(job.getInterval());
        job.setSelectedInterval(job.getInterval());

        currentJob.setInterval(job.getInterval());
        drawTiming();
    }

    private SplitPane createJobEditorPane() {
        VBox jobDetailsAndTimingEditor = createJobDetailsAndTimingEditor();

        notesTable = createNotesTableView(); // Assign the created TableView to notesTable

        SplitPane jobEditorSplitPane = new SplitPane();
        jobEditorSplitPane.setOrientation(Orientation.VERTICAL);
        jobEditorSplitPane.getItems().addAll(jobDetailsAndTimingEditor, notesTable);
        jobEditorSplitPane.setDividerPositions(0.7);

        VBox.setVgrow(notesTable, Priority.ALWAYS);

        return jobEditorSplitPane;
    }

    private VBox createJobDetailsAndTimingEditor() {
        return new VBox(createJobDetailsGrid(), createJobTimingEditor());
    }

    private GridPane createJobDetailsGrid() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        Label nameLabel = new Label("Job Name:");
        nameLabel.setStyle("-fx-font-weight: bold;");
        gridPane.add(nameLabel, 0, 0);
        gridPane.add(nameField, 1, 0);

        Label startNoteLabel = new Label("From Note:");
        startNoteLabel.setStyle("-fx-font-weight: bold;");
        gridPane.add(startNoteLabel, 0, 1);
        gridPane.add(startNoteSpinner, 1, 1);

        Label endNoteLabel = new Label("To Note:");
        endNoteLabel.setStyle("-fx-font-weight: bold;");
        gridPane.add(endNoteLabel, 0, 2);
        gridPane.add(endNoteSpinner, 1, 2);

        HBox intervalBox = createIntervalBox();
        TitledPane intervalPane = new TitledPane("Interval", intervalBox);
        intervalPane.setStyle("-fx-font-weight: bold;");
        intervalPane.setExpanded(true);
        gridPane.add(intervalPane, 0, 3, 2, 1);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(new ColumnConstraints(), cc);

        return gridPane;
    }

    private HBox createIntervalBox() {
        HBox hbox = new HBox(10);
        RadioButton rbOne = new RadioButton("One");
        rbOne.setToggleGroup(intervalGroup);
        rbOne.setUserData(Job.Interval.ONE);

        RadioButton rbThree = new RadioButton("Three");
        rbThree.setToggleGroup(intervalGroup);
        rbThree.setUserData(Job.Interval.THREE);

        RadioButton rbSix = new RadioButton("Six");
        rbSix.setToggleGroup(intervalGroup);
        rbSix.setUserData(Job.Interval.SIX);

        RadioButton rbTwelve = new RadioButton("Twelve");
        rbTwelve.setToggleGroup(intervalGroup);
        rbTwelve.setUserData(Job.Interval.TWELVE);

        hbox.getChildren().addAll(rbOne, rbThree, rbSix, rbTwelve);
        hbox.setPadding(new Insets(15, 20, 15, 20));

        return hbox;
    }

    private void updateRadioButtonGroup(Job.Interval interval) {
        for (Toggle toggle : intervalGroup.getToggles()) {
            if (toggle.getUserData() == interval) {
                intervalGroup.selectToggle(toggle);
                break;
            }
        }
    }

    private VBox createJobTimingEditor() {
        VBox editor = new VBox(
            createSliderSection("Duration", 100, 5000, durationSlider, durationValueLabel),
            createSliderSection("Decay", 100, 4500, decaySlider, decayValueLabel),
            createSliderSection("Gap", 100, 500, gapSlider, gapValueLabel)
        );
        editor.getChildren().add(canvas);
        editor.setSpacing(20);
        editor.setPadding(new Insets(20, 40, 20, 40));
        return editor;
    }

    private VBox createSliderSection(String labelName, double min, double max, Slider slider, Label valueLabel) {
        VBox section = new VBox(5);
        Label label = new Label(labelName + ": ");
        label.setStyle("-fx-font-weight: bold;");
        valueLabel.setStyle("-fx-font-weight: bold;");

        HBox labelBox = new HBox(5, label, valueLabel);
        labelBox.setPadding(new Insets(0, 20, 0, 20));

        slider.setPadding(new Insets(0, 20, 0, 20));
        setupSlider(slider, valueLabel);

        section.getChildren().addAll(labelBox, slider);
        return section;
    }

    private void setupSlider(Slider slider, Label valueLabel) {
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1000);
        slider.setBlockIncrement(100);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            valueLabel.setText(String.format("%.0f ms", newVal.doubleValue()));
            updateJobBasedOnSlider(slider);
            drawTiming();
            if (slider == durationSlider || slider == decaySlider || slider == gapSlider) {
                updateJobAndNotesTable(); // Update notes table when relevant sliders change
            }
        });
    }

    private void updateJobBasedOnSlider(Slider slider) {
        if (slider == durationSlider) {
            currentJob.setNoteDuration((int) slider.getValue());
        } else if (slider == decaySlider) {
            currentJob.setNoteDecay((int) slider.getValue());
        } else if (slider == gapSlider) {
            currentJob.setNoteGap((int) slider.getValue());
        }
    }

    private void drawTiming() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        double total = currentJob.getNoteDuration() + currentJob.getNoteDecay() + currentJob.getNoteGap();
        double durationWidth = (currentJob.getNoteDuration() / total) * canvas.getWidth();
        double decayWidth = (currentJob.getNoteDecay() / total) * canvas.getWidth();
        double gapWidth = (currentJob.getNoteGap() / total) * canvas.getWidth();

        gc.setFill(javafx.scene.paint.Color.RED);
        gc.fillRect(0, 10, durationWidth, 30);
        gc.setFill(javafx.scene.paint.Color.GREEN);
        gc.fillRect(durationWidth, 10, decayWidth, 30);
        gc.setFill(javafx.scene.paint.Color.BLUE);
        gc.fillRect(durationWidth + decayWidth, 10, gapWidth, 30);
    }

    private TableView<Note> createNotesTableView() {
        TableView<Note> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Add columns to the table
        TableColumn<Note, Integer> noteColumn = new TableColumn<>("Note");
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

        TableColumn<Note, List<Integer>> velocityColumn = new TableColumn<>("Velocity");
        velocityColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getVelocity()));
        velocityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<List<Integer>>() {
            @Override
            public String toString(List<Integer> velocities) {
                return velocities.stream().map(Object::toString).collect(Collectors.joining(", "));
            }

            @Override
            public List<Integer> fromString(String string) {
                return Arrays.stream(string.split(",")).map(Integer::parseInt).collect(Collectors.toList());
            }
        }));

        // Set the column editable
        velocityColumn.setEditable(true);

        // Add setOnEditCommit for editing
        velocityColumn.setOnEditCommit(event -> {
            Note note = event.getRowValue();
            note.setVelocity(event.getNewValue());
        });

        TableColumn<Note, Integer> startTimeColumn = new TableColumn<>("Start (ms)");
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));

        TableColumn<Note, Integer> endTimeColumn = new TableColumn<>("End (ms)");
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // Set columns editable
        table.setEditable(true);

        // Add columns to the table
        table.getColumns().addAll(noteColumn, velocityColumn, startTimeColumn, endTimeColumn);

        return table;
    }

    private ObservableList<Note> createNotesList(Job job) {
        List<Note> list = new ArrayList<>();
        // Generate notes based on job details
        // Example implementation, you should modify this based on your logic
        for (int i = job.getFromNote(); i <= job.getToNote(); i++) {
            list.add(new Note(i, new ArrayList<>(Arrays.asList(60)), i * 100, i * 100 + 1000));
        }
        return FXCollections.observableArrayList(list);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
