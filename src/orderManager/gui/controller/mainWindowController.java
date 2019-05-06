package orderManager.gui.controller;
import com.jfoenix.controls.JFXProgressBar;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import orderManager.be.DepartmentTask;
import orderManager.be.Worker;
import orderManager.bll.mainLogicClass;
import orderManager.dal.jsonReader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class mainWindowController implements Initializable {

    public AnchorPane mainPane;
    public Label dateLabel;
    public JFXProgressBar estimatedProgressBar;
    public Text estimatedProgressLabel;
    public TableView workersTable;
    public TableColumn typeCol;
    public TableColumn initialsCol;
    public TableColumn nameCol;
    public TableColumn salaryCol;
    public TableColumn idCol;


    private ScheduledExecutorService executor;
    private DepartmentTask actualDepartmentTask;
    private mainLogicClass mainLogic;
    private List<Worker> workersList;
    private ObservableList<Worker> observableWorkers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            mainLogic = new mainLogicClass();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        displayTime();
        refresh();
    }

    private void refresh() {
        Runnable runnable = () -> {
            Platform.runLater(() -> {
                try {
                    workersList = mainLogic.getWorkers();
                    observableWorkers = FXCollections.observableArrayList(workersList);
                    prepareWorkersTable();
                    calculateEstimatedProgress();
                } catch (ParseException | SQLException e) {
                    e.printStackTrace();
                }
            });
        };
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(2);
        ses.scheduleWithFixedDelay(runnable, 0, 5, TimeUnit.SECONDS);
    }

    public void displayTime() {
        Runnable thread = () -> setTime();
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(thread, 0, 1, TimeUnit.SECONDS);
    }

    public void setTime() {
        Platform.runLater(() -> dateLabel.setText(String.valueOf(Calendar.getInstance().getTime())));
    }

    public void prepareWorkersTable(){
        initialsCol.setCellValueFactory(new PropertyValueFactory<>("initials"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        workersTable.setItems(observableWorkers);
        workersTable.getColumns().clear();
        workersTable.getColumns().addAll(idCol,nameCol,initialsCol,salaryCol);
    }

    public void calculateEstimatedProgress() throws ParseException {

      //  its for later use when we will have start and end date
    /*
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date sDate = actualDepartmentTask.getStartTime();
        Date eDate = actualDepartmentTask.getEndDate();
        Instant instant = sDate.toInstant();
        Instant instant1 = eDate.toInstant();
        LocalDate startDate = instant.atZone(defaultZoneId).toLocalDate();
        LocalDate todaysDate = LocalDate.now();
        LocalDate endDate = instant1.atZone(defaultZoneId).toLocalDate();
        long daysBetweenStartAndEnd = ChronoUnit.DAYS.between(startDate, endDate);
        long daysBetweenStartAndNow = ChronoUnit.DAYS.between(startDate, todaysDate);
        double valOne = (double)daysBetweenStartAndEnd;
        double valTwo = (double)daysBetweenStartAndNow;
        double progress = valTwo/valOne;
        estimatedProgressLabel.setText((int)(progress*100)+"%");
        estimatedProgressBar.progressProperty().set(((double)progress));
*/
        //Its for now, raw data
        String startDateS = "2019-04-23";
        String endDateS = "2019-06-03";
        LocalDate startDate = LocalDate.parse(startDateS);
        LocalDate endDate = LocalDate.parse(endDateS);
        LocalDate todaysDate = LocalDate.now();
        long daysBetweenStartAndEnd = ChronoUnit.DAYS.between(startDate, endDate);
        long daysBetweenStartAndNow = ChronoUnit.DAYS.between(startDate, todaysDate);
        double valOne = (double)daysBetweenStartAndEnd;
        double valTwo = (double)daysBetweenStartAndNow;
        double progress = valTwo/valOne;
        estimatedProgressLabel.setText((int)(progress*100)+"%");
        estimatedProgressBar.progressProperty().set(progress);

    }



        @FXML
        private void clickToPickFile(ActionEvent event) throws IOException, SQLException // While creating/editing a song we are using this button to pick path of the song.
        {
            FileDialog fd = new FileDialog(new JFrame());
            fd.setFile("*.json");
            fd.setVisible(true);
            File[] f = fd.getFiles();
            if (f.length > 0) {
                String fullPath = f[0].toString();
                int index = f[0].toString().lastIndexOf('\\');
                String finalPath = fullPath.substring(index+1,fullPath.length());
                mainLogic.readFile(finalPath);

            }
        }

}
