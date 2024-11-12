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

//        List<String[]> list = new ArrayList<>();
//        Path path = null;
//        try {
//            path = Paths.get(ClassLoader.getSystemResource("UCI_Credit_Card.csv").toURI());
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//        try (Reader reader = Files.newBufferedReader(path)) {
//            try (CSVReader csvReader = new CSVReader(reader)) {
//                String[] line;
//                while ((line = csvReader.readNext()) != null) {
//                    list.add(line);
//                }
//            } catch (CsvValidationException e) {
//                throw new RuntimeException(e);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        System.out.println(list.toString());

    }
}