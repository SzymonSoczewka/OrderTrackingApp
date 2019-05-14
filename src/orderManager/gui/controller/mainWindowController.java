package orderManager.gui.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javax.swing.JFrame;

import orderManager.be.*;
import orderManager.bll.mainLogicClass;
import orderManager.gui.model.Model;
import orderManager.windowOpener;

public class mainWindowController implements Initializable, Observer {

    public AnchorPane mainPane;
    public Label dateLabel;
    public JFXProgressBar estimatedProgressBar;
    public Text estimatedProgressLabel;
    public JFXButton departmentBtn;
    public JFXTreeTableView workersTab;
    public JFXTreeTableView ordersTab;


    private ScheduledExecutorService executor;
    private mainLogicClass mainLogic;
    private ObservableList<Worker> observableWorkers;
    private ObservableList<OrderDetails> observableOrders;
    private IDepartment chosenDepartment;
    private Model model;
    private String taskWindowPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            model = Model.getInstance();
            mainLogic = new mainLogicClass();
            chosenDepartment = model.getDepartment();
            departmentBtn.setText(chosenDepartment.getName());
      /*
      mainLogic.addObserver(this);
      workersList = mainLogic.getWorkers();
      observableWorkers = FXCollections.observableArrayList(workersList);
      prepareWorkersTable();
       */
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        displayTime();
        refresh();
        taskWindowPath = "gui/view/taskWindow.fxml";
    }

    private void refresh() {
        Runnable runnable = () -> {
            Platform.runLater(() -> {
                try {
                    // workersList = mainLogic.getWorkers();
                    // observableWorkers = FXCollections.observableArrayList(workersList);
                    observableWorkers = (FXCollections.observableArrayList((List<Worker>) (List) mainLogic.getWorkers()));
                    observableOrders = model.obsOrdDet();
                    prepareWorkersTable();
                    prepareOrdersTable();
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

    public void prepareWorkersTable() {
        if (workersTab.getColumns().isEmpty()) {
            JFXTreeTableColumn<Worker, String> initialsCol = new JFXTreeTableColumn<>("Initials");
            prepareColumn(initialsCol,"initials",145);

            JFXTreeTableColumn<Worker, String> nameCol = new JFXTreeTableColumn<>("Name");
            prepareColumn(nameCol,"name",231);

            JFXTreeTableColumn<Worker, String> salaryCol = new JFXTreeTableColumn<>("SalaryNumber");
            prepareColumn(salaryCol,"salary",115);

            JFXTreeTableColumn<Worker, String> idCol = new JFXTreeTableColumn<>("ID");
            prepareColumn(idCol,"id",36);

            workersTab.getColumns().addAll(idCol, nameCol, initialsCol, salaryCol);
        }
        setWorkersTable();

    }

    private void setWorkersTable() {
        TreeItem<Worker> root = new RecursiveTreeItem<>(observableWorkers,
                RecursiveTreeObject::getChildren);
        workersTab.setRoot(root);
        workersTab.setShowRoot(false);
    }


    public void prepareOrdersTable() throws SQLException {
        if (ordersTab.getColumns().isEmpty()) {
            JFXTreeTableColumn<OrderDetails, String> orderNumber = new JFXTreeTableColumn<>("Order Number");
            prepareColumn(orderNumber,"orderNumber",145);


            JFXTreeTableColumn<OrderDetails, Date> startDate = new JFXTreeTableColumn<>("Start Date");
            prepareColumn(startDate,"startDate",145);

            JFXTreeTableColumn<OrderDetails, Date> endDate = new JFXTreeTableColumn<>("End Date");
            prepareColumn(endDate,"endDate",145);

            JFXTreeTableColumn<OrderDetails, Boolean> orderStatus = new JFXTreeTableColumn<>("Order Status");
            prepareColumn(orderStatus,"orderState",92);

            ordersTab.getColumns().addAll(orderNumber, startDate, endDate, orderStatus);
        }

        setOrdersTable();
    }
    private void setOrdersTable() {
        TreeItem<OrderDetails> root = new RecursiveTreeItem<>(observableOrders, RecursiveTreeObject::getChildren);
        ordersTab.setRoot(root);
        ordersTab.setShowRoot(false);
    }


    public void prepareColumn(JFXTreeTableColumn colName,String attributeName, int colWidth){
        colName.setCellValueFactory(new TreeItemPropertyValueFactory<>(attributeName));
        colName.setMinWidth(colWidth);
        colName.setStyle("-fx-alignment: CENTER");
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
        double valOne = (double) daysBetweenStartAndEnd;
        double valTwo = (double) daysBetweenStartAndNow;
        double progress = valTwo / valOne;
        estimatedProgressLabel.setText((int) (progress * 100) + "%");
        estimatedProgressBar.progressProperty().set(progress);

    }


    @FXML
    private void clickToPickFile(ActionEvent event)
            throws IOException, SQLException // While creating/editing a song we are using this button to pick path of the song.
    {
        FileDialog fd = new FileDialog(new JFrame());
        fd.setFile("*.json");
        fd.setVisible(true);
        File[] f = fd.getFiles();
        if (f.length > 0) {
            String fullPath = f[0].toString();
            int index = f[0].toString().lastIndexOf('\\');
            String finalPath = fullPath.substring(index + 1);
            mainLogic.readFile(finalPath);

        }
    }

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    observableWorkers = (FXCollections.observableArrayList((List<Worker>) (List) mainLogic.getWorkers()));
                    prepareWorkersTable();
                    calculateEstimatedProgress();
                } catch (ParseException | SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void doubleClickDetails(MouseEvent mouseEvent) throws IOException {
        if(mouseEvent.getClickCount()==2) {
            new windowOpener(taskWindowPath,682,446,true);
          //  RecursiveTreeItem<OrderDetails> od = (RecursiveTreeItem<OrderDetails>) ordersTab.getSelectionModel().getSelectedItem();
          //  model.setSelectedOrderNumber(od.);
            loadOrderTasks();
        }
    }

    private void loadOrderTasks() {
    }
}