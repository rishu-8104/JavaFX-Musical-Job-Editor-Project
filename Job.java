import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Describes a sampling job with successive notes.
 */
public class Job {
    private static final int DEFAULT_VELOCITY = 90; // example default velocity
    private static final int DEFAULT_START_TIME = 0; // example default start time
    private static final int DEFAULT_END_TIME = 1000;
    private Interval selectedInterval;

    /**
     * Interval between notes in semitones.
     */
    public enum Interval {
        ONE(1), THREE(3), SIX(6), TWELVE(12);

        private final int value;

        private Interval(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    // Internal count of instances created.
    // Used to construct the default name for the job.
    private static int count = 1;

    public Job() {
        this("Job" + count, 1000, 500, 200);
    }

    public Job(String name) {
        this(name, 1000, 500, 200);
    }


    /**
     * Constructs a job with default values.
     */
    public Job(String name, int noteDuration, int noteDecay, int noteGap) {
        this.name = name;
        this.id = UUID.randomUUID();
        this.fromNote = 40;
        this.toNote = 120;
        this.interval = Interval.ONE;
        this.noteDuration = noteDuration;
        this.noteDecay = noteDecay;
        this.noteGap = noteGap;
        this.velocities = new ArrayList<>();
        this.velocities.add(DEFAULT_VELOCITY); // Default single velocity
        count++;
    }

    /**
     * Sets the name of the job.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the job.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the unique identifier of the job.
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Gets the first note of the range.
     */
    public int getFromNote() {
        return this.fromNote;
    }

    /**
     * Sets the first note of the range.
     */
    public void setFromNote(int note) {
        if (note < 0 || note > 127) {
            throw new IllegalArgumentException("Note must be 0...127");
        }
        this.fromNote = note;
    }

    /**
     * Gets the last note of the range.
     */
    public int getToNote() {
        return this.toNote;
    }

    /**
     * Sets the last note of the range.
     */
    public void setToNote(int note) {
        if (note < 0 || note > 127) {
            throw new IllegalArgumentException("Note must be 0...127");
        }
        this.toNote = note;
    }

    /**
     * Gets the interval between the notes.
     */
    public Interval getInterval() {
        return interval;
    }

    /**
     * Sets the interval between the notes.
     */
    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    /**
     * Gets the note duration.
     *
     * @return the note duration in milliseconds
     */
    public int getNoteDuration() {
        return this.noteDuration;
    }

    /**
     * Sets the duration of each note.
     *
     * @param duration note duration (milliseconds)
     */
    public void setNoteDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Note duration must be positive");
        }

        this.noteDuration = duration;
    }

    /**
     * Gets the note decay time.
     *
     * @return the note decay time in milliseconds
     */
    public int getNoteDecay() {
        return this.noteDecay;
    }

    /**
     * Sets the decay of each note.
     *
     * @param decay note decay (milliseconds)
     */
    public void setNoteDecay(int decay) {
        if (decay <= 0) {
            throw new IllegalArgumentException("Note decay time must be positive");
        }

        this.noteDecay = decay;
    }

    /**
     * Gets the gap time between notes.
     *
     * @return the note gap time in milliseconds
     */
    public int getNoteGap() {
        return this.noteGap;
    }

    /**
     * Sets the gap time between notes.
     *
     * @param gap note gap time (milliseconds)
     */
    public void setNoteGap(int gap) {
        if (gap <= 0) {
            throw new IllegalArgumentException("Note gap time must be positive");
        }

        this.noteGap = gap;
    }

    public List<Note> getNotes() {
        List<Note> notes = new ArrayList<>();
        for (int i = this.fromNote; i <= this.toNote; i += this.interval.getValue()) {
            notes.add(new Note(i, new ArrayList<>(this.velocities), DEFAULT_START_TIME, DEFAULT_END_TIME));
        }
        return notes;
    }


    public List<Integer> getVelocities() {
        List<Integer> clone = new ArrayList<Integer>(this.velocities.size());
        for (Integer i : this.velocities) {
            clone.add(i);
        }
        return clone;
    }

    /**
     * Sets a singular velocity. Clears any existing velocities
     * and replaces them with this one.
     *
     * @param v the velocity to set
     */
    public void setVelocity(int v) {
        if (v < 1 || v > 127) {
            throw new IllegalArgumentException("Velocity must be 1...127");
        }

        this.velocities.clear();
        this.velocities.add(v);
    }

    public void setSpecificVelocities(List<Integer> vs) {
        this.velocities.clear();
        this.velocities.addAll(vs);
    }
    

    public void setDistributedVelocities(int first, int last, int count) {
        if (first <= 0) {
            throw new IllegalArgumentException("First velocity must be positive");
        }
        if (first > 127) {
            throw new IllegalArgumentException("First velocity can be at most 127");
        }
        if (last <= 0) {
            throw new IllegalArgumentException("Last velocity must be positive");
        }
        if (last > 127) {
            throw new IllegalArgumentException("Last velocity can be at most 127");
        }
        if (first > last) {
            throw new IllegalArgumentException("First velocity must be smaller than last");
        }
        if (count < 1) {
            throw new IllegalArgumentException("Count must be one or more");
        }

        List<Integer> result = new ArrayList<Integer>();

        int step = (last - first) / count;
        //System.out.println("step = " + step);
        int vel = first;
        do {
            result.add(vel);
            vel += step;
        } while (vel <= last);

        this.velocities = result;
    }

    public Interval getSelectedInterval() {
        return selectedInterval;
    }

    public void setSelectedInterval(Interval selectedInterval) {
        this.selectedInterval = selectedInterval;
    }



    /**
     * Gets a string representation of the job.
     */
    @Override
    public String toString() {
        StringBuilder velocitiesString = new StringBuilder();
        for (int v : this.getVelocities()) {
            velocitiesString.append(v);
            velocitiesString.append(" ");
        }

        return String.format(
            "%s: from %d to %d by %d semitones, duration %d ms, decay %d ms, gap %d ms, velocities: %s",
            this.getName(),
            this.getFromNote(),
            this.getToNote(),
            this.interval.getValue(),
            this.getNoteDuration(),
            this.getNoteDecay(),
            this.getNoteGap(),
            velocitiesString.toString()
        );
    }

    //
    // Private fields
    //

    private String name;
    private UUID id;
    private int fromNote;
    private int toNote;
    private Interval interval;
    private int noteDuration;  // milliseconds
    private int noteDecay;  // note decay time in ms
    private int noteGap;  // note gap time in ms
    private List<Integer> velocities;  // note velocities
}