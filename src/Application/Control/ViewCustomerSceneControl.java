package Application.Control;

import Application.DataTypes.Customer;
import DataAccess.CustomerData;
import Presentation.ViewCustomersScene;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

/**
 * Created by Administrator on 5/22/2016.
 */

public class ViewCustomerSceneControl {

    //fields
    private static TableView<Customer> table;
    private static TextField search;
    private static ObservableList<Customer> customers;
    private static ObservableList<Customer> tableItems;
    private static Button backB, addB, editB;


    //initialize fields
    public static void initialize(){
        //table
        table = ViewCustomersScene.getTable();
        table.setItems(CustomerData.getCustomers());


        //backB
        backB = ViewCustomersScene.getBackB();
        backB.setOnAction(e -> handle_backB());

        //addB
        addB = ViewCustomersScene.getAddB();
        addB.setOnAction(e -> handle_addB());

        //editB
        editB = ViewCustomersScene.getEditB();
        editB.setOnAction(e -> handle_editB());

        // search field
        search = ViewCustomersScene.getSearch();
        customers = table.getItems(); //set search arrayList items
        initializeSearch();
    }


    //add button action
    public static void handle_addB() {
        Customer customer = new Customer();
        boolean okPressed = MainControl.showCustomerEditScene(customer);

        if(okPressed) {
            customer = CustomerEditSceneControl.getCustomer();
            CustomerData.insertCustomer(customer); //add customer to database

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(MainControl.getWindow());
            alert.setContentText("Customer added!");
            alert.showAndWait();

            System.out.println("new customer added");
        }
    }


    //edit button action
    public static void handle_editB(){
        Customer customer = table.getSelectionModel().getSelectedItem();

        if(customer != null) {
            boolean okPressed = MainControl.showCustomerEditScene(customer);

            if(okPressed) {
                customer = CustomerEditSceneControl.getCustomer();
                CustomerData.updateCustomer(customer); //edit customer in database

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.initOwner(MainControl.getWindow());
                alert.setContentText("Customer edited!");
                alert.showAndWait();

                System.out.println("a customer edited");
            }
        }

        else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(MainControl.getWindow());
            alert.setHeaderText("Select customer!");
            alert.setContentText("No customer selected!");
            alert.showAndWait();
        }
    }


    //search bar setup
    public static void initializeSearch(){
        search.textProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (search.textProperty().get().isEmpty()) {
                    table.setItems(customers);
                    return;
                }

                tableItems = FXCollections.observableArrayList();

                for(Customer c : customers){
                    if(c.getFirst_name().toUpperCase().contains(search.getText().toUpperCase())||
                       c.getLast_name().toUpperCase().contains(search.getText().toUpperCase())||
                            c.getPassport_number().toUpperCase().contains(search.getText().toUpperCase())||
                            c.getPhone_nr().contains(search.getText())){

                        tableItems.add(c);
                    }
                }

                table.setItems(tableItems);
            }

        });
    }


    //back button action
    public static void handle_backB(){ MainControl.showMenuScene(); }

}
