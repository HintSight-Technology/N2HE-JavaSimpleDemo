import com.hintsight.n2he.FacialVerification;
import com.hintsight.n2he.LogisticRegression;

public class Main {
    public static void main(String[] args) {
        // Facial Verification
//        FacialVerification.verify();

        // Logistic Regression
        int numberOfUsers = 6000;
        int numberOfRows = 25;
        int id = 88; //can be modified to test other id 
        String filepath = "src/X_test.csv";
        LogisticRegression.predict(filepath, numberOfUsers, numberOfRows, id);
    }
}
