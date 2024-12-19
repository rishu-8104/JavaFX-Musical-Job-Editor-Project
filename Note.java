import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.stream.Collectors;

public class Note {
    private final IntegerProperty note;
    private ObservableList<IntegerProperty> velocities;
    private final IntegerProperty startTime;
    private final IntegerProperty endTime;

    public Note(int note, List<Integer> velocities, int startTime, int endTime) {
        this.note = new SimpleIntegerProperty(note);
        this.velocities = FXCollections.observableArrayList(
            velocities.stream().map(SimpleIntegerProperty::new).collect(Collectors.toList())
        );
        this.startTime = new SimpleIntegerProperty(startTime);
        this.endTime = new SimpleIntegerProperty(endTime);
    }

    public IntegerProperty noteProperty() {
        return this.note;
    }

    public ObservableList<IntegerProperty> getVelocities() {
        return this.velocities;
    }

    public void setVelocities(List<Integer> velocities) {
        this.velocities.setAll(velocities.stream().map(SimpleIntegerProperty::new).collect(Collectors.toList()));
    }

    public IntegerProperty startTimeProperty() {
        return this.startTime;
    }

    public IntegerProperty endTimeProperty() {
        return this.endTime;
    }

    public String getVelocityString() {
        return velocities.stream()
                         .map(IntegerProperty::get)
                         .map(String::valueOf)
                         .collect(Collectors.joining(", "));
    }

    public List<Integer> getVelocity() {
        return velocities.stream()
                         .map(IntegerProperty::get)
                         .collect(Collectors.toList());
    }

    public void setVelocity(List<Integer> velocity) {
        this.velocities.setAll(
            velocity.stream().map(SimpleIntegerProperty::new).collect(Collectors.toList())
        );
    }
}
