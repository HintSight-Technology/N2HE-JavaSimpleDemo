import com.hintsight.n2he.FacialVerification;
import com.hintsight.n2he.LogisticRegression;

public class Main {
    public static void main(String[] args) {
//        FacialVerification.verify();

        int numberOfUsers = 6000;
        int numberOfRows = 25;
        int id = 14;
        String filepath = "src/X_test.csv";
        LogisticRegression.predict(filepath, numberOfUsers, numberOfRows, id);
    }
}
