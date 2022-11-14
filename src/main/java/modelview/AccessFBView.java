package modelview;

import com.mycompany.mvvmexample.App;
import viewmodel.AccessDataViewModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.mycompany.mvvmexample.FirestoreContext;
import com.mycompany.mvvmexample.FirestoreContext;
import com.mycompany.mvvmexample.FirestoreContext;
import com.mycompany.mvvmexample.FirestoreContext;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Person;

public class AccessFBView implements Initializable {

 
     @FXML
    private TextField nameField;
    @FXML
    private TextField majorField;
    @FXML
    private TextField ageField;
    @FXML
    private Button writeButton;
    @FXML
    private Button readButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button updateButton;
    
    
    @FXML
    private TableView<Person> personTable;
    @FXML
    private TableColumn<Person, String> nameColumn;
    @FXML
    private TableColumn<Person, String> majorColumn;
    @FXML
    private TableColumn<Person, Integer> ageColumn;
    
    private boolean key;
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;
    public ObservableList<Person> getListOfUsers() {
        return listOfUsers;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Initialized...");
        AccessDataViewModel accessDataViewModel = new AccessDataViewModel();
        nameField.textProperty().bindBidirectional(accessDataViewModel.userNameProperty());
        majorField.textProperty().bindBidirectional(accessDataViewModel.userMajorProperty());
        writeButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());
        updateButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());
        
        nameColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("name"));
        majorColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("major"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<Person, Integer>("age"));
    }

    @FXML
    private void addRecord(ActionEvent event) {
        addData();
        clearFields();
    }
    
    @FXML
    private void switchScene(ActionEvent event) {
        addData();
    }
    
    @FXML
    private void regRecord(ActionEvent event) {
        registerUser();
    }

    
        @FXML
    private void writeRecord(ActionEvent event) {
        readFirebase();
    }
    
    public void registerUser()
    {
        CreateRequest request = new CreateRequest()
            .setEmail("user@example.com")
            .setEmailVerified(false)
            .setPassword("secretPassword")
            .setPhoneNumber("+11234567890")
            .setDisplayName("John Doe")
            .setPhotoUrl("http://www.example.com/12345678/photo.png")
            .setDisabled(false);
        
        try{
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            System.out.println("Successfully created new user: " + userRecord.getUid());
        }
        catch(FirebaseAuthException e){
            
        }
        
    }
    
    public void addData() {

        DocumentReference docRef = App.fstore.collection("References").document(UUID.randomUUID().toString());
        // Add document data  with id "alovelace" using a hashmap
        Map<String, Object> data = new HashMap<>();
        data.put("Name", nameField.getText());
        data.put("Major", majorField.getText());
        data.put("Age", Integer.parseInt(ageField.getText()));
        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);
    }
    
    public void clearFields(){
        nameField.clear();
        ageField.clear();
        majorField.clear();
    }
    
    public boolean readFirebase()
    {
        key = false;
        personTable.getItems().clear();

        //asynchronously retrieve all documents
        ApiFuture<QuerySnapshot> future =  App.fstore.collection("References").get();
        // future.get() blocks on response
        List<QueryDocumentSnapshot> documents;
        try 
        {
            documents = future.get().getDocuments();
            if(documents.size()>0)
            {
                System.out.println("Outing....");
                for (QueryDocumentSnapshot document : documents) 
                {
                    System.out.println(document.getId() + " => " + document.getData().get("Name"));
                    person  = new Person(String.valueOf(document.getData().get("Name")), 
                            document.getData().get("Major").toString(),
                            Integer.parseInt(document.getData().get("Age").toString()), document.getId());
                    listOfUsers.add(person);
                    
                }
            }
            else
            {
               System.out.println("No data"); 
            }
            key=true;
            personTable.setItems(listOfUsers);
        }
        catch (InterruptedException | ExecutionException ex) 
        {
             ex.printStackTrace();
        }
        return key;
    }
    
    @FXML
    private void deleteRecord(ActionEvent event) {
        doDeletion();
    }
    
    @FXML
    private void updateRecord(ActionEvent event) {
        doUpdate();
        clearFields();
    }
    
    void doDeletion()
    {
        Person selectedItem = personTable.getSelectionModel().getSelectedItem();
        personTable.getItems().remove(selectedItem);
        ApiFuture<WriteResult> writeResult =  App.fstore.collection("References").document(selectedItem.id).delete();
         try {
             System.out.println("Update time : " + writeResult.get().getUpdateTime());
         } catch (InterruptedException ex) {
             Logger.getLogger(AccessFBView.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ExecutionException ex) {
             Logger.getLogger(AccessFBView.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    
    void doUpdate()
    {
        Person selectedItem = personTable.getSelectionModel().getSelectedItem();
        selectedItem.setName(nameField.getText());
        selectedItem.setMajor(majorField.getText());
        selectedItem.setAge(Integer.parseInt(ageField.getText()));
        
        
        DocumentReference docRef = App.fstore.collection("References").document(selectedItem.id);
        // Add document data  with id "alovelace" using a hashmap
        Map<String, Object> data = new HashMap<>();
        data.put("Name", nameField.getText());
        data.put("Major", majorField.getText());
        data.put("Age", Integer.parseInt(ageField.getText()));
        //asynchronously write data
        ApiFuture<WriteResult> writeResult = docRef.set(data);
        try {
             System.out.println("Update time : " + writeResult.get().getUpdateTime());
         } catch (InterruptedException ex) {
             Logger.getLogger(AccessFBView.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ExecutionException ex) {
             Logger.getLogger(AccessFBView.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
}
