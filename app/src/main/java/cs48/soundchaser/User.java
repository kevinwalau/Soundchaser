package cs48.soundchaser;

/**
 *
 */
public class User {
    private String name, dobMonth, dobDay, dobYear,
                   heightFt, heightIn, weight;

    public User(String _name, String _dobMonth, String _dobDay, String _dobYear,
                String _heightFt, String _heightIn, String _weight) {
        name = _name;
        dobMonth = _dobMonth;
        dobDay = _dobDay;
        dobYear = _dobYear;
        heightFt = _heightFt;
        heightIn = _heightIn;
        weight = _weight;
    }

    public void setName(String _name) {
        name = _name;
    }

    public String getName() {
        return name;
    }

    public void setDobMonth(String _dobMonth) {
        dobMonth = _dobMonth;
    }

    public String getDobMonth() {
        return dobMonth;
    }

    public void setDobDay(String _dobDay) {
        dobDay = _dobDay;
    }

    public String getDobDay() {
        return dobDay;
    }

    public void setDobYear(String _dobYear) {
        dobYear = _dobYear;
    }

    public String getDobYear() {
        return dobYear;
    }

    public void setHeightFt(String _heightFt) {
        heightFt = _heightFt;
    }

    public String getHeightFt() {
        return heightFt;
    }

    public void setHeightIn(String _heightIn) {
        heightIn = _heightIn;
    }

    public String getHeightIn() {
        return heightIn;
    }

    public void setWeight(String _weight) {
        weight = _weight;
    }

    public String getWeight() {
        return weight;
    }
}
