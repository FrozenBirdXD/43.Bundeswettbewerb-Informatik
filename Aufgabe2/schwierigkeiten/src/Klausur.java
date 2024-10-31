import java.util.ArrayList;
import java.util.List;

public class Klausur {
    private List<String> tasks;

    public Klausur(List<String> tasks) {
        this.tasks = tasks;
    } 

    public Klausur() {
        this.tasks = new ArrayList<>();
    } 

    public List<String> getTasks() {
        return this.tasks;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("[");
        for (int i = 0; i < tasks.size(); i++) {
            String s = tasks.get(i);
            if (i == tasks.size() - 1) {
                result.append(s);
                break;
            }
            result.append(s + ", ");
        }
        result.append("]");
        return result.toString();
    }
}
