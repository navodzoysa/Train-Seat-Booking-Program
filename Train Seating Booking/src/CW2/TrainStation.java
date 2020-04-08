package CW2;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.bson.Document;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TrainStation extends Application {
    private Passenger[] waitingRoom = new Passenger[42];
    private PassengerQueue trainQueue = new PassengerQueue();

    public static void main(String[] args) {
        launch(args);
    }

    public void consoleMenu(Stage stage, Pane root, Scene scene) throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);

        // List created to store both train details
        List<List<String>> customerDetails = new ArrayList<>();

        // A List created to store each stop in the colonbo to badulla train
        List<String> stationStops = Arrays.asList("Colombo Fort" , "Polgahawela", "Peradeniya Junction", "Gampola",
                "Nawalapitiya", "Hatton", "Talawakelle", "Nanu Oya", "Haputale", "Diyatalawa", "Bandarawela",
                "Ella", "Badulla");

        ArrayList<String> stationDetails = new ArrayList<>(Arrays.asList("0","0","0"));

        while(true){
            System.out.println(
                    "\nWelcome To Sri Lanka Railways Department\n" +
                    "Denuwara Menike Intercity Express Train departure from Colombo to Badulla / Badulla to Colombo\n" +
                    "\nPlease enter 'A' to add passengers from waiting room to the train queue\n" +
                    "Please enter 'V' to view waiting room, train queue and boarded passengers\n" +
                    "Please enter 'D' to delete customer from train queue\n" +
                    "Please enter 'S' to store the train queue data to a file or database\n" +
                    "Please enter 'L' to load the train queue data from a file or database\n" +
                    "Please enter 'R' to run the simulation and produce report\n" +
                    "Please enter 'Q' to quit the program");

            String userInput = scanner.next().toUpperCase();
            // Switch case used to check which inputs were taken
            switch(userInput){
                /* For add, view and empty welcomeScreen is used to select route, destination and date. Then inside
                   trainDestination the relevant methods for adding, viewing and viewing only empty seats are called. */
                case "A":
                    int roomPassengers = 42;
                    for(Object item : waitingRoom){
                        if(item==null){
                            roomPassengers --;
                        }
                    }
                    if(roomPassengers==0) {
                        selectStation(stage, userInput, stationStops, stationDetails);
                        loadCustomersFromBooking(scanner, stationDetails, customerDetails);
                    }
                    addPassenger();
                    break;
                case "V":
                    selectStation(stage, userInput, stationStops, stationDetails);
                    viewPassenger();
                    break;
                case "D":
                    deletePassenger();
                    break;
                case "S":
                    saveTrainQueue();
                    break;
                case "L":
                    loadTrainQueue();
                    break;
                case "R":
                    runSimulation();
                    break;
                case "Q":
                    System.exit(0);
                default:
                    System.out.println("Invalid input! Please enter a valid input");
                    break;
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {
        Stage stage = new Stage();
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #1b87c2");
        Scene scene = new Scene(root, 1000, 500);    // Size of the window
        stage.setTitle("Train Station Queue Application");
        consoleMenu(stage, root, scene);
    }

    public void selectStation(Stage stage, String userInput, List<String> stationStops, ArrayList<String> stationDetails){
        Label title = new Label("Welcome to Sri Lanka Railways Department");
        title.setStyle("-fx-font: 30 arial; -fx-font-weight: bold; -fx-text-fill: black");
        title.setLayoutX(95);
        title.setLayoutY(5);

        Label details = new Label(
                "Train name - Denuwara Menike\n" +
                "Class - 1st Class A/C Compartment\n" +
                "Train number - Colombo to Badulla (1001)\n" +
                "Train number - Badulla to Colombo (1002)\n");
        details.setStyle("-fx-font: 18 arial; -fx-text-fill: black; -fx-font-weight: bold");
        details.setLayoutX(220);
        details.setLayoutY(100);

        Label information = new Label("Please select a date and station to view the train station queue");
        information.setStyle("-fx-font: 16 arial; -fx-text-fill: black;");
        information.setLayoutX(180);
        information.setLayoutY(200);

        // Default value set to the systems local date
        DatePicker selectDate = new DatePicker(LocalDate.now());
        selectDate.setPrefSize(150, 40);
        selectDate.setLayoutX(120);
        selectDate.setLayoutY(250);
        // Making the manual entry of dates unavailable which helps validation
        selectDate.setEditable(false);

        /*  Restricting past dates that can be selected from the DatePicker UI element compared to the local date of
            the system  */
        selectDate.setDayCellFactory(restrictDate -> new DateCell(){
            @Override
            public void updateItem(LocalDate item, boolean empty){
                super.updateItem(item, empty);
                LocalDate presentDay = LocalDate.now();

                // If the date in DatePicker is older than current local date disable that particular date
                if(item.compareTo(presentDay)<0 && userInput.equals("A")) {
                    setDisable(true);
                    setStyle("-fx-background-color: red");
                }
            }
        });

        // Dropdown list of trains
        ComboBox<String> train = new ComboBox<>();
        train.setPromptText("Train");
        train.setPrefSize(150, 40);
        train.setLayoutX(320);
        train.setLayoutY(250);
        train.getItems().add("1001");
        train.getItems().add("1002");

        // Dropdown list of stops from the starting location
        ComboBox<String> station = new ComboBox<>();
        station.setPromptText("Station");
        station.setPrefSize(150, 40);
        station.setLayoutX(520);
        station.setLayoutY(250);

        for(String item : stationStops){
            station.getItems().add(item);
        }

        Button confirmStation = new Button("Confirm");
        confirmStation.setPrefSize(150, 40);
        confirmStation.setLayoutX(320);
        confirmStation.setLayoutY(350);

        confirmStation.setOnAction(event -> {
            String date = selectDate.getValue().toString();
            String selectedTrain = train.getSelectionModel().getSelectedItem();
            String selectedStation = station.getSelectionModel().getSelectedItem();

            if(selectedTrain != null && selectedStation != null){
                stationDetails.set(0,date);
                stationDetails.set(1,selectedTrain);
                stationDetails.set(2,selectedStation);
            }
            else{
                Alert noSelection = new Alert(Alert.AlertType.WARNING);
                noSelection.setTitle("No Selection Detected");
                noSelection.setHeaderText("Warning! No Option selected!");
                noSelection.setContentText("Please select a date, train and a station! Try again.");
                noSelection.showAndWait();
            }
            stage.close();
        });

        Pane root1 = new Pane();
        root1.setStyle("-fx-background-color: #1b87c2");
        root1.getChildren().addAll(title, details, information, selectDate, train, station, confirmStation);
        Scene scene1 = new Scene(root1, 820, 500);
        stage.setScene(scene1);
        stage.showAndWait();
    }

    public void loadCustomersFromBooking(Scanner scanner, ArrayList<String> stationDetails, List<List<String>> customerDetails) throws FileNotFoundException {
        if(!stationDetails.contains("0")) {
            while (true) {
                scanner.nextLine();
                System.out.println("Please select from where you would like to load the customer details. Text file(T)/ Database(D) : ");
                String choice = scanner.nextLine().toUpperCase();
                if (choice.equals("T")) {
                    Scanner read = new Scanner(new File("src/CW1/customerData.txt"));
                    if (!read.hasNextLine()) {
                        // If the file is empty gives an error
                        System.out.println("Error file is empty! Please save booking data to file before loading");
                    } else {
                        // If the file already has data execute the code block below
                        while (read.hasNextLine()) { // Checks if each line has data Add each line to the variable line
                            String line = read.nextLine();
                            // Uses a string array to get each set of characters separated by "/" and the output looks like[Date, Start location,
                            // Destination, Seat number, Name]
                            String[] holdDetails = line.split(",");
                            // Creates a new List called details and adds each element from holdDetails up to 7 elements each time
                            List<String> details = new ArrayList<>(Arrays.asList(holdDetails).subList(0, 8));
                            if (details.get(5).equals(stationDetails.get(0)) && details.get(0).equals(stationDetails.get(1)) &&
                                    details.get(6).equals(stationDetails.get(2))) {
                                // Add each details List to customerDetails List
                                customerDetails.add(details);
                            }
                        }
                    }
                    System.out.println(customerDetails);
                    read.close();
                    break;
                } else if (choice.equals("D")) {
                    //Connecting to MongoDB then creating a database and then two collections for each train route
                    MongoClient mongoClient = new MongoClient("localhost", 27017);
                    MongoDatabase customerDatabase = mongoClient.getDatabase("customers");
                    MongoCollection<Document> colomboCollection = customerDatabase.getCollection("colomboDetails");
                    MongoCollection<Document> badullaCollection = customerDatabase.getCollection("badullaDetails");
                    System.out.println("Connected to the Database");

                    // Gets all the documents in colomboCollection train route into findColomboDocument
                    FindIterable<Document> findColomboDocument = colomboCollection.find();
                    // Gets all the documents in badullaCollection train route into findBadullaDocument
                    FindIterable<Document> findBadullaDocument = badullaCollection.find();

                    // Loops through each document in colomboColletion and adds each value from the keys to colomboCustomers
                    // and colomboBadullaDetails List
                    for (Document document : findColomboDocument) {
                        List<String> details = new ArrayList<>();
                        details.add(document.getString("train"));
                        details.add(document.getString("seat"));
                        details.add(document.getString("NIC"));
                        details.add(document.getString("firstname"));
                        details.add(document.getString("surname"));
                        details.add(document.getString("date"));
                        details.add(document.getString("from"));
                        details.add(document.getString("to"));
                        if (details.get(5).equals(stationDetails.get(0)) && details.get(0).equals(stationDetails.get(1)) &&
                                details.get(6).equals(stationDetails.get(2))) {
                            // Add each details List to customerDetails List
                            customerDetails.add(details);
                        }
                    }
                    // Loops through each document in badullaColletion and adds each value from the keys to badullaCustomers
                    // and colomboBadullaDetails List
                    for (Document document : findBadullaDocument) {
                        List<String> details = new ArrayList<>();
                        details.add(document.getString("train"));
                        details.add(document.getString("seat"));
                        details.add(document.getString("NIC"));
                        details.add(document.getString("firstname"));
                        details.add(document.getString("surname"));
                        details.add(document.getString("date"));
                        details.add(document.getString("from"));
                        details.add(document.getString("to"));
                        if (details.get(5).equals(stationDetails.get(0)) && details.get(0).equals(stationDetails.get(1)) &&
                                details.get(6).equals(stationDetails.get(2))) {
                            // Add each details List to customerDetails List
                            customerDetails.add(details);
                        }
                    }
                    mongoClient.close(); // Closes the database connection
                    System.out.println("Details loaded from the database successfully");
                    System.out.println(customerDetails);
                    break;
                } else {
                    System.out.println("Please enter a valid input and try again. Text file(T)/ Database(D).");
                }
            }
            addPassengerToWaitingRoom(customerDetails);
        }
    }

    public void addPassengerToWaitingRoom(List<List<String>> customerDetails){
        for(List<String> customer : customerDetails){
            Passenger passenger = new Passenger();
            passenger.setName(customer.get(3), customer.get(4));
            passenger.setOtherDetails(customer.get(0), customer.get(1), customer.get(2), customer.get(5), customer.get(6), customer.get(7));
            waitingRoom[customerDetails.indexOf(customer)] = passenger;
        }
    }

    public void addPassenger(){
        Stage stage = new Stage();
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1b87c2");
        Scene scene = new Scene(root, 1200, 800);    // Size of the window
        stage.setTitle("Train Station Queue Application");
        stage.setScene(scene);
        stage.showAndWait();
    }

    public void viewPassenger(){}

    public void deletePassenger(){}

    public void saveTrainQueue(){}

    public void loadTrainQueue(){}

    public void runSimulation(){}
}
