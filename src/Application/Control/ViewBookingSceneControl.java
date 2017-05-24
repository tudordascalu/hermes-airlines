package Application.Control;

import Application.DataTypes.*;
import DataAccess.*;
import Presentation.ViewBookingScene;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by Petru on 26-May-16.
 */

public class ViewBookingSceneControl {

    //fields
    private static TableView<BookingTable> table;
    private static Label departure_dateObs;
    private static Label arrival_dateObs;
    private static Label categoryObs;
    private static Label priceObs;

    private static Label first_nameObs;
    private static Label last_nameObs;
    private static Label ageObs;
    private static Label passportObs;
    private static Label phone_numberObs;

    private static TextField search;
    private static Button add_bookingButton;
    private static Button cancelButton;
    private static Button backButton;
    private static Button editButton;

    private static ObservableList<BookingTable> bookings, tableItems;
    private static double refund;


    //initialize method
    public static void initialize() {

        //table
        table = ViewBookingScene.getTable();
        table.setItems(BookingTableData.getBookingTableItems());
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> displayBookingInfo(newValue));

        //flight info labels
        departure_dateObs = ViewBookingScene.getDeparture_dateObs();
        arrival_dateObs = ViewBookingScene.getArrival_dateObs();
        categoryObs = ViewBookingScene.getCategoryObs();
        priceObs = ViewBookingScene.getPriceObs();

        //customer info labels
        first_nameObs = ViewBookingScene.getFirst_nameObs();
        last_nameObs = ViewBookingScene.getLast_nameObs();
        ageObs = ViewBookingScene.getAgeObs();
        passportObs = ViewBookingScene.getPassportObs();
        phone_numberObs = ViewBookingScene.getPhone_numberObs();

        //buttons
        add_bookingButton = ViewBookingScene.getAdd_bookingButton();
        add_bookingButton.setOnAction(e -> handle_addButton());

        cancelButton = ViewBookingScene.getCancelButton();
        cancelButton.setOnAction(e -> handle_cancelButton());

        backButton = ViewBookingScene.getBackButton();
        backButton.setOnAction(e -> handle_backButton());

        editButton = ViewBookingScene.getEditButton();
        editButton.setOnAction(e -> handle_editButton());


        //search field setup
        search = ViewBookingScene.getSearchField();
        bookings = table.getItems();
        initializeSearch();
    }


    //back button action
    public static void handle_backButton() {
        MainControl.showMenuScene();
    }


    //add button action
    public static void handle_addButton(){
        BookingTable bookingTable = new BookingTable();
        Booking booking = new Booking();

        boolean okPressed = MainControl.showBookingEditScene(bookingTable, booking);

        if (okPressed) {
            booking = BookingEditSceneControl.getBooking();
            BookingData.insertBooking(booking); //add booking to database

            table.setItems(BookingTableData.getBookingTableItems()); //set the table items
            bookings = table.getItems();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(MainControl.getWindow());
            alert.setContentText("Booking added!");
            alert.showAndWait();

            System.out.println("new booking added");
        }

    }


    //edit button action
    public static void handle_editButton(){
        BookingTable bookingTable = table.getSelectionModel().getSelectedItem();
        Booking booking = new Booking();

        if(bookingTable != null) {
            //assuming that the customer is going to change the ticket category
            for(Flight f: FlightData.getFlight()){
                if(booking.getFlight_id()==f.getFlight_id())
                    if(booking.getFare_class().equalsIgnoreCase("first class"))
                        f.setFirst_class_left(f.getFirst_class_left() + 1);
                    else if(booking.getFare_class().equalsIgnoreCase("coach"))
                        f.setCoach_left(f.getCoach_left() + 1);
                    else f.setEconomy_left(f.getEconomy_left() + 1);
                FlightData.updateFlight(f);
                break;
            }

            boolean okPressed = MainControl.showBookingEditScene(bookingTable, booking);

            if (okPressed) {
                booking = BookingEditSceneControl.getBooking();
                BookingData.updateBooking(booking); //edit booking in database

                table.setItems(BookingTableData.getBookingTableItems()); //set the table items
                table.getSelectionModel().select(bookingTable);
                bookings = table.getItems();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.initOwner(MainControl.getWindow());
                alert.setContentText("Booking edited!");
                alert.showAndWait();

                System.out.println("a booking edited");
            }
        }

        else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(MainControl.getWindow());
            alert.setHeaderText("Select booking!");
            alert.setContentText("No booking selected!");
            alert.showAndWait();
        }
    }


    //remove button action
    public static void handle_cancelButton() {
        BookingTable bookingTable = table.getSelectionModel().getSelectedItem();
        Booking booking = new Booking();

        if (bookingTable != null) {
            refund = 0.0;
            for (Booking b : BookingData.getBookings())
                if (bookingTable.getBooking_id() == b.getBooking_id()) {
                    booking = b;
                    break;
                }

            //no booking can be canceled with less than 2 days before flight
            if (LocalDate.parse(bookingTable.getDeparture_date()).isBefore(LocalDate.now().plusDays(2))) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.initOwner(MainControl.getWindow());
                alert.setHeaderText("Cancel not possible");
                alert.setContentText("It is impossible to cancel a booking with less than 2 days before flight");
                alert.showAndWait();
            }

            else { //set the refund amount
                for (Flight f : FlightData.getFlight())
                    if (f.getFlight_id() == booking.getFlight_id()) {
                        if (booking.getFare_class().equalsIgnoreCase("First class"))
                                refund = f.getPrice() + f.getPrice() * 1/2;
                        else if (booking.getFare_class().equalsIgnoreCase("Coach"))
                            refund = (f.getPrice() + f.getPrice() * 1/4) * 85/100;
                        else if (LocalDate.parse(bookingTable.getDeparture_date()).isBefore(LocalDate.now().plusWeeks(2)))
                            refund = 0;
                        else
                            refund = f.getPrice() * 70/100;

                        break;
                    }

                cancelBooking(bookingTable,booking);
            }
        }

        else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.initOwner(MainControl.getWindow());
                alert.setHeaderText("Select booking!");
                alert.setContentText("No booking selected!");
                alert.showAndWait();
            }
    }


    //method to cancel a booking
    public static void cancelBooking(BookingTable bookingTable, Booking booking) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(MainControl.getWindow());
        alert.setHeaderText("Remove booking");
        alert.setContentText("Are you sure you want to cancel " + bookingTable.getCustomer() + "'s booking?" +
                "\nThe refund will be " + refund + " kr.");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) { //confirmed

            BookingData.deleteBooking(booking); //delete booking from database

            table.setItems(BookingTableData.getBookingTableItems()); //set table items
            bookings = table.getItems();

            Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION);
            alert1.initOwner(MainControl.getWindow());
            alert1.setContentText("Booking canceled!\n"
                    + bookingTable.getCustomer() + " was refunded " + refund + " kr.");
            alert1.showAndWait();

            //set left seats
            for (Flight f : FlightData.getFlight())
                if (f.getFlight_id() == booking.getFlight_id()) {
                    if (booking.getFare_class().equalsIgnoreCase("First class"))
                        f.setFirst_class_left(f.getFirst_class_left() + 1);
                    else if (booking.getFare_class().equalsIgnoreCase("Coach"))
                        f.setCoach_left(f.getCoach_left() + 1);
                    else
                        f.setEconomy_left(f.getEconomy_left() + 1);
                }

            System.out.println("a booking removed");
        }
        else {
            alert.close();
        }
    }


    //method to display the booking details
    public static void displayBookingInfo(BookingTable buk) {
        if(buk != null) {

            Booking booking = new Booking();
            for(Booking b : BookingData.getBookings())
                if(b.getBooking_id() == buk.getBooking_id()) {
                    booking = b;
                    break;
                }

            FlightTable flight = new FlightTable();
            for(FlightTable f : FlightTableData.getFlightTableItems())
                if(f.getFlight_id() == booking.getFlight_id()) {
                    flight = f;
                    break;
                }

            Customer customer = new Customer();
            for(Customer c : CustomerData.getCustomers())
                if(c.getCustomer_id() == booking.getCustomer_id()) {
                    customer = c;
                    break;
                }


            departure_dateObs.setText(flight.getDeparture_date() + ", " + flight.getDeparture_city());
            arrival_dateObs.setText(flight.getDeparture_date() + ", " + flight.getArrival_city());
            categoryObs.setText(booking.getFare_class());

            if(booking.getFare_class().equalsIgnoreCase("first class"))
            priceObs.setText(String.valueOf(flight.getPrice()+flight.getPrice()*1/2) + " kr.");
            else if(booking.getFare_class().equalsIgnoreCase("coach"))
                priceObs.setText(String.valueOf((flight.getPrice()+flight.getPrice()*1/4) + " kr."));
            else
                priceObs.setText(String.valueOf(flight.getPrice() + " kr."));


            first_nameObs.setText(customer.getFirst_name());
            last_nameObs.setText(customer.getLast_name());
            ageObs.setText(String.valueOf(customer.getAge()));
            passportObs.setText(customer.getPassport_number());
            phone_numberObs.setText(customer.getPhone_nr());
        }

        else { //no selection
            departure_dateObs.setText("");
            arrival_dateObs.setText("");
            categoryObs.setText("");
            priceObs.setText("");
            first_nameObs.setText("");
            last_nameObs.setText("");
            ageObs.setText("");
            passportObs.setText("");
            phone_numberObs.setText("");
        }
    }


    //search bar setup
    public static void initializeSearch(){
        search.textProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (search.textProperty().get().isEmpty()) {
                    table.setItems(bookings);
                    return;
                }

                tableItems = FXCollections.observableArrayList();

                for(BookingTable b : bookings){
                    if(b.getRoute().toUpperCase().contains(search.getText().toUpperCase())||
                            b.getCustomer().toUpperCase().contains(search.getText().toUpperCase())) {

                        tableItems.add(b);
                    }
                }

                table.setItems(tableItems);
            }
        });
    }

}
